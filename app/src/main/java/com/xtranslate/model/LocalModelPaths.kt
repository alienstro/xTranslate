package com.xtranslate.model

import java.io.File

/**
 * Builds app-private file paths for local model files.
 */
class LocalModelPaths(
    private val filesDir: File,
) {
    fun translationModelFile(): File = File(filesDir, "models/translation/multilingual-translator.gguf")
}
