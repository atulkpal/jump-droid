# Jump Droid — Architecture

## Overview

Jump Droid is a single-activity Android app using Jetpack Compose. All gameplay rendering is done on a `Canvas` composable via `DrawScope` — there is no game engine, sprite system, or OpenGL. The game loop runs inside a `LaunchedEffect` with `withFrameNanos`, driving physics updates, collision detection, manager updates, and rendering each frame.

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
│  ┌─────────────┐  ┌─────────────┐  ┌────────────────────────────┐  │
│  │  Game Loop   │  │   Canvas    │  │       HUD Overlays         │  │
│  │  LaunchedEffect│  │  Renderer   │  │  (meters, text, buttons)  │  │
│  │  withFrameNanos│  │  DrawScope  │  └────────────────────────────┘  │
│  └──────┬───────┘  └──────┬──────┘                                   │
│         │                 │                                           │
│         ▼                 ▼                                           │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │                    State & Managers                           │    │
│  │  ┌──────────┐ ┌──────────┐ ┌───────────┐ ┌──────────────┐  │    │
│  │  │ Player   │ │Platforms │ │ PowerUps  │ │ BossesSpawned│  │    │
│  │  │ (mutable │ │(stateList)│ │ (stateList)│ │ (stateSet)   │  │    │
│  │  │  state)  │ │          │ │           │ │              │  │    │
│  │  └──────────┘ └──────────┘ └───────────┘ └──────────────┘  │    │
│  │  ┌──────────────┐ ┌────────────┐ ┌──────────────────┐     │    │
│  │  │Game State    │ │ Score       │ │ Camera / Screen  │     │    │
│  │  │(GameState    │ │ High Score  │ │ Dimensions       │     │    │
│  │  │ enum)        │ │ etc.        │ │                  │     │    │
│  │  └──────────────┘ └────────────┘ └──────────────────┘     │    │
│  └─────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Game Loop (per-frame execution order)

```
withFrameNanos { currentTime →
    1. Calculate dt (delta time)
    2. Update managers:
       ├── DiscoveryManager.update(dt)
       ├── ThreatManager.update(dt, camera, screen, player)
       ├── AmbientManager.update(dt, camera, screen, zone)
       └── ComboManager.update(dt)
    3. Process combo rewards (flying rewards, survival drops)
    4. Update timers (screenshake, flash, invulnerability, timers)
    5. Sub-stepped physics (4 substeps):
       ├── Update platform positions (moving platforms)
       ├── Apply physics (gravity, thrust, friction)
       ├── Update power-up positions
       ├── AABB collision detection (player ↔ platform)
       ├── Threat collision detection
       └── Platform recycling / generation
    6. Score calculation & zone update
    7. Boss spawn checks (milestone thresholds)
    8. Threat spawn checks (every 3s timer)
    9. Particle, floating text, landing effect lifecycle
   10. Mission progress updates
   11. Canvas rendering:
       ├── ZoneBackgroundRenderer (parallax + gradient)
       ├── AmbientSystem (ambient objects)
       ├── Threat rendering (hazards, enemies, bosses)
       ├── PlatformRenderer (per platform type)
       ├── PowerUp rendering
       ├── RocketRenderer (thruster, body, shield, damage)
       ├── Particle rendering
       ├── Floating text + landing effects
       └── HUD overlay (meters, combo, altitude, missions)
}
```

---

## Component Map

### Entry Point

| File | Responsibility |
|------|---------------|
| `MainActivity.kt` | Sets up edge-to-edge, hosts `GameScreen` composable |

### Core Game State

| File | Responsibility |
|------|---------------|
| `GameScreen.kt` | Monolithic composable: game loop, physics, collision, input, HUD, all state variables |
| `Models.kt` | `Player` class (mutable state), `PowerUp`, `Particle`, `FloatingText`, `LandingEffect`, enums (`GameState`, `PowerUpType`, `RocketType`, `DiscoveryType`) |
| `Constants.kt` | All tunable constants (gravity, thrust, fuel, heat, zone thresholds) |
| `AltitudeZone.kt` | 6 zone definitions with altitude thresholds |

### Platform System

| File | Responsibility |
|------|---------------|
| `Platform.kt` | `PlatformType` enum (10 types), `Platform` class with runtime state (breaking, jamming, landing) |
| `PlatformRenderer.kt` | Canvas rendering per platform type — normal, moving, boost, ice, breakable, phase, fuel, cooling, stability, magnetic |

### Threat System

| File | Responsibility |
|------|---------------|
| `ThreatDefinition.kt` | Data template for a threat (id, name, type, tier, stats, spawn rules) |
| `ThreatRegistry.kt` | Singleton catalog of 31 threat definitions, query by eligibility |
| `ThreatManager.kt` | Runtime lifecycle — spawn, update, cleanup active threat instances |
| `ActiveThreat.kt` | Runtime instance with AI behaviors per threat type — hazards (lightning, solar flare, EMP), enemies (drone, swarm, sentry), bosses (Gatekeeper, Star-Eater, etc.) |
| `ThreatSpawnRules.kt` | Spawn conditions (altitude range, allowed zones, chance) |
| `ThreatType.kt` | Enum: HAZARD, ENEMY, MINI_BOSS, BOSS |
| `ThreatTier.kt` | Enum: TIER_1 through TIER_5 |
| `ThreatState.kt` | Enum: SPAWNING, ACTIVE, DORMANT, DESTROYED |
| `Boss.kt` | Legacy boss model (superseded by `ActiveThreat`) |

### Mission System

| File | Responsibility |
|------|---------------|
| `Mission.kt` | Mission data model with progress, completion, ceremony stages |
| `MissionType.kt` | Enum: EXPLORATION, PLATFORMING, SURVIVAL, DISCOVERY, BOSS, COMBO |
| `MissionReward.kt` | Sealed class: Artifact, PowerUp, Unlock, Achievement |
| `MissionRegistry.kt` | Singleton catalog of 15 mission templates |
| `MissionManager.kt` | Runtime tracking — activate, progress, complete, auto-cycle 3 tracks |

### Discovery & Progression

| File | Responsibility |
|------|---------------|
| `DiscoveryManager.kt` | Codex discovery tracking, persistence via SharedPreferences, event queue |
| `ProgressionManager.kt` | Permanent progression — artifact records (date, altitude, count), ascension rank, stat upgrades |

### Rendering

| File | Responsibility |
|------|---------------|
| `RocketRenderer.kt` | Rocket body, thruster flame (side + main), tilt/banking, squash/stretch, destruction sequence, auras (turbo/efficiency), shield segmented armor, hull battle scars |
| `ZoneBackgroundRenderer.kt` | Per-zone gradient backgrounds, parallax layer rendering, zone transition interpolation, title screen starfield |
| `ParallaxSystem.kt` | Generic parallax framework — `RepeatingParallaxLayer`, `SilhouetteParallaxLayer`, `SingleObjectParallaxLayer`, `ParallaxManager` |
| `AmbientSystem.kt` | Zone-specific ambient objects — birds, aircraft, clouds, satellites, asteroids, anomalies — with spawn/render/cleanup |

### Combo System

| File | Responsibility |
|------|---------------|
| `ComboManager.kt` | Combo logic (chain on different platforms, break on repeat), 5 tiers, reward calculation, survival milestone rewards, `FlyingReward` animation |

### Other

| File | Responsibility |
|------|---------------|
| `DevConfig.kt` | Toggle for developer cheat features |
| `ui/theme/Color.kt` | Sci-Fi color palette (6 primary colors + surface/border) |
| `ui/theme/Theme.kt` | Material theme wrappers |
| `ui/theme/Type.kt` | Typography definitions |
| `ui/theme/TypographyConfig.kt` | Font configuration |

---

## Data Flow

### Input → Physics → Collision

```
Touch Input (pointerInput)
    │
    ▼
isThrusting = true/false
thrustTarget = finger position
    │
    ▼
Game Loop (per substep):
    ├── Apply gravity (player.vy += BASE_GRAVITY * dt)
    ├── Apply thrust (player.vy -= BASE_THRUST_POWER * dt)  [if thrusting & has fuel]
    ├── Apply fuel consumption / heat generation
    ├── Apply horizontal steering (toward thrustTarget.x)
    ├── Apply air friction / damping
    ├── AABB collision against all platforms
    └── AABB collision against all threats
```

### Manager Updates

```
Per Frame:
    ├── ThreatManager.update()     → move threats, run AI, cleanup dead
    ├── ComboManager.update()      → countdown combo timer, break if expired
    ├── DiscoveryManager.update()  → countdown event display timer
    └── AmbientManager.update()    → spawn/despawn ambient objects

Every 3 seconds:
    ├── Check eligible threats (by altitude + zone)
    ├── Roll for hazard spawns
    ├── Roll for enemy spawns
    └── Roll for boss encounter spawns

Score Milestone Checks:
    ├── Boss spawn thresholds (1500, 4000, 7000, 10000, 15000, 18000)
    ├── Rocket unlock thresholds (2000, 5000, 10000)
    ├── Zone change thresholds (500, 1500, 4000, 8000, 15000)
    └── Achievement checks (100, 500, 4000, 8000, combo 10, overheat 25)
```

### Persistence Flow

```
SharedPreferences (persistent across runs)
    ├── highScore (Int)
    ├── discovery_<TYPE> (Boolean)       — 43 entries
    ├── art_<NAME>_date/count/alt/zone   — artifact records
    ├── unlock_<ROCKET> (Boolean)        — 4 rockets
    ├── achievement_<ID> (Boolean)       — 6 achievements
    ├── max_integrity (Float)            — permanent upgrade
    └── max_shield (Float)               — permanent upgrade
```

---

## Key Architectural Decisions

### 1. Flat Package Structure

All game code lives in a single package (`com.example.jump_droid`) with no sub-package separation. This works for the current size (~20 files) but would benefit from organization into `game/`, `render/`, `data/`, `ui/` as the codebase grows.

### 2. Monolithic GameScreen

`GameScreen.kt` (~2000 lines) contains the game loop, physics, collision detection, platform generation, HUD, and state machine. This is the single largest maintenance concern. Future refactoring should extract:
- Physics engine
- Collision system
- HUD rendering
- Platform generation logic
- Input handling

### 3. Canvas Rendering vs. Composables

All game visuals are drawn via `DrawScope` on a single `Canvas` composable. This gives full control over rendering order and avoids Compose recomposition overhead for 60fps game rendering. HUD elements mix Canvas drawing with Compose `Text` and `LinearProgressIndicator` overlays.

### 4. Data-Driven Threat System

Threats are defined as data templates (`ThreatDefinition`) registered in a central catalog (`ThreatRegistry`), then instantiated at runtime (`ActiveThreat`). This pattern makes adding new threats purely data-driven — register a definition with spawn rules, and the spawning logic handles the rest.

### 5. SharedPreferences for Persistence

All persistent data uses Android `SharedPreferences` directly (via `androidx.core.content.edit`). There is no database, no serialization framework, and no save file abstraction. Sufficient for current scope but limits complexity of future progression systems.

### 6. Sub-Stepped Physics

The physics and collision loop runs 4 substeps per frame to prevent fast-moving objects from passing through thin platforms. Each substep applies forces, updates positions, and checks AABB collisions independently.

---

## Extension Points

| Point | Mechanism | Example |
|-------|-----------|---------|
| New threats | Register in `ThreatRegistry.populateCatalog()` | `HAZ_PLASMA_STORM` |
| New missions | Register in `MissionRegistry` | New discovery/boss missions |
| New discoveries | Add to `DiscoveryType` enum | New lore or artifact entries |
| New platform types | Add to `PlatformType` enum + render method in `PlatformRenderer` | New `LAVA` platform |
| New rocket types | Add to `RocketType` enum | New stealth rocket |
| New achievements | Add to `AchievementsList` in `GameScreen.kt` | "Reach 25000m" |
| New ambient objects | Add to `AmbientType` enum + render case | New `COMET` ambient |
| New boss behaviors | Add behavior case in `ActiveThreat.update()` | New phase logic |

---

## Dependencies

Runtime dependencies (via Gradle version catalog):

- `androidx.compose.ui` — Canvas, layout, input, graphics
- `androidx.compose.material3` — HUD text, buttons, progress bars
- `androidx.activity.compose` — `ComponentActivity` + `setContent`
- `androidx.lifecycle.runtime.ktx` — Lifecycle-aware coroutines

No external game libraries, physics engines, or serialization frameworks.
