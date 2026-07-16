# Analytics & Ads — Jump Droid

**Last Updated:** 2026-07-16
**Primary Doc:** `docs/ANALYTICS.md`
**Supporting Doc:** `docs/analysis/ANALYTICS_AUDIT.md`

---

## Firebase Analytics

The project uses a **domain-driven interface** pattern:

```
GameAnalytics (interface)
    └── FirebaseGameAnalytics (Firebase Analytics implementation)
```

| Component | File | Role |
|-----------|------|------|
| `GameAnalytics` interface | `GameAnalytics.kt:11-24` | 12 domain-specific analytics methods |
| `FirebaseGameAnalytics` | `GameAnalytics.kt:30-132` | Firebase implementation mapping domain events |
| `LocalAnalytics` CompositionLocal | `MainActivity.kt:15` | Provides analytics instance through Compose tree |
| Firebase init | `MainActivity.kt:23` | `FirebaseApp.initializeApp(this)` |
| Analytics instantiation | `MainActivity.kt:24` | `analytics = FirebaseGameAnalytics(this)` |

---

## Event Catalog

| Event | Type | Parameters | Where Fired |
|-------|------|------------|-------------|
| `level_start` | Standard | `rocket_type`, `rocket_title` | `GameEngine.kt:989` |
| `level_end` | Standard | `score`, `level_name`, `rocket_type`, `failure_reason` | `GameEngine.kt:343,763,981` |
| `zone_changed` | Custom | `level_name` | `GameEngine.kt:127` |
| `mission_started` | Custom | `mission_id`, `mission_type`, `mission_category` | `GameEngine.kt:147` |
| `mission_completed` | Custom | `mission_id`, `mission_type` | `GameEngine.kt:144` |
| `boss_spawned` | Custom | `boss_id`, `boss_name`, `zone` | `GameEngine.kt:948` |
| `boss_defeated` | Custom | `boss_id`, `zone` | `GameEngine.kt:133` |
| `rocket_unlocked` | Custom | `rocket_id` | `GameEngine.kt:306` |
| `module_equipped` | Custom | `module_id`, `slot_index` | `GameEngine.kt:150` |
| `screen_view` | Standard | `screen_name`, `screen_class` | `MainActivity.kt:56` |
| `ad_impression` | Standard | `ad_type`, `ad_unit_id` | `AdComponents.kt:30,61` |
| `ad_clicked` | Custom | `ad_type`, `ad_unit_id` | `AdComponents.kt:33`, `GameOverOverlay.kt:207` |

### Event Naming Convention
- Standard Firebase events use `FirebaseAnalytics.Event.*` constants.
- Custom events use `snake_case` (e.g. `"zone_changed"`, `"boss_spawned"`).
- All custom event names should follow this convention.

### Parameter Symmetry
Events that have a start/spawn pair should have symmetric parameters:
- `mission_started` includes `mission_category` — but `mission_completed` currently omits it (this is a gap).
- `boss_spawned` includes `boss_name` — but `boss_defeated` currently omits it (this is a gap).

---

## Screen Tracking

One observer in `MainActivity.kt:54-56` logs screen views on every NavHost route change:

| Route | Notable screens |
|-------|----------------|
| `title`, `main_menu`, `game`, `hangar`, `loadout`, `archive` | Full navigation destinations |
| `settings`, `about`, `missions`, `leaderboard`, `shop` | Full navigation destinations |

**Not tracked:** overlay composables (Pause, Help, Tutorial, GameOver, Unlock, AscensionCredits, ContinueReady) — these are not NavHost destinations.

---

## User Properties

**None currently set.** No `FirebaseAnalytics.setUserProperty()` calls exist. This is the primary gap for beta tester analytics.

---

## AdMob

### Configuration

All AdMob IDs are centralized in `AdConfig.kt` with automatic debug/release switching via `BuildConfig.DEBUG`:

| Build | Banner ID | Rewarded ID |
|-------|-----------|-------------|
| Debug | `ca-app-pub-3940256099942544/6300978111` (Google sample) | `ca-app-pub-3940256099942544/5224354917` (Google sample) |
| Release | `ca-app-pub-4153575596488132/3022346201` | `ca-app-pub-4153575596488132/5155087899` |

### Ad Event Tracking

| Event | Ad Type | FireSide | Where |
|-------|---------|----------|-------|
| `logAdImpression` | Banner | `adListener.onAdImpression()` callback | `AdComponents.kt:30` |
| `logAdClicked` | Banner | `adListener.onAdClicked()` callback | `AdComponents.kt:33` |
| `logAdImpression` | Rewarded | `RewardedAdHelper.show()` call time (not real impression) | `AdComponents.kt:61` |
| `logAdClicked` | Rewarded | Continue button click | `GameOverOverlay.kt:207` |

**Known issue:** Rewarded `ad_impression` is logged at `show()` call time, not on the actual ad impression callback. This may overcount impressions.

---

## Crashlytics

Firebase Crashlytics is auto-initialized via `google-services.json`. A debug-only crash test button exists in `PauseOverlay.kt:441` (gated on `BuildConfig.DEBUG`):

```kotlin
throw RuntimeException("Crashlytics Test")
```

---

## Intelligence Network (Internal Stats)

**Not Firebase Analytics.** The Intelligence Network tracks 19 session metrics for mission progress and 7 lifetime stats persisted to SharedPreferences. It feeds `MissionManager.updateProgressAll()` and `ProgressionManager.commitSessionStats()`.

| Lifetime Stat | SharedPrefs Key | Type |
|---------------|-----------------|------|
| Flight Time | `stat_lifetime_flight_time` | Float |
| Platform Time | `stat_lifetime_platform_time` | Float |
| Bosses Defeated | `stat_lifetime_bosses` | Int |
| Hazards Survived | `stat_lifetime_hazards` | Int |
| Artifacts Collected | `stat_lifetime_artifacts` | Int |
| Platform Landings | `stat_lifetime_landings` | Int |
| Missions Completed | `missions_completed` | Int |

These are **not** sent to Firebase. They are used for mission progress calculation and achievement tracking only.

---

## Governance Rules

- **No PII in Firebase Analytics**: Never log names, emails, or raw device IDs to Firebase Analytics events.
- **PII in Firestore only**: Beta tester email/name/phone stored in Firestore `testers` collection for closed beta management only.
- **No Firebase App Distribution**: Rejected as overkill for <100 testers (see `ADR_DISTRIBUTION_STRATEGY.md`).
- **Build Gating**: Analytics fire in both debug and release builds (no `BuildConfig.DEBUG` gating).

---

## Beta Analytics V0 — PlayerAnalyticsManager

Implemented via a **decorator pattern**: `PlayerAnalyticsManager` wraps `FirebaseGameAnalytics`, forwarding all events and adding Firestore-backed tester analytics on top.

### Architecture

```
GameAnalytics (interface)
    ↑
PlayerAnalyticsManager (decorator)
    ├── Forwards events → FirebaseGameAnalytics (unchanged)
    ├── Intercepts key events → local counters (SharedPreferences)
    └── Syncs to Firestore: tester profile + per-session documents
```

### Session Lifecycle (Beta V0)

One completed game (Play → Game Over/Abort) = one analytics session. This satisfies all closed beta requirements.

- **Born:** Session starts on `logGameStart()` (or on `onAppForeground()` if the app was cold-started outside a game).
- **Updated:** On `logGameOver()` — game result (score, zone, outcome, ads, continues, rockets) is accumulated in memory and persisted locally for crash resilience.
- **Synced:** A periodic timer syncs partial data to Firestore every 60s while a game is active.
- **Finalized:** Session is finalized (written as `COMPLETED` to Firestore) on:
  - **Game → Main Menu** navigation (player taps "Back to Menu" after game over or aborts mid-run)
  - **App background** (30s timeout, then finalized in background)
  - **Crash recovery** (pending session restored and finalized on next launch)
  - **Date change** (session crossing midnight is finalized)
- **Crash resilience:** Session state is persisted to SharedPreferences after every game over. On next launch, any pending session is restored and finalized.

### Production Cleanup Opportunity (~190 lines removable)
When shipping outside closed beta, the app-lifecycle infrastructure inherited from the original session model can be safely removed. The one-game=one-session model does not require it.

| Component | Lines | Reason |
|-----------|-------|--------|
| `onAppForeground()` | 38 | App-lifecycle hook — unnecessary with game-level sessions |
| `onAppBackground()` | 31 | App-lifecycle hook — unnecessary with game-level sessions |
| `persistSessionToPrefs()` | 19 | Crash resilience — only needed when session spans app background |
| `restoreSessionFromPrefs()` | 23 | Crash recovery — only needed when session spans app background |
| `accumulateAppOpenTimeSince()` | 12 | Incremental time tracking — only needed for lifecycle model |
| `formatDate()` | 4 | Date-change detection — only needed for lifecycle model |
| `start/stopPeriodicSync()` | 7 | Timer management — only needed for lifecycle model |
| Timer fields + constants | 7 | `syncHandler`, `PERIODIC_SYNC_MS`, `BG_TIMEOUT_MS`, `bgTimeoutRunnable`, `periodicSyncRunnable` |
| State flags | 8 | `appForegroundTime`, `todayDate`, `sessionGameActive`, `sessionAppOpenTimeAtBg`, `sessionPausedTime`, `lastAppOpenSync`, `syncRequired` |
| Partial-write branch in `syncSessionToFirestore()` | 16 | Only reachable via removed periodic sync — always writes all 17 fields |
| `if (BuildConfig.DEBUG)` wrappers around above | ~24 | Become dead code after method removal |
| **Total** | **~189** | Straightforward removal, zero impact on one-game=one-session metrics |

**Verification:** After removal, verify `adb logcat -s BetaAnalytics` produces zero output in release builds and the same session data in debug builds.

### Removal
Delete `PlayerAnalyticsManager.kt`, remove `firebase-firestore` dependency, revert `MainActivity.kt` lifecycle hooks.

### Migration to Analytics V2
See Migration Guide below.

### Registration Flow

On first launch, a `BetaRegistrationDialog` asks for:
- **Email** (required, validated via `Patterns.EMAIL_ADDRESS`)
- **Name** (optional)
- **Phone** (optional)

After submission, data is saved to SharedPreferences and synced to Firestore. The dialog never shows again.

### Firestore Schema

**Collection: `testers`**

Document ID: `{sanitizedEmail}` (`.` → `(dot)`, `@` → `(at)`)

| Field | Source | Type |
|-------|--------|------|
| `email` | Registration form | String |
| `name` | Registration form | String |
| `phone` | Registration form | String |
| `firstInstall` | First sync | Timestamp |
| `lastSeen` | Updated on each app background | Timestamp |
| `versionName` | `BuildConfig.VERSION_NAME` | String |
| `versionCode` | `BuildConfig.VERSION_CODE` | Int |
| `totalAppOpenTime` | Lifecycle timer | Float (seconds) |
| `todayAppOpenTime` | Lifecycle timer (resets daily) | Float (seconds) |
| `totalGameplayTime` | Intelligence Network `stat_lifetime_flight_time` | Float (seconds) |
| `todayGameplayTime` | Game start/stop timer (resets daily) | Float (seconds) |
| `totalSessions` | Count of `logGameStart()` calls | Int |
| `highestScore` | Intelligence Network `highScore` | Int |
| `rewardAdsWatched` | Count of `logAdImpression("rewarded")` | Int |
| `bannerImpressions` | Count of `logAdImpression("banner")` | Int |
| `continuesUsed` | Count of `logAdClicked("rewarded")` | Int |

**Subcollection: `sessions`**

Document ID: `{yyyyMMdd_HHmmss_SSS}`

| Field | Source | Type |
|-------|--------|------|
| `sessionStart` | `logGameStart()` time | Timestamp |
| `sessionEnd` | `logGameOver()` time | Timestamp |
| `gameplayTime` | Wall-clock between start and end | Float (seconds) |
| `score` | Game over score | Int |
| `zone` | Current `AltitudeZone.zoneName` | String |
| `rocketType` | `RocketType.name` | String |
| `outcome` | Game over reason | String |

### File

| File | Role |
|------|------|
| `app/src/main/java/com/ashwathai/jump_droid/PlayerAnalyticsManager.kt` | Decorator implementation — Firestore sync, local counters, registration |
| `MainActivity.kt` | Integration: lifecycle hooks, registration dialog |
| `gradle/libs.versions.toml` | Added `firebase-firestore` library |
| `app/build.gradle.kts` | Added `implentation(libs.firebase.firestore)` |

---

## Migration Guide: Beta V0 → Analytics V2

After Closed Testing ends, evolve `PlayerAnalyticsManager` into a full Analytics V2 system:

### Step 1 — Promote to `players` collection
- Rename `testers` → `players` in Firestore.
- The document schema is sound; add GDPR consent fields (`consent_granted`, `consent_date`).
- No code changes needed in `PlayerAnalyticsManager` — just change the collection name string.

### Step 2 — Add Firebase user properties
- In `PlayerAnalyticsManager`, call `firebaseAnalytics.setUserProperty()` on registration sync:
  - `player_id` (hashed email)
  - `total_sessions` (already tracked)
  - `tester_cohort` (version name)
- These are **not PII** — they are derivative identifiers.

### Step 3 — Promote local counters to real-time events
- Replace local SharedPreferences counters with Firebase Analytics event parameters where appropriate.
- Example: `bannerImpressions` can remain a Firestore field but also emit a `banner_impression_count` parameter on `ad_impression` events.

### Step 4 — Add privacy consent dialog
- Replace the simple `BetaRegistrationDialog` with a GDPR-compliant consent dialog.
- Gate Firestore sync and Firebase Analytics init on `consent_granted`.
- Store consent proof in SharedPreferences with a timestamp.

### Step 5 — Sessions → BigQuery
- For production scale, export the `sessions` subcollection to BigQuery via Firebase Extensions.
- Remove per-session Firestore writes or reduce to sampling.

### Step 6 — Remove migration path
- After V2 is stable, delete `PlayerAnalyticsManager.kt` and move its evolved logic into a new `PlayerAnalyticsService.kt`.
- Remove the `firebase-firestore` dependency if Firestore is no longer needed (migrated to BigQuery).

---

## Planned Additions (Analytics V2+)

See `docs/analysis/ANALYTICS_AUDIT.md` → Recommendations section for full details. Future additions include:

1. Hashed device identifier for individual tester tracking
2. Parameter symmetry fixes (`mission_category` in `mission_completed`, `boss_name` in `boss_defeated`)
3. Rewarded ad impression timing fix (move to real ad callback)
4. Overlay screen tracking
5. Performance monitoring (Firebase Performance Monitoring)
