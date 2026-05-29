package com.xtranslate.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xtranslate.domain.ImageInput
import com.xtranslate.domain.TranslationRequest
import com.xtranslate.runtime.EngineCoordinator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Runs chat actions and updates chat state.
 *
 * The UI calls this class when the user types text, sends a translation, or
 * starts an image translation flow. It uses the engine coordinator instead of
 * talking to OCR or translation engines directly.
 */
class ChatViewModel(
    private val engineCoordinator: EngineCoordinator,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = mutableState

    private var nextId = 1L

    fun updateComposer(text: String) {
        mutableState.update { it.copy(composerText = text) }
    }

    fun updateTargetLanguage(language: String) {
        mutableState.update { it.copy(targetLanguage = language) }
    }

    fun selectTab(tab: AppTab) {
        mutableState.update { it.copy(selectedTab = tab) }
    }

    fun sendText() {
        val text = state.value.composerText.trim()
        if (text.isEmpty()) return

        mutableState.update {
            it.copy(
                composerText = "",
                isBusy = true,
                messages = it.messages + ChatMessage(nextId++, ChatMessageKind.Source, text),
            )
        }

        viewModelScope.launch {
            val targetLanguage = state.value.targetLanguage
            val result =
                engineCoordinator.translateText(
                    TranslationRequest(
                        sourceText = text,
                        targetLanguage = targetLanguage,
                    ),
                )
            mutableState.update {
                it.copy(
                    isBusy = false,
                    messages =
                        it.messages +
                            ChatMessage(
                                id = nextId++,
                                kind = ChatMessageKind.Translation,
                                text = result.translatedText,
                                language = result.targetLanguage,
                            ),
                )
            }
        }
    }

    suspend fun translateImage(image: ImageInput) {
        mutableState.update { it.copy(isBusy = true) }
        val result =
            engineCoordinator.translateImage(
                image = image,
                targetLanguage = state.value.targetLanguage,
            )
        mutableState.update {
            it.copy(
                isBusy = false,
                messages =
                    it.messages +
                        ChatMessage(nextId++, ChatMessageKind.OcrReview, result.sourceText) +
                        ChatMessage(nextId++, ChatMessageKind.Translation, result.translatedText, result.targetLanguage),
            )
        }
    }
}
