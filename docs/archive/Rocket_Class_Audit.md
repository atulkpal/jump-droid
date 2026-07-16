# Rocket Identity Audit — Sprint 7.5

**Date:** 2026-06-22
**Project:** Jump Droid
**Epic:** 7 — Rocket Evolution
**Objective:** Determine if existing rockets are true Classes or Stat Profiles.

---

## Current Systems Audit

### 1. Existing Rocket Types
The codebase identifies four distinct rocket types defined in the `RocketType` enum (`Models.kt`):
1. **Balanced**: The baseline explorer.
2. **Scout**: High speed, low endurance.
3. **Tank**: High endurance, low maneuverability.
4. **Experimental**: High power, extreme risk.

### 2. Unlock Requirements
Unlocks are purely score-based (representing altitude reached) and managed by `ProgressionManager.kt`:
*   **Balanced**: 0m (Starter)
*   **Scout**: 2,000m
*   **Tank**: 5,000m
*   **Experimental**: 10,000m

### 3. Stat Modifiers & Gameplay Mechanics
Rockets modify three primary multipliers applied in the physics loop (`GameScreen.kt`):
*   `thrustMult`: Affects upward acceleration.
*   `fuelMult`: Affects maximum fuel capacity and consumption rate.
*   `heatMult`: Affects engine heat generation rate.

| Rocket | Unlock | Bonuses | Penalties | Unique Mechanics |
| :--- | :--- | :--- | :--- | :--- |
| **Balanced** | 0m | Baseline (1.0x) | Baseline (1.0x) | None |
| **Scout** | 2000m | +25% Thrust, -10% Heat | -30% Fuel | None |
| **Tank** | 5000m | +50% Fuel, -20% Heat | -15% Thrust | None |
| **Experimental**| 10000m | +50% Thrust | +40% Heat | None |

### 4. Categorization: Type vs. Class
**Current Classification: B. Rocket Types (Stat Profiles)**

**Reasoning:**
*   There is zero unique logic or specialized behaviors tied to specific rockets in `ActiveThreat.kt`, `SurvivalManager.kt`, or `GameScreen.kt`.
*   All rockets share the same physics code; only the variables passed into the equations change.
*   They function as "difficulty modes" or "playstyle presets" rather than unique classes with distinct abilities.

### 5. Module Synergies
The current module system (`Sprint 7.1-7.4`) applies multipliers *on top* of the rocket's base multipliers.
*   *Example:* A Scout (+25% Thrust) with Burst Thrusters (+30% Thrust) results in a cumulative ~1.62x thrust multiplier.
*   The system is highly synergistic but currently lacks "Class-only" modules or traits.

### 6. Existing UI
The `HangarScreen.kt` provides a high-quality vertical list showing:
*   Rocket title and description.
*   Unlock threshold.
*   Numerical summary of Thrust, Fuel, and Thermal stats.
*   Selection/Equip interaction.

---

## Recommendations

### Final Recommendation: Convert Rocket Types into true Classes

**Strategy:**
Instead of just being numbers, each rocket should have a **Native Trait** (a permanent, unique ability) and a **Slot Specialization**. This aligns with the "Rocket = Build" goal of EPIC 7.

**Proposed Class Traits for Sprint 7.5:**

1.  **Balanced -> Explorer Class**
    *   *Trait:* **Sensor Array**. Native +20% discovery range (stacks with Survey Scanner).
    *   *Specialization:* 2 Universal Slots.

2.  **Scout -> Striker Class**
    *   *Trait:* **Kinetic Dampening**. Take 50% less damage from weak point collisions (High-speed melee focus).
    *   *Specialization:* 1 Engine Slot, 1 Universal Slot.

3.  **Tank -> Heavy Class**
    *   *Trait:* **Hardened Plating**. Native -15% damage taken from all sources (stacks with Impact Dampeners).
    *   *Specialization:* 1 Hull Slot, 1 Universal Slot.

4.  **Experimental -> Prototype Class**
    *   *Trait:* **Overclocked Core**. While overheated, the rocket can still steer (but not thrust).
    *   *Specialization:* 3 Wildcard Slots (High customization potential).

---

## Conclusion

The current system provides an excellent visual and statistical foundation, but fails the "Rocket = Build" test because the player choice is purely mathematical. By implementing the **Class Traits** above, we transform presets into identities.
