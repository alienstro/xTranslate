# Chat Voice Placeholder Flow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the chat MIC button run the speech-to-text workflow placeholder instead of only writing placeholder text.

**Architecture:** Add `ChatViewModel.transcribeVoicePlaceholder()`. It calls `EngineCoordinator.transcribe` with a placeholder audio URI and places the transcript into the composer so the user can edit/send it.

**Tech Stack:** Kotlin, coroutines, JUnit 4, Jetpack Compose.

---

## Scope Check

Included:

- MIC button calls a ViewModel voice placeholder flow.
- Fake STT transcript lands in the composer.
- Errors show as system messages.

Deferred:

- Real microphone recording.
- Runtime permission flow.
- Whisper runtime integration.
- Automatic send after transcription.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/ui/chat/ChatViewModel.kt`
- Modify: `app/src/main/java/com/xtranslate/ui/XTranslateApp.kt`
- Modify: `app/src/test/java/com/xtranslate/ui/chat/ChatViewModelTest.kt`

---

### Task 1: Add Voice Placeholder Flow

- [x] **Step 1: Add tests**

Test successful placeholder transcription and failure handling.

- [x] **Step 2: Verify failing tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.ui.chat.ChatViewModelTest
```

Expected: fail because the voice method does not exist.

- [x] **Step 3: Add ViewModel method**

Add `transcribeVoicePlaceholder()`.

- [x] **Step 4: Wire MIC button**

Call `chatViewModel.transcribeVoicePlaceholder()` from `XTranslateApp`.

- [x] **Step 5: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
