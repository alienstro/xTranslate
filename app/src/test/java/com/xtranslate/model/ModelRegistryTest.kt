package com.xtranslate.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the app's model list and model install state.
 *
 * These tests make sure the app knows about the four main model packs:
 * OCR, translation, speech-to-text, and text-to-speech. They also check that
 * the temporary in-memory model store can track download and install changes.
 */
class ModelRegistryTest {
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
    fun inMemoryStoreTracksInstallState() {
        val store = InMemoryModelStore(ModelRegistry.defaultPacks())

        assertEquals(ModelInstallState.Missing, store.state("translation.multilingual.gguf"))

        store.markDownloading("translation.multilingual.gguf")
        assertEquals(ModelInstallState.Downloading, store.state("translation.multilingual.gguf"))

        store.markInstalled("translation.multilingual.gguf")
        assertEquals(ModelInstallState.Installed, store.state("translation.multilingual.gguf"))
    }
}
