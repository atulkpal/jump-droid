# EPIC 8.5 — Architecture Decomposition Master Blueprint

**Author:** AI Agent (OpenCode)
**Date:** 2026-06-23
**Status:** Planning Complete — Ready for Sprint 8.5.0 Execution
**Branch:** `epic8-mission-migration`
**Base Tag:** `epic8-recovery-stable` (`afbc562`)
**Primary Doc:** `docs/roadmap/EPIC_8_5_DECOMPOSITION_PLAN.md`
**Audit Basis:** `docs/analysis/EPIC8_TECH_DEBT_AUDIT.md`
**Coverage Matrix:** `docs/analysis/EPIC8_5_FINDING_COVERAGE_MATRIX.md`

---

## Table of Contents

1. [Purpose & Scope](#1-purpose--scope)
2. [Current Architecture Map](#2-current-architecture-map)
3. [Target Architecture Map](#3-target-architecture-map)
4. [Sprint Execution Plan](#4-sprint-execution-plan)
   - [Sprint 8.5.0 — Baseline Capture](#sprint-850--baseline-capture)
   - [Sprint 8.5.1 — Low-Risk Cleanup](#sprint-851--low-risk-cleanup)
   - [Sprint 8.5.2 — HUD Decomposition](#sprint-852--hud-decomposition)
   - [Sprint 8.5.3 — Notification & Celebration Extraction](#sprint-853--notification--celebration-extraction)
   - [Sprint 8.5.4 — Threat Rendering Extraction](#sprint-854--threat-rendering-extraction)
   - [Sprint 8.5.5 — Game Engine Boundary Creation](#sprint-855--game-engine-boundary-creation)
   - [Sprint 8.5.6 — ActiveThreat Strategy Architecture](#sprint-856--activethreat-strategy-architecture)
   - [Sprint 8.5.7 — Progression Service Decomposition](#sprint-857--progression-service-decomposition)
   - [Sprint 8.5.8 — Navigation Migration](#sprint-858--navigation-migration)
5. [Sprint Sequence Analysis](#5-sprint-sequence-analysis)
6. [Ownership Boundaries](#6-ownership-boundaries)
7. [Dependency Diagrams](#7-dependency-diagrams)
8. [Risk Register](#8-risk-register)
9. [Testing Matrix](#9-testing-matrix)
10. [Metrics Dashboard](#10-metrics-dashboard)
11. [EPIC Completion Criteria](#11-epic-completion-criteria)
12. [EPIC 9 Handoff Requirements](#12-epic-9-handoff-requirements)

---

## 1. Purpose & Scope

### Problem
Two God Objects (GameScreen.kt at 3,901 lines, ActiveThreat.kt at 1,224 lines) block all Phase 9 feature work. ProgressionManager violates SRP with 7+ subdomains. Navigation uses a fragile state-based `when` block with no NavHost. Threat rendering is inline inside GameScreen's Canvas block (~1,947 lines).

### Goal
Decompose these God Objects into focused, independently testable components before EPIC 9 feature work begins.

### Out of Scope
- No gameplay changes
- No content additions (missions, threats, bosses, items)
- No Hidden Signals or Dynamic Unlocks
- No audio system
- No balance changes

### Success Criteria
1. Baseline tag `epic8.5-baseline` created with full metric capture
2. `GameScreen.kt` < 1,800 lines
3. `ActiveThreat.kt` < 350 lines
4. All existing managers retain identical public API (backward compatible)
5. `./gradlew assembleDebug` — `BUILD SUCCESSFUL`
6. All gameplay behavior unchanged (verified by smoke test)
7. Zero dead code files in production source tree
8. `ProgressionManager` split into ≤ 4 focused services

---

## 2. Current Architecture Map

### System Relationship Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                           MainActivity                                        │
│                     (ComponentActivity + setContent)                          │
└──────────────────────────────────┬───────────────────────────────────────────┘
                                   │
                                   ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                   GameScreen.kt  (3,901 lines — 70 imports)                  │
│                                                                              │
│  ┌──────────────────────┐    ┌──────────────────────┐                       │
│  │   State Declaration   │    │     Game Loop         │                      │
│  │   (~110 lines)        │    │   LaunchedEffect       │                     │
│  │   27 remember blocks  │    │   withFrameNanos       │                     │
│  │   17 managers         │    │   (~810 lines)         │                     │
│  └──────────────────────┘    └──────────┬─────────────┘                     │
│                                         │                                    │
│                                         ▼                                    │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │                   Physics & Update Orchestration                      │   │
│  │  Manages: dt calc, AI updates, sub-stepped physics (4x), collision,   │   │
│  │  score, zones, platform lifecycle, particles, ceremonies              │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
│                                         │                                    │
│                                         ▼                                    │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │                Canvas Rendering (~2,075 lines)                        │   │
│  │  ├── ZoneBackgroundRenderer (delegated)                                │   │
│  │  ├── Threat Rendering (~1,947 lines INLINE — 26 types)                │   │
│  │  ├── CanvasEffects (delegated — 8 effects)                             │   │
│  │  ├── PlatformRenderer (delegated)                                      │   │
│  │  ├── RocketRenderer (delegated)                                        │   │
│  │  └── AmbientSystem (delegated)                                        │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
│                                         │                                    │
│                                         ▼                                    │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │              UI Layer (~220 lines)                                    │   │
│  │  ┌─────────────────────┐  ┌──────────────────────┐                    │   │
│  │  │  when(gameState)     │  │  HUD Widgets inline   │                   │   │
│  │  │  14 branches         │  │  + extracted files     │                  │   │
│  │  │  No NavHost          │  │                       │                    │   │
│  │  │  No back stack       │  │                       │                    │   │
│  │  └─────────────────────┘  └──────────────────────┘                    │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
│  Managers (instantiated in GameScreen scope):                                │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐          │
│  │Progression│ │ Mission  │ │  Combo   │ │Survival  │ │Encounter │          │
│  │Manager   │ │ Manager  │ │ Manager  │ │ Manager  │ │ Director │          │
│  │(334 ln)  │ │(241 ln)  │ │(moderate)│ │(moderate)│ │(227 ln)  │          │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘          │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐          │
│  │Platform  │ │Notifictn │ │ FloatText│ │ Projectl │ │ InputBuf │          │
│  │ Manager  │ │ Manager  │ │ Manager  │ │ Manager  │ │ Manager  │          │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘          │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐                                    │
│  │ Altitude │ │Discovery │ │ Loadout  │                                    │
│  │ Manager  │ │ Manager  │ │ Manager  │                                    │
│  └──────────┘ └──────────┘ └──────────┘                                    │
└──────────────────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                     ActiveThreat.kt  (1,224 lines)                          │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │  State Fields (52 lines — 30+ mutable properties)                     │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │  update() (~605 lines)                                               │   │
│  │  ├── Life cycle management (formation → active → dissipation)         │   │
│  │  ├── Per-type AI behavior (9 hazards × ~20 lines,                     │   │
│  │  │   5 enemies × ~40 lines, 6 bosses × ~50 lines)                   │   │
│  │  └── All inline — new threat = modify this function                   │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │  processInteraction() (~547 lines)                                    │   │
│  │  ├── Signature: 17 callback parameters                                │   │
│  │  ├── Collision logic for each threat type (inline)                    │   │
│  │  ├── Visual feedback + particle spawning (inline)                     │   │
│  │  ├── Boss phase mechanics (inline)                                    │   │
│  │  └── New threat = add callback parameter + inline logic               │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                     ThreatManager.kt  (runtime lifecycle)                    │
│                     ThreatRegistry.kt  (catalog)                             │
│                     ThreatType.kt  (enum)                                    │
│                     ThreatDefinition.kt  (data class)                        │
│                     ThreatState.kt  (enum)                                    │
│                     ThreatSpawnRules.kt  (data class)                        │
│                     ThreatTier.kt  (enum)                                     │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Current File Sizes

| File | Lines | Risk |
|------|-------|------|
| GameScreen.kt | 3,901 | 🔴 CRITICAL |
| ActiveThreat.kt | 1,224 | 🔴 CRITICAL |
| HudWidgets.kt | 654 | 🟡 Moderate |
| ProgressionManager.kt | 334 | 🟡 Moderate |
| MissionManager.kt | 241 | 🟡 Moderate |
| EncounterDirector.kt | 227 | 🟡 Moderate |
| **Median file** | **99** | 🟢 Healthy |

### Current Import Counts

| File | Imports |
|------|---------|
| GameScreen.kt | 70 |
| ActiveThreat.kt | 9 |
| ProgressionManager.kt | TBD in 8.5.0 |

---

## 3. Target Architecture Map

### Post-EPIC 8.5 System Relationship Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                           MainActivity                                       │
└──────────────────────────────────┬───────────────────────────────────────────┘
                                   │
                                   ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                     NavHost (replaces when(gameState))                       │
│                                                                              │
│  Routes: TITLE, MAIN_MENU, HANGAR, LOADOUT, ARCHIVE, SETTINGS, ABOUT,       │
│          MISSIONS, LEADERBOARD, PLAYING (with overlays)                     │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │  GameScreen.kt (~1,500 lines) — Compose orchestration only          │   │
│  │                                                                      │   │
│  │  ┌─────────────────────┐  ┌──────────────────┐                      │   │
│  │  │ State (reduced)     │  │ UI Orchestration  │                      │   │
│  │  │ Minimal composable  │  │ + Canvas wrapper  │                      │   │
│  │  │ state only          │  │ + overlay dispatch │                      │   │
│  │  └─────────────────────┘  └──────────────────┘                      │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────────────┘
                                   │
           ┌───────────────────────┼───────────────────────┐
           ▼                       ▼                       ▼
┌──────────────────────┐ ┌──────────────────┐ ┌──────────────────────────┐
│   GameEngine.kt      │ │  ThreatRenderer  │ │  Per-Screen Composables  │
│   (new — extracted)  │ │  Interface + 20  │ │  (already extracted)      │
│                      │ │  implementors    │ │                           │
│  Physics loop        │ │  (extracted from │ │  HudWidgets.kt ↓          │
│  Frame timing        │ │  GameScreen      │ │  GaugeBar.kt (new)       │
│  Update orchestration│ │  Canvas)         │ │  HudState.kt (new)       │
│  ┌────────────────┐   │ └──────────────────┘ │  StarfieldBackground.kt  │
│  │ CollisionSystem │  │                      │  (new — replaces 6x     │
│  │ (extracted from │  │                      │   copy-paste)           │
│  │  ActiveThreat)  │  │                      └──────────────────────────┘
│  └────────────────┘   │
│  ┌────────────────┐   │
│  │ EventBus       │   │
│  │ (replaces 17   │   │
│  │  callbacks)    │   │
│  └────────────────┘   │
└──────────────────────┘
           │
           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                     ActiveThreat.kt (~250 lines)                            │
│                                                                              │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────────────────┐          │
│  │ State Fields   │  │ update()       │  │ processInteraction() │          │
│  │ (reduced)      │  │ (delegates to  │  │ (delegates to event  │          │
│  │                 │  │  ThreatBehavior│  │  bus — no more 17    │          │
│  │                 │  │  strategies)   │  │  callback params)    │          │
│  └────────────────┘  └────────────────┘  └──────────────────────┘          │
└──────────────────────────────────────────────────────────────────────────────┘
           │
           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                     ThreatBehavior Interface + 20 Strategy Classes          │
│                                                                              │
│  ├── Hazards (9): RADIATION, SOLAR_FLARE, EMP, GRAVITY, VOID_ANOMALY,       │
│  │                LIGHTNING, TURBULENCE, DEBRIS, CRYO_MIST                  │
│  ├── Enemies (5): SCOUT_DRONE, SWARM_BOTS, ORBITAL_SENTRY, STALKER,         │
│  │                VOID_WRAITH, VOID_WHALE                                   │
│  └── Bosses (6): COMMANDER, GATEKEEPER, LEVIATHAN, VOID_ENGINE, SIGNAL,     │
│                   STAR_EATER                                                │
└──────────────────────────────────────────────────────────────────────────────┘
           │
           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                  Progression Services (decomposed)                          │
│                                                                              │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐                │
│  │Intelligence     │  │ UnlockService  │  │ MissionTracker │                │
│  │Network          │  │ (data-driven   │  │ (progress,     │                │
│  │(lifetime stats) │  │  conditions)   │  │  completions)  │                │
│  └────────────────┘  └────────────────┘  └────────────────┘                │
│  ┌────────────────┐  ┌────────────────┐                                    │
│  │InventoryManager│  │ RewardService  │                                    │
│  │(module owner)  │  │ (routing)      │                                    │
│  └────────────────┘  └────────────────┘                                    │
│                                                                              │
│  ProgressionManager.kt — slim facade (~80 lines)                            │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Target File Sizes

| File | Before | After | Reduction |
|------|--------|-------|-----------|
| GameScreen.kt | 3,901 lines | ~1,500 | 62% |
| ActiveThreat.kt | 1,224 lines | ~250 | 80% |
| ProgressionManager.kt | 334 lines | ~80 (facade) | 76% |
| HudWidgets.kt | 654 lines | ~400 | 39% |
| MissionRow.kt | 140 lines | 0 (deleted) | 100% |

New files created: ~25 (renderers, strategies, services, composables)
Total codebase change: ~14,800 → ~14,500 lines (reduction despite more files)

---

## 4. Sprint Execution Plan

---

### Sprint 8.5.0 — Baseline Capture

**Purpose:** Establish a known-good EPIC 8 baseline before any decomposition work begins. This ensures every subsequent change can be measured and compared against a frozen starting point.

**Scope:**
- Create `epic8.5-baseline` git tag
- Run `./gradlew assembleDebug` — confirm `BUILD SUCCESSFUL`
- Record line counts for all key files
- Count imports, functions, callback parameters, StateList usages per file
- Capture screenshots of all screens
- Produce baseline report

**Findings Addressed:** None directly — this is a prerequisite for all other findings.

**Files Affected:** None (read-only)

**New Files Created:**
- `docs/analysis/EPIC8_5_BASELINE_REPORT.md`

**Dependencies:** EPIC 8 validation signoff

**Architecture Changes:** None

**Migration Strategy:** N/A (read-only capture)

**Validation Strategy:**
- `./gradlew assembleDebug` must pass
- All screenshot filenames match screen names
- File size inventory is recorded for all key files

**Rollback Strategy:** Delete tag if capture is incomplete. Re-run.

**Risks:** None. Read-only operation.

**Exit Criteria:**
- `epic8.5-baseline` tag created at current HEAD
- Build verified successful
- Baseline report written with all metrics
- Screenshots saved to `docs/screenshots/EPIC8_BASELINE/`
- Zero production files modified

**Success Metrics:**
- Tag exists at correct commit
- Build log shows zero errors
- All 15 metrics in baseline report are populated

---

### Sprint 8.5.1 — Low-Risk Cleanup

**Purpose:** Eliminate dead code, zombie fields, and impure functions with zero regression risk. These are safe, mechanical changes that improve code quality without touching any logic.

**Scope:**
1. Delete `MissionRow.kt` (140 lines, zero imports)
2. Remove `isNew` zombie field from `Mission.kt`
3. Remove unused `CeremonyStage` values (`COMPLETED_TEXT`, `REWARD_SPAWNED`)
4. Make `checkCompletion()` a pure function (move `ceremonyStage` side effect to caller)
5. Remove redundant `progressionManager` parameter from `claimMissionRewards()`
6. Remove dead `MissionType.COMBO` enum value

**Findings Addressed:**
- FINDING-06 (Ceremony scattered) — partially: removes unused values, makes checkCompletion pure
- FINDING-07 (Mission UI/domain mix) — partially: removes `isNew` zombie
- FINDING-11 (MissionManager coupling) — partially: removes redundant parameter
- FINDING-14 (MissionRow dead code) — fully: deletes file

**Files Affected:**
- `MissionRow.kt` — DELETE
- `Mission.kt` — remove `isNew`, `ceremonyStage` values, `reset()` cleanup
- `MissionType.kt` — remove `COMBO`
- `GameScreen.kt` — update ceremony references, remove `Combo` mission references
- `MissionManager.kt` — remove redundant param from `claimMissionRewards`
- MissionRegistry.kt — audit for `MissionType.COMBO` references

**New Files Created:** None

**Dependencies:** Sprint 8.5.0 (baseline tag)

**Architecture Changes:**
- `CeremonyStage` collapses from 5 to 3 values (`NONE`, `GLOW`, `REPLACING`)
- `Mission.checkCompletion()` no longer mutates ceremony state — ceremony is set by caller
- `MissionRow.kt` removed from source tree

**Migration Strategy:**
1. Delete `MissionRow.kt` — verify zero compile errors (grep for imports)
2. Remove `isNew` field from `Mission.kt` — grep all references, remove
3. Remove unused `CeremonyStage` values — grep for `COMPLETED_TEXT` and `REWARD_SPAWNED`
4. Extract ceremony state mutation from `checkCompletion()` — move to call site
5. Remove redundant param from `claimMissionRewards`
6. Remove `MissionType.COMBO` — update icon mapping in GameScreen

**Validation Strategy:**
- `./gradlew assembleDebug` — must pass after each of the 6 items
- `rg "MissionRow" --type kt` — zero results after delete
- `rg "isNew" --type kt` — zero results after removal
- `rg "COMPLETED_TEXT\|REWARD_SPAWNED"` — zero results

**Rollback Strategy:** Each item is an independent commit. If any item fails, revert that single commit.

**Risks:** None. All 6 items are dead code or mechanical refactors with zero behavioral impact.

**Exit Criteria:**
- `MissionRow.kt` deleted from filesystem
- `isNew` field removed from `Mission.kt`
- `CeremonyStage` has exactly 3 values
- `checkCompletion()` is a pure function (no side effects)
- `claimMissionRewards()` has single ProgressionManager reference (constructor)
- `MissionType.COMBO` removed
- Build successful

**Success Metrics:**
- Lines removed: ~170 (140 MissionRow + 10 isNew + 10 ceremony + 5 param + 5 COMBO)
- File count reduced by 1

---

### Sprint 8.5.2 — HUD Decomposition

**Purpose:** Reduce duplication in HudWidgets.kt by extracting a base `GaugeBar` composable. Extract `StarfieldBackground` to eliminate 6x copy-paste. Move inline track selection logic from MissionScreen to MissionManager for testability.

**Scope:**
1. Extract `GaugeBar` base composable from 4 nearly-identical gauge implementations (Fuel, Heat, Shield, Integrity)
2. Hoist cross-cutting HUD state into `HudState` data class
3. Extract `StarfieldBackground` composable — replace 6 copy-paste instances
4. Extract MissionScreen inline track selection/filtering into `MissionManager`

**Findings Addressed:**
- FINDING-12 (Star duplicated 6x) — fully: single composable
- FINDING-16 (Inline MissionScreen logic) — fully: extracted to MissionManager
- FINDING-17 (Duplicate gauge structure) — fully: GaugeBar composable

**Files Affected:**
- `HudWidgets.kt` — replace 4 inline gauges with `GaugeBar` calls; add `HudState` parameter
- `MissionScreen.kt` — replace inline filter/sort with `getCurrentMissionForTrack()` call
- `MissionManager.kt` — add `getCurrentMissionForTrack(categories)` method
- `MainMenuScreen.kt`, `PauseOverlay.kt`, `GameOverOverlay.kt`, `AboutScreen.kt`, `SettingsScreen.kt`, `HangarScreen.kt` — replace star animation with `StarfieldBackground` composable

**New Files Created:**
- `GaugeBar.kt` — base composable with configurable color, icon, value, percentage
- `HudState.kt` — data class for `gameTime`, `interferenceTimer`, `zone`
- `StarfieldBackground.kt` — reusable starry background composable

**Dependencies:** Sprint 8.5.1 (clean baseline)

**Architecture Changes:**
- HudWidgets.kt switches from 4 independent gauge implementations to 1 reusable `GaugeBar` called 4 times with different configs
- 6 screens replace inline star LaunchedEffect with single `StarfieldBackground` import
- HudWidgets composables now accept `HudState` instead of individual parameters for cross-cutting state

**Migration Strategy:**
1. Create `GaugeBar.kt` with the shared implementation
2. Refactor each gauge site in `HudWidgets.kt` to use `GaugeBar`
3. Verify gauges render identically (screenshot comparison)
4. Create `HudState.kt`
5. Update HudWidgets signatures
6. Create `StarfieldBackground.kt`
7. Replace inline star code in all 6 screens — verify each screen still renders
8. Add `getCurrentMissionForTrack()` to `MissionManager`
9. Replace inline filter in `MissionScreen` with call to new method

**Validation Strategy:**
- Visual comparison: before/after screenshots for all 4 gauges
- Visual comparison: before/after for all 6 screens with starfield
- Unit test: `MissionManager.getCurrentMissionForTrack()` returns correct mission
- `./gradlew assembleDebug` after each major step

**Rollback Strategy:** GaugeBar and StarfieldBackground extractions are independent. Revert broken extraction, keep working ones.

**Risks:** Low. GaugeBar extraction is mechanical. Starfield replacement is copy-replace. MissionScreen extraction is a simple method move.

**Exit Criteria:**
- `GaugeBar` composable used by all 4 gauge sites
- `HudState` defined and used where applicable
- 0 inline star animations (all use `StarfieldBackground`)
- MissionScreen track selection calls `MissionManager.getCurrentMissionForTrack()`
- All screens render identically to baseline

**Success Metrics:**
- HudWidgets.kt reduced from 654 to ~400 lines
- 6 files each reduced by ~20 lines (star removal)
- 1 new file (StarfieldBackground)
- Gauge copy-paste eliminated

---

### Sprint 8.5.3 — Notification & Celebration Extraction

**Purpose:** Clean mission ceremony lifecycle by extracting timer management from GameScreen into MissionManager. Unify ComboManager's dual reward systems into one table. Define a `ProgressionService` interface to decouple MissionManager from ProgressionManager.

**Scope:**
1. Move ceremony lifecycle (GLOW → REPLACING timer transitions) from GameScreen to `MissionManager`
2. Merge ComboManager tier rewards + survival rewards into one reward table
3. Externalize combo reward table to data config
4. Define `ProgressionService` interface — MissionManager depends on interface, not concrete ProgressionManager

**Findings Addressed:**
- FINDING-06 (Ceremony scattered) — fully: ceremony lifecycle extracted from GameScreen
- FINDING-09 (ComboManager dual rewards) — fully: unified into single table
- FINDING-11 (MissionManager coupling) — fully: `ProgressionService` interface

**Files Affected:**
- `GameScreen.kt` — remove inline ceremony timer logic (~30 lines); reference `MissionManager.ceremonyTimers`
- `MissionManager.kt` — add ceremony lifecycle methods; implement `ProgressionService` consumer
- `ComboManager.kt` — merge reward systems, externalize table
- `ProgressionManager.kt` — optionally implement `ProgressionService` interface

**New Files Created:**
- `ProgressionService.kt` — interface with methods MissionManager needs
- `ComboRewardConfig.kt` or similar — externalized reward data

**Dependencies:** Sprint 8.5.1 (cleanup made checkCompletion pure), Sprint 8.5.2 (optional — no hard dependency)

**Architecture Changes:**
- Ceremony lifecycle state moves from GameScreen (inline timer map) to MissionManager (managed via `ceremonyTimers` map + `updateCeremonies(dt)`)
- MissionManager accesses progression only through `ProgressionService` interface
- ComboManager's `tiers` and `immediateSurvivalRewards` merged into single `rewardTable`

**Migration Strategy:**
1. Define `ProgressionService` interface with methods used by MissionManager
2. Update MissionManager to take `ProgressionService` instead of concrete `ProgressionManager`
3. Have `ProgressionManager` implement `ProgressionService` (or create adapter)
4. Move ceremony timer map from GameScreen into MissionManager
5. Add `updateCeremonies(dt)` to MissionManager
6. Replace inline ceremony timer code in GameScreen with MissionManager call
7. Merge ComboManager reward systems
8. Externalize reward data

**Validation Strategy:**
- Run game: verify ceremony sequence (GLOW → REPLACING) still plays identically
- Run game: verify combo rewards still fire at 5/10/15/20
- Run game: verify mission progress still tracked
- `./gradlew assembleDebug`

**Rollback Strategy:** Each item is independent. Merge after each works.

**Risks:** Low-Medium. Ceremony lifecycle is visual — if timing changes, it may look different but gameplay is unaffected.

**Exit Criteria:**
- MissionManager owns ceremony lifecycle
- MissionManager depends on `ProgressionService`, not concrete `ProgressionManager`
- ComboManager has one reward table
- Build successful
- Visual ceremony behavior unchanged

**Success Metrics:**
- GameScreen.kt reduced by ~30 lines
- MissionManager coupling reduced by 1 dependency (from concrete to interface)
- ComboManager code simplified by merging two paths

---

### Sprint 8.5.4 — Threat Rendering Extraction

**Purpose:** Extract ~1,947 lines of inline threat rendering from GameScreen's Canvas block into per-type renderers. This is the first major extraction targeting the GameScreen God Object.

**Scope:**
1. Define `ThreatRenderer` interface with `DrawScope.renderThreat(threat)` method
2. Extract 9 hazard renderers
3. Extract 5 enemy renderers
4. Extract 6 boss renderers
5. Replace inline Canvas block with single dispatch call
6. Eliminate `MissionType.toIcon()` extension (move to config)

**Findings Addressed:**
- FINDING-01 (GameScreen God Object) — partially: removes 1,947 lines from GameScreen
- FINDING-05 (Threat rendering inline) — fully: extracted to per-type renderers

**Files Affected:**
- `GameScreen.kt` — replace ~1,947-line Canvas block with delegate call
- New renderer files (see below)

**New Files Created:**
- `ThreatRenderer.kt` — interface definition
- `HazardRenderer.kt` — dispatcher for 9 hazards + base helpers
- `EnemyRenderer.kt` — dispatcher for 5 enemies + base helpers
- `BossRenderer.kt` — dispatcher for 6 bosses + base helpers
- Or alternately: one file per renderer (20 files)

**Dependencies:** Sprint 8.5.3 (optional — ceremony extraction not required)

**Architecture Changes:**
- GameScreen's Canvas block calls `renderer.drawThreat(threat, drawScope)` instead of inline draw calls
- Each renderer is a standalone `DrawScope` extension or class

**Migration Strategy:**
1. Define `ThreatRenderer` interface
2. Extract one hazard renderer first (e.g., RADIATION — simplest) — verify rendering
3. Extract all remaining hazards using the same pattern
4. Extract enemy renderers
5. Extract boss renderers
6. Replace inline block with single dispatch

**Validation Strategy:**
- Visual comparison: screenshot rendering for each threat type before/after
- Each renderer extracted independently and verified
- `./gradlew assembleDebug` after each renderer group

**Rollback Strategy:** Each renderer is independent. If boss rendering breaks, revert boss files but keep hazard/enemy ones.

**Risks:** Medium. 26 threat types, some with complex procedural rendering (BOSS_SIGNAL phase transitions, VOID_ENGINE gravity pulses). Rendering must look pixel-identical.

**Exit Criteria:**
- `ThreatRenderer` interface defined
- 20 renderer implementations (9 hazard, 5 enemy, 6 boss)
- GameScreen Canvas block has zero inline threat drawing
- All 26 threat types render identically to baseline screenshots

**Success Metrics:**
- GameScreen.kt reduced from 3,901 to ~2,200 lines
- New files: 20+ renderers
- Threat rendering is independently testable

---

### Sprint 8.5.5 — Game Engine Boundary Creation

**Purpose:** Extract the game loop (physics, frame timing, update orchestration) from GameScreen into a standalone `GameEngine` class. This is the highest-value extraction — it separates game logic from Compose rendering.

**Scope:**
1. Define `GameEngine` class encapsulating `LaunchedEffect` + `withFrameNanos` loop
2. Extract 4-step physics sub-routine into engine method
3. Extract entity update orchestration (AI → collision → score → lifecycle)
4. GameScreen retains only Composable rendering and UI orchestration

**Findings Addressed:**
- FINDING-01 (GameScreen God Object) — fully: moves core game loop out of GameScreen
- FINDING-02 (ActiveThreat God Object) — partially: interaction path established, but AI extraction is Sprint 8.5.6

**Files Affected:**
- `GameScreen.kt` — remove ~790 lines of game loop; reference `GameEngine` instead
- `Models.kt` or `GameEngine.kt` — define engine class

**New Files Created:**
- `GameEngine.kt` — game loop, physics, update orchestration, frame timing
- May need supporting data classes

**Dependencies:** Sprint 8.5.4 (must know the rendering interface before extracting engine boundary, because engine needs to call rendering dispatch)

**Architecture Changes:**
- GameEngine owns all per-frame logic
- GameScreen owns the `@Composable` structure and delegates rendering + UI
- GameEngine is a plain Kotlin class (no Compose dependencies) — independently testable

**Migration Strategy:**
1. Identify all per-frame operations in GameScreen (lines 795-1593)
2. Group into engine responsibilities: timing, physics, AI, collision, score, zone, lifecycle
3. Create `GameEngine` class with `update(dt)` method
4. Move timing logic first (easiest, most isolated)
5. Move physics sub-steps
6. Move update orchestration
7. GameScreen retains: Canvas call, UI overlay call, state declarations

**Validation Strategy:**
- Play game: verify physics feel is identical (same gravity, thrust, collision response)
- Play game: verify all 26 threat types still function
- Play game: verify mission progress, combo, score all track
- `./gradlew assembleDebug`

**Rollback Strategy:** Critical — extract incrementally. Commit after each subsection extraction. If physics timing changes, revert to last known-good commit and adjust.

**Risks:** Medium-High. This touches the most critical code path (physics loop). A bug breaks all gameplay.

**Exit Criteria:**
- `GameEngine` class defined with `update(dt)` method
- GameScreen.kt game loop removed (delegated to `GameEngine`)
- Physics behavior identical to baseline
- All gameplay unchanged
- Build successful

**Success Metrics:**
- GameScreen.kt reduced from ~2,200 to ~1,500 lines
- GameEngine.kt is a pure Kotlin class with zero Compose imports
- Game loop is independently unit-testable

---

### Sprint 8.5.6 — ActiveThreat Strategy Architecture

**Purpose:** Decompose ActiveThreat.kt by extracting per-threat AI strategies, collision system, and replacing the 17-callback `processInteraction` with an event bus.

**Scope:**
1. Define `ThreatBehavior` interface
2. Extract 9 hazard AI strategies
3. Extract 5 enemy AI strategies
4. Extract 6 boss AI strategies
5. Extract `CollisionSystem` from `processInteraction`
6. Replace 17-callback `processInteraction` with synchronous event bus
7. GameScreen wires event bus handlers instead of 17 lambda parameters

**Findings Addressed:**
- FINDING-02 (ActiveThreat God Object) — fully: strategies + collision + event bus

**Files Affected:**
- `ActiveThreat.kt` — remove ~1,100 lines (AI, collision, callbacks); retain state + dispatch
- `GameScreen.kt` — replace 17 callback lambdas with `eventBus.on<EventType> { ... }`
- New strategy files (see below)

**New Files Created:**
- `ThreatBehavior.kt` — interface definition
- 20 strategy files (or grouped: `HazardBehavior.kt`, `EnemyBehavior.kt`, `BossBehavior.kt`)
- `CollisionSystem.kt` — separated collision logic
- `EventBus.kt` — synchronous event dispatch

**Dependencies:** Sprint 8.5.4 (ThreatRenderer interface) and Sprint 8.5.5 (GameEngine boundary — event bus hooks into engine)

**Architecture Changes:**
- `ActiveThreat.kt` becomes a thin container: state fields + `currentBehavior: ThreatBehavior`
- `update()` calls `currentBehavior.update(this, dt, ...)`
- `processInteraction()` emits events via `EventBus`
- GameScreen subscribes to EventBus events instead of passing 17 callbacks
- `CollisionSystem` is a standalone utility class

**Migration Strategy:**
1. Define `ThreatBehavior` interface
2. Define `EventBus` class (synchronous, same-thread dispatch)
3. Extract one hazard strategy (e.g., RADIATION — simplest) — wire into ActiveThreat
4. Extract all remaining hazards
5. Extract enemy strategies
6. Extract boss strategies
7. Extract `CollisionSystem` from interaction logic
8. Replace callback wiring in GameScreen with EventBus handlers

**Validation Strategy:**
- Play game: verify all 26 threat types still exhibit correct AI behavior
- Play game: verify all collision effects (damage, heat, force, debuffs) still apply
- Play game: verify boss phases still transition correctly
- `./gradlew assembleDebug`

**Rollback Strategy:** Extract one threat type at a time. Commit after each. If event bus introduces bugs, revert EventBus commit but keep strategy extractions (they can still work with old callback pattern temporarily).

**Risks:** Medium. Event bus introduces a new dispatch mechanism. If misused, events could be missed or duplicated. Synchronous dispatch mitigates timing bugs.

**Exit Criteria:**
- `ThreatBehavior` interface defined with `update()` and `onCollision()`
- 20+ strategy implementations
- `CollisionSystem` extracted
- `EventBus` replaces 17-callback `processInteraction` signature
- ActiveThreat.kt < 350 lines
- All 26 threat types behave identically to baseline

**Success Metrics:**
- ActiveThreat.kt from 1,224 to ~250 lines
- `processInteraction()` callers drop from 17 params to 0-1 (EventBus)
- New threats: just add a `ThreatBehavior` class — no ActiveThreat changes

---

### Sprint 8.5.7 — Progression Service Decomposition

**Purpose:** Split ProgressionManager (334 lines, 7+ responsibilities) into focused domain services. This prevents the SRP violation from worsening when Dynamic Unlocks and Hidden Signals arrive in EPIC 9.

**Scope:**
1. Extract `IntelligenceNetwork` for lifetime stats tracking
2. Extract `UnlockService` for data-driven unlock condition checking
3. Extract `MissionTracker` for mission progress, completions, claims
4. Extract `InventoryManager` for module ownership (may already be adequately separated)
5. Externalize EncounterDirector spawn tables to data config
6. ProgressionManager becomes a thin facade for backward compatibility

**Findings Addressed:**
- FINDING-03 (ProgressionManager SRP) — fully: 4 extracted services
- FINDING-08 (EncounterDirector tables) — fully: externalized to config

**Files Affected:**
- `ProgressionManager.kt` — slim to ~80 lines (facade)
- `EncounterDirector.kt` — reduce to ~60 lines (generic iterator logic)

**New Files Created:**
- `IntelligenceNetwork.kt` — stat recording/retrieval
- `UnlockService.kt` — condition evaluation
- `MissionTracker.kt` — mission progress persistence
- `InventoryManager.kt` — module ownership (check if this already exists adequately)
- `ZoneSpawnTable.kt` or JSON config — externalized spawn tables

**Dependencies:** Sprint 8.5.6 (EventBus pattern established — useful but not strictly required)

**Architecture Changes:**
- ProgressionManager.kt: remove 7 subdomain implementations, keep only delegation methods
- New services: each owns one subdomain with focused API
- EncounterDirector: reads spawn rules from external config instead of inline
- External callers: gradually migrate from `ProgressionManager.xxx()` to `IntelligenceNetwork.xxx()` etc.

**Migration Strategy:**
1. Identify all ProgressionManager methods and group by subdomain
2. Create `IntelligenceNetwork` — move stat methods
3. Create `MissionTracker` — move mission methods
4. Create `UnlockService` — move `checkUnlocks()`
5. Create `InventoryManager` — move module methods
6. Reduce ProgressionManager to delegation only
7. Externalize EncounterDirector tables

**Validation Strategy:**
- Save game → play → exit → reload: verify all stats persist
- Complete a mission → verify progress recorded
- Unlock a module → verify unlock persists
- `./gradlew assembleDebug`

**Rollback Strategy:** Keep ProgressionManager facade throughout — external callers don't change. If a service has a bug, fix the service, not the callers.

**Risks:** Medium. Mission persistence is the highest-risk path. Keep `recordMissionCompletion()` and `saveMissionProgress()` as facade methods until migration is confirmed.

**Exit Criteria:**
- 4 domain services extracted
- `ProgressionManager` reduced to ≤ 80 lines (facade)
- `EncounterDirector` spawn tables externalized
- All persistence backward-compatible
- Build successful

**Success Metrics:**
- ProgressionManager.kt from 334 to ~80 lines
- New services: 4 files, each <100 lines focused
- EncounterDirector.kt from 227 to ~60 lines
- `checkUnlocks()` simplified from god function to config-driven

---

### Sprint 8.5.8 — Navigation Migration

**Purpose:** Replace the state-based `when(gameState)` block with Jetpack Navigation (`NavHost` + `NavController`). This is the final structural change — it enables proper back stack, deep linking, and independent screen state.

**Scope:**
1. Add Jetpack Navigation Compose dependency
2. Define `NavGraph` with routes for all 14 screens
3. Wrap each screen composable in NavHost route
4. Replace `gameState` enum with `NavController.navigate()`
5. Preserve `GameState` enum as bridge during transition
6. Remove `previousState` variable (replaced by NavController back stack)

**Findings Addressed:**
- FINDING-04 (Navigation not scalable) — fully: NavHost with proper routing

**Files Affected:**
- `build.gradle.kts` — add `androidx.navigation:navigation-compose` dependency
- `GameScreen.kt` — remove when(gameState) block; replace with NavHost + NavGraph
- `MainActivity.kt` — may need NavController host setup
- All screen composable files — may need route parameter adjustments

**New Files Created:**
- `NavGraph.kt` — navigation graph definition

**Dependencies:** All previous sprints — GameScreen.kt should be as lean as possible before this migration

**Architecture Changes:**
- Navigation framework: Jetpack Navigation Compose
- Back stack: automatic (NavController manages it)
- Screen state: each route has its own savedStateHandle
- `previousState` variable removed
- `GameState` enum preserved as route targets during transition

**Migration Strategy:**
1. Add navigation dependency to `build.gradle.kts`
2. Define `NavGraph` with composable routes for each GameState
3. Wrap current `when(gameState)` block — one branch at a time
4. Replace `gameState = X` assignments with `navController.navigate(X_route)`
5. Handle back navigation with `navController.popBackStack()`
6. Remove `previousState` — use `navController.previousBackStackEntry`
7. Test all 14 navigation paths

**Validation Strategy:**
- Navigate through every screen: verify all transitions work
- Back button: verify it returns to correct previous screen
- Test: Title → Main Menu → Hangar → Loadout → Back → Main Menu → Play
- Test: Title → Main Menu → Settings → Back → Main Menu
- Test: Play → Pause → Help → Back → Pause → Resume
- Test: Play → Game Over → Continue → Resume
- `./gradlew assembleDebug`

**Rollback Strategy:** Critical path. Keep `GameState` enum as route targets throughout migration. If NavHost breaks, switch back to `when(gameState)` — both can coexist during transition.

**Risks:** Medium. Navigation is UI-only but affects all 14 screens. Risk is visual/logical (wrong screen shows) rather than gameplay-physics.

**Exit Criteria:**
- Jetpack Navigation Compose dependency added
- `NavGraph` defined with 14+ routes
- `when(gameState)` block removed from GameScreen
- `previousState` variable removed
- All 14 navigation paths verified
- Build successful

**Success Metrics:**
- GameScreen.kt reduced by ~200 lines (when block removed)
- Navigation is now extensible — new screens are new routes
- Back stack works automatically
- Deep linking is possible

---

## 5. Sprint Sequence Analysis

### Current Order

```
0 (baseline) → 1 (cleanup) → 2 (HUD) → 3 (celebration) → [4 (rendering) ‖ 5 (engine)] → 6 (threat) → 7 (progression) → 8 (NavHost)
```

### Challenge: Should any sprints be reordered?

| Swap Consideration | Current Position | Proposed Alternative | Verdict |
|-------------------|-----------------|---------------------|---------|
| 2 (HUD) ↔ 3 (celebration) | 2 before 3 | 3 before 2 | **Keep current.** Both are 1-session, low risk, and independent. Finding-06 (ceremony) depends on Finding-07 cleanup in Sprint 8.5.1 — which is already satisfied before either 2 or 3. No benefit to swapping. |
| 5 (engine) before 4 (rendering) | Parallel | Engine first, then rendering | **Keep current (parallel).** The engine boundary (Sprint 8.5.5) extracts the game loop. Threat rendering extraction (Sprint 8.5.4) extracts Canvas drawing. These are independent — the game loop does not depend on how threats are rendered, and rendering does not depend on how the game loop is structured. Parallel execution minimizes total calendar time. |
| 6 (ActiveThreat) before 4+5 | After 4+5 | Before 4+5 | **Keep current.** ActiveThreat strategies depend on the event bus (Sprint 8.5.6 extracted) which is wired into GameEngine (Sprint 8.5.5). Doing 6 before 5 means the EventBus has no engine to hook into. Doing 6 before 4 means game rendering still uses old inline code — strategy extraction would have to consider both rendering paths. Current order is correct. |
| 7 (progression) parallel with 4+5 | After 6 | Parallel with 4/5 | **Acceptable alternative, but keep current.** Progression decomposition is logically independent of rendering and engine work. However, doing it too early risks: (a) MissionManager is still coupled (Sprint 8.5.3 fixes that), (b) the EventBus pattern from 8.5.6 provides the clean service boundary pattern to follow. Current placement is safer. |
| 8 (NavHost) before 4+5+6 | Last | Before rendering | **Keep current (last).** Attempting NavHost migration while GameScreen still has 3,901 lines and inline threat rendering is high-risk — every screen refactor would be tangled in the God Object. Doing NavHost last means GameScreen is at its leanest (~1,500 lines), reducing the surface area for navigation bugs. |

### Justification for Current Order

1. **Ramp-up from zero risk to high value:** 8.5.0 (read-only) → 8.5.1 (dead code) → 8.5.2 (duplication) → 8.5.3 (moderate extraction) provides a safe learning curve for the team.
2. **Critical God Objects split into focused strikes:** GameScreen is addressed by 3 sprints (4, 5, 8) — each removes a specific concern (rendering, engine loop, navigation). ActiveThreat is addressed by 1 focused sprint (6) after its rendering dependency is extracted.
3. **Event bus enables clean interfaces:** Sprint 8.5.6 establishes the event-driven pattern, which Sprint 8.5.7 (Progression) can optionally use for decoupled service communication.
4. **NavHost last minimizes merge conflicts:** All 14 screens are touched only once during Sprint 8.5.8. Doing it earlier would mean re-touching screens after extractor sprints.

### Suggested Micro-Optimization (Optional)

Consider **moving Sprint 8.5.7 (Progression) to run in parallel with Sprints 8.5.4 and 8.5.5**, since it has no hard dependency on rendering or engine extraction. This would reduce total calendar time if multiple engineers are available. The only prerequisite is Sprint 8.5.3 (ProgressionService interface). The risk is slight: the EventBus pattern from 8.5.6 may have influenced Progression service design, but the `ProgressionService` interface already provides sufficient decoupling.

**Recommendation:** Keep sequential unless parallel engineering resources are available.

---

## 6. Ownership Boundaries

### Post-8.5 Package Structure (Proposed)

```
com.example.jump_droid/
├── MainActivity.kt
├── NavGraph.kt                          # NEW: navigation routes
├── GameScreen.kt                       # REDUCED: ~1,500 lines
│
├── engine/
│   ├── GameEngine.kt                   # NEW: game loop
│   ├── CollisionSystem.kt              # NEW: collision logic
│   └── EventBus.kt                     # NEW: synchronous events
│
├── threats/
│   ├── ActiveThreat.kt                 # REDUCED: ~250 lines
│   ├── ThreatBehavior.kt              # NEW: interface
│   ├── ThreatDefinition.kt             # EXISTING
│   ├── ThreatType.kt                   # EXISTING
│   ├── ThreatState.kt                  # EXISTING
│   ├── ThreatTier.kt                   # EXISTING
│   ├── ThreatSpawnRules.kt            # EXISTING
│   ├── ThreatManager.kt               # EXISTING
│   ├── ThreatRegistry.kt              # EXISTING
│   ├── ThreatRenderer.kt              # NEW: interface
│   ├── renderers/                      # NEW: 20 files
│   │   ├── HazardRenderer.kt
│   │   ├── EnemyRenderer.kt
│   │   └── BossRenderer.kt
│   └── behaviors/                      # NEW: 20 files
│       ├── HazardBehavior.kt
│       ├── EnemyBehavior.kt
│       └── BossBehavior.kt
│
├── progression/
│   ├── ProgressionManager.kt          # REDUCED: ~80 lines (facade)
│   ├── IntelligenceNetwork.kt         # NEW: lifetime stats
│   ├── UnlockService.kt              # NEW: conditions
│   ├── MissionTracker.kt             # NEW: mission progress
│   ├── InventoryManager.kt           # NEW: module ownership
│   └── RewardService.kt              # NEW: reward routing
│
├── missions/
│   ├── MissionManager.kt              # EXISTING (+ new methods)
│   ├── Mission.kt                     # CLEANED (zombie fields removed)
│   ├── MissionType.kt                 # CLEANED (COMBO removed)
│   ├── MissionReward.kt               # EXISTING
│   ├── MissionRegistry.kt             # EXISTING
│   ├── MissionScreen.kt               # EXISTING (track selection extracted)
│   └── ProgressionService.kt          # NEW: interface for decoupling
│
├── hud/
│   ├── HudWidgets.kt                  # REDUCED: ~400 lines
│   ├── HudState.kt                    # NEW: cross-cutting state
│   ├── GaugeBar.kt                    # NEW: base composable
│   └── StarfieldBackground.kt         # NEW: reusable background
│
├── managers/                          # EXISTING — unchanged
│   ├── SurvivalManager.kt
│   ├── EncounterDirector.kt           # REDUCED: ~60 lines
│   ├── PlatformManager.kt
│   ├── NotificationManager.kt
│   ├── FloatingTextManager.kt
│   ├── ProjectileManager.kt
│   ├── InputBufferManager.kt
│   ├── ComboManager.kt               # CLEANED: unified rewards
│   ├── AltitudeManager.kt
│   ├── DiscoveryManager.kt
│   └── LoadoutManager.kt
│
├── screens/                           # EXISTING — unchanged
│   ├── TitleScreen.kt
│   ├── MainMenuScreen.kt
│   ├── HangarScreen.kt
│   ├── LoadoutScreen.kt
│   ├── ArchiveScreen.kt
│   ├── SettingsScreen.kt
│   ├── AboutScreen.kt
│   ├── LeaderboardScreen.kt
│   ├── PauseOverlay.kt
│   ├── TutorialOverlay.kt
│   ├── HelpOverlay.kt
│   ├── UnlockOverlay.kt
│   └── GameOverOverlay.kt
│
├── renderers/                         # EXISTING — focused
│   ├── RocketRenderer.kt
│   ├── PlatformRenderer.kt
│   ├── ZoneBackgroundRenderer.kt
│   ├── CanvasEffects.kt
│   └── AmbientSystem.kt
│
├── data/
│   ├── Models.kt
│   ├── Constants.kt
│   ├── GameStats.kt
│   ├── Projectile.kt
│   ├── Tether.kt
│   ├── Platform.kt
│   ├── Module.kt
│   └── ZoneSpawnTable.kt             # NEW: externalized spawn config
│
├── ui/theme/
│   ├── Color.kt
│   ├── Theme.kt
│   ├── Type.kt
│   └── TypographyConfig.kt
│
├── Achievements.kt                    # EXISTING
├── CodexCard.kt                       # EXISTING
├── TopRightUtilityButtons.kt          # EXISTING
├── FloatingTextsLayer.kt              # EXISTING
├── ParallaxSystem.kt                  # EXISTING
└── DevConfig.kt                       # EXISTING
```

---

## 7. Dependency Diagrams

### Sprint Dependency Graph

```
8.5.0 (Baseline)
   │
   ▼
8.5.1 (Cleanup)
   │
   ▼
8.5.2 (HUD)
   │
   ▼
8.5.3 (Celebration)
   │
   ├────────────────────┐
   ▼                    ▼
8.5.4 (Rendering)   8.5.5 (Engine)   8.5.7 (Progression — optional parallel)
   │                    │
   └────────┬───────────┘
            ▼
        8.5.6 (ActiveThreat)
            │
            ▼
        8.5.7 (Progression — if not parallel)
            │
            ▼
        8.5.8 (Navigation)
```

### File Dependency Map (New Files Only)

```
NavGraph.kt ───────────────────────────────── depends on → all screen files
    │
GameEngine.kt ───── depends on → CollisionSystem, EventBus, all managers
    │
CollisionSystem.kt ── depends on → Models.kt, Constants.kt
    │
EventBus.kt ──────── depends on → nothing (standalone utility)
    │
ThreatBehavior.kt ── depends on → ActiveThreat, ThreatDefinition
    │
ThreatRenderer.kt ── depends on → ThreatDefinition, DrawScope
    │
ProgressionService.kt ─ depends on → nothing (interface)
    │
IntelligenceNetwork.kt ─ depends on → SharedPreferences
UnlockService.kt ───── depends on → SharedPreferences, ModuleRegistry
MissionTracker.kt ──── depends on → SharedPreferences, Mission.kt
InventoryManager.kt ── depends on → SharedPreferences, Module.kt
    │
HudState.kt ───────── depends on → Zone model
GaugeBar.kt ───────── depends on → Compose foundation
StarfieldBackground.kt ─ depends on → Compose Canvas
    │
ZoneSpawnTable.kt ──── depends on → ThreatType, Zone model
```

---

## 8. Risk Register

| ID | Risk | Likelihood | Impact | Sprint | Mitigation | Owner |
|----|------|-----------|--------|--------|------------|-------|
| R01 | Physics timing changes during engine extraction | Low | Critical | 8.5.5 | Extract in stages with cherry-pick checkpoints; verify gameplay feel after each commit | Sprint 8.5.5 |
| R02 | NavHost causes screen state loss | Medium | High | 8.5.8 | Preserve GameState enum as bridge; both NavHost and when() coexist during transition | Sprint 8.5.8 |
| R03 | Event bus introduces missed/dispatched events | Medium | Medium | 8.5.6 | Synchronous dispatch (same-thread); exhaustive test of all callback paths before/after | Sprint 8.5.6 |
| R04 | Progression persistence breaks during decomposition | Low | High | 8.5.7 | Keep facade methods until migration confirmed; test save/load cycle | Sprint 8.5.7 |
| R05 | Sprint scope creep | High | Medium | ALL | Hard exit criteria per sprint; cut unstarted items if over-estimate | All |
| R06 | Baseline screenshots become outdated | Low | Medium | 8.5.0 | Capture baseline as first action; tag immediately | Sprint 8.5.0 |
| R07 | Threat rendering looks different after extraction | Medium | Medium | 8.5.4 | Per-threat before/after screenshot comparison; extract simplest first | Sprint 8.5.4 |
| R08 | Ceremony timing changes after lifecycle extraction | Low | Low | 8.5.3 | Visual comparison of ceremony sequence before/after | Sprint 8.5.3 |
| R09 | Gauge visuals change after GaugeBar extraction | Low | Low | 8.5.2 | Pixel-level before/after comparison of all 4 gauges | Sprint 8.5.2 |
| R10 | HUD state hoisting misses a cross-cutting concern | Low | Low | 8.5.2 | Audit all HudWidgets parameters before hoisting; add HudState fields as needed | Sprint 8.5.2 |
| R11 | Starfield replacement changes visual appearance | Low | Low | 8.5.2 | Compare star rendering parameters (count, speed, color, twinkle) for each screen | Sprint 8.5.2 |
| R12 | ActiveThreat strategy extraction misses edge case AI behavior | Medium | Medium | 8.5.6 | Unit test each strategy with edge case inputs; verify boss phase transitions | Sprint 8.5.6 |
| R13 | Multiple engineers cause merge conflicts during parallel sprints | Medium | Medium | 8.5.4+5 | Clear ownership boundaries; commit frequently; use feature branches | All |

---

## 9. Testing Matrix

| Test Scenario | What It Validates | Priority | Sprint | Manual/Auto |
|--------------|-------------------|----------|--------|-------------|
| Physics feel (gravity, thrust, friction) | GameEngine extraction didn't change physics | CRITICAL | 8.5.5 | Manual |
| All 26 threat type rendering | ThreatRenderer extraction is pixel-identical | CRITICAL | 8.5.4 | Manual (screenshot) |
| All 26 threat type AI behavior | ThreatBehavior strategies match original | CRITICAL | 8.5.6 | Manual |
| Boss phase transitions (all 6 bosses) | Strategy extraction didn't break state machines | CRITICAL | 8.5.6 | Manual |
| 14 screen navigation paths | NavHost migration is complete | HIGH | 8.5.8 | Manual |
| Mission completion ceremony | Ceremony lifecycle extraction preserved | HIGH | 8.5.3 | Manual |
| Combo rewards at 5/10/15/20 | Reward system unification | HIGH | 8.5.3 | Manual |
| Save/load cycle (mission progress) | Progression persistence | HIGH | 8.5.7 | Manual |
| Save/load cycle (unlocks) | UnlockService extraction | HIGH | 8.5.7 | Manual |
| Fuel gauge rendering | GaugeBar extraction | MEDIUM | 8.5.2 | Visual |
| Heat gauge rendering | GaugeBar extraction | MEDIUM | 8.5.2 | Visual |
| Shield gauge rendering | GaugeBar extraction | MEDIUM | 8.5.2 | Visual |
| Integrity gauge rendering | GaugeBar extraction | MEDIUM | 8.5.2 | Visual |
| Starfield on 6 screens | StarfieldBackground replacement | MEDIUM | 8.5.2 | Visual |
| 48 missions available and trackable | Mission cleanup didn't break registry | MEDIUM | 8.5.1 | Manual |
| `checkCompletion()` pure function | Ceremony cleanup | LOW | 8.5.1 | Auto (unit test) |
| `MissionRow.kt` zero references | Dead code removal | LOW | 8.5.1 | Auto (grep) |
| `isNew` zero references | Zombie field removal | LOW | 8.5.1 | Auto (grep) |
| `CeremonyStage` has 3 values | Enum cleanup | LOW | 8.5.1 | Auto (grep) |
| `MissionType.COMBO` zero references | Enum cleanup | LOW | 8.5.1 | Auto (grep) |
| `getCurrentMissionForTrack()` unit test | MissionScreen extraction | LOW | 8.5.2 | Auto |
| `./gradlew assembleDebug` | Build integrity | BLOCKER | ALL | Auto |

### Testing Cadence

| Phase | Tests |
|-------|-------|
| Per-commit | `./gradlew assembleDebug` |
| Per-sprint | All manual tests for that sprint's scope |
| Per-milestone (after 8.5.3) | Full gameplay smoke test |
| Per-milestone (after 8.5.6) | Full threat suite + boss phases |
| EPIC completion | All 22 test scenarios |

---

## 10. Metrics Dashboard

### Dynamic Metrics (Tracked Per Sprint)

| Metric | 8.5.0 | 8.5.1 | 8.5.2 | 8.5.3 | 8.5.4 | 8.5.5 | 8.5.6 | 8.5.7 | 8.5.8 | Target |
|--------|-------|-------|-------|-------|-------|-------|-------|-------|-------|--------|
| GameScreen.kt lines | 3,901 | ~3,880 | ~3,860 | ~3,830 | ~2,200 | ~1,500 | ~1,500 | ~1,500 | ~1,300 | <1,800 |
| ActiveThreat.kt lines | 1,224 | 1,224 | 1,224 | 1,224 | 1,224 | 1,224 | ~250 | ~250 | ~250 | <350 |
| ProgressionManager.kt lines | 334 | 334 | 334 | 334 | 334 | 334 | 334 | ~80 | ~80 | <100 |
| HudWidgets.kt lines | 654 | 654 | ~450 | ~450 | ~450 | ~450 | ~450 | ~450 | ~450 | <450 |
| Total files | ~66 | ~65 | ~68 | ~69 | ~89 | ~91 | ~111 | ~116 | ~117 | ~120 |
| Total lines (~K) | 14.8 | 14.6 | 14.6 | 14.6 | 14.7 | 14.7 | 14.6 | 14.5 | 14.5 | ~14.5 |
| Median file size | 99 | 99 | 98 | 97 | 85 | 83 | 70 | 68 | 67 | <70 |
| GameScreen imports | 70 | ~70 | ~70 | ~70 | ~55 | ~40 | ~40 | ~40 | ~35 | <40 |
| ActiveThreat callback params | 17 | 17 | 17 | 17 | 17 | 17 | 1 (EB) | 1 (EB) | 1 (EB) | ≤3 |
| GameScreen inline Canvas (ln) | ~1,947 | ~1,947 | ~1,947 | ~1,947 | 0 | 0 | 0 | 0 | 0 | 0 |
| Inline game loop (ln) | ~790 | ~790 | ~790 | ~790 | ~790 | 0 | 0 | 0 | 0 | 0 |
| `when(gameState)` branches | 14 | 14 | 14 | 14 | 14 | 14 | 14 | 14 | 0 | 0 |
| Starfield copy-paste | 6 | 6 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| Dead code files | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |

### Static Metrics (Captured at Baseline, Verified at Completion)

| Metric | Baseline (8.5.0) | Target (Post-8.5) |
|--------|-----------------|-------------------|
| Codebase total lines | ~14,800 | ~14,500 |
| Kotlin files | ~66 | ~117 |
| Top-3-file % of codebase | ~40% | ~20% |
| ProgressionManager responsibilities | 7+ | 1 (facade) |
| CeremonyStage values | 5 | 3 |
| Mission.kt UI fields | 2 | 0 |
| ComboManager reward paths | 2 | 1 |

---

## 11. EPIC Completion Criteria

All of the following must be true:

### File Size Gates
- [ ] `GameScreen.kt` < 1,800 lines
- [ ] `ActiveThreat.kt` < 350 lines
- [ ] `ProgressionManager.kt` < 100 lines
- [ ] `HudWidgets.kt` < 450 lines
- [ ] `MissionRow.kt` deleted from source tree

### Structural Gates
- [ ] `GameEngine` class extracted — physics loop not in GameScreen
- [ ] `ThreatBehavior` interface with 20+ strategy implementations
- [ ] `ThreatRenderer` interface with 20+ renderer implementations
- [ ] `CollisionSystem` extracted from ActiveThreat
- [ ] `EventBus` replaces 17-callback `processInteraction` signature
- [ ] `ProgressionService` interface defined — MissionManager uses it
- [ ] `ProgressionManager` is a thin facade (≤ 4 services)
- [ ] `NavHost` replaces `when(gameState)` — 14 routes defined
- [ ] `previousState` variable removed
- [ ] `GaugeBar` composable used by all 4 gauge sites
- [ ] `StarfieldBackground` composable used in all 6 screens
- [ ] `EncounterDirector` spawn tables externalized to config

### Cleanup Gates
- [ ] `isNew` zombie field removed from `Mission.kt`
- [ ] `CeremonyStage` has exactly 3 values
- [ ] `Mission.checkCompletion()` is a pure function
- [ ] `MissionType.COMBO` removed
- [ ] `claimMissionRewards()` has no redundant `ProgressionManager` parameter
- [ ] Star animation not duplicated (0 inline instances)

### Quality Gates
- [ ] `./gradlew assembleDebug` — `BUILD SUCCESSFUL`
- [ ] Full gameplay smoke test — all zones, all threats, all bosses playable
- [ ] All mission progress persists correctly
- [ ] All save/load cycles work
- [ ] All 14 navigation paths verify
- [ ] All 4 gauges render identically to baseline
- [ ] All 6 starfields render identically to baseline
- [ ] All 26 threat types render identically to baseline

---

## 12. EPIC 9 Handoff Requirements

EPIC 9 (Hidden Signals & Dynamic Unlocks) must NOT begin until EPIC 8.5 completes. The following handoff conditions apply:

### Required State
1. **GameScreen.kt** < 1,800 lines — EPIC 9 screens add as new routes, not new `when` branches
2. **ActiveThreat.kt** < 350 lines — EPIC 9 threats add as `ThreatBehavior` + `ThreatRenderer` classes, not inline code
3. **ProgressionManager** decomposed — EPIC 9 Dynamic Unlocks add conditions to `UnlockService`, not to `ProgressionManager`
4. **NavHost** operational — EPIC 9 screens (Signal Viewer, Decrypt Interface) add as new routes
5. **EventBus** available — EPIC 9 threat interactions use event dispatch

### Assets EPIC 9 Receives

| Asset | Location | Format |
|-------|----------|--------|
| NavHost with 14 routes | `NavGraph.kt` | Jetpack Navigation Compose |
| `GameEngine` class | `engine/GameEngine.kt` | Pure Kotlin class |
| `ThreatBehavior` interface | `threats/ThreatBehavior.kt` | Interface |
| 20+ threat behavior strategies | `threats/behaviors/` | Concrete classes |
| `ThreatRenderer` interface | `threats/ThreatRenderer.kt` | Interface + DrawScope extensions |
| 20+ threat renderers | `threats/renderers/` | DrawScope extensions/classes |
| `CollisionSystem` | `engine/CollisionSystem.kt` | Utility class |
| `EventBus` | `engine/EventBus.kt` | Synchronous dispatcher |
| `ProgressionService` interface | `missions/ProgressionService.kt` | Interface |
| `IntelligenceNetwork` | `progression/IntelligenceNetwork.kt` | Data service |
| `UnlockService` | `progression/UnlockService.kt` | Data-driven condition checker |
| `MissionTracker` | `progression/MissionTracker.kt` | Data service |
| `InventoryManager` | `progression/InventoryManager.kt` | Data service |
| `GaugeBar` | `hud/GaugeBar.kt` | Reusable composable |
| `HudState` | `hud/HudState.kt` | Data class |
| `StarfieldBackground` | `hud/StarfieldBackground.kt` | Reusable composable |
| `ZoneSpawnTable` config | `data/ZoneSpawnTable.kt` | Data config |
| Baseline metrics | `docs/analysis/EPIC8_5_BASELINE_REPORT.md` | Documentation |

### Prohibited During EPIC 9 (Until EPIC 8.5 Completes)
- Adding `when(gameState)` branches to GameScreen.kt
- Adding inline threat rendering to GameScreen Canvas block
- Adding methods directly to `ProgressionManager`
- Adding callback parameters to `ActiveThreat.processInteraction()`
- Adding inline star animations
- Modifying `MissionRow.kt` (it should not exist)

### EPIC 9 Entry Checklist
- [ ] EPIC 8.5 completion criteria fully met
- [ ] Baseline metrics recorded
- [ ] All 22 EPIC 8.5 test scenarios pass
- [ ] `epic8.5-complete` tag created
- [ ] EPIC 9 scope document updated to reference new architecture boundaries
- [ ] AGENTS.md updated for EPIC 9 transition
