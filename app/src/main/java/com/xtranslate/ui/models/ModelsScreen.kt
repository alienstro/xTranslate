package com.xtranslate.ui.models

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    onImportTranslationModel: () -> Unit,
    localTextTestStatus: String?,
    importStatus: String?,
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
        Button(onClick = onRunLocalTextTest) {
            Text("Run local text test")
        }
        localTextTestStatus?.let { status ->
            Text(text = status)
        }
        Button(onClick = onImportTranslationModel) {
            Text("Import translation GGUF")
        }
        importStatus?.let { status ->
            Text(text = status)
        }
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
