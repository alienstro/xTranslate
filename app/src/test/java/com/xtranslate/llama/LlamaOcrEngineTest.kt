package com.xtranslate.llama

import com.xtranslate.domain.ImageInput
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests the llama-backed OCR adapter for image text extraction.
 *
 * It checks that the adapter loads the OCR model profile, sends the image URI
 * to the llama runtime, and turns the generated text into OCR output.
 */
class LlamaOcrEngineTest {
    @Test
    fun extractTextLoadsOcrProfileAndSendsImageRequest() =
        runTest {
            val runtime = FakeLlamaRuntime(response = "Menu\nCoffee")
            val profile =
                LlamaProfile(
                    id = "ocr.paddleocr-vl-1_5.q4",
                    kind = LlamaProfileKind.Ocr,
                    modelPath = "/models/paddleocr-vl.gguf",
                    projectorPath = "/models/paddleocr-vl-mmproj.gguf",
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
}
