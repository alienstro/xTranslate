package com.xtranslate.model

import java.io.File

/**
 * Builds app-private file paths for local model files.
 */
class LocalModelPaths(
    private val filesDir: File,
) {
    fun translationModelFile(): File = File(filesDir, "models/translation/multilingual-translator.gguf")

    fun modelDirectory(pack: ModelPack): File =
        when (pack.engineType) {
            EngineType.LlamaOcr -> File(filesDir, "models/ocr")
            EngineType.LlamaTranslation -> File(filesDir, "models/translation")
            EngineType.WhisperStt -> File(filesDir, "models/stt")
            EngineType.OnnxTts -> File(filesDir, "models/tts")
        }

    fun modelFile(
        pack: ModelPack,
        file: ModelFile,
    ): File = File(modelDirectory(pack), file.name)

    fun modelFiles(pack: ModelPack): List<File> =
        pack.files.map { file -> modelFile(pack, file) }
}
