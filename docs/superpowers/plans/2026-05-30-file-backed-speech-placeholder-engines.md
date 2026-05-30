# File Backed Speech Placeholder Engines Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the current speech placeholders respect imported Whisper and Supertonic model files.

**Architecture:** Add lightweight wrapper engines that check the expected local model file before delegating to the current fake STT/TTS engines. This keeps the app behavior honest now and gives the future real runtimes a clear replacement point.

**Tech Stack:** Kotlin, JUnit 4.

---

## Scope Check

Included:

- STT placeholder throws when `whisper.bin` is missing.
- TTS placeholder throws when `supertonic-3.onnx` is missing.
- Main app wires these file-backed wrappers instead of raw fake speech engines.

Deferred:

- Real Whisper inference.
- Real Supertonic ONNX inference.
- Audio recording and playback.

## File Structure

- Create: `app/src/main/java/com/xtranslate/runtime/FileBackedSpeechEngines.kt`
- Create: `app/src/test/java/com/xtranslate/runtime/FileBackedSpeechEnginesTest.kt`
- Modify: `app/src/main/java/com/xtranslate/MainActivity.kt`

---

### Task 1: Add File Backed Speech Placeholders

- [x] **Step 1: Add failing tests**

Test missing-file errors and delegate success for STT and TTS.

- [x] **Step 2: Verify failing tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.runtime.FileBackedSpeechEnginesTest
```

Expected: fail because wrapper engines do not exist.

- [x] **Step 3: Add wrapper engines**

Create file-backed STT and TTS wrappers.

- [x] **Step 4: Wire MainActivity**

Use the wrappers around fake speech engines.

- [x] **Step 5: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
