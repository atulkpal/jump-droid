# EPIC 9 — Hidden Signals & Dynamic Unlocks

**Status:** READY 🚀
**Branch:** `epic9`
**Prerequisites:** EPIC 8.5 Architecture Decomposition COMPLETE ✅
**Execution Plan:** `docs/roadmap/EPIC_9_EXECUTION_PLAN.md`

---

- [x] Sprint 9.1: Hidden Signal Reveal Mechanics
- [x] Sprint 9.2: Dynamic Unlock Engine
- [x] Sprint 9.3: Artifact Set Bonuses
- [x] Sprint 9.4: Rare Lore Logs
- [x] Sprint 9.5: Blueprints & Stabilization

---

## Sprint 9.1: Hidden Signal Reveal Mechanics

- [x] 1.1 Create `GlitchText` composable
- [x] 1.2 Add `crypticHint` field to `Mission`
- [x] 1.3 Populate hints for 7 hidden missions
- [x] 1.4 Update `HiddenSignalsCard` for encrypted state (glitch + hint + masked progress)
- [x] 1.5 Create `SignalDecodedOverlay` (full-screen reveal ceremony)
- [x] 1.6 Trigger reveal ceremony from `MissionManager.checkUnlocks()` via callback
- [x] 1.7 Persist unlock state via `ProgressionService`

## Sprint 9.2: Dynamic Unlock Engine

- [x] 2.1 Enhance `UnlockRequirement` with `LogicOp.AND` / `LogicOp.OR`
- [x] 2.2 Create `UnlockEngine` evaluator (7 unlock types: SCORE, ALTITUDE, DISCOVERY, MISSION, ARTIFACT, ARTIFACT_SET, MISSION_COMPLETE)
- [x] 2.3 Update 17 module `unlockRequirement` fields in `ModuleRegistry`
- [x] 2.4 Gate module equipping in `LoadoutScreen` + `LoadoutManager` (Incl. 🔒 Icon Overlay)
- [x] 2.5 Add telemetry hook in `ProgressionManager.checkModuleUnlocks()`
- [x] 2.6 Module unlock notification via `FloatingTextManager` (Incl. Queue logic)

## Sprint 9.3: Artifact Set Bonuses

- [x] 3.1 Create `ArtifactSet` data model with `ArtifactBonus` sealed class
- [x] 3.2 Define 3-5 artifact sets (grouped DiscoveryTypes)
- [x] 3.3 Evaluate and apply set bonuses via `ProgressionManager`
- [x] 3.4 Apply bonuses to Player stats (fuel regen, shield regen, etc.)
- [x] 3.5 Set progress UI in ArchiveScreen

## Sprint 9.4: Rare Lore Logs

- [x] 4.1 Create `LoreLog` data class + define 10 altitude-triggered logs
- [x] 4.2 Persist discovered logs via `ProgressionService`
- [x] 4.3 Trigger lore on altitude threshold in `ProgressionManager`
- [x] 4.4 Lore log viewer in ArchiveScreen (category tabs + full-text modal)
- [x] 4.5 In-game lore popup via FloatingText + NotificationManager

## Sprint 9.5: Blueprints & Stabilization

- [x] 5.1 Add `BlueprintType` enum (3 blueprints)
- [x] 5.2 Define blueprint unlock requirements
- [x] 5.3 Persist unlocked blueprints
- [x] 5.4 Evaluate blueprint unlocks in `ProgressionManager`
- [x] 5.5 Blueprint notification on unlock
- [x] 5.6 Blueprint preview section in Hangar
- [x] 5.7 Visual regression pass (HUD clarity with multiple signals active)
- [x] 5.8 Balance pass (reveal thresholds, progression pacing)

---

## Artifact Sets (Data)

- [x] "The Great Signal" — 3 lore-related discoveries
- [x] "Planetary Ascent" — 3 area discoveries
- [x] "Deep Void" — 3 space discoveries
- [x] "Efficiency Protocol" — 3 efficiency discoveries
- [x] "Core Systems" — 3 system discoveries

## Lore Logs (Content)

- [x] 10 logs written at altitude milestones (500-10,000m)
- [x] 5 categories: SIGNAL, SURVIVOR, ANCIENT, VOID, EPILOGUE

## Blueprints (Data)

- [x] Engine Trail: Cyan
- [x] HUD Theme: Amber
- [x] Rocket Skin: Obsidian

---

## Definition of Done

- [ ] All 7 hidden missions have unique cryptic hints and glitch states
- [ ] 17 modules gated by UnlockEngine with AND/OR logic
- [ ] 3-5 artifact sets grant passive bonuses verified in-game
- [ ] 10 lore logs with Archive viewer
- [ ] 3 blueprints unlockable (visual rendering deferred)
- [ ] Build green, `GameScreen.kt` stays under 2,200 lines
- [ ] No regression on EPIC 8.5 content
