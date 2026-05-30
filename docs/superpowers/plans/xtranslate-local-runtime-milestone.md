# xTranslate Local Runtime Milestone Plan

## Purpose

This is the active plan for the current local-runtime milestone. Small task-level plans from May 29-30 are archived; this file is the plan to follow going forward.

## Completed In This Milestone

- Android Kotlin app foundation with Compose Chat and Models tabs.
- llama.cpp integration shell through the official Android library module.
- Local translation model import and smoke test.
- OCR model/projector import and image OCR smoke test.
- Chat text translation through local llama engine.
- Chat image picker and image translation flow.
- Friendly chat error messages for missing models.
- Target language picker.
- Chat message labels.
- Voice placeholder flow through STT interface.
- TTS placeholder flow from translation messages.
- Low-memory engine loading rules and failure cleanup.
- Stable speech model paths and file-backed speech placeholder engines.
- Local STT/TTS smoke test buttons.
- Models screen hardware hints, low-end friendly labels, file progress, and readable file sizes.
- In-app model download buttons for configured model packs.

## Active Rules

- Do not create a new plan file for every small task.
- Add new work to this milestone plan unless it is a genuinely new milestone.
- Use tests first for behavior changes.
- Run Gradle verification sequentially, not in parallel.
- Do not commit until the user asks.

## Next Recommended Work

1. Install the current debug APK on a phone and test the UI flows.
2. Confirm model downloads work from the Models tab on Wi-Fi.
3. Confirm model import still works from Android file picker.
4. Confirm missing model errors route to the Models tab.
5. Test one tiny placeholder Whisper file and one tiny placeholder Supertonic file to confirm the smoke tests unlock.
6. Pick the next native runtime integration target:
   - Whisper STT runtime, or
   - Supertonic ONNX TTS runtime, or
   - real OCR/VLM image handling in llama.cpp.

## Phone Test Checklist

- App opens without crashing.
- Models tab shows all model cards.
- Import buttons open Android file picker.
- Local text test shows a clear model-missing error if no GGUF is imported.
- Local OCR test opens image picker.
- Local STT/TTS tests show missing model errors before files are imported.
- Chat text translate shows missing translation model error before model import.
- Chat image translate opens image picker and shows missing OCR model/projector errors before import.

## Verification Commands

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Debug APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```
