# Jump Droid: Engine Extensions

This document details the core engine systems added during the Engine Expansion Sprint (Post-Refactor T4). These systems provide the infrastructure for advanced threat mechanics, environmental effects, and time-manipulation gameplay.

---

## 1. Projectile Engine
**Purpose:** Enables ranged combat and telegraphed boss attacks.

### Components
- **`Projectile.kt`**: Data model tracking position, velocity, type, owner, and lifecycle.
- **`ProjectileManager.kt`**: Central manager for spawning, updating, and collision detection against the player.
- **`ProjectileType`**: Supports `BOLT` (standard), `MISSILE` (tracked/flaming), `BEAM` (energy segments), and `WAVE` (area shockwaves).

### Integration
- **Collision**: Uses circle-to-AABB logic in the physics loop.
- **Rendering**: Extends `DrawScope` in `CanvasEffects.kt` with unique visuals for each projectile type including glow pulses and flame trails.

---

## 2. Visual Obstruction System
**Purpose:** Creates low-visibility challenges such as fog, smoke, or darkness.

### Components
- **`globalFogAlpha`**: A dynamic state in `GameScreen` controlling the intensity of the obstruction.
- **`drawVisualObstruction`**: Extension in `CanvasEffects.kt` using a radial gradient mask.

### Design
- **Clarity Radius**: The system ensures a "safe zone" around the player's rocket remains visible, preventing total blindness while obscuring distant platforms and threats.

---

## 3. Entity-to-Entity (E2E) Interaction
**Purpose:** Allows game entities (Threats) to perceive and interact with objects other than the player.

### Components
- **Expanded Loop**: `ActiveThreat.update` and `processInteraction` now receive the full list of `activeThreats` and `powerUps`.
- **Capability**: Threats can now hunt power-ups (denial), summon other threats, or coordinate movement based on the proximity of allies.

---

## 4. Tether & Joint Physics
**Purpose:** Physically links the player to environmental anchors.

### Components
- **`Tether.kt`**: Handles distance-based tension math.
- **`player.activeTether`**: State holder for current link.

### Integration
- **Physics**: Applied as a restorative force in the physics substep loop when the distance exceeds `maxLength`.
- **Visuals**: `drawTether` in `CanvasEffects.kt` renders an animated electrical zig-zag bolt between the anchor and the player.

---

## 5. Input Buffer System
**Purpose:** Enables time-distortion effects like lag or "Chrono-Rift" mechanics.

### Components
- **`InputBufferManager.kt`**: Manages a time-stamped queue of `ThrustEvent` objects.
- **`player.inputLatency`**: Controls the delay (in seconds) between user touch and engine response.

### Integration
- **Effective Thrust**: The game loop queries the manager for the state of input at `currentTime - inputLatency`, allowing for perfectly synchronized delayed responses without disrupting the UI event loop.

---

## Configuration & Safety
All expansion systems are guarded by feature toggles in `DevConfig.kt`:
- `ENABLE_PROJECTILE_ENGINE`
- `ENABLE_VISUAL_OBSTRUCTION`
- `ENABLE_ENTITY_INTERACTION`
- `ENABLE_TETHER_PHYSICS`
- `ENABLE_INPUT_BUFFER`

Defaults are **Enabled**.
