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
├── GameScreen.kt                # The Central Orchestrator & game loop
│
├── screens/                     # Full-screen UI states
│   ├── TitleScreen.kt           # Ascent initiation & animated logo
│   ├── MainMenuScreen.kt        # Primary navigation & flight prep
│   ├── HangarScreen.kt          # Rocket selection & upgrade bay
│   ├── ArchiveScreen.kt         # Codex browser & completion tracking
│   ├── SettingsScreen.kt        # Technical configs & data management
│   ├── AboutScreen.kt           # Credits & mission background
│   └── LeaderboardScreen.kt     # High-score & rank hierarchy
│
├── overlays/                    # Contextual game layers
│   ├── PauseOverlay.kt          # Mid-run menu & developer tools
│   ├── TutorialOverlay.kt       # New discovery orientation cards
│   ├── HelpOverlay.kt           # Legend & control cheatsheet
│   ├── UnlockOverlay.kt         # New tech acquisition celebration
│   └── GameOverOverlay.kt       # Expedition summary & continuation
│
├── hud/                         # In-flight instrumentation
│   ├── HudWidgets.kt            # Gauges (Fuel, Heat, Shield, Hull)
│   ├── MissionRow.kt            # Dynamic objective tracking cards
│   ├── NotificationLayer.kt     # Priority alert queue & fading alerts
│   ├── FloatingTextsLayer.kt    # In-world status popups (e.g. Damage)
│   └── TopRightUtilityButtons.kt# Quick-access Help/Pause controls
│
├── managers/                    # Delegated gameplay logic ("The Brains")
│   ├── SurvivalManager.kt       # Damage math, regen, & destruction sequence
│   ├── EncounterDirector.kt     # AI Director: Spawning rules & boss timing
│   ├── PlatformManager.kt       # Generation math & streak tracking
│   ├── NotificationManager.kt   # System-wide alert queue management
│   ├── FloatingTextManager.kt   # Popup lifecycle & drift physics
│   ├── ThreatManager.kt         # Instance lifecycle & cleanup
│   ├── MissionManager.kt        # Objective tracking & track cycling
│   ├── ComboManager.kt          # Chain logic & survival milestones
│   ├── ProgressionManager.kt    # Persistence, achievements, & ranks
│   ├── DiscoveryManager.kt      # Codex unlocking & persistence
│   └── AltitudeManager.kt       # Zone progression & thresholds
│
├── threats/                     # Hostile entity system
│   ├── ActiveThreat.kt          # Entity AI & interaction rules
│   ├── ThreatRegistry.kt        # Catalog of 31 threat templates
│   ├── ThreatDefinition.kt      # Data model for threat archetypes
│   ├── ThreatSpawnRules.kt      # Eligibility & zone weights
│   ├── ThreatType.kt            # HAZARD / ENEMY / BOSS categories
│   ├── ThreatTier.kt            # TIER_1 through TIER_5 scaling
│   └── ThreatState.kt           # Lifecycle: SPAWNING → ACTIVE → DESTROYED
│
├── systems/                     # Core data & world systems
│   ├── Models.kt                # Player state, PowerUp, & Particle data
│   ├── Constants.kt             # Global tunable balance parameters
│   ├── Platform.kt              # 10 platform types & unique behaviors
│   ├── AltitudeZone.kt          # 6 distinct environment definitions
│   ├── Mission.kt               # Goal data & ceremony lifecycle
│   ├── MissionType.kt           # Objectives: Exploration, Survival, etc.
│   ├── MissionReward.kt         # Loot types: Artifacts, Modules, Fuel
│   ├── MissionRegistry.kt       # Template catalog of all objectives
│   └── Achievements.kt          # Permanent achievement definitions
│
├── render/                      # Canvas-based drawing pipelines
│   ├── RocketRenderer.kt        # Player ship, armor, & damage visuals
│   ├── PlatformRenderer.kt      # Material-specific platform themes
│   ├── ZoneBackgroundRenderer.kt# Parallax gradients & zone transitions
│   ├── CanvasEffects.kt         # Particles, SpeedLines, Flash, Distortion
│   ├── AmbientSystem.kt         # Environmental objects (birds, satellites)
│   ├── ParallaxSystem.kt        # Multi-layer parallax drift framework
│   └── CodexCard.kt             # Reusable UI component for the Archive
│
├── ui/theme/                    # Sci-Fi aesthetic definition
│   ├── Color.kt                 # Primary palette: Green, Cyan, Purple, etc.
│   ├── Theme.kt                 # Material3 & custom font wrappers
│   ├── Type.kt                  # Typography & text scale logic
│   └── TypographyConfig.kt      # Font family integration
│
└── DevConfig.kt                 # Developer cheat flag

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
