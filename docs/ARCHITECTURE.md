# Jump Droid — Architecture

## Overview

Jump Droid is a single-activity Android app using Jetpack Compose. All gameplay rendering is done on a `Canvas` composable via `DrawScope` — there is no game engine, sprite system, or OpenGL. The game loop runs inside a `LaunchedEffect` with `withFrameNanos`, driving physics updates, collision detection, manager updates, and rendering each frame.

Following the EPIC 8.5 Architecture Decomposition, the monolithic `GameScreen.kt` was broken into three core components: `GameEngine` (state container + logic), `GamePlayScreen` (UI composition entry), and `WorldRenderer` (canvas dispatch). The system follows a delegated "Orchestrator" pattern where managers implement specific subsystems and the engine coordinates them.

---

## System Architecture

```
MainActivity
    │  (ComponentActivity + NavHost)
    ▼
GamePlayScreen (NavHost target — UI Composition)
    │
    ├── GameEngine (State Container + Game Loop)
    │   ├── runGameLoop() — LaunchedEffect withFrameNanos
    │   │   ├── inputProcessor.processInput() → effectiveThrust
    │   │   └── update(dt)
    │   │       ├── Utility managers (Notification, FloatingText, Combo, Discovery)
    │   │       ├── Environment (EncounterDirector, ThreatManager, AmbientManager)
    │   │       ├── SurvivalManager.update()
    │   │       ├── Sub-stepped physics (4× per frame):
    │   │       │   ├── Platform movement + break timers
    │   │       │   ├── Magnetic/Graviton field proximity effects
    │   │       │   ├── Projectile collision
    │   │       │   ├── Threat interactions (ActiveThreat.processInteraction)
    │   │       │   ├── handlePlayerPhysics() — gravity, thrust, steering
    │   │       │   └── resolveCollisions() — AABB player↔platform
    │   │       ├── Air friction (once per frame)
    │   │       ├── Thrust trail particles
    │   │       ├── Power-up management
    │   │       ├── Broken platform cleanup
    │   │       ├── Camera, score, zone updates
    │   │       └── Platform lifecycle (generation, recycling)
    │   │
    │   ├── State:
    │   │   ├── player (Player — mutableStateOf)
    │   │   ├── platforms (mutableStateListOf<Platform>)
    │   │   ├── gameState (GameState enum)
    │   │   ├── particles, landingEffects, flyingRewards
    │   │   ├── score, cameraY, screenShake, impactFlashAlpha, globalFogAlpha
    │   │   └── managers (Survival, Encounter, Platform, Mission, etc.)
    │   │
    │   └── Methods:
    │       ├── restartGame(), continueRun()
    │       ├── applyDamage(), handleLanding(), handlePowerUp()
    │       ├── spawnBurst(), checkDiscovery()
    │       └── saveHighScore(), getGameStats()
    │
    ├── WorldRenderer (Canvas dispatch — drawScope)
    │   ├── ZoneBackgroundRenderer (parallax + gradient per zone)
    │   ├── AmbientSystem (zone-specific ambient objects)
    │   ├── drawGround() (brown terrain)
    │   ├── PlatformRenderer (10 types with unique visuals)
    │   ├── drawParticles() (glow, cross-lines, trail)
    │   ├── drawLandingEffects() (colored expanding rings)
    │   ├── drawProjectiles()
    │   ├── drawTether() (cyan animated electrical bolt)
    │   ├── drawFlyingRewards() (combo reward animations)
    │   ├── ThreatRenderer (26 renderers via registry)
    │   ├── drawPowerUps()
    │   ├── RocketRenderer (thruster, body, shield, damage)
    │   ├── drawRealityDistortion() (void zone anomaly)
    │   ├── drawVisualObstruction() (radial fog)
    │   ├── drawSpeedLines() (high-velocity motion)
    │   └── drawImpactFlash() (white flash overlay)
    │
    └── UI Overlays (Composables)
        ├── Full-screen: Title, Main Menu, Hangar, Loadout, Archive, About, Leaderboard, Settings, Missions
        ├── HUD: HudWidgets.kt (Fuel, Heat, Shield, Integrity gauges), ComboDisplay, AltitudeDisplay
        └── Contextual: Pause, Help, Tutorial, GameOver, Unlock, AscensionCredits
```

---

## Game Loop (per-frame execution order)

```
GamePlayScreen → LaunchedEffect(withFrameNanos)
    │
    ▼
runGameLoop(currentTime, isThrusting, thrustTarget, inputProcessor)
    │
    1. Calculate dt = min(0.033f, (currentTime - lastFrameTime) / 1e9f)
    2. Process input → effectiveThrust, effectiveTarget
    3. update(dt):
       a. Module onUpdate hooks (per-frame module logic)
       b. Utility managers:
          ├── ComboManager.update(dt) — survival rewards, combo decay
          ├── NotificationManager.update(dt) — message queue
          ├── FloatingTextManager.update(dt) — popup drift
          ├── DiscoveryManager.update(dt) — cooldowns
          └── MissionManager — ceremony lifecycle, mission selection
       c. Environment:
          ├── EncounterDirector.update() — threat spawning weights
          ├── ThreatManager.update() — AI updates
          ├── AmbientManager.update() — ambient objects
          └── PowerUpManager — auto-spawn + movement + collection
       d. SurvivalManager.update() — shield regen, destruction sequence
       e. Particles, landing effects lifecycle
       f. Sub-stepped physics (4 substeps):
          ├── Reset isOnPlatform = false
          ├── Platform movement (moving platforms + break timers)
          ├── Magnetic/Graviton field proximity effects
          ├── Tether physics (activeTether.applyPhysics)
          ├── ProjectileManager.update() + player collision
          ├── Threat interactions (ActiveThreat.processInteraction)
          ├── handlePlayerPhysics() — gravity, thrust, steering, cooling
          └── resolveCollisions() — 4-directional AABB player↔platform
       g. Air friction (raw 0.98× once per frame, not per substep)
       h. Thrust trail particles (orange/cyan exhaust)
       i. Power-up collection
       j. Broken platform cleanup (remove + orange burst)
       k. Camera tracking (follows player ascent)
       l. Score calculation, zone updates, ascension protocol trigger
       m. Off-screen death check
    4. Render (via GamePlayScreen Canvas):
       ├── WorldRenderer.render(drawScope, engine) — all canvas drawing
       └── UI Overlays (composables stacked on Canvas)
```

---

## Component Map

### Entry Point & Orchestration

| File | Responsibility |
|------|---------------|
| `MainActivity.kt` | Sets up edge-to-edge, hosts NavHost with all screens. |
| `GamePlayScreen.kt` | NavHost target — composes Canvas + HUD + overlays, wires input, drives game loop. |
| `GameEngine.kt` | State container (player, platforms, score, game state, managers) + `runGameLoop()` + `update()` + all game logic. |
| `WorldRenderer.kt` | Central render pipeline — dispatches drawing to all renderers and CanvasEffects extensions. |
| `PlayerInputProcessor.kt` | Processes raw touch input into effective thrust + target with glitch factor support. |

### Core Logic Managers ("The Brains")

| File | Responsibility |
|------|---------------|
| `SurvivalManager.kt` | Centralizes damage distribution (Shield → Hull) and the 3-phase catastrophic destruction sequence. |
| `EncounterDirector.kt` | The "AI Director": decides hazard spawn types, zone-specific weights, boss arrival thresholds, **Difficulty Scaling (HP multipliers)**, and **Escalation Logic (Minion Summons)**. |
| `PlatformManager.kt` | Owns the mathematical generation of platforms and tracks streak counters (Breakable, Phase, Magnetic). |
| `NotificationManager.kt` | Encapsulates the message queue, priority alerts, and alpha/timer fading logic. |
| `FloatingTextManager.kt` | Manages the lifecycle and upward drift of status popups (e.g., "HULL IMPACT"). |
| `ThreatManager.kt` | Runtime lifecycle — spawn, update, and cleanup of threat instances. |
| `ProjectileManager.kt` | Manages lifecycle, rendering hooks, and collision for ranged attacks (Projectiles). |
| `InputBufferManager.kt` | Manages time-stamped input event queue to enable latency-based gameplay effects. |
| `ComboManager.kt` | Combo logic (chain on different platforms), 5 tiers, and survival milestone rewards. |
| `PowerUpManager.kt` | PowerUp lifecycle — spawning, movement (magnetic pull), collection detection. |

### Gameplay Systems

| File | Responsibility |
|------|---------------|
| `ActiveThreat.kt` | **Delegated Intelligence**: Handles its own interaction rules against player and other entities (E2E). Owns a **Phase-based AI State Machine** and triggers projectiles/minions via callbacks. |
| `Projectile.kt` | Data model for ranged attacks with support for bolts, missiles, beams, and waves. |
| `Tether.kt` | Physics sub-routine for distance-constrained restorative forces (Physical links). |
| `Models.kt` | `Player` class (mutable state), `PowerUp`, `Particle`, `FloatingText`, `LandingEffect`, enums (`GameState`, `RocketType`, `PlatformType`, `PowerUpType`, etc.). |
| `Constants.kt` | All tunable constants (gravity, thrust, fuel consumption rates, platform dimensions). |
| `MissionManager.kt` | Runtime tracking — activate, progress, complete, and auto-cycle 3 active tracks. |
| `ProgressionManager.kt` | Permanent account data: high score, achievements, artifact records, and ascension ranks. |
| `ThreatAIUpdater.kt` | AI behavior dispatch for all 26 threats (extracted from ActiveThreat). |
| `ThreatInteractionProcessor.kt` | Collision/damage dispatch for all 26 threats (extracted from ActiveThreat). |
| `ModuleRegistry.kt` | Registration of all module types with their onUpdate/onLanding/onDamage hooks. |
| `AltitudeManager.kt` | Zone progression tracking and zone unlock detection. |

### Rendering & Visuals

| File | Responsibility |
|------|---------------|
| `WorldRenderer.kt` | Central canvas render pipeline — orchestrates all draw calls. |
| `RocketRenderer.kt` | Rocket body, armor plates, battle damage, and destruction sequence visuals. |
| `CanvasEffects.kt` | Extension functions for DrawScope: Projectiles, Tethers, Ground, SpeedLines, Particles, Fog, Distortion, Impact Flash, Landing Effects, Flying Rewards, PowerUps. |
| `PlatformRenderer.kt` | Canvas rendering for 10 platform types with unique physics themes. |
| `ZoneBackgroundRenderer.kt` | Per-zone gradient backgrounds and parallax layer orchestration. |
| `AmbientSystem.kt` | Zone-specific ambient objects (birds, satellites, anomalies) with parallax drift. |
| `FloatingTextsLayer.kt` | Renders FloatingText instances with alpha fade and optional roundRect backgrounds. |
| `StarfieldBackground.kt` | Shared animated starfield composable (used across 7 screens). |
| `GaugeBar.kt` | Base gauge composable with shared clipPath + seg ticks + interference lines. |
| `HudWidgets.kt` | HUD composables: FuelGauge, HeatGauge, ShieldGauge, IntegrityGauge, ComboDisplay, AltitudeDisplay. |
| `ThreatRendererRegistry.kt` | Maps threat IDs to their renderers for dispatcher dispatch. |
| `*Renderer.kt` (26 files) | Per-threat canvas rendering (hazards, enemies, bosses). |

---

## Data Flow

### Input → Physics → Delegated Interaction

```
Touch Input (pointerInput)
    │
    ▼
GamePlayScreen (isThrusting = true, thrustTarget = position)
    │
    ▼
GameEngine.runGameLoop()
    ├── inputProcessor.processInput(isThrusting, target) → (effectiveThrust, effectiveTarget)
    ├── update(dt) → per substep:
    │   ├── ActiveThreat.processInteraction(player) — Apply effects (Force, Heat, Damage)
    │   ├── handlePlayerPhysics() — Apply gravity, thrust, steering, damping
    │   ├── resolveCollisions() — Player ↔ Platform 4-directional AABB
    │   └── Magnetic/Graviton field forces
    └── Render via WorldRenderer
```

### Escalation Flow (Intelligence Network)

```
ActiveThreat (Scout Drone) ──► Detects Player ──► Transmits (5s)
    │
    ▼
GameEngine → onEscalationEvent(zone)
    │
    ▼
EncounterDirector.getEscalationThreat(zone) ──► Hazard / Reinforcement
    │
    ▼
ThreatManager.spawnThreat(...)
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
`GameEngine.kt` does not implement gameplay rules directly. Instead, it provides the "Stage" (Player state, Collections) and tells the Managers when to act. This decomposition reduced `GameScreen.kt` from 3,901 to 8 lines and created focused files: `GameEngine.kt` (~935 lines), `GamePlayScreen.kt` (~223 lines), `WorldRenderer.kt` (~87 lines).

### 2. Delegated Threat Interaction
Moving threat interactions to `ActiveThreat.kt`, `ThreatAIUpdater.kt`, and `ThreatInteractionProcessor.kt` ensures the physics engine doesn't need to be modified when adding new hostile entities. Each threat is responsible for its own logic within the proximity of the player and other game entities (E2E).

### 3. Decoupled UI Framework
The UI is strictly partitioned into standalone `@Composable` files. Communication is handled via callbacks (e.g., `onNavigate`, `onLaunch`), preventing the UI from directly mutating gameplay state.

### 4. Sub-Stepped Physics Engine
The physics loop runs 4 substeps per frame. Interaction processing is called **within** these substeps to maintain pixel-perfect collision reliability and accurate force application. Magnetic/Graviton field effects, tether physics, and projectile collisions all execute inside these substeps.

### 5. WorldRenderer as Render Pipeline
All canvas drawing is centralized in `WorldRenderer.render()` which calls the appropriate renderers and `CanvasEffects` extensions in a fixed order. This decouples rendering logic from game state updates and provides a single location for visual debugging.

---

## The Journey of Change

Jump Droid began as a monolithic proof-of-concept where a single file (`GameScreen.kt`) handled everything. As the complexity grew—adding boss phases, survival economy, and artifact collections—the file became unmaintainable at 4,360+ lines.

1. **Sprints T1–T2 (UI Extraction)**: Removed 1,200+ lines of UI code. Partitioned the app into specialized Screens and HUD widgets. Created `CanvasEffects.kt` to clean up rendering flourishes.
2. **Sprint T3 (Logic Extraction)**: Modularized utility state. Extracted Notification and Floating Text handling. Moved high score and achievement audits into `ProgressionManager`.
3. **Sprint T4 (The Architectural Shift)**: Transitioned from an inline logic model to a delegated manager model. Extracted the "Brains" of the game into the `SurvivalManager` and `EncounterDirector`.
4. **Sprint C (Combat & Escalation)**: Finalized the transition from "Prototype Bosses" to "Production AI". Implemented global projectile systems, zone-based difficulty scaling, and the Scout Drone escalation network. Fixed the MINI_BOSS/BOSS logic branch.
5. **EPIC 8.5 (Architecture Decomposition)**: Decomposed `GameScreen.kt` (3,901→8 lines) and `ActiveThreat.kt` (1,224→123 lines). Extracted `GameEngine.kt` (state container + game loop), `WorldRenderer.kt` (render pipeline), `PlayerInputProcessor.kt` (input), and 26 threat renderers. Created `ThreatAIUpdater.kt`, `ThreatInteractionProcessor.kt`, `PowerUpManager.kt`, `MissionScreen.kt`, `ProgressionService.kt`, `GameStats.kt`, `HudContext.kt`, `GaugeBar.kt`, and `StarfieldBackground.kt`.
6. **EPIC 8.5 Recovery (Full Regression Cleanup)**: Restored behavioral parity with `epic11-complete` baseline. Fixed 12 regressions across physics (air friction, boundary bounce, multipliers, flux), collision (4-directional AABB), rendering (ground, particles, tether, fog, speedlines, flying rewards, visual obstruction), platform mechanics (PHASE intangibility, BREAKABLE/ghost triggering, magnetic/graviton fields, conveyor/moving carry, broken cleanup, 11 discovery checks, 8 missing landing branches), continue/respawn (platform-based respawn, destructionTimer reset, re-entry effects), effects (thrust trail, death burst, pendingReward infinite loop), and sound infrastructure.

The resulting architecture is robust, modular, and ready for high-velocity feature development.

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
| New minion spawns | Add to `EncounterDirector` escalation table | Boss summoning new enemy types |
| New sound effects | Drop .ogg in `res/raw/` + `soundManager.loadSfx("name", R.raw.file)` | Landing, thrust, damage SFX |

## Dependencies

- `androidx.compose.ui` — Canvas, layout, input, graphics.
- `androidx.compose.material3` — UI components and typography.
- `androidx.lifecycle.runtime.ktx` — Lifecycle-aware coroutines.

No external game libraries or engines are used.
