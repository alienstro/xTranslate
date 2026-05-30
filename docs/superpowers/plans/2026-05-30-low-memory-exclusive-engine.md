# Low Memory Exclusive Engine Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make low-memory mode keep only the current engine loaded.

**Architecture:** Change `EngineCoordinator.load(...)` so low-memory mode clears previously loaded engines before loading the next one. This keeps text, OCR, STT, and TTS from piling up on low-end Android phones.

**Tech Stack:** Kotlin, coroutines, JUnit 4.

---

## Scope Check

Included:

- Starting STT after translation unloads translation in low-memory mode.
- Starting TTS after translation unloads translation in low-memory mode.
- Normal mode can keep multiple engines loaded.

Deferred:

- Fine-grained cache policy.
- Device-specific RAM detection.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/runtime/EngineCoordinator.kt`
- Modify: `app/src/test/java/com/xtranslate/runtime/EngineCoordinatorTest.kt`

---

### Task 1: Add Exclusive Low-Memory Loading

- [x] **Step 1: Add failing tests**

Test that low-memory mode clears previous engines before STT and TTS.

- [x] **Step 2: Verify failing tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.runtime.EngineCoordinatorTest
```

Expected: fail because translation remains loaded.

- [x] **Step 3: Update load rule**

Clear previous loaded engines inside `load(...)` when low-memory mode is enabled.

- [x] **Step 4: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
