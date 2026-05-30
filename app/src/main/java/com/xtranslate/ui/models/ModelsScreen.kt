package com.xtranslate.ui.models

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xtranslate.model.LocalModelPaths
import com.xtranslate.model.ModelStore

/**
 * Shows the local model packs the app will need.
 */
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
    localTextTestStatus: String?,
    localOcrTestStatus: String?,
    speechTestStatus: String?,
    importStatus: String?,
    ocrImportStatus: String?,
    speechImportStatus: String?,
    modelStateRefreshKey: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Models",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        ModelActionSection(title = "Test") {
            Button(onClick = onRunLocalTextTest) {
                Text("Run local text test")
            }
            localTextTestStatus?.let { status ->
                Text(text = status)
            }
        }
        ModelActionSection(title = "Translation") {
            Button(onClick = onImportTranslationModel) {
                Text("Import translation GGUF")
            }
            importStatus?.let { status ->
                Text(text = status)
            }
        }
        ModelActionSection(title = "Image OCR") {
            Button(onClick = onRunLocalOcrTest) {
                Text("Run local OCR test")
            }
            localOcrTestStatus?.let { status ->
                Text(text = status)
            }
            Button(onClick = onImportOcrModel) {
                Text("Import OCR GGUF")
            }
            Button(onClick = onImportOcrProjector) {
                Text("Import OCR projector")
            }
            ocrImportStatus?.let { status ->
                Text(text = status)
            }
        }
        ModelActionSection(title = "Speech") {
            Button(onClick = onRunLocalSttTest) {
                Text("Run local STT test")
            }
            Button(onClick = onRunLocalTtsTest) {
                Text("Run local TTS test")
            }
            speechTestStatus?.let { status ->
                Text(text = status)
            }
            Button(onClick = onImportWhisperModel) {
                Text("Import Whisper STT")
            }
            Button(onClick = onImportSupertonicModel) {
                Text("Import Supertonic TTS")
            }
            speechImportStatus?.let { status ->
                Text(text = status)
            }
        }
        key(modelStateRefreshKey) {
            modelStore.packs().forEach { pack ->
                val uiModel =
                    ModelPackUiFormatter.format(
                        pack = pack,
                        state = modelStore.state(pack.id),
                        modelPaths = modelPaths,
                    )

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(text = uiModel.displayName, fontWeight = FontWeight.SemiBold)
                        Text(text = uiModel.engineLabel)
                        Text(text = uiModel.stateLabel)
                        Text(text = uiModel.ramTierLabel)
                        Text(text = uiModel.deviceFitLabel)
                        Text(text = uiModel.sizeLabel)
                        Text(text = uiModel.fileProgressLabel)
                        Text(text = "Required files")
                        uiModel.requiredFileLabels.forEach { fileLabel ->
                            Text(text = fileLabel)
                        }
                        Text(text = "Loaded only when used")
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelActionSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        content()
    }
}
