package com.xtranslate.llama

import java.io.File

/**
 * Creates llama model profiles from app-private model files.
 */
object LlamaProfileFactory {
    fun translationProfile(modelFile: File): LlamaProfile =
        LlamaProfile(
            id = "translation.multilingual.gguf",
            kind = LlamaProfileKind.Translation,
            modelPath = modelFile.absolutePath,
            projectorPath = null,
        )
}
