package com.xtranslate.llama

import com.xtranslate.domain.ImageInput
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Tests the llama-backed OCR adapter for image text extraction.
 *
 * It checks that the adapter loads the OCR model profile, sends the image URI
 * to the llama runtime, and turns the generated text into OCR output.
 */
class LlamaOcrEngineTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun extractTextLoadsOcrProfileAndSendsImageRequest() =
        runTest {
            val runtime = FakeLlamaRuntime(response = "Menu\nCoffee")
            val modelFile = temporaryFolder.newFile("paddleocr-vl.gguf")
            val projectorFile = temporaryFolder.newFile("paddleocr-vl-mmproj.gguf")
            val profile =
                LlamaProfile(
                    id = "ocr.paddleocr-vl-1_5.q4",
                    kind = LlamaProfileKind.Ocr,
                    modelPath = modelFile.absolutePath,
                    projectorPath = projectorFile.absolutePath,
                )
            val engine = LlamaOcrEngine(runtime, profile)

            val result = engine.extractText(ImageInput(uri = "content://image"))

            assertEquals(profile, runtime.loadedProfile)
            assertEquals("Menu\nCoffee", result.text)
            assertEquals(listOf("Menu", "Coffee"), result.blocks)
            assertEquals("content://image", runtime.requests.single().imageUri)
            assertTrue(
                runtime.requests
                    .single()
                    .prompt
                    .contains("Do not translate"),
            )
        }

    @Test
    fun extractTextFailsBeforeLoadWhenOcrModelFileIsMissing() =
        runTest {
            val runtime = FakeLlamaRuntime(response = "Menu")
            val engine =
                LlamaOcrEngine(
                    runtime = runtime,
                    profile =
                        LlamaProfile(
                            id = "ocr.paddleocr-vl-1_5.q4",
                            kind = LlamaProfileKind.Ocr,
                            modelPath = File("missing-ocr-model.gguf").absolutePath,
                            projectorPath = File("missing-ocr-projector.gguf").absolutePath,
                        ),
                )

            val error =
                runCatching { engine.extractText(ImageInput(uri = "content://image")) }
                    .exceptionOrNull()

            assertTrue(error?.message?.contains("Missing OCR model file") == true)
            assertEquals(null, runtime.loadedProfile)
        }

    @Test
    fun extractTextFailsBeforeLoadWhenOcrProjectorFileIsMissing() =
        runTest {
            val runtime = FakeLlamaRuntime(response = "Menu")
            val modelFile = kotlin.io.path.createTempFile().toFile()
            val engine =
                LlamaOcrEngine(
                    runtime = runtime,
                    profile =
                        LlamaProfile(
                            id = "ocr.paddleocr-vl-1_5.q4",
                            kind = LlamaProfileKind.Ocr,
                            modelPath = modelFile.absolutePath,
                            projectorPath = File("missing-ocr-projector.gguf").absolutePath,
                        ),
                )

            val error =
                runCatching { engine.extractText(ImageInput(uri = "content://image")) }
                    .exceptionOrNull()

            assertTrue(error?.message?.contains("Missing OCR projector file") == true)
            assertEquals(null, runtime.loadedProfile)
        }
}
