package com.xtranslate.runtime

import com.xtranslate.domain.AudioInput
import com.xtranslate.domain.ImageInput
import com.xtranslate.domain.OcrEngine
import com.xtranslate.domain.OcrResult
import com.xtranslate.domain.SpeechRequest
import com.xtranslate.domain.SpeechToTextEngine
import com.xtranslate.domain.TextToSpeechEngine
import com.xtranslate.domain.Transcript
import com.xtranslate.domain.TranslationEngine
import com.xtranslate.domain.TranslationRequest
import com.xtranslate.domain.TranslationResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * Tests the simple loading rules for AI engines.
 *
 * These tests check that text translation only loads the translator, and image
 * translation unloads OCR before translation when low-memory mode is enabled.
 */
class EngineCoordinatorTest {
    @Test
    fun textTranslationLoadsOnlyTranslator() =
        runTest {
            val coordinator =
                EngineCoordinator(
                    ocrEngine = FakeOcrEngine(),
                    translationEngine = FakeTranslationEngine(),
                    sttEngine = FakeSpeechToTextEngine(),
                    ttsEngine = FakeTextToSpeechEngine(),
                    lowMemoryMode = true,
                )

            coordinator.translateText(TranslationRequest("Hello", "English", "Filipino"))

            assertFalse(coordinator.loadedEngines.contains(LoadedEngine.Ocr))
            assertTrue(coordinator.loadedEngines.contains(LoadedEngine.Translation))
        }

    @Test
    fun imageTranslationUnloadsOcrBeforeTranslationInLowMemoryMode() =
        runTest {
            val coordinator =
                EngineCoordinator(
                    ocrEngine = FakeOcrEngine(text = "Hello"),
                    translationEngine = FakeTranslationEngine(),
                    sttEngine = FakeSpeechToTextEngine(),
                    ttsEngine = FakeTextToSpeechEngine(),
                    lowMemoryMode = true,
                )

            val result =
                coordinator.translateImage(
                    image = ImageInput(uri = "content://image"),
                    targetLanguage = "Filipino",
                )

            assertEquals("[Filipino] Hello", result.translatedText)
            assertFalse(coordinator.loadedEngines.contains(LoadedEngine.Ocr))
            assertTrue(coordinator.loadedEngines.contains(LoadedEngine.Translation))
        }

    @Test
    fun speechOutputUnloadsTtsInLowMemoryMode() =
        runTest {
            val coordinator =
                EngineCoordinator(
                    ocrEngine = FakeOcrEngine(),
                    translationEngine = FakeTranslationEngine(),
                    sttEngine = FakeSpeechToTextEngine(),
                    ttsEngine = FakeTextToSpeechEngine(),
                    lowMemoryMode = true,
                )

            coordinator.speak(SpeechRequest(text = "Hello", language = "English"))

            assertFalse(coordinator.loadedEngines.contains(LoadedEngine.Tts))
        }

    @Test
    fun speechOutputKeepsTtsLoadedWhenLowMemoryModeIsOff() =
        runTest {
            val coordinator =
                EngineCoordinator(
                    ocrEngine = FakeOcrEngine(),
                    translationEngine = FakeTranslationEngine(),
                    sttEngine = FakeSpeechToTextEngine(),
                    ttsEngine = FakeTextToSpeechEngine(),
                    lowMemoryMode = false,
                )

            coordinator.speak(SpeechRequest(text = "Hello", language = "English"))

            assertTrue(coordinator.loadedEngines.contains(LoadedEngine.Tts))
        }

    @Test
    fun transcriptionUnloadsPreviousTranslationInLowMemoryMode() =
        runTest {
            val coordinator =
                EngineCoordinator(
                    ocrEngine = FakeOcrEngine(),
                    translationEngine = FakeTranslationEngine(),
                    sttEngine = FakeSpeechToTextEngine(),
                    ttsEngine = FakeTextToSpeechEngine(),
                    lowMemoryMode = true,
                )

            coordinator.translateText(TranslationRequest("Hello", "English", "Filipino"))
            coordinator.transcribe(AudioInput(uri = "content://audio", durationMillis = 1000L))

            assertFalse(coordinator.loadedEngines.contains(LoadedEngine.Translation))
            assertFalse(coordinator.loadedEngines.contains(LoadedEngine.Stt))
        }

    @Test
    fun speechOutputUnloadsPreviousTranslationInLowMemoryMode() =
        runTest {
            val coordinator =
                EngineCoordinator(
                    ocrEngine = FakeOcrEngine(),
                    translationEngine = FakeTranslationEngine(),
                    sttEngine = FakeSpeechToTextEngine(),
                    ttsEngine = FakeTextToSpeechEngine(),
                    lowMemoryMode = true,
                )

            coordinator.translateText(TranslationRequest("Hello", "English", "Filipino"))
            coordinator.speak(SpeechRequest(text = "Kamusta", language = "Filipino"))

            assertFalse(coordinator.loadedEngines.contains(LoadedEngine.Translation))
            assertFalse(coordinator.loadedEngines.contains(LoadedEngine.Tts))
        }

    @Test
    fun normalModeCanKeepTranslationAndTtsLoaded() =
        runTest {
            val coordinator =
                EngineCoordinator(
                    ocrEngine = FakeOcrEngine(),
                    translationEngine = FakeTranslationEngine(),
                    sttEngine = FakeSpeechToTextEngine(),
                    ttsEngine = FakeTextToSpeechEngine(),
                    lowMemoryMode = false,
                )

            coordinator.translateText(TranslationRequest("Hello", "English", "Filipino"))
            coordinator.speak(SpeechRequest(text = "Kamusta", language = "Filipino"))

            assertTrue(coordinator.loadedEngines.contains(LoadedEngine.Translation))
            assertTrue(coordinator.loadedEngines.contains(LoadedEngine.Tts))
        }

    @Test
    fun failedTextTranslationUnloadsTranslationInLowMemoryMode() =
        runTest {
            val coordinator =
                EngineCoordinator(
                    ocrEngine = FakeOcrEngine(),
                    translationEngine = FailingTranslationEngine(),
                    sttEngine = FakeSpeechToTextEngine(),
                    ttsEngine = FakeTextToSpeechEngine(),
                    lowMemoryMode = true,
                )

            try {
                coordinator.translateText(TranslationRequest("Hello", "English", "Filipino"))
                fail("Expected translation failure")
            } catch (error: IllegalStateException) {
                assertEquals("Translation failed", error.message)
            }

            assertFalse(coordinator.loadedEngines.contains(LoadedEngine.Translation))
        }

    @Test
    fun failedImageTranslationUnloadsOcrInLowMemoryMode() =
        runTest {
            val coordinator =
                EngineCoordinator(
                    ocrEngine = FailingOcrEngine(),
                    translationEngine = FakeTranslationEngine(),
                    sttEngine = FakeSpeechToTextEngine(),
                    ttsEngine = FakeTextToSpeechEngine(),
                    lowMemoryMode = true,
                )

            try {
                coordinator.translateImage(ImageInput(uri = "content://image"), targetLanguage = "English")
                fail("Expected OCR failure")
            } catch (error: IllegalStateException) {
                assertEquals("OCR failed", error.message)
            }

            assertFalse(coordinator.loadedEngines.contains(LoadedEngine.Ocr))
        }

    @Test
    fun failedTranscriptionUnloadsSttInLowMemoryMode() =
        runTest {
            val coordinator =
                EngineCoordinator(
                    ocrEngine = FakeOcrEngine(),
                    translationEngine = FakeTranslationEngine(),
                    sttEngine = FailingSpeechToTextEngine(),
                    ttsEngine = FakeTextToSpeechEngine(),
                    lowMemoryMode = true,
                )

            try {
                coordinator.transcribe(AudioInput(uri = "content://audio", durationMillis = 1000L))
                fail("Expected STT failure")
            } catch (error: IllegalStateException) {
                assertEquals("STT failed", error.message)
            }

            assertFalse(coordinator.loadedEngines.contains(LoadedEngine.Stt))
        }

    @Test
    fun failedSpeechOutputUnloadsTtsInLowMemoryMode() =
        runTest {
            val coordinator =
                EngineCoordinator(
                    ocrEngine = FakeOcrEngine(),
                    translationEngine = FakeTranslationEngine(),
                    sttEngine = FakeSpeechToTextEngine(),
                    ttsEngine = FailingTextToSpeechEngine(),
                    lowMemoryMode = true,
                )

            try {
                coordinator.speak(SpeechRequest(text = "Hello", language = "English"))
                fail("Expected TTS failure")
            } catch (error: IllegalStateException) {
                assertEquals("TTS failed", error.message)
            }

            assertFalse(coordinator.loadedEngines.contains(LoadedEngine.Tts))
        }
}

private class FailingOcrEngine : OcrEngine {
    override suspend fun extractText(image: ImageInput): OcrResult {
        error("OCR failed")
    }
}

private class FailingTranslationEngine : TranslationEngine {
    override suspend fun translate(request: TranslationRequest): TranslationResult {
        error("Translation failed")
    }
}

private class FailingSpeechToTextEngine : SpeechToTextEngine {
    override suspend fun transcribe(audio: AudioInput): Transcript {
        error("STT failed")
    }
}

private class FailingTextToSpeechEngine : TextToSpeechEngine {
    override suspend fun synthesize(request: SpeechRequest) =
        error("TTS failed")
}
