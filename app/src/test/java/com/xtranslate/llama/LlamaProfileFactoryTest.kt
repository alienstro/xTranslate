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
}
