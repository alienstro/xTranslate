package com.xtranslate.domain

/**
 * Prompt text sent to the local language models.
 *
 * The OCR prompt tells the image model to extract text only. The translation
 * prompt tells the translation model which language to use and what source
 * text to translate.
 */
object Prompts {
    fun ocrExtractionPrompt(): String = """
        You are an OCR document parser.
        Extract all visible text from the image.
        Preserve reading order as much as possible.
        Return plain text with simple line breaks.
        Do not translate, summarize, or explain the text.
    """.trimIndent()

    fun translationPrompt(request: TranslationRequest): String {
        val source = request.sourceLanguage ?: "auto-detected source language"
        return """
            You are a translation engine.
            Task: translate the source text from $source into ${request.targetLanguage}.
            Output rules:
            - Return only the translated text.
            - Do not copy the source text unless it is a name, number, or untranslatable term.
            - Do not explain, summarize, add notes, or add labels.
            - Preserve names, numbers, punctuation, and useful line breaks.

            Source text:
            ${request.sourceText}
        """.trimIndent()
    }
}
