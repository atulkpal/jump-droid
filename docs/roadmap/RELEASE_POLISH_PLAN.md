# Jump Droid — Release Polish Plan

**Status:** **ALL PHASES COMPLETE** — v1.5.0 Released ✅
**Branch:** `master` (stable)
**Target:** Play Store Release (Complete)

---

## Overview

19 work items (Original 18 + Haptics) sequenced into 6 phases. Each phase is independent where possible; dependencies are noted. No phase introduces new gameplay features — only polish, UX improvement, and production readiness.

### Priority Order vs Dependencies

```
Phase 1 ──► Phase 2 ──► Phase 3 ──► Phase 4 ──► Phase 6 ──► Phase 7
                  │                      │
                  │                      ├──► Phase 5 (parallel)
                  └──► Phase 3 depends on Phase 2 notifications
```

### Key Design Decisions (from expert review)

1. **Power-up Dead Zones** — Spawns must avoid overlapping boss hitboxes, hazard radii, and off-screen areas. See Phase 1.
2. **Priority Tier System** — NotificationManager needs a 3-tier priority enum to prevent clutter from hiding critical warnings. See Phase 2.
3. **Audio Ducking** — Boss death sequences duck BGM + thrust to let explosion SFX dominate. See Phase 5.
4. **Monetization Early** — Moved from Phase 6 to Phase 4. Ad SDKs introduce lifecycle bugs; earlier integration gives more buffer before release.
5. **Banner Layout First** — Ad placeholder composable built in Phase 2 so UI spacing is correct before SDK integration. No layout breakage risk.
6. **Continue UX in Phase 1** — Rewarded continue button built during game feel pass; SDK callback swaps in during Phase 4 without touching engine.
7. **Premium Purchase = Hiding Placeholders** — Purchase sets `isPremiumUser` flag which hides existing `GlobalAdBanner` composables. No layout changes needed post-purchase.
8. **Haptic Feedback** — Tactical vibrations for impact and alerts. See Phase 5.

---

## Phase 1 — Core Game Feel (Items 2, 3, 4, 5, 16)

**Status: Complete**

 # | Item | Status | Effort | Risk |
---|------|--------|--------|------|
 2 | Power-up spawn redesign | **Complete** | Medium | Low |
 3 | Combo reward animation | **Complete** | Medium | Low |
 4 | Boss death sequence | **Complete** | Medium | Low |
 5 | Mission completion celebration | **Complete** | Small | Low |
 16 | Rewarded continue UX | **Complete** | Small | Low |

### 2. Power-up Spawn System

- Remove falling-drop architecture (`PowerUp.vy` descent)
- Spawn at random visible locations on screen
- **Dead zone avoidance**: never spawn inside a boss collision box, hazard radius (HAZ_LIGHTNING, HAZ_DEBRIS, etc.), or behind a wall of breakable platforms
- **Natural encounter**: bias spawns slightly above camera top so the player naturally flies into them during ascent
- Add glow effect (pulsing alpha/size, SciFi colors per type)
- Add despawn timer (~8s visible, then fade out)
- Increase glow pulse speed as despawn approaches
- Keep existing PowerUpType enum, pickup collision logic

### 3. Combo Rewards

- Rewards originate from the combo ring position (top-left)
- Animate reward travelling toward the player position
- Remove `FlyingReward` falling animation
- Add "COMBO REWARD" floating text near the ring
- Keep existing `ComboManager.pendingReward` → `applyReward()` flow

### 4. Boss Death Sequence

- On boss defeat (`health <= 0`), stop upward movement
- Replace `setState(DEAD)` fly-away with piece-by-piece destruction
- Spawn explosion bursts progressively over ~1.5s
- Spawn debris particles (multiple colors per boss type)
- Final large explosion + screen shake
- Keep existing onDefeat rewards/notifications

### 5. Mission Completion Celebration

- Target position: near the score display area (top-center)
- Burst + floating text + screen flash
- Keep existing notification (`notificationManager.showImmediately`)
- Remove duplicate celebration sources if any
- Coordinate with Phase 2 notification audit

### 16. Rewarded Continue UX

- Add `[AD]` iconography to the "RE-ESTABLISH LINK" button in `GameOverOverlay`
- Implement strict one-time-per-run continue logic (once used, button hides or grays out for that run)
- Continue callback already exists in `GameEngine.continueRun()` — no engine changes needed
- The actual ad SDK callback swaps in during Phase 4; for now the button works as "free continue" for testing
- Ensure the continue option does not appear if player has already continued this run

### Phase 1 Implementation Notes

- **Power-up spawn redesign** (`PowerUpManager.kt`): Removed falling-drop `PowerUp.vy` descent. Power-ups now hover at random visible locations with dead-zone avoidance (avoiding boss hitboxes, hazard radii, off-screen). Added 8s despawn timer with accelerating glow pulse fade-out. `Models.kt` PowerUp class gained `despawnTimer` and `glowPulseSpeed` fields. `CanvasEffects.kt` `drawPowerUps()` updated for pulsing glow per type.
- **Combo reward animation** (`GameEngine.kt`, `ComboManager.kt`): Rewards originate from top-left combo ring position, animate flying toward player. "COMBO REWARD" floating text appears near the ring. Removed `FlyingReward` falling animation.
- **Boss death sequence** (`ThreatInteractionProcessor.kt`): On boss defeat, movement stops immediately. Progressive 1.5s piece-by-piece destruction with color-mapped debris per boss type. Final large explosion + screen shake. `ActiveThreat.kt` added `destructionTimer`. `ThreatAIUpdater.kt` updated all 11 boss types to stop movement on defeat.
- **Mission completion celebration** (`GameEngine.kt`): Triple burst (Gold + Cyan) at top-center, floating text "MISSION COMPLETE", screen flash + shake.
- **Rewarded continue** (`GameOverOverlay.kt`): `[AD]` badge (SciFiGold) added to "RE-ESTABLISH LINK" button. One-time-per-run enforcement.

---

## Phase 2 — Notification Architecture (Items 8, 9, 10, 11, 17)

**Status: Complete**

 # | Item | Status | Effort | Risk |
---|------|--------|--------|------|
 8 | Notification system audit | **Complete** | Medium | High |
 9 | Unified notification area | **Complete** | Medium | High |
 10 | Improve gameplay notifications | **Complete** | Medium | Medium |
 11 | Remove weak notifications | **Complete** | Small | Low |
 17 | Banner ad placeholder UI | **Complete** | Small | Medium |

### 8. Notification System Audit

- Trace all `notificationManager.post()` / `.showImmediately()` call sites
- Identify duplicates, conflicts, and timing issues
- Document notification source → trigger → priority mapping
- Current known issue: `NotificationManager` lacks dedup/priority

#### Priority Tier Implementation

Implement a 3-tier priority enum in `NotificationManager`:

 Tier | Name | Display Rule | Color | Examples |
------|------|-------------|-------|----------|
 1 | CRITICAL | Always visible, shakes HUD, persists until clear | Red | Hull failure, boss arrival |
 2 | TACTICAL | Standard display, replaced by CRITICAL | Standard UI | Low fuel, discovery, mission update |
 3 | FLAVOR | Fades quickly, lowest display priority | Dimmed | Zone change, lore snippet |

Rules:
- A CRITICAL notification preempts and replaces any TACTICAL/FLAVOR in the queue
- TACTICAL preempts FLAVOR
- When a higher-tier notification clears, lower-tier items resume display
- All existing `showImmediately()` call sites get a tier assigned during the audit

### 9. Unified Notification Area

- Create a dedicated render area below the score
- Route gameplay notifications through this single location
- Retain world-space notifications for key events (boss taunts, Priest events)
- Use queue + display window (e.g. show 3 at a time, oldest expires)

### 10. Improve Notifications

Review and redesign for each category:

 Category | Action |
----------|--------|
 Boss approaching / danger | Keep, make prominent |
 Survey drone found | Keep, add to archives unlock |
 Enemy discovered | Move to archive discovery |
 Power-up collected | Already removed for basic items (EPIC 8 polish) |
 Hull/Shield/Fuel/Heat events | Keep critical only |
 Resource critical warnings | Keep, audio alarm support |
 Gate notifications | Review existing behavior |
 Mission updates | Keep, route to celebration area (Phase 1) |
 Unlock celebrations | Keep, route to celebration area |

### 11. Remove Weak Notifications

- Swarm notification: evaluate value, remove if clutter
- Review all for "does the player need to know this right now?"
- Eliminate notifications that add noise without actionable info

### 17. Banner Ad Placeholder UI

Reserve layout space on non-gameplay screens so banner ads never overlap buttons.

**Screens requiring banner space:**
- **Title Screen** — Reserve bottom 50dp–60dp. Ensure "INITIATE ASCENT" button is above the banner zone.
- **Data Archives** — Grid/list layout stops above banner zone to prevent accidental clicks while scrolling.
- **Settings / Hangar** — Create reserved space (top or bottom) that doesn't overlap rocket stats.

**Implementation:**
- Create a `GlobalAdBanner` composable in a shared UI component file
- During development (Phases 2–3): renders as a dark gray placeholder box with `"AD PLACEHOLDER"` text
- At SDK integration (Phase 4): swap the placeholder for real `AdView` from AdMob/AppLovin
- The `isPremiumUser` flag (Phase 4) hides the composable entirely
- **No banner on GamePlayScreen** — 100% full-screen to maintain immersion and prevent input issues during flight
- Adjust Compose `Scaffold` / `Column` layouts on each affected screen to embed the banner composable at the reserved position

### Phase 2 Implementation Notes

- **Notification priority system** (`NotificationManager.kt`): Full rewrite with 3-tier `NotificationPriority` enum (CRITICAL / TACTICAL / FLAVOR). Dedup by message content. Priority preemption (CRITICAL bumps all, TACTICAL bumps FLAVOR). `NotificationEntry` data class with priority, timestamp, message fields.
- **Unified notification area** (`HudWidgets.kt`): Stacks up to 3 entries. Priority-based text sizing and coloring (CRITICAL = SciFiRed bold, TACTICAL = standard, FLAVOR = dimmed). Positioned at top=180dp. World-space notifications retained for boss taunts and Priest events.
- **Notification priority assignments**: All call sites updated — CRITICAL for shield/hull critical (`SurvivalManager`) and boss defeat; TACTICAL for mission complete and zone messages (`EncounterDirector`); FLAVOR for archive entries and artifact discoveries (`checkDiscovery`).
- **Removed weak notifications**: Eliminated "REINFORCEMENTS INBOUND" from `EncounterDirector`. Reviewed all other notifications for actionable value.
- **GlobalAdBanner** (`AdComponents.kt`): New composable, 56dp placeholder box with `"AD PLACEHOLDER"` text. Hidden when `isPremiumUser = true`. Added to all menu screens (Title, MainMenu, Hangar, Loadout, Mission, Archive, Settings, About, Leaderboard) and overlays (Pause, GameOver, Help, Tutorial, Unlock). **No banner on GamePlayScreen** — full-screen immersion preserved.

---

## Phase 3 — Tutorial Removal + Discovery (Items 1, 7, 12, 13)

**Status: Complete**

 # | Item | Status | Effort | Risk |
---|------|--------|--------|------|
 1 | Remove tutorial pop-ups | **Complete** | Small | Low |
 7 | Move learning to archives | **Complete** | Medium | Low |
 12 | Platform discovery messages | **Complete** | Small | Low |
 13 | Unlock celebration | **Complete** | Small | Low |

### 1. Remove Tutorial Pop-ups

- Remove all `showTutorialPopup()` / `tutorialStep` logic
- Replace with non-blocking discovery: small notification + archive entry
- Players learn naturally while playing (see items 7, 12, 13)

### 7. Move Learning to Archives

- Pull educational text out of tutorial system
- Add entries to Data Archives (Phase 4) for each concept
- Discovery notifications point to archives: "New entry unlocked — check Archives"

### 12. Platform Discovery Messages

- On first landing on any platform type:
  - Show floating text with platform name
  - Show one-line gameplay hint
  - Examples:
    - **Magnetic Platform** — "Pulls nearby rockets"
    - **Breakable Platform** — "Collapses after landing"
    - **Mimic Platform** — "Not what it seems"
- Coordinate with Phase 2 notification area

### 13. Unlock Celebration

- Any new unlock (archive entry, module, rocket, artifact)
- Display notification + celebration burst
- Encourage player to explore archives
- Audio: `fanfare_unlock`

### Phase 3 Implementation Notes

- `checkDiscovery()`: Removed `forceTutorialState` parameter and `GameState.TUTORIAL` transition. Now posts FLAVOR notification "New Archive Entry — {title}" + spawns purple burst for artifacts.
- `handleLanding()`: All 14 platform types show one-time gameplay hint float text on first discovery (BOOST, ICE, MOVING, MAGNETIC, PHASE, BREAKABLE, CONVEYOR, STANDARD, FUEL, COOLING, STABILITY, MIMIC, FLUX, GRAVITON). FLUX_PLATFORM and GRAVITON_PLATFORM DiscoveryType entries added to Models.kt.
- `checkUnlock()`: Achievement unlocks now spawn gold burst via `spawnBurst`.
- **Archive badge**: MainMenuScreen shows SciFiPurple dot on ARCHIVE button when `engine.codexNotification != null`. Cleared on navigate to Archive.
- **Haptics activation**: `HapticManager` instantiated in `GameEngine`, `onVibrate` wired to both `SurvivalManager.applyDamage()` and `SurvivalManager.update()`.

---

## Phase 4 — Data Archives + Monetization (Items 6, 18)

**Status: Complete**

 # | Item | Status | Effort | Risk |
---|------|--------|--------|------|
 6 | Redesign Data Archives | **Complete** | Large | Medium |
 18 | Premium purchase + SDK integration | **Wired (Test Ads)** | Medium | High |

### 6. Data Archives Redesign

- Keep existing `ArchiveEntry`, `ArchiveCategory` data model
- Improve UI: categorized grid/list layout
- Categories:
  - **Enemies** — threat types, stats, lore
  - **Bosses** — boss entries, strategies
  - **Hazards** — environmental threats
  - **Platforms** — all 11 types
  - **Zones** — altitude regions
  - **Power-ups** — all pickup types
  - **Modules** — equipped modules
  - **Lore** — discovered story entries
- Locked entries: hidden with "???" label, unlock on first encounter
- Unlocked entries: show name, description, gameplay info, art
- Archive unlock triggers notification (Phase 3)

### 18. Premium Purchase + SDK Integration

#### Banner Ad Activation

- Connect `GlobalAdBanner` placeholders (Phase 2) to real AdMob/AppLovin `AdView`
- Initialize ad SDK in `Application.onCreate()` or first visible Activity
- Verify no ads appear during `GamePlayScreen` — only Title Screen, Archives, Settings

#### Rewarded Continue

- Wire the "RE-ESTABLISH LINK" button (Phase 1) to a rewarded ad callback
- On ad completion: grant 50% health restore and allow continue
- One-time per run (enforced by Phase 1 logic)
- On ad failure or skip: show "Ad not available" toast, do not continue

#### Premium Purchase ($2.99 — Remove Ads)

- Add a "Remove Ads" button to `SettingsScreen`
- Implement `isPremiumUser` flag stored in `SharedPreferences`
- Purchase uses Google Play Billing library
- On purchase confirmation:
  - Set `isPremiumUser = true`
  - Hide all `GlobalAdBanner` composables
  - Hide `[AD]` iconography on the continue button
  - Persist flag across reinstalls (tied to Google Play account)
- Restore purchases on app launch for existing premium users

#### Testing Checklist

- [x] Ad SDK initialization does not delay app startup
- [x] Banner ads render correctly on Title Screen, Archives, Settings
- [x] No banner on GamePlayScreen or GameOverOverlay (except continue button)
- [x] Rewarded continue grants health only once per run
- [x] Rewarded continue does not appear if already used this run
- [x] Premium purchase hides all banners immediately
- [x] Premium purchase hides `[AD]` icon on continue button
- [ ] Restore purchases works after reinstall *(requires Play Store)*
- [ ] No lifecycle crashes on Activity rotation / minimize-resume

### Phase 4 Implementation Notes

- **Archives redesign** (`ArchiveScreen.kt`): Re-organized into 12 categories splitting THREATS into BOSSES (THREAT_*), ENEMIES (ENEMY_*), and HAZARDS (HAZARD_*). Renamed AREAS → ZONES, ROCKETS → MODULES. Added MECHANICS category. Category accent colors per type. New `ArchiveCard` composable with locked/unlocked visual states, colored dot indicator, italic locked descriptions.
- **AdMob SDK integration** (`build.gradle.kts`, `AndroidManifest.xml`): Added `play-services-ads:23.6.0` dependency, `INTERNET` permission, test AdMob app ID. `MainActivity.onCreate()` initializes `MobileAds`.
- **GlobalAdBanner rewrite** (`AdComponents.kt`): Replaced dark placeholder with real `AdView` rendering Google banner ad (`ca-app-pub-4153575596488132/3022346201`). Reads `isPremiumUser` from SharedPreferences internally — no parameter wiring needed.
- **Rewarded continue** (`AdComponents.kt`, `GameOverOverlay.kt`): `RewardedAdHelper` object preloads `RewardedAd` (`ca-app-pub-4153575596488132/5155087899`) when overlay appears. Continue button shows ad on click; falls back to free continue on failure.
- **Premium purchase** (`PurchaseManager.kt`, `SettingsScreen.kt`): `PurchaseManager` rewritten with full `BillingClient` implementation — `setListener`, `queryPurchasesAsync`, `queryProductDetailsAsync`, `launchBillingFlow`, `acknowledgePurchase`. Fallback confirmation dialog when Play Store unavailable (sets `isPremiumUser` flag directly). SettingsScreen shows "ADS REMOVED ✓" disabled button when premium, "UPGRADE: REMOVE ADS ($1.99)" otherwise with purchase flow + fallback dialog. No toggle-off.
- **ShopScreen** (`ShopScreen.kt`): New currency exchange composable with cash balance display, premium purchase card ($1.99, "ADS REMOVED ✓" when purchased), V2 placeholder items (Rocket Skins, Engine Trails, UI Themes — greyed out with "V2" badge). Route added to MainActivity. `GameState.SHOP` added to enum. SHOP button in MainMenuScreen.
- **ProgressionManager cash tracking**: `totalCash` field persisted in SharedPreferences (`getInt("total_cash", 0)`). `grantReward(Cash)` now accumulates (`totalCash += reward.amount`). `wipeData()` resets to 0. `getCashBalance()` returns current total.
- **Archive badge** (Phase 3 carryover): `MainMenuScreen` shows SciFiPurple dot on ARCHIVE button when `engine.codexNotification != null`. Cleared on navigate to Archive.

---

## Phase 5 — Audio Pass & Haptics (Items 14, 19)

 # | Item | Status | Effort | Risk |
---|------|--------|--------|------|
 14 | Audio pass | **Production Assets Loaded** | Large | Low |
 19 | Haptic feedback | **Wiring Complete — GameEngine Activated** | Small | Low |

### Implementation Strategy

**Audio:**
- **Full Production Asset Swap**: All 46+ procedural tones replaced with high-quality `.ogg` assets in `res/raw`.
- **Menu BGM**: Added `bgm_menu` support for all non-gameplay screens (Title, Hangar, Archive, etc.).
- **Impact Randomization**: `sfx_impact_small` now dynamically alternates between version `_1` and `_2` to prevent audio fatigue.
- **Thrust Loop**: Switched from white-noise thread to native `SoundPool` looping for `sfx_thrust_loop.ogg`.
- **12-Zone Dynamic Music**: Full mapping for all 12 zones from `bgm_earth` to `bgm_singularity`.
- **Multi-material Landing SFX**: Mapped real assets for ICE, ENERGY, GRAVITY, FRAGILE, and UTILITY types.
- **Audio Ducking**: Engine ducks BGM by 50% during boss death sequences to emphasize explosions.

**Haptics:**
- `HapticManager.kt` created for pattern management.
- `android.permission.VIBRATE` added to manifest.
- Settings toggle (HAPTIC ON/OFF) added to `SettingsScreen`.
- Callbacks established in `SurvivalManager` and `ThreatInteractionProcessor`.
- **GameEngine activation**: `HapticManager` instantiated in `GameEngine` constructor. `onVibrate` callback wired to both `SurvivalManager.applyDamage()` and `SurvivalManager.update()`. All haptic triggers (IMPACT_MEDIUM, IMPACT_HEAVY, EXPLOSION, WARNING, SUCCESS, IMPACT_LIGHT) now propagate through the survival and threat systems.

### Haptics Technical Documentation

The haptic system uses a **callback pattern** to decouple physics/survival logic from the Android `Vibrator` service.

#### 1. Patterns (`HapticManager.HapticType`)
- `IMPACT_LIGHT`: Subtle tap (e.g. UI ticks, subtle pulses).
- `IMPACT_MEDIUM`: Noticeable impact (e.g. Shield hits, hazard proximity).
- `IMPACT_HEAVY`: Strong jolt (e.g. Hull integrity damage).
- `SUCCESS`: Double pulse (e.g. Weakpoint destroyed, mission complete).
- `WARNING`: Pulsing sequence (e.g. Critical fuel, overheating).
- `EXPLOSION`: Sustained vibration (e.g. Boss defeat, droid destruction).

#### 2. Wiring Logic
Logic systems (like `SurvivalManager`) do not reference `HapticManager` directly. Instead, they accept a lambda:
`onVibrate: (HapticManager.HapticType) -> Unit = {}`

**Trigger Points:**
- **Hull/Shield Hit**: Triggered inside `SurvivalManager.applyDamage`.
- **Boss Defeat**: Triggered inside `ThreatInteractionProcessor` when boss health reaches 0.
- **Critical Alerts**: Triggered in `SurvivalManager.update` based on resource thresholds.

#### 3. Activation
To activate haptics project-wide, `GameEngine.kt` needs to initialize the manager and pass the `vibrate` call into the wired manager callbacks.

### Sound Asset Specification

#### Player Movement & Status

 Asset ID | Description | Context / Trigger | Notes |
----------|-------------|-------------------|-------|
 `sfx_thrust_loop` | Sustained engine rumble | While thrusting (screen held) | Loop, stop on release |
 `sfx_land_metal` | Heavy industrial thud | Landing on standard platforms | Already hooked via handleLanding |
 `sfx_land_ice` | Glassy clink / slide | Landing on ICE platforms | New mapping |
 `sfx_land_energy` | Electronic zip / electronic whir | Landing on BOOST, PHASE, FLUX | New mapping |
 `sfx_land_gravity`| Resonant low hum | Landing on MAGNETIC, GRAVITON | New mapping |
 `sfx_land_utility`| Pneumatic hiss / vent | Landing on FUEL, COOLING, STABILITY | New mapping |
 `sfx_land_fragile`| Structural snapping / crack | Landing on BREAKABLE, MIMIC | New mapping |
 `sfx_overheat_alarm` | High-pitch warning tone | `isOverheated == true` | Loop, stop on cooldown |
 `sfx_cooling_vent` | Pressure hiss | Engine cooling / COOLING_PLATFORM | One-shot |
 `sfx_alarm_low_fuel` | Rhythmic beep | Fuel < 20% | Loop, stop on refuel |
 `sfx_alarm_critical` | Heavy klaxon | Hull integrity < 15% | Loop, stop on repair |

#### Combat & Interaction

 Asset ID | Description | Context / Trigger | Notes |
----------|-------------|-------------------|-------|
 `sfx_hit_hull` | Metal crunch / strain | Damage without shields | `SurvivalManager` hull branch |
 `sfx_hit_shield` | Energy ripple / zap | Damage with shields active | `SurvivalManager` shield branch |
 `sfx_collect_item` | Ascending digital chime | Collecting any power-up | PowerUp pickup handler |
 `sfx_projectile_fire` | Laser pulse / thump | Enemy fires projectile | `ProjectileManager.spawn()` threat branch |
 `sfx_impact_small` | Pop / glass break | Projectile hits platform | Projectile collision |
 `sfx_explosion_enemy` | Quick explosion | Standard threat destroyed | `ActiveThreat` death |
 `sfx_boss_weakpoint` | Shattering glass + boom | Boss weak point destroyed | Boss-specific |
 `sfx_boss_defeat` | Layered cinematic explosion | Total boss defeat | Boss death sequence (Phase 1) |

#### Environment & Hazards (new calls needed for most)

 Asset ID | Description | Context / Trigger | Notes |
----------|-------------|-------------------|-------|
 `sfx_hazard_lightning` | Electrical crackle / thunder | HAZ_LIGHTNING strike | `ThreatInteractionProcessor` |
 `sfx_hazard_emp` | Low-frequency distortion | EMP pulse expansion | Same |
 `sfx_hazard_void` | Deep spatial drone | Near Void Anomaly | Distance-based volume |
 `sfx_hazard_wind` | Rushing air / whistling | In Turbulence Front | Continuous while in zone |
 `sfx_hazard_radiation` | Geiger static / hiss | In Radiation Zone | Distance-based volume |

#### Progression & Meta

 Asset ID | Description | Context / Trigger | Notes |
----------|-------------|-------------------|-------|
 `sfx_ui_click` | Crisp "pip" | Button press / menu nav | New hook in GameScreen UI |
 `sfx_ui_confirm` | Positive chime | Selecting option / start game | Same |
 `sfx_ui_back` | Lower muted click | Back / Cancel | Same |
 `sfx_fanfare_mission` | 2s heroic jingle | Mission complete | Phase 1 item 5 |
 `sfx_fanfare_unlock" | Triumphant startup | Unlocking rocket/blueprint | Phase 3 item 13 |
 `sfx_data_scan` | Sci-fi scanning | Lore / Artifact discovery | Archive unlock |

#### Music (BGM) — Dynamic Zone Music

 Asset ID | Zone Context | Notes |
----------|-------------|-------|
 `bgm_earth` | Ambient hopeful, 0–500m | Crossfade on zone transition |
 `bgm_clouds` | Airy ethereal, 500m–1.5km | |
 `bgm_atmosphere`| Empty lonely, 1.5km–4km | |
 `bgm_orbit` | Grand cinematic, 4km–6km | |
 `bgm_foundry` | Industrial rhythmic, 6km–8km | |
 `bgm_space` | Silent echoing, 8km–13km | |
 `bgm_chrono` | Glitchy unstable, 13km–15km | |
 `bgm_void` | Distorted dark, 15km–25km | |
 `bgm_beyond` | Surreal melodic, 25km–45km | |
 `bgm_gate` | Monolithic humming, 45km–70km | |
 `bgm_construct` | Ancient mechanical, 70km–100km | |
 `bgm_singularity`| Intense chaotic, 100km+ | |
 `bgm_boss` | High-tempo orchestral | Any boss encounter |

### Audio Implementation Notes

- **`thrust_loop`**: Use `SoundPool` loop mode, stop on `releaseThrust()` / game over
- **`alarm_low_fuel` / `alarm_critical`**: Loop, stop when condition clears; avoid stacking
- **`hazard_*`**: Spatial-ish — vary volume by distance from hazard center
- **BGM**: One active track at a time, crossfade between zone changes
- **SoundPool limits**: ~16 simultaneous streams; prioritize combat/UI over ambient
- **Existing `playSfx(id)` method is ready** — drop in `.ogg` files, call by string key
- **Audio formats**: Use `.ogg` for loops (thrust, BGM, alarms), `.wav` or `.mp3` for one-shot (SFX, impacts, UI clicks)
- **Audio ducking**: When a boss death or critical event triggers, duck (lower) BGM + thrust volume by ~50% for 1.5s to let the explosion SFX dominate the mix

---

## Phase 6 — Archive Entity Detail Popups (Item 20)

| # | Item | Status | Effort | Risk |
|---|------|--------|--------|------|
| 20 | Entity detail popup + preview renders | **Complete** | Large | Medium |

### Sub-tasks

- [x] Clickable archive cards open detail popup
- [x] Full animated entity render for threats (40+ existing renderers)
- [x] Full animated entity render for platforms (PlatformRenderer) and power-ups (drawPowerUps)
- [x] Simple shape renders for zones, modules, lore, mechanics, artifacts, logs, achievements
- [x] Locked entries show glitch preview + "NO SIGNAL" placeholders
- [x] Unlocked entries show per-section text (STATUS / CLASSIFICATION / ARCHIVE RECORD / TACTICAL NOTE)
- [x] EntityDetailRegistry data class + reverse mapping

### Phase 6 Implementation Notes

- **EntityDetailRegistry.kt**: Data class with `status`, `classification`, `archiveRecord`, and `tacticalNote`. Full registry populated for all `DiscoveryType` entries, including new `LOG_GENERIC` and `ACHIEVEMENT_GENERIC` types.
- **EntityPreview.kt**: Renders live animated previews for all entities. Threats use their actual combat renderers; platforms and power-ups use their world renderers. Fallback geometric shapes provided for meta-categories (Zones, Modules, Lore, etc.).
- **EntityDetailPopup.kt**: Implemented with 4-section layout. Features real-time character-level glitching for locked entries using `Random` seeded by time.
- **ArchiveScreen.kt Integration**: All categories (Logs and Achievements included) now support detail popups on click.

---

## Phase 7 — Release Preparation (Item 15)

**Status: Complete** — v1.5.0 Released ✅

 # | Item | Effort | Risk |
---|------|--------|------|
 15 | Release preparation | Large | Medium |

### Sub-tasks

- [x] Bug bash: full playthrough of all zones, threats, bosses
  - [x] Player hitbox collision radius added (28f) — full 40×70 rocket counts
  - [x] WP tracking fixed per-WP `wpDestroyedMask` — no more index-ordering bug
  - [x] WP hitbox positions aligned to rendered visuals (11 bosses)
  - [x] Alarm loop fix (all 3 death paths: applyDamage, destruction timer, fell off screen)
  - [x] Shield hit sound + haptics for HAZ_RADIATION / BOSS_ENTROPY_CORE drain bypass
  - [x] Heat Bat always-visible cyan aura + damage rebalance (10→5 / 20→10)
  - [x] Lightning damage rebalanced (25→13, ~26% base shield)
  - [x] Boss HP/WP scaled by difficultyMultiplier
  - [x] wpInvulnerabilityTimer separated from body invulnerability
  - [x] Boss balance: WP cooldown 0.25s, WP hit radius 45f
  - [x] Cloud Zone purple-blue storm clouds (washed-out whites → purple cumulus)
  - [x] Earth Zone golden hour gradient (night → warm golden hour palette)
- [x] Shield Platform (STABILITY→SHLD): no bounce, shield fully restored, "SHIELDS RESTORED" text
- [x] Conveyor Platform fix: velocityX=150f continuous push (no bounce cycle)
- [x] Zone jump freeze fix: jumpToZone() full cleanup (threats, projectiles, tethers, particles, stats, music)
- [x] Zone change notification: TACTICAL notification + ZoneDiscoveryCard auto-fade (4s)
- [x] Multi-hit WPs: wpHitCounts: IntArray per-WP, tiered by difficulty (1-4 hits), partial-hit purple burst + shield-hit SFX
- [x] Heat Bat visibility: cyan aura 0.06→0.15, wing-beat shadow 0.08→0.15, eye glow 0.5→0.7, white silhouette outline
- [x] Boss kill score removed: all 3 onScoreUpdate(1000) removed — score = altitude only
- [x] Milestone threshold rebalance: 8 thresholds increased (Commander 1500, Hive 3000, Gatekeeper 4500, Forger 6500, Leviathan 8500, Star Eater 11000, Anchor 14000, Engine 17000, Signal 21000, Architect 30000, Core 50000, Singularity 100000)
- [x] One boss per frame guard + no boss while boss alive check
- [x] Boss Recurrence system: ~3s bossRecurrenceTimer, previously-defeated bosses + zone-eligible mini-bosses, 5-25% chance per check, 1.3× difficulty, RECURRENCE notification
- [x] Boss music fix: setBossActive checks BOSS || MINI_BOSS
- [x] Hazard suppression 0.3f→0.1f during bosses, Solar Flare filtered out
- [x] ThreatRegistry.getEntries() added
- [x] Billing integration: PurchaseManager rewritten with BillingClient + fallback dialog
- [x] ShopScreen: currency exchange with premium card, cash balance, V2 placeholders
- [x] ProgressionManager: totalCash persistence (grantReward accumulation)
- [x] SettingsScreen: premium no-toggle-off (ADS REMOVED ✓ disabled) + RESET PROGRESS / FACTORY RESET buttons
- [x] MainActivity/MainMenu: shop route + SHOP button added
- [x] AboutScreen: updated to V1.5.0 with cash balance display
- [x] Dev menu gated on BuildConfig.DEBUG: cheatsEnabled = BuildConfig.DEBUG
- [x] Play Store purchase gating: debug=confirm dialog, release="PLAY STORE REQUIRED" info dialog
- [x] Cloud/Earth visual polish
## Post-Release / Deferred to EPIC 12
- [~] Performance profiling (frame drops in upper zones / dense threat fields)
- [~] Play Store listing prep (screenshots, description, assets)
- [~] Final APK build + testing

## Phase Sequencing Rationale

```
Phase 1 (Game Feel)
  │
  ├──► Biggest player-facing impact
  ├──► Isolated changes, low risk
  └──► Quick wins build momentum

Phase 2 (Notifications)
  │
  ├──► Unblocks Phases 3 and 5 (notification-based events)
  ├──► Highest risk — touches every system
  └──► Best done early before more notification hooks are added

Phase 3 (Tutorial Removal + Discovery)
  │
  ├──► Depends on Phase 2 for clean notification pipeline
  └──► Relatively mechanical replacement

Phase 4 (Archives + Monetization)
  │
  ├──► Large UI effort + SDK integration
  ├──► Monetization early gives buffer for ad lifecycle bug fixes
  └──► Notification hooks already set up by Phase 2 + 3

Phase 5 (Audio + Haptics)
  │
  ├──► Independent of all other phases
  └──► Procedural wiring done; ready for final assets

Phase 6 (Archive Popups)
  │
  ├──► Depends on Phase 4 archive redesign
  ├──► Large UI effort + render integration
  └──► Unlocks before release for final data entry

Phase 7 (Release)
  │
  └──► Naturally last — requires all other phases complete
```

## Effort Estimate

 Phase | Description | Est. Days |
-------|-------------|-----------|
 1 | Core Game Feel (items 2,3,4,5,16) | **Complete** |
 2 | Notification + Ad Layout (items 8,9,10,11,17) | **Complete** |
 3 | Tutorial Removal + Discovery (items 1,7,12,13) | **Complete** |
 4 | Archives + SDK Integration (items 6,18) | 5–8 |
 5 | Audio Pass + Haptics (item 14, 19) | 3–5 + asset sourcing |
 6 | Archive Detail Popups (item 20) | 3–5 |
 7 | Release Prep (item 15) | 3–5 |

**Total:** ~22–38 days development + asset sourcing time.
