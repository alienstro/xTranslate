# Model File Progress Label Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Show how many required files are installed for each model pack.

**Architecture:** Add `fileProgressLabel` to `ModelPackUi`. The formatter counts existing required files and renders a simple label like `Files: 1/2 installed`.

**Tech Stack:** Kotlin, Jetpack Compose, JUnit 4.

---

## Scope Check

Included:

- Count installed required files.
- Show the count on each model card.

Deferred:

- Progress bars.
- Optional model files.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/ui/models/ModelPackUi.kt`
- Modify: `app/src/main/java/com/xtranslate/ui/models/ModelsScreen.kt`
- Modify: `app/src/test/java/com/xtranslate/ui/models/ModelPackUiTest.kt`

---

### Task 1: Add File Progress Label

- [x] **Step 1: Add failing tests**

Test missing and partially installed file counts.

- [x] **Step 2: Verify failing tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.ui.models.ModelPackUiTest
```

Expected: fail because `fileProgressLabel` does not exist.

- [x] **Step 3: Add formatter label**

Count existing required files and format the label.

- [x] **Step 4: Render label**

Show the label on each model card.

- [x] **Step 5: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
