package com.xtranslate.runtime

import com.xtranslate.domain.AudioInput
import com.xtranslate.domain.SpeechRequest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

/**
 * Tests the file checks around placeholder speech engines.
 */
class FileBackedSpeechEnginesTest {
    @Test
    fun sttThrowsWhenWhisperModelFileIsMissing() =
        runTest {
            val engine =
                FileBackedSpeechToTextEngine(
                    modelFile = File("missing/whisper.bin"),
                    delegate = FakeSpeechToTextEngine(),
                )

            try {
                engine.transcribe(AudioInput(uri = "content://audio", durationMillis = 1000L))
                fail("Expected missing Whisper model error")
            } catch (error: IllegalStateException) {
                assertEquals("Missing Whisper model file: missing/whisper.bin", error.message)
            }
        }

    @Test
    fun sttDelegatesWhenWhisperModelFileExists() =
        runTest {
            val modelFile = kotlin.io.path.createTempFile().toFile()
            val engine =
                FileBackedSpeechToTextEngine(
                    modelFile = modelFile,
                    delegate = FakeSpeechToTextEngine(transcript = "Hello voice"),
                )

            val result = engine.transcribe(AudioInput(uri = "content://audio", durationMillis = 1000L))

            assertEquals("Hello voice", result.text)
        }

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
