package com.xtranslate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.xtranslate.llama.LocalTextGenerationRunner
import com.xtranslate.llama.nativebridge.OfficialNativeLlamaBridge
import com.xtranslate.model.FileBackedModelStore
import com.xtranslate.model.LocalModelPaths
import com.xtranslate.model.ModelRegistry
import com.xtranslate.runtime.EngineCoordinator
import com.xtranslate.runtime.FakeOcrEngine
import com.xtranslate.runtime.FakeSpeechToTextEngine
import com.xtranslate.runtime.FakeTextToSpeechEngine
import com.xtranslate.runtime.FakeTranslationEngine
import com.xtranslate.ui.XTranslateApp
import com.xtranslate.ui.chat.ChatViewModel
import com.xtranslate.ui.theme.XTranslateAppTheme
import kotlinx.coroutines.launch

/**
 * Android entry point that wires the first app shell to fake local engines.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val coordinator =
            EngineCoordinator(
                ocrEngine = FakeOcrEngine(),
                translationEngine = FakeTranslationEngine(),
                sttEngine = FakeSpeechToTextEngine(),
                ttsEngine = FakeTextToSpeechEngine(),
                lowMemoryMode = true,
            )
        val chatViewModel = ChatViewModel(coordinator)
        val modelStore =
            FileBackedModelStore(
                modelPacks = ModelRegistry.defaultPacks(),
                modelPaths = LocalModelPaths(filesDir),
            )

        setContent {
            XTranslateAppTheme {
                val scope = rememberCoroutineScope()
                var localTextTestStatus by remember { mutableStateOf<String?>(null) }
                val localTextGenerationRunner =
                    remember {
                        LocalTextGenerationRunner(
                            modelPaths = LocalModelPaths(filesDir),
                            bridge = OfficialNativeLlamaBridge(this@MainActivity),
                        )
                    }

                XTranslateApp(
                    chatViewModel = chatViewModel,
                    modelStore = modelStore,
                    onRunLocalTextTest = {
                        localTextTestStatus = "Running local text test..."
                        scope.launch {
                            localTextTestStatus =
                                runCatching { localTextGenerationRunner.translateSampleText() }
                                    .fold(
                                        onSuccess = { result -> "Result: $result" },
                                        onFailure = { error -> "Error: ${error.message}" },
                                    )
                        }
                    },
                    localTextTestStatus = localTextTestStatus,
                )
            }
        }
    }
}
