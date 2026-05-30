# xTranslate Local Runtime Spec

## Goal

xTranslate is an offline-first Android translation app. It should support text translation, image-to-text translation, speech-to-text, and text-to-speech while keeping model memory use safe for low-end phones.

## Current Runtime Shape

- Text translation uses llama.cpp through the app's llama runtime adapter.
- Image translation uses llama.cpp OCR/VLM flow with a model file and projector file.
- STT records local WAV audio and runs Whisper through whisper.cpp. TTS is still a placeholder engine, but it checks imported local model files before running.
- The runtime coordinator lazy-loads engines and clears loaded state aggressively in low-memory mode.
- Model files are imported or downloaded into app-private storage under `files/models/...`.

## Model Packs

- Translation: `models/translation/qwen2.5-0.5b-translator-q4_k_m.gguf`
- OCR model: `models/ocr/paddleocr-vl-1.5-q4.gguf`
- OCR projector: `models/ocr/paddleocr-vl-1.5-mmproj.gguf`
- Whisper STT: `models/stt/ggml-large-v3-turbo-q8_0.bin`
- Supertonic TTS: `models/tts/supertonic-3.onnx`

## User Flows

### Chat

- User can type text and translate it to a selected target language.
- User can pick an image and run image translation.
- User can press MIC once to start local recording and press it again to stop and transcribe with Whisper.
- User can press Speak on a translation message to run the current TTS placeholder flow.
- Missing model errors are shown as system messages and route the user to the Models tab.

### Models

- User can import translation, OCR, OCR projector, Whisper, and Supertonic files.
- User can download configured model files from the Models tab without leaving the app.
- User can see the active download file and progress while a model file is downloading.
- User can run local text, OCR, STT, and TTS smoke tests.
- Model cards show state, engine type, RAM tier, low-end fit, expected size, installed-file progress, required file paths, and readable installed file sizes.

## Low-Memory Rules

- In low-memory mode, loading one engine clears previously loaded engine state.
- OCR, STT, and TTS unload after each use.
- OCR, STT, TTS, and failed translation calls clean up loaded state after errors.
- Successful text translation may remain loaded for reuse until another engine is needed.

## Error Rules

- Missing model messages must include stable forward-slash paths.
- Missing model errors should be clear enough for the UI to tell the user what to import.
- Download failures should leave the model marked failed and show the user a short reason.
- Non-model errors should stay on the current tab.

## Deferred Work

- Real Supertonic ONNX inference.
- Real audio playback.
- Device RAM detection and automatic Q4/Q8 model selection.
- Checksum validation.
- Performance testing on low-end Android devices.

## Verification

Before treating a milestone as usable:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Run Gradle commands sequentially. Parallel Gradle builds can fight over Kotlin incremental caches on this Windows/OneDrive workspace.
