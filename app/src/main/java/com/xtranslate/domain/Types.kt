package com.xtranslate.domain

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