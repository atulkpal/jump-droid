# Jump Droid — Architecture

## Overview

Jump Droid is a single-activity Android app using Jetpack Compose. All gameplay rendering is done on a `Canvas` composable via `DrawScope` — there is no game engine, sprite system, or OpenGL. The game loop runs inside a `LaunchedEffect` with `withFrameNanos`, driving physics updates, collision detection, manager updates, and rendering each frame.

Following a series of architectural refinements (Refactor Sprints T1–T4), the system has evolved from a monolithic "Brain" into a delegated "Orchestrator" pattern.

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        MainActivity                                 │
│                  (ComponentActivity + setContent)                    │
└───────────────────────────┬─────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                          GameScreen                                  │
│                 (The Central Orchestrator)                           │
│  ┌─────────────┐  ┌─────────────┐  ┌────────────────────────────┐  │
│  │  Game Loop   │  │   Canvas    │  │       UI Framework         │  │
│  │  LaunchedEffect│  │  Renderer   │  │  (Screens, HUD, Overlays)  │  │
│  │  withFrameNanos│  │  DrawScope  │  └──────────────┬─────────────┘  │
│  └──────┬───────┘  └──────┬──────┘                 │                 │
│         │                 │                        ▼                 │
│         ▼                 ▼              ┌────────────────────────┐  │
│  ┌─────────────────────────────────────┐ │  Extracted UI Files    │  │
│  │         State & Managers            │ │ (Title, Hangar, Pause, │  │
│  │  ┌──────────┐ ┌──────────┐          │ │  HudWidgets.kt, etc.)  │  │
│  │  │ Player   │ │Platforms │          │ └────────────────────────┘  │
│  │  │ (mutable │ │(stateList)│          │                             │
│  │  │  state)  │ │          │          │   ┌───────────────────────┐ │
│  │  └──────────┘ └──────────┘          │   │    CanvasEffects.kt   │ │
│  │  ┌──────────────┐ ┌────────────┐    │   │ (Particles, Flash,    │ │
│  │  │Game State    │ │ Score       │    │   │  SpeedLines, etc.)    │ │
│  │  │(GameState    │ │ High Score  │    │   └───────────────────────┘ │
│  │  │ enum)        │ │ etc.        │    │                             │
│  └──────┬──────────────────────────────┘                             │
│         │                                                            │
│         ▼                                                            │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │                    Specialized Managers                     │    │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐         │    │
│  │  │SurvivalMngr  │ │EncounterDir. │ │PlatformMngr  │         │    │
│  │  │(Damage/Regen)│ │(Spawning)    │ │(Generation)  │         │    │
│  │  └──────────────┘ └──────────────┘ └──────────────┘         │    │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐         │    │
│  │  │NOTIFICATIONM.│ │FLOATTEXTMNGR  │ │MISSIONMNGR   │         │    │
│  │  │(QUEUE/ALERTS)│ │(POPUPS)      │ │(OBJECTIVES)  │         │    │
│  │  └──────────────┘ └──────────────┘ └──────────────┘         │    │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐         │    │
│  │  │PROJECTILEMNGR│ │INPUTBUFFMNGR │ │ TETHERSOLVER │         │    │
│  │  │(RANGED CMBT) │ │(INPUT LAG)   │ │(PHYSICS LINK)│         │    │
│  │  └──────────────┘ └──────────────┘ └──────────────┘         │    │
│  └─────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Game Loop (per-frame execution order)

```
withFrameNanos { currentTime →
    1. Calculate dt (delta time)
    2. Update utility managers:
       ├── NotificationManager.update(dt)
       ├── FloatingTextManager.update(dt)
       ├── ProjectileManager.update(dt)
       ├── InputBufferManager.recordInput(thrust, target)
       ├── DiscoveryManager.update(dt)
       ├── ComboManager.update(dt)
       └── MissionManager.selectNextMission() (Cycle tracks)
    3. Update Director & Environment:
       ├── EncounterDirector.update(dt, score, zone, cameraY, player)
       ├── ThreatManager.update(dt, camera, screen, player)
       └── AmbientManager.update(dt, camera, screen, zone)
    4. Survival System Update:
       └── SurvivalManager.update(dt, player, gameTime, notifications)
    5. Sub-stepped physics (4 substeps):
       ├── Update platform positions (moving platforms)
       ├── Resolve input from InputBufferManager.getEffectiveThrust()
       ├── Apply physical links via activeTether.applyPhysics()
       ├── ProjectileManager.processPlayerCollision(player)
       ├── ActiveThreat.processInteraction(player, sdt, platforms, powerUps, threats)
       ├── Update power-up positions (magnetic pull, hover)
       ├── AABB collision detection (player ↔ platform)
       └── Physics application (gravity, thrust, steering, damping)
    6. Score calculation & zone update
    7. Platform lifecycle (recycling & generation via PlatformManager)
    8. Particle, landing effect, and ceremony lifecycle
   10. Canvas rendering:
       ├── ZoneBackgroundRenderer (parallax + gradient)
       ├── AmbientSystem (ambient objects)
       ├── Threat rendering (Hazards, Enemies, Bosses)
       ├── PlatformRenderer (per platform type)
       ├── CanvasEffects: drawProjectiles, drawTether
       ├── CanvasEffects: drawParticles, drawPowerUps, drawRewards
       ├── RocketRenderer (thruster, body, shield, damage)
       ├── CanvasEffects: drawVisualObstruction (Fog)
       ├── CanvasEffects: drawGround, drawSpeedLines, drawDistortion
       └── CanvasEffects: drawImpactFlash
   11. UI Overlay (Composables):
       ├── Full-screen State (Title, Main Menu, Hangar, Archive, etc.)
       ├── HUD Gauges (Fuel, Heat, Shield, Integrity via HudWidgets.kt)
       ├── HUD Overlays (Missions, Combo Bar, Notifications, Popups)
       └── Contextual Overlays (Pause, Tutorial, Unlock, GameOver)
}
```

---

## Component Map

### Entry Point & Orchestration

| File | Responsibility |
|------|---------------|
| `MainActivity.kt` | Sets up edge-to-edge, hosts `GameScreen` composable. |
| `GameScreen.kt` | **The Orchestrator**: Manages state, the core loop, and coordinates all sub-systems. |

### Core Logic Managers ("The Brains")

| File | Responsibility |
|------|---------------|
| `SurvivalManager.kt` | Centralizes damage distribution (Shield → Hull) and the 3-phase catastrophic destruction sequence. |
| `EncounterDirector.kt` | The "AI Director": decides hazard spawn types, zone-specific weights, and boss arrival thresholds. |
| `PlatformManager.kt` | Owns the mathematical generation of platforms and tracks streak counters (Breakable, Phase, Magnetic). |
| `NotificationManager.kt` | Encapsulates the message queue, priority alerts, and alpha/timer fading logic. |
| `FloatingTextManager.kt` | Manages the lifecycle and upward drift of status popups (e.g., "HULL IMPACT"). |
| `ThreatManager.kt` | Runtime lifecycle — spawn, update, and cleanup of threat instances. |
| `ProjectileManager.kt` | Manages lifecycle, rendering hooks, and collision for ranged attacks (Projectiles). |
| `InputBufferManager.kt` | Manages time-stamped input event queue to enable latency-based gameplay effects. |

### Gameplay Systems

| File | Responsibility |
|------|---------------|
| `ActiveThreat.kt` | **Delegated Intelligence**: Handles its own interaction rules against player and other entities (E2E). |
| `Projectile.kt` | Data model for ranged attacks with support for bolts, missiles, beams, and waves. |
| `Tether.kt` | Physics sub-routine for distance-constrained restorative forces (Physical links). |
| `Models.kt` | `Player` class (mutable state), `PowerUp`, `Particle`, `FloatingText`, enums (`GameState`, `RocketType`, etc.). |
| `Constants.kt` | All tunable constants (gravity, thrust, fuel consumption rates). |
| `MissionManager.kt` | Runtime tracking — activate, progress, complete, and auto-cycle 3 active tracks. |
| `ComboManager.kt` | Combo logic (chain on different platforms), 5 tiers, and survival milestone rewards. |
| `ProgressionManager.kt` | Permanent account data: high score, achievements, artifact records, and ascension ranks. |

### Rendering & Visuals

| File | Responsibility |
|------|---------------|
| `RocketRenderer.kt` | Rocket body, armor plates, battle damage, and destruction sequence visuals. |
| `CanvasEffects.kt` | Extension functions for DrawScope: Projectiles, Tethers, Ground, SpeedLines, Particles, Fog, Distortion. |
| `PlatformRenderer.kt` | Canvas rendering for 10 platform types with unique physics themes. |
| `ZoneBackgroundRenderer.kt` | Per-zone gradient backgrounds and parallax layer orchestration. |
| `AmbientSystem.kt` | Zone-specific ambient objects (birds, satellites, anomalies) with parallax drift. |

---

## Data Flow

### Input → Physics → Delegated Interaction

```
Touch Input (pointerInput)
    │
    ▼
GameScreen (isThrusting = true, thrustTarget = finger)
    │
    ▼
Game Loop (per substep):
    ├── ActiveThreat.processInteraction(player) ──► Apply specific effects (Force, Heat, Damage)
    ├── Physics Substep ──► Apply gravity, steering, and damping
    └── Collision Resolution ──► Player ↔ Platform AABB check
```

### Persistence Flow

```
SharedPreferences (via ProgressionManager)
    ├── highScore (Int)
    ├── discovery_<TYPE> (Boolean)
    ├── unlock_<ROCKET> (Boolean)
    └── achievement_<ID> (Boolean)
```

---

## Key Architectural Decisions

### 1. Delegated "Orchestrator" Pattern
`GameScreen.kt` no longer implements gameplay rules. Instead, it provides the "Stage" (Player state, Collections) and tells the Managers when to act. This reduced the main file from 4,360+ lines to ~2,600.

### 2. Delegated Threat Interaction
Moving threat interactions to `ActiveThreat.kt` ensures the physics engine doesn't need to be modified when adding new hostile entities. Each threat is responsible for its own logic within the proximity of the player and other game entities (E2E).

### 3. Decoupled UI Framework
The UI is strictly partitioned into standalone `@Composable` files. Communication is handled via callbacks (e.g., `onNavigate`, `onLaunch`), preventing the UI from directly mutating gameplay state.

### 4. Sub-Stepped Physics Engine
The physics loop runs 4 substeps per frame. Interaction processing is called **within** these substeps to maintain pixel-perfect collision reliability and accurate force application.

---

## The Journey of Change

Jump Droid began as a monolithic proof-of-concept where a single file (`GameScreen.kt`) handled everything. As the complexity grew—adding boss phases, survival economy, and artifact collections—the file became unmaintainable.

1.  **Sprints T1–T2 (UI Extraction)**: Removed 1,200+ lines of UI code. Partitioned the app into specialized Screens and HUD widgets. Created `CanvasEffects.kt` to clean up rendering flourishes.
2.  **Sprint T3 (Logic Extraction)**: Modularized utility state. Extracted Notification and Floating Text handling. Moved high score and achievement audits into `ProgressionManager`.
3.  **Sprint T4 (The Architectural Shift)**: Transitioned from an inline logic model to a delegated manager model. Extracted the "Brains" of the game into the `SurvivalManager` and `EncounterDirector`.

The resulting architecture is now robust, modular, and ready for high-velocity feature development.

---

## Extension Points

| Point | Mechanism | Example |
|-------|-----------|---------|
| New threats | Register in `ThreatRegistry.populateCatalog()` | `HAZ_CRYO_MIST` |
| New projectiles | Add to `ProjectileType` enum + render case | `HOMING_MISSILE` |
| New missions | Register in `MissionRegistry` | New discovery/boss missions |
| New discoveries | Add to `DiscoveryType` enum | New lore or artifact entries |
| New platform types | Add to `PlatformType` enum + render method in `PlatformRenderer` | New `LAVA` platform |
| New rocket types | Add to `RocketType` enum | New stealth rocket |
| New achievements | Add to `AchievementsList` in `Achievements.kt` | "Reach 25000m" |
| New ambient objects | Add to `AmbientType` enum + render case | New `COMET` ambient |
| New boss behaviors | Add phase logic in `ActiveThreat.kt` | New boss attack pattern |

---

## Dependencies

- `androidx.compose.ui` — Canvas, layout, input, graphics.
- `androidx.compose.material3` — UI components and typography.
- `androidx.lifecycle.runtime.ktx` — Lifecycle-aware coroutines.

No external game libraries or engines are used.
