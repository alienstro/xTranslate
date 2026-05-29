package com.xtranslate.llama

import com.xtranslate.domain.TranslationRequest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests the llama-backed translation engine adapter.
 *
 * It checks that the adapter loads the translation model profile, sends a
 * translation prompt to the llama runtime, and returns the generated text.
 */
class LlamaTranslationEngineTest {
    @Test
    fun translateLoadsTranslationProfileAndSendsPrompt() =
        runTest {
            val runtime = FakeLlamaRuntime(response = "Kamusta")
            val profile =
                LlamaProfile(
                    id = "translation.multilingual.gguf",
                    kind = LlamaProfileKind.Translation,
                    modelPath = "/models/translator.gguf",
                )
            val engine = LlamaTranslationEngine(runtime, profile)

            val result =
                engine.translate(
                    TranslationRequest(
                        sourceText = "Hello",
                        sourceLanguage = "English",
                        targetLanguage = "Filipino",
                    ),
                )

            assertEquals(profile, runtime.loadedProfile)
            assertEquals("Kamusta", result.translatedText)
            assertEquals("Hello", result.sourceText)
            assertEquals("Filipino", result.targetLanguage)
            assertTrue(
                runtime.requests
                    .single()
                    .prompt
                    .contains("Hello"),
            )
            assertTrue(
                runtime.requests
                    .single()
                    .prompt
                    .contains("Filipino"),
            )
        }
}
