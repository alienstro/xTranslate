package com.xtranslate.llama.nativebridge

import android.content.Context
import com.arm.aichat.AiChat
import com.arm.aichat.InferenceEngine
import kotlinx.coroutines.flow.Flow

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
        engine.loadModel(modelPath)
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
}
