# Official llama.cpp Android Library Integration Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the official llama.cpp Android library as a local project dependency and connect it to `NativeLlamaBridge`.

**Architecture:** The app keeps its own `LlamaRuntime` and `NativeLlamaBridge` contracts. The official llama.cpp Android library is vendored under `third_party/llama.cpp` and included as a Gradle module. A small adapter class calls the official `AiChat.getInferenceEngine(context)` facade and maps it to the app's `NativeLlamaBridge`.

**Tech Stack:** Kotlin, Android Gradle Plugin, Gradle composite/module include, Android NDK/CMake, official `ggml-org/llama.cpp/examples/llama.android/lib`.

---

## Source Reference

Official docs:

- https://github.com/ggml-org/llama.cpp/blob/master/docs/android.md
- https://github.com/ggml-org/llama.cpp/tree/master/examples/llama.android

Important current facts:

- The official docs say to import `examples/llama.android` into Android Studio and build it.
- The official sample exposes an `AiChat` facade and `InferenceEngine`.
- The library loads a GGUF model from an app-private file path.
- Generation is exposed as Kotlin `Flow`.
- The current official library build file uses `minSdk = 33`, `compileSdk = 36`, NDK `29.0.13113456`, and CMake `3.31.6`. This project uses installed Android SDK CMake `4.1.2` unless `3.31.6` is installed separately.

## Scope Check

Included:

- Add llama.cpp as vendored third-party source.
- Include the official Android lib module in Gradle.
- Add dependency aliases needed by the official lib.
- Add `OfficialNativeLlamaBridge` adapter.
- Compile and run tests.

Deferred:

- Model downloader.
- UI model selection.
- PaddleOCR-VL image/projector request support.
- Lowering official lib `minSdk` back to older Android devices.
- Device performance tuning.

## SDK Decision

The app now uses `minSdk = 33` to match the official llama.cpp Android library. A later compatibility plan can investigate safely lowering the minSdk or maintaining a fork if older Android support becomes important.

## File Structure

Create or modify:

- `third_party/llama.cpp/` - official llama.cpp checkout.
- `settings.gradle.kts` - include the official Android lib module.
- `gradle/libs.versions.toml` - add plugin/dependency aliases required by the official lib.
- `app/build.gradle.kts` - depend on the official lib module.
- `app/src/main/java/com/xtranslate/llama/nativebridge/OfficialNativeLlamaBridge.kt`
- `app/src/test/java/com/xtranslate/llama/nativebridge/OfficialNativeLlamaBridgeTest.kt`
- `third_party/llama.cpp/examples/llama.android/lib/build.gradle.kts` - adjusted to use installed Android SDK CMake `4.1.2`.

---

### Task 1: Add llama.cpp Source

**Files:**
- Create: `third_party/llama.cpp/`

- [x] **Step 1: Add llama.cpp as vendored source**

Clone llama.cpp into `third_party/llama.cpp`:

```powershell
git clone https://github.com/ggml-org/llama.cpp.git third_party/llama.cpp
```

If it was added as a submodule first, convert it to normal vendored files:

```powershell
git rm --cached -f .gitmodules third_party/llama.cpp
Remove-Item -LiteralPath .gitmodules -Force
Remove-Item -LiteralPath third_party\llama.cpp\.git -Force
```

- [x] **Step 2: Confirm Android library exists**

Run:

```powershell
Test-Path third_party/llama.cpp/examples/llama.android/lib/build.gradle.kts
```

Expected output:

```text
True
```

---

### Task 2: Include Official Android Lib Module

**Files:**
- Modify: `settings.gradle.kts`
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`

- [x] **Step 1: Update settings.gradle.kts**

Replace `settings.gradle.kts` with:

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

include(":llamaAndroidLib")
project(":llamaAndroidLib").projectDir = file("third_party/llama.cpp/examples/llama.android/lib")
```

- [x] **Step 2: Update version catalog**

In `gradle/libs.versions.toml`, add these aliases if missing:

```toml
[versions]
datastorePreferences = "1.1.1"

[libraries]
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastorePreferences" }

[plugins]
android-library = { id = "com.android.library", version.ref = "agp" }
jetbrains-kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
```

Keep existing entries. Do not duplicate section headers.

- [x] **Step 3: Add app dependency**

In `app/build.gradle.kts`, add:

```kotlin
implementation(project(":llamaAndroidLib"))
```

- [x] **Step 4: Compile**

Run:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Expected: PASS, unless the official library minSdk blocks the app.

- [x] **Step 5: Confirm minSdk**

Confirm `app/build.gradle.kts` uses:

```kotlin
minSdk = 33
```

Then run:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Expected: PASS.

---

### Task 3: Add Official Native Bridge Adapter

**Files:**
- Create: `app/src/main/java/com/xtranslate/llama/nativebridge/OfficialNativeLlamaBridge.kt`

- [x] **Step 1: Add adapter**

Create `app/src/main/java/com/xtranslate/llama/nativebridge/OfficialNativeLlamaBridge.kt`:

```kotlin
package com.xtranslate.llama.nativebridge

import android.content.Context
import com.arm.aichat.AiChat
import com.arm.aichat.InferenceEngine
import kotlinx.coroutines.flow.Flow

/**
 * Native bridge backed by the official llama.cpp Android library.
 *
 * This supports text prompts first. Image/projector support for PaddleOCR-VL is
 * planned separately after the official text runtime is verified on device.
 */
class OfficialNativeLlamaBridge(
    context: Context,
) : NativeLlamaBridge {
    private val engine: InferenceEngine =
        AiChat.getInferenceEngine(context.applicationContext)

    override suspend fun loadModel(
        modelPath: String,
        projectorPath: String?,
    ) {
        require(projectorPath == null) {
            "Projector/image input is not wired yet. Use text-only profiles first."
        }
        engine.loadModel(modelPath)
    }

    override suspend fun unloadModel() {
        engine.cleanUp()
    }

    override fun generate(
        prompt: String,
        imageUri: String?,
    ): Flow<String> {
        require(imageUri == null) {
            "Image input is not wired yet. Use text-only profiles first."
        }
        return engine.sendUserPrompt(prompt)
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

### Task 4: Verify Plan

**Files:**
- Modify only files needed to fix build failures.

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

- [x] **Step 3: Build debug APK**

Run:

```powershell
.\gradlew.bat :app:assembleDebug
```

Expected: PASS.

- [x] **Step 4: Commit once for the full plan**

```powershell
git add settings.gradle.kts gradle/libs.versions.toml app/build.gradle.kts app/src/main/java/com/xtranslate/llama/nativebridge docs/superpowers/plans/2026-05-29-official-llama-android-lib.md
git add third_party/llama.cpp
git commit -m "feat: integrate official llama android library"
```

---

## Follow-up Plan

After text-only native loading builds, create a separate plan for:

- App-private GGUF model placement.
- Runtime loading from the Models screen.
- First on-device text generation test.
- PaddleOCR-VL image/projector support.

## Self-Review Notes

- Spec coverage: this plan begins the real llama.cpp integration while preserving the app-owned runtime contracts.
- Deferred work: image input, downloads, and benchmarking are separate because each can fail independently.
- Red-flag scan: no unfinished markers or open-ended implementation instructions.
- Type consistency: `OfficialNativeLlamaBridge` implements the existing `NativeLlamaBridge` interface.
