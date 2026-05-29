package com.xtranslate.ui.models

import com.xtranslate.model.LocalModelPaths
import com.xtranslate.model.ModelInstallState
import com.xtranslate.model.ModelRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

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

        assertEquals("Broad Multilingual Translator", uiModel.displayName)
        assertEquals("Engine: LlamaTranslation", uiModel.engineLabel)
        assertEquals("State: Missing", uiModel.stateLabel)
        assertTrue(
            uiModel.requiredFileLabels.single().endsWith(
                "/models/translation/multilingual-translator.gguf - Missing",
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
                "/models/translation/multilingual-translator.gguf - Installed - 5 B",
            ),
        )
    }
}
