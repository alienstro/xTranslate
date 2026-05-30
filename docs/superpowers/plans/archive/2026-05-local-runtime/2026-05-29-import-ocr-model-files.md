# Import OCR Model Files Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let the user import the PaddleOCR-VL OCR GGUF file and its projector file into app-private storage.

**Architecture:** Extend `LocalModelImporter` with focused OCR import methods. Reuse Android's document picker flow in `MainActivity`, but keep separate buttons and status text so the user knows which OCR file they are importing.

**Tech Stack:** Kotlin, Android Storage Access Framework, Jetpack Compose, JUnit 4.

---

## Scope Check

Included:

- Import `paddleocr-vl-1.5-q4.gguf`.
- Import `paddleocr-vl-1.5-mmproj.gguf`.
- Copy both files into `files/models/ocr/`.
- Show import status for each selected OCR file.
- Unit test the copy logic.

Deferred:

- Running image OCR.
- Image picker/camera.
- Checksum validation.
- Downloading from Hugging Face.
- Importing Whisper or Supertonic files.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/model/LocalModelImporter.kt`
- Modify: `app/src/test/java/com/xtranslate/model/LocalModelImporterTest.kt`
- Modify: `app/src/main/java/com/xtranslate/ui/models/ModelsScreen.kt`
- Modify: `app/src/main/java/com/xtranslate/ui/XTranslateApp.kt`
- Modify: `app/src/main/java/com/xtranslate/MainActivity.kt`

---

### Task 1: Add OCR Import Copy Logic

- [x] **Step 1: Add importer tests**

Add tests for importing the OCR model file and the OCR projector file.

- [x] **Step 2: Verify failing tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.model.LocalModelImporterTest
```

Expected: fail because the OCR import methods do not exist.

- [x] **Step 3: Add OCR import methods**

Add `importOcrModel(inputStream)` and `importOcrProjector(inputStream)`.

- [x] **Step 4: Verify importer tests pass**

Run the focused test again. Expected: pass.

---

### Task 2: Add OCR Import Buttons

- [x] **Step 1: Thread OCR import callbacks**

Pass `onImportOcrModel`, `onImportOcrProjector`, and `ocrImportStatus` from `MainActivity` to `XTranslateApp` to `ModelsScreen`.

- [x] **Step 2: Add buttons and status text**

Add `Import OCR GGUF` and `Import OCR projector` buttons on the Models screen.

---

### Task 3: Wire Android File Pickers

- [x] **Step 1: Add two document picker launchers**

Use `rememberLauncherForActivityResult(ActivityResultContracts.GetContent())` for OCR model and projector imports.

- [x] **Step 2: Copy selected files**

Open each selected URI with `contentResolver.openInputStream(uri)` and import through `LocalModelImporter`.

- [x] **Step 3: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.

---

## Commit

Commit once for this full plan:

```powershell
git add app/src/main/java/com/xtranslate/model/LocalModelImporter.kt app/src/test/java/com/xtranslate/model/LocalModelImporterTest.kt app/src/main/java/com/xtranslate/ui/models/ModelsScreen.kt app/src/main/java/com/xtranslate/ui/XTranslateApp.kt app/src/main/java/com/xtranslate/MainActivity.kt docs/superpowers/plans/2026-05-29-import-ocr-model-files.md
git commit -m "feat: import ocr model files"
```
