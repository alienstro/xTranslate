# Low End Friendly Model Label Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make low-end-friendly model packs easy to spot on the Models screen.

**Architecture:** Add a `deviceFitLabel` to `ModelPackUi`. Low RAM packs show `Device fit: Low-end friendly`; Medium and High packs show their minimum tier plainly.

**Tech Stack:** Kotlin, Jetpack Compose, JUnit 4.

---

## Scope Check

Included:

- Low RAM tier packs show a low-end friendly label.
- Medium/High packs show a clear minimum device tier.

Deferred:

- Automatic device RAM detection.
- Model recommendation sorting.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/ui/models/ModelPackUi.kt`
- Modify: `app/src/main/java/com/xtranslate/ui/models/ModelsScreen.kt`
- Modify: `app/src/test/java/com/xtranslate/ui/models/ModelPackUiTest.kt`

---

### Task 1: Add Device Fit Label

- [x] **Step 1: Add failing tests**

Test Low and Medium device fit labels.

- [x] **Step 2: Verify failing tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.ui.models.ModelPackUiTest
```

Expected: fail because `deviceFitLabel` does not exist.

- [x] **Step 3: Add formatter label**

Add `deviceFitLabel`.

- [x] **Step 4: Render label**

Show the label on model cards.

- [x] **Step 5: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
