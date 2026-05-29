package com.xtranslate.llama

import com.xtranslate.llama.nativebridge.NativeLlamaBridge
import com.xtranslate.model.LocalModelPaths
import com.xtranslate.model.ModelRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Tests file checks before the local OCR runner calls llama.cpp.
 */
class LocalOcrRunnerTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun extractSampleImageTextFailsWhenOcrModelIsMissing() =
        runTest {
            val runner =
                LocalOcrRunner(
                    modelPaths = LocalModelPaths(temporaryFolder.root),
                    bridge = NoOpNativeLlamaBridge(),
                )

            val error =
                runCatching { runner.extractSampleImageText("content://image") }
                    .exceptionOrNull()

            assertTrue(error?.message?.contains("Missing OCR model file") == true)
        }

    @Test
    fun extractSampleImageTextFailsWhenOcrProjectorIsMissing() =
        runTest {
            val modelPaths = LocalModelPaths(temporaryFolder.root)
            val ocrPack = ModelRegistry.defaultPacks().first { it.id == "ocr.paddleocr-vl-1_5.q4" }
            val modelFile = modelPaths.modelFiles(ocrPack).first()
            modelFile.parentFile?.mkdirs()
            modelFile.writeText("fake ocr model")
            val runner =
                LocalOcrRunner(
                    modelPaths = modelPaths,
                    bridge = NoOpNativeLlamaBridge(),
                )

            val error =
                runCatching { runner.extractSampleImageText("content://image") }
                    .exceptionOrNull()

            assertTrue(error?.message?.contains("Missing OCR projector file") == true)
        }
}

private class NoOpNativeLlamaBridge : NativeLlamaBridge {
    override suspend fun loadModel(
        modelPath: String,
        projectorPath: String?,
    ) = Unit

    override suspend fun unloadModel() = Unit

    override fun generate(
        prompt: String,
        imageUri: String?,
    ): Flow<String> = emptyFlow()
}
