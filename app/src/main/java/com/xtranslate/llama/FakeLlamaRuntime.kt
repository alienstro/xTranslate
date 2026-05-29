package com.xtranslate.llama

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Fake llama runtime used by tests before native llama.cpp is connected.
 *
 * It remembers the loaded model profile and returns a fixed response when
 * `generate` is called.
 */
class FakeLlamaRuntime(
    private val response: String,
) : LlamaRuntime {
    private var currentProfile: LlamaProfile? = null
    val requests = mutableListOf<LlamaRequest>()

    override val loadedProfile: LlamaProfile?
        get() = currentProfile

    override suspend fun load(profile: LlamaProfile) {
        currentProfile = profile
    }

    override suspend fun unload() {
        currentProfile = null
    }

    override fun generate(request: LlamaRequest): Flow<String> =
        flow {
            requests.add(request)
            emit(response)
        }
}
