# EPIC 8 — Technical Debt Audit

**Date:** 2026-06-23
**Mode:** READ ONLY
**Scope:** Codebase health after EPIC 8
**Branch:** `epic8-mission-migration`
**Build Reference:** `BUILD SUCCESSFUL` (all tasks up-to-date)

---

## 1. File Size Heatmap

| File | Lines | Risk | Notes |
|------|-------|------|-------|
| **GameScreen.kt** | **3,901** | 🔴 CRITICAL | Single composable, ~30 subsystems |
| **ActiveThreat.kt** | **1,224** | 🔴 CRITICAL | Becoming second God Object |
| HudWidgets.kt | 654 | 🟡 Moderate | Copy-paste gauge structure |
| ZoneBackgroundRenderer.kt | 558 | 🟢 Low | Focused rendering concern |
| CanvasEffects.kt | 483 | 🟢 Low | Focused rendering helpers |
| ModuleRegistry.kt | 419 | 🟢 Low | Data-only |
| TitleScreen.kt | 364 | 🟢 Low | Splash screen |
| ProgressionManager.kt | 334 | 🟡 Moderate | 7+ responsibilities |
| MissionScreen.kt | 318 | 🟢 Low | Clean extractable |
| MissionManager.kt | 241 | 🟡 Moderate | Tightly coupled |
| MissionRegistry.kt | 230 | 🟡 Moderate | 48-mission static init |
| EncounterDirector.kt | 227 | 🟡 Moderate | Hardcoded tables |
| **Median file size** | **99** | — | Codebase median is healthy |

---

## 2. Findings

### FINDING-01 — GameScreen is a God Object (CRITICAL)

**File:** `GameScreen.kt` (3,901 lines)
**Risk:** CRITICAL
**Impact:** Future development bottleneck

**Evidence:**
- Manages ~30 distinct subsystems (physics, AI, 15 managers, 3 renderers, particles, power-ups, HUD, 14 screen states, dev cheats, escalation, tether)
- 70 import statements spanning physics, UI, animation, Canvas, Coroutines, materials
- 14 local functions closing over composable state — none extractable without refactoring
- ~1,947 lines of inline threat rendering inside a single `Canvas { }` block (26 threat/boss types)
- ~790 lines of inline game loop (`LaunchedEffect` + `withFrameNanos`) handling physics, collision, AI, progression
- All 14 screen states live in a single `when(gameState)` block sharing one composition scope

**Why it matters for Phase 9:**
- Adding Hidden Signals, Dynamic Unlocks, more threats, more bosses, or more progression will all require modifying this file
- The single `remember` scope means state from one screen can contaminate another
- Cannot independently test any subsystem without composing the entire GameScreen
- Inline threat rendering prevents adding new threats without growing an already-oversized Canvas block

**Recommendation:** Extract game engine into a standalone `GameEngine` class. Move navigation to a `NavHost`. Decompose the rendering pipeline into per-system renderers. **Priority: Phase 9 prerequisite.**

---

### FINDING-02 — ActiveThreat is Becoming a Second God Object (CRITICAL)

**File:** `ActiveThreat.kt` (1,224 lines)
**Risk:** CRITICAL
**Impact:** Will block new threat/boss development

**Evidence:**
- Mixes AI behavior, collision logic, visual feedback, and event coordination in one class
- Boss AI (500+ lines) is inline `when` blocks for each phase — each new boss adds 80-100 inline lines
- `processInteraction()` takes **17 callback parameters** — an exploded API surface
- Collision logic directly mutates player fields (`player.shield`, `player.velocityX`, etc.)
- Visual feedback (bursts, impacts) is mixed with business logic

**Why it matters for Phase 9:**
- "More Threats" and "More Bosses" are explicitly planned. Every new entity increases ActiveThreat's size inline.
- The 17-callback `processInteraction` signature becomes harder to maintain with each new interaction type.
- Per-threat AI is not extensible — adding a new behavior requires modifying the central `when` block.

**Recommendation:** Extract a `ThreatBehavior` interface/strategy. Each threat type gets its own behavior class. Extract collision logic into a `CollisionSystem`. Replace 17 callbacks with an event bus. **Priority: Phase 9 prerequisite.**

---

### FINDING-03 — ProgressionManager SRP Violation (MAJOR)

**File:** `ProgressionManager.kt` (334 lines)
**Risk:** MAJOR
**Impact:** Fragile when adding new progression systems

**Evidence:**
Handles 7+ distinct subdomains:

| Responsibility | Lines | Methods |
|---|---|---|
| Artifact management | 39-50 | 2 |
| Module inventory | 42-43, 152-163 | 2 |
| Mission tracking | 45-49, 60, 165-191 | 6 |
| Rank system | 51-52, 242-257 | 2 |
| Lifetime stats (Intelligence Network) | 65-77, 196-211 | 2 |
| Reward granting | 216-240 | 1 |
| Unlock auditing | 311-369 | 1 ("god function") |

`checkUnlocks()` (lines 311-369) simultaneously evaluates rocket unlocks, module unlocks (with special-case for `MOD_SHIELD_FAST_RECHARGE`), lore discovery thresholds, and achievement conditions all in one method.

**Why it matters for Phase 9:**
- Dynamic Unlocks will add more unlock conditions — `checkUnlocks()` will grow further
- Hidden Signals likely involves new discovery/progression paths — new subdomain, same class
- Any change to any progression subdomain risks touching this file

**Recommendation:** Decompose into:
- `ArtifactManager` / `DiscoveryJournal` (artifact CRUD)
- `MissionTracker` (progress, completions, claims — extract from MissionManager too)
- `InventoryManager` (module ownership)
- `IntelligenceNetwork` / `StatRecorder` (lifetime stats)
- `RewardService` (reward routing)
- `UnlockService` (condition checking — data-driven conditions)

**Priority: Phase 9.**

---

### FINDING-04 — Navigation Architecture is Not Scalable (MAJOR)

**File:** `GameScreen.kt` lines 3672-3888, `MainActivity.kt`
**Risk:** MAJOR
**Impact:** Every new screen requires modifying the central `when` block

**Evidence:**
- Single `when (gameState)` block manages 14 screen states
- No `NavHost`, no Jetpack Navigation, no navigation graph
- No back stack — single `previousState` variable (fragile)
- All `remember` state lives in one composition scope — state pollution risk
- Deep linking unsupported

**Why it matters for Phase 9:**
- Hidden Signals likely requires new screens (signal viewer, decrypt interface)
- Dynamic Unlocks likely requires notification/selection screens
- More progression means more menu screens

**Recommendation:** Migrate to Jetpack Navigation (`NavHost` + `NavController`) with per-screen state hoisting or ViewModels. Extract screen composables into their own files (most already are — just need the navigation wrapper). **Priority: Phase 9.**

---

### FINDING-05 — Threat Rendering is Inline and Non-extensible (MAJOR)

**File:** `GameScreen.kt` lines 1672-3619
**Risk:** MAJOR
**Impact:** Cannot add threats without growing GameScreen

**Evidence:**
- 26 threat/boss types rendered inline in a single Canvas block (~1,947 lines)
- Each threat has hundreds of procedural `drawCircle`, `drawPath`, `drawLine` calls inline
- No delegation to external renderers or strategy objects
- Adding one new threat type adds ~60-100 lines to GameScreen.kt

**Why it matters for Phase 9:**
- "More Threats" and "More Bosses" explicitly planned
- Every new threat adds to the 3,901-line file

**Recommendation:** Extract a `ThreatRenderer` interface. Each threat type gets its own `DrawScope` extension function or renderer class. The Canvas block calls a single `drawThreat(threat)` dispatch. **Priority: Phase 9.**

---

### FINDING-06 — Ceremony/Replacement State Machine is Scattered (MODERATE)

**Files:** `Mission.kt`, `MissionManager.kt`, `GameScreen.kt`
**Risk:** MODERATE
**Impact:** Hard to reason about mission lifecycle

**Evidence:**
- `CeremonyStage` has 5 values: `NONE`, `GLOW`, `COMPLETED_TEXT`, `REWARD_SPAWNED`, `REPLACING`
- Only 2 are used in production (`GLOW` in `Mission.checkCompletion()`, `REPLACING` in `GameScreen.kt:1556`)
- `COMPLETED_TEXT` and `REWARD_SPAWNED` only exist in dead file `MissionRow.kt`
- No single place to trace the ceremony lifecycle
- `checkCompletion()` has a side effect (sets `ceremonyStage`) despite being named as a query

**Recommendation:** Remove unused `CeremonyStage` values. Extract the ceremony lifecycle into `MissionManager` or a dedicated `MissionCeremony` system. Make `checkCompletion()` a pure function. **Priority: Moderate.**

---

### FINDING-07 — Mission.kt Mixes UI State with Domain State (MODERATE)

**File:** `Mission.kt`
**Risk:** MODERATE
**Impact:** State mutation paths are hard to trace

**Evidence:**
- Domain: `currentProgress`, `isUnlocked`, `isCompleted`, `isClaimed`
- UI: `ceremonyStage`, `isNew`
- `isNew` is a **zombie field** — initialized `true`, never set to `false` in production code
- `reset()` must clear both domain and UI state — fragile

**Recommendation:** Split into `MissionState` (domain, persistent) and separate UI state holder. Remove `isNew`. **Priority: Moderate.**

---

### FINDING-08 — EncounterDirector Spawn Tables are Hardcoded (MODERATE)

**File:** `EncounterDirector.kt` (227 lines)
**Risk:** MODERATE
**Impact:** Adding new zones/threats requires code changes

**Evidence:**
- Zone multipliers defined in two separate `when` blocks
- Hazard weights per zone: hardcoded triple-nested structure
- Zone-specific entity spawning: 9 repeated `check zone -> check count -> roll chance -> spawn` blocks
- Boss milestones: inline list

**Recommendation:** Define `ZoneSpawnTable` data configuration (JSON or data class). Move boss milestones to config. Reduce EncounterDirector to ~60 lines of generic iterator logic. **Priority: Phase 9.**

---

### FINDING-09 — ComboManager Has Two Overlapping Reward Systems (MODERATE)

**File:** `ComboManager.kt`
**Risk:** MODERATE
**Impact:** Reward balancing requires changing two separate code paths

**Evidence:**
- `tiers` list (5 entries): combo milestone rewards (tier-based lookup)
- `immediateSurvivalRewards` (lines 44-66): separate hardcoded survival rewards at combo 5, 10, 15, 20
- Two reward systems, same class, different lookup mechanisms
- Reward table is hardcoded — tuning requires recompilation

**Recommendation:** Merge into one reward system. Externalize reward table to JSON/data config. **Priority: Moderate.**

---

### FINDING-10 — MissionRegistry 48-Mission Static Init (MODERATE)

**File:** `MissionRegistry.kt`
**Risk:** MODERATE
**Impact:** Adding missions requires code changes and recompilation

**Evidence:**
- 48 missions registered in a single 215-line `init` block
- No uniqueness validation on IDs
- No data-driven loading (JSON/CSV/annotation)
- All 48 in memory for process lifetime (3 used at a time)

**Recommendation:** Load missions from JSON asset with ID validation. Keep MissionRegistry as a loader/index, not a 215-line block. **Priority: Phase 9 (if mission count grows).**

---

### FINDING-11 — MissionManager Tightly Coupled to ProgressionManager (MODERATE)

**File:** `MissionManager.kt`
**Risk:** MODERATE
**Impact:** Changes to ProgressionManager risk breaking mission system

**Evidence:**
- Accesses ~14 distinct ProgressionManager fields/methods across 8 code paths
- `calculateProgress()` reads 9 lifetime stats directly from ProgressionManager
- `claimMissionRewards()` redundantly takes ProgressionManager as both constructor *and* method parameter

**Recommendation:** Define a `ProgressionService` interface that MissionManager depends on (Dependency Inversion). Remove redundant method parameter. **Priority: Phase 9.**

---

### FINDING-12 — Duplicate Star Animation Code (MODERATE)

**Files:** `MainMenuScreen.kt`, `PauseOverlay.kt`, `GameOverOverlay.kt`, `AboutScreen.kt`, `SettingsScreen.kt`, `HangarScreen.kt`
**Risk:** MODERATE
**Impact:** Visual inconsistency, maintenance burden

**Evidence:** Each file independently defines a `*Star` data class, `frameTime` LaunchedEffect, and Canvas star rendering loop. The same ~20-line pattern is duplicated 6 times.

**Recommendation:** Extract a reusable `StarfieldBackground` composable. **Priority: Moderate.**

---

### FINDING-13 — PauseOverlay 11-Callback API (MODERATE)

**File:** `PauseOverlay.kt`
**Risk:** MODERATE
**Impact:** Brittle interface, hard to evolve

**Evidence:** 11 callbacks, 5 boolean flags, 1 config flag, 1 data value = 18 parameters. Dev menu accounts for 7 callbacks.

**Recommendation:** Split dev menu into separate composable. Group callbacks into data classes or use event sealed class. **Priority: Phase 9.**

---

### FINDING-14 — MissionRow.kt is Dead Code (MINOR)

**File:** `MissionRow.kt` (140 lines)
**Risk:** MINOR
**Impact:** Maintenance trap

**Evidence:** Zero imports in production code. Explicit "removed" comment in GameScreen.kt. References `toIcon()` which would create circular dependency.

**Recommendation:** Delete the file. **Priority: Immediate (safe, zero risk).**

---

### FINDING-15 — MissionManager.updateProgress() and updateProgressAll() Overlap (MINOR)

**File:** `MissionManager.kt`
**Risk:** MINOR
**Impact:** Two code paths with different semantics

**Recommendation:** Document the difference. Consider merging. **Priority: Low.**

---

### FINDING-16 — Inline Track Selection Logic in MissionScreen (MINOR)

**File:** `MissionScreen.kt` lines 118-138
**Risk:** MINOR
**Impact:** Cannot unit-test

**Recommendation:** Extract to `MissionManager.getCurrentMissionForTrack(categories)`. **Priority: Low.**

---

### FINDING-17 — Duplicate Gauge Structure (MINOR)

**File:** `HudWidgets.kt`
**Risk:** MINOR
**Impact:** Adding a new gauge means copy-paste

**Recommendation:** Extract base `GaugeBar` composable. **Priority: Low.**

---

## 3. Top 5 Technical Debt Items to Address After EPIC 8

| Rank | Finding | Risk | Files Affected | Effort |
|------|---------|------|---------------|--------|
| **1** | Decompose GameScreen.kt | CRITICAL | GameScreen.kt + ~20 files | 3-5 sprints |
| **2** | Refactor ActiveThreat.kt with ThreatBehavior strategy | CRITICAL | ActiveThreat.kt | 2-3 sprints |
| **3** | Split ProgressionManager into domain services | MAJOR | ProgressionManager.kt | 1-2 sprints |
| **4** | Extract threat rendering from GameScreen Canvas | MAJOR | GameScreen.kt | 1 sprint |
| **5** | Migrate navigation to NavHost | MAJOR | GameScreen.kt, all screen files | 2-3 sprints |

---

## 4. Low-Risk / High-Value Refactor Candidates

These can be performed safely after EPIC 8 with low regression risk:

| Refactor | Value | Risk | Effort | Rationale |
|----------|-------|------|--------|-----------|
| Delete MissionRow.kt | Low | None | 1 min | Dead code |
| Remove `isNew` field from Mission.kt | Low | None | 5 min | Zombie field |
| Remove unused CeremonyStage values | Low | None | 10 min | Dead enum values |
| Extract StarfieldBackground composable | Medium | Low | 1 session | Eliminates 6x duplication |
| Extract MissionScreen track selection to MissionManager | Medium | Low | 30 min | Enables unit testing |
| Remove redundant ProgressionManager param from claimMissionRewards | Low | Low | 5 min | Dead parameter |

---

## 5. Phase 9 Readiness Assessment

### Planned Work Impact Analysis

| Planned Work | Primary Files Affected | Current Readiness | Blocking Tech Debt |
|---|---|---|---|
| Hidden Signals | MissionScreen.kt, DiscoveryManager.kt, MissionRegistry.kt | 🟡 Needs new screens | FINDING-01 (navigation), FINDING-04 (screens) |
| Dynamic Unlocks | ProgressionManager.kt, MissionManager.kt | 🟡 Needs condition system | FINDING-03 (SRP), FINDING-11 (coupling) |
| More Threats | ActiveThreat.kt, EncounterDirector.kt, GameScreen.kt | 🔴 Blocked | FINDING-02 (God Object), FINDING-05 (inline rendering) |
| More Bosses | ActiveThreat.kt, GameScreen.kt | 🔴 Blocked | FINDING-02, FINDING-05 |
| More Progression | ProgressionManager.kt, MissionRegistry.kt | 🟡 Needs decomposition | FINDING-03, FINDING-10 |

### Bottlenecks

1. **GameScreen.kt** — modifications to any subsystem require touching this 3,901-line file
2. **ActiveThreat.kt** — cannot add new threats/bosses without growing a 1,224-line file
3. **ProgressionManager.kt** — Dynamic Unlocks will make the SRP violation worse
4. **GameScreen.kt Canvas block** — inline threat rendering prevents independent threat development

---

## 6. Answers

### Q1: Is the current architecture healthy?

**Partially.** The architecture was clearly designed for rapid prototyping and has served that purpose well. The manager pattern isolated many concerns (15 dedicated managers, 3 renderers). However, the **orchestration layer (GameScreen.kt) and threat implementation (ActiveThreat.kt) have outgrown their original design.** The codebase is healthy for a prototype but will become a bottleneck for sustained development through Phases 9 and beyond.

**Healthy aspects:**
- Manager pattern successfully extracted physics, combos, survival, encounters, projectiles, input, altitude, notifications, floating text, loadouts
- Renderer pattern extracted background, rocket, platform, and ambient rendering
- Data models (GameStats, MissionReward sealed class, ComboReward) are clean and idiomatic Kotlin
- Compose usage is appropriate and reactive

**Unhealthy aspects:**
- GameScreen.kt at 3,901 lines with no clear decomposition path
- ActiveThreat.kt at 1,224 lines with inline AI and 17-callback interaction API
- ProgressionManager.kt with 7+ responsibilities
- No navigation framework (state-based `when` block)
- 6x copy-paste star animation

### Q2: Is GameScreen becoming a problem?

**Yes, it already is one.** At 3,901 lines with ~30 subsystems and 70 imports, it exceeds what a single file or single composable can reasonably manage. It is the primary bottleneck for all future development:

- Every new feature requires touching GameScreen.kt
- Cannot independently test subsystems
- The single `remember` scope creates state pollution risk across screens
- The inline Canvas rendering (especially 1,947 lines of threat drawing) prevents adding new content without growing the file
- The `LaunchedEffect` game loop (790 lines) mixes physics, AI, and UI updates in one sequential block

The file is not yet at the "rewrite required" stage — the manager pattern means subsystems are extractable — but it is past the "comfortably maintainable" stage.

### Q3: Top 5 Technical Debt Items to Address After EPIC 8

| Rank | Finding | Why |
|------|---------|-----|
| **1** | Decompose GameScreen.kt | Primary bottleneck for ALL future work. Extract game engine, navigation, rendering pipeline. |
| **2** | Refactor ActiveThreat.kt with ThreatBehavior strategy | Required before adding new threats or bosses. Currently impossible without growing a 1,224-line file. |
| **3** | Split ProgressionManager into domain services | Dynamic Unlocks and Hidden Signals will add more progression paths. SRP violation will worsen. |
| **4** | Extract threat rendering from GameScreen Canvas | Enables independent threat development. Moves 1,947 lines out of GameScreen.kt. |
| **5** | Migrate navigation to NavHost | Hidden Signals and additional screens will stress the state-based `when` block pattern. |

### Q4: What should be tackled first in EPIC 9?

**Phase 9 should begin by addressing the two God Objects in parallel:**

**Sprint 9.1 — Game Engine Extraction**
- Extract game engine loop (physics, collision, update) into a `GameEngine` class
- Extract `updateProgressAll()`, entity updates, and frame timing out of GameScreen.kt
- GameScreen retains only Compose rendering and UI orchestration
- **Target:** GameScreen.kt reduced from 3,901 to ~1,500 lines

**Sprint 9.2 — Threat Behavior Strategy**
- Define `ThreatBehavior` interface
- Extract each threat type's AI, collision response, and rendering into its own strategy class
- Replace 17-callback `processInteraction` with an event bus
- **Target:** ActiveThreat.kt reduced from 1,224 to ~200 lines (state + common fields)

**Sprint 9.3 — Progression Decomposition**
- Split ProgressionManager into 3-4 domain services
- Move mission persistence to MissionTracker
- Move unlock evaluation to UnlockService with data-driven conditions

These three sprints would unblock all Phase 9 work (Hidden Signals, Dynamic Unlocks, more threats/bosses/progression) and reduce the maintenance burden across the codebase by ~40%.

---

## 7. Appendix: Codebase Profile

| Category | Files | Total Lines |
|----------|-------|:-----------:|
| **Navigation/Shell** | `MainActivity.kt`, `GameScreen.kt` | 3,915 |
| **Mission System** | 7 files (1 dead) | 1,071 |
| **Progression** | `ProgressionManager.kt`, `GameStats.kt` | 360 |
| **Threat System** | 11 files | 2,131 |
| **HUD / UI** | 6 files | 1,365 |
| **Screens** | 14 files | 2,368 |
| **Rendering** | 6 files | 2,066 |
| **Game Systems** | 9 files | 457 |
| **Data / Models** | 5 files | 730 |
| **Dev / Config** | `DevConfig.kt` | 11 |

**Total codebase:** ~14,800 lines across 66 Kotlin files
**Top 3 files by size:** 40% of the codebase in just 3 files (GameScreen + ActiveThreat + HudWidgets)
**Median file size:** 99 lines — healthy except for the 3 outliers
**Screens:** 14 screen states, 0 are unit-testable outside Compose runtime
