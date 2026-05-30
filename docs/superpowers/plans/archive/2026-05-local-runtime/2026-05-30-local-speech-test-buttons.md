# Local Speech Test Buttons Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let the Models screen run quick local STT and TTS smoke tests.

**Architecture:** Add `LocalSpeechTestRunner` that uses the file-backed speech placeholder engines. Add Models-screen buttons that call the runner and show a speech test status.

**Tech Stack:** Kotlin, Jetpack Compose, JUnit 4.

---

## Scope Check

Included:

- Run local STT test button.
- Run local TTS test button.
- Missing Whisper/Supertonic files produce clear status errors.

Deferred:

- Real microphone input.
- Real audio playback.
- Real Whisper/Supertonic inference.

## File Structure

- Create: `app/src/main/java/com/xtranslate/runtime/LocalSpeechTestRunner.kt`
- Create: `app/src/test/java/com/xtranslate/runtime/LocalSpeechTestRunnerTest.kt`
- Modify: `app/src/main/java/com/xtranslate/MainActivity.kt`
- Modify: `app/src/main/java/com/xtranslate/ui/XTranslateApp.kt`
- Modify: `app/src/main/java/com/xtranslate/ui/models/ModelsScreen.kt`

---

### Task 1: Add Local Speech Tests

- [x] **Step 1: Add failing runner tests**

Test STT/TTS success and missing-file errors.

- [x] **Step 2: Verify failing tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.runtime.LocalSpeechTestRunnerTest
```

Expected: fail because the runner does not exist.

- [x] **Step 3: Add runner**

Create `LocalSpeechTestRunner`.

- [x] **Step 4: Wire Models screen buttons**

Thread callbacks and status through `MainActivity`, `XTranslateApp`, and `ModelsScreen`.

- [x] **Step 5: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
