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

Phase T4 removes the "Gameplay Intelligence" from the monolithic game loop. This involves delegating survival calculations (damage/regen), the "AI Director" (spawning rules), and the complex threat interaction logic to dedicated components. This is the final major tidying sprint before full-scale feature expansion in EPIC 5/6.

---

## Phase T4A — Low Risk: Manager Extractions

### 1. SurvivalManager

| Field | Value |
|-------|-------|
| **Location** | `applyDamage`: ~510 \| Update Loop: ~1945–1964 |
| **Description** | Handles damage distribution (shield → integrity), shield regeneration timers, and the 3-phase catastrophic destruction sequence. |
| **Dependencies** | `player`, `dt`, `gameTime` |
| **Target API** | `survivalManager.applyDamage(amount)`, `survivalManager.update(dt)` |

**File:** `SurvivalManager.kt`

---

### 2. EncounterDirector

| Field | Value |
|-------|-------|
| **Location** | Boss Milestones: ~810–835 \| Spawning Logic: ~837–976 |
| **Description** | Encapsulates the 3-second spawning timer, zone-specific hazard weighting, and boss arrival thresholds. |
| **Dependencies** | `score`, `altitudeZone`, `screenWidth`, `cameraY` |
| **Target API** | `encounterDirector.update(dt, score, zone, cameraY)` |

**File:** `EncounterDirector.kt`

---

## Phase T4B — Medium Risk: Architectural Shift

### 3. Threat Interaction Delegation

| Field | Value |
|-------|-------|
| **Location** | Physics Sub-step Loop: ~1082–1430 |
| **Description** | Moves the massive `when(threat.definition.id)` interaction block from the physics loop into `ActiveThreat`. The physics loop will now call a unified interaction method on each active threat. |
| **Dependencies** | `player`, `sdt`, `platforms`, `powerUps` |
| **Target API** | `activeThreat.processInteraction(player, sdt, platforms, powerUps)` |

---

## Execution Order

| Step | Action | Estimated Reduction |
|------|--------|---------------------|
| 1 | Extract `SurvivalManager.kt` | ~120 lines |
| 2 | Extract `EncounterDirector.kt` | ~180 lines |
| 3 | Delegate interaction logic to `ActiveThreat.kt` | ~450 lines |
| 4 | Final cleanup of state variables and substep loop | ~50 lines |
| **Total** | | **~800 lines (gross)** |

---

## Files to Create

| File | Purpose |
|------|---------|
| `SurvivalManager.kt` | Damage distribution, shield regen, and destruction lifecycle |
| `EncounterDirector.kt` | Spawning rules, zone weighting, and boss milestones |

## Files to Modify

| File | Change |
|------|--------|
| `GameScreen.kt` | Replace inline director and survival logic with manager calls; clean physics loop |
| `ActiveThreat.kt` | Accept interaction processing logic from physics loop |
| `docs/CHANGELOG.md` | Add T4 entry |
| `OPENCODE.md` | Update line counts and refactor status |

---

## Summary

| Metric | Current | After T4 |
|--------|---------|----------|
| GameScreen.kt lines | 3,033 | ~2,280 (−25%) |
| Cumulative Reduction | −30.2% | ~−47.5% |
| Risk level | — | MEDIUM |
| Build verification | — | `assembleDebug` |
