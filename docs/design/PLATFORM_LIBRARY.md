# Jump Droid: Platform Library

This document is the permanent content vault for all Platform types in Jump Droid, covering current implementations and future design concepts.

---

## A. Existing Platforms

These platforms are fully implemented with unique physics and visual rendering.

### 1. Standard Platform
*   **Name:** Standard Platform
*   **Description:** Safe landing zone. Recover and plan your next move.
*   **Visual Appearance:** Solid white block with a semi-transparent core and side accents.
*   **Behavior:** Static, no special effects.
*   **Spawn Rules:** Default type, appears in all zones.
*   **Zone Usage:** Global.
*   **Current Status:** Complete.

### 2. Moving Platform
*   **Name:** Moving Platform
*   **Description:** Moves horizontally between screen bounds.
*   **Visual Appearance:** Cyan block with animated white chevrons indicating direction.
*   **Behavior:** Oscillates horizontally. Player inherits platform velocity while grounded.
*   **Spawn Rules:** 20% chance replacement for Normal platforms after early altitude.
*   **Zone Usage:** Cloud Layer and above.
*   **Current Status:** Complete.

### 3. Ice Platform
*   **Name:** Ice Platform
*   **Description:** Low friction surface that is very slippery.
*   **Visual Appearance:** Shimmering cyan block with crystalline internal details and top sheen.
*   **Behavior:** Reduces horizontal damping significantly, making steering difficult.
*   **Spawn Rules:** Zone-specific.
*   **Zone Usage:** Cloud Layer and above.
*   **Current Status:** Complete.

### 4. Boost Platform
*   **Name:** Boost Platform
*   **Description:** Launches the player upward upon landing.
*   **Visual Appearance:** Gold pulsing block with an animated upward arrow and top glow.
*   **Behavior:** Applies a vertical velocity burst on contact.
*   **Spawn Rules:** Rare.
*   **Zone Usage:** Global (except Earth).
*   **Current Status:** Complete.

### 5. Breakable Platform
*   **Name:** Breakable Platform
*   **Description:** Collapses shortly after landing.
*   **Visual Appearance:** Red block with warning stripes and increasing cracks upon landing.
*   **Behavior:** Triggers a 2.4s timer on landing, after which it explodes and is removed.
*   **Spawn Rules:** Common in Atmosphere and Orbit.
*   **Zone Usage:** Upper Atmosphere and above.
*   **Current Status:** Complete.

### 6. Phase Platform
*   **Name:** Phase Platform
*   **Description:** Periodically appears and disappears based on a timer.
*   **Visual Appearance:** Purple block with digital "noise" and scanlines. Fades in and out.
*   **Behavior:** Collision is only active when alpha > 0.9.
*   **Spawn Rules:** Timing-based challenge.
*   **Zone Usage:** Upper Atmosphere and above.
*   **Current Status:** Complete.

### 7. Fuel Platform
*   **Name:** Fuel Platform
*   **Description:** Refuels the rocket on landing.
*   **Visual Appearance:** Green block with "FUEL" text and tech decals.
*   **Behavior:** Restores 50 fuel units on landing.
*   **Spawn Rules:** Utility variant.
*   **Zone Usage:** Upper Atmosphere and above.
*   **Current Status:** Complete.

### 8. Cooling Platform
*   **Name:** Cooling Platform
*   **Description:** Removes engine heat on landing.
*   **Visual Appearance:** Cyan block with "COOL" text and outer glow frame.
*   **Behavior:** Removes 30 heat units on landing.
*   **Spawn Rules:** Utility variant.
*   **Zone Usage:** Upper Atmosphere and above.
*   **Current Status:** Complete.

### 9. Stability Platform
*   **Name:** Stability Platform
*   **Description:** Stabilizes flight control temporarily.
*   **Visual Appearance:** White block with "STAB" text.
*   **Behavior:** Grants a 10-second stability buff (improved steering).
*   **Spawn Rules:** Utility variant.
*   **Zone Usage:** Upper Atmosphere and above.
*   **Current Status:** Complete.

### 10. Magnetic Platform
*   **Name:** Magnetic Platform
*   **Description:** Generates a gravitational field that influences movement.
*   **Visual Appearance:** Purple block with a white core and pulsing concentric rings representing the field.
*   **Behavior:** Applies proximity-based pull/push forces to the player and power-ups.
*   **Spawn Rules:** High-altitude hazard/aid.
*   **Zone Usage:** Deep Space and Void.
*   **Current Status:** Complete.

---

## B. Future Platforms

Proposed concepts for future expansion.

### 1. Conveyor Platform
*   **Classification:** APPROVED
*   **Description:** A moving belt that pushes the player horizontally while they are standing on it.
*   **Gameplay Purpose:** Forces precise positioning or adds extra momentum to jumps.
*   **Visual Concept:** Black metallic surface with animated tread patterns.
*   **Complexity:** Medium. Requires modifying `player.velocityX` based on standing state.
*   **Status:** APPROVED.

### 2. Disguised Platform (Mimic)
*   **Classification:** APPROVED (Moved from THREAT_LIBRARY)
*   **Description:** Appears as a normal platform but reveals itself as a hazard or trap.
*   **Gameplay Purpose:** Punishes blind ascending; requires observation.
*   **Visual Concept:** Identical to Standard/Fuel, but with a 1-pixel glitch every 5s.
*   **Complexity:** Low. Class variant.
*   **Status:** APPROVED.

### 3. Slingshot Platform
*   **Classification:** BACKLOG
*   **Description:** Catch the player and allows them to charge a jump in a specific direction.
*   **Gameplay Purpose:** Skilled navigation and puzzle solving.
*   **Visual Concept:** Two energy pylons with a tension field between them.
*   **Complexity:** High. Requires suspending normal physics logic.
*   **Status:** BACKLOG.

### 4. Adhesive Platform
*   **Classification:** REJECTED
*   **Rationale:** Slowing down or stopping the player's momentum too abruptly feels frustrating in a high-speed survival climber.
*   **Status:** REJECTED.
