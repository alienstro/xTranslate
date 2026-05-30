package com.xtranslate.runtime

import com.xtranslate.domain.AudioInput
import com.xtranslate.domain.AudioOutput
import com.xtranslate.domain.SpeechRequest
import com.xtranslate.domain.SpeechToTextEngine
import com.xtranslate.domain.TextToSpeechEngine
import com.xtranslate.domain.Transcript
import java.io.File

/**
 * Speech placeholders that require imported local model files.
 *
 * These wrappers keep the app honest before real Whisper and Supertonic
 * runtimes are connected. They fail when the expected file is missing and
 * delegate to the current placeholder engine when the file exists.
 */
class FileBackedSpeechToTextEngine(
    private val modelFile: File,
    private val delegate: SpeechToTextEngine,
) : SpeechToTextEngine {
    override suspend fun transcribe(audio: AudioInput): Transcript {
        check(modelFile.exists()) { "Missing Whisper model file: ${modelFile.normalizedPath()}" }
        return delegate.transcribe(audio)
    }
}

class FileBackedTextToSpeechEngine(
    private val modelFile: File,
    private val delegate: TextToSpeechEngine,
) : TextToSpeechEngine {
    override suspend fun synthesize(request: SpeechRequest): AudioOutput {
        check(modelFile.exists()) { "Missing Supertonic model file: ${modelFile.normalizedPath()}" }
        return delegate.synthesize(request)
    }
}

private fun File.normalizedPath(): String = path.replace('\\', '/')
