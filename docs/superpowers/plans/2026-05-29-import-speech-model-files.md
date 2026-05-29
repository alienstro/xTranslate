# Import Speech Model Files Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let the user import Whisper STT and Supertonic 3 TTS model files into app-private storage.

**Architecture:** Extend `LocalModelImporter` with focused speech import methods. Reuse Android document picker launchers in `MainActivity` and expose simple buttons/status text on the Models screen.

**Tech Stack:** Kotlin, Android Storage Access Framework, Jetpack Compose, JUnit 4.

---

## Scope Check

Included:

- Import `whisper.bin` into `files/models/stt/`.
- Import `supertonic-3.onnx` into `files/models/tts/`.
- Show import status for speech model imports.
- Unit test copy logic.

Deferred:

- Whisper runtime integration.
- ONNX Runtime integration.
- Supertonic extra assets beyond the first ONNX placeholder.
- Downloading and checksum validation.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/model/LocalModelImporter.kt`
- Modify: `app/src/test/java/com/xtranslate/model/LocalModelImporterTest.kt`
- Modify: `app/src/main/java/com/xtranslate/ui/models/ModelsScreen.kt`
- Modify: `app/src/main/java/com/xtranslate/ui/XTranslateApp.kt`
- Modify: `app/src/main/java/com/xtranslate/MainActivity.kt`

---

### Task 1: Add Speech Import Copy Logic

- [x] **Step 1: Add importer tests**

Add tests for importing Whisper and Supertonic model files.

- [x] **Step 2: Verify failing tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.model.LocalModelImporterTest
```

Expected: fail because the speech import methods do not exist.

- [x] **Step 3: Add speech import methods**

Add `importWhisperModel(inputStream)` and `importSupertonicModel(inputStream)`.

- [x] **Step 4: Verify importer tests pass**

Run the focused test again. Expected: pass.

---

### Task 2: Add Speech Import Buttons

- [x] **Step 1: Thread speech import callbacks**

Pass `onImportWhisperModel`, `onImportSupertonicModel`, and `speechImportStatus` from `MainActivity` to `XTranslateApp` to `ModelsScreen`.

- [x] **Step 2: Add buttons and status text**

Add `Import Whisper STT` and `Import Supertonic TTS` buttons on the Models screen.

---

### Task 3: Wire Android File Pickers

- [x] **Step 1: Add two document picker launchers**

Use `rememberLauncherForActivityResult(ActivityResultContracts.GetContent())` for Whisper and Supertonic imports.

- [x] **Step 2: Copy selected files**

Open each selected URI with `contentResolver.openInputStream(uri)` and import through `LocalModelImporter`.

- [x] **Step 3: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
