# Recording UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the invisible recording state with a clear animated pill in the composer, and move voice error strings out of the text field into the chat.

**Architecture:** Thread the existing `isRecordingVoice` Boolean from `MainActivity` down through `XTranslateApp` → `ChatScreen` → `ChatComposer`. When true, the composer action row transforms into an animated red-dot pill with a live timer and stop button; errors go to `ChatViewModel.showVoiceError()` which emits `System` messages to the chat list.

**Tech Stack:** Kotlin, Jetpack Compose, `androidx.compose.animation.core`, `material-icons-extended`

---

## File Map

| File | Change |
|---|---|
| `app/src/test/java/com/xtranslate/ui/chat/ChatViewModelTest.kt` | Add test for `showVoiceError()` |
| `app/src/main/java/com/xtranslate/ui/chat/ChatViewModel.kt` | Add `showVoiceError()` method |
| `app/src/main/java/com/xtranslate/ui/XTranslateApp.kt` | Add `isRecordingVoice: Boolean` param, pass to `ChatScreen` |
| `app/src/main/java/com/xtranslate/ui/chat/ChatScreen.kt` | Add `isRecordingVoice` to `ChatScreen` + `ChatComposer`; recording pill composable; timer |
| `app/src/main/java/com/xtranslate/MainActivity.kt` | Pass `isRecordingVoice` to `XTranslateApp`; replace 2 bad `updateComposer` calls |

---

## Task 1: Add `showVoiceError()` to ChatViewModel

**Files:**
- Modify: `app/src/main/java/com/xtranslate/ui/chat/ChatViewModel.kt`
- Test: `app/src/test/java/com/xtranslate/ui/chat/ChatViewModelTest.kt`

- [ ] **Step 1: Write the failing test**

Add inside the `ChatViewModelTest` class body (after the last `@Test` function, before the closing `}`):

```kotlin
@Test
fun showVoiceErrorAddsSystemMessage() =
    runTest {
        val viewModel = ChatViewModel(fakeCoordinator())

        viewModel.showVoiceError("Voice recording failed: Permission denied")

        assertEquals(false, viewModel.state.value.isBusy)
        assertTrue(
            viewModel.state.value.messages.any {
                it.kind == ChatMessageKind.System &&
                    it.text == "Voice recording failed: Permission denied"
            },
        )
    }
```

- [ ] **Step 2: Run the test to confirm it fails**

```
.\gradlew :app:test --tests "com.xtranslate.ui.chat.ChatViewModelTest.showVoiceErrorAddsSystemMessage"
```

Expected: FAIL — `Unresolved reference: showVoiceError`

- [ ] **Step 3: Implement `showVoiceError()` in ChatViewModel**

Add after the `speakTranslationPlaceholder()` method and before `private fun showError(...)` in `ChatViewModel.kt`:

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

- [ ] **Step 4: Run the test to confirm it passes**

```
.\gradlew :app:test --tests "com.xtranslate.ui.chat.ChatViewModelTest.showVoiceErrorAddsSystemMessage"
```

Expected: PASS

- [ ] **Step 5: Commit**

```
git add app/src/main/java/com/xtranslate/ui/chat/ChatViewModel.kt
git add app/src/test/java/com/xtranslate/ui/chat/ChatViewModelTest.kt
git commit -m "feat: add showVoiceError to ChatViewModel"
```

---

## Task 2: Thread `isRecordingVoice` through the component tree

**Files:**
- Modify: `app/src/main/java/com/xtranslate/ui/XTranslateApp.kt`
- Modify: `app/src/main/java/com/xtranslate/ui/chat/ChatScreen.kt`

The goal of this task is ONLY to pass the parameter through — no UI behavior changes yet. This makes Task 3 a pure UI task with a clean compile baseline.

- [ ] **Step 1: Add `isRecordingVoice` to `XTranslateApp`**

In `XTranslateApp.kt`, add the parameter to the function signature (after `onPickImage`):

```kotlin
@Composable
fun XTranslateApp(
    chatViewModel: ChatViewModel,
    modelStore: ModelStore,
    modelPaths: LocalModelPaths,
    onPickImage: () -> Unit,
    isRecordingVoice: Boolean,          // ← add this
    onMic: () -> Unit,
    onSpeakTranslation: (ChatMessage) -> Unit,
    // … rest of params unchanged …
```

Then in the `ChatScreen(...)` call inside `XTranslateApp`, add:

```kotlin
AppTab.Chat ->
    ChatScreen(
        state = state,
        isRecordingVoice = isRecordingVoice,   // ← add this
        onComposerChange = chatViewModel::updateComposer,
        onTargetLanguageChange = chatViewModel::updateTargetLanguage,
        onSend = chatViewModel::sendText,
        onImage = onPickImage,
        onMic = onMic,
        onSpeakTranslation = onSpeakTranslation,
    )
```

- [ ] **Step 2: Add `isRecordingVoice` to `ChatScreen` and `ChatComposer`**

In `ChatScreen.kt`, update the `ChatScreen` signature (add `isRecordingVoice` after `state`):

```kotlin
@Composable
fun ChatScreen(
    state: ChatUiState,
    isRecordingVoice: Boolean,
    onComposerChange: (String) -> Unit,
    onTargetLanguageChange: (String) -> Unit,
    onSend: () -> Unit,
    onImage: () -> Unit,
    onMic: () -> Unit,
    onSpeakTranslation: (ChatMessage) -> Unit,
) {
```

Pass it through in the `ChatComposer(...)` call inside `ChatScreen`:

```kotlin
ChatComposer(
    text = state.composerText,
    targetLanguage = state.targetLanguage,
    isBusy = state.isBusy,
    isRecordingVoice = isRecordingVoice,    // ← add this
    onTextChange = onComposerChange,
    onTargetLanguageChange = onTargetLanguageChange,
    onSend = onSend,
    onImage = onImage,
    onMic = onMic,
)
```

Add `isRecordingVoice: Boolean` to `ChatComposer`'s signature (after `isBusy`, before `onTextChange`):

```kotlin
@Composable
private fun ChatComposer(
    text: String,
    targetLanguage: String,
    isBusy: Boolean,
    isRecordingVoice: Boolean,
    onTextChange: (String) -> Unit,
    onTargetLanguageChange: (String) -> Unit,
    onSend: () -> Unit,
    onImage: () -> Unit,
    onMic: () -> Unit,
) {
```

Do not use `isRecordingVoice` inside the body yet — that happens in Task 3.

- [ ] **Step 3: Verify it compiles**

```
.\gradlew :app:compileDebugKotlin
```

Expected: BUILD SUCCESSFUL (the call site in `MainActivity` now has a type error for the missing param — that's fine, Task 5 fixes it)

Actually, it will NOT be successful because `MainActivity` calls `XTranslateApp(...)` without `isRecordingVoice`. To keep the build green, add a temporary default to `XTranslateApp`:

```kotlin
isRecordingVoice: Boolean = false,
```

That default lets `MainActivity` compile unchanged until Task 5 wires it properly.

- [ ] **Step 4: Compile check**

```
.\gradlew :app:compileDebugKotlin
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```
git add app/src/main/java/com/xtranslate/ui/XTranslateApp.kt
git add app/src/main/java/com/xtranslate/ui/chat/ChatScreen.kt
git commit -m "feat: thread isRecordingVoice through component tree"
```

---

## Task 3: Build the recording pill UI in `ChatComposer`

**Files:**
- Modify: `app/src/main/java/com/xtranslate/ui/chat/ChatScreen.kt`

- [ ] **Step 1: Add required imports to `ChatScreen.kt`**

Add these imports at the top of `ChatScreen.kt` (after the existing imports):

```kotlin
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.draw.alpha
import kotlinx.coroutines.delay
```

- [ ] **Step 2: Add `RecordingPill` composable**

Add this private composable at the bottom of `ChatScreen.kt` (after `TargetLanguagePicker`):

```kotlin
@Composable
private fun RecordingPill(
    elapsedSeconds: Int,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rec")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dot",
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
            modifier = Modifier.weight(1f),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = dotAlpha)),
                )
                Text(
                    text = "Listening…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "%d:%02d".format(elapsedSeconds / 60, elapsedSeconds % 60),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        IconButton(
            onClick = onStop,
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.error, CircleShape),
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Stop recording",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onError,
            )
        }
    }
}
```

- [ ] **Step 3: Update `ChatComposer` body**

Replace the entire body of `ChatComposer` with the following. The changes are: (a) timer state + `LaunchedEffect`, (b) branched placeholder, (c) branched action row:

```kotlin
@Composable
private fun ChatComposer(
    text: String,
    targetLanguage: String,
    isBusy: Boolean,
    isRecordingVoice: Boolean,
    onTextChange: (String) -> Unit,
    onTargetLanguageChange: (String) -> Unit,
    onSend: () -> Unit,
    onImage: () -> Unit,
    onMic: () -> Unit,
) {
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            elapsedSeconds = 0
            while (true) {
                delay(1_000)
                elapsedSeconds++
            }
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Translate to",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TargetLanguagePicker(
                    selectedLanguage = targetLanguage,
                    onTargetLanguageChange = onTargetLanguageChange,
                )
            }

            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRecordingVoice,
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                minLines = 1,
                maxLines = 5,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(12.dp),
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        if (text.isEmpty()) {
                            Text(
                                text = if (isRecordingVoice) "Speak now…" else "Type a message...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isRecordingVoice) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                },
                            )
                        }
                        innerTextField()
                    }
                },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onImage,
                    modifier = Modifier.alpha(if (isRecordingVoice) 0.4f else 1f),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = "Add image",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (isRecordingVoice) {
                    RecordingPill(
                        elapsedSeconds = elapsedSeconds,
                        onStop = onMic,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    IconButton(onClick = onMic) {
                        Icon(
                            imageVector = Icons.Outlined.Mic,
                            contentDescription = "Voice input",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = onSend,
                        enabled = !isBusy && text.isNotBlank(),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    ) {
                        if (isBusy) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Translate")
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 4: Compile check**

```
.\gradlew :app:compileDebugKotlin
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```
git add app/src/main/java/com/xtranslate/ui/chat/ChatScreen.kt
git commit -m "feat: add recording pill UI to ChatComposer"
```

---

## Task 4: Wire `isRecordingVoice` in `MainActivity` and fix error calls

**Files:**
- Modify: `app/src/main/java/com/xtranslate/MainActivity.kt`

- [ ] **Step 1: Fix the recording-failed error call in `startVoiceRecording()`**

Find the `startVoiceRecording()` function. Inside the `audioRecorder.startRecording(outputFile)` error callback, replace:

```kotlin
// BEFORE
chatViewModel.updateComposer("Voice recording failed: ${error.message}")
```

with:

```kotlin
// AFTER
chatViewModel.showVoiceError("Voice recording failed: ${error.message}")
```

- [ ] **Step 2: Fix the null-audio branch in `stopVoiceRecording()`**

Find `stopVoiceRecording()`. Replace the `if (audioFile == null)` branch:

```kotlin
// BEFORE
if (audioFile == null) {
    chatViewModel.updateComposer("No voice recording was active.")
    return@launch
}
```

with:

```kotlin
// AFTER
if (audioFile == null) return@launch
```

- [ ] **Step 3: Remove the default from `XTranslateApp.isRecordingVoice` and wire the real value**

In `XTranslateApp.kt`, remove the `= false` default added in Task 2:

```kotlin
// BEFORE
isRecordingVoice: Boolean = false,

// AFTER
isRecordingVoice: Boolean,
```

In `MainActivity.kt`, in the `XTranslateApp(...)` call (inside `setContent`), add the named argument:

```kotlin
XTranslateApp(
    chatViewModel = chatViewModel,
    modelStore = modelStore,
    modelPaths = modelPaths,
    onPickImage = {
        chatImagePicker.launch("image/*")
    },
    isRecordingVoice = isRecordingVoice,    // ← add this
    onMic = {
        // … existing body unchanged …
    },
    // … rest of args unchanged …
)
```

- [ ] **Step 4: Full compile and test check**

```
.\gradlew :app:compileDebugKotlin
.\gradlew :app:test
```

Expected: BUILD SUCCESSFUL, all tests PASS

- [ ] **Step 5: Commit**

```
git add app/src/main/java/com/xtranslate/MainActivity.kt
git add app/src/main/java/com/xtranslate/ui/XTranslateApp.kt
git commit -m "feat: wire recording state and fix voice error handling"
```

---

## Self-Review

**Spec coverage:**
- ✓ Wire `isRecordingVoice` through tree → Tasks 2 + 4
- ✓ Recording pill with pulsing dot + timer + stop button → Task 3 `RecordingPill`
- ✓ Text field placeholder "Speak now…" when recording → Task 3 `ChatComposer`
- ✓ `showVoiceError()` method → Task 1
- ✓ Fix "No voice recording was active." → Task 4 Step 2
- ✓ Fix "Voice recording failed: …" → Task 4 Step 1
- ✓ Image icon dims to 0.4 alpha during recording → Task 3 `ChatComposer`

**Placeholder scan:** No TBD/TODO, all steps have exact code.

**Type consistency:**
- `showVoiceError(message: String)` defined in Task 1, called in Task 4 ✓
- `isRecordingVoice: Boolean` added to `XTranslateApp`, `ChatScreen`, `ChatComposer` in Task 2; used in Task 3 ✓
- `RecordingPill(elapsedSeconds, onStop, modifier)` defined and called in Task 3 ✓
- `elapsedSeconds` is `Int` everywhere ✓
