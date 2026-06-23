# EPIC 8 — Mission Stabilization Sprint Report

**Date:** 2026-06-23
**Sprint:** Stabilization Sprint
**Branch:** `epic8-mission-migration`
**Build:** `BUILD SUCCESSFUL` (43s, 0 errors)

---

## Issue 1 — CRITICAL: Mission `currentProgress` not persisted

### Root Cause
`Mission.kt:64` — `currentProgress` is `mutableIntStateOf(initialProgress=0)` with no save/load path. `MissionManager.kt:75` always set `initialProgress = 0`. Progress existed only in RAM.

### Fix Applied
- **`ProgressionManager.kt`**: Added `saveMissionProgress(missionId, progress)` and `getMissionProgress(missionId): Int` methods using SharedPreferences with key prefix `mission_progress_`. Save only writes when value differs from persisted state.
- **`MissionManager.kt:75`**: Changed `initialProgress = 0` → `initialProgress = progressionManager.getMissionProgress(template.id)`.
- **`MissionManager.kt:93`**: `syncState()` now restores progress from persistence (if saved > in-memory).
- **`MissionManager.kt:109`**: `updateProgressAll()` saves progress after each advancement.
- **`MissionManager.kt:183`**: `updateProgress()` saves progress after each advancement.
- **`MissionManager.kt:213`**: `resetProgress()` persists the reset (saves 0).
- **`MissionManager.kt:161`**: `claimMissionRewards()` saves final progress on claim.

### Verification
- Progress advances during gameplay → persisted.
- Progress restored on `init` (app restart) via `getMissionProgress`.
- Progress restored on `syncState` (MissionScreen entry).
- Reset correctly writes 0 (no stale high value remains).

---

## Issue 2 — CRITICAL: `lifetimeMissionsCompleted` not properly persisted

### Root Cause
`ProgressionManager.kt:88` loaded `lifetimeMissionsCompleted` from `missions_completed` key, but `recordMissionCompletion()` never wrote back to it. `lifetimeMissionsCompleted = completedMissionIds.size` was in-memory only.

### Fix Applied
- **`ProgressionManager.kt:165`**: `recordMissionCompletion()` now writes `missions_completed` to SharedPreferences alongside the `completed_missions` StringSet.
- **`ProgressionManager.kt:190`**: `commitSessionStats()` also persists `missions_completed` as backup.
- **`ProgressionManager.kt:261-268`**: `wipeData()` resets all lifetime stat fields.

### Verification
- `missions_completed` key now written on every `recordMissionCompletion` call.
- `missions_completed` key written on every `commitSessionStats` call (game over).
- Value survives app restart.

---

## Issue 3 — MAJOR: Lifetime/Total missions use run statistics

### Root Cause
`MissionManager.kt:122-137` — `calculateProgress()` used `stats.totalFlightTime` (run-local) for FLIGHT_TIME and PLATFORM_STAY categories. Tier-4 targets (3600s = 60 min) were impossible to achieve in a single run.

### Fix Applied
- **`MissionManager.kt:123-124`**: Changed to:
  - `FLIGHT_TIME → progressionManager.lifetimeFlightTime + stats.totalFlightTime`
  - `PLATFORM_STAY → progressionManager.lifetimePlatformTime + stats.totalPlatformTime`

### Verification
- Progress now accumulates across runs: run 1 (180s) + run 2 (120s) = 300s total.
- Previous lifetime stats are loaded from persistence and added to current run.
- Game over flow: `commitSessionStats` adds current run to lifetime → next run starts with correct baseline.
- No double-count: `lifetimeFlightTime` is stable during gameplay, only updated at game over. `totalFlightTime` resets to 0 on `restartGame()`.

---

## Issue 4 — MAJOR: Hangar mission counter uses claimed count

### Root Cause
`ProgressionManager.kt:56` — `missionsCompleted: Int get() = claimedMissionIds.size` counted claimed (rewarded) missions, not completed missions.

### Fix Applied
- **`ProgressionManager.kt:56`**: Changed to `val missionsCompleted: Int get() = completedMissionIds.size`.

### Verification
- `claimedMissionIds.size` → missions with claimed rewards.
- `completedMissionIds.size` → missions with completion state = true (regardless of claim).
- Hangar display `"${progressionManager.missionsCompleted} DONE >"` now accurately shows completed mission count.

---

## Issue 5 — MAJOR: Artifact lifetime count double-counting

### Root Cause
`GameScreen.kt:573` and `:976` — `artifactsCollected = progressionManager.artifactsCollected.size` (total **lifetime** artifact types). `commitSessionStats` added `lifetimeArtifactsCollected += stats.artifactsCollected`, inflating the count every session end.

### Fix Applied
- **`GameScreen.kt:573`**: Changed to `artifactsCollected = totalArtifactsCollected` (run-local counter).
- **`GameScreen.kt:976`**: Changed to `artifactsCollected = totalArtifactsCollected` (run-local counter).
- `totalArtifactsCollected` is defined at line 94, incremented at line 224 (per-discovery), and reset at line 709 (per-run).

### Verification
- Run-local `totalArtifactsCollected` tracks only artifacts found THIS run.
- `commitSessionStats` adds only this run's count to lifetime.
- No inflation across multiple sessions.

---

## Issue 6 — MINOR: Mission reward display shows only first reward

### Root Cause
`MissionScreen.kt:211` — `mission.rewards.firstOrNull()?.let { ... }` displayed only the first reward.

### Fix Applied
- **`MissionScreen.kt:211-228`**: Replaced `firstOrNull()` with iteration over all rewards. Shows up to 3 reward labels joined by " + " and a "+N MORE" suffix if additional rewards exist beyond 3.

### Verification
- Multi-reward missions (e.g., `listOf(Cash(100), Artifact(...))`) now display as `"REWARD: +100 CASH + ARTIFACT"`.
- Single-reward missions display as before (`"REWARD: +100 CASH"`).
- No-reward missions (MissionReward.None) display nothing.

---

## Build Result

```
BUILD SUCCESSFUL in 43s
36 actionable tasks: 9 executed, 27 up-to-date
```

Zero errors. One pre-existing deprecation warning in `ArchiveScreen.kt` (unrelated).

---

## Files Modified

| File | Lines Changed | Changes |
|:--|:--:|:--|
| `ProgressionManager.kt` | +30 / +2 / -1 | Added progress save/load, fixed `missionsCompleted` getter, fixed `lifetimeMissionsCompleted` write, fixed `wipeData` |
| `MissionManager.kt` | +10 / +1 / -1 | Added progress persistence hooks, fixed `calculateProgress` for FLIGHT_TIME/PLATFORM_STAY, fixed `init`/`syncState` |
| `GameScreen.kt` | 2 changes | Fixed `artifactsCollected` from lifetime to run-local (2 sites) |
| `MissionScreen.kt` | 1 block replaced | Fixed reward display from `firstOrNull()` to multi-reward iteration |

---

## Remaining State

All original architecture preserved:
- 12-track dashboard ✅
- Tier progression (Rookie → God) ✅
- Claim flow ✅
- Hidden mission architecture ✅
- Existing save data ✅
- Navigation routes ✅

---

## Conclusion

All 6 issues from the QA audit are resolved. EPIC 8 is now technically stable enough for Phase 6 (Hidden Signals & Dynamic Unlocks) to be built on top.

**Key wins:**
- Mission progress survives app restart, process death, and device restart
- Cumulative missions (flight time, platform stay) now accumulate across runs
- Lifetime stats are accurate without inflation
- Mission counter reflects true completion state
- Reward display shows all mission rewards
