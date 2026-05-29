# xTranslate Design

Date: 2026-05-29

## Summary

xTranslate is an Android-first Kotlin translation app for local text, voice, and image translation. The app uses llama.cpp for both image OCR/VLM extraction and broad multilingual text translation. Speech input uses Whisper, and speech output uses Supertonic 3 through ONNX Runtime.

The v1 UI is a mobile-first Claude-like chat experience. Text, voice, and image input all produce translation results inside a conversation feed, while model downloads and runtime status live in a separate Models screen.

## Goals

- Build a Kotlin Android app with a modern mobile chat UI inspired by Claude.
- Use llama.cpp for image-to-text OCR/VLM and text-to-text translation.
- Use PaddleOCR-VL-1.5 Q4 as the image-text-to-text OCR/VLM model profile.
- Use a broad multilingual GGUF model for translation.
- Use lazy loading so OCR, translation, STT, and TTS engines load only when invoked.
- Keep inference local after explicit user-initiated model downloads.
- Store downloaded models in app-private storage so uninstalling the app removes them.

## Non-Goals

- Cloud inference fallback.
- Desktop-first UI.
- Keeping all models loaded at the same time on low-end devices.
- Translating directly onto image overlays in v1.
- Full PDF or document-layout reconstruction in v1.
- Using Paddle/Python services for OCR in the Android app.

## Architecture

The app should be organized around small runtime interfaces and a central engine coordinator.

```text
app
domain
model-manager
llama-runtime
speech-runtime
```

Runtime ownership:

```text
llama.cpp
  - PaddleOCR-VL-1.5 Q4 image-text-to-text OCR/VLM profile
  - broad multilingual translation GGUF profile

whisper.cpp
  - speech-to-text

ONNX Runtime
  - Supertonic 3 text-to-speech
```

Core interfaces:

```kotlin
interface OcrEngine {
    suspend fun extractText(image: ImageInput): OcrResult
}

interface TranslationEngine {
    suspend fun translate(request: TranslationRequest): TranslationResult
}

interface SpeechToTextEngine {
    suspend fun transcribe(audio: AudioInput): Transcript
}

interface TextToSpeechEngine {
    suspend fun synthesize(request: SpeechRequest): AudioOutput
}
```

`EngineCoordinator` owns lazy loading, unloading, and runtime reuse. It should keep the UI and workflow code from directly managing native runtime lifecycles.

## llama.cpp Profiles

The app uses two separate llama.cpp model profiles.

### OCR/VLM Profile

- Model: PaddleOCR-VL-1.5 Q4 GGUF.
- Companion files: vision/projector files required by the model.
- Input: Android image or camera capture.
- Output: extracted source text or markdown-like structured text.
- Purpose: image-text-to-text OCR/document parsing.

### Translation Profile

- Model: broad multilingual GGUF translation/chat model.
- Input: source text, source language if known, target language.
- Output: translated text.
- Purpose: text-to-text translation for typed text, OCR text, and STT transcripts.

Low-end devices should load these profiles sequentially during image translation:

```text
load OCR VLM
extract text
unload OCR VLM if memory is tight
load translator
translate extracted text
```

## User Experience

The primary UI is a mobile-first Claude-like chat shell.

Main screen:

- Warm neutral color palette.
- Conversation feed for translation history.
- Bottom composer with text input, mic button, image/camera button, target language selector, and send button.
- Translation messages show source content, translated content, copy/share actions, and optional play-audio action.

Image flow:

```text
tap image/camera
choose photo or capture image
llama.cpp PaddleOCR-VL extracts text
show OCR review/edit screen
translate reviewed text
append translated result to chat
```

Voice flow:

```text
tap mic
Whisper transcribes audio
editable transcript appears in composer
user sends transcript
llama.cpp translates
Supertonic 3 speaks result only if playback is requested
```

Models screen:

- Shows installed, missing, downloading, and failed model states.
- Separates model packs for OCR, translation, STT, and TTS.
- Provides explicit download buttons.
- Shows that engines are loaded only when used.

## Lazy Loading And Memory Policy

Only engines needed for the current workflow are loaded.

Text translation:

```text
load translator only
```

Image translation:

```text
load OCR VLM
extract text
unload OCR VLM if low memory
load translator
translate
```

Voice translation:

```text
load Whisper
transcribe
unload Whisper if low memory
load translator
translate
load TTS only if playback requested
```

The coordinator should unload idle engines after a timeout and immediately respond to Android memory pressure callbacks. It should avoid keeping OCR VLM and translation loaded together on low-RAM devices unless benchmarking proves it is acceptable.

## Model Management

All model files live in app-private storage.

Model registry entries should include:

- Stable model id.
- Display name.
- Engine type.
- Required files.
- Download URL or manual resolve-link slot.
- Expected size.
- Optional checksum.
- Installed state.
- Minimum recommended RAM tier.

Initial model packs:

- OCR: PaddleOCR-VL-1.5 Q4 GGUF plus required projector files.
- Translation: broad multilingual GGUF.
- STT: Whisper model pack.
- TTS: Supertonic 3 ONNX assets.

Hugging Face downloads should use direct resolve URLs for file content, not web UI blob URLs.

## Error Handling

Missing model:

- Show the missing dependency and route the user to the Models screen.

Not enough memory:

- Unload idle engines and retry once.
- If still failing, show a lighter-model recommendation.

OCR empty result:

- Preserve the image.
- Let the user crop, retake, or manually enter text.

Translation failure:

- Preserve the source text or OCR result.
- Allow retry after changing language/model settings.

STT failure:

- Preserve the recording during the active flow.
- Let the user retry or manually edit the transcript.

TTS failure:

- Keep translated text usable.
- Show playback failure without blocking translation.

## Testing

Automated tests:

- Unit test `EngineCoordinator` lifecycle decisions.
- Unit test model registry and install-state transitions.
- Unit test OCR and translation prompt construction.
- Integration test mocked image input through OCR text and translation result.
- UI tests for text, image, and voice translation flows.

Manual validation:

- Test on one low-RAM Android phone and one modern phone.
- Confirm image OCR can unload before translation.
- Confirm missing model states route cleanly to the Models screen.
- Confirm downloaded models are removed when the app is uninstalled.

## Approved Decisions

- Build Android-first in Kotlin.
- Use llama.cpp for both OCR/VLM and translation.
- Use PaddleOCR-VL-1.5 Q4 as the OCR/VLM model profile.
- Use broad multilingual translation for v1.
- Use a mobile-first Claude-like chat UI.
- Use Whisper for STT.
- Use Supertonic 3 through ONNX Runtime for TTS.
- Use lazy loading for all heavyweight engines.
