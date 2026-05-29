package com.xtranslate.llama

import com.xtranslate.domain.Prompts
import com.xtranslate.domain.TranslationEngine
import com.xtranslate.domain.TranslationRequest
import com.xtranslate.domain.TranslationResult
import kotlinx.coroutines.flow.toList
import java.io.File

/**
 * Translation engine that sends prompts to a llama.cpp translation profile.
 *
 * This connects the app's `TranslationEngine` interface to the shared
 * `LlamaRuntime` interface. The real runtime can be added later without
 * changing the UI or chat workflow.
 */
class LlamaTranslationEngine(
    private val runtime: LlamaRuntime,
    private val profile: LlamaProfile,
) : TranslationEngine {
    override suspend fun translate(request: TranslationRequest): TranslationResult {
        require(File(profile.modelPath).exists()) {
            "Missing translation model file: ${profile.modelPath}"
        }

        runtime.load(profile)
        val prompt = Prompts.translationPrompt(request)
        val text =
            runtime
                .generate(LlamaRequest(prompt = prompt))
                .toList()
                .joinToString(separator = "")

        return TranslationResult(
            translatedText = text,
            sourceText = request.sourceText,
            targetLanguage = request.targetLanguage,
        )
    }
}
