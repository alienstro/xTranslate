package com.xtranslate.runtime

import com.xtranslate.domain.AudioInput
import com.xtranslate.domain.AudioOutput
import com.xtranslate.domain.ImageInput
import com.xtranslate.domain.OcrEngine
import com.xtranslate.domain.OcrResult
import com.xtranslate.domain.SpeechRequest
import com.xtranslate.domain.SpeechToTextEngine
import com.xtranslate.domain.TextToSpeechEngine
import com.xtranslate.domain.Transcript
import com.xtranslate.domain.TranslationEngine
import com.xtranslate.domain.TranslationRequest
import com.xtranslate.domain.TranslationResult

/**
 * Fake AI engines used while the real local engines are not connected yet.
 *
 * These classes make tests and early UI work possible before llama.cpp,
 * Whisper, and ONNX Runtime are added to the app.
 */
class FakeOcrEngine(
    private val text: String = "Extracted text from image",
) : OcrEngine {
    override suspend fun extractText(image: ImageInput): OcrResult = OcrResult(text = text, blocks = listOf(text))
}

class FakeTranslationEngine : TranslationEngine {
    override suspend fun translate(request: TranslationRequest): TranslationResult =
        TranslationResult(
            translatedText = "[${request.targetLanguage}] ${request.sourceText}",
            sourceText = request.sourceText,
            targetLanguage = request.targetLanguage,
        )
}

class FakeSpeechToTextEngine(
    private val transcript: String = "Voice transcript",
) : SpeechToTextEngine {
    override suspend fun transcribe(audio: AudioInput): Transcript =
        Transcript(
            text = transcript,
            confidence =
            0.99f,
        )
}

class FakeTextToSpeechEngine : TextToSpeechEngine {
    override suspend fun synthesize(request: SpeechRequest): AudioOutput =
        AudioOutput(uri = "memory://tts/${request.language}", durationMillis = 1200L)
}
