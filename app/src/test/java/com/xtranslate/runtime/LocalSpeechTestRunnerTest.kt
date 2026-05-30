package com.xtranslate.runtime

import com.xtranslate.model.LocalModelPaths
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

/**
 * Tests quick local speech smoke tests for the Models screen.
 */
class LocalSpeechTestRunnerTest {
    @Test
    fun transcribeSampleAudioReturnsPlaceholderTranscriptWhenWhisperModelExists() =
        runTest {
            val root = createTempDirectory().toFile()
            val modelPaths = LocalModelPaths(root)
            modelPaths.whisperModelFile().parentFile?.mkdirs()
            modelPaths.whisperModelFile().writeText("model")
            val runner = LocalSpeechTestRunner(modelPaths)

            val result = runner.transcribeSampleAudio()

            assertEquals("Voice transcript", result)
        }

    @Test
    fun transcribeSampleAudioThrowsWhenWhisperModelIsMissing() =
        runTest {
            val root = createTempDirectory().toFile()
            val runner = LocalSpeechTestRunner(LocalModelPaths(root))

            try {
                runner.transcribeSampleAudio()
                fail("Expected missing Whisper model error")
            } catch (error: IllegalStateException) {
                assertEquals(
                    "Missing Whisper model file: ${File(root, "models/stt/ggml-large-v3-turbo-q8_0.bin").path.replace('\\', '/')}",
                    error.message,
                )
            }
        }

    @Test
    fun synthesizeSampleSpeechReturnsPlaceholderAudioWhenSupertonicModelExists() =
        runTest {
            val root = createTempDirectory().toFile()
            val modelPaths = LocalModelPaths(root)
            modelPaths.supertonicModelFile().parentFile?.mkdirs()
            modelPaths.supertonicModelFile().writeText("model")
            val runner = LocalSpeechTestRunner(modelPaths)

            val result = runner.synthesizeSampleSpeech()

            assertEquals("memory://tts/English", result)
        }

    @Test
    fun synthesizeSampleSpeechThrowsWhenSupertonicModelIsMissing() =
        runTest {
            val root = createTempDirectory().toFile()
            val runner = LocalSpeechTestRunner(LocalModelPaths(root))

            try {
                runner.synthesizeSampleSpeech()
                fail("Expected missing Supertonic model error")
            } catch (error: IllegalStateException) {
                assertEquals(
                    "Missing Supertonic model file: ${File(root, "models/tts/supertonic-3.onnx").path.replace('\\', '/')}",
                    error.message,
                )
            }
        }
}
