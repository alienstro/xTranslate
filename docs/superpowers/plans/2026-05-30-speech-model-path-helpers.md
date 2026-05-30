# Speech Model Path Helpers Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give Whisper STT and Supertonic TTS stable local file path helpers.

**Architecture:** Add `whisperModelFile()` and `supertonicModelFile()` to `LocalModelPaths`. Use those helpers from `LocalModelImporter` so future real speech engines can reuse the same paths without duplicating filenames.

**Tech Stack:** Kotlin, JUnit 4.

---

## Scope Check

Included:

- Add STT and TTS file path helpers.
- Use the helpers in the model importer.
- Cover paths with unit tests.

Deferred:

- Real Whisper runtime.
- Real Supertonic ONNX runtime.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/model/LocalModelPaths.kt`
- Modify: `app/src/main/java/com/xtranslate/model/LocalModelImporter.kt`
- Modify: `app/src/test/java/com/xtranslate/model/LocalModelPathsTest.kt`

---

### Task 1: Add Speech Path Helpers

- [x] **Step 1: Add failing tests**

Add tests for Whisper and Supertonic local model file paths.

- [x] **Step 2: Verify failing tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.model.LocalModelPathsTest
```

Expected: fail because the helper methods do not exist.

- [x] **Step 3: Add helpers and importer usage**

Add helper methods and use them from `LocalModelImporter`.

- [x] **Step 4: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
