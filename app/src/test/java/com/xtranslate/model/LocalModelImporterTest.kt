package com.xtranslate.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream

/**
 * Tests copying user-selected model files into app-private storage.
 */
class LocalModelImporterTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun importTranslationModelCopiesBytesIntoTranslationModelFile() {
        val paths = LocalModelPaths(temporaryFolder.root)
        val importer = LocalModelImporter(paths)

        val targetFile =
            importer.importTranslationModel(
                ByteArrayInputStream("fake gguf bytes".toByteArray()),
            )

        assertEquals(paths.translationModelFile(), targetFile)
        assertTrue(targetFile.exists())
        assertEquals("fake gguf bytes", targetFile.readText())
    }

    @Test
    fun importOcrModelCopiesBytesIntoOcrModelFile() {
        val paths = LocalModelPaths(temporaryFolder.root)
        val importer = LocalModelImporter(paths)

        val targetFile =
            importer.importOcrModel(
                ByteArrayInputStream("fake ocr gguf".toByteArray()),
            )

        assertEquals("paddleocr-vl-1.5-q4.gguf", targetFile.name)
        assertTrue(targetFile.path.replace('\\', '/').endsWith("/models/ocr/paddleocr-vl-1.5-q4.gguf"))
        assertEquals("fake ocr gguf", targetFile.readText())
    }

    @Test
    fun importOcrProjectorCopiesBytesIntoOcrProjectorFile() {
        val paths = LocalModelPaths(temporaryFolder.root)
        val importer = LocalModelImporter(paths)

        val targetFile =
            importer.importOcrProjector(
                ByteArrayInputStream("fake projector gguf".toByteArray()),
            )

        assertEquals("paddleocr-vl-1.5-mmproj.gguf", targetFile.name)
        assertTrue(targetFile.path.replace('\\', '/').endsWith("/models/ocr/paddleocr-vl-1.5-mmproj.gguf"))
        assertEquals("fake projector gguf", targetFile.readText())
    }

    @Test
    fun importSupertonicModelCopiesBytesIntoTtsModelFile() {
        val paths = LocalModelPaths(temporaryFolder.root)
        val importer = LocalModelImporter(paths)

        val targetFile =
            importer.importSupertonicModel(
                ByteArrayInputStream("fake supertonic bytes".toByteArray()),
            )

        assertEquals("supertonic-3.onnx", targetFile.name)
        assertTrue(targetFile.path.replace('\\', '/').endsWith("/models/tts/supertonic-3.onnx"))
        assertEquals("fake supertonic bytes", targetFile.readText())
    }
}
