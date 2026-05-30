package com.xtranslate.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.xtranslate.model.LocalModelPaths
import com.xtranslate.model.ModelStore
import com.xtranslate.ui.chat.AppTab
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
    onRunLocalTextTest: () -> Unit,
    onRunLocalOcrTest: () -> Unit,
    onRunLocalSttTest: () -> Unit,
    onRunLocalTtsTest: () -> Unit,
    onImportTranslationModel: () -> Unit,
    onImportOcrModel: () -> Unit,
    onImportOcrProjector: () -> Unit,
    onImportWhisperModel: () -> Unit,
    onImportSupertonicModel: () -> Unit,
    localTextTestStatus: String?,
    localOcrTestStatus: String?,
    speechTestStatus: String?,
    importStatus: String?,
    ocrImportStatus: String?,
    speechImportStatus: String?,
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
                    icon = { Text("C") },
                )
                NavigationBarItem(
                    selected = state.selectedTab == AppTab.Models,
                    onClick = { chatViewModel.selectTab(AppTab.Models) },
                    label = { Text("Models") },
                    icon = { Text("M") },
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
                        onComposerChange = chatViewModel::updateComposer,
                        onTargetLanguageChange = chatViewModel::updateTargetLanguage,
                        onSend = chatViewModel::sendText,
                        onImage = onPickImage,
                        onMic = chatViewModel::transcribeVoicePlaceholder,
                        onSpeakTranslation = chatViewModel::speakTranslationPlaceholder,
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
                        localTextTestStatus = localTextTestStatus,
                        localOcrTestStatus = localOcrTestStatus,
                        speechTestStatus = speechTestStatus,
                        importStatus = importStatus,
                        ocrImportStatus = ocrImportStatus,
                        speechImportStatus = speechImportStatus,
                        modelStateRefreshKey = modelStateRefreshKey,
                    )
            }
        }
    }
}
