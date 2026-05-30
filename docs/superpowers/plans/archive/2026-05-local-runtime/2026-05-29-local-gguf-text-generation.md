# Local GGUF Text Generation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a first text-only path that loads a local GGUF model from app-private storage and sends a prompt through the official llama.cpp Android bridge.

**Architecture:** Keep model download out of scope. Add a small model path resolver for app-private files, a text-generation use case that combines `AndroidLlamaRuntime`, `OfficialNativeLlamaBridge`, and `LlamaTranslationEngine`, and a developer-only UI action to prove the runtime path can be called from the app.

**Tech Stack:** Kotlin, Android app-private files, official llama.cpp Android library, Jetpack Compose, kotlinx.coroutines.

---

## Scope Check

Included:

- App-private model path resolver.
- Text-only llama profile factory.
- Developer text generation runner.
- UI button on Models screen to run a local text prompt when a GGUF file exists.
- Unit tests for path/profile construction.

Deferred:

- Model downloading.
- Hugging Face resolve URLs.
- Model checksum verification.
- PaddleOCR-VL image/projector input.
- User-facing model selection.
- Streaming tokens into the chat UI.

## Manual Model Placement

For this plan, the model must be manually placed at:

```text
<app-private-files-dir>/models/translation/multilingual-translator.gguf
```

On an emulator/device, this can be pushed later with `adb` after the app is installed. The exact command depends on the app sandbox path and will be documented in the follow-up device test plan.

## File Structure

Create or modify:

- `app/src/main/java/com/xtranslate/model/LocalModelPaths.kt`
- `app/src/main/java/com/xtranslate/llama/LlamaProfileFactory.kt`
- `app/src/main/java/com/xtranslate/llama/LocalTextGenerationRunner.kt`
- `app/src/test/java/com/xtranslate/model/LocalModelPathsTest.kt`
- `app/src/test/java/com/xtranslate/llama/LlamaProfileFactoryTest.kt`
- `app/src/main/java/com/xtranslate/ui/models/ModelsScreen.kt`
- `app/src/main/java/com/xtranslate/ui/XTranslateApp.kt`
- `app/src/main/java/com/xtranslate/MainActivity.kt`

---

### Task 1: Add Local Model Path Resolver

**Files:**
- Create: `app/src/main/java/com/xtranslate/model/LocalModelPaths.kt`
- Create: `app/src/test/java/com/xtranslate/model/LocalModelPathsTest.kt`

- [x] **Step 1: Write path resolver test**

Create `app/src/test/java/com/xtranslate/model/LocalModelPathsTest.kt`:

```kotlin
package com.xtranslate.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * Tests app-private file paths for local model files.
 */
class LocalModelPathsTest {
    @Test
    fun translationModelPathUsesAppPrivateModelsDirectory() {
        val filesDir = File("/data/user/0/com.xtranslate/files")
        val paths = LocalModelPaths(filesDir)

        assertEquals(
            "/data/user/0/com.xtranslate/files/models/translation/multilingual-translator.gguf",
            paths.translationModelFile().path.replace('\\', '/'),
        )
    }
}
```

- [x] **Step 2: Run test and verify failure**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.model.LocalModelPathsTest
```

Expected: FAIL because `LocalModelPaths` does not exist.

- [x] **Step 3: Add path resolver**

Create `app/src/main/java/com/xtranslate/model/LocalModelPaths.kt`:

```kotlin
package com.xtranslate.model

import java.io.File

/**
 * Builds app-private file paths for local model files.
 */
class LocalModelPaths(
    private val filesDir: File,
) {
    fun translationModelFile(): File =
        File(filesDir, "models/translation/multilingual-translator.gguf")
}
```

- [x] **Step 4: Run test and verify pass**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.model.LocalModelPathsTest
```

Expected: PASS.

---

### Task 2: Add llama Profile Factory

**Files:**
- Create: `app/src/main/java/com/xtranslate/llama/LlamaProfileFactory.kt`
- Create: `app/src/test/java/com/xtranslate/llama/LlamaProfileFactoryTest.kt`

- [x] **Step 1: Write profile factory test**

Create `app/src/test/java/com/xtranslate/llama/LlamaProfileFactoryTest.kt`:

```kotlin
package com.xtranslate.llama

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

/**
 * Tests creation of llama profiles from local model files.
 */
class LlamaProfileFactoryTest {
    @Test
    fun translationProfileUsesTextOnlyLlamaProfile() {
        val modelFile = File("/models/translation.gguf")

        val profile = LlamaProfileFactory.translationProfile(modelFile)

        assertEquals("translation.multilingual.gguf", profile.id)
        assertEquals(LlamaProfileKind.Translation, profile.kind)
        assertEquals("/models/translation.gguf", profile.modelPath.replace('\\', '/'))
        assertNull(profile.projectorPath)
    }
}
```

- [x] **Step 2: Run test and verify failure**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.llama.LlamaProfileFactoryTest
```

Expected: FAIL because `LlamaProfileFactory` does not exist.

- [x] **Step 3: Add profile factory**

Create `app/src/main/java/com/xtranslate/llama/LlamaProfileFactory.kt`:

```kotlin
package com.xtranslate.llama

import java.io.File

/**
 * Creates llama model profiles from app-private model files.
 */
object LlamaProfileFactory {
    fun translationProfile(modelFile: File): LlamaProfile =
        LlamaProfile(
            id = "translation.multilingual.gguf",
            kind = LlamaProfileKind.Translation,
            modelPath = modelFile.absolutePath,
            projectorPath = null,
        )
}
```

- [x] **Step 4: Run test and verify pass**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.llama.LlamaProfileFactoryTest
```

Expected: PASS.

---

### Task 3: Add Local Text Generation Runner

**Files:**
- Create: `app/src/main/java/com/xtranslate/llama/LocalTextGenerationRunner.kt`

- [x] **Step 1: Add runner**

Create `app/src/main/java/com/xtranslate/llama/LocalTextGenerationRunner.kt`:

```kotlin
package com.xtranslate.llama

import com.xtranslate.domain.TranslationRequest
import com.xtranslate.llama.nativebridge.NativeLlamaBridge
import com.xtranslate.model.LocalModelPaths

/**
 * Developer-only runner for the first local GGUF text generation path.
 */
class LocalTextGenerationRunner(
    private val modelPaths: LocalModelPaths,
    private val bridge: NativeLlamaBridge,
) {
    suspend fun translateSampleText(): String {
        val modelFile = modelPaths.translationModelFile()
        require(modelFile.exists()) {
            "Missing model file: ${modelFile.absolutePath}"
        }

        val runtime = AndroidLlamaRuntime(bridge)
        val engine =
            LlamaTranslationEngine(
                runtime = runtime,
                profile = LlamaProfileFactory.translationProfile(modelFile),
            )

        return engine
            .translate(
                TranslationRequest(
                    sourceText = "Hello",
                    sourceLanguage = "English",
                    targetLanguage = "Filipino",
                ),
            ).translatedText
    }
}
```

- [x] **Step 2: Compile**

Run:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Expected: PASS.

---

### Task 4: Add Developer Button To Models Screen

**Files:**
- Modify: `app/src/main/java/com/xtranslate/ui/models/ModelsScreen.kt`
- Modify: `app/src/main/java/com/xtranslate/ui/XTranslateApp.kt`
- Modify: `app/src/main/java/com/xtranslate/MainActivity.kt`

- [x] **Step 1: Update ModelsScreen**

Change `ModelsScreen` signature to:

```kotlin
fun ModelsScreen(
    modelStore: ModelStore,
    onRunLocalTextTest: () -> Unit,
    localTextTestStatus: String?,
    modifier: Modifier = Modifier,
)
```

Add this button below the `Models` title:

```kotlin
Button(onClick = onRunLocalTextTest) {
    Text("Run local text test")
}

if (localTextTestStatus != null) {
    Text(text = localTextTestStatus)
}
```

Add import:

```kotlin
import androidx.compose.material3.Button
```

- [x] **Step 2: Update XTranslateApp**

Change `XTranslateApp` signature to:

```kotlin
fun XTranslateApp(
    chatViewModel: ChatViewModel,
    modelStore: ModelStore,
    onRunLocalTextTest: () -> Unit,
    localTextTestStatus: String?,
)
```

Change the Models tab call to:

```kotlin
ModelsScreen(
    modelStore = modelStore,
    onRunLocalTextTest = onRunLocalTextTest,
    localTextTestStatus = localTextTestStatus,
)
```

- [x] **Step 3: Update MainActivity**

Add imports:

```kotlin
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.xtranslate.llama.LocalTextGenerationRunner
import com.xtranslate.llama.nativebridge.OfficialNativeLlamaBridge
import com.xtranslate.model.LocalModelPaths
import kotlinx.coroutines.launch
```

Inside `setContent`, before `XTranslateApp`, add:

```kotlin
val scope = rememberCoroutineScope()
var localTextTestStatus by remember { mutableStateOf<String?>(null) }
val runner =
    remember {
        LocalTextGenerationRunner(
            modelPaths = LocalModelPaths(filesDir),
            bridge = OfficialNativeLlamaBridge(this),
        )
    }
```

Pass these into `XTranslateApp`:

```kotlin
onRunLocalTextTest = {
    localTextTestStatus = "Running local text test..."
    scope.launch {
        localTextTestStatus =
            runCatching { runner.translateSampleText() }
                .fold(
                    onSuccess = { "Result: $it" },
                    onFailure = { "Error: ${it.message}" },
                )
    }
},
localTextTestStatus = localTextTestStatus,
```

- [x] **Step 4: Compile**

Run:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Expected: PASS.

---

### Task 5: Verify Plan

**Files:**
- Modify only files required to fix failures.

- [x] **Step 1: Run new tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.model.LocalModelPathsTest --tests com.xtranslate.llama.LlamaProfileFactoryTest
```

Expected: PASS.

- [x] **Step 2: Run all unit tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Expected: PASS.

- [x] **Step 3: Build debug APK**

Run:

```powershell
.\gradlew.bat :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 4: Commit once for the full plan**

```powershell
git add app/src/main/java/com/xtranslate/model/LocalModelPaths.kt app/src/main/java/com/xtranslate/llama/LlamaProfileFactory.kt app/src/main/java/com/xtranslate/llama/LocalTextGenerationRunner.kt app/src/test/java/com/xtranslate/model/LocalModelPathsTest.kt app/src/test/java/com/xtranslate/llama/LlamaProfileFactoryTest.kt app/src/main/java/com/xtranslate/ui/models/ModelsScreen.kt app/src/main/java/com/xtranslate/ui/XTranslateApp.kt app/src/main/java/com/xtranslate/MainActivity.kt docs/superpowers/plans/2026-05-29-local-gguf-text-generation.md
git commit -m "feat: add local gguf text generation path"
```

---

## Self-Review Notes

- Spec coverage: this plan adds the first text-only path from app-private GGUF file to llama.cpp runtime.
- Deferred work: download, checksums, image/projector support, and user-facing model selection remain separate.
- Red-flag scan: no unfinished markers or open-ended implementation instructions.
- Type consistency: `LocalModelPaths`, `LlamaProfileFactory`, and `LocalTextGenerationRunner` are used consistently.
