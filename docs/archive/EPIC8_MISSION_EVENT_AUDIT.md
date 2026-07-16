# EPIC 8 — Mission Completion Event Pipeline Audit

**Date:** 2026-06-23
**Auditor:** ASHWAth AI
**Scope:** Mission completion detection, notification firing, ceremony system
**Branch:** `epic8-mission-migration`
**Build:** `BUILD SUCCESSFUL` (14s, 0 errors)

---

## 1. Problem Statement

Players reported that mission completion notifications ("MISSION COMPLETE: ...") fire repeatedly at the start of every new run, even for missions that were completed in previous sessions. The investigation focused on whether:

- Already-completed missions are being re-evaluated as newly completed
- Completion events fire during init, syncState(), or mission loading
- The ceremony/notification system distinguishes between "just now completed" and "completed in a previous run"

---

## 2. Pipeline Diagram

```
  init/syncState()
       │
       ▼
  allMissionInstances[id].isCompleted = progressionManager.completedMissionIds.contains(id)
       │
       ▼
  restartGame()
       │
       ├── missionManager.clear()           ← clears activeMissions, completedIdsInRun
       │
       └── missionManager.selectNextMission()
              │
              ├── template lookup: MissionRegistry.getAllTemplates().find {
              │       type == trackType &&
              │       !completedIdsInRun.contains(template.id)    ← BUG: always true (empty set)
              │   }
              │
              └── activateMission(id) → activeMissions.add(allMissionInstances[id])
                                               │
                                               ▼
  PER FRAME:
       │
       ├── updateProgressAll(stats)     ← GUARD: if (mission.isCompleted) return → SAFE
       │
       ├── updateProgress(type, ...)    ← GUARD: filter { !it.isCompleted } → SAFE
       │
       └── Ceremony loop (lines 1531-1541):
              activeMissions.forEach { mission ->
                  if (mission.isCompleted && missionCeremonies[id] == null) 
                      → BUG: no "completed this run" check → FIRES NOTIFICATION
              }
```

---

## 3. Root Causes Found

### Root Cause A — `selectNextMission()` activates previously-completed missions

**File:** `MissionManager.kt:237-239`
**Severity:** CRITICAL

```kotlin
val nextTemplate = MissionRegistry.getAllTemplates().find { template ->
    template.type == type && !completedIdsInRun.contains(template.id)
}
```

`completedIdsInRun` is a `mutableSetOf<String>()` cleared by `clear()` at every `restartGame()`. Since the set is always empty at run start, the `find` predicate matches ALL templates for a given type — including missions that were completed in previous runs.

**Impact:** Previously-completed missions are re-activated into the 3-slot active HUD on every new run.

### Root Cause B — Ceremony loop has no "completed this run" guard

**File:** `GameScreen.kt:1533`
**Severity:** CRITICAL

```kotlin
if (mission.isCompleted && missionCeremonies[mission.id] == null) {
```

This check only verifies:
1. The mission has `isCompleted == true` (could be from a previous run)
2. No ceremony timer is currently running for this mission

It does NOT check whether the mission was actually completed during the current run. Combined with Root Cause A, a mission re-activated from a previous run immediately triggers notification, burst, shake, and impact flash on frame 1.

### Root Cause C — `missionCeremonies` persists across runs

**File:** `GameScreen.kt:122`
**Severity:** MAJOR

```kotlin
val missionCeremonies = remember { mutableStateMapOf<String, Float>() }
```

`missionCeremonies` is a `remember` state that survives `restartGame()`. Although entries auto-expire after 3.0 seconds, the combination of Root Cause A + B means the ceremony fires before any cleanup has a chance to run.

---

## 4. Verification: Guards That WORK Correctly

### `updateProgressAll()` — SAFE

```kotlin
// MissionManager.kt:106
if (mission.isCompleted) return@forEach
```

This early-return guards `calculateProgress()` and `checkCompletion()` for completed missions. Mission completion is only re-detected when `progress.toInt() > before`, which cannot happen for completed missions because this line skips them entirely.

### `updateProgress()` — SAFE

```kotlin
// MissionManager.kt:177
activeMissions.filter { it.type == type && !it.isCompleted && ... }
```

The `.filter { !it.isCompleted }` prevents progress updates from re-firing completion detection on already-completed missions.

### `checkCompletion()` — SAFE (by itself)

```kotlin
// Mission.kt:76-83
fun checkCompletion(): Boolean {
    if (!isCompleted && currentProgress >= targetValue) {
```

The `!isCompleted` guard prevents double-firing on the same mission within a run. However, this method is never reached for completed missions because `updateProgressAll` and `updateProgress` already filter them out (see above).

---

## 5. Fix Applied

### Fix A — Filter completed missions in `selectNextMission()`

**File:** `MissionManager.kt:237-239`

**Before:**
```kotlin
val nextTemplate = MissionRegistry.getAllTemplates().find { template ->
    template.type == type && !completedIdsInRun.contains(template.id)
}
```

**After:**
```kotlin
val nextTemplate = MissionRegistry.getAllTemplates().find { template ->
    template.type == type &&
    !completedIdsInRun.contains(template.id) &&
    allMissionInstances[template.id]?.isCompleted != true
}
```

**Why this works:** `allMissionInstances` is populated for every template during `init()` (line 62-83), and each instance's `isCompleted` is synced from `progressionManager.completedMissionIds`. The new check prevents any mission whose `isCompleted == true` from being activated into the HUD.

**Edge case:** If a player has completed ALL missions of a given track, the track slot remains empty. This is correct behavior — there are no more missions to progress.

### Fix B — Clear `missionCeremonies` on restart

**File:** `GameScreen.kt:720`

**Added line:** `missionCeremonies.clear()` in `restartGame()`

**Why this works:** Eliminates stale ceremony state from the previous run. Combined with Fix A, the ceremony loop will see either:
- Newly-completed missions (legitimate ceremony) with `missionCeremonies[id] == null` → fires once
- No completed missions at all (no re-activated stale ones)

---

## 6. Remaining Risk Assessment

| Scenario | Risk | Mitigation |
|----------|------|------------|
| Mission completed THIS run, ceremony fires correctly | None | Guards preserved |
| Mission completed previous run, not claimed, no other missions in track | Track slot empty | Correct — no active mission to show |
| Mission completed previous run, claimed, track has other missions | Next uncompleted mission activates | Correct progression |
| Player completes mission, dies before ceremony finishes | Ceremony lost on restart | Acceptable — mission already recorded as completed |
| Unlock conditions make next mission unavailable | Track slot empty until unlock | Correct — prevents showing locked missions |

No remaining paths where a stale completed mission triggers a false notification.

---

## 7. Build Verification

```powershell
PS> .\gradlew.bat assembleDebug
BUILD SUCCESSFUL in 14s
38 actionable tasks: 9 executed, 29 up-to-date
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

---

## 8. Files Changed

| File | Change |
|------|--------|
| `app/.../MissionManager.kt:239` | Added `allMissionInstances[template.id]?.isCompleted != true` guard to `selectNextMission()` |
| `app/.../GameScreen.kt:720` | Added `missionCeremonies.clear()` to `restartGame()` |

---

## 9. Appendix: Event Flow Walkthrough

### Scenario: First run, mission completes

```
Frame N:     updateProgressAll → progress advances → checkCompletion() → true
             → isCompleted = true
             → completedIdsInRun.add(id)
             → onMissionCompleted.invoke(mission) → recordMissionCompletion()
             
Frame N+1:   Ceremony loop: isCompleted=true, missionCeremonies[id]==null → POST NOTIFICATION
             → missionCeremonies[id] = 0f
             
Frame N+3s:  Ceremony cleanup: remove from missionCeremonies → ceremonyStage = REPLACING
             → next selectNextMission() removes from activeMissions
```

### Scenario: New run, mission already completed (BEFORE FIX)

```
restartGame():
  clear():       activeMissions = [] , completedIdsInRun = {}
  selectNextMission():
                   Template find matches ALL (completedIdsInRun is empty)
                   → completed mission activated into activeMissions
                   
Frame 1:  Ceremony loop: isCompleted=true, missionCeremonies[id]==null → POST NOTIFICATION ← BUG
```

### Scenario: New run, mission already completed (AFTER FIX)

```
restartGame():
  clear():       activeMissions = [] , completedIdsInRun = {}
  missionCeremonies.clear()  ← NEW
  selectNextMission():
                   Template find: allMissionInstances[id].isCompleted=true → SKIPPED
                   → next uncompleted mission activated (if any)
                   
Frame 1:  Ceremony loop: no active missions have isCompleted=true → NO FALSE NOTIFICATION ✓
```
