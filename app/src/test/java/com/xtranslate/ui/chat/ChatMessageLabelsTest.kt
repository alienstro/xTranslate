package com.xtranslate.ui.chat

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests user-facing labels for chat messages.
 */
class ChatMessageLabelsTest {
    @Test
    fun labelsUsePlainUserFacingText() {
        assertEquals("Source", ChatMessageLabels.labelFor(ChatMessageKind.Source))
        assertEquals("Detected text", ChatMessageLabels.labelFor(ChatMessageKind.OcrReview))
        assertEquals("Translation", ChatMessageLabels.labelFor(ChatMessageKind.Translation))
        assertEquals("Notice", ChatMessageLabels.labelFor(ChatMessageKind.System))
    }
}
