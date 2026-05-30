# Low Memory Failure Unload Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Keep failed OCR, STT, and TTS calls from leaving large engines marked as loaded on low-end phones.

**Architecture:** Use `try/finally` around low-memory engines that should be temporary. OCR, STT, and TTS unload even when their engine throws. Translation can remain loaded because text translation is the main active engine.

**Tech Stack:** Kotlin, coroutines, JUnit 4.

---

## Scope Check

Included:

- OCR unloads after failed image extraction in low-memory mode.
- STT unloads after failed transcription in low-memory mode.
- TTS unloads after failed synthesis in low-memory mode.

Deferred:

- Translation unload policy.
- Retry/backoff behavior.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/runtime/EngineCoordinator.kt`
- Modify: `app/src/test/java/com/xtranslate/runtime/EngineCoordinatorTest.kt`

---

### Task 1: Add Failure Cleanup

- [x] **Step 1: Add failing tests**

Add tests for OCR, STT, and TTS failure cleanup.

- [x] **Step 2: Verify failing tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.runtime.EngineCoordinatorTest
```

Expected: fail because failed engines stay loaded.

- [x] **Step 3: Add try/finally cleanup**

Wrap temporary low-memory engine calls so unload always runs.

- [x] **Step 4: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
