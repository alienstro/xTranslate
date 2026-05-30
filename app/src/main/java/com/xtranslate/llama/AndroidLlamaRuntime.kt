package com.xtranslate.llama

import com.xtranslate.llama.nativebridge.NativeLlamaBridge
import kotlinx.coroutines.flow.Flow

/**
 * LlamaRuntime implementation backed by an Android native bridge.
 *
 * This class owns the app-level profile state. The bridge owns the real native
 * llama.cpp session details.
 */
class AndroidLlamaRuntime(
    private val bridge: NativeLlamaBridge,
) : LlamaRuntime {
    private var currentProfile: LlamaProfile? = null

    override val loadedProfile: LlamaProfile?
        get() = currentProfile

    override suspend fun load(profile: LlamaProfile) {
        if (currentProfile == profile) {
            return
        }
        if (currentProfile != null) {
            bridge.unloadModel()
            currentProfile = null
        }
        bridge.loadModel(
            modelPath = profile.modelPath,
            projectorPath = profile.projectorPath,
        )
        currentProfile = profile
    }

    override suspend fun unload() {
        bridge.unloadModel()
        currentProfile = null
    }

    override fun generate(request: LlamaRequest): Flow<String> =
        bridge.generate(
            prompt = request.prompt,
            imageUri = request.imageUri,
        )
}
