# Release History — Jump Droid

All official releases with build artifacts and key changes.

---

## v2.2.4 — Elite Urgency & Global Delivery

| Field | Value |
|-------|-------|
| **Date** | 2026-07-31 |
| **Version Code** | 14 |
| **Tag** | `v2.2.4` |
| **Branch** | `master` |
| **Status** | 🚀 Production Released on GitHub |

### Highlights
- **Engine Safety**: Fixed a fatal `RadialGradient` crash by guarding against `NaN` values and division by zero during engine initialization.
- **Elite Monetization**: Added dynamic Play Store offer selection and native discount detection.
- **Notification Hardening**: Restored Android 13+ background updates via mandatory permission flow.
- **UI Polish**: Fixed footer accessibility in Settings and unified the "Elite Upgrade" flow.

### Downloads
- [GitHub Release](https://github.com/atulkpal/jump-droid/releases/tag/v2.2.4)

---

## v2.2.3 — Zen Mastery & Impossible Uplink

| Field | Value |
|-------|-------|
| **Date** | 2026-07-30 |
| **Version Code** | 13 |
| **Tag** | `v2.2.3` |
| **Branch** | `feature/zen-mp-overhaul` |
| **Status** | ✅ Implementation Complete |

### Highlights
- **Absolute Zen**: Hardened isolation with zero boss spawning and zero achievement popups.
- **Album Curation**: Grouped Zen music into thematic albums with active selection highlighting.
- **Elite Block**: Multiplayer unlock set to "3 runs with 50x Combo" as a temporary dev blocker.
- **UX Safety**: Global system back button now correctly triggers the pause menu during any flight.
- **Robust Reset**: Fixed state race conditions to ensure clean mode transitions every time.

---

## v2.2.1 — Tactical Terminal & Boss Intelligence

| Field | Value |
|-------|-------|
| **Date** | 2026-07-30 |
| **Version Code** | 12 |
| **Tag** | `v2.2.1` |
| **Branch** | `bugfix/zen-terminal-polish` |
| **Status** | ✅ Implementation Complete |

### Highlights
- **Pilot Command Center**: Dense 2x4 telemetry grid with radar sweep and staggered animations.
- **CRT Vector Console**: Tactical monitor aesthetic for Zen/Multiplayer protocols.
- **Intelligent Boss Log**: Confirmed-only encounters (Slayed/Escaped/Defeated By) with "CLASSIFIED" filtering.
- **Death Attribution**: Active bosses now claim player defeats in the permanent pilot record.
- **UX Safety**: Automatic game resume on overlay dismissal (Back button hardening).

---

## v2.2.0 — Fleet Terminal & Hangar Ascension

| Field | Value |
|-------|-------|
| **Date** | 2026-07-29 |
| **Version Code** | 11 |
| **Tag** | `v2.2.0` |
| **Branch** | `feature/remote-announcements` |
| **Status** | 🚀 Production Deployment |

### Highlights
- **Fleet Terminal**: Firestore aggregate sync (99% read reduction) and write-squelching.
- **Hangar Restoration**: Pentagon stat chart and consolidated Command Console.
- **Hardened Gestures**: Velocity-sensitive flick physics for expedition reward cards.
- **Zen Immersion**: Binary rain backgrounds and flickering telemetry for locked states.
- **Intelligence Fixes**: Lifetime stat commitment during all game-over scenarios.

---

## v2.1.1 — Tactical Polish Update

| Field | Value |
|-------|-------|
| **Date** | 2026-07-29 |
| **Version Code** | 8 |
| **Tag** | `v2.1.1` |
| **Branch** | `master` |
| **Status** | ✅ Release Prep |

### Highlights
- **Zen Mode**: Holographic console with decryption progress bars and music selector.
- **Reward Refactor**: Stacked card deck with swipe-to-sync physics and currency HUD.
- **Lore Integration**: 48 unique mission debriefs and track-specific insignia watermarks.
- **Feature Lockdown**: Maintenance protocols for Cosmetics and Leaderboard for stability.
- **Menu Polish**: ARC station wiggle, badge centering, and collapsed default state.

---

## v2.0.0 — Production Ascension

| Field | Value |
|-------|-------|
| **Date** | 2026-07-27 |
| **Version Code** | 7 |
| **Tag** | `v2.0.0` |
| **Branch** | `master` |
| **Status** | ✅ Production Release |

### Highlights
- **Fleet Expansion**: Full access to Scout, Tank, and Experimental rocket classes.
- **Cosmetic Economy**: Integrated Currency Shop with tiered Continue Credits (250-2000 JC).
- **Visual Overhaul**: "Morning City" Earth backdrop and lively Main Menu with Scan Rings.
- **Flight Log System**: Non-blocking tactical notifications with physical "Thud" fly-in animations.
- **Privacy-First Connectivity**: Manual opt-in for Google Play Games connectivity.
- **Online Integration**: Stable Firebase Auth + Firestore leaderboards with telemetry logging.

---

## v1.5.3 — Scout Drone Gradient Crash Fix (Hotfix)

| Field | Value |
|-------|-------|
| **Date** | 2026-07-24 |
| **Version Code** | 6 |
| **Branch** | `hotfix/v1.5.3-scout-drone-gradient-crash` |
| **Status** | ✅ Hotfix |

### Highlights
- Fixed `IllegalArgumentException` crash in `ScoutDroneRenderer.render()` — `RadialGradient.nativeCreate` on certain devices when using a `Random`-generated dynamic glow radius. Radius is now clamped to `≥ 0.01f`.

---

## v1.5.2 — Closed Beta

| Field | Value |
|-------|-------|
| **Date** | 2026-07-15 |
| **Version Code** | 5 |
| **Tag** | `v1.5.2` |
| **Branch** | `master` |
| **Track** | Google Play Internal Testing + GitHub Releases |
| **Status** | ✅ Closed Beta Released |

### Highlights
- Continue-ready overlay with tap-to-resume
- Share button in Game Over and Main Menu
- Heat Bat 4-state AI overhaul
- Boss cooldown map (60s per boss)
- Archive "NEW" badge system
- ADR distribution strategy documentation

### Downloads
- [GitHub Release](https://github.com/atulkpal/jump-droid/releases/tag/v1.5.2)

---

## v1.5.1 — Release Polish Update (Hotfix)

| Field | Value |
|-------|-------|
| **Date** | 2026-07-13 |
| **Version Code** | 2 |
| **Tag** | `v1.5.1` |
| **Branch** | `master` |
| **Status** | ✅ Published |

### Highlights
- Portrait lock (app-wide `screenOrientation="portrait"`)
- Keep screen on during gameplay
- Fixed `SoundManager` crash on game over (`IllegalStateException` from `MediaPlayer.setVolume()`)
- Fixed `SoundManager` crash during music crossfade

### Downloads
- [GitHub Release](https://github.com/atulkpal/jump-droid/releases/tag/v1.5.1)

---

## v1.5.0 — Initial Production Release

| Field | Value |
|-------|-------|
| **Date** | 2026-07-13 |
| **Version Code** | 1 |
| **Tag** | `v1.5.0` |
| **Branch** | `master` |
| **Status** | ✅ Published (pre-release) |

### Highlights
- Firebase Analytics + Crashlytics integration
- AdMob banner + rewarded ads
- Google Play Billing (premium purchase)
- Production signing configuration (environment variable + keystore.properties fallback)
- Full EPIC 11 content: Ascension Protocol, Singularity meta-boss, prestige system, Eternal Mode

### Downloads
- [GitHub Release](https://github.com/atulkpal/jump-droid/releases/tag/v1.5.0)
- Debug APK, signed Release APK, signed Release AAB produced

---

## Artifact Naming Convention

| Artifact | Path | Usage |
|----------|------|-------|
| Debug APK | `app/build/outputs/apk/debug/app-debug.apk` | Internal dev testing |
| Release APK | `app/build/outputs/apk/release/app-release.apk` | Tester sideloading |
| Release AAB | `app/build/outputs/bundle/release/app-release.aab` | Google Play Console upload |

All release artifacts are signed with the `jump_droid_release.keystore` certificate (`CN=Ashwath AI`).

---

## Signing Configuration

Credentials resolve in this priority:
1. Environment variables: `STORE_FILE`, `STORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`
2. `keystore.properties` file (gitignored)
