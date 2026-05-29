package com.xtranslate.llama.nativebridge

import kotlinx.coroutines.flow.Flow

/**
 * Small Kotlin boundary around the native llama.cpp binding.
 *
 * The real implementation will call the official Android llama.cpp binding.
 * Tests can use a fake implementation of this interface.
 */
interface NativeLlamaBridge {
    suspend fun loadModel(
        modelPath: String,
        projectorPath: String?,
    )

    suspend fun unloadModel()

    fun generate(
        prompt: String,
        imageUri: String?,
    ): Flow<String>
}
