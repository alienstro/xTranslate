# llama.cpp Runtime Wrapper Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a Kotlin llama.cpp runtime adapter layer that can later connect OCR and translation engines to the official Android llama.cpp binding.

**Architecture:** This plan does not build native llama.cpp yet. It adds app-owned runtime interfaces, request/result types, prompt-based fake runtime tests, and engine adapters that let the existing `OcrEngine` and `TranslationEngine` use a shared llama runtime contract. The real native binding can then be added behind the same interface in a follow-up plan.

**Tech Stack:** Kotlin, Android Gradle Plugin, kotlinx.coroutines `Flow`, JUnit 4, existing xTranslate domain contracts.

---

## Source Reference

Use the official llama.cpp Android direction as the source of truth:

- https://github.com/ggml-org/llama.cpp/blob/master/docs/android.md

Relevant facts from the official docs:

- llama.cpp includes an Android binding under `examples/llama.android`.
- The Android binding can read GGUF metadata from a `Uri` or local app-private `File`.
- The binding can load a selected model from an app-private file path.
- Token output can be collected from Kotlin `Flow`.

## Scope Check

This plan is a runtime-wrapper slice, not the full native integration.

Included:

- App-owned llama runtime contract.
- Fake llama runtime for tests.
- OCR and translation adapters that implement existing domain engine interfaces.
- Unit tests that prove prompts flow into the llama runtime.

Deferred:

- Importing or vendoring llama.cpp source.
- Building CMake/NDK native libraries.
- Vision/projector image input for PaddleOCR-VL.
- Real GGUF metadata parsing.
- App-private model download and checksum verification.

## File Structure

Create these files:

- `app/src/main/java/com/xtranslate/llama/LlamaRuntime.kt` - common llama runtime contract.
- `app/src/main/java/com/xtranslate/llama/FakeLlamaRuntime.kt` - fake runtime for tests and UI previews.
- `app/src/main/java/com/xtranslate/llama/LlamaTranslationEngine.kt` - adapts `LlamaRuntime` to `TranslationEngine`.
- `app/src/main/java/com/xtranslate/llama/LlamaOcrEngine.kt` - adapts `LlamaRuntime` to `OcrEngine`.
- `app/src/test/java/com/xtranslate/llama/LlamaTranslationEngineTest.kt` - translation adapter tests.
- `app/src/test/java/com/xtranslate/llama/LlamaOcrEngineTest.kt` - OCR adapter tests.

---

### Task 1: Add llama Runtime Contract

**Files:**
- Create: `app/src/main/java/com/xtranslate/llama/LlamaRuntime.kt`
- Test: no direct test; tested through adapters in later tasks.

- [ ] **Step 1: Create runtime contract**

Create `app/src/main/java/com/xtranslate/llama/LlamaRuntime.kt`:

```kotlin
package com.xtranslate.llama

import kotlinx.coroutines.flow.Flow

/**
 * Common API for local llama.cpp model sessions.
 *
 * The real Android binding will load a GGUF model from app-private storage and
 * stream generated tokens. Tests use a fake version of this interface.
 */
interface LlamaRuntime {
    val loadedProfile: LlamaProfile?

    suspend fun load(profile: LlamaProfile)

    suspend fun unload()

    fun generate(request: LlamaRequest): Flow<String>
}

enum class LlamaProfileKind {
    Ocr,
    Translation,
}

data class LlamaProfile(
    val id: String,
    val kind: LlamaProfileKind,
    val modelPath: String,
    val projectorPath: String? = null,
)

data class LlamaRequest(
    val prompt: String,
    val imageUri: String? = null,
)
```

- [ ] **Step 2: Run compile check**

Run:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Expected: PASS.

- [ ] **Step 3: Commit runtime contract**

```powershell
git add app/src/main/java/com/xtranslate/llama/LlamaRuntime.kt
git commit -m "feat: add llama runtime contract"
```

---

### Task 2: Add Fake llama Runtime

**Files:**
- Create: `app/src/main/java/com/xtranslate/llama/FakeLlamaRuntime.kt`

- [ ] **Step 1: Create fake runtime**

Create `app/src/main/java/com/xtranslate/llama/FakeLlamaRuntime.kt`:

```kotlin
package com.xtranslate.llama

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Fake llama runtime used by tests before native llama.cpp is connected.
 */
class FakeLlamaRuntime(
    private val response: String,
) : LlamaRuntime {
    private var currentProfile: LlamaProfile? = null
    val requests = mutableListOf<LlamaRequest>()

    override val loadedProfile: LlamaProfile?
        get() = currentProfile

    override suspend fun load(profile: LlamaProfile) {
        currentProfile = profile
    }

    override suspend fun unload() {
        currentProfile = null
    }

    override fun generate(request: LlamaRequest): Flow<String> =
        flow {
            requests.add(request)
            emit(response)
        }
}
```

- [ ] **Step 2: Run compile check**

Run:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Expected: PASS.

- [ ] **Step 3: Commit fake runtime**

```powershell
git add app/src/main/java/com/xtranslate/llama/FakeLlamaRuntime.kt
git commit -m "test: add fake llama runtime"
```

---

### Task 3: Add Translation Adapter

**Files:**
- Create: `app/src/main/java/com/xtranslate/llama/LlamaTranslationEngine.kt`
- Create: `app/src/test/java/com/xtranslate/llama/LlamaTranslationEngineTest.kt`

- [ ] **Step 1: Write failing translation adapter test**

Create `app/src/test/java/com/xtranslate/llama/LlamaTranslationEngineTest.kt`:

```kotlin
package com.xtranslate.llama

import com.xtranslate.domain.TranslationRequest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests the llama-backed translation engine adapter.
 */
class LlamaTranslationEngineTest {
    @Test
    fun translateLoadsTranslationProfileAndSendsPrompt() =
        runTest {
            val runtime = FakeLlamaRuntime(response = "Kamusta")
            val profile =
                LlamaProfile(
                    id = "translation.multilingual.gguf",
                    kind = LlamaProfileKind.Translation,
                    modelPath = "/models/translator.gguf",
                )
            val engine = LlamaTranslationEngine(runtime, profile)

            val result =
                engine.translate(
                    TranslationRequest(
                        sourceText = "Hello",
                        sourceLanguage = "English",
                        targetLanguage = "Filipino",
                    ),
                )

            assertEquals(profile, runtime.loadedProfile)
            assertEquals("Kamusta", result.translatedText)
            assertEquals("Hello", result.sourceText)
            assertEquals("Filipino", result.targetLanguage)
            assertTrue(runtime.requests.single().prompt.contains("Hello"))
            assertTrue(runtime.requests.single().prompt.contains("Filipino"))
        }
}
```

- [ ] **Step 2: Run test and verify failure**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.llama.LlamaTranslationEngineTest
```

Expected: FAIL because `LlamaTranslationEngine` does not exist.

- [ ] **Step 3: Add translation adapter**

Create `app/src/main/java/com/xtranslate/llama/LlamaTranslationEngine.kt`:

```kotlin
package com.xtranslate.llama

import com.xtranslate.domain.Prompts
import com.xtranslate.domain.TranslationEngine
import com.xtranslate.domain.TranslationRequest
import com.xtranslate.domain.TranslationResult
import kotlinx.coroutines.flow.toList

/**
 * Translation engine that sends prompts to a llama.cpp translation profile.
 */
class LlamaTranslationEngine(
    private val runtime: LlamaRuntime,
    private val profile: LlamaProfile,
) : TranslationEngine {
    override suspend fun translate(request: TranslationRequest): TranslationResult {
        runtime.load(profile)
        val prompt = Prompts.translationPrompt(request)
        val text =
            runtime
                .generate(LlamaRequest(prompt = prompt))
                .toList()
                .joinToString(separator = "")

        return TranslationResult(
            translatedText = text,
            sourceText = request.sourceText,
            targetLanguage = request.targetLanguage,
        )
    }
}
```

- [ ] **Step 4: Run test and verify pass**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.llama.LlamaTranslationEngineTest
```

Expected: PASS.

- [ ] **Step 5: Commit translation adapter**

```powershell
git add app/src/main/java/com/xtranslate/llama app/src/test/java/com/xtranslate/llama/LlamaTranslationEngineTest.kt
git commit -m "feat: add llama translation adapter"
```

---

### Task 4: Add OCR Adapter

**Files:**
- Create: `app/src/main/java/com/xtranslate/llama/LlamaOcrEngine.kt`
- Create: `app/src/test/java/com/xtranslate/llama/LlamaOcrEngineTest.kt`

- [ ] **Step 1: Write failing OCR adapter test**

Create `app/src/test/java/com/xtranslate/llama/LlamaOcrEngineTest.kt`:

```kotlin
package com.xtranslate.llama

import com.xtranslate.domain.ImageInput
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests the llama-backed OCR adapter for image text extraction.
 */
class LlamaOcrEngineTest {
    @Test
    fun extractTextLoadsOcrProfileAndSendsImageRequest() =
        runTest {
            val runtime = FakeLlamaRuntime(response = "Menu\nCoffee")
            val profile =
                LlamaProfile(
                    id = "ocr.paddleocr-vl-1_5.q4",
                    kind = LlamaProfileKind.Ocr,
                    modelPath = "/models/paddleocr-vl.gguf",
                    projectorPath = "/models/paddleocr-vl-mmproj.gguf",
                )
            val engine = LlamaOcrEngine(runtime, profile)

            val result = engine.extractText(ImageInput(uri = "content://image"))

            assertEquals(profile, runtime.loadedProfile)
            assertEquals("Menu\nCoffee", result.text)
            assertEquals(listOf("Menu", "Coffee"), result.blocks)
            assertEquals("content://image", runtime.requests.single().imageUri)
            assertTrue(runtime.requests.single().prompt.contains("Do not translate"))
        }
}
```

- [ ] **Step 2: Run test and verify failure**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.llama.LlamaOcrEngineTest
```

Expected: FAIL because `LlamaOcrEngine` does not exist.

- [ ] **Step 3: Add OCR adapter**

Create `app/src/main/java/com/xtranslate/llama/LlamaOcrEngine.kt`:

```kotlin
package com.xtranslate.llama

import com.xtranslate.domain.ImageInput
import com.xtranslate.domain.OcrEngine
import com.xtranslate.domain.OcrResult
import com.xtranslate.domain.Prompts
import kotlinx.coroutines.flow.toList

/**
 * OCR engine that sends image-text requests to a llama.cpp OCR/VLM profile.
 */
class LlamaOcrEngine(
    private val runtime: LlamaRuntime,
    private val profile: LlamaProfile,
) : OcrEngine {
    override suspend fun extractText(image: ImageInput): OcrResult {
        runtime.load(profile)
        val text =
            runtime
                .generate(
                    LlamaRequest(
                        prompt = Prompts.ocrExtractionPrompt(),
                        imageUri = image.uri,
                    ),
                )
                .toList()
                .joinToString(separator = "")

        return OcrResult(
            text = text,
            blocks = text.lines().filter { it.isNotBlank() },
        )
    }
}
```

- [ ] **Step 4: Run test and verify pass**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.llama.LlamaOcrEngineTest
```

Expected: PASS.

- [ ] **Step 5: Commit OCR adapter**

```powershell
git add app/src/main/java/com/xtranslate/llama app/src/test/java/com/xtranslate/llama/LlamaOcrEngineTest.kt
git commit -m "feat: add llama ocr adapter"
```

---

### Task 5: Verify Runtime Wrapper Slice

**Files:**
- Modify only files required to fix compile or test failures.

- [ ] **Step 1: Run all llama adapter tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.llama.*
```

Expected: PASS.

- [ ] **Step 2: Run all unit tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Expected: PASS.

- [ ] **Step 3: Run debug build**

Run:

```powershell
.\gradlew.bat :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 4: Commit verification fixes if any**

If fixes were needed:

```powershell
git add app/src/main app/src/test
git commit -m "fix: stabilize llama runtime wrapper"
```

If no fixes were needed, do not create an empty commit.

---

## Follow-up Plan

After this wrapper passes, create a separate plan for the native llama.cpp Android binding:

- Import or reference the official `examples/llama.android` binding pattern.
- Add app-private model path loading.
- Connect the binding to `LlamaRuntime`.
- Add device/manual tests with a small GGUF model before using PaddleOCR-VL.

## Self-Review Notes

- Spec coverage: this plan advances the llama.cpp requirement by adding app-owned OCR and translation adapters over a shared runtime contract.
- Deferred spec items: native CMake/NDK integration, projector/image handling, model downloads, and real GGUF loading are intentionally left for the native binding plan.
- Red-flag scan: no unfinished markers or open-ended implementation instructions.
- Type consistency: `LlamaRuntime`, `LlamaProfile`, `LlamaRequest`, `LlamaTranslationEngine`, and `LlamaOcrEngine` are used consistently across tests and implementation steps.
