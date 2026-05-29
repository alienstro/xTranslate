# Local OCR Image Picker Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let the developer OCR test button pick a real image and pass its URI to the local OCR runner.

**Architecture:** Reuse Android's document picker with `ActivityResultContracts.GetContent()`. The Models screen keeps one `Run local OCR test` button; `MainActivity` opens the image picker, then calls `LocalOcrRunner.extractSampleImageText(uri.toString())`.

**Tech Stack:** Kotlin, Android Storage Access Framework, Jetpack Compose.

---

## Scope Check

Included:

- Pick an image from the device.
- Pass the selected image URI into `LocalOcrRunner`.
- Show status for cancelled, running, result, or error.

Deferred:

- Camera capture.
- Chat integration.
- OCR review/edit screen.
- Persisting selected image permissions.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/MainActivity.kt`
- Modify: `docs/superpowers/plans/2026-05-29-local-ocr-test-button.md`

---

### Task 1: Replace Placeholder OCR URI

- [x] **Step 1: Add image picker launcher**

Add `rememberLauncherForActivityResult(ActivityResultContracts.GetContent())` for images.

- [x] **Step 2: Run OCR from selected image**

Call `LocalOcrRunner.extractSampleImageText(uri.toString())`.

- [x] **Step 3: Update button callback**

Make `onRunLocalOcrTest` launch the image picker with `image/*`.

- [x] **Step 4: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
