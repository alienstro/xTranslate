# Import Translation Model Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let the user import a local translation GGUF file into app-private storage from the Models screen.

**Architecture:** Add a small file importer that copies an input stream into `LocalModelPaths.translationModelFile()`. Wire Android's document picker in `MainActivity`, then expose a simple import button and status text in the Models screen.

**Tech Stack:** Kotlin, Android Storage Access Framework, Jetpack Compose, JUnit 4.

---

## Scope Check

Included:

- Import one broad multilingual translation GGUF file.
- Copy the selected file into app-private storage.
- Show import success/failure status.
- Unit test the copy logic.

Deferred:

- OCR/projector import.
- Whisper and TTS import.
- Downloading from Hugging Face.
- Checksum validation.
- Progress bar for large files.

## File Structure

- Create: `app/src/main/java/com/xtranslate/model/LocalModelImporter.kt`
- Create: `app/src/test/java/com/xtranslate/model/LocalModelImporterTest.kt`
- Modify: `app/src/main/java/com/xtranslate/ui/models/ModelsScreen.kt`
- Modify: `app/src/main/java/com/xtranslate/ui/XTranslateApp.kt`
- Modify: `app/src/main/java/com/xtranslate/MainActivity.kt`

---

### Task 1: Add Import Copy Logic

- [x] **Step 1: Write importer test**

Create a unit test that copies bytes from an input stream into the translation model file.

- [x] **Step 2: Verify failing test**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.model.LocalModelImporterTest
```

Expected: fail because `LocalModelImporter` does not exist.

- [x] **Step 3: Add importer**

Create `LocalModelImporter` with `importTranslationModel(inputStream)` and make it create parent directories before copying.

- [x] **Step 4: Verify importer test passes**

Run the focused test again. Expected: pass.

---

### Task 2: Add Import Button To Models Screen

- [x] **Step 1: Thread import callbacks**

Pass `onImportTranslationModel` and `importStatus` from `MainActivity` to `XTranslateApp` to `ModelsScreen`.

- [x] **Step 2: Add button and status text**

Add `Import translation GGUF` button under the local text test button.

---

### Task 3: Wire Android File Picker

- [x] **Step 1: Add document picker launcher**

Use `rememberLauncherForActivityResult(ActivityResultContracts.GetContent())`.

- [x] **Step 2: Copy selected file**

Open the selected URI with `contentResolver.openInputStream(uri)` and import through `LocalModelImporter`.

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
git add app/src/main/java/com/xtranslate/model/LocalModelImporter.kt app/src/test/java/com/xtranslate/model/LocalModelImporterTest.kt app/src/main/java/com/xtranslate/ui/models/ModelsScreen.kt app/src/main/java/com/xtranslate/ui/XTranslateApp.kt app/src/main/java/com/xtranslate/MainActivity.kt docs/superpowers/plans/2026-05-29-import-translation-model.md
git commit -m "feat: import translation model file"
```
