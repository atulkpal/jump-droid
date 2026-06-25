# Jump Droid: Rocket Library

This document is the permanent content vault for all Rocket Classes in Jump Droid, defining current hulls and future experimental concepts.

---

## A. Existing Rockets

Currently implemented rocket classes with distinct flight characteristics and visual identities.

### 1. Balanced Rocket
*   **Name:** Balanced Rocket
*   **Description:** Standard exploration craft. Reliable and stable.
*   **Visual Appearance:** Sleek white fuselage with red fins and cyan cockpit.
*   **Strengths:** No weaknesses; standard fuel and heat management.
*   **Weaknesses:** None (baseline stats).
*   **Unlock Method:** Default class.
*   **Gameplay Identity:** The "All-Rounder" for learning mechanics.

### 2. Scout Rocket
*   **Name:** Scout Rocket
*   **Description:** High thrust, low fuel. Designed for rapid surveying.
*   **Visual Appearance:** Slender gold fuselage with aggressive fin geometry.
*   **Strengths:** 1.25x Thrust; 0.9x Heat generation.
*   **Weaknesses:** 0.7x Fuel capacity.
*   **Unlock Method:** Reach Score 2000.
*   **Gameplay Identity:** The "Speeder" for rapid ascent and skilled dodging.

### 3. Tank Rocket
*   **Name:** Tank Rocket
*   **Description:** Massive fuel, low maneuverability. Built for long-range expeditions.
*   **Visual Appearance:** Heavy industrial gray fuselage with reinforced plating.
*   **Strengths:** 1.5x Fuel capacity; 0.8x Heat generation.
*   **Weaknesses:** 0.85x Thrust power; heavier steering feel.
*   **Unlock Method:** Reach Score 5000.
*   **Gameplay Identity:** The "Endurance" hull for surviving between resource stations.

### 4. Experimental Rocket
*   **Name:** Experimental Rocket
*   **Description:** Exceptional thrust, high heat. A dangerous prototype.
*   **Visual Appearance:** Glossy purple hull with glowing plasma conduits.
*   **Strengths:** 1.5x Thrust power.
*   **Weaknesses:** 1.4x Heat generation.
*   **Unlock Method:** Reach Score 10000.
*   **Gameplay Identity:** The "High-Risk" hull for experts; requires perfect thermal timing.

---

## B. Future Rockets

Proposed hull classes for advanced playstyles.

### 1. Stealth Hull
*   **Classification:** APPROVED
*   **Description:** Reduces detection range of enemies like Scout Drones and Orbital Sentries.
*   **Gameplay Role:** Evasion-focused playstyle; allows bypassing dense combat zones.
*   **Visual Concept:** Matte black, angular "F-117" inspired geometry.
*   **Complexity:** Medium. Requires modifying threat detection radius logic.
*   **Status:** APPROVED.

---

## C. Omega Modules (Legendary Tier)

Specialized, game-breaking modules earned only after reaching the Singularity. Only equipable in Eternal Mode or Prestige Tiers.

### 1. Void Engine (MOD_VOID_ENGINE)
*   **Description:** Provides infinite fuel by drawing energy from spatial folding.
*   **Gameplay Effect:** Fuel consumption set to 0.
*   **Visual:** Constant dark-matter trail behind the rocket.
*   **Status:** APPROVED (EPIC 11).

### 2. Singularity Core (MOD_SINGULARITY_CORE)
*   **Description:** A miniaturized gravitational anchor.
*   **Gameplay Effect:** 100% stability; immune to wind, turbulence, and gravity shear.
*   **Visual:** Ethereal white-noise aura surrounding the hull.
*   **Status:** APPROVED (EPIC 11).

### 2. Kinetic Reflector
*   **Classification:** APPROVED
*   **Description:** Deals damage or knockback to enemies on collision.
*   **Gameplay Role:** Aggressive "Brawler" style.
*   **Visual Concept:** Spiked hull with energy-reactive armor plates.
*   **Complexity:** Medium. Handled in `processInteraction`.
*   **Status:** APPROVED.

### 3. Drone Carrier
*   **Classification:** BACKLOG
*   **Description:** Periodically spawns small support drones that collect power-ups automatically.
*   **Gameplay Role:** Automation/Support focus.
*   **Visual Concept:** Large, bulky hull with side-mounted docking bays.
*   **Complexity:** High. Requires AI companion system.
*   **Status:** BACKLOG.

### 4. Solar Glider
*   **Classification:** REJECTED
*   **Rationale:** Concept focused on "not thrusting" to gain fuel, which conflicts with the core "Jump Droid" vertical loop.
*   **Status:** REJECTED.
