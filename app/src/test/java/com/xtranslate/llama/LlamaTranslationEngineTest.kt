package com.xtranslate.llama

import com.xtranslate.domain.TranslationRequest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Tests the llama-backed translation engine adapter.
 *
 * It checks that the adapter loads the translation model profile, sends a
 * translation prompt to the llama runtime, and returns the generated text.
 */
class LlamaTranslationEngineTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun translateLoadsTranslationProfileAndSendsPrompt() =
        runTest {
            val runtime = FakeLlamaRuntime(response = "Kamusta")
            val modelFile = temporaryFolder.newFile("translator.gguf")
            val profile =
                LlamaProfile(
                    id = "translation.multilingual.gguf",
                    kind = LlamaProfileKind.Translation,
                    modelPath = modelFile.absolutePath,
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

    @Test
    fun translateFailsBeforeLoadWhenModelFileIsMissing() =
        runTest {
            val runtime = FakeLlamaRuntime(response = "Kamusta")
            val missingModel = File("missing-translation-model.gguf")
            val engine =
                LlamaTranslationEngine(
                    runtime = runtime,
                    profile =
                        LlamaProfile(
                            id = "translation.multilingual.gguf",
                            kind = LlamaProfileKind.Translation,
                            modelPath = missingModel.absolutePath,
                        ),
                )

            val error =
                runCatching {
                    engine.translate(
                        TranslationRequest(
                            sourceText = "Hello",
                            targetLanguage = "Filipino",
                        ),
                    )
                }.exceptionOrNull()

            assertTrue(error?.message?.contains("Missing translation model file") == true)
            assertEquals(null, runtime.loadedProfile)
        }
}
