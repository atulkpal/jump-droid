# Jump Droid: Engine Expansion Architecture Report

This report outlines the proposed architectural designs for the five missing core systems required to implement the content defined in the **THREAT_LIBRARY**.

---

## 1. Projectile Engine
**Objective:** Provide a global system for spawning, tracking, and colliding ranged attacks.

### Architecture
- **Data Model (`Projectile.kt`):**
    - `x, y`: Current position.
    - `vx, vy`: Velocity.
    - `type`: `BOLT`, `MISSILE`, `BEAM`, `WAVE`.
    - `owner`: `THREAT` or `PLAYER` (future-proofing).
    - `damage`: Impact value.
    - `life`: Remaining time before despawn.
- **Manager (`ProjectileManager.kt`):**
    - `projectiles`: A `SnapshotStateList` of active instances.
    - `spawn()`: Method called by Bosses/Enemies to create instances.
    - `update(dt)`: Standard movement and aging.
    - `processCollisions(player, platforms)`: Simple circle-to-AABB collision detection.
- **Rendering (`CanvasEffects.kt`):**
    - `DrawScope.drawProjectiles()`: Extension function to render projectiles based on type (glows, trails).

---

## 2. Visual Obstruction & Fog System
**Objective:** Dynamically mask or obscure portions of the screen to create low-visibility challenges (e.g., Nebula Serpent).

### Architecture
- **Manager (`WeatherManager.kt`):**
    - Manages global or localized visibility states (`visibilityAlpha`).
    - Tracks "Fog Clouds" (localized obstructions).
- **Rendering Strategy:**
    - **Layered approach:** Draw the game world -> Draw the Obstruction layer -> Draw the HUD.
    - **Methods:**
        - **Masking:** Use `DrawScope.drawRect` with high-alpha dark colors.
        - **Blur/Grain:** Use `graphicsLayer` with a Grain shader or Noise texture overlay.
        - **Path Clipping:** For "Serpents," use `DrawScope.clipPath` to restrict visibility around a moving point.

---

## 3. Entity-to-Entity (E2E) Interaction
**Objective:** Allow threats to perceive and interact with other objects (Power-Ups, Platforms, other Threats) rather than just the player.

### Architecture
- **Broadening the Interaction Loop:**
    - Modify `ActiveThreat.update()` and `processInteraction()` to accept a `WorldState` object or a list of interactive entities (`List<Any>`).
- **Entity Awareness:**
    - Implement a `scanFor(type)` helper in `ActiveThreat`.
    - Example: `Void Harvester` scans for nearby `PowerUp` objects, then changes its `vx, vy` to intercept them.
- **Collision Resolution:**
    - Threats that "eat" or "destroy" other entities call `activeEntities.remove(target)`.

---

## 4. Tether & Joint Physics
**Objective:** Physically link the player to environmental objects or threats (e.g., Static Tether).

### Architecture
- **Data Model (`Tether.kt`):**
    - `origin`: Anchor point (X, Y or a `Threat` instance).
    - `target`: The `Player`.
    - `length`: Maximum slack before force is applied.
    - `stiffness`: Multiplier for the pull force.
- **Physics Logic (`TetherManager.kt`):**
    - If `distance(origin, target) > length`:
        - Calculate `pullDirection`.
        - Apply counter-force: `player.velocityX -= pullX * stiffness * dt`.
- **Rendering:**
    - `DrawScope.drawLine` between origin and player with electrical or rope visual effects.

---

## 5. Input Buffer System
**Objective:** Simulate input lag or "Chrono-Lag" without breaking the Compose touch handling loop.

### Architecture
- **Input Wrapper (`InputManager.kt`):**
    - Instead of `pointerInput` directly setting `isThrusting`, it posts a `ThrustEvent(timestamp, targetX, targetY)` to a queue.
- **Delayed Processing:**
    - The `GameScreen` loop reads the queue.
    - `val effectiveEvent = queue.find { it.timestamp <= currentTime - currentLag }`.
- **State Management:**
    - `currentLag` is a dynamic value (0.0s to 0.5s) modified by hazards or zones.
