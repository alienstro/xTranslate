package com.xtranslate.llama

import com.xtranslate.llama.nativebridge.NativeLlamaBridge
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests AndroidLlamaRuntime without loading real native code.
 */
class AndroidLlamaRuntimeTest {
    @Test
    fun loadStoresProfileAndCallsNativeBridge() =
        runTest {
            val bridge = FakeNativeLlamaBridge()
            val runtime = AndroidLlamaRuntime(bridge)
            val profile =
                LlamaProfile(
                    id = "translation.multilingual.gguf",
                    kind = LlamaProfileKind.Translation,
                    modelPath = "/models/translator.gguf",
                )

            runtime.load(profile)

            assertEquals(profile, runtime.loadedProfile)
            assertEquals("/models/translator.gguf", bridge.loadedModelPath)
            assertNull(bridge.loadedProjectorPath)
        }

    @Test
    fun generateDelegatesToNativeBridge() =
        runTest {
            val bridge = FakeNativeLlamaBridge(response = listOf("Ka", "musta"))
            val runtime = AndroidLlamaRuntime(bridge)

            val output =
                runtime
                    .generate(LlamaRequest(prompt = "Translate", imageUri = null))
                    .toList()
                    .joinToString("")

            assertEquals("Kamusta", output)
            assertEquals("Translate", bridge.lastPrompt)
        }

    @Test
    fun loadDoesNotReloadSameProfile() =
        runTest {
            val bridge = FakeNativeLlamaBridge()
            val runtime = AndroidLlamaRuntime(bridge)
            val profile =
                LlamaProfile(
                    id = "translation.multilingual.gguf",
                    kind = LlamaProfileKind.Translation,
                    modelPath = "/models/translator.gguf",
                )

            runtime.load(profile)
            runtime.load(profile)

            assertEquals(1, bridge.loadCount)
            assertEquals(0, bridge.unloadCount)
            assertEquals(profile, runtime.loadedProfile)
        }

    @Test
    fun loadUnloadsPreviousProfileBeforeLoadingDifferentProfile() =
        runTest {
            val bridge = FakeNativeLlamaBridge()
            val runtime = AndroidLlamaRuntime(bridge)
            val translationProfile =
                LlamaProfile(
                    id = "translation.multilingual.gguf",
                    kind = LlamaProfileKind.Translation,
                    modelPath = "/models/translator.gguf",
                )
            val ocrProfile =
                LlamaProfile(
                    id = "ocr.paddleocr-vl-1_5.q4",
                    kind = LlamaProfileKind.Ocr,
                    modelPath = "/models/ocr.gguf",
                    projectorPath = "/models/ocr-mmproj.gguf",
                )

            runtime.load(translationProfile)
            runtime.load(ocrProfile)

            assertEquals(2, bridge.loadCount)
            assertEquals(1, bridge.unloadCount)
            assertEquals(ocrProfile, runtime.loadedProfile)
            assertEquals("/models/ocr.gguf", bridge.loadedModelPath)
            assertEquals("/models/ocr-mmproj.gguf", bridge.loadedProjectorPath)
        }

    @Test
    fun unloadClearsProfileAndCallsNativeBridge() =
        runTest {
            val bridge = FakeNativeLlamaBridge()
            val runtime = AndroidLlamaRuntime(bridge)
            val profile =
                LlamaProfile(
                    id = "ocr.paddleocr-vl-1_5.q4",
                    kind = LlamaProfileKind.Ocr,
                    modelPath = "/models/ocr.gguf",
                    projectorPath = "/models/ocr-mmproj.gguf",
                )

            runtime.load(profile)
            runtime.unload()

            assertNull(runtime.loadedProfile)
            assertEquals(true, bridge.unloaded)
        }
}

private class FakeNativeLlamaBridge(
    private val response: List<String> = listOf("ok"),
) : NativeLlamaBridge {
    var loadedModelPath: String? = null
    var loadedProjectorPath: String? = null
    var lastPrompt: String? = null
    var unloaded: Boolean = false
    var loadCount: Int = 0
    var unloadCount: Int = 0

    override suspend fun loadModel(
        modelPath: String,
        projectorPath: String?,
    ) {
        loadCount += 1
        loadedModelPath = modelPath
        loadedProjectorPath = projectorPath
        unloaded = false
    }

    override suspend fun unloadModel() {
        unloadCount += 1
        unloaded = true
        loadedModelPath = null
        loadedProjectorPath = null
    }

    override fun generate(
        prompt: String,
        imageUri: String?,
    ): Flow<String> =
        flow {
            lastPrompt = prompt
            response.forEach { emit(it) }
        }
}
