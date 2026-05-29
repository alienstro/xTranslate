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
    onRunLocalTextTest: () -> Unit,
    onImportTranslationModel: () -> Unit,
    onImportOcrModel: () -> Unit,
    onImportOcrProjector: () -> Unit,
    localTextTestStatus: String?,
    importStatus: String?,
    ocrImportStatus: String?,
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
                        onSend = chatViewModel::sendText,
                        onImage = {
                            chatViewModel.updateComposer("Image flow will open camera/gallery in a later step.")
                        },
                        onMic = {
                            chatViewModel.updateComposer("Voice flow will record audio in a later step.")
                        },
                    )

                AppTab.Models ->
                    ModelsScreen(
                        modelStore = modelStore,
                        modelPaths = modelPaths,
                        onRunLocalTextTest = onRunLocalTextTest,
                        onImportTranslationModel = onImportTranslationModel,
                        onImportOcrModel = onImportOcrModel,
                        onImportOcrProjector = onImportOcrProjector,
                        localTextTestStatus = localTextTestStatus,
                        importStatus = importStatus,
                        ocrImportStatus = ocrImportStatus,
                    )
            }
        }
    }
}
