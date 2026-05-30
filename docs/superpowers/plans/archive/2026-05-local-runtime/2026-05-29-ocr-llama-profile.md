# OCR Llama Profile Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create a local llama.cpp profile for PaddleOCR-VL using the OCR GGUF file and projector file.

**Architecture:** Extend `LlamaProfileFactory` with an OCR profile constructor. Use explicit `File` parameters so runtime code can validate missing files before profile creation.

**Tech Stack:** Kotlin, JUnit 4, llama.cpp wrapper contracts.

---

## Scope Check

Included:

- Build `LlamaProfileKind.Ocr` profile.
- Set OCR model path.
- Set OCR projector path.
- Unit test profile construction.

Deferred:

- Running OCR on an image.
- Image picker/camera.
- OCR review screen.
- Device validation with real PaddleOCR-VL files.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/llama/LlamaProfileFactory.kt`
- Modify: `app/src/test/java/com/xtranslate/llama/LlamaProfileFactoryTest.kt`

---

### Task 1: Add OCR Profile Factory

- [x] **Step 1: Add failing OCR profile test**

Add a test for `LlamaProfileFactory.ocrProfile(modelFile, projectorFile)`.

- [x] **Step 2: Verify failing test**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.llama.LlamaProfileFactoryTest
```

Expected: fail because `ocrProfile` does not exist.

- [x] **Step 3: Add OCR profile factory**

Add `ocrProfile(modelFile: File, projectorFile: File): LlamaProfile`.

- [x] **Step 4: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
