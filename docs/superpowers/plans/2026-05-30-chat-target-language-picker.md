# Chat Target Language Picker Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let users choose the chat target language from the chat composer.

**Architecture:** Add a compact dropdown menu in `ChatScreen`. Keep target language state in `ChatViewModel` and pass updates through `XTranslateApp`.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, JUnit 4.

---

## Scope Check

Included:

- Add target language dropdown to chat composer.
- Include a small broad multilingual starter list.
- Unit test selected language is used for translation.

Deferred:

- Full language database.
- Searchable language picker.
- Auto-detect source language UI.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/ui/chat/ChatScreen.kt`
- Modify: `app/src/main/java/com/xtranslate/ui/XTranslateApp.kt`
- Modify: `app/src/test/java/com/xtranslate/ui/chat/ChatViewModelTest.kt`

---

### Task 1: Add Target Language Picker

- [x] **Step 1: Add test for selected target language**

Verify `ChatViewModel.updateTargetLanguage` affects `sendText`.

- [x] **Step 2: Add dropdown UI**

Add a Material 3 dropdown in `ChatScreen`.

- [x] **Step 3: Wire callback**

Pass `chatViewModel::updateTargetLanguage` through `XTranslateApp`.

- [x] **Step 4: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
