package com.xtranslate.ui.chat

import com.xtranslate.domain.ImageInput
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
    fun translateImageAddsOcrReviewAndTranslationMessages() =
        runTest {
            val viewModel = ChatViewModel(fakeCoordinator())

            viewModel.translateImage(ImageInput("content://image"))

            val messages = viewModel.state.value.messages
            assertTrue(messages.any { it.kind == ChatMessageKind.OcrReview })
            assertTrue(messages.any { it.kind == ChatMessageKind.Translation })
        }

    private fun fakeCoordinator() =
        EngineCoordinator(
            ocrEngine = FakeOcrEngine(text = "Image text"),
            translationEngine = FakeTranslationEngine(),
            sttEngine = FakeSpeechToTextEngine(),
            ttsEngine = FakeTextToSpeechEngine(),
            lowMemoryMode = true,
        )
}
