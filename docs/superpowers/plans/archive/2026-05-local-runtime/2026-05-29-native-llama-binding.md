# Native llama.cpp Android Binding Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Connect the existing `LlamaRuntime` adapter layer to an Android native llama.cpp binding without changing the UI, chat workflow, or domain contracts.

**Architecture:** Build this in two phases. First, add a small Android-side bridge interface and `AndroidLlamaRuntime` class that can be tested without native code. Second, import the official llama.cpp Android binding pattern and make the bridge call the real native engine. This avoids mixing native build setup with app workflow code.

**Tech Stack:** Kotlin, Android Gradle Plugin, kotlinx.coroutines `Flow`, llama.cpp Android example, Android app-private model files.

---

## Source Reference

Use official llama.cpp Android docs as the source of truth:

- https://github.com/ggml-org/llama.cpp/blob/master/docs/android.md
- https://github.com/ggml-org/llama.cpp/tree/master/examples/llama.android

Important notes from the official docs:

- The official Android binding lives under `examples/llama.android`.
- The sample can parse GGUF metadata from `Uri` or app-private `File`.
- The sample loads a selected model from an app-private file path.
- The sample emits generated tokens through Kotlin `Flow`.

## Scope Check

This plan connects the app to a native-capable runtime boundary.

Included:

- `NativeLlamaBridge` Kotlin interface.
- `AndroidLlamaRuntime` implementation of the existing `LlamaRuntime`.
- Fake bridge tests proving model load, unload, and token streaming behavior.
- Build verification.

Deferred:

- Copying or vendoring the full llama.cpp native source.
- CMake/NDK library build.
- PaddleOCR-VL projector/image support.
- Real GGUF model download.
- Device benchmarking.

## File Structure

Create these files:

- `app/src/main/java/com/xtranslate/llama/nativebridge/NativeLlamaBridge.kt`
- `app/src/main/java/com/xtranslate/llama/AndroidLlamaRuntime.kt`
- `app/src/test/java/com/xtranslate/llama/AndroidLlamaRuntimeTest.kt`

---

### Task 1: Add Native Bridge Interface

**Files:**
- Create: `app/src/main/java/com/xtranslate/llama/nativebridge/NativeLlamaBridge.kt`

- [x] **Step 1: Create native bridge contract**

Create `app/src/main/java/com/xtranslate/llama/nativebridge/NativeLlamaBridge.kt`:

```kotlin
package com.xtranslate.llama.nativebridge

import kotlinx.coroutines.flow.Flow

/**
 * Small Kotlin boundary around the native llama.cpp binding.
 *
 * The real implementation will call the official Android llama.cpp binding.
 * Tests can use a fake implementation of this interface.
 */
interface NativeLlamaBridge {
    suspend fun loadModel(
        modelPath: String,
        projectorPath: String?,
    )

    suspend fun unloadModel()

    fun generate(
        prompt: String,
        imageUri: String?,
    ): Flow<String>
}
```

- [x] **Step 2: Run compile check**

Run:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Expected: PASS.

- [x] **Step 3: Commit bridge contract**

```powershell
git add app/src/main/java/com/xtranslate/llama/nativebridge/NativeLlamaBridge.kt
git commit -m "feat: add native llama bridge contract"
```

---

### Task 2: Add AndroidLlamaRuntime

**Files:**
- Create: `app/src/main/java/com/xtranslate/llama/AndroidLlamaRuntime.kt`
- Create: `app/src/test/java/com/xtranslate/llama/AndroidLlamaRuntimeTest.kt`

- [x] **Step 1: Write runtime test**

Create `app/src/test/java/com/xtranslate/llama/AndroidLlamaRuntimeTest.kt`:

```kotlin
package com.xtranslate.llama

import com.xtranslate.llama.nativebridge.NativeLlamaBridge
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests AndroidLlamaRuntime without loading real native code.
 */
class AndroidLlamaRuntimeTest {
    @Test
    fun loadStoresProfileAndCallsNativeBridge() =
        runTest {
            val bridge = FakeNativeLlamaBridge()
            val runtime = AndroidLlamaRuntime(bridge)
            val profile =
                LlamaProfile(
                    id = "translation.multilingual.gguf",
                    kind = LlamaProfileKind.Translation,
                    modelPath = "/models/translator.gguf",
                )

            runtime.load(profile)

            assertEquals(profile, runtime.loadedProfile)
            assertEquals("/models/translator.gguf", bridge.loadedModelPath)
            assertNull(bridge.loadedProjectorPath)
        }

    @Test
    fun generateDelegatesToNativeBridge() =
        runTest {
            val bridge = FakeNativeLlamaBridge(response = listOf("Ka", "musta"))
            val runtime = AndroidLlamaRuntime(bridge)

            val output =
                runtime
                    .generate(LlamaRequest(prompt = "Translate", imageUri = null))
                    .toList()
                    .joinToString("")

            assertEquals("Kamusta", output)
            assertEquals("Translate", bridge.lastPrompt)
        }

    @Test
    fun unloadClearsProfileAndCallsNativeBridge() =
        runTest {
            val bridge = FakeNativeLlamaBridge()
            val runtime = AndroidLlamaRuntime(bridge)
            val profile =
                LlamaProfile(
                    id = "ocr.paddleocr-vl-1_5.q4",
                    kind = LlamaProfileKind.Ocr,
                    modelPath = "/models/ocr.gguf",
                    projectorPath = "/models/ocr-mmproj.gguf",
                )

            runtime.load(profile)
            runtime.unload()

            assertNull(runtime.loadedProfile)
            assertEquals(true, bridge.unloaded)
        }
}

private class FakeNativeLlamaBridge(
    private val response: List<String> = listOf("ok"),
) : NativeLlamaBridge {
    var loadedModelPath: String? = null
    var loadedProjectorPath: String? = null
    var lastPrompt: String? = null
    var unloaded: Boolean = false

    override suspend fun loadModel(
        modelPath: String,
        projectorPath: String?,
    ) {
        loadedModelPath = modelPath
        loadedProjectorPath = projectorPath
        unloaded = false
    }

    override suspend fun unloadModel() {
        unloaded = true
        loadedModelPath = null
        loadedProjectorPath = null
    }

    override fun generate(
        prompt: String,
        imageUri: String?,
    ): Flow<String> =
        flow {
            lastPrompt = prompt
            response.forEach { emit(it) }
        }
}
```

- [x] **Step 2: Run test and verify failure**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.llama.AndroidLlamaRuntimeTest
```

Expected: FAIL because `AndroidLlamaRuntime` does not exist.

- [x] **Step 3: Add AndroidLlamaRuntime**

Create `app/src/main/java/com/xtranslate/llama/AndroidLlamaRuntime.kt`:

```kotlin
package com.xtranslate.llama

import com.xtranslate.llama.nativebridge.NativeLlamaBridge
import kotlinx.coroutines.flow.Flow

/**
 * LlamaRuntime implementation backed by an Android native bridge.
 *
 * This class owns the app-level profile state. The bridge owns the real native
 * llama.cpp session details.
 */
class AndroidLlamaRuntime(
    private val bridge: NativeLlamaBridge,
) : LlamaRuntime {
    private var currentProfile: LlamaProfile? = null

    override val loadedProfile: LlamaProfile?
        get() = currentProfile

    override suspend fun load(profile: LlamaProfile) {
        bridge.loadModel(
            modelPath = profile.modelPath,
            projectorPath = profile.projectorPath,
        )
        currentProfile = profile
    }

    override suspend fun unload() {
        bridge.unloadModel()
        currentProfile = null
    }

    override fun generate(request: LlamaRequest): Flow<String> =
        bridge.generate(
            prompt = request.prompt,
            imageUri = request.imageUri,
        )
}
```

- [x] **Step 4: Run test and verify pass**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.llama.AndroidLlamaRuntimeTest
```

Expected: PASS.

- [x] **Step 5: Commit runtime implementation**

```powershell
git add app/src/main/java/com/xtranslate/llama/AndroidLlamaRuntime.kt app/src/test/java/com/xtranslate/llama/AndroidLlamaRuntimeTest.kt
git commit -m "feat: add android llama runtime"
```

---

### Task 3: Verify Native Binding Prep Slice

**Files:**
- Modify only files needed to fix failures.

- [x] **Step 1: Run llama tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.llama.*
```

Expected: PASS.

- [x] **Step 2: Run all unit tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Expected: PASS.

- [x] **Step 3: Run debug build**

Run:

```powershell
.\gradlew.bat :app:assembleDebug
```

Expected: PASS.

- [x] **Step 4: Commit plan update**

After the plan checklist is updated:

```powershell
git add docs/superpowers/plans/2026-05-29-native-llama-binding.md
git commit -m "docs: mark native llama binding prep complete"
```

---

## Follow-up Plan

The next plan should add the real native implementation of `NativeLlamaBridge` by following the official llama.cpp Android sample. That follow-up should be done separately because it introduces CMake, NDK, native libraries, and likely source vendoring.

## Self-Review Notes

- Spec coverage: this plan connects the existing app-owned `LlamaRuntime` contract to a native bridge boundary.
- Deferred work: real llama.cpp C++/JNI/CMake integration is intentionally separate.
- Red-flag scan: no unfinished markers or open-ended implementation instructions.
- Type consistency: `NativeLlamaBridge`, `AndroidLlamaRuntime`, `LlamaProfile`, and `LlamaRequest` are consistent across tests and implementation.
