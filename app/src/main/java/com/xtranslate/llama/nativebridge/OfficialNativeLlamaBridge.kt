package com.xtranslate.llama.nativebridge

import android.content.Context
import com.arm.aichat.AiChat
import com.arm.aichat.InferenceEngine
import com.arm.aichat.UnsupportedArchitectureException
import com.arm.aichat.gguf.GgufMetadataReader
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Native bridge backed by the official llama.cpp Android library.
 *
 * This supports text prompts first. Image/projector support for PaddleOCR-VL is
 * planned separately after the official text runtime is verified on device.
 */
class OfficialNativeLlamaBridge(
    context: Context,
) : NativeLlamaBridge {
    private val engine: InferenceEngine =
        AiChat.getInferenceEngine(context.applicationContext)

    override suspend fun loadModel(
        modelPath: String,
        projectorPath: String?,
    ) {
        require(projectorPath == null) {
            "Projector/image input is not wired yet. Use text-only profiles first."
        }
        resetErrorStateIfNeeded()
        unloadReadyModelIfNeeded()
        val arch = readGgufArchitecture(modelPath)
        val fileSizeMb = File(modelPath).length() / 1_048_576
        try {
            engine.loadModel(modelPath)
        } catch (error: UnsupportedArchitectureException) {
            resetErrorStateIfNeeded()
            val archDetail = if (arch != null) " (arch: '$arch', ${fileSizeMb}MB)" else " (${fileSizeMb}MB)"
            throw IllegalStateException(
                "Failed to load GGUF model$archDetail. " +
                    "The model may be too large for available device memory. " +
                    "Try Qwen2.5 0.5B Q4_K_M from the Models tab.",
                error,
            )
        } catch (error: Exception) {
            resetErrorStateIfNeeded()
            throw error
        }
    }

    override suspend fun unloadModel() {
        engine.cleanUp()
    }

    override fun generate(
        prompt: String,
        imageUri: String?,
    ): Flow<String> {
        require(imageUri == null) {
            "Image input is not wired yet. Use text-only profiles first."
        }
        return engine.sendUserPrompt(prompt)
    }

    private fun resetErrorStateIfNeeded() {
        if (engine.state.value is InferenceEngine.State.Error) {
            engine.cleanUp()
        }
    }

    private fun unloadReadyModelIfNeeded() {
        if (engine.state.value is InferenceEngine.State.ModelReady) {
            engine.cleanUp()
        }
    }

    private suspend fun readGgufArchitecture(modelPath: String): String? =
        try {
            File(modelPath).inputStream().buffered().use { stream ->
                GgufMetadataReader.create().readStructuredMetadata(stream).architecture?.architecture
            }
        } catch (_: Exception) {
            null
        }
}
