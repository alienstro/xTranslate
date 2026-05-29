package com.xtranslate.model

import java.io.File
import java.io.InputStream

/**
 * Copies selected model files into app-private storage.
 */
class LocalModelImporter(
    private val modelPaths: LocalModelPaths,
) {
    fun importTranslationModel(inputStream: InputStream): File {
        val targetFile = modelPaths.translationModelFile()
        targetFile.parentFile?.mkdirs()
        targetFile.outputStream().use { outputStream ->
            inputStream.use { input ->
                input.copyTo(outputStream)
            }
        }
        return targetFile
    }
}
