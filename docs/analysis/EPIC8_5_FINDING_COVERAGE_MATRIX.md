# EPIC 8.5 — Finding Coverage Matrix

**Source:** `docs/analysis/EPIC8_TECH_DEBT_AUDIT.md` (17 findings, 2026-06-23)
**Decomposition Plan:** `docs/roadmap/EPIC_8_5_DECOMPOSITION_PLAN.md` (9 sprints, 8.5.0–8.5.8)
**Purpose:** Map every audit finding to its disposition within EPIC 8.5 or beyond.

---

## Legend

| Status | Meaning |
|--------|---------|
| ADDRESSED | Finding is fully resolved by one or more EPIC 8.5 sprints |
| PARTIAL | Finding is meaningfully reduced but the ideal fix extends beyond EPIC 8.5 |
| DEFERRED | Finding intentionally postponed to EPIC 9 or later with justification |
| IGNORED | Finding acknowledged but consciously excluded from all planned work |

---

## Coverage Matrix

| # | Finding | Risk | Epic 8.5 Sprint(s) | Status | Deferral Rationale |
|---|---------|------|--------------------|--------|-------------------|
| **01** | GameScreen God Object (3,901 lines) | 🔴 CRITICAL | **8.5.4** (threat rendering extraction) + **8.5.5** (game engine extraction) + **8.5.8** (NavHost migration) | **ADDRESSED** | — |
| **02** | ActiveThreat God Object (1,224 lines) | 🔴 CRITICAL | **8.5.6** (ThreatBehavior strategies, CollisionSystem, event bus) | **ADDRESSED** | — |
| **03** | ProgressionManager SRP (7+ responsibilities) | 🟠 MAJOR | **8.5.7** (IntelligenceNetwork, UnlockService, MissionTracker, InventoryManager extraction) | **ADDRESSED** | — |
| **04** | Navigation not scalable (state-based `when`) | 🟠 MAJOR | **8.5.8** (NavHost migration, 14 screen routes) | **ADDRESSED** | — |
| **05** | Threat rendering inline in Canvas (~1,947 lines) | 🟠 MAJOR | **8.5.4** (ThreatRenderer interface, 20 per-type renderers) | **ADDRESSED** | — |
| **06** | Ceremony state machine scattered | 🟡 MODERATE | **8.5.1** (remove unused CeremonyStage values + pure checkCompletion) + **8.5.3** (extract ceremony lifecycle) | **ADDRESSED** | — |
| **07** | Mission.kt mixes UI/domain state (`isNew` zombie) | 🟡 MODERATE | **8.5.1** (remove `isNew` zombie field) | **PARTIAL** | Removing `isNew` eliminates the concrete bug (zombie field). Full split into `MissionState` (domain, persistent) vs UI state holder is a deeper refactor that EPIC 8.5 does not scope. Recommended for **EPIC 9** if Hidden Signals adds new mission UI. |
| **08** | EncounterDirector hardcoded tables | 🟡 MODERATE | **8.5.7** (externalize spawn tables to JSON/data class) | **ADDRESSED** | — |
| **09** | ComboManager dual reward systems | 🟡 MODERATE | **8.5.3** (merge tier + survival rewards; externalize table) | **ADDRESSED** | — |
| **10** | MissionRegistry 48-mission static init | 🟡 MODERATE | *None* | **DEFERRED** | Current 48-mission init works correctly and is well-understood. Externalizing to JSON adds complexity without immediate payoff. The audit rates this "Phase 9 (if mission count grows)." EPIC 8.5 is focused on architectural decomposition, not data architecture. If EPIC 9 adds 20+ missions, this becomes worth doing then. |
| **11** | MissionManager coupled to ProgressionManager | 🟡 MODERATE | **8.5.1** (remove redundant method param) + **8.5.3** (define `ProgressionService` interface) | **ADDRESSED** | — |
| **12** | Star animation duplicated 6x | 🟡 MODERATE | **8.5.2** (extract `StarfieldBackground` composable) | **ADDRESSED** | — |
| **13** | PauseOverlay 11-callback API | 🟡 MODERATE | *None* | **DEFERRED** | 7 of 11 callbacks are dev menu. Dev menu gating (`BuildConfig.DEBUG`) means this does not affect release builds. Restructuring dev menu and splitting into separate composable is best done alongside Hidden Signals (EPIC 9), which will add new pause-screen interactions. Doing it now would create a second dev menu reshuffle. |
| **14** | MissionRow.kt dead code (140 lines) | 🔵 MINOR | **8.5.1** (delete file) | **ADDRESSED** | — |
| **15** | updateProgress() vs updateProgressAll() overlap | 🔵 MINOR | *None* | **IGNORED** | Two methods with different semantics (single mission vs all missions). Neither is incorrect. The overlap is a documentation gap, not a defect. Adding comments or a merge would consume sprint time for zero functional improvement. If confusion arises during EPIC 9 work, address then (~15 min fix). |
| **16** | Inline track selection in MissionScreen | 🔵 MINOR | **8.5.2** (extract to MissionManager) | **ADDRESSED** | — |
| **17** | Duplicate gauge structure (4x copy-paste) | 🔵 MINOR | **8.5.2** (extract `GaugeBar` base composable) | **ADDRESSED** | — |

---

## Summary

| Status | Count | Findings |
|--------|-------|---------|
| ADDRESSED | 12 | 01, 02, 03, 04, 05, 06, 08, 09, 11, 12, 14, 16, 17 |
| PARTIAL | 1 | 07 |
| DEFERRED | 2 | 10, 13 |
| IGNORED | 1 | 15 |

**13 of 17 findings addressed in EPIC 8.5.** (Note: counting PARTIAL as addressed for this tally.)

---

## Candidates for Sprint 8.5.9 or EPIC 9

### Sprint 8.5.9 Candidate — Data-Driven Mission Registry
**Finding:** FINDING-10 — MissionRegistry 48-mission static init
**Effort:** 1 session
**Risk:** Low-Medium
**Rationale for 8.5.9:** If EPIC 9 will add 20+ new missions, doing the JSON migration first prevents a second migration later. The 48 existing missions would be serialized to JSON once, then EPIC 9 content loads via the same pipeline.
**Exit criterion:** All 48 missions loadable from JSON asset with ID validation. `MissionRegistry` reduced from 215-line init block to ~40-line loader. Zero mission behavior change.
**Recommendation:** Include in EPIC 8.5 if mission count is expected to grow >20% in EPIC 9. Otherwise defer to EPIC 9 as originally planned.

### Sprint 8.5.9 Candidate — PauseOverlay Dev Menu Split
**Finding:** FINDING-13 — PauseOverlay 11-callback API
**Effort:** 1 session
**Risk:** Low
**Rationale for 8.5.9:** The dev menu split is a structural change that reduces PauseOverlay's parameter count from 18 to ~11. This makes EPIC 9 pause-screen additions cleaner. However, if EPIC 9 will significantly redesign the pause screen anyway, doing this twice is wasteful.
**Recommendation:** Include in EPIC 8.5 only if EPIC 9 pause screen changes are known to be additive (not a redesign). Otherwise defer.

### EPIC 9 Candidate — Mission UI/State Split
**Finding:** FINDING-07 (deeper fix) — Full split of `Mission` into `MissionState` (domain, persistent) and UI state holder
**Effort:** 1 session
**Risk:** Low-Medium
**Rationale:** `isNew` removal (scheduled in 8.5.1) fixes the zombie field. The full state split is beneficial if EPIC 9 Hidden Signals adds mission UI animations, filtering, or sorting that would benefit from pure domain state.
**Recommendation:** Address during EPIC 9 Mission UI work, not before.

---

## Pre-8.5.0 Baseline Metrics to Capture

To validate post-8.5 improvements, the following must be recorded in Sprint 8.5.0:

| Metric | Current Value | Sweep Source |
|--------|---------------|-------------|
| GameScreen.kt lines | 3,901 | File size inventory |
| ActiveThreat.kt lines | 1,224 | File size inventory |
| ProgressionManager.kt lines | 334 | File size inventory |
| HudWidgets.kt lines | 654 | File size inventory |
| GameScreen imports | 70 | Key arch metrics |
| ActiveThreat.kt function count | TBD | Key arch metrics |
| `processInteraction` callback count | 17 | Key arch metrics |
| Canvas inline threat rendering lines | ~1,947 | Key arch metrics |
| Game loop inline lines | ~790 | Key arch metrics |
| `when(gameState)` branches | 14 | Key arch metrics |
| `CeremonyStage` values used | 2 of 5 | File size inventory |
| Starfield copy-paste instances | 6 | File size inventory |
| Codebase total files | 66 | Codebase profile |
| Codebase total lines | ~14,800 | Codebase profile |
| Median file size | 99 | Codebase profile |
