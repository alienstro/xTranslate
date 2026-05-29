package com.xtranslate.domain

/**
 * Simple data objects used by the app.
 *
 * These objects describe the text, image, and audio data that moves between
 * the UI and the AI engines. Keeping them here makes the rest of the app use
 * the same names and shapes for translation, OCR, speech input, and speech
 * output.
 */
data class ImageInput(
    val uri: String,
    val description: String? = null,
)

data class AudioInput(
    val uri: String,
    val durationMillis: Long,
)

data class OcrResult(
    val text: String,
    val blocks: List<String> = emptyList(),
)

data class TranslationRequest(
    val sourceText: String,
    val sourceLanguage: String? = null,
    val targetLanguage: String,
)

data class TranslationResult(
    val translatedText: String,
    val sourceText: String,
    val targetLanguage: String,
)

data class Transcript(
    val text: String,
    val confidence: Float? = null,
)

data class SpeechRequest(
    val text: String,
    val language: String,
)

data class AudioOutput(
    val uri: String,
    val durationMillis: Long? = null,
)
