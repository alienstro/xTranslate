package com.xtranslate.runtime

import com.whispercpp.whisper.WhisperContext
import com.xtranslate.domain.AudioInput
import com.xtranslate.domain.SpeechToTextEngine
import com.xtranslate.domain.Transcript
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

interface WhisperCppRuntime {
    suspend fun transcribe(
        modelFile: File,
        audioFile: File,
    ): String
}

class WhisperCppSpeechToTextEngine(
    private val modelFile: File,
    private val runtime: WhisperCppRuntime = NativeWhisperCppRuntime(),
) : SpeechToTextEngine {
    override suspend fun transcribe(audio: AudioInput): Transcript {
        val audioFile = audio.toFile()
        require(audioFile.exists()) { "Missing audio file: ${audioFile.normalizedPath()}" }

        return Transcript(text = runtime.transcribe(modelFile, audioFile).trim())
    }

    private fun AudioInput.toFile(): File =
        if (uri.startsWith("file:")) {
            File(java.net.URI(uri))
        } else {
            File(uri)
        }
}

class NativeWhisperCppRuntime : WhisperCppRuntime {
    override suspend fun transcribe(
        modelFile: File,
        audioFile: File,
    ): String {
        val context = WhisperContext.createContextFromFile(modelFile.absolutePath)
        return try {
            context.transcribeData(Pcm16WavReader.readSamples(audioFile), printTimestamp = false)
        } finally {
            context.release()
        }
    }
}

object Pcm16WavReader {
    fun readSamples(file: File): FloatArray {
        val bytes = file.readBytes()
        require(bytes.size > 44) { "Audio file is too short: ${file.normalizedPath()}" }
        require(String(bytes, 0, 4) == "RIFF" && String(bytes, 8, 4) == "WAVE") {
            "Audio file must be WAV PCM: ${file.normalizedPath()}"
        }

        val dataOffset = findDataChunk(bytes)
        val dataSize = littleEndianInt(bytes, dataOffset + 4)
        val start = dataOffset + 8
        val end = (start + dataSize).coerceAtMost(bytes.size)
        val sampleCount = (end - start) / 2
        val samples = FloatArray(sampleCount)
        val buffer = ByteBuffer.wrap(bytes, start, sampleCount * 2).order(ByteOrder.LITTLE_ENDIAN)

        for (index in 0 until sampleCount) {
            samples[index] = buffer.short / 32768.0f
        }

        return samples
    }

    private fun findDataChunk(bytes: ByteArray): Int {
        var offset = 12
        while (offset + 8 <= bytes.size) {
            val chunkName = String(bytes, offset, 4)
            val chunkSize = littleEndianInt(bytes, offset + 4)
            if (chunkName == "data") {
                return offset
            }
            offset += 8 + chunkSize
        }
        error("Audio file has no data chunk")
    }

    private fun littleEndianInt(
        bytes: ByteArray,
        offset: Int,
    ): Int =
        ByteBuffer.wrap(bytes, offset, 4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .int
}

private fun File.normalizedPath(): String = path.replace('\\', '/')
