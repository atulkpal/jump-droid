<p align="center">
  <img src="https://raw.githubusercontent.com/atulkpal/jump-droid/feature/github-media-kit/media/logos/hero-banner.jpg" alt="Jump Droid — Ascend Beyond the Atmosphere" width="800">
</p>

<p align="center">
  <b>A vertical rocket exploration game built with Kotlin and Jetpack Compose Canvas.</b><br>
  Precision propulsion, modular ship builds, and high-intensity atmospheric discovery.
</p>

<p align="center">
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License: MIT"></a>
  <a href="https://github.com/atulkpal/jump-droid/releases/latest"><img src="https://img.shields.io/github/v/release/atulkpal/jump-droid?include_prereleases&label=release" alt="Latest Release"></a>
  <a href="https://github.com/atulkpal/jump-droid"><img src="https://img.shields.io/badge/platform-android-3DDC84?logo=android" alt="Platform: Android"></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/kotlin-2.0+-7F52FF?logo=kotlin" alt="Kotlin 2.0+"></a>
  <a href="https://github.com/atulkpal/jump-droid/issues"><img src="https://img.shields.io/github/issues/atulkpal/jump-droid" alt="Issues"></a>
  <a href="https://github.com/atulkpal/jump-droid/stargazers"><img src="https://img.shields.io/github/stars/atulkpal/jump-droid?style=flat&logo=github" alt="Stars"></a>
</p>

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

<p align="center">
  <img src="https://raw.githubusercontent.com/atulkpal/jump-droid/feature/github-media-kit/media/screenshots/02-master-precision-flight.jpg" alt="Gameplay in the Earth zone with HUD visible" width="800">
  <br>
  <em>Navigating the lower atmosphere — manage your gauges and avoid hazards.</em>
</p>

### Altitude Zones

Explore 12 distinct zones, from Earth's surface to the reality-warped Singularity. Each zone features unique parallax backgrounds, environmental hazards, enemy types, and dynamic background music.

| Zone | Description |
|------|-------------|
| Earth → Cloud Layer → Upper Atmosphere → Orbit | The early ascent — learn the mechanics |
| The Foundry → Deep Space → Chrono-Rift | Mid-game challenges and new platform types |
| Void → The Beyond → Stellar Gate → Ancient Construct | Late-game intensity |
| **The Singularity** | The final meta-boss at 100,000m |

### Boss Encounters

Face 11 unique bosses across your ascent. Each boss features multi-phase attack patterns, weak point targeting, and distinct AI behaviors — from the Cloud Commander's patrol patterns to the reality-warping Singularity's HUD manipulation.

<p align="center">
  <img src="https://raw.githubusercontent.com/atulkpal/jump-droid/feature/github-media-kit/media/screenshots/03-face-colossal-bosses.jpg" alt="Boss encounter with the Cloud Commander" width="800">
  <br>
  <em>Every boss demands precision, adaptation, and knowledge of their attack patterns.</em>
</p>

---

## ✨ Core Features

### 🚀 Build Your Rocket

Your rocket is not a vehicle — it's a **Build.** Choose from 4 rocket classes and customize with 17 unlockable modules. Each module changes how you play, and the combination possibilities create deep strategic variety.

<p align="center">
  <img src="https://raw.githubusercontent.com/atulkpal/jump-droid/feature/github-media-kit/media/screenshots/04-build-your-perfect-fleet.jpg" alt="Rocket build customization in the Hangar" width="800">
  <br>
  <em>The Hangar — where you craft your perfect ascent configuration.</em>
</p>

### ⚔️ Combat & Survival

- **26+ threat types** — environmental hazards, enemy drones, mini-bosses, and screen-filling bosses
- **Fuel, Heat, Shield, Hull management** — every gauge matters
- **14 platform types** — Flux, Graviton, Chrono-Rift, and more with unique traversal mechanics
- **Power-ups & artifacts** — rare drops with set bonuses

### 📋 Mission System

A 12-track progression system with tiers that evolve from Rookie to God. Complete challenges, earn rewards, and unlock hidden content.

### 🧩 Discovery & Lore

Secret missions, hidden lore signals, and Codex entries reward thorough exploration. The dynamic unlock engine uses AND/OR conditions for flexible progression.

### 🌌 Endgame

<p align="center">
  <img src="https://raw.githubusercontent.com/atulkpal/jump-droid/feature/github-media-kit/media/screenshots/07-every-expedition-makes-you-stronger.jpg" alt="The Ascension Ceremony overlay" width="800">
  <br>
  <em>The Ascension Ceremony — a cinematic culmination of your journey.</em>
</p>

Beyond 100,000 meters lies **The Singularity** — the final meta-boss with HUD Pull and control glitch mechanics. Defeating it triggers the **Ascension Ceremony** and unlocks:

- **Prestige system** — permanent +10% hull/shield per reset
- **Omega Modules** — Void Engine (infinite fuel), Singularity Core (perfect stability)
- **Eternal Mode** — capped infinite scaling for endless ascent

---

## 🖥️ HUD & Interface

The tactical HUD prioritizes critical information — fuel, heat, shields, hull integrity, and combo tracking — while minimizing visual clutter.

<p align="center">
  <img src="https://raw.githubusercontent.com/atulkpal/jump-droid/feature/github-media-kit/media/screenshots/06-adapt-or-be-destroyed.jpg" alt="Game HUD showing all gauges and combo display" width="800">
  <br>
  <em>Clean, tactical HUD displaying everything you need at a glance.</em>
</p>

---

<p align="center">
  <img src="https://raw.githubusercontent.com/atulkpal/jump-droid/feature/github-media-kit/media/screenshots/01-begin-the-ascension.jpg" alt="Jump Droid title screen with scanning drone" width="800">
  <br>
  <em>The title screen — your journey begins here.</em>
</p>

---

## 🛠️ Technical Highlights

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.0+ |
| UI Framework | Jetpack Compose Canvas |
| Audio Engine | SoundPool + MediaPlayer (46 production OGG assets) |
| Architecture | Component-based with extracted managers |
| State Management | Observable Compose state + coroutine-based game loop |
| Persistence | SharedPreferences |
| Analytics | Firebase Analytics + Crashlytics |
| Ads | AdMob (production IDs in release, test IDs in debug) |
| Build System | Gradle with Kotlin DSL |

### Architecture

The game loop uses 4 physics sub-steps per frame for collision reliability. Major systems are extracted into dedicated managers:

| Manager | Responsibility |
|---------|---------------|
| `GameEngine.kt` | Central state container, orchestrates all subsystems |
| `ProgressionManager.kt` | Persistence, unlocks, prestige, lifetime stats |
| `EncounterDirector.kt` | Spawn rules, boss milestones, zone difficulty scaling |
| `MissionManager.kt` | 12-track mission progression with tier evolution |
| `SoundManager.kt` | Audio engine with crossfade, ducking, per-zone BGM |
| `PlayerInputProcessor.kt` | Input handling with glitch effect hooks |

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

Open the project in Android Studio, sync Gradle, and run on a device or emulator (API 34+).

### Building a Release APK/AAB

Signing credentials are resolved in this priority order:

1. **Environment variables**:
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
- **Modular progression**: Your rocket is a Build, not a vehicle
- **No pay-to-win**: All power modules earned through gameplay
- **Tactical HUD**: Critical info prioritized, clutter minimized
- **Mystery & discovery**: Hidden content rewards deep exploration

---

## 📊 Project Status

| | |
|---|---|
| **Version** | v1.5.1 — Release Polish Update (Hotfix) |
| **Latest EPIC** | 11 — Ascension (The End) — **Complete** |
| **Next** | EPIC 12 — Fleet Expansion (planned) |
| **Platform** | Android (API 34+) |
| **License** | [MIT](LICENSE) |

### Known Issues

- Visual noise during high-combo streaks (excessive floating text) — deferred
- `GameScreen.kt` at ~2,080 lines — ongoing refactoring in `refactor/cleanup` branch

### EPIC History

| EPIC | Summary |
|------|---------|
| **1–3** | World architecture, visual identity, threats & bosses |
| **4** | Mission tree, achievements, Codex discovery system |
| **5** | Survival protocol — shield/hull model, destruction sequence |
| **6** | Hostile Skies — AI, projectiles, multi-phase bosses |
| **7** | Rocket Evolution — 4 classes, 17-module Registry |
| **8** | Missions & Progression — Intelligence Network, 12-track dashboard |
| **8.5** | Architecture Decomposition — 48% GameScreen reduction |
| **9** | Hidden Signals — secret missions, dynamic unlocks, artifacts |
| **10** | The Outer Reaches — 4 new zones, 12 threats, 5 bosses |
| **11** | Ascension — The Singularity, prestige, Omega Modules, Eternal Mode |

---

## 💬 Feedback & Community

- **Bug Reports**: [Open a bug report](https://github.com/atulkpal/jump-droid/issues/new?labels=bug)
- **Feature Requests**: [Open a feature request](https://github.com/atulkpal/jump-droid/issues/new?labels=enhancement)
- **Discussions**: [GitHub Discussions](https://github.com/atulkpal/jump-droid/discussions) (Q&A, ideas, community)
- **Security Issues**: See [SECURITY.md](SECURITY.md)

Contributions are welcome! Read [`CONTRIBUTING.md`](CONTRIBUTING.md) to get started. For gameplay content, review the design library rules in [`AGENTS.md`](AGENTS.md).

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [`AGENTS.md`](AGENTS.md) | Project governance, architecture decisions, branch policy |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | Guide for contributors |
| [`docs/CHANGELOG.md`](docs/CHANGELOG.md) | Full release history |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | System architecture overview |
| [`docs/JumpDroid_EPIC_Tracker.md`](docs/JumpDroid_EPIC_Tracker.md) | EPIC roadmap and milestone tracking |
| [`docs/INVENTORY.md`](docs/INVENTORY.md) | Technical document index |
| [`docs/design/`](docs/design/) | Design libraries (threats, platforms, zones, power-ups, lore, artifacts, rockets) |
| [`docs/releases/`](docs/releases/) | Publication reports for each release |
| [`docs/marketing/`](docs/marketing/) | Brand guide, store copy, social posts, press templates |
| [`media/PRESS_KIT.md`](media/PRESS_KIT.md) | Official press kit with asset index |

---

## 📸 Media Kit

Promotional assets (logos, screenshots, wallpapers, social graphics) are available in the [`media/`](media/) directory. See the [Press Kit](media/PRESS_KIT.md) for usage guidelines.

---

<div align="center">

<p>
  <a href="https://github.com/atulkpal/jump-droid">
    <img src="https://raw.githubusercontent.com/atulkpal/jump-droid/feature/github-media-kit/media/logos/extra-01-logo.jpg" alt="Jump Droid" width="64">
  </a>
</p>

**Built with ❤️ using Kotlin & Jetpack Compose**

[Report a Bug](https://github.com/atulkpal/jump-droid/issues) · [Request a Feature](https://github.com/atulkpal/jump-droid/issues) · [Latest Release](https://github.com/atulkpal/jump-droid/releases/latest)

</div>
