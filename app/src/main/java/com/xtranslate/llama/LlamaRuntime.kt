package com.xtranslate.llama

import kotlinx.coroutines.flow.Flow

/**
 * Common API for local llama.cpp model sessions.
 *
 * The real Android binding will load a GGUF model from app-private storage and
 * stream generated tokens. Tests use a fake version of this interface.
 */
interface LlamaRuntime {
    val loadedProfile: LlamaProfile?

    suspend fun load(profile: LlamaProfile)

    suspend fun unload()

    fun generate(request: LlamaRequest): Flow<String>
}

enum class LlamaProfileKind {
    Ocr,
    Translation,
}

data class LlamaProfile(
    val id: String,
    val kind: LlamaProfileKind,
    val modelPath: String,
    val projectorPath: String? = null,
)

data class LlamaRequest(
    val prompt: String,
    val imageUri: String? = null,
)
