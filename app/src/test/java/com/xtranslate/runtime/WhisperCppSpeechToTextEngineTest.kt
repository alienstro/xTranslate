package com.xtranslate.runtime

import com.xtranslate.domain.AudioInput
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WhisperCppSpeechToTextEngineTest {
    @Test
    fun transcribesFileAudioWithConfiguredModel() =
        runTest {
            val modelFile = kotlin.io.path.createTempFile().toFile()
            val audioFile = kotlin.io.path.createTempFile(suffix = ".wav").toFile()
            val runtime = RecordingWhisperRuntime("  Hallo Welt  ")
            val engine = WhisperCppSpeechToTextEngine(modelFile = modelFile, runtime = runtime)

            val result = engine.transcribe(AudioInput(uri = audioFile.absolutePath, durationMillis = 1200L))

            assertEquals("Hallo Welt", result.text)
            assertEquals(modelFile.absolutePath, runtime.modelPath)
            assertEquals(audioFile.absolutePath, runtime.audioPath)
        }

    @Test
    fun transcribesFileUriAudioWithConfiguredModel() =
        runTest {
            val modelFile = kotlin.io.path.createTempFile().toFile()
            val audioFile = kotlin.io.path.createTempFile(suffix = ".wav").toFile()
            val runtime = RecordingWhisperRuntime("Kamusta")
            val engine = WhisperCppSpeechToTextEngine(modelFile = modelFile, runtime = runtime)

            val result = engine.transcribe(AudioInput(uri = audioFile.toURI().toString(), durationMillis = 1200L))

            assertEquals("Kamusta", result.text)
            assertEquals(audioFile.absolutePath, runtime.audioPath)
        }

    @Test
    fun rejectsMissingAudioFile() =
        runTest {
            val modelFile = kotlin.io.path.createTempFile().toFile()
            val engine = WhisperCppSpeechToTextEngine(modelFile = modelFile, runtime = RecordingWhisperRuntime("ignored"))
            val audioFile = File("missing/recording.wav")

            try {
                engine.transcribe(AudioInput(uri = audioFile.path, durationMillis = 0L))
                fail("Expected missing audio file error")
            } catch (error: IllegalArgumentException) {
                assertEquals("Missing audio file: missing/recording.wav", error.message)
            }
        }

    @Test
    fun wavReaderDecodesPcm16MonoSamples() {
        val audioFile = kotlin.io.path.createTempFile(suffix = ".wav").toFile()
        writePcm16Wav(audioFile, shortArrayOf(Short.MIN_VALUE, 0, Short.MAX_VALUE))

        val samples = Pcm16WavReader.readSamples(audioFile)

        assertArrayEquals(floatArrayOf(-1.0f, 0.0f, 0.9999695f), samples, 0.00001f)
    }

    @Test
    fun wavReaderRejectsNonWaveFiles() {
        val audioFile = kotlin.io.path.createTempFile(suffix = ".wav").toFile()
        audioFile.writeText("not a wave file")

        try {
            Pcm16WavReader.readSamples(audioFile)
            fail("Expected invalid WAV error")
        } catch (error: IllegalArgumentException) {
            assertEquals("Audio file is too short: ${audioFile.path.replace('\\', '/')}", error.message)
        }
    }
}

private fun writePcm16Wav(
    file: File,
    samples: ShortArray,
) {
    file.outputStream().use { output ->
        output.write(wavHeader(samples.size * 2))
        val buffer = ByteBuffer.allocate(samples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        samples.forEach { buffer.putShort(it) }
        output.write(buffer.array())
    }
}

private fun wavHeader(dataByteCount: Int): ByteArray =
    ByteBuffer.allocate(44)
        .order(ByteOrder.LITTLE_ENDIAN)
        .apply {
            putAscii("RIFF")
            putInt(36 + dataByteCount)
            putAscii("WAVE")
            putAscii("fmt ")
            putInt(16)
            putShort(1)
            putShort(1)
            putInt(16000)
            putInt(32000)
            putShort(2)
            putShort(16)
            putAscii("data")
            putInt(dataByteCount)
        }.array()

private fun ByteBuffer.putAscii(value: String) {
    value.forEach { put(it.code.toByte()) }
}

private fun ByteBuffer.putShort(value: Int) {
    putShort(value.toShort())
}

private class RecordingWhisperRuntime(
    private val transcript: String,
) : WhisperCppRuntime {
    var modelPath: String? = null
    var audioPath: String? = null

    override suspend fun transcribe(
        modelFile: File,
        audioFile: File,
    ): String {
        modelPath = modelFile.absolutePath
        audioPath = audioFile.absolutePath
        return transcript
    }
}
