# EPIC 8 — Technical QA & Audit Report

**Date:** 2026-06-23
**Role:** CHECKER (OpenCode Technical QA)
**Audit Scope:** Mission System, Progression System, Persistence, Navigation
**Branch:** `epic8-mission-migration`
**Stable Tags:** `epic8-recovery-stable`, `documentation-closed`

---

## Classification Key

| Severity | Definition |
| :--- | :--- |
| **CRITICAL** | Data loss or incorrect persistence. Blocks release. |
| **MAJOR** | Logic error that degrades gameplay or progression. Must fix before release. |
| **MINOR** | Deviates from design spec or user expectation. Should fix. |
| **TECH DEBT** | Structural concern. Does not block release but should be scheduled. |

---

## 1. Gemini Finding Verification

### Finding 1: `currentProgress` is not persisted

| | |
| :--- | :--- |
| **Status** | **CONFIRMED — CRITICAL** |
| **Evidence** | `Mission.kt:64` — `currentProgress` is `mutableIntStateOf(initialProgress)` with no persistence. `MissionManager.kt:62-83` — Init always sets `initialProgress = 0`. No SharedPreferences key exists for mission progress. Only `isCompleted` and `isClaimed` are synced with disk. |
| **Impact** | All mission progress (e.g., 270s out of 300s for "Sky Explorer") is lost on app restart. Player must re-earn progress from 0. This fundamentally breaks cumulative/lifetime missions that span multiple sessions. |
| **Recommendation** | Add a `progress_<missionId>` key to SharedPreferences. Persist `currentProgress` in `updateProgressAll()` and restore on init via `syncState()`. |

---

### Finding 2: Lifetime/"Total" missions use run statistics

| | |
| :--- | :--- |
| **Status** | **CONFIRMED — MAJOR** |
| **Evidence** | `GameStats.kt:7` — Explicitly documented as "Real-time statistics for the **current run**". `MissionManager.kt:122-137` — `calculateProgress()` reads from `GameStats` fields: `stats.totalFlightTime`, `stats.maxAltitude`, etc. `GameScreen.kt:961-981` — Stats are built from run-local variables (`airborneTimer`, `platformStayTimer`, `noOverheatTimer`, `perfectRunTimer`). |
| **Impact** | Missions requiring cumulative time ("Spend 5 minutes total in the air") reset every run. Player must achieve the target in a single run rather than accumulating across runs. This makes upper-tier time missions (60 min flight time) functionally impossible. |
| **Recommendation** | `updateProgressAll()` should use `progressionManager.lifetime*` values for categories that represent cumulative totals. Build `GameStats` by combining run-local deltas with lifetime baselines. |

---

### Finding 3: `lifetimeMissionsCompleted` is not properly persisted

| | |
| :--- | :--- |
| **Status** | **CONFIRMED — CRITICAL** |
| **Evidence** | `ProgressionManager.kt:88` — Loads `lifetimeMissionsCompleted` from key `missions_completed`. `ProgressionManager.kt:161-167` — `recordMissionCompletion()` sets in-memory value (`lifetimeMissionsCompleted = completedMissionIds.size`) but **never writes** back to the `missions_completed` SharedPreferences key. The StringSet `completed_missions` IS written, but the scalar `missions_completed` is only read at init. |
| **Impact** | After app restart, `lifetimeMissionsCompleted` reverts to the stale value in `missions_completed` (or 0 if never saved). Module unlock logic using `UnlockType.MISSION` queries `missionsCompleted` which relies on `claimedMissionIds.size` (correct in-memory). |
| **Recommendation** | Either (a) remove the redundant `missions_completed` key and derive entirely from `claimedMissionIds.size`/`completedMissionIds.size`, or (b) persist `lifetimeMissionsCompleted` during `recordMissionCompletion()`. |

---

### Finding 4: Hangar mission counter uses claimed count

| | |
| :--- | :--- |
| **Status** | **CONFIRMED — MAJOR** |
| **Evidence** | `ProgressionManager.kt:56` — `val missionsCompleted: Int get() = claimedMissionIds.size`. `HangarScreen.kt:149` — Display: `"${progressionManager.missionsCompleted} DONE >"`. The label "DONE" implies completed missions, but the counter counts only claimed missions. |
| **Impact** | If a player has completed 8 missions but only claimed 5, the counter shows "5 DONE" instead of "8 DONE". This is misleading and inconsistent with the `MissionScreen` dashboard which distinguishes completed vs claimed. |
| **Recommendation** | Change `missionsCompleted` to return `completedMissionIds.size`, or display both counts (e.g., `"${claimedMissionIds.size}/${completedMissionIds.size} CLAIMED"`). |

---

### Finding 5: Mission reward display only shows first reward

| | |
| :--- | :--- |
| **Status** | **CONFIRMED — MINOR** |
| **Evidence** | `MissionScreen.kt:211` — `val rewardText = mission.rewards.firstOrNull()?.let { ... }`. `MissionRegistry.kt` — Many missions define 2 rewards (e.g., `listOf(MissionReward.Cash(100), MissionReward.Artifact(...))`). |
| **Impact** | Players can only see the first reward in the TrackRow UI. However, `claimMissionRewards()` correctly iterates ALL rewards (`mission.rewards.forEach`), so claiming still grants all rewards correctly. |
| **Recommendation** | Display all rewards (e.g., comma-separated list), or show a "+N more" indicator for multi-reward missions. |

---

## 2. Additional Findings

### 2.1 Double-counting of `lifetimeArtifactsCollected` on session commit

| | |
| :--- | :--- |
| **Severity** | **MAJOR** |
| **Evidence** | `GameScreen.kt:976` — `stats.artifactsCollected` is set to `progressionManager.artifactsCollected.size` (total **lifetime** artifact types discovered). `ProgressionManager.kt:182` — `commitSessionStats` does `lifetimeArtifactsCollected += stats.artifactsCollected`. This adds the entire lifetime count on every session end. |
| **Impact** | `lifetimeArtifactsCollected` inflates with each game session. After 10 sessions with 3 artifacts, it reads 30 instead of 3. This field is not used in current unlock logic but is incorrect state. |
| **Recommendation** | `stats.artifactsCollected` should track only artifacts **collected during this run**, not the lifetime total. Add a run-local counter `runArtifactsCollected` in `GameScreen`. |

---

### 2.2 Missing `commitSessionStats` persistence of `lifetimeMissionsCompleted`

| | |
| :--- | :--- |
| **Severity** | **MAJOR** |
| **Evidence** | `ProgressionManager.kt:178-192` — `commitSessionStats()` persists flight time, platform time, bosses, artifacts, hazards — but NOT `lifetimeMissionsCompleted`. No mission completion data is written here. |
| **Impact** | `lifetimeMissionsCompleted` has no backup write path. |
| **Recommendation** | Add `lifetimeMissionsCompleted` persistence to `commitSessionStats()`. |

---

### 2.3 Run-local variables inflate cumulative missions

| | |
| :--- | :--- |
| **Severity** | **MAJOR** |
| **Evidence** | `GameScreen.kt:961-981` — `GameStats` is rebuilt every frame using run-local timers. `airborneTimer` resets to 0 on `restartGame()`. `updateProgressAll()` passes this to `calculateProgress()`, which uses `stats.totalFlightTime` for FLIGHT_TIME missions. |
| **Impact** | A player with 30 min lifetime flight time across 6 runs sees progress as only the current run's flight time (5 min) when evaluating "Spend 12 minutes total in the air". This makes cumulative missions impossible to complete through normal multi-run play. |
| **Recommendation** | For cumulative categories (FLIGHT_TIME, PLATFORM_STAY, NO_HEAT, PERFECT_RUN), `calculateProgress()` should use `progressionManager.lifetime* + stats.*` or similar. |

---

### 2.4 Navigation Audit

| Route | From | Back To | Status |
| :--- | :--- | :--- | :--- |
| TITLE | TitleScreen | MAIN_MENU | ✅ |
| MAIN_MENU | MainMenuScreen | Various via `onNavigate` | ✅ |
| HANGAR | HangarScreen | MAIN_MENU ("RETURN TO COMMAND") | ✅ |
| MISSIONS | MissionScreen | `previousState` (supports HANGAR/MENU back) | ✅ |
| LOADOUT | LoadoutScreen | HANGAR ("BACK TO HANGAR") | ✅ |
| ARCHIVE | ArchiveScreen | MAIN_MENU | ✅ |
| ABOUT (Protocol) | AboutScreen | MAIN_MENU ("DISMISS") | ✅ |
| SETTINGS | SettingsScreen | MAIN_MENU (onReturn) | ✅ |
| LEADERBOARD | LeaderboardScreen | MAIN_MENU | ✅ |
| PLAYING/PAUSED | GameScreen | Via overlays | ✅ |
| GAMEOVER | GameOverOverlay | MAIN_MENU | ✅ |

**All navigation routes verified as correct.** Protocol screen properly separated as `AboutScreen.kt` ("SYSTEM PROTOCOL") per design.

---

### 2.5 Shared Object Mutation Risk

| | |
| :--- | :--- |
| **Severity** | **TECH DEBT** |
| **Evidence** | `MissionManager.kt:146` — `activateMission()` adds the **same reference** from `allMissionInstances` to `activeMissions`. Both collections point to the same `Mission` object. |
| **Impact** | Mutations through `activeMissions` directly modify the in-memory state in `allMissionInstances`. By design, but couples HUD display with persistence layer. |
| **Recommendation** | Create a defensive copy if separation of concerns becomes necessary. |

---

### 2.6 `MissionManager.syncState()` not called on all entry paths

| | |
| :--- | :--- |
| **Severity** | **MINOR** |
| **Evidence** | `MissionScreen.kt:57-59` — `syncState()` is called in `LaunchedEffect(Unit)` only when MissionScreen is composed. No other call sites. |
| **Impact** | Stale display if mission state changes outside of MissionScreen visits. Unlikely in current flow but fragile. |
| **Recommendation** | Call `syncState()` after `recordMissionCompletion` in `GameScreen.kt` or make it reactive. |

---

## 3. Summary

### By Severity

| Severity | Count | Findings |
| :--- | :---: | :--- |
| **CRITICAL** | 2 | #1 (currentProgress not persisted), #3 (lifetimeMissionsCompleted not persisted) |
| **MAJOR** | 4 | #2 (run stats for lifetime missions), #4 (claimed vs completed counter), 2.1 (artifact double-count), 2.3 (run vars inflate cumulative) |
| **MINOR** | 3 | #5 (first reward only), 2.2 (missing commit path), 2.6 (syncState entry paths) |
| **TECH DEBT** | 1 | 2.5 (shared object mutation) |

### Verdict

**EPIC 8 is structurally sound but contains 2 CRITICAL persistence defects and 4 MAJOR logic issues that must be resolved before release.**

The mission architecture (12-track system, tier progression, ceremony flow, claim pipeline) is well-designed and correctly implemented. The Intelligence Network hooks into the physics loop correctly. Navigation and protocol separation meet the design spec.

However, the persistence layer has significant gaps:
- **Mission progress is entirely in-memory** — any app restart loses player progress
- **Cumulative missions don't accumulate across runs** — time-based missions require single-run completion
- **Lifetime stats tracking has double-counting bugs** — artifact count inflates on each session commit
- **Mission counter is misleading** — shows claimed count instead of completed count

**Recommendation:** Fix the CRITICAL and MAJOR items before proceeding to Phase 6 (Hidden Signals & Dynamic Unlocks). The architecture itself does not need rework, only the persistence wiring.

---

## 4. Files Referenced

| File | Purpose |
| :--- | :--- |
| `app/.../Mission.kt` | Mission data model with mutable state |
| `app/.../MissionManager.kt` | Mission lifecycle, progress updates, claim flow |
| `app/.../MissionRegistry.kt` | 50 mission template definitions |
| `app/.../MissionReward.kt` | Sealed reward classes |
| `app/.../MissionType.kt` | 6 mission type enum |
| `app/.../MissionScreen.kt` | 12-track dashboard with claim flow |
| `app/.../MissionRow.kt` | HUD mission card composable |
| `app/.../ProgressionManager.kt` | Persistence, lifetime stats, unlock logic |
| `app/.../GameStats.kt` | Run-local stat snapshot schema |
| `app/.../GameScreen.kt` | Main game loop, stat collection, mission hooks |
| `app/.../HangarScreen.kt` | Mission counter display |
| `app/.../LoadoutScreen.kt` | Module loadout with back navigation |
| `app/.../Models.kt` | GameState enum (14 states) |
| `app/.../MainMenuScreen.kt` | Command Center navigation |
| `app/.../AboutScreen.kt` | SYSTEM PROTOCOL (separated per design) |
| `docs/analysis/MISSION_ARCHITECTURE_AUDIT.md` | Mission design documentation |
| `docs/analysis/Progression_Architecture_Audit.md` | Progression design documentation |
| `docs/roadmap/EPIC_8_TRACKER.md` | Sprint tracking |
| `docs/CHANGELOG.md` | Project changelog |
| `AGENTS.md` | Project governance |
