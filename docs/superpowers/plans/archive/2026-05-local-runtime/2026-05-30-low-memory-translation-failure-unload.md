# Low Memory Translation Failure Unload Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Do not leave translation marked as loaded when text translation fails.

**Architecture:** Keep successful text translation loaded for reuse, but unload `LoadedEngine.Translation` when the translation engine throws in low-memory mode.

**Tech Stack:** Kotlin, coroutines, JUnit 4.

---

## Scope Check

Included:

- Failed text translation unloads translation in low-memory mode.
- Successful text translation remains loaded.

Deferred:

- Full native model unload hooks.
- Retry behavior after failed translation.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/runtime/EngineCoordinator.kt`
- Modify: `app/src/test/java/com/xtranslate/runtime/EngineCoordinatorTest.kt`

---

### Task 1: Add Failed Translation Cleanup

- [x] **Step 1: Add failing test**

Add a test for failed text translation cleanup.

- [x] **Step 2: Verify failing test**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.runtime.EngineCoordinatorTest
```

Expected: fail because failed translation stays loaded.

- [x] **Step 3: Update text translation**

Unload translation on failure in low-memory mode.

- [x] **Step 4: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
