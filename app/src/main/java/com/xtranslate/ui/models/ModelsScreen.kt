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
import com.xtranslate.model.ModelStore

/**
 * Shows the local model packs the app will need.
 */
@Composable
fun ModelsScreen(
    modelStore: ModelStore,
    onRunLocalTextTest: () -> Unit,
    localTextTestStatus: String?,
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
        modelStore.packs().forEach { pack ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(text = pack.displayName, fontWeight = FontWeight.SemiBold)
                    Text(text = "Engine: ${pack.engineType}")
                    Text(text = "State: ${modelStore.state(pack.id)}")
                    Text(text = "Loaded only when used")
                }
            }
        }
    }
}
