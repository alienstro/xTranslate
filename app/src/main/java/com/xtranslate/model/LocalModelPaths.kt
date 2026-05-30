package com.xtranslate.model

import java.io.File

/**
 * Builds app-private file paths for local model files.
 */
class LocalModelPaths(
    private val filesDir: File,
) {
    fun translationModelFile(): File = File(filesDir, "models/translation/qwen2.5-0.5b-translator-q4_k_m.gguf")

    fun whisperModelFile(): File = File(filesDir, "models/stt/ggml-large-v3-turbo-q8_0.bin")

    fun supertonicModelFile(): File = File(filesDir, "models/tts/supertonic-3.onnx")

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
