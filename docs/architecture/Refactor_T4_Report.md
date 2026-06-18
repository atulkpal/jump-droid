# Refactor Sprint T4 Report: Survival & Encounter Delegation

## Metadata

| Field | Value |
|-------|-------|
| **Sprint** | T4 (Tidying - System Delegation) |
| **Branch** | `refactor/system-delegation` |
| **Base commit** | `e79068d` (Refactor T3 complete) |
| **Goal** | Extract survival mechanics, encounter spawning rules, and threat interaction logic from `GameScreen.kt` |
| **Status** | **FAILED / REVERTED** |

---

## Summary

Sprint T4 attempted a major architectural shift by delegating core gameplay "intelligence" to sub-managers. While the code was successfully extracted and line count was reduced significantly, the run was identified as failed due to multiple bugs and regressions in physics/interaction timing.

**The codebase has been reverted to the Sprint T3 baseline.**

---

## Attempted Changes

### Files Created (Reverted)

- `SurvivalManager.kt`: Damage distribution and destruction lifecycle.
- `EncounterDirector.kt`: Spawning rules and boss milestones.

### Logic Delegated (Reverted)

- **Threat Interaction**: Replaced the massive `when` block in the physics loop with a callback-heavy bridge to `ActiveThreat.kt`.
- **AI Director**: Moved spawn timing and zone weighting.
- **Survival Economy**: Extracted damage application and shield regen.

---

## Reasons for Failure

1. **Coupling**: The physics loop and threat interactions are too tightly coupled for a simple delegation without a more robust event or callback system.
2. **Timing Regressions**: Sub-stepped physics delta (sdt) calculations were inconsistent after moving logic to external classes.
3. **Complexity**: The callback bridge required to maintain visual feedback (bursts, shake, text) became more complex than the original inline code.

---

## Lessons Learned

- Core physics and interaction logic should remain inline until a proper ECS (Entity Component System) or similar decoupled architecture is implemented.
- Extractions should focus on standalone utilities (like Sprint T3) rather than interleaved state mutation.
