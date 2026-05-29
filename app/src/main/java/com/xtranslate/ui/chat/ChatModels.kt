package com.xtranslate.ui.chat

/**
 * State used by the mobile chat screen.
 *
 * These models keep track of chat messages, the text the user is typing, the
 * target language, and which tab is currently selected.
 */
enum class ChatMessageKind {
    Source,
    OcrReview,
    Translation,
    System,
}

data class ChatMessage(
    val id: Long,
    val kind: ChatMessageKind,
    val text: String,
    val language: String? = null,
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val composerText: String = "",
    val targetLanguage: String = "English",
    val isBusy: Boolean = false,
    val selectedTab: AppTab = AppTab.Chat,
)

enum class AppTab {
    Chat,
    Models,
}
