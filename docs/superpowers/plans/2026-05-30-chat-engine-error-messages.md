# Chat Engine Error Messages Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Show friendly chat messages when local OCR or translation engines fail.

**Architecture:** Catch exceptions inside `ChatViewModel` text and image workflows. Set `isBusy` back to false and append a `System` chat message with the error message.

**Tech Stack:** Kotlin, coroutines, JUnit 4.

---

## Scope Check

Included:

- Text translation failures show a system message.
- Image translation failures show a system message.
- Busy state is cleared after failures.

Deferred:

- Routing directly to the Models tab from errors.
- Per-model missing file actions.
- Rich error types.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/ui/chat/ChatViewModel.kt`
- Modify: `app/src/test/java/com/xtranslate/ui/chat/ChatViewModelTest.kt`

---

### Task 1: Add Error Handling

- [x] **Step 1: Add failing tests**

Add tests for text and image workflow failures.

- [x] **Step 2: Verify failing tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.ui.chat.ChatViewModelTest
```

Expected: fail because errors are not caught yet.

- [x] **Step 3: Catch workflow errors**

Catch exceptions and append `ChatMessageKind.System`.

- [x] **Step 4: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
