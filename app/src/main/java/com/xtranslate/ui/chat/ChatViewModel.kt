package com.xtranslate.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xtranslate.domain.AudioInput
import com.xtranslate.domain.ImageInput
import com.xtranslate.domain.SpeechRequest
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
            runCatching {
                engineCoordinator.translateText(
                    TranslationRequest(
                        sourceText = text,
                        targetLanguage = state.value.targetLanguage,
                    ),
                )
            }.fold(
                onSuccess = { result ->
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
                },
                onFailure = { error -> showError(error) },
            )
        }
    }

    suspend fun translateImage(image: ImageInput) {
        mutableState.update { it.copy(isBusy = true) }
        runCatching {
            engineCoordinator.translateImage(
                image = image,
                targetLanguage = state.value.targetLanguage,
            )
        }.fold(
            onSuccess = { result ->
                mutableState.update {
                    it.copy(
                        isBusy = false,
                        messages =
                            it.messages +
                                ChatMessage(nextId++, ChatMessageKind.OcrReview, result.sourceText) +
                                ChatMessage(nextId++, ChatMessageKind.Translation, result.translatedText, result.targetLanguage),
                    )
                }
            },
            onFailure = { error -> showError(error) },
        )
    }

    fun transcribeVoicePlaceholder() {
        transcribeAudio(
            AudioInput(
                uri = "memory://voice-placeholder",
                durationMillis = 0L,
            ),
        )
    }

    fun transcribeAudio(audio: AudioInput) {
        mutableState.update {
            it.copy(
                isBusy = true,
                messages = it.messages.filterNot(::isMissingWhisperModelMessage),
            )
        }
        viewModelScope.launch {
            runCatching {
                engineCoordinator.transcribe(audio)
            }.fold(
                onSuccess = { transcript ->
                    mutableState.update {
                        it.copy(
                            isBusy = false,
                            composerText = transcript,
                        )
                    }
                },
                onFailure = { error -> showError(error) },
            )
        }
    }

    fun clearMissingWhisperModelMessages() {
        mutableState.update {
            it.copy(messages = it.messages.filterNot(::isMissingWhisperModelMessage))
        }
    }

    fun speakTranslationPlaceholder(message: ChatMessage) {
        if (message.kind != ChatMessageKind.Translation) return

        mutableState.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            runCatching {
                engineCoordinator.speak(
                    SpeechRequest(
                        text = message.text,
                        language = message.language ?: state.value.targetLanguage,
                    ),
                )
            }.fold(
                onSuccess = { audio ->
                    mutableState.update {
                        it.copy(
                            isBusy = false,
                            messages =
                                it.messages +
                                    ChatMessage(
                                        id = nextId++,
                                        kind = ChatMessageKind.System,
                                        text = "Speech ready: ${audio.uri}",
                                    ),
                        )
                    }
                },
                onFailure = { error -> showError(error) },
            )
        }
    }

    private fun showError(error: Throwable) {
        val message = error.message ?: "Local AI error: ${error::class.java.simpleName}"
        mutableState.update {
            it.copy(
                isBusy = false,
                selectedTab =
                    if (message.contains("Missing") && message.contains("model file")) {
                        AppTab.Models
                    } else {
                        it.selectedTab
                    },
                messages =
                    it.messages +
                        ChatMessage(
                            id = nextId++,
                            kind = ChatMessageKind.System,
                            text = message,
                        ),
            )
        }
    }

    private fun isMissingWhisperModelMessage(message: ChatMessage): Boolean =
        message.kind == ChatMessageKind.System &&
            message.text.contains("Missing Whisper model file")
}
