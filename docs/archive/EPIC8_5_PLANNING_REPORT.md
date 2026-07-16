# EPIC 8.5 — Architecture Decomposition Planning Report

**Date:** 2026-06-23
**Author:** AI Agent (OpenCode)
**Status:** Planning Complete
**Branch:** `epic8-mission-migration`
**Base:** `epic8-recovery-stable` (`afbc562`)

---

## 1. Summary

EPIC 8.5 is a dedicated architecture decomposition sprint created in response to the EPIC 8 Tech Debt Audit, which identified 2 CRITICAL God Objects blocking Phase 9 feature work:

- **GameScreen.kt** — 3,901 lines (70 imports, ~30 subsystems)
- **ActiveThreat.kt** — 1,224 lines (AI + collision + rendering mixed, 17-callback API)

EPIC 8.5 targets these files alongside 15 other findings (3 MAJOR, 8 MODERATE, 4 MINOR) to create a sustainable architecture for future development.

---

## 2. Context

### Why Not Defer to EPIC 9?

The planned Phase 9 features (Hidden Signals, Dynamic Unlocks, additional threats/bosses) would:
- Add more screens to the state-based `when(gameState)` block, growing GameScreen.kt further
- Add more unlock logic to the SRP-violating ProgressionManager
- Add rendering code to the ~1,947 lines of inline Canvas rendering
- Add more threat types to the 17-callback `processInteraction` API

Deferring decomposition would compound the debt. EPIC 8.5 addresses it now.

### Why Not Merge into EPIC 8?

EPIC 8 is a feature sprint (Missions & Progression). EPIC 8.5 is a structural sprint. Merging risks:
1. Scope creep diluting EPIC 8's mission focus
2. Breaking mission functionality during the recovery validation window
3. No clear "done" criteria for either sprint

---

## 3. Sprint Structure

| Sprint | Focus | Sessions | Risk | Parallel |
|--------|-------|----------|------|----------|
| 8.5.0 | Baseline Capture (tag, metrics, screenshots) | 1 | None | — |
| 8.5.1 | Low-Risk Cleanup (dead code, zombie fields) | 1 | None | — |
| 8.5.2 | HUD Decomposition (GaugeBar, Starfield, filters) | 1 | Low | — |
| 8.5.3 | Notification & Celebration Extraction | 1 | Low | — |
| 8.5.4 | Threat Rendering Extraction | 2-3 | Medium | With 8.5.5 |
| 8.5.5 | Game Engine Boundary Creation | 2-3 | Medium-High | With 8.5.4 |
| 8.5.6 | ActiveThreat Strategy Architecture | 2-3 | Medium | — |
| 8.5.7 | Progression Service Decomposition | 1-2 | Medium | — |
| 8.5.8 | Navigation Migration | 2-3 | Medium | — |

**Total:** 13-18 sessions, 9 sprints.

---

## 4. Key Decisions

1. **EPIC 9 officially deferred** until EPIC 8.5 completion — documented in AGENTS.md and JumpDroid_EPIC_Tracker.md
2. **Backward compatibility required** — all extracted managers must retain identical public API
3. **No gameplay changes** — zero behavior modification during decomposition
4. **Synchronous event bus** for threat-game interaction (no async, same-thread dispatch)
5. **GameState enum preserved** as navigation target during NavHost transition
6. **Sprint 8.5.0 inserted before 8.5.1** — baseline capture establishes known-good EPIC 8 state before any decomposition begins

---

## 5. Risk Assessment

| Risk | Mitigation |
|------|-----------|
| Physics timing breakage | Extract in stages with cherry-pick checkpoints between each |
| NavHost breaks 14 screens | Preserve GameState as bridge; validate all screens render |
| Event bus timing bugs | Synchronous dispatch; no async; same test patterns |
| Progression persistence breakage | Keep facade methods until all call sites migrated |
| Sprint scope creep | Hard exit criteria per sprint; cut unstarted items if over-estimate |

---

## 6. Post-8.5 Target State

| File | Before | After | Reduction |
|------|--------|-------|-----------|
| GameScreen.kt | 3,901 lines | ~1,500 | 62% |
| ActiveThreat.kt | 1,224 lines | ~250 | 80% |
| ProgressionManager.kt | 334 lines | ~80 (facade) | 76% |
| HudWidgets.kt | 654 lines | ~400 | 39% |
| MissionRow.kt | 140 lines | 0 (deleted) | 100% |

New files: ~25 (renderers, strategies, services, composables)
Total codebase change: ~14,800 → ~14,500 lines

---

## 7. Dependencies

- EPIC 8 validation signoff (precondition)
- `epic8-mission-migration` branch (base)
- No concurrent feature branches during EPIC 8.5
- EPIC8_TECH_DEBT_AUDIT.md (priority reference)

---

## 8. Next Steps

1. ✅ Decomposition plan written
2. ✅ Tech debt audit delivered
3. ✅ AGENTS.md updated for EPIC 8.5 transition
4. ✅ Trackers updated (EPIC 8 complete, 8.5 planned, 9 deferred)
5. ⏳ Start Sprint 8.5.0 (Baseline Capture) — tag `epic8.5-baseline`, verify build, capture file metrics and screenshots, produce baseline report
