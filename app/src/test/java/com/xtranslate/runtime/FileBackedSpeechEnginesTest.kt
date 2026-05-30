package com.xtranslate.runtime

import com.xtranslate.domain.SpeechRequest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

class FileBackedSpeechEnginesTest {
    @Test
    fun ttsThrowsWhenSupertonicModelFileIsMissing() =
        runTest {
            val engine =
                FileBackedTextToSpeechEngine(
                    modelFile = File("missing/supertonic-3.onnx"),
                    delegate = FakeTextToSpeechEngine(),
                )

            try {
                engine.synthesize(SpeechRequest(text = "Hello", language = "English"))
                fail("Expected missing Supertonic model error")
            } catch (error: IllegalStateException) {
                assertEquals("Missing Supertonic model file: missing/supertonic-3.onnx", error.message)
            }
        }

    @Test
    fun ttsDelegatesWhenSupertonicModelFileExists() =
        runTest {
            val modelFile = kotlin.io.path.createTempFile().toFile()
            val engine =
                FileBackedTextToSpeechEngine(
                    modelFile = modelFile,
                    delegate = FakeTextToSpeechEngine(),
                )

            val result = engine.synthesize(SpeechRequest(text = "Hello", language = "English"))

            assertEquals("memory://tts/English", result.uri)
        }
}
