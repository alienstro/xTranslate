package com.xtranslate.ui.chat

/**
 * User-facing labels for chat message cards.
 */
object ChatMessageLabels {
    fun labelFor(kind: ChatMessageKind): String =
        when (kind) {
            ChatMessageKind.Source -> "Source"
            ChatMessageKind.OcrReview -> "Detected text"
            ChatMessageKind.Translation -> "Translation"
            ChatMessageKind.System -> "Notice"
        }
}
