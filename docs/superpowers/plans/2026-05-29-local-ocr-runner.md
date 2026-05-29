# Local OCR Runner Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a developer-only runner that can call the local PaddleOCR-VL llama profile for one image URI.

**Architecture:** Add `LocalOcrRunner`, parallel to `LocalTextGenerationRunner`. It checks that both OCR files exist, builds an OCR llama profile, then calls `LlamaOcrEngine`.

**Tech Stack:** Kotlin, llama.cpp wrapper contracts, JUnit 4.

---

## Scope Check

Included:

- Validate OCR model GGUF exists.
- Validate OCR projector GGUF exists.
- Build OCR profile from local files.
- Run OCR against a provided image URI.
- Unit test missing-file validation.

Deferred:

- Image picker/camera.
- OCR review screen.
- Real-device PaddleOCR-VL validation.
- Chat flow integration.

## File Structure

- Create: `app/src/main/java/com/xtranslate/llama/LocalOcrRunner.kt`
- Create: `app/src/test/java/com/xtranslate/llama/LocalOcrRunnerTest.kt`

---

### Task 1: Add Local OCR Runner

- [x] **Step 1: Add failing missing-file tests**

Test that missing OCR model/projector files produce useful errors.

- [x] **Step 2: Verify failing test**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.llama.LocalOcrRunnerTest
```

Expected: fail because `LocalOcrRunner` does not exist.

- [x] **Step 3: Add runner**

Create `LocalOcrRunner` with `extractSampleImageText(imageUri: String)`.

- [x] **Step 4: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
