# File-Backed Model State Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the Models screen report installed/missing state from app-private model files.

**Architecture:** Extend `LocalModelPaths` so every `ModelPack` can resolve its expected files under app-private storage. Add a `FileBackedModelStore` that keeps temporary downloading/failed state but derives installed/missing from real files.

**Tech Stack:** Kotlin, Android app-private files, JUnit 4, Jetpack Compose existing UI.

---

## Scope Check

Included:

- Resolve file paths for all registered model packs.
- Detect installed state when all required files exist.
- Detect missing state when any required file is absent.
- Use file-backed store from `MainActivity`.

Deferred:

- Model downloads.
- File picker/import UI.
- Checksums.
- Hugging Face URLs.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/model/LocalModelPaths.kt`
- Modify: `app/src/main/java/com/xtranslate/model/ModelStore.kt`
- Modify: `app/src/main/java/com/xtranslate/MainActivity.kt`
- Modify: `app/src/test/java/com/xtranslate/model/LocalModelPathsTest.kt`
- Modify: `app/src/test/java/com/xtranslate/model/ModelRegistryTest.kt`

---

### Task 1: Expand Local Model Paths

- [x] **Step 1: Add tests for pack file paths**

Verify translation and OCR files resolve under app-private `models` directories.

- [x] **Step 2: Add generic file resolver**

Add `modelDirectory(pack)`, `modelFile(pack, file)`, and `modelFiles(pack)`.

---

### Task 2: Add File-Backed Model Store

- [x] **Step 1: Add tests for file-backed installed/missing state**

Verify a pack is missing when required files do not exist and installed when all required files exist.

- [x] **Step 2: Implement `FileBackedModelStore`**

Use actual files for `Missing` and `Installed`, while preserving temporary `Downloading` and `Failed` states.

---

### Task 3: Wire App To File-Backed Store

- [x] **Step 1: Update `MainActivity`**

Use `FileBackedModelStore` instead of `InMemoryModelStore`.

- [x] **Step 2: Verify**

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
git add app/src/main/java/com/xtranslate/model/LocalModelPaths.kt app/src/main/java/com/xtranslate/model/ModelStore.kt app/src/main/java/com/xtranslate/MainActivity.kt app/src/test/java/com/xtranslate/model/LocalModelPathsTest.kt app/src/test/java/com/xtranslate/model/ModelRegistryTest.kt docs/superpowers/plans/2026-05-29-file-backed-model-state.md
git commit -m "feat: add file-backed model state"
```
