package com.xtranslate.runtime

import com.xtranslate.model.LocalModelPaths
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class LocalSpeechTestRunnerTest {
    @Test
    fun synthesizeSampleSpeechReturnsPlaceholderAudioWhenSupertonicModelExists() =
        runTest {
            val root = createTempDirectory().toFile()
            val modelPaths = LocalModelPaths(root)
            modelPaths.supertonicModelFile().parentFile?.mkdirs()
            modelPaths.supertonicModelFile().writeText("model")
            val runner = LocalSpeechTestRunner(modelPaths)

            val result = runner.synthesizeSampleSpeech()

            assertEquals("memory://tts/English", result)
        }

    @Test
    fun synthesizeSampleSpeechThrowsWhenSupertonicModelIsMissing() =
        runTest {
            val root = createTempDirectory().toFile()
            val runner = LocalSpeechTestRunner(LocalModelPaths(root))

            try {
                runner.synthesizeSampleSpeech()
                fail("Expected missing Supertonic model error")
            } catch (error: IllegalStateException) {
                assertEquals(
                    "Missing Supertonic model file: ${File(root, "models/tts/supertonic-3.onnx").path.replace('\\', '/')}",
                    error.message,
                )
            }
        }
}
