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
}
