package com.xtranslate.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.Rule

/**
 * Tests for the app's model list and model install state.
 *
 * These tests make sure the app knows about the four main model packs:
 * OCR, translation, speech-to-text, and text-to-speech. They also check that
 * the temporary in-memory model store can track download and install changes.
 */
class ModelRegistryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun defaultRegistryContainsFourModelPacks() {
        val packs = ModelRegistry.defaultPacks()

        assertEquals(4, packs.size)
        assertTrue(packs.any { it.id == "ocr.paddleocr-vl-1_5.q4" })
        assertTrue(packs.any { it.id == "translation.multilingual.gguf" })
        assertTrue(packs.any { it.id == "stt.whisper" })
        assertTrue(packs.any { it.id == "tts.supertonic-3" })
    }

    @Test
    fun speechPackUsesWhisperLargeV3TurboQ8() {
        val pack = ModelRegistry.defaultPacks().first { it.id == "stt.whisper" }

        assertEquals("Whisper Large v3 Turbo Q8", pack.displayName)
        assertEquals(900, pack.expectedSizeMb)
        assertEquals(RamTier.High, pack.minimumRamTier)
        assertEquals("ggml-large-v3-turbo-q8_0.bin", pack.files.single().name)
        assertEquals(
            "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-large-v3-turbo-q8_0.bin",
            pack.files.single().downloadUrl,
        )
    }

    @Test
    fun inMemoryStoreTracksInstallState() {
        val store = InMemoryModelStore(ModelRegistry.defaultPacks())

        assertEquals(ModelInstallState.Missing, store.state("translation.multilingual.gguf"))

        store.markDownloading("translation.multilingual.gguf")
        assertEquals(ModelInstallState.Downloading, store.state("translation.multilingual.gguf"))

        store.markInstalled("translation.multilingual.gguf")
        assertEquals(ModelInstallState.Installed, store.state("translation.multilingual.gguf"))
    }

    @Test
    fun fileBackedStoreReportsMissingUntilRequiredFilesExist() {
        val paths = LocalModelPaths(temporaryFolder.root)
        val packs = ModelRegistry.defaultPacks()
        val store = FileBackedModelStore(packs, paths)
        val translationPack = packs.first { it.id == "translation.multilingual.gguf" }
        val translationFile = paths.modelFiles(translationPack).single()

        assertEquals(ModelInstallState.Missing, store.state("translation.multilingual.gguf"))

        translationFile.parentFile?.mkdirs()
        translationFile.writeText("fake model")

        assertEquals(ModelInstallState.Installed, store.state("translation.multilingual.gguf"))
    }

    @Test
    fun fileBackedStoreKeepsTemporaryDownloadAndFailureStates() {
        val store =
            FileBackedModelStore(
                modelPacks = ModelRegistry.defaultPacks(),
                modelPaths = LocalModelPaths(temporaryFolder.root),
            )

        store.markDownloading("translation.multilingual.gguf")
        assertEquals(ModelInstallState.Downloading, store.state("translation.multilingual.gguf"))

        store.markFailed("translation.multilingual.gguf")
        assertEquals(ModelInstallState.Failed, store.state("translation.multilingual.gguf"))
    }
}
