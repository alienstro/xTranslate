package com.xtranslate.runtime

import com.xtranslate.domain.ImageInput
import com.xtranslate.domain.TranslationRequest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
}
