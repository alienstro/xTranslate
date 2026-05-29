package com.xtranslate.llama

import com.xtranslate.domain.TranslationRequest
import com.xtranslate.llama.nativebridge.NativeLlamaBridge
import com.xtranslate.model.LocalModelPaths

/**
 * Developer-only runner for the first local GGUF text generation path.
 *
 * It expects the translation GGUF file to already exist in app-private storage.
 */
class LocalTextGenerationRunner(
    private val modelPaths: LocalModelPaths,
    private val bridge: NativeLlamaBridge,
) {
    suspend fun translateSampleText(): String {
        val modelFile = modelPaths.translationModelFile()
        require(modelFile.exists()) {
            "Missing model file: ${modelFile.absolutePath}"
        }

        val runtime = AndroidLlamaRuntime(bridge)
        val engine =
            LlamaTranslationEngine(
                runtime = runtime,
                profile = LlamaProfileFactory.translationProfile(modelFile),
            )

        return engine
            .translate(
                TranslationRequest(
                    sourceText = "Hello",
                    sourceLanguage = "English",
                    targetLanguage = "Filipino",
                ),
            ).translatedText
    }
}
