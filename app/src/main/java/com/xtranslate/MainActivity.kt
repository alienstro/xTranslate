package com.xtranslate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.xtranslate.llama.LocalTextGenerationRunner
import com.xtranslate.llama.nativebridge.OfficialNativeLlamaBridge
import com.xtranslate.model.FileBackedModelStore
import com.xtranslate.model.LocalModelImporter
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        val modelPaths = LocalModelPaths(filesDir)
        val modelStore =
            FileBackedModelStore(
                modelPacks = ModelRegistry.defaultPacks(),
                modelPaths = modelPaths,
            )

        setContent {
            XTranslateAppTheme {
                val scope = rememberCoroutineScope()
                var localTextTestStatus by remember { mutableStateOf<String?>(null) }
                var importStatus by remember { mutableStateOf<String?>(null) }
                var ocrImportStatus by remember { mutableStateOf<String?>(null) }
                var speechImportStatus by remember { mutableStateOf<String?>(null) }
                var modelStateRefreshKey by remember { mutableStateOf(0) }
                val localModelImporter = remember { LocalModelImporter(modelPaths) }
                val localTextGenerationRunner =
                    remember {
                        LocalTextGenerationRunner(
                            modelPaths = modelPaths,
                            bridge = OfficialNativeLlamaBridge(this@MainActivity),
                        )
                    }
                val translationModelPicker =
                    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                        if (uri == null) {
                            importStatus = "Import cancelled"
                            return@rememberLauncherForActivityResult
                        }

                        importStatus = "Importing translation GGUF..."
                        scope.launch {
                            importStatus =
                                runCatching {
                                    withContext(Dispatchers.IO) {
                                        val inputStream =
                                            requireNotNull(contentResolver.openInputStream(uri)) {
                                                "Could not open selected file"
                                            }
                                        localModelImporter.importTranslationModel(inputStream)
                                    }
                                }.fold(
                                    onSuccess = { file ->
                                        modelStateRefreshKey += 1
                                        "Imported: ${file.name}"
                                    },
                                    onFailure = { error -> "Import failed: ${error.message}" },
                                )
                        }
                    }
                val ocrModelPicker =
                    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                        if (uri == null) {
                            ocrImportStatus = "OCR import cancelled"
                            return@rememberLauncherForActivityResult
                        }

                        ocrImportStatus = "Importing OCR GGUF..."
                        scope.launch {
                            ocrImportStatus =
                                runCatching {
                                    withContext(Dispatchers.IO) {
                                        val inputStream =
                                            requireNotNull(contentResolver.openInputStream(uri)) {
                                                "Could not open selected file"
                                            }
                                        localModelImporter.importOcrModel(inputStream)
                                    }
                                }.fold(
                                    onSuccess = { file ->
                                        modelStateRefreshKey += 1
                                        "Imported OCR model: ${file.name}"
                                    },
                                    onFailure = { error -> "OCR import failed: ${error.message}" },
                                )
                        }
                    }
                val ocrProjectorPicker =
                    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                        if (uri == null) {
                            ocrImportStatus = "OCR import cancelled"
                            return@rememberLauncherForActivityResult
                        }

                        ocrImportStatus = "Importing OCR projector..."
                        scope.launch {
                            ocrImportStatus =
                                runCatching {
                                    withContext(Dispatchers.IO) {
                                        val inputStream =
                                            requireNotNull(contentResolver.openInputStream(uri)) {
                                                "Could not open selected file"
                                            }
                                        localModelImporter.importOcrProjector(inputStream)
                                    }
                                }.fold(
                                    onSuccess = { file ->
                                        modelStateRefreshKey += 1
                                        "Imported OCR projector: ${file.name}"
                                    },
                                    onFailure = { error -> "OCR import failed: ${error.message}" },
                                )
                        }
                    }
                val whisperModelPicker =
                    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                        if (uri == null) {
                            speechImportStatus = "Speech import cancelled"
                            return@rememberLauncherForActivityResult
                        }

                        speechImportStatus = "Importing Whisper STT..."
                        scope.launch {
                            speechImportStatus =
                                runCatching {
                                    withContext(Dispatchers.IO) {
                                        val inputStream =
                                            requireNotNull(contentResolver.openInputStream(uri)) {
                                                "Could not open selected file"
                                            }
                                        localModelImporter.importWhisperModel(inputStream)
                                    }
                                }.fold(
                                    onSuccess = { file ->
                                        modelStateRefreshKey += 1
                                        "Imported Whisper STT: ${file.name}"
                                    },
                                    onFailure = { error -> "Speech import failed: ${error.message}" },
                                )
                        }
                    }
                val supertonicModelPicker =
                    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                        if (uri == null) {
                            speechImportStatus = "Speech import cancelled"
                            return@rememberLauncherForActivityResult
                        }

                        speechImportStatus = "Importing Supertonic TTS..."
                        scope.launch {
                            speechImportStatus =
                                runCatching {
                                    withContext(Dispatchers.IO) {
                                        val inputStream =
                                            requireNotNull(contentResolver.openInputStream(uri)) {
                                                "Could not open selected file"
                                            }
                                        localModelImporter.importSupertonicModel(inputStream)
                                    }
                                }.fold(
                                    onSuccess = { file ->
                                        modelStateRefreshKey += 1
                                        "Imported Supertonic TTS: ${file.name}"
                                    },
                                    onFailure = { error -> "Speech import failed: ${error.message}" },
                                )
                        }
                    }

                XTranslateApp(
                    chatViewModel = chatViewModel,
                    modelStore = modelStore,
                    modelPaths = modelPaths,
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
                    onImportTranslationModel = {
                        translationModelPicker.launch("*/*")
                    },
                    onImportOcrModel = {
                        ocrModelPicker.launch("*/*")
                    },
                    onImportOcrProjector = {
                        ocrProjectorPicker.launch("*/*")
                    },
                    onImportWhisperModel = {
                        whisperModelPicker.launch("*/*")
                    },
                    onImportSupertonicModel = {
                        supertonicModelPicker.launch("*/*")
                    },
                    localTextTestStatus = localTextTestStatus,
                    importStatus = importStatus,
                    ocrImportStatus = ocrImportStatus,
                    speechImportStatus = speechImportStatus,
                    modelStateRefreshKey = modelStateRefreshKey,
                )
            }
        }
    }
}
