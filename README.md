# xTranslate

An Android-first Kotlin app for local text, voice, and image translation — no cloud required after the initial model download.

## What it does

xTranslate provides a mobile chat UI (inspired by Claude) where translation results appear inline as conversation messages. All inference runs on-device using:

- **llama.cpp** — image OCR via PaddleOCR-VL-1.5 Q4, and text translation via a broad multilingual GGUF model
- **Whisper** — speech-to-text transcription
- **ONNX Runtime + Supertonic 3** — text-to-speech playback

Models are stored in app-private storage and removed automatically when the app is uninstalled.

## Flows

**Text translation** — type in the composer, pick a target language, tap Translate.

**Image translation** — tap the image button, pick from gallery or capture with camera. llama.cpp extracts text via PaddleOCR-VL, shows an editable OCR review, then translates the confirmed text.

**Voice translation** — tap the mic, Whisper transcribes the audio into the composer, then the standard translation flow runs. TTS playback is opt-in.

## Architecture

```
app/
  domain/         — engine interfaces, shared types, prompt construction
  model/          — model registry, install state, local file paths, importer
  llama/          — llama.cpp runtime wrapper, OCR and translation engines
  ui/
    chat/         — chat screen, ViewModel, message models
    models/       — model pack status screen
    theme/        — warm neutral Material 3 theme
  MainActivity.kt — Compose entry point
third_party/
  llama.cpp       — native inference backend
```

The `EngineCoordinator` owns lazy loading so only the engines needed for the current workflow are resident in memory. On low-RAM devices, the OCR model is unloaded before the translation model is loaded.

### Engine interfaces

```kotlin
interface OcrEngine         { suspend fun extractText(image: ImageInput): OcrResult }
interface TranslationEngine { suspend fun translate(request: TranslationRequest): TranslationResult }
interface SpeechToTextEngine{ suspend fun transcribe(audio: AudioInput): Transcript }
interface TextToSpeechEngine{ suspend fun synthesize(request: SpeechRequest): AudioOutput }
```

## Models

| Pack | Engine | Model |
|------|--------|-------|
| OCR | llama.cpp | PaddleOCR-VL-1.5 Q4 GGUF + mmproj file |
| Translation | llama.cpp | Broad multilingual GGUF |
| STT | Whisper | Whisper model binary |
| TTS | ONNX Runtime | Supertonic 3 ONNX assets |

Models are downloaded on demand from the Models screen and tracked per-pack with `Missing / Downloading / Installed / Failed` states.

## Requirements

- Android 13+ (API 33)
- ~4 GB RAM recommended for simultaneous OCR + translation (sequential unloading is used on tighter devices)
- Camera and microphone permissions for image and voice flows

## Building

```bash
./gradlew :app:assembleDebug
```

The APK is output to `app/build/outputs/apk/debug/`.

Run unit tests:

```bash
./gradlew :app:testDebugUnitTest
```

## Project status

The foundation — chat UI, domain contracts, model registry, engine coordinator, and llama.cpp runtime binding — is implemented and building. Native OCR and translation engines are wired to llama.cpp. Whisper STT and Supertonic TTS placeholder flows are in place; full native integration for those is in progress.

## License

MIT — see [LICENSE](LICENSE).
