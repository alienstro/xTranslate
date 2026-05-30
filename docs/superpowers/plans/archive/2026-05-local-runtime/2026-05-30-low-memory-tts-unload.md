# Low Memory TTS Unload Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Keep TTS lazy-loading friendly for low-end Android phones.

**Architecture:** `EngineCoordinator.speak(...)` should load the TTS engine only while speech is generated, then unload it again when `lowMemoryMode` is enabled.

**Tech Stack:** Kotlin, coroutines, JUnit 4.

---

## Scope Check

Included:

- TTS unloads after speech generation in low-memory mode.
- TTS can remain loaded when low-memory mode is disabled.

Deferred:

- Real Supertonic ONNX loading.
- Audio playback lifecycle.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/runtime/EngineCoordinator.kt`
- Modify: `app/src/test/java/com/xtranslate/runtime/EngineCoordinatorTest.kt`

---

### Task 1: Update TTS Lazy Loading

- [x] **Step 1: Add tests**

Test low-memory unload and non-low-memory retention.

- [x] **Step 2: Verify failing tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.runtime.EngineCoordinatorTest
```

Expected: fail because TTS stays loaded in low-memory mode.

- [x] **Step 3: Update coordinator**

Unload `LoadedEngine.Tts` after synthesis when `lowMemoryMode` is enabled.

- [x] **Step 4: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
