# Stable Model Size Formatting Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Keep model file size labels stable across device locales.

**Architecture:** `ModelPackUiFormatter` should format decimal KB/MB values with a dot decimal separator, regardless of the Android device locale.

**Tech Stack:** Kotlin, JUnit 4.

---

## Scope Check

Included:

- Decimal file sizes use `.` even if the default locale uses `,`.
- Existing B/KB/MB labels keep working.

Deferred:

- Localized number formatting.
- GB formatting.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/ui/models/ModelPackUi.kt`
- Modify: `app/src/test/java/com/xtranslate/ui/models/ModelPackUiTest.kt`

---

### Task 1: Stabilize Size Formatting

- [x] **Step 1: Add failing test**

Set a comma-decimal locale and assert the label still uses `1.5 KB`.

- [x] **Step 2: Verify failing test**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.ui.models.ModelPackUiTest
```

Expected: fail because `String.format` uses the default locale.

- [x] **Step 3: Use fixed locale**

Use `Locale.US` for formatter-only decimal output.

- [ ] **Step 4: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
