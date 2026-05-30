# Model File Details UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Show each model pack's required local files on the Models screen.

**Architecture:** Add a small UI formatter that turns `ModelPack`, install state, and `LocalModelPaths` into display text. Keep path formatting outside the composable so it can be tested with plain unit tests.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, JUnit 4.

---

## Scope Check

Included:

- Show required file paths for each model pack.
- Keep installed/missing state display unchanged.
- Unit test the formatter.

Deferred:

- File picker/import button.
- Clipboard copy action.
- Download buttons.
- Checksum display.

## File Structure

- Create: `app/src/main/java/com/xtranslate/ui/models/ModelPackUi.kt`
- Create: `app/src/test/java/com/xtranslate/ui/models/ModelPackUiTest.kt`
- Modify: `app/src/main/java/com/xtranslate/ui/models/ModelsScreen.kt`
- Modify: `app/src/main/java/com/xtranslate/ui/XTranslateApp.kt`
- Modify: `app/src/main/java/com/xtranslate/MainActivity.kt`

---

### Task 1: Add Model Pack UI Formatter

- [x] **Step 1: Write formatter test**

Create a unit test that verifies a translation pack displays state, engine, and required local file path.

- [x] **Step 2: Verify failing test**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.ui.models.ModelPackUiTest
```

Expected: fail because the formatter does not exist.

- [x] **Step 3: Add formatter**

Create `ModelPackUi.kt` with `ModelPackUi` and `ModelPackUiFormatter`.

- [x] **Step 4: Verify formatter test passes**

Run the same focused test. Expected: pass.

---

### Task 2: Show File Details On Models Screen

- [x] **Step 1: Pass `LocalModelPaths` to Models screen**

Thread `LocalModelPaths` from `MainActivity` to `XTranslateApp` to `ModelsScreen`.

- [x] **Step 2: Render required file paths**

Show a short `Required files` label and each required file path under the model card.

- [x] **Step 3: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.

---

## Commit

Commit once for this full plan:

```powershell
git add app/src/main/java/com/xtranslate/ui/models/ModelPackUi.kt app/src/test/java/com/xtranslate/ui/models/ModelPackUiTest.kt app/src/main/java/com/xtranslate/ui/models/ModelsScreen.kt app/src/main/java/com/xtranslate/ui/XTranslateApp.kt app/src/main/java/com/xtranslate/MainActivity.kt docs/superpowers/plans/2026-05-29-model-file-details-ui.md
git commit -m "feat: show model file details"
```
