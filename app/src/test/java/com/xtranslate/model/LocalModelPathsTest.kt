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

    @Test
    fun modelFilesUseEngineSpecificModelDirectories() {
        val filesDir = File("/data/user/0/com.xtranslate/files")
        val paths = LocalModelPaths(filesDir)
        val packs = ModelRegistry.defaultPacks()

        val translationPack = packs.first { it.id == "translation.multilingual.gguf" }
        val ocrPack = packs.first { it.id == "ocr.paddleocr-vl-1_5.q4" }

        assertEquals(
            "/data/user/0/com.xtranslate/files/models/translation/multilingual-translator.gguf",
            paths.modelFiles(translationPack).single().path.replace('\\', '/'),
        )
        assertEquals(
            listOf(
                "/data/user/0/com.xtranslate/files/models/ocr/paddleocr-vl-1.5-q4.gguf",
                "/data/user/0/com.xtranslate/files/models/ocr/paddleocr-vl-1.5-mmproj.gguf",
            ),
            paths.modelFiles(ocrPack).map { it.path.replace('\\', '/') },
        )
    }
}
