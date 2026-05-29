package com.xtranslate.ui.models

import com.xtranslate.model.LocalModelPaths
import com.xtranslate.model.ModelInstallState
import com.xtranslate.model.ModelPack

/**
 * Text prepared for one model card on the Models screen.
 */
data class ModelPackUi(
    val displayName: String,
    val engineLabel: String,
    val stateLabel: String,
    val requiredFileLabels: List<String>,
)

object ModelPackUiFormatter {
    fun format(
        pack: ModelPack,
        state: ModelInstallState,
        modelPaths: LocalModelPaths,
    ): ModelPackUi =
        ModelPackUi(
            displayName = pack.displayName,
            engineLabel = "Engine: ${pack.engineType}",
            stateLabel = "State: $state",
            requiredFileLabels =
                modelPaths
                    .modelFiles(pack)
                    .map { file ->
                        val path = file.path.replace('\\', '/')
                        if (file.exists()) {
                            "$path - Installed - ${formatBytes(file.length())}"
                        } else {
                            "$path - Missing"
                        }
                    },
        )

    private fun formatBytes(bytes: Long): String = "$bytes B"
}
