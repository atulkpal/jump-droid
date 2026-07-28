# Jump Droid — Analytics Reference Guide

**Version:** v2.0.0 — Production Ascension
**Last Updated:** 2026-07-28

---

## 1. Overview
Jump Droid uses **Firebase Analytics** for telemetry and **Firebase Crashlytics** for error monitoring. The system follows a domain-driven interface pattern to keep game logic decoupled from the specific analytics provider.

### Core Architecture
- **`GameAnalytics` (interface)**: Defines domain events (e.g., `logMissionStarted`).
- **`FirebaseGameAnalytics`**: Maps these domain events to Firebase `logEvent` calls.
- **`LocalAnalytics`**: A Compose `CompositionLocal` providing access to the analytics instance across the UI tree.

---

## 2. Event Catalog

| Event | Type | Parameters | Purpose |
|-------|------|------------|---------|
| `level_start` | Standard | `rocket_type`, `rocket_title` | Tracks when a flight begins. |
| `level_end` | Standard | `score`, `level_name`, `rocket_type`, `failure_reason` | Tracks game over state and reasons. |
| `zone_changed` | Custom | `level_name` | Tracks transitions between atmospheric layers. |
| `mission_started`| Custom | `mission_id`, `mission_type`, `mission_category` | Tracks when a new mission is selected. |
| `mission_completed`| Custom | `mission_id`, `mission_type`, `mission_category` | Tracks successful mission completion. |
| `boss_spawned` | Custom | `boss_id`, `boss_name`, `zone` | Tracks boss encounter frequency. |
| `boss_defeated` | Custom | `boss_id`, `boss_name`, `zone` | Tracks boss difficulty/success rates. |
| `rocket_unlocked` | Custom | `rocket_id` | Tracks progression and player retention. |
| `module_equipped` | Custom | `module_id`, `slot_index` | Tracks popular module loadouts. |
| `screen_view` | Standard | `screen_name`, `screen_class` | Tracks navigation between app screens. |
| `ad_impression` | Standard | `ad_type`, `ad_unit_id` | Tracks successful ad displays. |
| `ad_clicked` | Custom | `ad_type`, `ad_unit_id` | Tracks ad engagement rates. |

---

## 3. Screen Tracking
Screen views are automatically tracked via the `NavHost` backstack observer in `MainActivity.kt`.

**Tracked Destinations:**
- `title`, `main_menu`, `game`, `hangar`, `loadout`, `archive`, `settings`, `about`, `missions`, `leaderboard`, `shop`.

---

## 4. AdMob Integration
Ad IDs are managed in `AdConfig.kt`.
- **Debug Builds**: Uses Google sample test IDs.
- **Release Builds**: Uses production IDs.

**Impression Logging:**
- **Banner Ads**: Logged via `onAdImpression` callback.
- **Rewarded Ads**: Logged via `fullScreenContentCallback.onAdImpression` to ensure accuracy.

---

## 5. Intelligence Network (Internal Stats)
Separate from Firebase, the app maintains a **local** stat tracking system (`StatRecorder.kt`) for mission progress and in-game achievements.

**Lifetime Stats (SharedPreferences):**
- `stat_lifetime_flight_time`
- `stat_lifetime_platform_time`
- `stat_lifetime_bosses`
- `stat_lifetime_hazards`
- `stat_lifetime_artifacts`
- `stat_lifetime_landings`
- `missions_completed`

---

## 6. Governance & Privacy
- **No PII**: No emails, names, or raw device IDs are sent to Firebase.
- **Build Gating**: Critical debug logs are wrapped in `if (BuildConfig.DEBUG)`.
- **Optimization**: R8 minification is enabled for release builds to protect telemetry hooks.
