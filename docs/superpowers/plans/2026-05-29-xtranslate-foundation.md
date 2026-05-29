# xTranslate Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first runnable Android Kotlin foundation for xTranslate: Claude-like mobile chat UI, model registry, runtime interfaces, lazy-loading coordinator, and mocked text/image/voice translation flows.

**Architecture:** This slice creates a standard single-module Android Compose app with focused domain/runtime/model-manager packages. Real llama.cpp, Whisper, and ONNX integrations are represented by interfaces and fake engines so UI, lifecycle, and tests can stabilize before native runtime work starts.

**Tech Stack:** Kotlin, Android Gradle Plugin, Jetpack Compose Material 3, kotlinx.coroutines, kotlinx.serialization, JUnit 4, AndroidX test.

---

## Scope Check

The approved design covers several independent subsystems: Android UI, llama.cpp OCR, llama.cpp translation, Whisper STT, ONNX TTS, model downloads, and model lifecycle. Implementing all native engines in one pass would make verification brittle. This plan implements the foundation slice only:

- Android project scaffold.
- Domain contracts for OCR, translation, STT, and TTS.
- Model registry and install state.
- Engine coordinator lazy-loading policy.
- Mobile-first Claude-like chat UI.
- Mocked text, image, and voice flows.
- Unit tests for lifecycle, model state, and prompt construction.

Follow-up plans should cover native llama.cpp integration, model downloads/checksums, Whisper integration, Supertonic/ONNX integration, and device benchmarking.

## File Structure

Create these files:

- `settings.gradle.kts` - Gradle project settings.
- `build.gradle.kts` - root Gradle plugin declarations.
- `gradle.properties` - AndroidX, Kotlin, and JVM settings.
- `app/build.gradle.kts` - Android app module configuration.
- `app/src/main/AndroidManifest.xml` - app manifest and permissions.
- `app/src/main/java/com/xtranslate/MainActivity.kt` - Compose activity entrypoint.
- `app/src/main/java/com/xtranslate/domain/Types.kt` - shared value types.
- `app/src/main/java/com/xtranslate/domain/Engines.kt` - engine interfaces.
- `app/src/main/java/com/xtranslate/domain/Prompts.kt` - OCR and translation prompt construction.
- `app/src/main/java/com/xtranslate/model/ModelRegistry.kt` - model pack definitions.
- `app/src/main/java/com/xtranslate/model/ModelStore.kt` - installed/download state abstraction and in-memory implementation.
- `app/src/main/java/com/xtranslate/runtime/EngineCoordinator.kt` - lazy-loading lifecycle policy.
- `app/src/main/java/com/xtranslate/runtime/FakeEngines.kt` - fake OCR, translation, STT, and TTS engines.
- `app/src/main/java/com/xtranslate/ui/theme/Theme.kt` - Claude-like warm neutral theme.
- `app/src/main/java/com/xtranslate/ui/XTranslateApp.kt` - top-level navigation state.
- `app/src/main/java/com/xtranslate/ui/chat/ChatModels.kt` - chat UI state and actions.
- `app/src/main/java/com/xtranslate/ui/chat/ChatViewModel.kt` - workflow orchestration for text/image/voice flows.
- `app/src/main/java/com/xtranslate/ui/chat/ChatScreen.kt` - mobile chat UI.
- `app/src/main/java/com/xtranslate/ui/models/ModelsScreen.kt` - model pack status UI.
- `app/src/test/java/com/xtranslate/domain/PromptsTest.kt` - prompt unit tests.
- `app/src/test/java/com/xtranslate/model/ModelRegistryTest.kt` - model registry tests.
- `app/src/test/java/com/xtranslate/runtime/EngineCoordinatorTest.kt` - lazy-loading lifecycle tests.
- `app/src/test/java/com/xtranslate/ui/chat/ChatViewModelTest.kt` - mocked workflow tests.

Do not modify `.planning/PROJECT.md` in this plan. It is currently deleted in the working tree from earlier user/project state and should remain untouched unless the user explicitly asks.

---

### Task 1: Create Android Project Scaffold

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`

- [x] **Step 1: Create Gradle settings**

Create `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "xTranslate"
include(":app")
```

- [x] **Step 2: Create root Gradle build file**

Create `build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false
}
```

- [x] **Step 3: Create Gradle properties**

Create `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.nonTransitiveRClass=true
kotlin.code.style=official
```

- [x] **Step 4: Create app module build file**

Create `app/build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.xtranslate"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.xtranslate"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.ui:ui:1.7.6")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    debugImplementation("androidx.compose.ui:ui-tooling:1.7.6")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.6")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.6")
}
```

- [x] **Step 5: Create Android manifest**

Create `app/src/main/AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="false"
        android:label="xTranslate"
        android:supportsRtl="true"
        android:theme="@style/Theme.XTranslate">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [x] **Step 6: Add temporary theme resource**

Create `app/src/main/res/values/styles.xml`:

```xml
<resources>
    <style name="Theme.XTranslate" parent="android:style/Theme.Material.Light.NoActionBar" />
</resources>
```

- [x] **Step 7: Run scaffold verification**

Run:

```bash
./gradlew :app:tasks
```

Expected: Gradle lists tasks for `:app`. If `gradlew` does not exist, generate it with the installed Gradle distribution:

```bash
gradle wrapper
./gradlew :app:tasks
```

- [x] **Step 8: Commit scaffold**

```bash
git add settings.gradle.kts build.gradle.kts gradle.properties app/build.gradle.kts app/src/main/AndroidManifest.xml app/src/main/res/values/styles.xml
git commit -m "chore: scaffold android app"
```

---

### Task 2: Add Domain Types, Engine Interfaces, And Prompts

**Files:**
- Create: `app/src/main/java/com/xtranslate/domain/Types.kt`
- Create: `app/src/main/java/com/xtranslate/domain/Engines.kt`
- Create: `app/src/main/java/com/xtranslate/domain/Prompts.kt`
- Test: `app/src/test/java/com/xtranslate/domain/PromptsTest.kt`

- [x] **Step 1: Write failing prompt tests**

Create `app/src/test/java/com/xtranslate/domain/PromptsTest.kt`:

```kotlin
package com.xtranslate.domain

import org.junit.Assert.assertTrue
import org.junit.Test

class PromptsTest {
    @Test
    fun ocrPromptRequestsPlainExtractedText() {
        val prompt = Prompts.ocrExtractionPrompt()

        assertTrue(prompt.contains("extract all visible text", ignoreCase = true))
        assertTrue(prompt.contains("do not translate", ignoreCase = true))
    }

    @Test
    fun translationPromptIncludesTargetLanguageAndSourceText() {
        val prompt = Prompts.translationPrompt(
            request = TranslationRequest(
                sourceText = "Hello",
                sourceLanguage = "English",
                targetLanguage = "Filipino"
            )
        )

        assertTrue(prompt.contains("Filipino"))
        assertTrue(prompt.contains("English"))
        assertTrue(prompt.contains("Hello"))
        assertTrue(prompt.contains("only the translation", ignoreCase = true))
    }
}
```

- [x] **Step 2: Run tests and verify failure**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests com.xtranslate.domain.PromptsTest
```

Expected: FAIL because `Prompts`, `TranslationRequest`, and related domain types do not exist.

- [x] **Step 3: Add shared domain types**

Create `app/src/main/java/com/xtranslate/domain/Types.kt`:

```kotlin
package com.xtranslate.domain

data class ImageInput(
    val uri: String,
    val description: String? = null,
)

data class AudioInput(
    val uri: String,
    val durationMillis: Long,
)

data class OcrResult(
    val text: String,
    val blocks: List<String> = emptyList(),
)

data class TranslationRequest(
    val sourceText: String,
    val sourceLanguage: String? = null,
    val targetLanguage: String,
)

data class TranslationResult(
    val translatedText: String,
    val sourceText: String,
    val targetLanguage: String,
)

data class Transcript(
    val text: String,
    val confidence: Float? = null,
)

data class SpeechRequest(
    val text: String,
    val language: String,
)

data class AudioOutput(
    val uri: String,
    val durationMillis: Long? = null,
)
```

- [x] **Step 4: Add engine interfaces**

Create `app/src/main/java/com/xtranslate/domain/Engines.kt`:

```kotlin
package com.xtranslate.domain

interface OcrEngine {
    suspend fun extractText(image: ImageInput): OcrResult
}

interface TranslationEngine {
    suspend fun translate(request: TranslationRequest): TranslationResult
}

interface SpeechToTextEngine {
    suspend fun transcribe(audio: AudioInput): Transcript
}

interface TextToSpeechEngine {
    suspend fun synthesize(request: SpeechRequest): AudioOutput
}
```

- [x] **Step 5: Add prompt construction**

Create `app/src/main/java/com/xtranslate/domain/Prompts.kt`:

```kotlin
package com.xtranslate.domain

object Prompts {
    fun ocrExtractionPrompt(): String = """
        You are an OCR document parser.
        Extract all visible text from the image.
        Preserve reading order as much as possible.
        Return plain text with simple line breaks.
        Do not translate, summarize, or explain the text.
    """.trimIndent()

    fun translationPrompt(request: TranslationRequest): String {
        val source = request.sourceLanguage ?: "auto-detected source language"
        return """
            Translate from $source to ${request.targetLanguage}.
            Return only the translation.
            Preserve names, numbers, punctuation, and line breaks where useful.

            Source text:
            ${request.sourceText}
        """.trimIndent()
    }
}
```

- [x] **Step 6: Run prompt tests and verify pass**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests com.xtranslate.domain.PromptsTest
```

Expected: PASS.

- [x] **Step 7: Commit domain contracts**

```bash
git add app/src/main/java/com/xtranslate/domain app/src/test/java/com/xtranslate/domain/PromptsTest.kt
git commit -m "feat: add translation domain contracts"
```

---

### Task 3: Add Model Registry And Install State

**Files:**
- Create: `app/src/main/java/com/xtranslate/model/ModelRegistry.kt`
- Create: `app/src/main/java/com/xtranslate/model/ModelStore.kt`
- Test: `app/src/test/java/com/xtranslate/model/ModelRegistryTest.kt`

- [x] **Step 1: Write failing model registry tests**

Create `app/src/test/java/com/xtranslate/model/ModelRegistryTest.kt`:

```kotlin
package com.xtranslate.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelRegistryTest {
    @Test
    fun defaultRegistryContainsFourModelPacks() {
        val packs = ModelRegistry.defaultPacks()

        assertEquals(4, packs.size)
        assertTrue(packs.any { it.id == "ocr.paddleocr-vl-1_5.q4" })
        assertTrue(packs.any { it.id == "translation.multilingual.gguf" })
        assertTrue(packs.any { it.id == "stt.whisper" })
        assertTrue(packs.any { it.id == "tts.supertonic-3" })
    }

    @Test
    fun inMemoryStoreTracksInstallState() {
        val store = InMemoryModelStore(ModelRegistry.defaultPacks())

        assertEquals(ModelInstallState.Missing, store.state("translation.multilingual.gguf"))

        store.markDownloading("translation.multilingual.gguf")
        assertEquals(ModelInstallState.Downloading, store.state("translation.multilingual.gguf"))

        store.markInstalled("translation.multilingual.gguf")
        assertEquals(ModelInstallState.Installed, store.state("translation.multilingual.gguf"))
    }
}
```

- [x] **Step 2: Run tests and verify failure**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests com.xtranslate.model.ModelRegistryTest
```

Expected: FAIL because model registry classes do not exist.

- [x] **Step 3: Add model registry**

Create `app/src/main/java/com/xtranslate/model/ModelRegistry.kt`:

```kotlin
package com.xtranslate.model

enum class EngineType {
    LlamaOcr,
    LlamaTranslation,
    WhisperStt,
    OnnxTts,
}

enum class RamTier {
    Low,
    Medium,
    High,
}

data class ModelFile(
    val name: String,
    val required: Boolean = true,
    val downloadUrl: String? = null,
    val checksumSha256: String? = null,
)

data class ModelPack(
    val id: String,
    val displayName: String,
    val engineType: EngineType,
    val files: List<ModelFile>,
    val expectedSizeMb: Int? = null,
    val minimumRamTier: RamTier,
)

object ModelRegistry {
    fun defaultPacks(): List<ModelPack> = listOf(
        ModelPack(
            id = "ocr.paddleocr-vl-1_5.q4",
            displayName = "PaddleOCR-VL 1.5 Q4",
            engineType = EngineType.LlamaOcr,
            files = listOf(
                ModelFile(name = "paddleocr-vl-1.5-q4.gguf"),
                ModelFile(name = "paddleocr-vl-1.5-mmproj.gguf"),
            ),
            expectedSizeMb = null,
            minimumRamTier = RamTier.Low,
        ),
        ModelPack(
            id = "translation.multilingual.gguf",
            displayName = "Broad Multilingual Translator",
            engineType = EngineType.LlamaTranslation,
            files = listOf(ModelFile(name = "multilingual-translator.gguf")),
            expectedSizeMb = null,
            minimumRamTier = RamTier.Low,
        ),
        ModelPack(
            id = "stt.whisper",
            displayName = "Whisper STT",
            engineType = EngineType.WhisperStt,
            files = listOf(ModelFile(name = "whisper.bin")),
            expectedSizeMb = null,
            minimumRamTier = RamTier.Low,
        ),
        ModelPack(
            id = "tts.supertonic-3",
            displayName = "Supertonic 3 TTS",
            engineType = EngineType.OnnxTts,
            files = listOf(ModelFile(name = "supertonic-3.onnx")),
            expectedSizeMb = null,
            minimumRamTier = RamTier.Medium,
        ),
    )
}
```

- [x] **Step 4: Add model store**

Create `app/src/main/java/com/xtranslate/model/ModelStore.kt`:

```kotlin
package com.xtranslate.model

enum class ModelInstallState {
    Missing,
    Downloading,
    Installed,
    Failed,
}

interface ModelStore {
    fun packs(): List<ModelPack>
    fun state(modelId: String): ModelInstallState
    fun markDownloading(modelId: String)
    fun markInstalled(modelId: String)
    fun markFailed(modelId: String)
}

class InMemoryModelStore(
    private val modelPacks: List<ModelPack>,
) : ModelStore {
    private val states = modelPacks.associate { it.id to ModelInstallState.Missing }.toMutableMap()

    override fun packs(): List<ModelPack> = modelPacks

    override fun state(modelId: String): ModelInstallState =
        states[modelId] ?: ModelInstallState.Missing

    override fun markDownloading(modelId: String) {
        states[modelId] = ModelInstallState.Downloading
    }

    override fun markInstalled(modelId: String) {
        states[modelId] = ModelInstallState.Installed
    }

    override fun markFailed(modelId: String) {
        states[modelId] = ModelInstallState.Failed
    }
}
```

- [x] **Step 5: Run model tests and verify pass**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests com.xtranslate.model.ModelRegistryTest
```

Expected: PASS.

- [x] **Step 6: Commit model registry**

```bash
git add app/src/main/java/com/xtranslate/model app/src/test/java/com/xtranslate/model/ModelRegistryTest.kt
git commit -m "feat: add model registry"
```

---

### Task 4: Add Engine Coordinator And Fake Engines

**Files:**
- Create: `app/src/main/java/com/xtranslate/runtime/EngineCoordinator.kt`
- Create: `app/src/main/java/com/xtranslate/runtime/FakeEngines.kt`
- Test: `app/src/test/java/com/xtranslate/runtime/EngineCoordinatorTest.kt`

- [x] **Step 1: Write failing coordinator tests**

Create `app/src/test/java/com/xtranslate/runtime/EngineCoordinatorTest.kt`:

```kotlin
package com.xtranslate.runtime

import com.xtranslate.domain.ImageInput
import com.xtranslate.domain.TranslationRequest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EngineCoordinatorTest {
    @Test
    fun textTranslationLoadsOnlyTranslator() = runTest {
        val coordinator = EngineCoordinator(
            ocrEngine = FakeOcrEngine(),
            translationEngine = FakeTranslationEngine(),
            sttEngine = FakeSpeechToTextEngine(),
            ttsEngine = FakeTextToSpeechEngine(),
            lowMemoryMode = true,
        )

        coordinator.translateText(TranslationRequest("Hello", "English", "Filipino"))

        assertFalse(coordinator.loadedEngines.contains(LoadedEngine.Ocr))
        assertTrue(coordinator.loadedEngines.contains(LoadedEngine.Translation))
    }

    @Test
    fun imageTranslationUnloadsOcrBeforeTranslationInLowMemoryMode() = runTest {
        val coordinator = EngineCoordinator(
            ocrEngine = FakeOcrEngine(text = "Hello"),
            translationEngine = FakeTranslationEngine(),
            sttEngine = FakeSpeechToTextEngine(),
            ttsEngine = FakeTextToSpeechEngine(),
            lowMemoryMode = true,
        )

        val result = coordinator.translateImage(
            image = ImageInput(uri = "content://image"),
            targetLanguage = "Filipino",
        )

        assertEquals("[Filipino] Hello", result.translatedText)
        assertFalse(coordinator.loadedEngines.contains(LoadedEngine.Ocr))
        assertTrue(coordinator.loadedEngines.contains(LoadedEngine.Translation))
    }
}
```

- [x] **Step 2: Run tests and verify failure**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests com.xtranslate.runtime.EngineCoordinatorTest
```

Expected: FAIL because coordinator and fake engines do not exist.

- [x] **Step 3: Add fake engines**

Create `app/src/main/java/com/xtranslate/runtime/FakeEngines.kt`:

```kotlin
package com.xtranslate.runtime

import com.xtranslate.domain.AudioInput
import com.xtranslate.domain.AudioOutput
import com.xtranslate.domain.ImageInput
import com.xtranslate.domain.OcrEngine
import com.xtranslate.domain.OcrResult
import com.xtranslate.domain.SpeechRequest
import com.xtranslate.domain.SpeechToTextEngine
import com.xtranslate.domain.TextToSpeechEngine
import com.xtranslate.domain.Transcript
import com.xtranslate.domain.TranslationEngine
import com.xtranslate.domain.TranslationRequest
import com.xtranslate.domain.TranslationResult

class FakeOcrEngine(
    private val text: String = "Extracted text from image",
) : OcrEngine {
    override suspend fun extractText(image: ImageInput): OcrResult =
        OcrResult(text = text, blocks = listOf(text))
}

class FakeTranslationEngine : TranslationEngine {
    override suspend fun translate(request: TranslationRequest): TranslationResult =
        TranslationResult(
            translatedText = "[${request.targetLanguage}] ${request.sourceText}",
            sourceText = request.sourceText,
            targetLanguage = request.targetLanguage,
        )
}

class FakeSpeechToTextEngine(
    private val transcript: String = "Voice transcript",
) : SpeechToTextEngine {
    override suspend fun transcribe(audio: AudioInput): Transcript =
        Transcript(text = transcript, confidence = 0.99f)
}

class FakeTextToSpeechEngine : TextToSpeechEngine {
    override suspend fun synthesize(request: SpeechRequest): AudioOutput =
        AudioOutput(uri = "memory://tts/${request.language}", durationMillis = 1200L)
}
```

- [x] **Step 4: Add engine coordinator**

Create `app/src/main/java/com/xtranslate/runtime/EngineCoordinator.kt`:

```kotlin
package com.xtranslate.runtime

import com.xtranslate.domain.AudioInput
import com.xtranslate.domain.AudioOutput
import com.xtranslate.domain.ImageInput
import com.xtranslate.domain.OcrEngine
import com.xtranslate.domain.SpeechRequest
import com.xtranslate.domain.SpeechToTextEngine
import com.xtranslate.domain.TextToSpeechEngine
import com.xtranslate.domain.TranslationEngine
import com.xtranslate.domain.TranslationRequest
import com.xtranslate.domain.TranslationResult

enum class LoadedEngine {
    Ocr,
    Translation,
    Stt,
    Tts,
}

class EngineCoordinator(
    private val ocrEngine: OcrEngine,
    private val translationEngine: TranslationEngine,
    private val sttEngine: SpeechToTextEngine,
    private val ttsEngine: TextToSpeechEngine,
    private val lowMemoryMode: Boolean,
) {
    private val mutableLoadedEngines = linkedSetOf<LoadedEngine>()
    val loadedEngines: Set<LoadedEngine> get() = mutableLoadedEngines.toSet()

    suspend fun translateText(request: TranslationRequest): TranslationResult {
        load(LoadedEngine.Translation)
        return translationEngine.translate(request)
    }

    suspend fun translateImage(
        image: ImageInput,
        targetLanguage: String,
        sourceLanguage: String? = null,
    ): TranslationResult {
        load(LoadedEngine.Ocr)
        val ocr = ocrEngine.extractText(image)
        if (lowMemoryMode) unload(LoadedEngine.Ocr)
        return translateText(
            TranslationRequest(
                sourceText = ocr.text,
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLanguage,
            )
        )
    }

    suspend fun transcribe(audio: AudioInput): String {
        load(LoadedEngine.Stt)
        val transcript = sttEngine.transcribe(audio).text
        if (lowMemoryMode) unload(LoadedEngine.Stt)
        return transcript
    }

    suspend fun speak(request: SpeechRequest): AudioOutput {
        load(LoadedEngine.Tts)
        return ttsEngine.synthesize(request)
    }

    fun unloadIdle() {
        mutableLoadedEngines.clear()
    }

    private fun load(engine: LoadedEngine) {
        mutableLoadedEngines.add(engine)
    }

    private fun unload(engine: LoadedEngine) {
        mutableLoadedEngines.remove(engine)
    }
}
```

- [x] **Step 5: Run coordinator tests and verify pass**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests com.xtranslate.runtime.EngineCoordinatorTest
```

Expected: PASS.

- [x] **Step 6: Commit coordinator**

```bash
git add app/src/main/java/com/xtranslate/runtime app/src/test/java/com/xtranslate/runtime/EngineCoordinatorTest.kt
git commit -m "feat: add lazy engine coordinator"
```

---

### Task 5: Add Chat ViewModel And Workflow Tests

**Files:**
- Create: `app/src/main/java/com/xtranslate/ui/chat/ChatModels.kt`
- Create: `app/src/main/java/com/xtranslate/ui/chat/ChatViewModel.kt`
- Test: `app/src/test/java/com/xtranslate/ui/chat/ChatViewModelTest.kt`

- [x] **Step 1: Write failing chat workflow tests**

Create `app/src/test/java/com/xtranslate/ui/chat/ChatViewModelTest.kt`:

```kotlin
package com.xtranslate.ui.chat

import com.xtranslate.domain.ImageInput
import com.xtranslate.runtime.EngineCoordinator
import com.xtranslate.runtime.FakeOcrEngine
import com.xtranslate.runtime.FakeSpeechToTextEngine
import com.xtranslate.runtime.FakeTextToSpeechEngine
import com.xtranslate.runtime.FakeTranslationEngine
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatViewModelTest {
    @Test
    fun sendTextAddsSourceAndTranslationMessages() = runTest {
        val viewModel = ChatViewModel(fakeCoordinator())

        viewModel.updateComposer("Hello")
        viewModel.sendText()

        assertEquals("", viewModel.state.value.composerText)
        assertEquals(2, viewModel.state.value.messages.size)
        assertEquals(ChatMessageKind.Source, viewModel.state.value.messages[0].kind)
        assertEquals(ChatMessageKind.Translation, viewModel.state.value.messages[1].kind)
    }

    @Test
    fun translateImageAddsOcrReviewAndTranslationMessages() = runTest {
        val viewModel = ChatViewModel(fakeCoordinator())

        viewModel.translateImage(ImageInput("content://image"))

        val messages = viewModel.state.value.messages
        assertTrue(messages.any { it.kind == ChatMessageKind.OcrReview })
        assertTrue(messages.any { it.kind == ChatMessageKind.Translation })
    }

    private fun fakeCoordinator() = EngineCoordinator(
        ocrEngine = FakeOcrEngine(text = "Image text"),
        translationEngine = FakeTranslationEngine(),
        sttEngine = FakeSpeechToTextEngine(),
        ttsEngine = FakeTextToSpeechEngine(),
        lowMemoryMode = true,
    )
}
```

- [x] **Step 2: Run tests and verify failure**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests com.xtranslate.ui.chat.ChatViewModelTest
```

Expected: FAIL because chat state and ViewModel do not exist.

- [x] **Step 3: Add chat state models**

Create `app/src/main/java/com/xtranslate/ui/chat/ChatModels.kt`:

```kotlin
package com.xtranslate.ui.chat

enum class ChatMessageKind {
    Source,
    OcrReview,
    Translation,
    System,
}

data class ChatMessage(
    val id: Long,
    val kind: ChatMessageKind,
    val text: String,
    val language: String? = null,
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val composerText: String = "",
    val targetLanguage: String = "English",
    val isBusy: Boolean = false,
    val selectedTab: AppTab = AppTab.Chat,
)

enum class AppTab {
    Chat,
    Models,
}
```

- [x] **Step 4: Add chat ViewModel**

Create `app/src/main/java/com/xtranslate/ui/chat/ChatViewModel.kt`:

```kotlin
package com.xtranslate.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xtranslate.domain.ImageInput
import com.xtranslate.domain.TranslationRequest
import com.xtranslate.runtime.EngineCoordinator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val engineCoordinator: EngineCoordinator,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = mutableState

    private var nextId = 1L

    fun updateComposer(text: String) {
        mutableState.update { it.copy(composerText = text) }
    }

    fun updateTargetLanguage(language: String) {
        mutableState.update { it.copy(targetLanguage = language) }
    }

    fun selectTab(tab: AppTab) {
        mutableState.update { it.copy(selectedTab = tab) }
    }

    fun sendText() {
        val text = state.value.composerText.trim()
        if (text.isEmpty()) return

        mutableState.update {
            it.copy(
                composerText = "",
                isBusy = true,
                messages = it.messages + ChatMessage(nextId++, ChatMessageKind.Source, text)
            )
        }

        viewModelScope.launch {
            val targetLanguage = state.value.targetLanguage
            val result = engineCoordinator.translateText(
                TranslationRequest(
                    sourceText = text,
                    targetLanguage = targetLanguage,
                )
            )
            mutableState.update {
                it.copy(
                    isBusy = false,
                    messages = it.messages + ChatMessage(
                        id = nextId++,
                        kind = ChatMessageKind.Translation,
                        text = result.translatedText,
                        language = result.targetLanguage,
                    )
                )
            }
        }
    }

    suspend fun translateImage(image: ImageInput) {
        mutableState.update { it.copy(isBusy = true) }
        val result = engineCoordinator.translateImage(
            image = image,
            targetLanguage = state.value.targetLanguage,
        )
        mutableState.update {
            it.copy(
                isBusy = false,
                messages = it.messages +
                    ChatMessage(nextId++, ChatMessageKind.OcrReview, result.sourceText) +
                    ChatMessage(nextId++, ChatMessageKind.Translation, result.translatedText, result.targetLanguage)
            )
        }
    }
}
```

- [x] **Step 5: Run chat workflow tests and verify pass**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests com.xtranslate.ui.chat.ChatViewModelTest
```

Expected: PASS.

- [x] **Step 6: Commit chat workflow**

```bash
git add app/src/main/java/com/xtranslate/ui/chat/ChatModels.kt app/src/main/java/com/xtranslate/ui/chat/ChatViewModel.kt app/src/test/java/com/xtranslate/ui/chat/ChatViewModelTest.kt
git commit -m "feat: add chat workflow state"
```

---

### Task 6: Add Claude-like Mobile Compose UI

**Files:**
- Create: `app/src/main/java/com/xtranslate/MainActivity.kt`
- Create: `app/src/main/java/com/xtranslate/ui/theme/Theme.kt`
- Create: `app/src/main/java/com/xtranslate/ui/XTranslateApp.kt`
- Create: `app/src/main/java/com/xtranslate/ui/chat/ChatScreen.kt`
- Create: `app/src/main/java/com/xtranslate/ui/models/ModelsScreen.kt`

- [x] **Step 1: Add app theme**

Create `app/src/main/java/com/xtranslate/ui/theme/Theme.kt`:

```kotlin
package com.xtranslate.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WarmLightScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF24211D),
    onPrimary = Color(0xFFFFFDF8),
    secondary = Color(0xFF756B5F),
    onSecondary = Color(0xFFFFFDF8),
    background = Color(0xFFFAF8F2),
    onBackground = Color(0xFF26231F),
    surface = Color(0xFFFFFDF8),
    onSurface = Color(0xFF26231F),
    surfaceVariant = Color(0xFFF0E9DF),
    onSurfaceVariant = Color(0xFF6E665C),
    outline = Color(0xFFE2DBCF),
)

@Composable
fun XTranslateTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WarmLightScheme,
        content = content,
    )
}
```

- [x] **Step 2: Add chat screen**

Create `app/src/main/java/com/xtranslate/ui/chat/ChatScreen.kt`:

```kotlin
package com.xtranslate.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChatScreen(
    state: ChatUiState,
    onComposerChange: (String) -> Unit,
    onSend: () -> Unit,
    onImage: () -> Unit,
    onMic: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(text = "xTranslate")

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.messages, key = { it.id }) { message ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = message.kind.name)
                        Text(text = message.text)
                    }
                }
            }
        }

        Surface(tonalElevation = 2.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = state.composerText,
                    onValueChange = onComposerChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Type, speak, or add an image") },
                    minLines = 2,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onImage) { Text("IMG") }
                    IconButton(onClick = onMic) { Text("MIC") }
                    Button(
                        onClick = onSend,
                        modifier = Modifier.weight(1f),
                        enabled = !state.isBusy,
                    ) {
                        Text(if (state.isBusy) "Working" else "Translate")
                    }
                }
            }
        }
    }
}
```

- [x] **Step 3: Add models screen**

Create `app/src/main/java/com/xtranslate/ui/models/ModelsScreen.kt`:

```kotlin
package com.xtranslate.ui.models

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xtranslate.model.ModelStore

@Composable
fun ModelsScreen(
    modelStore: ModelStore,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(text = "Models")
        modelStore.packs().forEach { pack ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = pack.displayName)
                    Text(text = "Engine: ${pack.engineType}")
                    Text(text = "State: ${modelStore.state(pack.id)}")
                    Text(text = "Loaded only when used")
                }
            }
        }
    }
}
```

- [x] **Step 4: Add app shell**

Create `app/src/main/java/com/xtranslate/ui/XTranslateApp.kt`:

```kotlin
package com.xtranslate.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.xtranslate.domain.ImageInput
import com.xtranslate.model.ModelStore
import com.xtranslate.ui.chat.AppTab
import com.xtranslate.ui.chat.ChatScreen
import com.xtranslate.ui.chat.ChatViewModel
import com.xtranslate.ui.models.ModelsScreen

@Composable
fun XTranslateApp(
    chatViewModel: ChatViewModel,
    modelStore: ModelStore,
) {
    val state by chatViewModel.state.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = state.selectedTab == AppTab.Chat,
                    onClick = { chatViewModel.selectTab(AppTab.Chat) },
                    label = { Text("Chat") },
                    icon = { Text("C") },
                )
                NavigationBarItem(
                    selected = state.selectedTab == AppTab.Models,
                    onClick = { chatViewModel.selectTab(AppTab.Models) },
                    label = { Text("Models") },
                    icon = { Text("M") },
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.weight(1f)) {
                when (state.selectedTab) {
                    AppTab.Chat -> ChatScreen(
                        state = state,
                        onComposerChange = chatViewModel::updateComposer,
                        onSend = chatViewModel::sendText,
                        onImage = {
                            chatViewModel.updateComposer("Image flow will open camera/gallery in the native integration slice.")
                        },
                        onMic = {
                            chatViewModel.updateComposer("Voice flow will record audio in the native integration slice.")
                        },
                    )
                    AppTab.Models -> ModelsScreen(modelStore = modelStore)
                }
            }
        }
    }
}
```

- [x] **Step 5: Add activity entrypoint**

Create `app/src/main/java/com/xtranslate/MainActivity.kt`:

```kotlin
package com.xtranslate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.xtranslate.model.InMemoryModelStore
import com.xtranslate.model.ModelRegistry
import com.xtranslate.runtime.EngineCoordinator
import com.xtranslate.runtime.FakeOcrEngine
import com.xtranslate.runtime.FakeSpeechToTextEngine
import com.xtranslate.runtime.FakeTextToSpeechEngine
import com.xtranslate.runtime.FakeTranslationEngine
import com.xtranslate.ui.XTranslateApp
import com.xtranslate.ui.chat.ChatViewModel
import com.xtranslate.ui.theme.XTranslateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val coordinator = EngineCoordinator(
            ocrEngine = FakeOcrEngine(),
            translationEngine = FakeTranslationEngine(),
            sttEngine = FakeSpeechToTextEngine(),
            ttsEngine = FakeTextToSpeechEngine(),
            lowMemoryMode = true,
        )
        val chatViewModel = ChatViewModel(coordinator)
        val modelStore = InMemoryModelStore(ModelRegistry.defaultPacks())

        setContent {
            XTranslateTheme {
                XTranslateApp(
                    chatViewModel = chatViewModel,
                    modelStore = modelStore,
                )
            }
        }
    }
}
```

- [x] **Step 6: Run compile verification**

Run:

```bash
./gradlew :app:compileDebugKotlin
```

Expected: PASS. If Compose complains about missing imports or unused variables, fix only the compile error and rerun.

- [x] **Step 7: Commit Compose UI**

```bash
git add app/src/main/java/com/xtranslate/MainActivity.kt app/src/main/java/com/xtranslate/ui
git commit -m "feat: add mobile chat ui"
```

---

### Task 7: Run Full Foundation Verification

**Files:**
- Modify only files required to fix compile or test failures from this task.

- [x] **Step 1: Run all unit tests**

Run:

```bash
./gradlew :app:testDebugUnitTest
```

Expected: PASS.

- [x] **Step 2: Run debug build**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: PASS and an APK under `app/build/outputs/apk/debug/`.

- [x] **Step 3: Inspect git state**

Run:

```bash
git status --short
```

Expected: only intentional files modified. `.planning/PROJECT.md` may still show as deleted from pre-existing state; do not stage it.

- [x] **Step 4: Commit verification fixes if any**

If Step 1 or Step 2 required source fixes:

```bash
git add app/src/main app/src/test
git commit -m "fix: stabilize foundation build"
```

If no fixes were required, do not create an empty commit.

---

## Follow-up Plans

After this foundation passes, create separate implementation plans for:

1. Native llama.cpp Android runtime wrapper.
2. PaddleOCR-VL-1.5 Q4 image-text-to-text profile integration.
3. Broad multilingual translation GGUF profile integration.
4. App-private model downloader with Hugging Face resolve URLs and checksums.
5. Whisper STT integration.
6. Supertonic 3 ONNX Runtime TTS integration.
7. Android camera/gallery/audio permission flows and device benchmarking.

## Self-Review Notes

- Spec coverage: this plan covers Android-first Kotlin scaffold, Claude-like mobile chat UI, domain interfaces, model registry, lazy-loading coordinator, and mocked text/image/voice flows.
- Deferred spec items: real llama.cpp, PaddleOCR-VL, Whisper, Supertonic/ONNX, downloads, and device testing are intentionally follow-up plans because they are independent native/runtime subsystems.
- Red-flag scan: no task uses unfinished markers or open-ended implementation instructions.
- Type consistency: `TranslationRequest`, `EngineCoordinator`, `ChatUiState`, `ModelStore`, and fake engine names are consistent across tasks.
