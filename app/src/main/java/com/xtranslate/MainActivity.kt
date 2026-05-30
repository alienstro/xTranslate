package com.xtranslate

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
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
import androidx.core.content.ContextCompat
import com.xtranslate.domain.AudioInput
import com.xtranslate.domain.ImageInput
import com.xtranslate.llama.AndroidLlamaRuntime
import com.xtranslate.llama.LocalOcrRunner
import com.xtranslate.llama.LocalTextGenerationRunner
import com.xtranslate.llama.LlamaOcrEngine
import com.xtranslate.llama.LlamaProfileFactory
import com.xtranslate.llama.LlamaTranslationEngine
import com.xtranslate.llama.nativebridge.OfficialNativeLlamaBridge
import com.xtranslate.model.FileBackedModelStore
import com.xtranslate.model.EngineType
import com.xtranslate.model.LocalModelImporter
import com.xtranslate.model.LocalModelPaths
import com.xtranslate.model.ModelDownloader
import com.xtranslate.model.ModelDownloadProgress
import com.xtranslate.model.ModelRegistry
import com.xtranslate.runtime.EngineCoordinator
import com.xtranslate.runtime.FileBackedSpeechToTextEngine
import com.xtranslate.runtime.FileBackedTextToSpeechEngine
import com.xtranslate.runtime.FakeTextToSpeechEngine
import com.xtranslate.runtime.LocalAudioRecorder
import com.xtranslate.runtime.LocalSpeechTestRunner
import com.xtranslate.runtime.WhisperCppSpeechToTextEngine
import com.xtranslate.ui.XTranslateApp
import com.xtranslate.ui.chat.ChatViewModel
import com.xtranslate.ui.theme.XTranslateAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/**
 * Android entry point that wires the first app shell to fake local engines.
 */
class MainActivity : ComponentActivity() {
    private var textToSpeech: TextToSpeech? = null
    private var isTextToSpeechReady: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        textToSpeech =
            TextToSpeech(this) { status ->
                isTextToSpeechReady = status == TextToSpeech.SUCCESS
            }

        val modelPaths = LocalModelPaths(filesDir)
        val ocrPack = ModelRegistry.defaultPacks().first { pack -> pack.id == "ocr.paddleocr-vl-1_5.q4" }
        val ocrFiles = modelPaths.modelFiles(ocrPack)
        val coordinator =
            EngineCoordinator(
                ocrEngine =
                    LlamaOcrEngine(
                        runtime = AndroidLlamaRuntime(OfficialNativeLlamaBridge(this)),
                        profile = LlamaProfileFactory.ocrProfile(ocrFiles.first(), ocrFiles.drop(1).first()),
                    ),
                translationEngine =
                    LlamaTranslationEngine(
                        runtime = AndroidLlamaRuntime(OfficialNativeLlamaBridge(this)),
                        profile = LlamaProfileFactory.translationProfile(modelPaths.translationModelFile()),
                    ),
                sttEngine =
                    FileBackedSpeechToTextEngine(
                        modelFile = modelPaths.whisperModelFile(),
                        delegate = WhisperCppSpeechToTextEngine(modelPaths.whisperModelFile()),
                    ),
                ttsEngine =
                    FileBackedTextToSpeechEngine(
                        modelFile = modelPaths.supertonicModelFile(),
                        delegate = FakeTextToSpeechEngine(),
                    ),
                lowMemoryMode = true,
            )
        val chatViewModel = ChatViewModel(coordinator)
        val modelStore =
            FileBackedModelStore(
                modelPacks = ModelRegistry.defaultPacks(),
                modelPaths = modelPaths,
            )

        setContent {
            XTranslateAppTheme {
                val scope = rememberCoroutineScope()
                var localTextTestStatus by remember { mutableStateOf<String?>(null) }
                var localOcrTestStatus by remember { mutableStateOf<String?>(null) }
                var speechTestStatus by remember { mutableStateOf<String?>(null) }
                var importStatus by remember { mutableStateOf<String?>(null) }
                var ocrImportStatus by remember { mutableStateOf<String?>(null) }
                var speechImportStatus by remember { mutableStateOf<String?>(null) }
                var modelDownloadStatus by remember { mutableStateOf<String?>(null) }
                var modelDownloadProgress by remember { mutableStateOf<ModelDownloadProgress?>(null) }
                var modelStateRefreshKey by remember { mutableStateOf(0) }
                var isRecordingVoice by remember { mutableStateOf(false) }
                var voiceRecordingStartedAt by remember { mutableStateOf(0L) }
                val localModelImporter = remember { LocalModelImporter(modelPaths) }
                val modelDownloader = remember { ModelDownloader(modelPaths) }
                val audioRecorder = remember { LocalAudioRecorder() }
                val localTextGenerationRunner =
                    remember {
                        LocalTextGenerationRunner(
                            modelPaths = modelPaths,
                            bridge = OfficialNativeLlamaBridge(this@MainActivity),
                        )
                    }
                val localOcrRunner =
                    remember {
                        LocalOcrRunner(
                            modelPaths = modelPaths,
                            bridge = OfficialNativeLlamaBridge(this@MainActivity),
                        )
                    }
                val localSpeechTestRunner =
                    remember {
                        LocalSpeechTestRunner(modelPaths)
                    }
                fun startVoiceRecording() {
                    val outputFile = File(cacheDir, "voice-input/latest.wav")
                    voiceRecordingStartedAt = System.currentTimeMillis()
                    isRecordingVoice = true
                    Toast.makeText(this@MainActivity, "Recording voice input...", Toast.LENGTH_SHORT).show()
                    scope.launch {
                        audioRecorder.startRecording(outputFile) { error ->
                            runOnUiThread {
                                isRecordingVoice = false
                                chatViewModel.updateComposer("Voice recording failed: ${error.message}")
                            }
                        }
                    }
                }

                fun stopVoiceRecording() {
                    scope.launch {
                        val audioFile = audioRecorder.stopRecording()
                        isRecordingVoice = false
                        if (audioFile == null) {
                            chatViewModel.updateComposer("No voice recording was active.")
                            return@launch
                        }

                        val durationMillis = System.currentTimeMillis() - voiceRecordingStartedAt
                        chatViewModel.transcribeAudio(
                            AudioInput(
                                uri = audioFile.absolutePath,
                                durationMillis = durationMillis,
                            ),
                        )
                    }
                }

                val recordAudioPermissionLauncher =
                    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                        if (granted) {
                            startVoiceRecording()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Microphone permission is required for local Whisper.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                val localOcrImagePicker =
                    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                        if (uri == null) {
                            localOcrTestStatus = "OCR image selection cancelled"
                            return@rememberLauncherForActivityResult
                        }

                        localOcrTestStatus = "Running local OCR test..."
                        scope.launch {
                            localOcrTestStatus =
                                runCatching {
                                    localOcrRunner.extractSampleImageText(uri.toString())
                                }.fold(
                                    onSuccess = { result -> "OCR result: $result" },
                                    onFailure = { error -> "OCR error: ${error.message}" },
                                )
                        }
                    }
                val chatImagePicker =
                    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                        if (uri == null) {
                            chatViewModel.updateComposer("Image selection cancelled.")
                            return@rememberLauncherForActivityResult
                        }

                        scope.launch {
                            chatViewModel.translateImage(ImageInput(uri = uri.toString()))
                        }
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
                                        chatViewModel.clearMissingWhisperModelMessages()
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
                    onPickImage = {
                        chatImagePicker.launch("image/*")
                    },
                    onMic = {
                        if (isRecordingVoice) {
                            stopVoiceRecording()
                        } else {
                            val permissionState =
                                ContextCompat.checkSelfPermission(
                                    this@MainActivity,
                                    Manifest.permission.RECORD_AUDIO,
                                )
                            if (permissionState == PackageManager.PERMISSION_GRANTED) {
                                startVoiceRecording()
                            } else {
                                recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    onSpeakTranslation = { message ->
                        speakText(
                            text = message.text,
                            language = message.language ?: chatViewModel.state.value.targetLanguage,
                        )
                    },
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
                    onRunLocalOcrTest = {
                        localOcrImagePicker.launch("image/*")
                    },
                    onRunLocalSttTest = {
                        speechTestStatus = "Running local STT test..."
                        scope.launch {
                            speechTestStatus =
                                runCatching { localSpeechTestRunner.transcribeSampleAudio() }
                                    .fold(
                                        onSuccess = { result -> "STT result: $result" },
                                        onFailure = { error -> "STT error: ${error.message}" },
                                    )
                        }
                    },
                    onRunLocalTtsTest = {
                        speechTestStatus = "Running local TTS test..."
                        scope.launch {
                            speechTestStatus =
                                runCatching { localSpeechTestRunner.synthesizeSampleSpeech() }
                                    .fold(
                                        onSuccess = { result -> "TTS result: $result" },
                                        onFailure = { error -> "TTS error: ${error.message}" },
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
                    onDeleteModelPack = { pack ->
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                localModelImporter.deleteModelFiles(pack)
                            }
                            modelStore.markDeleted(pack.id)
                            modelStateRefreshKey += 1
                        }
                    },
                    onDownloadModelPack = { pack ->
                        modelDownloadStatus = "Downloading ${pack.displayName}..."
                        modelDownloadProgress = null
                        modelStore.markDownloading(pack.id)
                        modelStateRefreshKey += 1
                        scope.launch {
                            modelDownloadStatus =
                                runCatching {
                                    modelDownloader.downloadPack(pack) { progress ->
                                        runOnUiThread {
                                            modelDownloadProgress = progress
                                            modelDownloadStatus =
                                                "Downloading ${pack.displayName}: ${progress.fileName}"
                                        }
                                    }
                                }.fold(
                                    onSuccess = { files ->
                                        if (pack.engineType == EngineType.WhisperStt) {
                                            chatViewModel.clearMissingWhisperModelMessages()
                                        }
                                        modelStore.markInstalled(pack.id)
                                        modelStateRefreshKey += 1
                                        modelDownloadProgress = null
                                        "Downloaded ${pack.displayName}: ${files.size} file(s)"
                                    },
                                    onFailure = { error ->
                                        modelStore.markFailed(pack.id)
                                        modelStateRefreshKey += 1
                                        modelDownloadProgress = null
                                        "Download failed: ${error.message}"
                                    },
                                )
                        }
                    },
                    localTextTestStatus = localTextTestStatus,
                    localOcrTestStatus = localOcrTestStatus,
                    speechTestStatus = speechTestStatus,
                    importStatus = importStatus,
                    ocrImportStatus = ocrImportStatus,
                    speechImportStatus = speechImportStatus,
                    modelDownloadStatus = modelDownloadStatus,
                    modelDownloadProgress = modelDownloadProgress,
                    modelStateRefreshKey = modelStateRefreshKey,
                )
            }
        }
    }

    override fun onDestroy() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        super.onDestroy()
    }

    private fun speakText(
        text: String,
        language: String,
    ) {
        if (!isTextToSpeechReady) {
            Toast.makeText(this, "Speech is still starting.", Toast.LENGTH_SHORT).show()
            return
        }

        val locale = localeForLanguage(language)
        val result = textToSpeech?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(this, "$language speech is not supported on this device.", Toast.LENGTH_SHORT).show()
            return
        }

        textToSpeech?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "translation-${System.currentTimeMillis()}",
        )
    }

    private fun localeForLanguage(language: String): Locale =
        when (language.lowercase(Locale.US)) {
            "filipino", "tagalog" -> Locale.Builder().setLanguage("fil").setRegion("PH").build()
            "japanese" -> Locale.JAPANESE
            "korean" -> Locale.KOREAN
            "chinese" -> Locale.CHINESE
            "spanish" -> Locale.Builder().setLanguage("es").setRegion("ES").build()
            "french" -> Locale.FRENCH
            "german" -> Locale.GERMAN
            else -> Locale.ENGLISH
        }
}
