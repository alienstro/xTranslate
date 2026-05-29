# Chat Message Labels Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Show friendly labels for chat messages instead of enum names.

**Architecture:** Add a small formatter for `ChatMessageKind` labels and use it in `ChatScreen`.

**Tech Stack:** Kotlin, Jetpack Compose, JUnit 4.

---

## Scope Check

Included:

- Source messages show `Source`.
- OCR messages show `Detected text`.
- Translation messages show `Translation`.
- System messages show `Notice`.

Deferred:

- Copy/share actions.
- Message timestamps.
- Rich OCR review controls.

## File Structure

- Create: `app/src/main/java/com/xtranslate/ui/chat/ChatMessageLabels.kt`
- Create: `app/src/test/java/com/xtranslate/ui/chat/ChatMessageLabelsTest.kt`
- Modify: `app/src/main/java/com/xtranslate/ui/chat/ChatScreen.kt`

---

### Task 1: Add Message Labels

- [x] **Step 1: Add label formatter test**

Test every `ChatMessageKind` maps to a user-facing label.

- [x] **Step 2: Verify failing test**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.ui.chat.ChatMessageLabelsTest
```

Expected: fail because the formatter does not exist.

- [x] **Step 3: Add formatter and use it**

Add `ChatMessageLabels.labelFor(kind)` and use it in `ChatMessageCard`.

- [x] **Step 4: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
