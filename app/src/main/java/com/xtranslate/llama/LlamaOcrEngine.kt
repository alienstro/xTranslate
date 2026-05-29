package com.xtranslate.llama

import com.xtranslate.domain.ImageInput
import com.xtranslate.domain.OcrEngine
import com.xtranslate.domain.OcrResult
import com.xtranslate.domain.Prompts
import kotlinx.coroutines.flow.toList

/**
 * OCR engine that sends image-text requests to a llama.cpp OCR/VLM profile.
 *
 * This will later be used for PaddleOCR-VL. For now it depends only on the
 * shared `LlamaRuntime` interface, so tests can use a fake runtime.
 */
class LlamaOcrEngine(
    private val runtime: LlamaRuntime,
    private val profile: LlamaProfile,
) : OcrEngine {
    override suspend fun extractText(image: ImageInput): OcrResult {
        runtime.load(profile)
        val text =
            runtime
                .generate(
                    LlamaRequest(
                        prompt = Prompts.ocrExtractionPrompt(),
                        imageUri = image.uri,
                    ),
                ).toList()
                .joinToString(separator = "")

        return OcrResult(
            text = text,
            blocks = text.lines().filter { it.isNotBlank() },
        )
    }
}
