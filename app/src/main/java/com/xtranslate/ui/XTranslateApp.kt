package com.xtranslate.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.xtranslate.model.LocalModelPaths
import com.xtranslate.model.ModelDownloadProgress
import com.xtranslate.model.ModelPack
import com.xtranslate.model.ModelStore
import com.xtranslate.ui.chat.AppTab
import com.xtranslate.ui.chat.ChatMessage
import com.xtranslate.ui.chat.ChatScreen
import com.xtranslate.ui.chat.ChatViewModel
import com.xtranslate.ui.models.ModelsScreen

/**
 * Main app shell with Chat and Models tabs.
 */
@Composable
fun XTranslateApp(
    chatViewModel: ChatViewModel,
    modelStore: ModelStore,
    modelPaths: LocalModelPaths,
    onPickImage: () -> Unit,
    isRecordingVoice: Boolean,
    onMic: () -> Unit,
    onSpeakTranslation: (ChatMessage) -> Unit,
    onRunLocalTextTest: () -> Unit,
    onRunLocalOcrTest: () -> Unit,
    onRunLocalSttTest: () -> Unit,
    onRunLocalTtsTest: () -> Unit,
    onImportTranslationModel: () -> Unit,
    onImportOcrModel: () -> Unit,
    onImportOcrProjector: () -> Unit,
    onImportWhisperModel: () -> Unit,
    onImportSupertonicModel: () -> Unit,
    onDownloadModelPack: (ModelPack) -> Unit,
    onDeleteModelPack: (ModelPack) -> Unit,
    localTextTestStatus: String?,
    localOcrTestStatus: String?,
    speechTestStatus: String?,
    importStatus: String?,
    ocrImportStatus: String?,
    speechImportStatus: String?,
    modelDownloadStatus: String?,
    modelDownloadProgress: ModelDownloadProgress?,
    modelStateRefreshKey: Int,
) {
    val state by chatViewModel.state.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = state.selectedTab == AppTab.Chat,
                    onClick = { chatViewModel.selectTab(AppTab.Chat) },
                    label = { Text("Chat") },
                    icon = {
                        Icon(
                            imageVector =
                                if (state.selectedTab == AppTab.Chat) {
                                    Icons.AutoMirrored.Filled.Chat
                                } else {
                                    Icons.AutoMirrored.Outlined.Chat
                                },
                            contentDescription = "Chat",
                        )
                    },
                )
                NavigationBarItem(
                    selected = state.selectedTab == AppTab.Models,
                    onClick = { chatViewModel.selectTab(AppTab.Models) },
                    label = { Text("Models") },
                    icon = {
                        Icon(
                            imageVector = if (state.selectedTab == AppTab.Models) Icons.Filled.Storage else Icons.Outlined.Storage,
                            contentDescription = "Models",
                        )
                    },
                )
            }
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            when (state.selectedTab) {
                AppTab.Chat ->
                    ChatScreen(
                        state = state,
                        isRecordingVoice = isRecordingVoice,
                        onComposerChange = chatViewModel::updateComposer,
                        onTargetLanguageChange = chatViewModel::updateTargetLanguage,
                        onSend = chatViewModel::sendText,
                        onImage = onPickImage,
                        onMic = onMic,
                        onSpeakTranslation = onSpeakTranslation,
                    )

                AppTab.Models ->
                    ModelsScreen(
                        modelStore = modelStore,
                        modelPaths = modelPaths,
                        onRunLocalTextTest = onRunLocalTextTest,
                        onRunLocalOcrTest = onRunLocalOcrTest,
                        onRunLocalSttTest = onRunLocalSttTest,
                        onRunLocalTtsTest = onRunLocalTtsTest,
                        onImportTranslationModel = onImportTranslationModel,
                        onImportOcrModel = onImportOcrModel,
                        onImportOcrProjector = onImportOcrProjector,
                        onImportWhisperModel = onImportWhisperModel,
                        onImportSupertonicModel = onImportSupertonicModel,
                        onDownloadModelPack = onDownloadModelPack,
                        onDeleteModelPack = onDeleteModelPack,
                        localTextTestStatus = localTextTestStatus,
                        localOcrTestStatus = localOcrTestStatus,
                        speechTestStatus = speechTestStatus,
                        importStatus = importStatus,
                        ocrImportStatus = ocrImportStatus,
                        speechImportStatus = speechImportStatus,
                        modelDownloadStatus = modelDownloadStatus,
                        modelDownloadProgress = modelDownloadProgress,
                        modelStateRefreshKey = modelStateRefreshKey,
                    )
            }
        }
    }
}
