package com.xtranslate.llama

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Tests creation of llama profiles from local model files.
 */
class LlamaProfileFactoryTest {
    @Test
    fun translationProfileUsesTextOnlyLlamaProfile() {
        val modelFile = File("/models/translation.gguf")

        val profile = LlamaProfileFactory.translationProfile(modelFile)

        assertEquals("translation.multilingual.gguf", profile.id)
        assertEquals(LlamaProfileKind.Translation, profile.kind)
        assertTrue(profile.modelPath.replace('\\', '/').endsWith("/models/translation.gguf"))
        assertNull(profile.projectorPath)
    }

    @Test
    fun ocrProfileUsesVisionLlamaProfileWithProjector() {
        val modelFile = File("/models/ocr/paddleocr-vl-1.5-q4.gguf")
        val projectorFile = File("/models/ocr/paddleocr-vl-1.5-mmproj.gguf")

        val profile = LlamaProfileFactory.ocrProfile(modelFile, projectorFile)

        assertEquals("ocr.paddleocr-vl-1_5.q4", profile.id)
        assertEquals(LlamaProfileKind.Ocr, profile.kind)
        assertTrue(profile.modelPath.replace('\\', '/').endsWith("/models/ocr/paddleocr-vl-1.5-q4.gguf"))
        assertTrue(profile.projectorPath?.replace('\\', '/')?.endsWith("/models/ocr/paddleocr-vl-1.5-mmproj.gguf") == true)
    }
}
