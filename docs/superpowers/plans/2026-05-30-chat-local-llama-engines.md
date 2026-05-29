# Chat Local Llama Engines Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Use local llama OCR and translation engines in the chat image/text workflows.

**Architecture:** Build `AndroidLlamaRuntime` instances in `MainActivity`, create OCR and translation profiles from app-private model paths, and pass `LlamaOcrEngine`/`LlamaTranslationEngine` into `EngineCoordinator`. Keep STT and TTS fake for this slice.

**Tech Stack:** Kotlin, llama.cpp wrapper contracts, Jetpack Compose.

---

## Scope Check

Included:

- Chat text translation uses `LlamaTranslationEngine`.
- Chat image translation uses `LlamaOcrEngine` then `LlamaTranslationEngine`.
- STT and TTS remain fake.

Deferred:

- Friendly missing-model UI in chat.
- Real STT/TTS runtime wiring.
- Runtime reuse optimization.
- Device testing with real GGUF files.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/MainActivity.kt`

---

### Task 1: Wire Local Llama Engines Into Coordinator

- [x] **Step 1: Build model profiles**

Create translation and OCR llama profiles from `LocalModelPaths`.

- [x] **Step 2: Build llama engines**

Create `LlamaTranslationEngine` and `LlamaOcrEngine`.

- [x] **Step 3: Use llama engines in coordinator**

Replace fake OCR/translation engines in `EngineCoordinator`.

- [x] **Step 4: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
