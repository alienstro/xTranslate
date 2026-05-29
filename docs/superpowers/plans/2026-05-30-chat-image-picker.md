# Chat Image Picker Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the chat `IMG` button pick a real image and run the existing image translation flow.

**Architecture:** Add an image picker launcher in `MainActivity`. Pass an `onPickImage` callback into `XTranslateApp`, then into `ChatScreen`. When an image is selected, call `ChatViewModel.translateImage(ImageInput(uri.toString()))`.

**Tech Stack:** Kotlin, Android Storage Access Framework, Jetpack Compose, existing chat workflow.

---

## Scope Check

Included:

- Open image picker from chat `IMG`.
- Pass selected image URI into `ChatViewModel.translateImage`.
- Keep existing fake OCR/translation engines for this slice.

Deferred:

- Replacing fake OCR with local llama OCR in chat.
- OCR review/edit screen.
- Camera capture.
- Persisting selected image permissions.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/MainActivity.kt`
- Modify: `app/src/main/java/com/xtranslate/ui/XTranslateApp.kt`

---

### Task 1: Wire Chat Image Picker

- [x] **Step 1: Add app-level image callback**

Add `onPickImage` to `XTranslateApp`.

- [x] **Step 2: Use callback in Chat tab**

Pass `onPickImage` to `ChatScreen.onImage`.

- [x] **Step 3: Add MainActivity picker**

Use `rememberLauncherForActivityResult(ActivityResultContracts.GetContent())` with `image/*`.

- [x] **Step 4: Call chat image flow**

Call `chatViewModel.translateImage(ImageInput(uri.toString()))` when an image is selected.

- [x] **Step 5: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
