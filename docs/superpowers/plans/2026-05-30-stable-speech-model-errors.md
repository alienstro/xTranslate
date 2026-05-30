# Stable Speech Model Errors Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make missing speech model errors stable across Windows tests and Android devices.

**Architecture:** Normalize model paths in speech placeholder error messages to forward slashes. Android already uses forward slashes, and tests on Windows should match the same format.

**Tech Stack:** Kotlin, JUnit 4.

---

## Scope Check

Included:

- Whisper missing-file message uses `/`.
- Supertonic missing-file message uses `/`.

Deferred:

- User-facing copy refinement.
- Localized error messages.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/runtime/FileBackedSpeechEngines.kt`
- Modify: `app/src/test/java/com/xtranslate/runtime/FileBackedSpeechEnginesTest.kt`

---

### Task 1: Normalize Speech Missing-File Errors

- [x] **Step 1: Add failing tests**

Update tests to expect forward-slash paths.

- [x] **Step 2: Verify failing tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.runtime.FileBackedSpeechEnginesTest
```

Expected: fail because current messages use platform-specific paths.

- [x] **Step 3: Normalize error paths**

Replace backslashes with forward slashes in error messages.

- [x] **Step 4: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
