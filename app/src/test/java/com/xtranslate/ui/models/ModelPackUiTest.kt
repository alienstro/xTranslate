package com.xtranslate.ui.models

import com.xtranslate.model.LocalModelPaths
import com.xtranslate.model.EngineType
import com.xtranslate.model.ModelInstallState
import com.xtranslate.model.ModelPack
import com.xtranslate.model.ModelRegistry
import com.xtranslate.model.RamTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.Locale

/**
 * Tests text shown for model packs on the Models screen.
 */
class ModelPackUiTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun formatterShowsStateEngineAndRequiredFiles() {
        val pack = ModelRegistry.defaultPacks().first { it.id == "translation.multilingual.gguf" }
        val paths = LocalModelPaths(File("/data/user/0/com.xtranslate/files"))

        val uiModel =
            ModelPackUiFormatter.format(
                pack = pack,
                state = ModelInstallState.Missing,
                modelPaths = paths,
            )

        assertEquals("Hy-MT2 1.8B Multilingual Translator", uiModel.displayName)
        assertEquals("Engine: LlamaTranslation", uiModel.engineLabel)
        assertEquals("State: Missing", uiModel.stateLabel)
        assertEquals("Minimum RAM: Low", uiModel.ramTierLabel)
        assertEquals("Size: 1156 MB", uiModel.sizeLabel)
        assertEquals("Device fit: Low-end friendly", uiModel.deviceFitLabel)
        assertEquals("Files: 0/1 installed", uiModel.fileProgressLabel)
        assertTrue(
            uiModel.requiredFileLabels.single().endsWith(
                "/models/translation/Hy-MT2-1.8B-Q4_K_M.gguf - Missing",
            ),
        )
    }

    @Test
    fun formatterShowsInstalledFileSizeWhenRequiredFileExists() {
        val pack = ModelRegistry.defaultPacks().first { it.id == "translation.multilingual.gguf" }
        val paths = LocalModelPaths(temporaryFolder.root)
        val file = paths.translationModelFile()
        file.parentFile?.mkdirs()
        file.writeText("12345")

        val uiModel =
            ModelPackUiFormatter.format(
                pack = pack,
                state = ModelInstallState.Installed,
                modelPaths = paths,
            )

        assertTrue(
            uiModel.requiredFileLabels.single().endsWith(
                "/models/translation/Hy-MT2-1.8B-Q4_K_M.gguf - Installed - 5 B",
            ),
        )
    }

    @Test
    fun formatterShowsInstalledFileSizeInKilobytes() {
        val pack = ModelRegistry.defaultPacks().first { it.id == "translation.multilingual.gguf" }
        val paths = LocalModelPaths(temporaryFolder.root)
        val file = paths.translationModelFile()
        file.parentFile?.mkdirs()
        file.writeBytes(ByteArray(1536))

        val uiModel =
            ModelPackUiFormatter.format(
                pack = pack,
                state = ModelInstallState.Installed,
                modelPaths = paths,
            )

        assertTrue(
            uiModel.requiredFileLabels.single().endsWith(
                "/models/translation/Hy-MT2-1.8B-Q4_K_M.gguf - Installed - 1.5 KB",
            ),
        )
    }

    @Test
    fun formatterUsesDotDecimalSeparatorForFileSizes() {
        val previousLocale = Locale.getDefault()
        Locale.setDefault(Locale.GERMANY)
        try {
            val pack = ModelRegistry.defaultPacks().first { it.id == "translation.multilingual.gguf" }
            val paths = LocalModelPaths(temporaryFolder.root)
            val file = paths.translationModelFile()
            file.parentFile?.mkdirs()
            file.writeBytes(ByteArray(1536))

            val uiModel =
                ModelPackUiFormatter.format(
                    pack = pack,
                    state = ModelInstallState.Installed,
                    modelPaths = paths,
                )

            assertTrue(
                uiModel.requiredFileLabels.single().endsWith(
                    "/models/translation/Hy-MT2-1.8B-Q4_K_M.gguf - Installed - 1.5 KB",
                ),
            )
        } finally {
            Locale.setDefault(previousLocale)
        }
    }

    @Test
    fun formatterShowsInstalledFileSizeInMegabytes() {
        val pack = ModelRegistry.defaultPacks().first { it.id == "translation.multilingual.gguf" }
        val paths = LocalModelPaths(temporaryFolder.root)
        val file = paths.translationModelFile()
        file.parentFile?.mkdirs()
        file.writeBytes(ByteArray(2 * 1024 * 1024))

        val uiModel =
            ModelPackUiFormatter.format(
                pack = pack,
                state = ModelInstallState.Installed,
                modelPaths = paths,
            )

        assertTrue(
            uiModel.requiredFileLabels.single().endsWith(
                "/models/translation/Hy-MT2-1.8B-Q4_K_M.gguf - Installed - 2.0 MB",
            ),
        )
    }

    @Test
    fun formatterShowsPartialRequiredFileProgress() {
        val pack = ModelRegistry.defaultPacks().first { it.id == "ocr.paddleocr-vl-1_5.q4" }
        val paths = LocalModelPaths(temporaryFolder.root)
        val firstFile = paths.modelFiles(pack).first()
        firstFile.parentFile?.mkdirs()
        firstFile.writeText("model")

        val uiModel =
            ModelPackUiFormatter.format(
                pack = pack,
                state = ModelInstallState.Missing,
                modelPaths = paths,
            )

        assertEquals("Files: 1/2 installed", uiModel.fileProgressLabel)
    }

    @Test
    fun formatterShowsExpectedSizeWhenKnown() {
        val pack =
            ModelPack(
                id = "test.pack",
                displayName = "Test Pack",
                engineType = EngineType.OnnxTts,
                files = emptyList(),
                expectedSizeMb = 128,
                minimumRamTier = RamTier.Medium,
            )
        val paths = LocalModelPaths(File("/data/user/0/com.xtranslate/files"))

        val uiModel =
            ModelPackUiFormatter.format(
                pack = pack,
                state = ModelInstallState.Missing,
                modelPaths = paths,
            )

        assertEquals("Minimum RAM: Medium", uiModel.ramTierLabel)
        assertEquals("Size: 128 MB", uiModel.sizeLabel)
        assertEquals("Device fit: Medium RAM or better", uiModel.deviceFitLabel)
    }
}
