package com.xtranslate.runtime

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class LocalAudioRecorder {
    private val scope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    private var recorderThread: AudioRecordThread? = null

    suspend fun startRecording(
        outputFile: File,
        onError: (Exception) -> Unit,
    ) = withContext(scope.coroutineContext) {
        recorderThread = AudioRecordThread(outputFile, onError).also { it.start() }
    }

    suspend fun stopRecording(): File? =
        withContext(scope.coroutineContext) {
            val recorder = recorderThread ?: return@withContext null
            recorder.stopRecording()
            recorder.join()
            recorderThread = null
            recorder.outputFile
        }
}

private class AudioRecordThread(
    val outputFile: File,
    private val onError: (Exception) -> Unit,
) : Thread("xtranslate-audio-recorder") {
    private val shouldStop = AtomicBoolean(false)

    @SuppressLint("MissingPermission")
    override fun run() {
        try {
            val minBufferSize =
                AudioRecord.getMinBufferSize(
                    SAMPLE_RATE_HZ,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                )
            val bufferSize = minBufferSize.coerceAtLeast(SAMPLE_RATE_HZ / 2)
            val buffer = ShortArray(bufferSize / 2)
            val samples = mutableListOf<Short>()
            val audioRecord =
                AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE_HZ,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                )

            try {
                outputFile.parentFile?.mkdirs()
                audioRecord.startRecording()
                while (!shouldStop.get()) {
                    val read = audioRecord.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        for (index in 0 until read) {
                            samples.add(buffer[index])
                        }
                    } else if (read < 0) {
                        error("AudioRecord read failed: $read")
                    }
                }
                audioRecord.stop()
                writePcm16Wav(outputFile, samples.toShortArray())
            } finally {
                audioRecord.release()
            }
        } catch (error: Exception) {
            onError(error)
        }
    }

    fun stopRecording() {
        shouldStop.set(true)
    }
}

private fun writePcm16Wav(
    file: File,
    samples: ShortArray,
) {
    file.outputStream().use { output ->
        output.write(wavHeader(dataByteCount = samples.size * 2))
        val buffer = ByteBuffer.allocate(samples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        samples.forEach { sample -> buffer.putShort(sample) }
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
            putInt(SAMPLE_RATE_HZ)
            putInt(SAMPLE_RATE_HZ * 2)
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

private const val SAMPLE_RATE_HZ = 16_000
