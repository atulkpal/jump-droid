# Release History — Jump Droid

All official releases with build artifacts and key changes.

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
