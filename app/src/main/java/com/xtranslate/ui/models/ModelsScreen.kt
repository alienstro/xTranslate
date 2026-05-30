package com.xtranslate.ui.models

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xtranslate.model.LocalModelPaths
import com.xtranslate.model.ModelDownloadProgress
import com.xtranslate.model.ModelInstallState
import com.xtranslate.model.ModelPack
import com.xtranslate.model.ModelStore

@Composable
fun ModelsScreen(
    modelStore: ModelStore,
    modelPaths: LocalModelPaths,
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Models",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        modelDownloadStatus?.let { status ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        modelDownloadProgress?.let { progress ->
            DownloadProgressCard(progress)
        }

        key(modelStateRefreshKey) {
            modelStore.packs().forEach { pack ->
                val state = modelStore.state(pack.id)
                val uiModel = ModelPackUiFormatter.format(
                    pack = pack,
                    state = state,
                    modelPaths = modelPaths,
                )
                val importAction: (() -> Unit)? = when (pack.id) {
                    "translation.multilingual.gguf" -> onImportTranslationModel
                    "ocr.paddleocr-vl-1_5.q4" -> onImportOcrModel
                    "stt.whisper" -> onImportWhisperModel
                    "tts.supertonic-3" -> onImportSupertonicModel
                    else -> null
                }
                ModelCard(
                    uiModel = uiModel,
                    pack = pack,
                    state = state,
                    onDownload = { onDownloadModelPack(pack) },
                    onDelete = { onDeleteModelPack(pack) },
                    onImport = importAction,
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        )

        Text(
            text = "Developer Tools",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )

        DevToolSection(
            label = "Text translation test",
            status = localTextTestStatus,
            onRun = onRunLocalTextTest,
        )
        DevToolSection(
            label = "OCR test",
            status = localOcrTestStatus,
            onRun = onRunLocalOcrTest,
        )
        DevToolSection(
            label = "Speech-to-text test",
            status = speechTestStatus,
            onRun = onRunLocalSttTest,
        )
        DevToolSection(
            label = "Text-to-speech test",
            status = null,
            onRun = onRunLocalTtsTest,
        )
        speechTestStatus?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        listOf(
            importStatus,
            ocrImportStatus,
            speechImportStatus,
        ).forEach { status ->
            status?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ModelCard(
    uiModel: ModelPackUi,
    pack: ModelPack,
    state: ModelInstallState,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onImport: (() -> Unit)?,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = uiModel.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(state = state)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetaChip(text = uiModel.engineLabel)
                MetaChip(text = uiModel.deviceFitLabel.removePrefix("Device fit: "))
                if (uiModel.sizeLabel != "Size: Unknown") {
                    MetaChip(text = uiModel.sizeLabel.removePrefix("Size: "))
                }
            }

            Text(
                text = uiModel.fileProgressLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (state == ModelInstallState.Installed) {
                uiModel.requiredFileLabels.forEach { label ->
                    Text(
                        text = label.substringAfterLast('/'),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when (state) {
                    ModelInstallState.Missing, ModelInstallState.Failed -> {
                        Button(
                            onClick = onDownload,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text("Download")
                        }
                        if (onImport != null) {
                            OutlinedButton(
                                onClick = onImport,
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Text("Import")
                            }
                        }
                    }
                    ModelInstallState.Installed -> {
                        if (onImport != null) {
                            OutlinedButton(
                                onClick = onImport,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Text("Replace")
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        OutlinedButton(
                            onClick = onDelete,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                            ),
                        ) {
                            Text("Delete")
                        }
                    }
                    ModelInstallState.Downloading -> {
                        Text(
                            text = "Downloading...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(state: ModelInstallState) {
    val (label, color) = when (state) {
        ModelInstallState.Installed -> "Installed" to Color(0xFF3D9970)
        ModelInstallState.Missing -> "Missing" to Color(0xFF888888)
        ModelInstallState.Downloading -> "Downloading" to Color(0xFFD4A017)
        ModelInstallState.Failed -> "Failed" to Color(0xFFCC3333)
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun MetaChip(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
        )
    }
}

@Composable
private fun DownloadProgressCard(progress: ModelDownloadProgress) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val percent = progress.percent
            if (percent == null) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                LinearProgressIndicator(
                    progress = { percent / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Text(
                text = progress.readableLabel(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DevToolSection(
    label: String,
    status: String?,
    onRun: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextButton(
            onClick = onRun,
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        status?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private fun ModelDownloadProgress.readableLabel(): String {
    val downloadedMb = bytesDownloaded / BYTES_PER_MB
    val total = totalBytes
    return if (total == null) {
        "$fileName — $downloadedMb MB"
    } else {
        val totalMb = total / BYTES_PER_MB
        "$fileName — ${percent ?: 0}% ($downloadedMb / $totalMb MB)"
    }
}

private const val BYTES_PER_MB = 1024L * 1024L
