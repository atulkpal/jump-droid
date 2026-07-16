# EPIC 7 — Rocket Evolution Discovery Audit

**Date:** 2026-06-22
**Project:** Jump Droid
**Objective:** Determine system readiness for "Rocket = Build" transition.

---

## Current Systems

### 1. Rocket Class System
*   **Implementation**: `RocketType` enum in `Models.kt`.
*   **Parameters**: Multipliers for `thrust`, `fuel`, and `heat`.
*   **Classes**: Balanced, Scout, Tank, Experimental.
*   **Status**: Fully functional with visual shifts in `RocketRenderer.kt`.

### 2. Player Progression & Persistence
*   **Manager**: `ProgressionManager.kt`.
*   **Save Logic**: `SharedPreferences` via `android.content.edit`.
*   **Data Stored**: High score, Artifact records (date, count, altitude, zone), Permanent stat upgrades (Integrity, Shield).
*   **Unlocks**: Score-based milestones for Rocket Types and Lore.

### 3. Survival Mechanics
*   **Manager**: `SurvivalManager.kt`.
*   **Logic**: Damage distribution (Shield -> Hull), shield regeneration delay, and destruction lifecycle.
*   **Variables**: `maxShield`, `shieldRegenPauseTimer`, `maxIntegrity` in `Player` class.

### 4. Reward Framework
*   **Logic**: `MissionReward.kt` (Sealed Class) and `ProgressionManager.checkUnlocks`.
*   **Existing Rewards**: Artifacts, PowerUps, Unlocks (Rockets), Achievements.

### 5. Combat & Interaction
*   **Mechanics**: Proximity-based collision and weak point system (`ActiveThreat.kt`).
*   **Offense**: Rocket-to-WeakPoint collision (Kinetic).
*   **Ranged**: `ProjectileManager.kt` exists but is currently THREAT-only.

---

## Reusable Systems

| System | Reusability Potential |
|--------|-----------------------|
| **ProgressionManager** | High. Can easily store `equipped_module_X` strings/IDs in SharedPreferences. |
| **MissionReward** | High. Extending the sealed class to include `Module` type is trivial. |
| **SurvivalManager** | High. The `applyDamage` and `update` loops are already decoupled and ready for module hooks. |
| **HangarScreen** | Medium. Layout supports vertical scrolling; module slot UI can be appended or moved to a sub-tab. |
| **RocketRenderer** | Medium. Can use a `DrawScope` hook to overlay visual module indicators (e.g., thicker hull plating). |

---

## Missing Systems

### 1. Module Framework
*   **Definition**: No `Module` data class or interface currently exists.
*   **Registration**: No central registry of available modules.

### 2. Loadout/Build Management
*   **Logic**: No system to manage "Equipped" state or enforce slot limits (e.g., 2 slots per rocket).
*   **Validation**: No logic to prevent incompatible module combinations.

### 3. Module Execution Hooks
*   **Hooks**: While the survival update loop exists, there is no generic "OnHit" or "OnThrust" observer pattern that modules can subscribe to.

### 4. Loadout UI
*   **Interface**: No screen currently exists for the "Equip/Unequip" interaction.

---

## Technical Risks

1.  **Physics Performance**: Injecting complex module logic into the 4-substep physics loop (`GameScreen.kt`) could cause frame drops if not optimized (Avoid allocation in substeps).
2.  **Stat Compounding**: Multiple modules stacking multipliers (e.g., +20% Shield and +20% Shield) needs a robust calculation order (Additive vs Multiplicative).
3.  **Persistence Bloat**: Storing unique module states/variants in SharedPreferences might become messy; a structured JSON or indexed naming convention is recommended.

---

## Recommended EPIC 7 Structure

### Phase 1 — Framework (Foundation)
*   **Task 1**: Create `Module` data structure and `ModuleRegistry`.
*   **Task 2**: Implement `LoadoutManager` for persistence and slot logic.
*   **Task 3**: Refactor `SurvivalManager` to accept a list of active `ModuleHooks`.

### Phase 2 — Hull & Shield (Core)
*   **Task 4**: Implement Stat-Modifier modules (Reinforced Hull, Fast Recharge).
*   **Task 5**: Implement Reactive modules (Emergency Shield on hit).

### Phase 3 — Mobility & Heat (Specialization)
*   **Task 6**: Implement Thruster and Vector modules.
*   **Task 7**: Implement Heat Sink and Battery modules.

### Phase 4 — Utility & Support (Strategic)
*   **Task 8**: Implement Scanner and Drone modules (Visual overlays).

### Phase 5 — Class Synergy (Differentiation)
*   **Task 9**: Assign "Native Traits" to Rocket Classes (e.g., Scout gets +1 Utility Slot).

---

## Estimated Completion

*   **Low Effort**: Module definitions, Stat-Modifier modules, basic persistence.
*   **Medium Effort**: Hangar UI expansion, Scanner/Locator overlays.
*   **High Effort**: Reactive logic modules (Drones, Emergency Beacons), Class balancing.

---

## Final Recommendation

### "Based on the current codebase, what should EPIC 7 actually be?"

EPIC 7 should be an **Expansion of the Player State**.

The current codebase is very "Stat Heavy" but "Logic Light" for the player. We have a robust system for *receiving* damage and *tracking* discoveries, but the player is effectively a static block of multipliers (RocketType).

**EPIC 7 should be the implementation of a "Modifier Layer" between the Player and the Managers.**

Instead of `GameScreen` directly modifying `player.shield`, it should pass events to a `ModuleController` which determines the final result. This turns the Rocket from a **fixed vehicle** into a **dynamic capability set**, allowing the player to solve altitude-specific problems (e.g., "I keep dying to Heat in Orbit, I need a Battery Build") through strategic choice.
