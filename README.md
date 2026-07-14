<div align="center">

# 🚀 Jump Droid

**A vertical rocket exploration game built with Kotlin and Jetpack Compose Canvas.**

Explore 12 hostile atmospheric zones, build modular rocket configurations, survive 11 boss encounters, and ascend beyond 100,000m to face The Singularity.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Latest Release](https://img.shields.io/github/v/release/atulkpal/jump-droid?include_prereleases&label=release)](https://github.com/atulkpal/jump-droid/releases/latest)
[![Platform](https://img.shields.io/badge/platform-android-3DDC84?logo=android)](https://github.com/atulkpal/jump-droid)
[![Kotlin](https://img.shields.io/badge/kotlin-2.0+-7F52FF?logo=kotlin)](https://kotlinlang.org)

</div>

---

## 📥 Download

| Build | Link |
|-------|------|
| **Latest Release (v1.5.1)** | [Download APK](https://github.com/atulkpal/jump-droid/releases/tag/v1.5.1) |
| Debug APK | Build from source (see [Installation](#-installation)) |

---

## 🎮 Gameplay Overview

Jump Droid is a precision vertical exploration simulator. You pilot a droid-controlled rocket through increasingly hostile atmospheric layers, managing fuel, heat, shields, and hull integrity while battling environmental hazards, enemy drones, and colossal bosses.

The core loop: **ascend → survive → discover → upgrade → ascend further.**

### Altitude Zones

| Zone | Description |
|------|-------------|
| Earth – Singularity | 12 distinct zones, each with unique visual themes, hazards, enemies, and BGM. |

Zone progression drives difficulty scaling, unlockable content, and the boss encounter schedule.

---

## ✨ Core Features

- **12 altitude zones** (Earth → The Singularity) — each with unique parallax backgrounds, hazards, and dynamic BGM
- **11 boss encounters** — multi-phase fights with weak point targeting and unique AI behaviors
- **26+ threat types** — hazards, enemies, mini-bosses, and bosses
- **14 platform types** — Flux, Graviton, Chrono-Rift, and more with unique traversal mechanics
- **4 rocket classes + 17 unlockable modules** — customize your build for different play styles
- **12-track mission system** — evolving tier progression (Rookie → God)
- **Combo & survival rewards** — risk-reward balancing for high-score play
- **Power-ups & artifacts** — rare drops with set bonuses
- **Prestige system** — permanent +10% hull/shield per reset after 100km
- **Eternal Mode** — capped infinite scaling past 100,000m
- **Secret missions & blueprints** — hidden content for deep explorers
- **Codex & lore discovery** — cryptic signals reward thorough exploration

### The Singularity

The final meta-boss encounter at 100,000m features HUD Pull and control glitch mechanics. Defeating it triggers the **Ascension Ceremony** — a cinematic overlay with Architect's Log and Hall of Pioneers. Survival unlocks Omega Modules and Eternal Mode.

---

## 🛠️ Technical Highlights

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.0+ |
| UI Framework | Jetpack Compose (Canvas rendering) |
| Audio Engine | SoundPool + MediaPlayer (46 production OGG assets) |
| Architecture | Component-based with extracted managers |
| State Management | Observable Compose state + coroutine-based game loop |
| Persistence | SharedPreferences |
| Analytics | Firebase Analytics + Crashlytics |
| Ads | AdMob (production IDs in release, test IDs in debug) |
| Build System | Gradle with Kotlin DSL |

### Architecture

The game loop uses 4 physics sub-steps per frame for collision reliability. Major systems are extracted into dedicated managers:

- `GameEngine.kt` — Central state container
- `ProgressionManager.kt` — Persistence, unlocks, prestige
- `EncounterDirector.kt` — Spawn rules and boss milestones
- `MissionManager.kt` — 12-track mission progression
- `SoundManager.kt` — Audio engine with crossfade support
- `PlayerInputProcessor.kt` — Input handling with glitch hooks

---

## 📸 Screenshots

> Screenshots coming soon. This section will be updated with promotional imagery during Phase 3.

<!-- 
  Phase 3 — Screenshot & Community:
  Add promotional screenshots here:
  - Title screen
  - Gameplay (early zone)
  - Gameplay (boss encounter)
  - HUD showcase
  - Hangar / Build customization
  - Ascension Ceremony overlay
-->

| | | |
|---|---|---|
| `[Screenshot 1]` | `[Screenshot 2]` | `[Screenshot 3]` |
| `[Screenshot 4]` | `[Screenshot 5]` | `[Screenshot 6]` |

---

## 📖 Installation

### Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK 34+
- Kotlin 2.0+

### Build from Source

```bash
git clone https://github.com/atulkpal/jump-droid.git
cd jump-droid
```

Open the project in Android Studio, sync Gradle, and run on a device or emulator.

### Building a Release APK/AAB

Signing credentials are resolved in this priority order:

1. **Environment variables:**
   ```bash
   export STORE_FILE=jump_droid_release.keystore
   export STORE_PASSWORD=your-password
   export KEY_ALIAS=your-alias
   export KEY_PASSWORD=your-password
   ./gradlew assembleRelease
   ```

2. **`keystore.properties`** (gitignored, fallback):
   ```properties
   storeFile=jump_droid_release.keystore
   storePassword=your-password
   keyAlias=your-alias
   keyPassword=your-password
   ```

Copy `keystore.properties.example` → `keystore.properties` and fill in credentials. The keystore (`app/jump_droid_release.keystore`) and properties file are gitignored.

---

## 🧭 Development Philosophy

- **Physics-first**: Movement and collision are the heart of the experience
- **Hard but fair**: High difficulty balanced by robust recovery mechanics
- **Modular progression**: Your rocket is a "Build," not a vehicle
- **No pay-to-win**: All power modules earned through gameplay
- **Tactical HUD**: Critical info prioritized, clutter minimized
- **Mystery & discovery**: Hidden content rewards deep exploration

---

## 📊 Project Status

| | |
|---|---|
| **Version** | v1.5.1 — Release Polish Update |
| **Latest EPIC** | 11 — Ascension (The End) — **Complete** |
| **Next** | EPIC 12 — Fleet Expansion (planned) |
| **Platform** | Android |
| **License** | MIT |

### Known Issues

- Visual noise during high-combo streaks (excessive floating text) — deferred
- GameScreen.kt at ~2,080 lines — ongoing refactoring

### EPIC History

| EPIC | Description |
|------|-------------|
| 1–3 | World, visuals, threats & bosses |
| 4 | Mission tree, achievements, Codex |
| 5 | Survival protocol (shield/hull, destruction) |
| 6 | Advanced AI, projectiles, multi-phase bosses |
| 7 | Rocket customization (4 classes, 17 modules) |
| 8 | Missions & progression, Intelligence Network |
| 8.5 | Architecture decomposition (48% GameScreen reduction) |
| 9 | Hidden signals, dynamic unlocks, artifacts |
| 10 | 4 new zones, 12 threats, 5 bosses |
| 11 | The Singularity, prestige, Eternal Mode |

---

## 💬 Feedback & Community

- **Issues & Feature Requests**: [GitHub Issues](https://github.com/atulkpal/jump-droid/issues)
- **Discussions**: GitHub Discussions (coming soon)
- **Report a Bug**: Open a [bug report](https://github.com/atulkpal/jump-droid/issues/new?labels=bug)

Contributions are welcome\! Please read the design library rules in [`AGENTS.md`](AGENTS.md) before implementing gameplay content.

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [`AGENTS.md`](AGENTS.md) | Project governance, architecture decisions, branch policy |
| [`docs/CHANGELOG.md`](docs/CHANGELOG.md) | Full release history |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | System architecture overview |
| [`docs/JumpDroid_EPIC_Tracker.md`](docs/JumpDroid_EPIC_Tracker.md) | EPIC roadmap |
| [`docs/INVENTORY.md`](docs/INVENTORY.md) | Technical specs index |
| [`docs/design/`](docs/design/) | Design libraries (threats, platforms, zones, power-ups, lore, artifacts, rockets) |
| [`docs/releases/`](docs/releases/) | Publication reports |

---

<div align="center">

Built with ❤️ using Kotlin & Jetpack Compose

[Report a Bug](https://github.com/atulkpal/jump-droid/issues) · [Request a Feature](https://github.com/atulkpal/jump-droid/issues) · [Latest Release](https://github.com/atulkpal/jump-droid/releases/latest)

</div>
