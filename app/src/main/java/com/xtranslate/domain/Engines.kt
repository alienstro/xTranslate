package com.xtranslate.domain

/**
 * Interfaces for the app's AI engines.
 *
 * The UI will call these interfaces instead of calling llama.cpp, Whisper, or
 * ONNX directly. This lets the app use fake engines while the UI is being
 * built, then swap in the real local engines later without changing the UI.
 */
interface OcrEngine {
    suspend fun extractText(image: ImageInput): OcrResult
}

interface TranslationEngine {
    suspend fun translate(request: TranslationRequest): TranslationResult
}

interface SpeechToTextEngine {
    suspend fun transcribe(audio: AudioInput): Transcript
}

interface TextToSpeechEngine {
    suspend fun synthesize(request: SpeechRequest): AudioOutput
}
