package com.xtranslate.model

import java.io.File
import java.io.InputStream

/**
 * Copies selected model files into app-private storage.
 */
class LocalModelImporter(
    private val modelPaths: LocalModelPaths,
) {
    fun importTranslationModel(inputStream: InputStream): File =
        copyToModelFile(inputStream, modelPaths.translationModelFile())

    fun importOcrModel(inputStream: InputStream): File =
        copyToModelFile(inputStream, File(modelPaths.modelDirectory(ocrPack()), "paddleocr-vl-1.5-q4.gguf"))

    fun importOcrProjector(inputStream: InputStream): File =
        copyToModelFile(inputStream, File(modelPaths.modelDirectory(ocrPack()), "paddleocr-vl-1.5-mmproj.gguf"))

    fun importSupertonicModel(inputStream: InputStream): File =
        copyToModelFile(inputStream, modelPaths.supertonicModelFile())

    private fun copyToModelFile(
        inputStream: InputStream,
        targetFile: File,
    ): File {
        targetFile.parentFile?.mkdirs()
        targetFile.outputStream().use { outputStream ->
            inputStream.use { input ->
                input.copyTo(outputStream)
            }
        }
        return targetFile
    }

    fun deleteModelFiles(pack: ModelPack) {
        pack.files.forEach { file ->
            modelPaths.modelFile(pack, file).delete()
        }
    }

    private fun ocrPack(): ModelPack =
        ModelRegistry.defaultPacks().first { pack -> pack.id == "ocr.paddleocr-vl-1_5.q4" }

}
