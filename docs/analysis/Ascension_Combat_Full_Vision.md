# Jump Droid: Ascension Combat Full Vision (Planning)

This report outlines the "Three Pillars of Interaction" that will transform Jump Droid into a dynamic combat-survival game.

---

## 1. Threat Offensive & Summoning (The "Call")
**Objective:** Every hostile entity becomes a functional part of the "Great Signal's" defense network.

### New Behaviors
- **Surveyor Probe (The Spotter):**
    - **Detects:** Shows a white laser.
    - **Lock-on:** Laser turns yellow.
    - **The Call:** Laser turns red and a **Major Notification** appears ("REINFORCEMENTS INBOUND").
    - **Action:** Spawns a `HUNTING_DRONE` or `STALKER` nearby.
- **Orbital Sentry (The Sniper):**
    - **Action:** Fires a high-speed energy bolt after a 1.5s charge.
- **Hazards (The Environment):**
    - **Action:** Hazards like `EMP` or `RADIATION` now have "Escalation" chances during boss fights, triggered by the boss.

---

## 2. Player Offensive: The "Hull & Hammer" (The "Punch")
**Objective:** The player’s rocket is a physical weapon.

### Mechanics
- **Kinetic Slam:**
    - High-velocity downward movement acts as an attack.
    - Impact deals `Mass * Velocity` damage to enemies.
    - Perfect for destroying `Surveyor Probes` or `Debris`.
- **Momentum Burst:**
    - Spending 50% Fuel to perform a high-speed horizontal dash. 
    - Destroys anything in the path but increases Heat by 40%.

---

## 3. The "Ascension Satellite Strike" (The "Hammer")
**Objective:** Long-range support from the home team.

### System Design
- **Targeting State:**
    - Hold a "Target" button to slow time slightly and drag a reticle over a threat.
    - **Condition:** Requires an "Ascension Beacon" (found as a rare Power-Up or earned via combos).
- **The Strike:**
    - After a 2s delay, a massive vertical laser beam (Visual effect) wipes out the targeted area.
    - **Lore:** "The Ascension Team is watching your telemetry. They can't help often, but when they do, they clear the sky."

---

## 4. Summary of Planned Interactions

| Entity | Action | Response |
| :--- | :--- | :--- |
| **Scout Drone** | Detects Player | Calls **Hunting Drones** |
| **Sentry** | Charges Beam | Player must **Slam** it to disrupt |
| **Player** | Collects Beacon | Can call **Satellite Strike** |
| **Boss** | Reaches 50% HP | Triggers **Aerosol Swarms** |
