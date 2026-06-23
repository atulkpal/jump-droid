# Jump Droid: Combat & Stealth Foundations (Planning)

This report outlines the structural requirements for the **Combat Layer** and **Stealth Layer**. These systems must exist before specialized rockets (like Kinetic or Stealth hulls) can be implemented.

---

## 1. The Stealth Layer (Awareness Engine)
**Objective:** Replace hardcoded "if distance < X" checks with a dynamic detection system.

### Architecture
- **Detection Profile (`Player` class):**
    - `visibilityRadius`: Base radius (e.g., 400f).
    - `noiseLevel`: (Future) Increased by thrusting, reduced by drifting.
- **Threat Awareness (`ActiveThreat.kt`):**
    - Threats no longer check `dist < 400f`. 
    - Instead, they check `dist < (player.visibilityRadius * threat.perceptionMult)`.
- **States:**
    - `IDLE`: Threat is on patrol.
    - `SUSPICIOUS`: Threat has detected something but isn't locked on (telegraph with yellow pulse).
    - `HUNTING`: Threat is locked on (telegraph with red pulse).

---

## 2. The Combat Layer (Interaction & Damage Engine)
**Objective:** Standardize how damage is dealt *to* threats and how "Contact Combat" works.

### Architecture
- **Threat Health & Vulnerability:**
    - Standardize `health` and `maxHealth` across all `ActiveThreat` instances.
    - Define `VulnerabilityType`: `PHYSICAL` (Collisions), `ENERGY` (Projectiles), `NONE` (Hazards).
- **Combat Resolution (`ActiveThreat.processInteraction`):**
    - **Current:** Collision always damages the player.
    - **New:** 
        - If `player.isAttacking` (e.g., Kinetic Slam state):
            - Calculate `impactForce`.
            - Apply `impactForce` to `threat.health`.
            - Trigger `onThreatDamaged` callback (shaking, particles).
- **Attack States (`Player` class):**
    - `combatState`: `NONE`, `SLAM`, `DASH`.
    - `isInvulnerableDuringAttack`: Boolean (used by Kinetic rocket).

---

## 3. Integration into Orchestrator (`GameScreen.kt`)

### Awareness Loop
1.  **EncounterDirector** determines which threats are active.
2.  **ThreatManager** updates threat AI.
3.  Each threat queries the **Stealth Layer** to determine if the player is "Visible."

### Combat Loop
1.  **Physics Substep** detects a collision.
2.  **Combat Layer** determines if it's a "Hit" (Player damages Threat) or a "Hurt" (Threat damages Player).
3.  **SurvivalManager** applies hull/shield damage if "Hurt."
4.  **ThreatManager** handles threat destruction if "Hit."

---

## 4. Way Ahead: Strategic Phasing

1.  **Phase 1: Awareness Refactor**
    - Move all hardcoded detection ranges in `ActiveThreat.kt` to a property-based system.
    - Add `visibilityRadius` to the `Player` model.
2.  **Phase 2: Contact Combat System**
    - Implement the "Kinetic Slam" logic where velocity-based collisions can damage specific threat types.
3.  **Phase 3: Visual Signaling (UX)**
    - Add "Detection Meters" or clearer visual states to threats (Idle vs. Alert).
4.  **Phase 4: Rocket Implementation**
    - Finally, plug the `Stealth` and `Kinetic` rockets into these established systems.
