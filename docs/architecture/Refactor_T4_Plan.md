# Refactor Sprint T4 — Plan: Survival & Encounter Delegation

## Metadata

| Field | Value |
|-------|-------|
| **Sprint** | T4 (Tidying - System Delegation) |
| **Phases** | 2 (T4A Low Risk, T4B Medium Risk) |
| **Branch** | `refactor/system-delegation` |
| **Base commit** | `e79068d` (Refactor T3 complete) |
| **Goal** | Extract survival mechanics, encounter spawning rules, and threat interaction logic from `GameScreen.kt` |
| **Constraints** | Zero behavioral changes; preserve frame-delta precision in physics; no modifications to rendering |
| **Estimated net reduction** | ~750 lines (3,033 → ~2,280) |

---

## Scope

Phase T4 removes the "Gameplay Intelligence" from the monolithic game loop. This involves delegating survival calculations (damage/regen), the "AI Director" (spawning rules), and the complex threat interaction logic to dedicated components.

---

## Phase T4A — Low Risk: Manager Extractions

### 1. SurvivalManager
- Handles damage distribution (shield → integrity), shield regeneration timers, and the 3-phase catastrophic destruction sequence.
- **Target API**: `survivalManager.applyDamage(amount)`, `survivalManager.update(dt)`

### 2. EncounterDirector
- Encapsulates the 3-second spawning timer, zone-specific hazard weighting, and boss arrival thresholds.
- **Target API**: `encounterDirector.update(dt, score, zone, cameraY)`

---

## Phase T4B — Medium Risk: Architectural Shift

### 3. Threat Interaction Delegation
- Moves the massive `when(threat.definition.id)` interaction block from the physics loop into `ActiveThreat`.
- The physics loop will now call a unified interaction method on each active threat.
- **Target API**: `activeThreat.processInteraction(player, sdt, platforms, powerUps)`

---

## Summary

| Metric | Current | After T4 |
|--------|---------|----------|
| GameScreen.kt lines | 3,033 | ~2,280 (−25%) |
| Cumulative Reduction | −30.2% | ~−47.5% |
| Risk level | — | MEDIUM |
| Build verification | — | `assembleDebug` |
