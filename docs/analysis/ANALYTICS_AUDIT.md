# Analytics Architecture Audit

**Date:** 2026-07-16
**Project:** Jump Droid
**Version:** v1.5.2
**Type:** Read-Only Audit

---

## Existing Documentation

Every document reviewed during this audit:

| # | Document | Analytics Relevance |
|---|----------|-------------------|
| 1 | `docs/INVENTORY.md` | Lines 69-75: Lists analytics/ad files (with incorrect paths) |
| 2 | `docs/ARCHITECTURE.md` | No analytics section; mentions `getGameStats()` as engine method only |
| 3 | `docs/CHANGELOG.md` | Scattered mentions of Intelligence Network, stat tracking, Firebase (non-analytics usage) |
| 4 | `docs/releases/v1.5.0_PUBLICATION_REPORT.md` | Lines 41-42: Firebase Analytics "Enabled" for debug; lines 58-59: release; line 111: operational check |
| 5 | `docs/releases/v1.5.1_PUBLICATION_REPORT.md` | Lines 55-56: Firebase Analytics + Crashlytics "Enabled"; line 127-128: validation checks |
| 6 | `docs/releases/v1.5.2_PUBLICATION_REPORT.md` | No explicit analytics mentions |
| 7 | `docs/ADR_DISTRIBUTION_STRATEGY.md` | Line 58: Rejected Firebase App Distribution ("Overkill for <100 testers") |
| 8 | `docs/analysis/Phase_8_2_Report.md` | Documents Intelligence Network (internal stats, not Firebase) |
| 9 | `docs/roadmap/EPIC_8_TRACKER.md` | Phase 2: Intelligence Network — 19 gameplay metrics tracking |
| 10 | `docs/roadmap/EPIC_9_TRACKER.md` | Sprint 9.2.5: telemetry hook in `ProgressionManager.checkModuleUnlocks()` |
| 11 | `docs/roadmap/MONETIZATION_VISION.md` | No analytics mention; references ad framework |
| 12 | `docs/JumpDroid_EPIC_Tracker.md` | No analytics-specific entries |

**No dedicated `docs/ANALYTICS.md` exists.**

**No existing `docs/analysis/*analytics*` audit document exists.**

---

## Existing Analytics Architecture

The analytics system follows a **domain-driven interface** pattern:

```
GameAnalytics (interface)
    └── FirebaseGameAnalytics (Firebase Analytics implementation)
```

### Key Components

| Component | File | Role |
|-----------|------|------|
| `GameAnalytics` interface | `GameAnalytics.kt:11-24` | Defines 12 domain-specific analytics methods |
| `FirebaseGameAnalytics` | `GameAnalytics.kt:30-132` | Firebase implementation mapping domain events to Firebase events |
| `LocalAnalytics` CompositionLocal | `MainActivity.kt:15` | Provides analytics instance through Compose tree |
| Firebase initialization | `MainActivity.kt:23` | `FirebaseApp.initializeApp(this)` in `onCreate()` |
| Analytics instantiation | `MainActivity.kt:24` | `analytics = FirebaseGameAnalytics(this)` |

### Injection Points

| File | Purpose |
|------|---------|
| `GameEngine.kt:25` | Constructor parameter `val analytics: GameAnalytics` |
| `MainActivity.kt:28` | Passed to `GameEngine(this, analytics)` |
| `MainActivity.kt:30` | Provided via `CompositionLocalProvider(LocalAnalytics provides analytics)` |
| Consumer composables | Accessed via `val analytics = LocalAnalytics.current` |

### Infrastructure

| Component | Status |
|-----------|--------|
| `google-services.json` | Present at `app/google-services.json` (project: `jump-droid`) |
| Firebase BOM | `33.10.0` via `gradle/libs.versions.toml:16` |
| Firebase Analytics | `com.google.firebase:firebase-analytics` via `app/build.gradle.kts:74` |
| Firebase Crashlytics | `com.google.firebase:firebase-crashlytics` via `app/build.gradle.kts:75`, plugin applied |
| AdMob | `com.google.android.gms:play-services-ads` via `libs.versions.toml:34` |
| Google Services plugin | Applied in `app/build.gradle.kts:4` |

---

## Existing Firebase Events

| Event | Type | Parameters | Purpose | Where Fired |
|-------|------|------------|---------|-------------|
| `level_start` | Standard (FirebaseAnalytics.Event.LEVEL_START) | `rocket_type`, `rocket_title` | Track game session start with rocket choice | `GameEngine.kt:989` — `analytics.logGameStart()` |
| `level_end` | Standard (FirebaseAnalytics.Event.LEVEL_END) | `score`, `level_name`, `rocket_type`, `failure_reason` | Track game over with score, zone, rocket, and failure reason | `GameEngine.kt:343` — `"structural_failure"`, `763` — `"hull_breach"`, `981` — `"off_screen_fall"` |
| `zone_changed` | Custom | `level_name` | Track zone transitions during gameplay | `GameEngine.kt:127` — zone change callback |
| `mission_started` | Custom | `mission_id`, `mission_type`, `mission_category` | Track mission activation | `GameEngine.kt:147` |
| `mission_completed` | Custom | `mission_id`, `mission_type` | Track mission completion | `GameEngine.kt:144` |
| `boss_spawned` | Custom | `boss_id`, `boss_name`, `zone` | Track boss encounters | `GameEngine.kt:948` — EncounterDirector boss spawn callback |
| `boss_defeated` | Custom | `boss_id`, `zone` | Track boss defeats | `GameEngine.kt:133` |
| `rocket_unlocked` | Custom | `rocket_id` | Track rocket unlock progression | `GameEngine.kt:306` |
| `module_equipped` | Custom | `module_id`, `slot_index` | Track module loadout choices | `GameEngine.kt:150` |
| `screen_view` | Standard (FirebaseAnalytics.Event.SCREEN_VIEW) | `screen_name`, `screen_class` | Track screen navigation | `MainActivity.kt:56` — NavHost back stack entry flow |
| `ad_impression` | Standard (FirebaseAnalytics.Event.AD_IMPRESSION) | `ad_type`, `ad_unit_id` | Track ad views | `AdComponents.kt:30` — banner impression, `61` — rewarded impression |
| `ad_clicked` | Custom | `ad_type`, `ad_unit_id` | Track ad interactions | `AdComponents.kt:33` — banner click, `GameOverOverlay.kt:207` — rewarded continue click |

**Total: 12 events (4 standard, 8 custom)**

---

## Existing Screen Tracking

Screen tracking is implemented via a single `NavHost` back stack entry observer in `MainActivity.kt:54-56`:

```kotlin
navController.currentBackStackEntryFlow.collect { backStackEntry ->
    val route = backStackEntry.destination.route ?: "unknown"
    analytics.logScreenView(route, route.replaceFirstChar { it.uppercase() })
}
```

Tracked screens (all `NavHost` destinations):

| Route | Screen Class | Notes |
|-------|-------------|-------|
| `"title"` | `"Title"` | Title screen — first screen on launch |
| `"main_menu"` | `"Main_menu"` | Main menu hub |
| `"game"` | `"Game"` | Active gameplay screen |
| `"hangar"` | `"Hangar"` | Rocket hangar / customization |
| `"loadout"` | `"Loadout"` | Module loadout screen |
| `"archive"` | `"Archive"` | Codex / discoveries / lore |
| `"settings"` | `"Settings"` | App settings |
| `"about"` | `"About"` | About / system protocol |
| `"missions"` | `"Missions"` | Mission dashboard |
| `"leaderboard"` | `"Leaderboard"` | Score leaderboard |
| `"shop"` | `"Shop"` | In-app shop / purchases |

**Total: 11 tracked screen destinations**

**Not tracked:** Overlay composables (Pause, Help, Tutorial, GameOver, Unlock, AscensionCredits, ContinueReady) — these are not `NavHost` destinations.

---

## Existing User Properties

**None.** The codebase contains zero calls to `FirebaseAnalytics.setUserProperty()` or equivalent. No user properties are recorded.

---

## Existing Firestore Usage

**None.** The codebase contains zero Firestore dependencies, imports, or calls. The project uses `SharedPreferences` as its sole persistence layer via `ProgressionManager`.

---

## Existing AdMob Tracking

| Event | Ad Type | Firebase Event | File | Line |
|-------|---------|---------------|------|------|
| `logAdImpression` | Banner | `ad_impression` (standard) | `AdComponents.kt` | 30 |
| `logAdClicked` | Banner | `ad_clicked` (custom) | `AdComponents.kt` | 33 |
| `logAdImpression` | Rewarded | `ad_impression` (standard) | `AdComponents.kt` | 61 |
| `logAdClicked` | Rewarded | `ad_clicked` (custom) | `GameOverOverlay.kt` | 207 |

Ad configuration is centralized in `AdConfig.kt` with automatic debug/release switching via `BuildConfig.DEBUG`.

**Note:** The banner `ad_impression` and `ad_clicked` events are fired via `AdListener` callbacks. The rewarded `ad_impression` is fired immediately in `RewardedAdHelper.show()` before the ad is shown (logged at call time, not on real impression callback) — this may overcount impressions.

---

## Documentation vs Reality

### Missing Documentation

| Gap | Detail |
|-----|--------|
| **No dedicated analytics document** | `docs/ANALYTICS.md` does not exist. All Firebase analytics knowledge is embedded in source code and publication reports. |
| **No event catalog** | There is no document listing all Firebase events, their parameters, and purposes. |
| **No screen tracking map** | Screen tracking is not documented anywhere; must be reverse-engineered from `NavHost` routes. |
| **No user property plan** | No documentation on whether user properties are planned or intentionally omitted. |
| **No analytics roadmap** | The EPIC tracker has no section for analytics expansion, beta analytics, or Analytics V1. |

### Outdated Documentation

| Document | Issue |
|----------|-------|
| `docs/INVENTORY.md:69-70` | Claims files are at `analytics/GameAnalytics.kt` and `analytics/FirebaseGameAnalytics.kt` — they are actually at the root package `com.ashwathai.jump_droid/GameAnalytics.kt` (single file, not two). |
| `docs/INVENTORY.md:71-73` | Claims `ads/AdConfig.kt`, `ads/GlobalAdBanner.kt`, `ads/RewardedAdHelper.kt` — these are actually at the root package: `AdConfig.kt`, `AdComponents.kt` (both banner and rewarded helper in one file). No `ads/` subdirectory exists. |

### Undocumented Code

| Code Element | Location | Issue |
|-------------|----------|-------|
| `ad_clicked` custom event | `GameAnalytics.kt:131` | Uses custom event `"ad_clicked"` while `ad_impression` uses standard `FirebaseAnalytics.Event.AD_IMPRESSION`. Inconsistency is undocumented. |
| Rewarded ad impression timing | `AdComponents.kt:61` | Impression is logged at `show()` call time, not when the ad is actually displayed. This may overcount impressions. |
| Crashlytics test crash | `PauseOverlay.kt:441` | Dev-only crash test button (`throw RuntimeException("Crashlytics Test")`) — undocumented in any analytics doc. |
| `logMissionCompleted` missing `mission_category` | `GameAnalytics.kt:72` | `mission_category` is included in `logMissionStarted` params but not in `logMissionCompleted`. This is an inconsistency. |
| `logBossDefeated` missing `boss_name` | `GameAnalytics.kt:89` | `boss_name` is included in `logBossSpawned` but not in `logBossDefeated`. |

### Duplicate Events

No duplicate Firebase events were found. Each analytics method is called from exactly one logical location in the codebase (per game loop iteration). However:

- `logGameOver` is called from 3 distinct death paths (`structural_failure`, `hull_breach`, `off_screen_fall`). These are intentional distinct failure reasons, not duplicates.
- `logAdClicked` for rewarded ads is called from both `AdComponents.kt:33` (banner) and `GameOverOverlay.kt:207` (rewarded continue) — different ad types, intentional.

### Unused Events

All 12 events defined in the `GameAnalytics` interface are implemented in `FirebaseGameAnalytics` and called from the codebase. No dead events.

---

## Intelligence Network (Separate System)

The **Intelligence Network** is an internal stats tracking system, **not** Firebase Analytics. It is included here because documentation sometimes conflates the two.

| Component | Purpose |
|-----------|---------|
| `GameStats.kt` | Data class with 19 run-time session metrics |
| `MissionManager.updateProgressAll(stats)` | Feeds GameStats into mission progress calculations |
| `ProgressionManager.commitSessionStats(stats)` | Persists cumulative lifetime stats to SharedPreferences |
| `GameEngine.getGameStats()` (line 199) | Collects session stats from engine state |

**Lifetime stats tracked internally** (SharedPreferences keys):

| Key | Type | Description |
|-----|------|-------------|
| `stat_lifetime_flight_time` | Float | Cumulative airborne time |
| `stat_lifetime_platform_time` | Float | Cumulative time on platforms |
| `stat_lifetime_bosses` | Int | Total bosses defeated |
| `stat_lifetime_hazards` | Int | Total hazard hits survived |
| `stat_lifetime_artifacts` | Int | Total artifacts collected |
| `stat_lifetime_landings` | Int | Total platform landings |
| `missions_completed` | Int | Total missions completed |

These are **not** sent to Firebase. They are used solely for mission progress calculation and achievement tracking.

---

## Recommendations (No Code Changes)

### Beta Tester Analytics

Minimum additions required:

1. **Tester user property**: Add `firebaseAnalytics.setUserProperty("beta_tester", "true")` at a single initialization point (e.g., in `MainActivity` after `FirebaseApp.initializeApp`).
2. **Tester cohort tracking**: Add two user properties for tester identification:
   - `tester_id` — anonymized device-scoped identifier (derived from `Settings.Secure.ANDROID_ID`, hashed)
   - `tester_cohort` — closed beta version (e.g., `v1.5.2`)
3. **No separate event mutation needed** — all existing events already flow to Firebase; user properties suffice for cohort filtering in the Firebase console.

### Individual Tester Tracking

Minimum additions required:

1. **Hashed device identifier**: One user property `tester_uid` set once at app init (SHA-256 of `ANDROID_ID`).
2. **Opt-in consent**: A `SharedPreferences` flag (`analytics_consent_given`) gating Firebase initialization. No analytics events should fire before explicit consent.
3. **Do not use PII**: No names, emails, or raw device IDs should be logged. Hash everything.

### Gameplay Time Tracking

The Intelligence Network already tracks `lifetimeFlightTime` in SharedPreferences, but this is **not** sent to Firebase. Minimum additions:

1. **Firebase event or user property**: Either:
   - Add a `total_play_time` parameter to the existing `logGameOver` event, or
   - Set a `lifetime_play_time_seconds` user property at game over
2. **Session count**: Add `session_count` user property incremented on each `logGameStart`.
3. **Current session duration**: The engine already has frame-time tracking via `gameTime` — plug it into `logGameOver` as a `session_duration` parameter.

### Future Analytics V1

Minimum additions to support a planned Analytics V1 expansion:

1. **Dedicated `docs/ANALYTICS.md`**: Create a living document with event catalog, parameter schemas, user properties, screen map, and governance rules.
2. **Analytics initialization gate**: Extract analytics init into a dedicated method with consent check, build-type gating (`!BuildConfig.DEBUG` or runtime flag), and clear error logging when disabled.
3. **User properties initialization**: Add a `setDefaultUserProperties()` method called once after Firebase init. Include:
   - `app_version` (from `BuildConfig.VERSION_NAME`)
   - `build_type` (`debug`/`release`)
   - `beta_tester` (when applicable)
   - `tester_cohort` (when applicable)
4. **Standardize custom event naming**: Decide convention (snake_case is current) and document it. Consider standard Firebase recommended event names where applicable.
5. **Parameter completeness audit**: Ensure symmetric parameters across start/complete events (e.g., `mission_category` in both `mission_started` and `mission_completed`; `boss_name` in both `boss_spawned` and `boss_defeated`).
6. **Rewarded ad impression fix**: Move the `logAdImpression` for rewarded ads from `show()` to the actual `onAdImpression()` callback (requires a `RewardedAd` ad listener).
7. **Screen tracking audit**: Track sub-screens beyond NavHost routes (e.g., overlays: Pause, Help, Tutorial, GameOver, Unlock, AscensionCredits).
8. **Performance monitoring**: Consider Firebase Performance Monitoring for frame rate and physics loop timing.

---

## Summary Statistics

| Category | Count |
|----------|-------|
| Firebase events (custom) | 8 |
| Firebase events (standard) | 4 |
| Total Firebase events | 12 |
| Tracked screens | 11 |
| User properties | 0 |
| Firestore collections | 0 |
| Analytics source files | 1 (`GameAnalytics.kt`) |
| Files consuming analytics | 5 (`GameEngine.kt`, `MainActivity.kt`, `AdComponents.kt`, `GameOverOverlay.kt`, `AltitudeManager.kt`) |
| Intelligence Network internal stats | 19 session metrics, 7 lifetime persisted |
| Documentation gaps found | 4 |
| Documentation inaccuracies | 1 (`INVENTORY.md` file paths) |
