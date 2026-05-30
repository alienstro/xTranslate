# Chat TTS Placeholder Flow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let the user trigger the text-to-speech path from a translated chat message.

**Architecture:** Add a ViewModel method that calls `EngineCoordinator.speak(...)` for translated text. Until real playback is added, the app shows a short system notice with the generated audio URI.

**Tech Stack:** Kotlin, coroutines, JUnit 4, Jetpack Compose.

---

## Scope Check

Included:

- Translation messages show a Speak action.
- Speak calls the existing TTS engine path.
- Success and failure are visible in chat.

Deferred:

- Real Android audio playback.
- ONNX Runtime Supertonic execution.
- Voice selection and speed controls.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/ui/chat/ChatViewModel.kt`
- Modify: `app/src/main/java/com/xtranslate/ui/chat/ChatScreen.kt`
- Modify: `app/src/test/java/com/xtranslate/ui/chat/ChatViewModelTest.kt`

---

### Task 1: Add TTS Placeholder Flow

- [x] **Step 1: Add tests**

Test successful TTS placeholder output and failure handling.

- [x] **Step 2: Verify failing tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.ui.chat.ChatViewModelTest
```

Expected: fail because the ViewModel method does not exist.

- [x] **Step 3: Add ViewModel method**

Add `speakTranslationPlaceholder(message)`.

- [x] **Step 4: Wire Speak action**

Show a Speak button on translation messages and call the ViewModel method.

- [x] **Step 5: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
