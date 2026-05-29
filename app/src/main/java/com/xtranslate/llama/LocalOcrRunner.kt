package com.xtranslate.llama

import com.xtranslate.domain.ImageInput
import com.xtranslate.llama.nativebridge.NativeLlamaBridge
import com.xtranslate.model.LocalModelPaths
import com.xtranslate.model.ModelRegistry

/**
 * Developer-only runner for the first local OCR/VLM path.
 *
 * It expects the PaddleOCR-VL model and projector files to already exist in
 * app-private storage.
 */
class LocalOcrRunner(
    private val modelPaths: LocalModelPaths,
    private val bridge: NativeLlamaBridge,
) {
    suspend fun extractSampleImageText(imageUri: String): String {
        val ocrFiles = ocrModelFiles()
        val modelFile = ocrFiles.first()
        val projectorFile = ocrFiles.drop(1).first()

        require(modelFile.exists()) {
            "Missing OCR model file: ${modelFile.absolutePath}"
        }
        require(projectorFile.exists()) {
            "Missing OCR projector file: ${projectorFile.absolutePath}"
        }

        val runtime = AndroidLlamaRuntime(bridge)
        val engine =
            LlamaOcrEngine(
                runtime = runtime,
                profile = LlamaProfileFactory.ocrProfile(modelFile, projectorFile),
            )

        return engine.extractText(ImageInput(uri = imageUri)).text
    }

    private fun ocrModelFiles() =
        modelPaths.modelFiles(
            ModelRegistry.defaultPacks().first { pack -> pack.id == "ocr.paddleocr-vl-1_5.q4" },
        )
}
