# Local OCR Test Button Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a developer-only Models screen button that calls the local OCR runner.

**Architecture:** Thread an OCR test callback and status string from `MainActivity` to `ModelsScreen`. The button calls `LocalOcrRunner.extractSampleImageText` with a placeholder image URI until the real image picker is built.

**Tech Stack:** Kotlin, Jetpack Compose, llama.cpp wrapper contracts.

---

## Scope Check

Included:

- Add `Run local OCR test` button.
- Show OCR test status.
- Call `LocalOcrRunner`.
- Verify compile/tests/build.

Deferred:

- Real image picker.
- Camera capture.
- OCR review screen.
- Chat integration.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/MainActivity.kt`
- Modify: `app/src/main/java/com/xtranslate/ui/XTranslateApp.kt`
- Modify: `app/src/main/java/com/xtranslate/ui/models/ModelsScreen.kt`

---

### Task 1: Add OCR Test UI Wiring

- [x] **Step 1: Thread callback and status**

Add `onRunLocalOcrTest` and `localOcrTestStatus`.

- [x] **Step 2: Add Models screen button**

Add `Run local OCR test` in the `Image OCR` section.

- [x] **Step 3: Call runner from MainActivity**

Instantiate `LocalOcrRunner` and call it from the callback.

- [x] **Step 4: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
