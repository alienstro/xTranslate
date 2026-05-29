package com.xtranslate.model

/**
 * Tracks whether each model is missing, downloading, installed, or failed.
 *
 * This is in-memory for now so the app can be tested without real downloads.
 * Later this will be replaced or backed by app-private storage.
 */
enum class ModelInstallState {
    Missing,
    Downloading,
    Installed,
    Failed,
}

interface ModelStore {
    fun packs(): List<ModelPack>

    fun state(modelId: String): ModelInstallState

    fun markDownloading(modelId: String)

    fun markInstalled(modelId: String)

    fun markFailed(modelId: String)
}

class InMemoryModelStore(
    private val modelPacks: List<ModelPack>,
) : ModelStore {
    private val states =
        modelPacks
            .associate { it.id to ModelInstallState.Missing }
            .toMutableMap()

    override fun packs(): List<ModelPack> = modelPacks

    override fun state(modelId: String): ModelInstallState = states[modelId] ?: ModelInstallState.Missing

    override fun markDownloading(modelId: String) {
        states[modelId] = ModelInstallState.Downloading
    }

    override fun markInstalled(modelId: String) {
        states[modelId] = ModelInstallState.Installed
    }

    override fun markFailed(modelId: String) {
        states[modelId] = ModelInstallState.Failed
    }
}
