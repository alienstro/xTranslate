# xTranslate

## What This Is

xTranslate is an Android-first Kotlin translation app for offline text, voice, and camera/image translation. It is built for low-end Android phones, with local inference and lazy-loaded model engines so the app only loads translation, OCR, STT, or TTS when that capability is invoked.

The v1 app uses a tabbed interface: a Chat tab for text, voice, and camera translation flows, and a Models tab for managing local model dependencies. Image translation in v1 is OCR-first: the app extracts visible text from photos or screenshots, then translates the extracted text instead of overlaying translations back onto the image.

## Core Value

Users can translate text, speech, and text found in images fully offline on low-end Android phones after downloading the required models.

## Requirements

### Validated

(None yet - ship to validate)

### Active

- [ ] Android-first Kotlin app with a tabbed Chat and Models interface
- [ ] Local-only inference after user-initiated model downloads
- [ ] Lazy loading for translation, OCR, STT, and TTS engines
- [ ] Text translation using llama.cpp with small quantized GGUF models as the primary translation engine
- [ ] Optional ONNX Runtime translation support where a dedicated translation model is a better fit for a device or language
- [ ] Camera/image translation via OCR-first extraction followed by llama.cpp text translation
- [ ] Voice input through local Whisper-based STT
- [ ] Voice output through local Supertonic 3-based TTS
- [ ] App-private model storage that is removed when the app is uninstalled
- [ ] Manual Hugging Face resolve-link downloads for user-provided image/OCR model files where applicable
- [ ] In-app download buttons for supported Whisper and Supertonic model packs
- [ ] Broad multilingual translation as the v1 language goal

### Out of Scope

- Direct multimodal image-to-text-to-text through llama.cpp in v1 - too heavy for the low-end Android baseline; revisit as a high-end or future advanced mode
- Translating text as an overlay back onto source images - v1 translates extracted text only
- Full document parsing for PDFs, forms, tables, charts, formulas, and layout reconstruction - v1 focuses on photos, screenshots, signs, menus, labels, and other visible text
- Cloud inference fallback - privacy and offline guarantees require local inference
- Keeping downloaded models outside app-private storage - deleting the app should delete its dependencies

## Context

The project is driven by a local-first translation workflow where the user can type text, speak, or capture an image, then receive translated output without sending data to a server. Network access is allowed only for explicit model downloads initiated by the user.

The primary translation runtime is llama.cpp. For image translation, v1 should use a lightweight OCR pipeline first, then pass extracted text into llama.cpp for translation. This keeps the app aligned with llama.cpp while avoiding the RAM, latency, and battery cost of direct vision-language inference on low-end phones.

ONNX Runtime remains part of the architecture for components that fit it well, especially Supertonic 3 TTS and optional dedicated translation/OCR helper models. Whisper is the STT baseline, likely through whisper.cpp or an ONNX Runtime export depending on Android performance and packaging constraints.

PaddleOCR-VL-1.6 is a promising high-quality document parsing candidate, but it should not be assumed as the embedded v1 Android OCR baseline. It is a 1B-parameter image-text-to-text/document parsing model with Python/PaddlePaddle/server-oriented usage paths, which conflicts with the low-end Android baseline. Lightweight OCR options, including Android-native OCR or mobile ONNX OCR models, should be evaluated first.

Model download behavior matters:

- Image/OCR model files that are manually downloaded from Hugging Face should use `resolve` URLs, not `blob` URLs, so downloads retrieve the actual file content.
- Supertonic and Whisper model packs should be downloadable through buttons in the Models tab.
- All downloaded dependencies should live in app-private storage.

## Constraints

- **Platform**: Android first, implemented in Kotlin - Android app behavior, lifecycle, app-private storage, camera, microphone, and native inference bindings drive architecture.
- **Performance**: Optimize for low-end Android phones - choose small quantized models, keep context sizes conservative, and load engines only when needed.
- **Privacy**: Inference is local-only - no user text, audio, image, OCR output, or translation request should require a network call.
- **Network**: Network use is limited to explicit model downloads - app behavior should remain useful offline after dependencies are installed.
- **Storage**: Models live in app-private storage - uninstalling the app removes downloaded dependencies.
- **Translation Runtime**: llama.cpp is the primary translation engine - v1 should prioritize small GGUF models and speed over maximum quality.
- **Image Translation**: OCR-first in v1 - extract text, translate extracted text, and defer direct multimodal llama.cpp image mode.
- **TTS**: Supertonic 3 is the preferred TTS candidate - use local ONNX inference if Android performance is acceptable.
- **STT**: Whisper is the preferred STT candidate - evaluate whisper.cpp versus ONNX Runtime for low-end Android.

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Build Android-first in Kotlin | The app targets mobile camera, microphone, app-private storage, and offline use. | - Pending |
| Use llama.cpp as the primary translation engine | The user specifically wants llama.cpp for translation and local GGUF model execution. | - Pending |
| Keep ONNX Runtime in the architecture | ONNX is a strong fit for Supertonic 3 TTS and possible dedicated helper models. | - Pending |
| Use OCR-first image translation in v1 | Low-end Android devices are unlikely to handle direct multimodal VLM inference well. | - Pending |
| Defer direct multimodal llama.cpp image mode | Vision-capable GGUF plus projector files are heavier and better suited to future/high-end mode. | - Pending |
| Store models in app-private storage | Dependencies should be removed when the app is deleted. | - Pending |
| Use manual Hugging Face resolve links for applicable image/OCR models | `resolve` URLs download file contents directly; `blob` URLs are web pages. | - Pending |
| Provide in-app downloads for Whisper and Supertonic packs | These are core voice features and should be easy to install from the Models tab. | - Pending |
| Prioritize speed with smaller models | The app is optimized for low-end Android phones. | - Pending |
| Translate extracted image text only | Overlay translation and document layout reconstruction add complexity outside v1 scope. | - Pending |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `$gsd-transition`):
1. Requirements invalidated? Move to Out of Scope with reason
2. Requirements validated? Move to Validated with phase reference
3. New requirements emerged? Add to Active
4. Decisions to log? Add to Key Decisions
5. "What This Is" still accurate? Update if drifted

**After each milestone** (via `$gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check - still the right priority?
3. Audit Out of Scope - reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-05-29 after initialization*
