package com.xtranslate.ui.chat

import com.xtranslate.domain.ImageInput
import com.xtranslate.domain.OcrEngine
import com.xtranslate.domain.OcrResult
import com.xtranslate.domain.AudioInput
import com.xtranslate.domain.AudioOutput
import com.xtranslate.domain.SpeechRequest
import com.xtranslate.domain.SpeechToTextEngine
import com.xtranslate.domain.TextToSpeechEngine
import com.xtranslate.domain.Transcript
import com.xtranslate.domain.TranslationEngine
import com.xtranslate.domain.TranslationRequest
import com.xtranslate.domain.TranslationResult
import com.xtranslate.runtime.EngineCoordinator
import com.xtranslate.runtime.FakeOcrEngine
import com.xtranslate.runtime.FakeSpeechToTextEngine
import com.xtranslate.runtime.FakeTextToSpeechEngine
import com.xtranslate.runtime.FakeTranslationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests the chat workflow before the real AI engines are connected.
 *
 * These tests use fake engines to check that text and image translation add the
 * right messages to the chat screen.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {
    private val dispatcher: TestDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun sendTextAddsSourceAndTranslationMessages() =
        runTest {
            val viewModel = ChatViewModel(fakeCoordinator())

            viewModel.updateComposer("Hello")
            viewModel.sendText()
            advanceUntilIdle()

            assertEquals("", viewModel.state.value.composerText)
            assertEquals(2, viewModel.state.value.messages.size)
            assertEquals(ChatMessageKind.Source, viewModel.state.value.messages[0].kind)
            assertEquals(ChatMessageKind.Translation, viewModel.state.value.messages[1].kind)
        }

    @Test
    fun sendTextUsesSelectedTargetLanguage() =
        runTest {
            val viewModel = ChatViewModel(fakeCoordinator())

            viewModel.updateTargetLanguage("Japanese")
            viewModel.updateComposer("Hello")
            viewModel.sendText()
            advanceUntilIdle()

            val translation = viewModel.state.value.messages.last()
            assertEquals(ChatMessageKind.Translation, translation.kind)
            assertEquals("Japanese", translation.language)
            assertEquals("[Japanese] Hello", translation.text)
        }

    @Test
    fun translateImageAddsOcrReviewAndTranslationMessages() =
        runTest {
            val viewModel = ChatViewModel(fakeCoordinator())

            viewModel.translateImage(ImageInput("content://image"))

            val messages = viewModel.state.value.messages
            assertTrue(messages.any { it.kind == ChatMessageKind.OcrReview })
            assertTrue(messages.any { it.kind == ChatMessageKind.Translation })
        }

    @Test
    fun sendTextFailureAddsSystemMessageAndClearsBusy() =
        runTest {
            val viewModel =
                ChatViewModel(
                    fakeCoordinator(
                        translationEngine = FailingTranslationEngine("Missing translation model file"),
                    ),
                )

            viewModel.updateComposer("Hello")
            viewModel.sendText()
            advanceUntilIdle()

            assertEquals(false, viewModel.state.value.isBusy)
            assertTrue(
                viewModel.state.value.messages.any {
                    it.kind == ChatMessageKind.System &&
                        it.text.contains("Missing translation model file")
                },
            )
            assertEquals(AppTab.Models, viewModel.state.value.selectedTab)
        }

    @Test
    fun translateImageFailureAddsSystemMessageAndClearsBusy() =
        runTest {
            val viewModel =
                ChatViewModel(
                    fakeCoordinator(
                        ocrEngine = FailingOcrEngine("Missing OCR model file"),
                    ),
                )

            viewModel.translateImage(ImageInput("content://image"))

            assertEquals(false, viewModel.state.value.isBusy)
            assertTrue(
                viewModel.state.value.messages.any {
                    it.kind == ChatMessageKind.System &&
                        it.text.contains("Missing OCR model file")
                },
            )
            assertEquals(AppTab.Models, viewModel.state.value.selectedTab)
        }

    @Test
    fun nonModelFailureKeepsCurrentTab() =
        runTest {
            val viewModel =
                ChatViewModel(
                    fakeCoordinator(
                        translationEngine = FailingTranslationEngine("Network should not be used"),
                    ),
                )

            viewModel.updateComposer("Hello")
            viewModel.sendText()
            advanceUntilIdle()

            assertEquals(AppTab.Chat, viewModel.state.value.selectedTab)
        }

    @Test
    fun failureWithoutMessageShowsErrorType() =
        runTest {
            val viewModel =
                ChatViewModel(
                    fakeCoordinator(
                        translationEngine = NullMessageTranslationEngine(),
                    ),
                )

            viewModel.updateComposer("Hello")
            viewModel.sendText()
            advanceUntilIdle()

            assertTrue(
                viewModel.state.value.messages.any {
                    it.kind == ChatMessageKind.System &&
                        it.text.contains("Local AI error") &&
                        it.text.contains("RuntimeException")
                },
            )
        }

    @Test
    fun transcribeVoicePlaceholderTranscribesAndAutoTranslates() =
        runTest {
            val viewModel = ChatViewModel(fakeCoordinator())

            viewModel.transcribeVoicePlaceholder()
            advanceUntilIdle()

            assertEquals(false, viewModel.state.value.isBusy)
            val messages = viewModel.state.value.messages
            assertEquals(2, messages.size)
            assertEquals(ChatMessageKind.Source, messages[0].kind)
            assertEquals("Voice transcript", messages[0].text)
            assertEquals(ChatMessageKind.Translation, messages[1].kind)
        }

    @Test
    fun transcribeAudioTranscribesAndAutoTranslates() =
        runTest {
            val viewModel = ChatViewModel(fakeCoordinator())

            viewModel.transcribeAudio(AudioInput(uri = "recordings/latest.wav", durationMillis = 1000L))
            advanceUntilIdle()

            assertEquals(false, viewModel.state.value.isBusy)
            val messages = viewModel.state.value.messages
            assertEquals(2, messages.size)
            assertEquals(ChatMessageKind.Source, messages[0].kind)
            assertEquals("Voice transcript", messages[0].text)
            assertEquals(ChatMessageKind.Translation, messages[1].kind)
        }

    @Test
    fun transcribeVoicePlaceholderFailureAddsSystemMessage() =
        runTest {
            val viewModel =
                ChatViewModel(
                    fakeCoordinator(
                        sttEngine = FailingSpeechToTextEngine("Missing STT model file"),
                    ),
                )

            viewModel.transcribeVoicePlaceholder()
            advanceUntilIdle()

            assertEquals(false, viewModel.state.value.isBusy)
            assertEquals(AppTab.Models, viewModel.state.value.selectedTab)
            assertTrue(
                viewModel.state.value.messages.any {
                    it.kind == ChatMessageKind.System &&
                        it.text.contains("Missing STT model file")
                },
            )
        }

    @Test
    fun transcribeAudioFailureAddsSystemMessageAndKeepsCurrentTabForNonModelErrors() =
        runTest {
            val viewModel =
                ChatViewModel(
                    fakeCoordinator(
                        sttEngine = FailingSpeechToTextEngine("Missing audio file: cache/voice.wav"),
                    ),
                )

            viewModel.transcribeAudio(AudioInput(uri = "cache/voice.wav", durationMillis = 0L))
            advanceUntilIdle()

            assertEquals(false, viewModel.state.value.isBusy)
            assertEquals(AppTab.Chat, viewModel.state.value.selectedTab)
            assertTrue(
                viewModel.state.value.messages.any {
                    it.kind == ChatMessageKind.System &&
                        it.text.contains("Missing audio file: cache/voice.wav")
                },
            )
        }

    @Test
    fun speakTranslationPlaceholderAddsSystemMessage() =
        runTest {
            val viewModel = ChatViewModel(fakeCoordinator())
            val message =
                ChatMessage(
                    id = 1L,
                    kind = ChatMessageKind.Translation,
                    text = "Kamusta",
                    language = "Filipino",
                )

            viewModel.speakTranslationPlaceholder(message)
            advanceUntilIdle()

            assertEquals(false, viewModel.state.value.isBusy)
            assertTrue(
                viewModel.state.value.messages.any {
                    it.kind == ChatMessageKind.System &&
                        it.text.contains("Speech ready") &&
                        it.text.contains("memory://tts/Filipino")
                },
            )
        }

    @Test
    fun speakTranslationPlaceholderFailureAddsSystemMessage() =
        runTest {
            val viewModel =
                ChatViewModel(
                    fakeCoordinator(
                        ttsEngine = FailingTextToSpeechEngine("Missing Supertonic model file"),
                    ),
                )
            val message =
                ChatMessage(
                    id = 1L,
                    kind = ChatMessageKind.Translation,
                    text = "Hello",
                    language = "English",
                )

            viewModel.speakTranslationPlaceholder(message)
            advanceUntilIdle()

            assertEquals(false, viewModel.state.value.isBusy)
            assertEquals(AppTab.Models, viewModel.state.value.selectedTab)
            assertTrue(
                viewModel.state.value.messages.any {
                    it.kind == ChatMessageKind.System &&
                        it.text.contains("Missing Supertonic model file")
                },
            )
        }

    @Test
    fun translateVoiceTranscriptAddsSourceAndTranslationMessages() =
        runTest {
            val viewModel = ChatViewModel(fakeCoordinator())

            viewModel.translateVoiceTranscript("Hello world")
            advanceUntilIdle()

            assertEquals(false, viewModel.state.value.isBusy)
            val messages = viewModel.state.value.messages
            assertEquals(2, messages.size)
            assertEquals(ChatMessageKind.Source, messages[0].kind)
            assertEquals("Hello world", messages[0].text)
            assertEquals(ChatMessageKind.Translation, messages[1].kind)
        }

    @Test
    fun translateVoiceTranscriptIgnoresBlankInput() =
        runTest {
            val viewModel = ChatViewModel(fakeCoordinator())

            viewModel.translateVoiceTranscript("   ")

            assertEquals(false, viewModel.state.value.isBusy)
            assertTrue(viewModel.state.value.messages.isEmpty())
        }

    @Test
    fun showVoiceErrorAddsSystemMessage() =
        runTest {
            val viewModel = ChatViewModel(fakeCoordinator())

            viewModel.showVoiceError("Voice recording failed: Permission denied")

            assertEquals(false, viewModel.state.value.isBusy)
            assertTrue(
                viewModel.state.value.messages.any {
                    it.kind == ChatMessageKind.System &&
                        it.text == "Voice recording failed: Permission denied"
                },
            )
        }

    private fun fakeCoordinator(
        ocrEngine: OcrEngine = FakeOcrEngine(text = "Image text"),
        translationEngine: TranslationEngine = FakeTranslationEngine(),
        sttEngine: SpeechToTextEngine = FakeSpeechToTextEngine(),
        ttsEngine: TextToSpeechEngine = FakeTextToSpeechEngine(),
    ) =
        EngineCoordinator(
            ocrEngine = ocrEngine,
            translationEngine = translationEngine,
            sttEngine = sttEngine,
            ttsEngine = ttsEngine,
            lowMemoryMode = true,
        )
}

private class FailingTranslationEngine(
    private val message: String,
) : TranslationEngine {
    override suspend fun translate(request: TranslationRequest): TranslationResult {
        error(message)
    }
}

private class NullMessageTranslationEngine : TranslationEngine {
    override suspend fun translate(request: TranslationRequest): TranslationResult {
        throw RuntimeException()
    }
}

private class FailingOcrEngine(
    private val message: String,
) : OcrEngine {
    override suspend fun extractText(image: ImageInput): OcrResult {
        error(message)
    }
}

private class FailingSpeechToTextEngine(
    private val message: String,
) : SpeechToTextEngine {
    override suspend fun transcribe(audio: AudioInput): Transcript {
        error(message)
    }
}

private class QueueSpeechToTextEngine(
    private val results: List<Result<Transcript>>,
) : SpeechToTextEngine {
    private var index = 0

    override suspend fun transcribe(audio: AudioInput): Transcript =
        results[index++].getOrThrow()
}

private class FailingTextToSpeechEngine(
    private val message: String,
) : TextToSpeechEngine {
    override suspend fun synthesize(request: SpeechRequest): AudioOutput {
        error(message)
    }
}
