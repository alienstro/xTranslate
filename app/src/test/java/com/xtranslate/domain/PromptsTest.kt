package com.xtranslate.domain

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the prompt text used by the AI models.
 *
 * These tests make sure the OCR prompt still says not to translate, and the
 * translation prompt still includes the source text and target language.
 */
class PromptsTest {
    @Test
    fun ocrPromptRequestsPlainExtractedText() {
        val prompt = Prompts.ocrExtractionPrompt()

        assertTrue(prompt.contains("extract all visible text", ignoreCase = true))
        assertTrue(prompt.contains("do not translate", ignoreCase = true))
    }

    @Test
    fun translationPromptIncludesTargetLanguageAndSourceText() {
        val prompt =
            Prompts.translationPrompt(
                request =
                    TranslationRequest(
                        sourceText = "Hello",
                        sourceLanguage = "English",
                        targetLanguage = "Filipino",
                    ),
            )

        assertTrue(prompt.contains("Filipino"))
        assertTrue(prompt.contains("English"))
        assertTrue(prompt.contains("Hello"))
        assertTrue(prompt.contains("only the translation", ignoreCase = true))
    }
}
