package com.xtranslate.runtime

import com.xtranslate.domain.AudioOutput
import com.xtranslate.domain.SpeechRequest
import com.xtranslate.domain.TextToSpeechEngine
import java.io.File

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
