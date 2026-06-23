# EPIC 8 — Missions & Progression Tracker

**Status:** Functionally Complete — Awaiting Runtime Signoff
**Next:** EPIC 8.5 Architecture Decomposition (see `EPIC_8_5_DECOMPOSITION_PLAN.md`)
**Migration Branch:** `epic8-mission-migration`
**Last Validated:** 2026-06-23
**Latest Build:** `BUILD SUCCESSFUL` (1s, all up-to-date)

## Sprint 8.1 — Migration & Logic
*Goal: Port content from prototype and build new backend.*

- [x] Phase 1: Mission Data Migration (Models & Aligned Registry)
- [x] Phase 2: Intelligence Network (Real-time Stat Tracking)
- [x] Phase 3: Mission Reward Integration (Permanent account bridge)
- [x] Phase 4: Mission UI & Claim Flow (Manual grid interaction)
- [x] Phase 5: Mission UX & Gameplay Communication (Redesign & Notifications)
- [x] Unified Recovery Sprint (Visual & Logic Alignment)
- [x] EPIC 8 Polish Pass (Navigation, Readability, Noise Reduction)

## Phase 1 — Data Model Migration
- [x] Refactor `Mission.kt` for sealed reward class support
- [x] Align `ObjectiveType` with engine metrics
- [x] Manually re-map 48 mission templates in `MissionRegistry.kt`
- [x] Implement prerequisite and hidden mission metadata

## Phase 2 — Intelligence Network
- [x] Create `GameStats.kt` data schema
- [x] Implement background tracking for 19 gameplay metrics
- [x] Inject stat-hooks into Physics Sub-steps
- [x] Sync session stats to `ProgressionManager` lifetime data

## Phase 3 — Reward Integration
- [x] Implement `grantReward` in `ProgressionManager`
- [x] Connect Module Unlocks to mission completion
- [x] Connect Artifact discovery to mission completion
- [x] Implement Achievement auto-unlock via rewards

## Phase 4 — Mission Screen & Claiming
- [x] Update `MissionScreen.kt` colors and premium theme
- [x] Implement manual "Claim" button logic
- [x] Link claim action to `ProgressionManager.grantReward`
- [x] Mission Dashboard & 12-Track system implemented

## Phase 5 — HUD & UX Polish
- [x] Circular Combo Timer (Moved to Top Left)
- [x] Radar Sweep animation polish
- [x] Remove legacy HUD mission cards
- [x] Mission Card Readability (Track Row redesign)
- [x] Celebration Spam Audit & Consolidation
- [x] Navigation route verification (Back to Command)

## Release Candidate Readiness
- [x] **Mission Screen Divergence**: Transitioned to 12-Track dashboard.
- [x] **UI Sync**: Unified Hangar counter with Mission Log set data.
- [x] **Visual Regression**: Restored geometric cut corners to menu buttons.
- [x] **HUD De-clutter**: Removed `newMissionOverlay` and legacy cards.
- [x] **Route Mismatch**: Resolved MISSION DATA naming collision; created ABOUT screen.
- [x] **Communication Audit**: Completed priority-based notification filtering.

## Technical Audit Sprints (EPIC 8 Stabilization + Polish + Mission Events)
- [x] **Stabilization Sprint** — 6 issues fixed: mission progress persistence, lifetimeMissionsCompleted, cumulative mission progress, Hangar counter, artifact double-count, multi-reward display
- [x] **Polish Sprint** — 7 tasks completed: terminal routing, celebration cleanup, notification spam, combo ring, claim feedback, dashboard readability, gauge font sizes
- [x] **Mission Event Audit** — Fixed repeated mission completion notifications at run start by preventing re-activation of completed missions and clearing ceremony state on restart

## EPIC 8.5 — Architecture Decomposition (Planned)
See `docs/roadmap/EPIC_8_5_DECOMPOSITION_PLAN.md` for the full 8-sprint structure.

**Goals:**
- Decompose GameScreen.kt (3,901 lines) and ActiveThreat.kt (1,224 lines) God Objects
- Extract game engine, threat renderers, threat AI strategies, collision system
- Split ProgressionManager into domain services
- Migrate navigation to NavHost
- Eliminate dead code (MissionRow.kt, zombie fields)
- Reduce duplication (GaugeBar, StarfieldBackground)

## Deferred Polish (Post-EPIC 8.5 — Future Work)
- [ ] Track mission category specific stats (e.g. "Platform Stay")
- [ ] Implement multi-reward mission backend support
- [ ] Balance pass on god-tier mission thresholds
- [ ] Visual noise during high-combo streaks (excessive floating text)
- [ ] Capture EPIC 8 screenshots for documentation
