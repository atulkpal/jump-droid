# Jump Droid: Platform Library

This document is the permanent content vault for all Platform types in Jump Droid, covering current implementations and future design concepts.

---

## A. Existing Platforms

These platforms are fully implemented with unique physics and visual rendering.

### 1. Standard Platform
*   **Name:** Standard Platform
*   **Description:** Safe landing zone. Recover and plan your next move.
*   **Visual Appearance:** Semi-transparent white block with horizontal gradient fill, side accents, corner cyber-brackets, and a faint holographic grid overlay (cross-hatch pattern). Zone-reactive edge glow tint (orange in Foundry, purple in Chrono-Rift).
*   **Behavior:** Static, no special effects.
*   **Spawn Rules:** Default type, appears in all zones.
*   **Zone Usage:** Global.
*   **Current Status:** Complete.

### 2. Moving Platform
*   **Name:** Moving Platform
*   **Description:** Moves horizontally between screen bounds.
*   **Visual Appearance:** Cyan block with animated directional chevrons and trailing glow particles that drift off the trailing edge. Speed-reactive brightness (faster = brighter arrows and particles).
*   **Behavior:** Oscillates horizontally. Player inherits platform velocity while grounded.
*   **Spawn Rules:** 20% chance replacement for Normal platforms after early altitude.
*   **Zone Usage:** Cloud Layer and above.
*   **Current Status:** Complete.

### 3. Ice Platform
*   **Name:** Ice Platform
*   **Description:** Low friction surface that is very slippery.
*   **Visual Appearance:** Shimmering cyan block with hexagonal frost cross-hatch pattern, polarized light sweep (moving highlight band), icicle spikes along the bottom edge, and a roaming sparkle point.
*   **Behavior:** Reduces horizontal damping significantly, making steering difficult.
*   **Spawn Rules:** Zone-specific.
*   **Zone Usage:** Cloud Layer and above.
*   **Current Status:** Complete.

### 4. Boost Platform
*   **Name:** Boost Platform
*   **Description:** Launches the player upward upon landing.
*   **Visual Appearance:** Gold pulsing block with a dual-layer thrust-cone gradient above (white-hot gold fading to transparent), animated upward arrow, rising spark particles, and a compression ring indicator.
*   **Behavior:** Applies a vertical velocity burst on contact.
*   **Spawn Rules:** Rare.
*   **Zone Usage:** Global (except Earth).
*   **Current Status:** Complete.

### 5. Breakable Platform
*   **Name:** Breakable Platform
*   **Description:** Collapses shortly after landing.
*   **Visual Appearance:** Red block with a glowing fracture network — branching white cracks that brighten and widen as damage progresses. Warning hologram flickering at edges. Debris particles shed from active cracks.
*   **Behavior:** Triggers a 2.4s timer on landing, after which it explodes and is removed.
*   **Spawn Rules:** Common in Atmosphere and Orbit.
*   **Zone Usage:** Upper Atmosphere and above.
*   **Current Status:** Complete.

### 6. Phase Platform
*   **Name:** Phase Platform
*   **Description:** Periodically appears and disappears based on a timer.
*   **Visual Appearance:** Purple block with digital noise (random pixel bars), glitch-displacement bands during fade transitions, a phase-shift ripple ring when appearing/disappearing, and a moving scan line. Corner brackets present when visible.
*   **Behavior:** Collision is only active when alpha > 0.9.
*   **Spawn Rules:** Timing-based challenge.
*   **Zone Usage:** Upper Atmosphere and above.
*   **Current Status:** Complete.

### 7. Fuel Platform
*   **Name:** Fuel Platform
*   **Description:** Refuels the rocket on landing.
*   **Visual Appearance:** Green block with pulsing inner border, corner decals, animated resource-flow particles flowing inward, and "FUEL" text label with white glow shadow.
*   **Behavior:** Restores 50 fuel units on landing.
*   **Spawn Rules:** Utility variant.
*   **Zone Usage:** Upper Atmosphere and above.
*   **Current Status:** Complete.

### 8. Cooling Platform
*   **Name:** Cooling Platform
*   **Description:** Removes engine heat on landing.
*   **Visual Appearance:** Cyan block with pulsing inner border, corner decals, animated resource-flow particles, and "COOL" text label with white glow shadow.
*   **Behavior:** Removes 30 heat units on landing.
*   **Spawn Rules:** Utility variant.
*   **Zone Usage:** Upper Atmosphere and above.
*   **Current Status:** Complete.

### 9. Shield Platform (formerly Stability Platform)
*   **Name:** Shield Platform
*   **Internal ID:** `PlatformType.STABILITY` (legacy name — not renamed to avoid scope creep)
*   **Description:** Fully recharges energy shields on contact.
*   **Visual Appearance:** Cyan block with pulsing inner border, corner decals, animated resource-flow particles, and "SHLD" text label with cyan glow shadow.
*   **Behavior:** Instantly restores `player.shield = player.maxShield` and resets `shieldRegenPauseTimer` to `0f` on landing.
*   **Spawn Rules:** Utility variant (same spawn slot as FUEL/COOLING).
*   **Zone Usage:** Upper Atmosphere and above.
*   **Current Status:** Complete.

### 10. Magnetic Platform
*   **Name:** Magnetic Platform
*   **Description:** Generates a gravitational field that influences movement.
*   **Visual Appearance:** Purple block with a white core, curved magnetic field lines (bezier arcs from center), expanding concentric ring pulses, and orbiting ferrous particle attraction dots.
*   **Behavior:** Applies proximity-based pull/push forces to the player and power-ups.
*   **Spawn Rules:** High-altitude hazard/aid.
*   **Zone Usage:** Deep Space and Void.
*   **Current Status:** Complete.

### 11. Flux Platform (Teleportation)
*   **Name:** Flux Platform
*   **Description:** Teleports the player to a linked platform.
*   **Visual Appearance:** Purple horizontal-gradient body with glowing cyan edge rails, a swirling vortex core (animated spiral path), spatial distortion shimmer lines, orbiting vortex particles, and a teleport-charge inner glow that intensifies before teleport.
*   **Behavior:** Teleports player to linked Flux platform on contact.
*   **Spawn Rules:** EPIC 10 zone-exclusive.
*   **Zone Usage:** The Foundry, Chrono-Rift and above.
*   **Current Status:** Complete.

### 12. Graviton Platform (Gravity Well)
*   **Name:** Graviton Platform
*   **Description:** Creates a gravity well that pulls/pushes nearby objects.
*   **Visual Appearance:** Black body with reddened horizontal-gradient edges, an event horizon glow ring, singularity lens flare (bright white core with radial gradient), expanding gravity well rings, bent spacetime grid lines, stretched tidal-force particle indicators, and accretion disk shimmer dots.
*   **Behavior:** Applies gravity forces to player and power-ups within radius.
*   **Spawn Rules:** EPIC 10 zone-exclusive.
*   **Zone Usage:** Deep Space, The Foundry, Chrono-Rift and above.
*   **Current Status:** Complete.

### 13. Conveyor Platform
*   **Name:** Conveyor Platform
*   **Description:** A moving belt that pushes the player horizontally while standing on it.
*   **Visual Appearance:** Dark metallic base with horizontal gradient, industrial red/yellow warning stripes on edges, animated vertical roller segments, diagonal moving belt lines, gear-tooth glow at ends, and speed-matched orange accent glow.
*   **Behavior:** Applies continuous horizontal force to the player while grounded.
*   **Spawn Rules:** High-altitude zones.
*   **Zone Usage:** The Foundry and above.
*   **Current Status:** Complete.

### 14. Disguised Platform (Mimic)
*   **Name:** Mimic Platform
*   **Description:** Looks like a normal platform but reveals itself as a trap.
*   **Visual Appearance:** Identical to Standard platform when idle. Every 5 seconds, glitches with: reality-tear fracture paths (jagged black/red splits), static interference bars, red flash overlay, and a wrong-perspective offset shadow.
*   **Behavior:** Shatters on landing, dealing 15 integrity damage.
*   **Spawn Rules:** Random replacement for Normal platforms.
*   **Zone Usage:** All zones.
*   **Current Status:** Complete.

---

## B. Future Platforms

Proposed concepts for future expansion.

### 1. Slingshot Platform
*   **Classification:** BACKLOG
*   **Description:** Catch the player and allows them to charge a jump in a specific direction.
*   **Gameplay Purpose:** Skilled navigation and puzzle solving.
*   **Visual Concept:** Two energy pylons with a tension field between them.
*   **Complexity:** High. Requires suspending normal physics logic.
*   **Status:** BACKLOG.

### 2. Adhesive Platform
*   **Classification:** REJECTED
*   **Rationale:** Slowing down or stopping the player's momentum too abruptly feels frustrating in a high-speed survival climber.
*   **Status:** REJECTED.
