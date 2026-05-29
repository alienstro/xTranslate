package com.xtranslate.ui.models

import com.xtranslate.model.LocalModelPaths
import com.xtranslate.model.ModelInstallState
import com.xtranslate.model.ModelRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Tests text shown for model packs on the Models screen.
 */
class ModelPackUiTest {
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
                "/models/translation/multilingual-translator.gguf",
            ),
        )
    }
}
