package com.xtranslate.ui.models

import com.xtranslate.model.LocalModelPaths
import com.xtranslate.model.ModelInstallState
import com.xtranslate.model.ModelPack
import com.xtranslate.model.RamTier
import java.util.Locale

/**
 * Text prepared for one model card on the Models screen.
 */
data class ModelPackUi(
    val displayName: String,
    val engineLabel: String,
    val stateLabel: String,
    val ramTierLabel: String,
    val sizeLabel: String,
    val deviceFitLabel: String,
    val fileProgressLabel: String,
    val requiredFileLabels: List<String>,
)

object ModelPackUiFormatter {
    fun format(
        pack: ModelPack,
        state: ModelInstallState,
        modelPaths: LocalModelPaths,
    ): ModelPackUi {
        val requiredFiles = modelPaths.modelFiles(pack)
        val installedRequiredFileCount = requiredFiles.count { file -> file.exists() }

        return ModelPackUi(
            displayName = pack.displayName,
            engineLabel = "Engine: ${pack.engineType}",
            stateLabel = "State: $state",
            ramTierLabel = "Minimum RAM: ${pack.minimumRamTier}",
            sizeLabel = pack.expectedSizeMb?.let { size -> "Size: $size MB" } ?: "Size: Unknown",
            deviceFitLabel = deviceFitLabel(pack.minimumRamTier),
            fileProgressLabel = "Files: $installedRequiredFileCount/${requiredFiles.size} installed",
            requiredFileLabels =
                requiredFiles.map { file ->
                    val path = file.path.replace('\\', '/')
                    if (file.exists()) {
                        "$path - Installed - ${formatBytes(file.length())}"
                    } else {
                        "$path - Missing"
                    }
                },
        )
    }

    private fun deviceFitLabel(ramTier: RamTier): String =
        when (ramTier) {
            RamTier.Low -> "Device fit: Low-end friendly"
            RamTier.Medium -> "Device fit: Medium RAM or better"
            RamTier.High -> "Device fit: High RAM only"
        }

    private fun formatBytes(bytes: Long): String =
        when {
            bytes >= BYTES_PER_MB -> "${formatOneDecimal(bytes.toDouble() / BYTES_PER_MB)} MB"
            bytes >= BYTES_PER_KB -> "${formatOneDecimal(bytes.toDouble() / BYTES_PER_KB)} KB"
            else -> "$bytes B"
        }

    private fun formatOneDecimal(value: Double): String = String.format(Locale.US, "%.1f", value)

    private const val BYTES_PER_KB = 1024
    private const val BYTES_PER_MB = 1024 * 1024
}
