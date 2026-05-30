# Model Hardware Hints UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Show simple hardware hints on each Models screen card.

**Architecture:** Extend `ModelPackUiFormatter` to expose RAM tier and expected size labels from `ModelPack`. Render those labels in `ModelsScreen` so users can quickly see which packs are low-end friendly.

**Tech Stack:** Kotlin, Jetpack Compose, JUnit 4.

---

## Scope Check

Included:

- Show minimum RAM tier.
- Show expected size when known, otherwise `Unknown size`.
- Keep labels simple for non-technical users.

Deferred:

- Runtime device RAM detection.
- Automatic Q4/Q8 model selection.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/ui/models/ModelPackUi.kt`
- Modify: `app/src/main/java/com/xtranslate/ui/models/ModelsScreen.kt`
- Modify: `app/src/test/java/com/xtranslate/ui/models/ModelPackUiTest.kt`

---

### Task 1: Add Hardware Hint Labels

- [x] **Step 1: Add failing tests**

Test RAM tier and unknown/known size labels.

- [x] **Step 2: Verify failing tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.ui.models.ModelPackUiTest
```

Expected: fail because `ModelPackUi` does not expose the new labels.

- [x] **Step 3: Add formatter labels**

Add `ramTierLabel` and `sizeLabel`.

- [x] **Step 4: Render labels**

Show the labels on each model card.

- [x] **Step 5: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
