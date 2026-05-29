package com.xtranslate.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * Tests app-private file paths for local model files.
 */
class LocalModelPathsTest {
    @Test
    fun translationModelPathUsesAppPrivateModelsDirectory() {
        val filesDir = File("/data/user/0/com.xtranslate/files")
        val paths = LocalModelPaths(filesDir)

        assertEquals(
            "/data/user/0/com.xtranslate/files/models/translation/multilingual-translator.gguf",
            paths.translationModelFile().path.replace('\\', '/'),
        )
    }
}
