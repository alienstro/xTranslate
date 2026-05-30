# Recording UI — Design Spec
Date: 2026-05-30

## Problem

The voice recording flow has two UX failures:

1. **No recording indicator** — `isRecordingVoice` is tracked in `MainActivity` but never passed to `ChatScreen` or `ChatComposer`. The mic button looks identical whether idle or actively recording. Users have no visual feedback that the app is listening.
2. **Error text pollutes the composer** — when recording stops with no active session, or when recording fails, the error string is written directly into `composerText` (the text field). This is confusing and non-dismissable.

## Goal

Make it immediately obvious when recording is active, and clean up error handling so errors appear as system messages in the chat rather than in the text field.

## Design: Recording Pill (Option A)

### Recording state visual

When `isRecordingVoice = true`, the composer action row transforms:

**Idle row:**
```
[🖼️] [🎙️]                    [▶ Translate]
```

**Recording row:**
```
[🖼️ (dimmed)] [ ● Listening…  0:04 ]  [■]
```

- Image icon dims to `alpha = 0.4` (still tappable to cancel recording implicitly, but visually de-emphasized)
- Mic icon is replaced by an animated red-dot pill spanning the available width:
  - Pulsing red dot (`.5s` fade in/out)
  - "Listening…" label
  - Live seconds timer (`0:00` format, increments every 1s via `LaunchedEffect`)
- A circular red stop button (■) replaces the send button area on the right — tapping it calls `onMic()` which triggers `stopVoiceRecording()` in `MainActivity`

### Text field hint

When recording, the `BasicTextField` placeholder changes from `"Type a message…"` to `"Speak now…"` rendered in a softer indigo tint (`primary.copy(alpha = 0.6f)`).

### Error handling fix

Two call sites in `MainActivity` currently write strings into `composerText`:

| Location | Current behavior | New behavior |
|---|---|---|
| `audioRecorder.startRecording` error callback | `updateComposer("Voice recording failed: …")` | `showVoiceError("Voice recording failed: …")` → emits `ChatMessageKind.System` |
| `stopVoiceRecording` with null audioFile | `updateComposer("No voice recording was active.")` | silent no-op (or omit the branch entirely — this state shouldn't happen in normal flow) |

A new `ChatViewModel.showVoiceError(message: String)` method posts a `System` message to the chat list. This is the same pattern already used by `showError()` for translation failures.

## Architecture

### Data flow

```
MainActivity.isRecordingVoice (Boolean)
  └─→ XTranslateApp(isRecordingVoice)
        └─→ ChatScreen(isRecordingVoice)
              └─→ ChatComposer(isRecordingVoice)
```

`isRecordingVoice` is read-only from the UI side. The mic button always calls `onMic()` whether starting or stopping; `MainActivity` owns the toggle logic (already implemented).

### New composable parameter chain

- `XTranslateApp`: add `isRecordingVoice: Boolean` parameter, thread to `ChatScreen`
- `ChatScreen`: add `isRecordingVoice: Boolean` parameter, thread to `ChatComposer`
- `ChatComposer`: add `isRecordingVoice: Boolean` parameter, use to branch the action row and placeholder text

### Timer

Inside `ChatComposer`, a `var elapsedSeconds by remember { mutableIntStateOf(0) }` tracks duration.

```kotlin
LaunchedEffect(isRecordingVoice) {
    if (isRecordingVoice) {
        elapsedSeconds = 0
        while (true) {
            delay(1000)
            elapsedSeconds++
        }
    }
}
```

Formatted as `"%d:%02d".format(elapsedSeconds / 60, elapsedSeconds % 60)`.

### ChatViewModel change

Add one method:

```kotlin
fun showVoiceError(message: String) {
    mutableState.update {
        it.copy(
            isBusy = false,
            messages = it.messages + ChatMessage(nextId++, ChatMessageKind.System, message),
        )
    }
}
```

### MainActivity change

Replace two `chatViewModel.updateComposer(errorString)` calls with `chatViewModel.showVoiceError(errorString)` and `Unit` respectively. Also pass `isRecordingVoice` into `XTranslateApp`.

## Files Changed

| File | Change |
|---|---|
| `MainActivity.kt` | Pass `isRecordingVoice` to `XTranslateApp`; replace 2 bad `updateComposer` calls |
| `XTranslateApp.kt` | Add `isRecordingVoice` param, pass to `ChatScreen` |
| `ChatScreen.kt` | Add `isRecordingVoice` to `ChatScreen` and `ChatComposer`; recording pill UI; timer `LaunchedEffect` |
| `ChatViewModel.kt` | Add `showVoiceError()` method |

## Out of Scope

- Waveform visualization (Option C) — deferred
- Haptic feedback on recording start/stop
- Max recording duration limit
