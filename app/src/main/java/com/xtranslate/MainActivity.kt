package com.xtranslate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.xtranslate.model.InMemoryModelStore
import com.xtranslate.model.ModelRegistry
import com.xtranslate.runtime.EngineCoordinator
import com.xtranslate.runtime.FakeOcrEngine
import com.xtranslate.runtime.FakeSpeechToTextEngine
import com.xtranslate.runtime.FakeTextToSpeechEngine
import com.xtranslate.runtime.FakeTranslationEngine
import com.xtranslate.ui.XTranslateApp
import com.xtranslate.ui.chat.ChatViewModel
import com.xtranslate.ui.theme.XTranslateAppTheme

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
        val modelStore = InMemoryModelStore(ModelRegistry.defaultPacks())

        setContent {
            XTranslateAppTheme {
                XTranslateApp(
                    chatViewModel = chatViewModel,
                    modelStore = modelStore,
                )
            }
        }
    }
}
