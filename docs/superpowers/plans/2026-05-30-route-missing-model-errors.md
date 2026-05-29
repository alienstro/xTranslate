# Route Missing Model Errors Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Send the user to the Models tab when chat fails because a local model file is missing.

**Architecture:** Keep chat error handling inside `ChatViewModel`. When an error message mentions a missing model file, append the system message, clear busy state, and select the Models tab.

**Tech Stack:** Kotlin, coroutines, JUnit 4.

---

## Scope Check

Included:

- Route missing translation model errors to Models tab.
- Route missing OCR model/projector errors to Models tab.
- Keep non-model errors on the current tab.

Deferred:

- Inline action buttons inside chat messages.
- Per-model import deep links.
- Rich typed error classes.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/ui/chat/ChatViewModel.kt`
- Modify: `app/src/test/java/com/xtranslate/ui/chat/ChatViewModelTest.kt`

---

### Task 1: Route Missing Model Errors

- [x] **Step 1: Add failing tests**

Test that missing model errors select `AppTab.Models`.

- [x] **Step 2: Verify failing tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.ui.chat.ChatViewModelTest
```

Expected: fail because errors do not route tabs yet.

- [x] **Step 3: Update error handling**

Update `showError` to select `AppTab.Models` for missing model file messages.

- [x] **Step 4: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
