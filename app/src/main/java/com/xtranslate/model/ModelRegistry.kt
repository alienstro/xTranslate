package com.xtranslate.model

/**
 * List of model packs the app knows how to use.
 *
 * Each model pack describes one local AI dependency, such as OCR, translation,
 * speech-to-text, or text-to-speech. The app uses this list to show what is
 * missing, what can be downloaded, and what engine each model belongs to.
 */
enum class EngineType {
    LlamaOcr,
    LlamaTranslation,
    WhisperStt,
    OnnxTts,
}

enum class RamTier {
    Low,
    Medium,
    High,
}

data class ModelFile(
    val name: String,
    val required: Boolean = true,
    val downloadUrl: String? = null,
    val checksumSha256: String? = null,
)

data class ModelPack(
    val id: String,
    val displayName: String,
    val engineType: EngineType,
    val files: List<ModelFile>,
    val expectedSizeMb: Int? = null,
    val minimumRamTier: RamTier,
)

object ModelRegistry {
    fun defaultPacks(): List<ModelPack> =
        listOf(
            ModelPack(
                id = "ocr.paddleocr-vl-1_5.q4",
                displayName = "PaddleOCR-VL 1.5 Q4",
                engineType = EngineType.LlamaOcr,
                files =
                    listOf(
                        ModelFile(
                            name = "paddleocr-vl-1.5-q4.gguf",
                            downloadUrl =
                                "https://huggingface.co/PaddlePaddle/PaddleOCR-VL-1.5-GGUF/resolve/main/PaddleOCR-VL-1.5.gguf",
                        ),
                        ModelFile(
                            name = "paddleocr-vl-1.5-mmproj.gguf",
                            downloadUrl =
                                "https://huggingface.co/PaddlePaddle/PaddleOCR-VL-1.5-GGUF/resolve/main/PaddleOCR-VL-1.5-mmproj.gguf",
                        ),
                    ),
                expectedSizeMb = null,
                minimumRamTier = RamTier.Low,
            ),
            ModelPack(
                id = "translation.multilingual.gguf",
                displayName = "Qwen2.5 0.5B Translator",
                engineType = EngineType.LlamaTranslation,
                files =
                    listOf(
                        ModelFile(
                            name = "qwen2.5-0.5b-translator-q4_k_m.gguf",
                            downloadUrl =
                                "https://huggingface.co/bartowski/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/Qwen2.5-0.5B-Instruct-Q4_K_M.gguf",
                        ),
                    ),
                expectedSizeMb = 398,
                minimumRamTier = RamTier.Low,
            ),
            ModelPack(
                id = "stt.whisper",
                displayName = "Whisper Large v3 Turbo Q8",
                engineType = EngineType.WhisperStt,
                files =
                    listOf(
                        ModelFile(
                            name = "ggml-large-v3-turbo-q8_0.bin",
                            downloadUrl =
                                "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-large-v3-turbo-q8_0.bin",
                        ),
                    ),
                expectedSizeMb = 900,
                minimumRamTier = RamTier.High,
            ),
            ModelPack(
                id = "tts.supertonic-3",
                displayName = "Supertonic 3 TTS",
                engineType = EngineType.OnnxTts,
                files =
                    listOf(
                        ModelFile(
                            name = "supertonic-3.onnx",
                            downloadUrl =
                                "https://huggingface.co/Supertone/supertonic-3/resolve/main/onnx/vector_estimator.onnx",
                        ),
                    ),
                expectedSizeMb = null,
                minimumRamTier = RamTier.Medium,
            ),
        )
}
