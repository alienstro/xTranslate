# Readable Model File Sizes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make installed model file sizes easier to read on the Models screen.

**Architecture:** Improve `ModelPackUiFormatter.formatBytes(...)` so file labels use B, KB, or MB instead of always raw bytes.

**Tech Stack:** Kotlin, JUnit 4.

---

## Scope Check

Included:

- Show bytes for small files.
- Show KB for files at least 1024 bytes.
- Show MB for files at least 1024 KB.

Deferred:

- GB formatting.
- Decimal precision beyond one digit.

## File Structure

- Modify: `app/src/main/java/com/xtranslate/ui/models/ModelPackUi.kt`
- Modify: `app/src/test/java/com/xtranslate/ui/models/ModelPackUiTest.kt`

---

### Task 1: Add Readable File Sizes

- [x] **Step 1: Add failing tests**

Add tests for KB and MB file labels.

- [x] **Step 2: Verify failing tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.xtranslate.ui.models.ModelPackUiTest
```

Expected: fail because sizes still show raw bytes.

- [x] **Step 3: Update formatter**

Format file sizes as B, KB, or MB.

- [x] **Step 4: Verify full app**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both pass.
