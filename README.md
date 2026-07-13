# Jump Droid

A vertical sci-fi exploration game where a lone droid-piloted rocket ascends through increasingly hostile atmospheric layers to reach "The Void." Built entirely with Kotlin and Jetpack Compose Canvas.

---

## Current Status

| | |
|---|---|
| **Version** | **v1.5.1** — Release Polish Update |
| **EPIC** | 11 — Ascension (The End) — **Complete** |
| **Stage** | Production |
| **Platform** | Android |
| **Engine** | Jetpack Compose + Kotlin |

---

## Features

### Gameplay
- **12 altitude zones** (Earth → Singularity) with dynamic BGM
- **11 boss encounters** with multi-hit weak point systems
- **26+ threat types** (hazards, enemies, bosses, mini-bosses)
- **14 platform types** with unique mechanics (Flux, Graviton, etc.)
- **4 rocket classes** + 17 unlockable modules for custom builds
- **12-track mission progression** system with evolving tiers
- **Combo system** with survival rewards
- **Prestige system** (+10% per reset, infinite scaling beyond 100km)
- **Eternal Mode** — capped infinite ascent past 100,000m

### Meta-Boss: The Singularity
The final encounter at 100,000m features HUD Pull and control glitch mechanics. Defeating it triggers the Ascension Ceremony — a cinematic overlay with Architect's Log and Hall of Pioneers. An origin reset unlocks Omega Modules (Void Engine / Singularity Core) and Eternal Mode.

### Systems
- Fuel, Heat, Shield, and Hull management
- Power-ups and rare artifacts (with set bonuses)
- Discovery & Codex system with hidden lore signals
- Dynamic unlock engine (AND/OR conditions)
- Secret missions and blueprints
- Achievement framework
- Continue / revive system

### Visual & Audio
- Atmospheric parallax backgrounds per zone
- Particle effects and screen shake
- Dynamic platform visuals
- 46 production OGG audio assets (14 BGM + 32 SFX)
- Programmatic PCM audio generation via SoundManager

---

## Building The Project

**Requirements:** Android Studio, Kotlin, Android SDK

```bash
git clone https://github.com/atulkpal/jump-droid.git
```

Open in Android Studio and run on an Android device or emulator.

### Building for Release (Signed APK/AAB)

Signing credentials are resolved in this priority order:

1. **Environment variables** (highest priority):
   ```bash
   # PowerShell
   $env:STORE_FILE="jump_droid_release.keystore"
   $env:STORE_PASSWORD="your-password"
   $env:KEY_ALIAS="your-alias"
   $env:KEY_PASSWORD="your-password"
   ./gradlew assembleRelease

   # Bash
   export STORE_FILE=jump_droid_release.keystore
   export STORE_PASSWORD=your-password
   export KEY_ALIAS=your-alias
   export KEY_PASSWORD=your-password
   ./gradlew assembleRelease
   ```

2. **`keystore.properties`** (fallback, gitignored):
   ```properties
   storeFile=jump_droid_release.keystore
   storePassword=your-password
   keyAlias=your-alias
   keyPassword=your-password
   ```

Copy `keystore.properties.example` to `keystore.properties` and fill in your credentials. The keystore file path is relative to the `app/` directory. Both the keystore (`app/jump_droid_release.keystore`) and `keystore.properties` are **gitignored** — they will never be committed.

---

## Releases

**Latest:** v1.5.1 — Release Polish Update ([Download](https://github.com/atulkpal/jump-droid/releases/tag/v1.5.1))

---

## Development History

| EPIC | Status | Summary |
|------|--------|---------|
| **EPIC 1** — World & Atmosphere | ✅ | Altitude architecture, zone progression, discovery system, atmospheric generation |
| **EPIC 2** — Visual Identity | ✅ | Rocket visual overhaul, platform visual language, particle systems, feedback |
| **EPIC 3** — Threats & Bosses | ✅ | Threat framework, hostile entities, environmental dangers, zone-specific encounters |
| **EPIC 4** — The Ascension Program | ✅ | Mission tree, achievement tracking, Codex system |
| **EPIC 5** — Survival Protocol | ✅ | Shield/Hull health model, environmental hazards, destruction sequence |
| **EPIC 6** — Hostile Skies | ✅ | Advanced AI, projectile systems, multi-phase boss encounters |
| **EPIC 7** — Rocket Evolution | ✅ | Customizable builds with Rocket Classes + 17-module Registry |
| **EPIC 8** — Missions & Progression | ✅ | Intelligence Network, 12-track dashboard, real-time stats |
| **EPIC 8.5** — Architecture Decomposition | ✅ | God-object decomposition: GameScreen 48% smaller, ActiveThreat 90% smaller |
| **EPIC 9** — Hidden Signals | ✅ | Secret missions, dynamic unlocks (AND/OR), artifact sets, lore logs, blueprints |
| **EPIC 10** — The Outer Reaches | ✅ | 4 new zones, Flux/Graviton platforms, 12 threats, 3 power-ups, 5 bosses |
| **EPIC 11** — Ascension (The End) | ✅ | The Singularity meta-boss, origin reset, prestige, Omega Modules, Eternal Mode |

---

## Known Issues

- Visual noise during high-combo streaks (excessive floating text) — non-blocking, deferred
- GameScreen.kt at ~2,080 lines — continued refactoring in `refactor/cleanup` branch

---

## License

Development project. License to be determined.
