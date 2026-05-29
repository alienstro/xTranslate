package com.xtranslate.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Mobile-first chat screen for text, image, and voice translation.
 */
@Composable
fun ChatScreen(
    state: ChatUiState,
    onComposerChange: (String) -> Unit,
    onTargetLanguageChange: (String) -> Unit,
    onSend: () -> Unit,
    onImage: () -> Unit,
    onMic: () -> Unit,
    onSpeakTranslation: (ChatMessage) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "xTranslate",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.messages, key = { it.id }) { message ->
                ChatMessageCard(
                    message = message,
                    onSpeakTranslation = onSpeakTranslation,
                )
            }
        }

        Surface(
            tonalElevation = 2.dp,
            shape = RoundedCornerShape(18.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = state.composerText,
                    onValueChange = onComposerChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Type, speak, or add an image") },
                    minLines = 2,
                )
                TargetLanguagePicker(
                    selectedLanguage = state.targetLanguage,
                    onTargetLanguageChange = onTargetLanguageChange,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onImage) {
                        Text("IMG")
                    }
                    IconButton(onClick = onMic) {
                        Text("MIC")
                    }
                    Button(
                        onClick = onSend,
                        modifier = Modifier.weight(1f),
                        enabled = !state.isBusy,
                    ) {
                        Text(if (state.isBusy) "Working" else "Translate")
                    }
                }
            }
        }
    }
}

@Composable
private fun TargetLanguagePicker(
    selectedLanguage: String,
    onTargetLanguageChange: (String) -> Unit,
) {
    val languages =
        listOf(
            "English",
            "Filipino",
            "Japanese",
            "Korean",
            "Chinese",
            "Spanish",
            "French",
            "German",
        )
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Target",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = { expanded = true }) {
            Text(selectedLanguage)
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                languages.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(language) },
                        onClick = {
                            onTargetLanguageChange(language)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageCard(
    message: ChatMessage,
    onSpeakTranslation: (ChatMessage) -> Unit,
) {
    val containerColor =
        when (message.kind) {
            ChatMessageKind.Source -> MaterialTheme.colorScheme.surface
            ChatMessageKind.OcrReview -> MaterialTheme.colorScheme.surfaceVariant
            ChatMessageKind.Translation -> MaterialTheme.colorScheme.surfaceVariant
            ChatMessageKind.System -> MaterialTheme.colorScheme.surface
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = ChatMessageLabels.labelFor(message.kind),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(text = message.text)
            if (message.kind == ChatMessageKind.Translation) {
                TextButton(onClick = { onSpeakTranslation(message) }) {
                    Text("Speak")
                }
            }
        }
    }
}
