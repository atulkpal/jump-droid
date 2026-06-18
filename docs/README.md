# Jump Droid

**Reach what keeps calling from above.**

Jump Droid is a vertical survival platformer for Android. You pilot an experimental rocket through six increasingly hostile atmospheric zones — from Earth's blue skies to the reality-distorting Void — managing fuel, heat, shield, and hull while chaining platform landings, surviving boss encounters, and uncovering a mystery that has been calling from above.

Built entirely with Kotlin and Jetpack Compose Canvas rendering. No game engine — just raw `DrawScope` and physics.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI Framework | Jetpack Compose (Canvas-based rendering) |
| Build System | Gradle + Version Catalog (`libs.versions.toml`) |
| Min SDK | 24 |
| Target SDK | 37 |
| Persistence | SharedPreferences |
| Game Engine | None — custom game loop via `withFrameNanos`, physics, and `DrawScope` rendering |

---

## Build & Run

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34+

### Steps

```bash
# Clone
git clone <repo-url>
cd Jump_droid

# Build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Or open in Android Studio
# File → Open → select Jump_droid directory → Run
```

---

## Features

### Ascent Journey (6 Zones)

| Zone | Altitude | Color | Key Features |
|------|----------|-------|-------------|
| Earth | 0m | Green | Tutorial space, normal platforms, no hazards |
| Cloud Layer | 500m | Cyan | Moving/ice platforms, lightning, turbulence |
| Upper Atmosphere | 1500m | Purple | Breakable/phase platforms, radiation |
| Orbit | 4000m | Gold | EMP, defense nodes, Command Cruiser mini-boss |
| Deep Space | 8000m | Purple-Blue | Gravity distortion, Leviathan, Star-Eater |
| The Void | 15000m | Red | All hazards, Void Engine, The Signal |

### Platform System (10 Types)

Normal, Moving, Boost, Ice, Breakable, Phase, Fuel, Cooling, Stability, Magnetic — each with unique physics and visual rendering.

### Threat System (31 Entries)

- **7 Hazards:** Lightning, Turbulence, Debris, Radiation, Solar Flare, Gravity Distortion, EMP
- **6 Enemies:** Surveyor Probe, Sky Ray, Aerosol Swarm, Orbital Sentry, Derelict Echo, Shadow Entity
- **1 Mini-Boss:** Command Cruiser (3-phase AI)
- **5 Bosses:** Gatekeeper, Star-Eater, Leviathan, Void Engine, The Signal — each with destructible weak points

### Survival Systems

- **Fuel** — Consumed during thrust (27/s), recharges while coasting (40/s). Base 100 capacity.
- **Heat** — Builds during thrust (13/s), overheat at 100% triggers 2-second engine lockout.
- **Shield** — Absorbs damage first, auto-regens after 4-second delay (1/s). Base 50.
- **Hull** — Last line of defense, run ends at 0. 2.5-second destruction sequence with visual break-up.

### Combo System

Chain landings on different platform types. 5 named tiers (Basic → Legendary) with fuel, power-ups, altitude boosts, and artifact rewards. Survival supplies auto-spawn at combo milestones 5/10/15/20+.

### Rocket Types (4)

Balanced (all-around), Scout (high thrust, low fuel), Tank (massive fuel capacity), Experimental (extreme thrust, high heat risk). Each unlocked at score milestones.

### Discovery / Codex

43 discoverable entries across 7 categories (Platforms, PowerUps, Mechanics, Areas, Rockets, Lore, Artifacts, Threats). Persist across runs. Feed into Ascension Rank for permanent stat upgrades.

### Mission System

15 missions across 4 tracks (Exploration, Platforming, Survival, Discovery/Boss). 3 active at a time, auto-cycle on completion with ceremony animation.

---

## Project Structure

```
app/src/main/java/com/example/jump_droid/
├── MainActivity.kt              # Entry point, Compose host
├── GameScreen.kt                # Game loop, physics, collision, HUD, state machine
├── Models.kt                    # Core data types: Player, PowerUp, Particle, etc.
├── Constants.kt                 # All tunable game constants
├── AltitudeManager.kt           # Zone progression tracking
├── AltitudeZone.kt              # 6 altitude zone definitions
├── Platform.kt                  # 10 platform types + runtime state
├── PlatformRenderer.kt          # Canvas rendering for platforms
├── RocketRenderer.kt            # Canvas rendering for rocket with damage/shield visuals
├── ZoneBackgroundRenderer.kt    # Parallax background rendering per zone
├── ParallaxSystem.kt            # Generic parallax layer framework
├── AmbientSystem.kt             # Ambient objects (birds, satellites, anomalies)
├── ComboManager.kt              # Combo tracking, tiers, survival rewards
├── DiscoveryManager.kt          # Codex discovery + persistence
├── ProgressionManager.kt        # Permanent progression, artifact records, ranks
├── MissionManager.kt            # Active mission tracking + progress
├── MissionRegistry.kt           # 15 mission template definitions
├── Mission.kt                   # Mission data model + ceremony stages
├── MissionType.kt               # Mission category enum
├── MissionReward.kt             # Reward types (PowerUp, Artifact, Unlock)
├── ThreatManager.kt             # Runtime threat lifecycle
├── ThreatRegistry.kt            # 31 threat definitions
├── ActiveThreat.kt              # Threat instance with AI behaviors
├── ThreatDefinition.kt          # Threat data template
├── ThreatSpawnRules.kt          # Spawn conditions per threat
├── ThreatType.kt                # HAZARD / ENEMY / MINI_BOSS / BOSS
├── ThreatTier.kt                # Danger level (TIER_1 through TIER_5)
├── ThreatState.kt               # Lifecycle state enum
├── DevConfig.kt                 # Developer cheat toggle
├── Boss.kt                      # Legacy boss model (superseded by ActiveThreat)
└── ui/theme/
    ├── Color.kt                 # Sci-Fi color palette (6 primary colors)
    ├── Theme.kt                 # Material theme wrappers
    ├── Type.kt                  # Typography definitions
    └── TypographyConfig.kt      # Font configuration
```

---

## Design Docs

Comprehensive website design and content strategy documentation lives in `website/v2/`:

- `DESIGN_BRIEF.md` — Audience, identity, goals, success criteria
- `VISUAL_DIRECTION.md` — Color, typography, motion, responsive strategy
- `CONTENT_STRATEGY.md` — Messaging for every system, codebase references
- `WEBSITE_ARCHITECTURE.md` — Section layout, zone mapping, nav, mobile
- `GAME_TO_WEBSITE_MAPPING.md` — Every game system → website representation

---

## License

[License information TBD]
