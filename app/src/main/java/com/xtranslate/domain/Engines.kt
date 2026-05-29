package com.xtranslate.domain

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