# Sprint 7.5 Report — Rocket Identity

**Date:** 2026-06-22
**Project:** Jump Droid
**Epic:** 7 — Rocket Evolution
**Status:** Completed

---

## 1. Accomplishments

### Rocket Class Framework
*   Evolved `RocketType` from simple stat presets into true gameplay **Classes**.
*   Implemented **Native Traits** for each ship class, providing unique logic that cannot be obtained through modules.
*   Preserved all existing unlock requirements and save data compatibility.

### Native Traits (Implemented)
1.  **Explorer Class (Balanced)**
    *   *Trait:* **Sensor Array**. Native +20% discovery range. Initialized in `restartGame`.
2.  **Striker Class (Scout)**
    *   *Trait:* **Target Lock**. Increased hit radius (50px -> 70px) for weak point collisions, allowing for easier precision strikes.
3.  **Heavy Class (Tank)**
    *   *Trait:* **Kinetic Mass**. Destructible weak points now trigger a massive kinetic shockwave (50 particles, increased screenshake) upon destruction.
4.  **Prototype Class (Experimental)**
    *   *Trait:* **Overclocked Core**. The ship retains full steering authority while in an **Overheated** state, allowing for survival maneuvering even when engines are locked out.

### User Interface
*   Updated `HangarScreen.kt` to clearly display **Class Name**, **Trait Name**, and **Trait Description**.
*   Integrated trait information into the rocket selection cards for better player communication.

---

## 2. Scope Changes

### Planned Work
*   Implement native bonuses and traits for the 4 base ships.
*   Noticeable gameplay differences between classes.

### Added During Development
*   **Physics Loop Refactoring**: Restructured the core physics loop in `GameScreen.kt` to explicitly support "Rule-Breaking" traits like steering during overheat.
*   **Weak Point Logic Expansion**: Updated `ActiveThreat.kt` to differentiate hit detection and destruction effects based on player class.

### Deferred Work
*   (None)

### Technical Debt Created
*   **Trait HUD Feedback**: No on-screen icon for when a trait is "active" (e.g., Target Lock indicator).
*   **Class-Specific Modules**: The framework supports them, but no modules currently require a specific class to equip.

---

## 3. Sprint 7.5 Emergent Features
- [x] **Class Trait System**: Logic-based differentiation beyond stat multipliers.
- [x] **Native Multiplier Injection**: Initialization of player state variables (e.g., `discoveryRangeMultiplier`) based on class.
- [x] **Trait-Specific Visual FX**: Class-exclusive particle bursts and camera feedback.
- [x] **Rule-Breaking Physics Branching**: Support for conditional physics overrides (e.g., Prototype steering).
- [x] **Class Metadata Display**: Enhanced UI communication for non-numerical traits.

---

## 4. Final Recommendation Alignment
Sprint 7.5 has successfully transformed Jump Droid's ships into distinct **Identities**. Players now choose a class for its unique gameplay loop (Exploration, Precision, Impact, or Rule-breaking) rather than just its numbers.
