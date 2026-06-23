# Jump Droid: Power-Up Library

This document is the permanent content vault for all Power-Ups in Jump Droid, encompassing currently implemented items and proposed future concepts.

---

## A. Existing Power-Ups

These items are fully functional and integrated into the current game loop.

### 1. Fuel Tank
*   **Name:** Fuel Tank
*   **Description:** Increases maximum fuel capacity and refills current fuel.
*   **Visual Appearance:** Gold pulsing capsule with a fuel icon.
*   **Spawn Method:** Randomly during flight or as a mission/combo reward.
*   **Spawn Frequency:** Rare (0.05% weight in random pool).
*   **Gameplay Effect:** Increases `maxFuel` by 25 units (up to 300 limit).
*   **Duration:** Permanent (Per run).
*   **Associated Systems:** `Player` state, `Constants.MAX_FUEL_CAPACITY_LIMIT`.
*   **Current Status:** Complete.

### 2. Turbo Booster
*   **Name:** Turbo Booster
*   **Description:** Temporarily increases thrust power.
*   **Visual Appearance:** Cyan pulsing capsule with a lightning bolt icon.
*   **Spawn Method:** Randomly during flight or as a mission/combo reward.
*   **Spawn Frequency:** Common.
*   **Gameplay Effect:** Multiplies thrust by 1.2x.
*   **Duration:** 8 seconds.
*   **Associated Systems:** `Player.turboTimer`, `Thrust` calculation.
*   **Current Status:** Complete.

### 3. Efficiency Module
*   **Name:** Efficiency Module
*   **Description:** Reduces fuel consumption during thrust.
*   **Visual Appearance:** Green pulsing capsule with a leaf icon.
*   **Spawn Method:** Randomly during flight or as a mission/combo reward.
*   **Spawn Frequency:** Common.
*   **Gameplay Effect:** Multiplies fuel consumption by 0.8x.
*   **Duration:** 8 seconds.
*   **Associated Systems:** `Player.efficiencyTimer`, `Fuel` consumption logic.
*   **Current Status:** Complete.

### 4. Heat Sink
*   **Name:** Heat Sink
*   **Description:** Instantly removes a significant portion of engine heat.
*   **Visual Appearance:** White pulsing capsule with a snowflake icon.
*   **Spawn Method:** Randomly during flight or as a mission/combo reward.
*   **Spawn Frequency:** Common.
*   **Gameplay Effect:** Reduces `heat` by 50 units and clears `isOverheated` state.
*   **Duration:** Instant.
*   **Associated Systems:** `Player.heat`, `Player.isOverheated`.
*   **Current Status:** Complete.

### 5. Shield Capsule
*   **Name:** Shield Capsule
*   **Description:** Recharges a portion of the energy shield.
*   **Visual Appearance:** Cyan pulsing capsule.
*   **Spawn Method:** Randomly during flight or as a mission/combo reward.
*   **Spawn Frequency:** Common.
*   **Gameplay Effect:** Restores 25 shield units.
*   **Duration:** Instant.
*   **Associated Systems:** `Player.shield`.
*   **Current Status:** Complete.

### 6. Hull Repair
*   **Name:** Hull Repair
*   **Description:** Repairs structural integrity damage.
*   **Visual Appearance:** Green pulsing capsule.
*   **Spawn Method:** Randomly during flight or as a mission/combo reward.
*   **Spawn Frequency:** Uncommon.
*   **Gameplay Effect:** Restores 20 integrity units.
*   **Duration:** Instant.
*   **Associated Systems:** `Player.integrity`.
*   **Current Status:** Complete.

### 7. Altitude Booster
*   **Name:** Altitude Booster
*   **Description:** Provides a massive vertical thrust.
*   **Visual Appearance:** White pulsing capsule.
*   **Spawn Method:** Randomly during flight or as a mission/combo reward.
*   **Spawn Frequency:** Rare.
*   **Gameplay Effect:** Sets `velocityY` to -2500f.
*   **Duration:** Instant.
*   **Associated Systems:** `Player.velocityY`.
*   **Current Status:** Complete.

---

## B. Future Power-Ups

These concepts are proposed for future development cycles to expand gameplay depth.

### 1. Kinetic Battery
*   **Classification:** APPROVED
*   **Description:** Converts landing impact into a shield recharge or small thrust burst.
*   **Gameplay Purpose:** Rewards precise platforming with resource recovery.
*   **Visual Concept:** Yellow sparking battery that floats horizontally.
*   **Complexity:** Medium. Requires hook in `handleLanding`.
*   **Status:** APPROVED.

### 2. Magnetic Siphon
*   **Classification:** APPROVED
*   **Description:** Automatically pulls nearby power-ups toward the rocket.
*   **Gameplay Purpose:** Quality of life improvement, especially during intense boss fights.
*   **Visual Concept:** Purple pulsing magnet.
*   **Complexity:** Medium. Requires `PowerUp` proximity logic.
*   **Status:** APPROVED.

### 3. Chrono-Anchor
*   **Classification:** BACKLOG
*   **Description:** Allows the player to "save" their state and return to it if they take damage or fall.
*   **Gameplay Purpose:** High-skill survival tool for Void/Deep Space.
*   **Visual Concept:** Clock-like object with glowing blue gears.
*   **Complexity:** High. Requires state snapshot/restore system.
*   **Status:** BACKLOG.

### 4. Overdrive Module
*   **Classification:** APPROVED
*   **Description:** Massively increases thrust but causes continuous integrity damage.
*   **Gameplay Purpose:** High-risk, high-reward speed-running or escape tool.
*   **Visual Concept:** Pulsing red/orange core with smoke particles.
*   **Complexity:** Low. Modified thrust/damage per second.
*   **Status:** APPROVED.

### 5. Infinite Fuel (Temporary)
*   **Classification:** REJECTED
*   **Rationale:** Undermines the core resource management loop too significantly compared to Efficiency Module.
*   **Status:** REJECTED.
