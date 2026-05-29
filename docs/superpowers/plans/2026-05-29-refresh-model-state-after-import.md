# Refresh Model State After Import Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refresh the Models screen after model imports so `Missing` changes to `Installed` immediately.

**Architecture:** Add a simple Compose refresh counter in `MainActivity`. Increment it after each successful import and pass it through to `ModelsScreen`; the screen reads this value so Compose recomposes model cards after files are copied.

**Tech Stack:** Kotlin, Jetpack Compose, existing file-backed model store.

---

## Scope Check

Included:

- Refresh model state after translation import.
- Refresh model state after OCR model import.
- Refresh model state after OCR projector import.
- Keep `FileBackedModelStore` unchanged.

Deferred:

- Flow-based model store.
- Persistent import history.
- Download progress.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/MainActivity.kt`
- Modify: `app/src/main/java/com/xtranslate/ui/XTranslateApp.kt`
- Modify: `app/src/main/java/com/xtranslate/ui/models/ModelsScreen.kt`

---

### Task 1: Add Refresh Signal

- [x] **Step 1: Add refresh parameter**

Thread `modelStateRefreshKey: Int` from `MainActivity` to `XTranslateApp` to `ModelsScreen`.

- [x] **Step 2: Use refresh parameter in Models screen**

Read `modelStateRefreshKey` before rendering model cards so model state is recalculated after imports.

---

### Task 2: Increment Refresh Signal After Imports

- [x] **Step 1: Increment after translation import succeeds**

Increment the refresh key after `importTranslationModel(...)` succeeds.

- [x] **Step 2: Increment after OCR imports succeed**

Increment the refresh key after `importOcrModel(...)` and `importOcrProjector(...)` succeed.

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
git add app/src/main/java/com/xtranslate/MainActivity.kt app/src/main/java/com/xtranslate/ui/XTranslateApp.kt app/src/main/java/com/xtranslate/ui/models/ModelsScreen.kt docs/superpowers/plans/2026-05-29-refresh-model-state-after-import.md
git commit -m "feat: refresh model state after import"
```
