package com.xtranslate.runtime

import com.xtranslate.domain.AudioInput
import com.xtranslate.domain.AudioOutput
import com.xtranslate.domain.ImageInput
import com.xtranslate.domain.OcrEngine
import com.xtranslate.domain.SpeechRequest
import com.xtranslate.domain.SpeechToTextEngine
import com.xtranslate.domain.TextToSpeechEngine
import com.xtranslate.domain.TranslationEngine
import com.xtranslate.domain.TranslationRequest
import com.xtranslate.domain.TranslationResult

/**
 * Controls which AI engine is loaded for the current task.
 *
 * Low-end phones should not keep every engine loaded at once. This class gives
 * the app one place to load, reuse, and unload OCR, translation, speech input,
 * and speech output engines.
 */
enum class LoadedEngine {
    Ocr,
    Translation,
    Stt,
    Tts,
}

class EngineCoordinator(
    private val ocrEngine: OcrEngine,
    private val translationEngine: TranslationEngine,
    private val sttEngine: SpeechToTextEngine,
    private val ttsEngine: TextToSpeechEngine,
    private val lowMemoryMode: Boolean,
) {
    private val mutableLoadedEngines = linkedSetOf<LoadedEngine>()

    val loadedEngines: Set<LoadedEngine>
        get() = mutableLoadedEngines.toSet()

    suspend fun translateText(request: TranslationRequest): TranslationResult {
        load(LoadedEngine.Translation)
        return try {
            translationEngine.translate(request)
        } catch (error: Throwable) {
            if (lowMemoryMode) {
                unload(LoadedEngine.Translation)
            }
            throw error
        }
    }

    suspend fun translateImage(
        image: ImageInput,
        targetLanguage: String,
        sourceLanguage: String? = null,
    ): TranslationResult {
        load(LoadedEngine.Ocr)
        val ocr =
            try {
                ocrEngine.extractText(image)
            } finally {
                if (lowMemoryMode) {
                    unload(LoadedEngine.Ocr)
                }
            }

        return translateText(
            TranslationRequest(
                sourceText = ocr.text,
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLanguage,
            ),
        )
    }

    suspend fun transcribe(audio: AudioInput): String {
        load(LoadedEngine.Stt)
        val transcript =
            try {
                sttEngine.transcribe(audio).text
            } finally {
                if (lowMemoryMode) {
                    unload(LoadedEngine.Stt)
                }
            }

        return transcript
    }

    suspend fun speak(request: SpeechRequest): AudioOutput {
        load(LoadedEngine.Tts)
        val audio =
            try {
                ttsEngine.synthesize(request)
            } finally {
                if (lowMemoryMode) {
                    unload(LoadedEngine.Tts)
                }
            }

        return audio
    }

    fun unloadIdle() {
        mutableLoadedEngines.clear()
    }

    private fun load(engine: LoadedEngine) {
        if (lowMemoryMode) {
            mutableLoadedEngines.clear()
        }
        mutableLoadedEngines.add(engine)
    }

    private fun unload(engine: LoadedEngine) {
        mutableLoadedEngines.remove(engine)
    }
}
