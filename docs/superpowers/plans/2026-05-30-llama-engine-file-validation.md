# Llama Engine File Validation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Validate local model files before llama engines try to load them.

**Architecture:** Add file existence checks inside `LlamaTranslationEngine` and `LlamaOcrEngine`. This keeps chat and developer flows from reaching native llama.cpp with missing paths.

**Tech Stack:** Kotlin, JUnit 4, llama runtime contracts.

---

## Scope Check

Included:

- Translation engine validates model file exists.
- OCR engine validates model file exists.
- OCR engine validates projector file exists.
- Unit tests for missing-file errors.

Deferred:

- Checksum validation.
- File size minimum checks.
- Routing errors to Models tab.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/llama/LlamaTranslationEngine.kt`
- Modify: `app/src/main/java/com/xtranslate/llama/LlamaOcrEngine.kt`
- Modify: `app/src/test/java/com/xtranslate/llama/LlamaTranslationEngineTest.kt`
- Modify: `app/src/test/java/com/xtranslate/llama/LlamaOcrEngineTest.kt`

---

### Task 1: Add File Validation

- [x] **Step 1: Add failing tests**

Add missing model/projector tests.

- [x] **Step 2: Verify failing tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.llama.LlamaTranslationEngineTest --tests com.xtranslate.llama.LlamaOcrEngineTest
```

Expected: fail because engines do not validate files yet.

- [x] **Step 3: Add validation**

Use `File(profile.modelPath).exists()` and projector checks before `runtime.load(profile)`.

- [x] **Step 4: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
