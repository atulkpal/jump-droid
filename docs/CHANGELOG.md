# Changelog

All notable changes to this project are recorded as dated engineering events.

---

## 2026-07-02

**Sprint / Phase:** Firebase Integration & Release Hardening Pass

**Branch:** `refactor/cleanup`

**Status:** Production Hardened ✅

### Added
- **Analytics Layer** (`GameAnalytics.kt`, `FirebaseGameAnalytics.kt`): Created domain-driven abstraction for high-value events.
  - No gameplay code references Firebase SDK directly.
  - Strongly typed events: `game_start`, `game_over` (with reason), `zone_changed`, `mission_status`, `boss_encounter`, `rocket_unlock`, `module_equip`, `ad_impression`, `ad_clicked`.
- **Ad Configuration** (`AdConfig.kt`): Centralized unit IDs with automatic debug/release switching.
  - Debug builds use Google sample IDs to prevent invalid production traffic.
  - Release builds use production AdMob unit IDs.
- **Dependency Injection**: `GameAnalytics` injected into `GameEngine` and provided to UI via `LocalAnalytics` CompositionLocal.

### Changed
- **GameEngine Instrumentation**: Level start, game over (with reason), boss events, and mission events hooked into the analytics layer.
- **MainActivity Refactor**: Entry point for analytics initialization and automatic screen tracking via NavHost listener.
- **AdMob Hardening**: `GlobalAdBanner` and `RewardedAdHelper` migrated to `AdConfig` and instrumented for impression/click tracking.
- **Loadout Tracking**: Added callback to `LoadoutManager` to track module equipping.
- **Mission Tracking**: Added callback to `MissionManager` to track mission activation.

### Validation
- `:app:assembleDebug` — BUILD SUCCESSFUL.
- `:app:assembleRelease` — BUILD SUCCESSFUL.
- Verified debug ads (sample IDs) and production ad configuration.
- Verified domain models used for all analytics calls.

---

## 2026-07-01

**Sprint / Phase:** Release Polish — Phase 7 Boss Density & Score Integrity

**Branch:** `refactor/cleanup`

**Status:** Phases 1–7 Complete ✅ — Performance Profiling, Store Listing, Final APK

### Changed
- **Score integrity** (`ThreatInteractionProcessor.kt`): Removed `onScoreUpdate(1000)` from all 3 boss defeat paths (WP phase transition, WP auto-collapse, full defeat). Score is now purely altitude-based — no more artificial spikes. Boss kills no longer push score toward the next milestone, preventing cascading boss spawns.
- **Milestone guards** (`EncounterDirector.kt`): Added two guards to milestone spawning — (1) one boss per frame (`spawnedThisFrame` + break), (2) no new milestone boss while any boss/mini-boss is alive. Prevents 2–3 bosses from spawning simultaneously and ensures one-at-a-time encounters.
- **Milestone thresholds rebalanced** (`EncounterDirector.kt`): Commander 1500 (unchanged), Thermal Hive 2500→3000, Gatekeeper 4000→4500, Forger 5500→6500, Leviathan 7000→8500, Star Eater 10000→11000, Gravity Anchor 13000→14000, Void Engine 15000→17000, Signal 18000→21000, Architect 25000→30000. Entropy Core and Singularity unchanged. More even spacing through late game.
- **Boss Recurrence system** (`EncounterDirector.kt`, `ThreatRegistry.kt`): New `bossRecurrenceTimer` field (~3s cadence). When no boss is alive, picks from previously-defeated bosses + any zone-eligible mini-boss and spawns at 1.3× difficulty. Fills all dead zones between milestones with dynamic repeat encounters.
- **Boss music fix** (`GameEngine.kt`): Changed `setBossActive` check from `ThreatType.BOSS` only to `BOSS || MINI_BOSS`. All 4 mini-bosses now trigger `bgm_boss` — previously only full bosses played boss music.
- **Hazard suppression during bosses** (`EncounterDirector.kt`): `spawnChanceMod` reduced from `0.3f` to `0.1f` when a boss is alive. Solar Flare explicitly filtered out during boss fights. Prevents unfair hazard+boss combinations.
- **Heat Bat visibility & damage** (`HeatBatRenderer.kt`, `ThreatInteractionProcessor.kt`): Cyan aura alpha 0.06f→0.15f (2.5× brighter), wing-beat shadow 0.08f→0.15f, eye glow 0.5f→0.7f, added 0.1f white silhouette outline. Damage reduced: heat ≥ 70 from 20→10, heat < 70 from 10→5.
- **Shield Platform** (`GameEngine.kt`, `PlatformRenderer.kt`, `Models.kt`): STABILITY_PLATFORM renamed to Shield Platform — no bounce, velocityY = 0f, shield fully restored, "SHIELDS RESTORED" floating text. Visual label "STAB"→"SHLD" in SciFiCyan.
- **Conveyor Platform fix** (`GameEngine.kt`): Position push replaced with `velocityX = 150f` for continuous velocity-based push every sub-step. Removed bounce.
- **Zone jump freeze fix** (`GameEngine.kt`): `jumpToZone()` rewritten with full cleanup matching `restartGame()` (clears threats/projectiles/tethers/particles, resets timers, fuel/shield/integrity, plays zone music).
- **Zone change notification** (`GameEngine.kt`, `GamePlayScreen.kt`): `onZoneChanged` posts TACTICAL notification. `ZoneDiscoveryCard` wired in HUDLayer via `discoveryManager.activeEvent` with 4s auto-fade.
- **Multi-hit weak points** (`ActiveThreat.kt`, `ThreatAIUpdater.kt`, `ThreatInteractionProcessor.kt`): Added `wpHitCounts: IntArray` per-WP hit counter. Tiered difficulty: 4 zone tiers based on `difficultyMultiplier` control hitDist (45→22f), wpRequiredHits (1→4), and wpInvulnerabilityTimer (0.25→0.75s). WP only destroys when `wpHitCounts[i] >= tierWpHits`. Partial hits show purple burst + shield-hit SFX.
- **Data reset safety** (`SettingsScreen.kt`): Added RESET PROGRESS button (preserves premium_user) and FACTORY RESET button (wipes everything including premium) with confirmation dialogs.
- **Dev menu gated on BuildConfig.DEBUG** (`GamePlayScreen.kt`): `cheatsEnabled = BuildConfig.DEBUG` — dev UI hidden in release builds.
- **Play Store purchase gating** (`SettingsScreen.kt`, `ShopScreen.kt`): Fallback dialog switches on `BuildConfig.DEBUG` — debug shows purchase confirmation, release shows "PLAY STORE REQUIRED" info-only dialog.
- **Cloud Layer gradient** (`ZoneBackgroundRenderer.kt`): Dark purple (`#1A0033`/`#0D001A`/`#1A1A3E`).
- **Earth Zone golden hour** (`ZoneBackgroundRenderer.kt`): Night palette replaced with warm golden hour gradient.
- **Bug fixes**: Alarm loops stop on all 3 death paths, BGM restarts on continue, shield hit sound for bypass damage, Heat Bat always-visible cyan aura, Lightning damage 25→13, boss HP/WP scaling by difficultyMultiplier, wpInvulnerabilityTimer separation, WP cooldown 0.25s / radius 45f, Cloud Zone purple-blue clouds.

### Added
- `ThreatRegistry.getEntries()` — exposes all registered threat definitions for recurrence pool queries.

### Documentation
- `AGENTS.md`: Updated project state to Release Polish Phase 7 Complete. Added boss recurrence, score fix, Heat Bat, Shield Platform, multi-hit WP, data reset to Completed Work. Updated Next Planned Work.
- `CHANGELOG.md`: Added this entry.
- `docs/THREAT_MASTER_TABLE.md`: Removed "+1000 score" from Thermal Hive entry. Updated Heat Bat damage values (10/20→5/10).
- `docs/JumpDroid_EPIC_Tracker.md`: Updated Phase 7 checklist with boss recurrence, score integrity, Heat Bat, Shield Platform, notifications, multi-hit WP, Play Store gating, data reset.
- `docs/roadmap/RELEASE_POLISH_PLAN.md`: Updated Phase 7 status.
- `docs/REPORTS/SPRINT_10_6_IMPLEMENTATION_REPORT.md`: Updated milestone thresholds.
- `docs/gameplay/BOSS_DESIGN_BIBLE.md`: Added Boss Recurrence section.

---

## 2026-06-29

**Sprint / Phase:** Release Polish — Phase 7 Bug Bash & Hitbox Fixes

**Branch:** `refactor/cleanup`

**Status:** Phases 1–6 Complete ✅ — Phase 7 Bug Bash Active

### Fixed
- **Player hitbox** (`ThreatInteractionProcessor.kt`): Added `rPlayer = 28f` to all 5 threat collision checks + WP hit check. The rocket's full 40×70 body now counts for contact, not just its center point.
- **WP system — per-WP tracking** (`ActiveThreat.kt`, 11 renderers): Replaced broken `i >= activeWeakPoints` index assumption with `wpDestroyedMask` bitmask. WPs now track correctly regardless of destruction order. Previously, hitting WP0 first would make WP2 invisible/untargetable and leave WP0 falsely visible — affecting all multi-WP bosses.
- **WP hitbox positions** (11 bosses): Every boss's WP detection now matches exactly where the WP is rendered. Signals: random→center. GravityAnchor: center→`(x, y ± 50)`. Forger: `(i-1)*70, y-30` → `(i-1)*60, y`. Architect, EntropyCore, Singularity: static orbits → time-based/jittered orbits matching renderers. StarEater: 1 at center → 3 orbiting (renderer + hitbox). Leviathan: all indices → every-other segment. VoidEngine: 180° → 120° arms.
- **Signal ghost platform spam** (`ThreatInteractionProcessor.kt`): Per-frame rate reduced 0.15→0.01 / 0.25→0.02 with 600ms cooldown gate. ≈1–2 ghost platforms/sec max instead of 36–60.
- **Gravity Anchor pull** (`ThreatInteractionProcessor.kt`): Added 3s cycle with 0.6s "PULL WEAKENED" safe window (pull drops to 15%). Strength capped at 3500×2.5 max. WP hit distance increased 60→100f.
- **Lightning dissolve** (`ThreatAIUpdater.kt`, `LightningRenderer.kt`): 1.5s dissolve phase with cloud fragment particles and fading sparks instead of instant vanish.

### Changed
- **Boss rebalance** (`ThreatAIUpdater.kt`, `ThreatInteractionProcessor.kt`, `ThreatRegistry.kt`): Boss HP scaling by zone (150–1200). Y-pursuit added to all 10 bosses. Phase-based AI for Forger, GravityAnchor, Architect, EntropyCore. WP counts: StarEater 1→3, Signal 1→3, GravityAnchor 1→2. Destruction burst scaling by boss type with continuous emission. Power-up glow enhanced with outer halo ring.

### Documentation
- `CHANGELOG.md`: Added this entry.
- `docs/roadmap/RELEASE_POLISH_PLAN.md`: Updated Phase 7 checklist.
- `docs/JumpDroid_EPIC_Tracker.md`: Updated Release Polish section.
- `AGENTS.md`: Updated project state to Release Polish Phase 7 Active.

---

## 2026-06-27

**Sprint / Phase:** Release Polish — Phases 1–4 Complete

**Branch:** `refactor/cleanup`

**Status:** Phases 1–4 Complete ✅ — Phase 5 (Audio) In Progress

### Phase 1 — Core Game Feel
- **Power-up spawn redesign** (`PowerUpManager.kt`): Removed falling-drop architecture. Power-ups now hover at random visible locations with dead-zone avoidance (boss hitboxes, hazard radii). Added 8s despawn timer with accelerating glow pulse fade-out.
- **Combo reward animation** (`GameEngine.kt`, `ComboManager.kt`): Rewards originate from top-left combo ring position, animate toward player. "COMBO REWARD" floating text. Removed FlyingReward falling animation.
- **Boss death sequence** (`ThreatInteractionProcessor.kt`): Progressive 1.5s piece-by-piece destruction with color-mapped debris per boss type. Final large explosion + screen shake.
- **Mission completion celebration** (`GameEngine.kt`): Triple burst (Gold + Cyan) at top-center, floating text "MISSION COMPLETE", screen flash + shake.
- **Rewarded continue UX** (`GameOverOverlay.kt`): `[AD]` badge added to "RE-ESTABLISH LINK" button. One-time-per-run enforcement.

### Phase 2 — Notification Architecture
- **Notification priority system** (`NotificationManager.kt`): 3-tier `NotificationPriority` enum (CRITICAL / TACTICAL / FLAVOR). Dedup by message content. Priority preemption.
- **Unified notification area** (`HudWidgets.kt`): Stacks up to 3 entries. Priority-based text sizing and coloring. Positioned top=180dp.
- **Priority assignments**: All call sites updated — CRITICAL for shield/hull critical, TACTICAL for mission/zone, FLAVOR for archive/artifact.
- **Weak notification removal**: Eliminated "REINFORCEMENTS INBOUND" and other low-value entries.
- **GlobalAdBanner placeholder** (`AdComponents.kt`): 56dp placeholder box, hidden when `isPremiumUser`. Added to all menu screens and overlays. No banner on GamePlayScreen.

### Phase 3 — Tutorial Removal + Discovery
- **Tutorial pop-ups removed**: All `showTutorialPopup()` / `tutorialStep` logic deleted.
- **Platform discovery messages**: All 14 platform types show one-time gameplay hint float text on first landing.
- **Unlock celebration**: Achievement unlocks spawn gold burst.
- **Archive badge**: MainMenuScreen shows SciFiPurple dot on ARCHIVE button when notification pending.
- **Haptics activation**: HapticManager instantiated in GameEngine, wired to SurvivalManager callbacks.

### Phase 4 — Data Archives + Monetization
- **Archives redesign** (`ArchiveScreen.kt`): Re-organized into 12 categories. Added MECHANICS category. New ArchiveCard composable with locked/unlocked visual states.
- **AdMob SDK integration**: `play-services-ads:23.6.0` dependency, INTERNET permission, test AdMob app ID. MobileAds initialization in MainActivity.
- **GlobalAdBanner rewrite**: Real AdView rendering Google banner ad (`ca-app-pub-4153575596488132/3022346201`).
- **Rewarded continue**: RewardedAdHelper preloads RewardedAd (`ca-app-pub-4153575596488132/5155087899`), shows on continue button click, falls back to free continue on failure.
- **Premium purchase**: PurchaseManager wraps isPremiumUser flag. SettingsScreen shows "UPGRADE: REMOVE ADS" / "ADS REMOVED ✓" button.

### Documentation
- `AGENTS.md`: Updated project state to Release Polish Phases 1–4 Complete. Fixed Doc_Migration reference, removed duplicate index entry.
- `docs/roadmap/RELEASE_POLISH_PLAN.md`: Updated status headers, added Phase 4 implementation notes, checked off testing checklist items.
- `docs/analysis/MISSION_RECOVERY_AUDIT.md`: Created redirect stub.
- `docs/roadmap/EPIC_8_MIGRATION_PLAN.md`: Created redirect stub.
- `docs/REPORTS/Doc_Migration.md`: Created redirect stub.

---

## 2026-06-26

**Sprint / Phase:** EPIC 8.5 Recovery — Full Regression Cleanup

**Branch:** `refactor/cleanup`

**Status:** Complete ✅

### Fixed
- **continueRun()**: Restored platform-based respawn (find lowest visible, exclude PHASE), destructionTimer/heat/lastPlatform reset, re-entry effects (burst, shake, flash, "SYSTEM REBOOTED" text)
- **Thrust trail particles**: Orange (0xFFFF9800) continuous exhaust particles, cyan when turbo active, at 40% chance per thrusting frame
- **Death burst**: Red burst + screen shake on hull failure and off-screen fall death
- **WorldRenderer rendering pipeline**: Reconnected drawTether() (cyan animated electrical bolt) and drawVisualObstruction() (radial fog gradient)
- **PHASE platform collision**: Added 4000ms cycle check — player falls through when visually intangible (progress ≥ 0.4f)
- **BREAKABLE platform trigger**: isBreaking=true set on landing + discovery check
- **Ghost/trap platform**: isTrapPlatform now breaks instantly on landing + NORMAL_PLATFORM discovery
- **Broken platform cleanup**: Fully-broken platforms removed from list with orange burst particle effect
- **Missing platform landing branches restored**: ICE (slippery friction 0.98×), MOVING (speed transfer + mission progress), FLUX (teleport + purple burst + cooldown), GRAVITON (gravity well text), MAGNETIC (discovery), CONVEYOR (discovery), PHASE (discovery), BREAKABLE (discovery)
- **Platform discovery checks**: All 11 platform types now register first-landing discovery (was missing for 7 types)
- **Magnetic/Graviton field effects**: Proximity force fields applied per sub-step — MAGNETIC (pull 1200, damping 0.85) and GRAVITON (pull 3000, damping 0.75, radius 180px)
- **Moving platform carry-over**: Player velocity follows moving platform while standing on top
- **Conveyor speed push**: 150px/s horizontal push applied while standing on conveyor platform
- **pendingReward infinite loop**: comboManager.pendingReward now cleared after FlyingReward creation — stopped endless "FUEL RECOVERED" text, green circle animation, and fuel regeneration spam

### Changed
- **SoundManager.isMuted**: Default remains true — generated tones retained as placeholder; infrastructure ready for production audio files

---

## 2026-06-24

**Sprint / Phase:** EPIC 8.5 Execution — Architecture Decomposition Complete

**Branch:** `epic8.5-rebased`

**Status:** Complete ✅

### Added
- **26 Threat Renderers**: One file per threat (11 hazards, 8 enemies, 6 bosses + 3 shared). `ThreatRenderer` interface + `ThreatRendererRegistry` with eager map dispatch.
- **ThreatAIUpdater.kt**: Extracted AI behavior `when`-block from `ActiveThreat.update()` (566 lines).
- **ThreatInteractionProcessor.kt**: Extracted collision/damage `when`-block from `ActiveThreat.processInteraction()` (551 lines).
- **StarfieldBackground.kt**: Shared animated starfield composable (replaced 6 copy-paste implementations).
- **GaugeBar.kt**: Base gauge composable with shared clipPath + seg ticks + interference lines (4 gauges refactored).
- **HudContext.kt**: Bundles `gameTime` + `interferenceTimer` + `zone` — eliminates 12 parameter passthroughs.
- **GameEngine.kt**: State container with all managers + 40+ observable game state vars.
- **ProgressionService.kt**: Interface decoupling MissionManager from ProgressionManager.
- **GameStats.kt**: Data class for session stat commit.
- **MissionScreen.kt**: Created from earlier extraction.
- **PowerUpManager.kt**: PowerUp lifecycle extracted from GameScreen.

### Changed
- **ActiveThreat.kt**: 1,224 → 123 lines (90% reduction). AI and interaction logic delegated to extension functions.
- **GameScreen.kt**: 3,901 → 2,011 lines (48% reduction). Threat rendering extracted to 26 files. Ceremony lifecycle extracted. PowerUpManager extracted.
- **Mission.kt**: `checkCompletion()` made pure (side-effect moved to callers).
- **MissionManager.kt**: Added `getBestMissionForTrack()`. Decoupled from ProgressionManager via interface. Ceremony lifecycle extracted from GameScreen.
- **ComboManager.kt**: Rewards merged (unified tier + survival drop table). Tier table externalized to `TIERS` companion.
- **HudWidgets.kt**: 4 gauges refactored to use shared `GaugeBar` composable. `HudContext` hoisted.
- **MainMenuScreen.kt, AboutScreen.kt, SettingsScreen.kt, PauseOverlay.kt, GameOverOverlay.kt, HangarScreen.kt**: Starfield replaced with shared `StarfieldBackground` composable.
- **AGENTS.md**: Updated for EPIC 8.5 completion. Points to `epic8.5-structured` tag.

### Removed
- **MissionRow.kt**: Deleted (ceremony lifecycle moved elsewhere).
- **`isNew` property**: Removed from `Mission.kt`.
- **`MissionType.COMBO`**: Removed.
- **`CeremonyStage`**: Simplified to NONE/GLOW/REPLACING.
- **Duplicate ComboDisplay**: Removed from center HUD.

### Tag
- **`epic8.5-structured`**: Rollback point at commit `9363434`.

---

## 2026-06-23

**Sprint / Phase:** EPIC 8.5 Planning — Architecture Decomposition Roadmap

**Branch:** `epic8-mission-migration`

**Status:** Planned

### Added
- **EPIC 8.5 Decomposition Plan**: 9-sprint roadmap (8.5.0–8.5.8) to decompose `GameScreen.kt` (3,901 lines), `ActiveThreat.kt` (1,224 lines), `ProgressionManager.kt`, and navigation into manageable components before Phase 9 feature work begins.
- **EPIC 8 Tech Debt Audit**: 17 findings classified across 5 severity levels. 2 CRITICAL (GameScreen God Object, ActiveThreat God Object), 3 MAJOR, 8 MODERATE, 4 MINOR. Report: `docs/analysis/EPIC8_TECH_DEBT_AUDIT.md`
- **EPIC 8 Validation Report**: Final validation of all EPIC 8 systems — stabilization, polish, and mission event audit. No blockers found. Report: `docs/REPORTS/EPIC8_VALIDATION_REPORT.md`
- **EPIC 8.5 Planning Report**: Executive summary of decomposition sprint planning. Report: `docs/REPORTS/EPIC8_5_PLANNING_REPORT.md`

### Changed
- **EPIC 9**: Officially deferred until EPIC 8.5 Architecture Decomposition completes.
- **AGENTS.md**: Updated for EPIC 8.5 transition. Known issues now include GameScreen.kt (3,901 lines) and ActiveThreat.kt (1,224 lines) as EPIC 8.5 targets.
- **EPIC_8_TRACKER.md**: Status updated to "Functionally Complete — Awaiting Runtime Signoff". Post-8 content moved under deferred column.
- **JumpDroid_EPIC_Tracker.md**: EPIC 8.5 section added with 9-sprint structure (8.5.0–8.5.8). EPIC 9 marked DEFERRED.

### Documentation
- `docs/roadmap/EPIC_8_5_DECOMPOSITION_PLAN.md`: Full decomposition roadmap with sprint breakdown, risks, dependencies, and success criteria.
- `docs/REPORTS/EPIC8_5_PLANNING_REPORT.md`: Executive summary for cross-team communication.
- `docs/analysis/EPIC8_TECH_DEBT_AUDIT.md`: Comprehensive 17-finding codebase audit.

### Revision: Sprint 8.5.0 Baseline Added
- **Request**: User approved the decomposition plan but requested Sprint 8.5.0 (Baseline Capture) be inserted before Sprint 8.5.1.
- **Action**: Added Sprint 8.5.0 to all documents. Existing sprints 8.5.1–8.5.8 unchanged.
- **8.5.0 scope**: Baseline tag `epic8.5-baseline`, build verification, file size inventory, architecture metrics, screenshot baseline, baseline report.
- **Impact**: 9 sprints total (was 8). Estimated effort: 13-18 sessions (was 12-17). All sprint numbers 8.5.1–8.5.8 preserved. Sprint 8.5.0 is precondition for all subsequent sprints.
- **Documents updated**: EPIC_8_5_DECOMPOSITION_PLAN.md, EPIC8_5_PLANNING_REPORT.md, JumpDroid_EPIC_Tracker.md, CHANGELOG.md

---

## 2026-06-22

**Sprint / Phase:** EPIC 8 — Missions & Progression (Phase 1-3)

**Branch:** `epic8-mission-migration`

**Status:** In Progress

### Added
- **Intelligence Network**: Real-time gameplay stat tracking across physics and combat loops.
- **GameStats Model**: Unified 19-point metric schema for run evaluation.
- **Mission Reward Sealed Classes**: Added `Cash` and `ModuleUnlock` support to the reward pipeline.
- **Persistent Lifetime Stats**: Added cumulative flight time and encounter tracking to `ProgressionManager`.
- **EPIC 8 Tracker**: Detailed tracking for mission migration.

### Changed
- **Mission Model Refactor**: Aligned legacy mission data with EPIC 7 sealed reward classes.
- **Mission Registry Port**: Migrated 48 missions from prototype branch to production architecture.
- **Manager Integration**: Unified `MissionManager` with `ProgressionManager` for central reward handling.

---

## 2026-06-22

**Sprint / Phase:** Sprint C Completion — Structural Fix, Boss Projectiles, Enemy Projectiles, Zone Redistribution

**Branch:** `development` (merged from `sprintc-fixes`)

**Commit:** `a1accfb`

**Status:** Completed

### Fixed
- **Critical structural bug**: Boss `update()` code was inside `ThreatType.ENEMY ->` `when` branch. Commander (MINI_BOSS) and Gatekeeper (BOSS) never executed their AI logic. Moved entity-specific handlers outside the `when` block.
- **BOSS_VOID_ENGINE**: `onVisualFeedback(10f, 0.4f)` now fires max once per 0.5s instead of every sub-step (240/sec). `gravityPulseTimer` dead code removed.
- **BOSS_LEVIATHAN**: Weak point oscillation corrected from `sin(life×1000f)` to `sin(life×1.5f)`. Wall pressure increased 50× to 100000.
- **BOSS_SIGNAL**: Phase 3 now checks `activeWeakPoints <= 0` for exit. Weak point position uses per-frame Random instead of seeded.
- **ENT_VOID_WHALE**: Slipstream force multiplied 16× (2000 → 32000), vacuum 16× (3000 → 48000) for noticeable gameplay impact.
- **ENT_VOID_WRAITH**: Visual distinction between materialized (purple aura, full body, crackling energy) and phased (gray wireframe, 8% alpha, glitch rects).
- **ENT_STALKER**: Base speed increased, alertLevel ramp rate increased.
- **HAZ_VOID_ANOMALY**: Zone-specific duration override for longer-lasting Void anomalies.
- **BOLT rendering**: Reverted to original size (5-trail, no 2.5× scale, no pulsing glow).
- **Floating text**: Critical text font size reduced from `headlineSmall` to `titleSmall` (~42% smaller).

### Added
- **Boss projectile systems**: All 6 bosses fire phase-specific projectiles. Commander (P3+ 3-way BOLT). Gatekeeper (P2 BOLT / P3 BEAM). Star-Eater (P2 WAVE / P3 MISSILE). Leviathan (P2 BOLT / P3 BEAM). Void Engine (P2 WAVE / P3 3-way BOLT). Signal (P2 BOLT / P3 BEAM).
- **Enemy projectiles**: Defense Node fires orange BOLT (8dmg, 2s cooldown) when locked-on. Void Tracker fires red BOLT (8dmg, 1.5s cooldown) at alertLevel > 0.5.
- **Zone redistribution**: Earth is boss-free. Commander spans Cloud Layer–Void. Gatekeeper: Orbit+. Star-Eater/Leviathan: Deep Space+. Void Engine/Signal: Void. All bosses expanded to native zone + all zones above.
- **Difficulty scaling**: Zone-based HP multiplier (×1.0–×3.0) for bosses at spawn via `difficultyMultiplier`.
- **Threat density scaling**: `intensityFactor` applied to EncounterDirector spawn intervals and caps.
- **EncounterDirector zone gating**: Milestone boss spawns check `currentZone in def.spawnRules.allowedZones`.
- **Cloud Commander fallback**: Spawn chance reduced 60% (×0.4 multiplier).
- **`onSpawnThreat` callback**: Wired in GameScreen.kt for boss minion spawning.

### Documentation
- **THREAT_MASTER_TABLE.md**: Created with Status, AI Behavior, and Projectiles columns for all 22 entities.
- **THREATS.md**: Updated all boss/projectile entries. Checklist finalized. Changelog entries added.
- **BOSS_DESIGN_BIBLE.md**: Updated boss entries, zone expansions, enemy projectiles, structural fix note, v1.3 changelog.
- **SPRINT_C_AUDIT.md**: Updated with current session fixes, bug resolutions, Appendix A projectile data.
- **SPRINT_C_COMPLIANCE_GAP.md**: Updated gap closure status.

---

## 2026-06-20

**Sprint / Phase:** Visual Overhaul Sprint 2-3 — Premium Screen Polish

**Branch:** `development`

**Commits:** `35e03ce`, `47ee28e`, `afbecdc`, `47d4191`

**Status:** Completed

### Added
- **TitleScreen scanning drone**: Cinematic patrol with Z-axis depth scaling, rotating radar sweep beam with layered glow, light column, rocket detection glow
- **Ambient scan rings**: Pulsing concentric circles emanating from rocket on title screen
- **Floating data particles**: 6 orbiting colored data dots around the rocket
- **Animated starfields**: Drifting starfield with sine-wave twinkle on HangarScreen, SettingsScreen, AboutScreen, GameOverOverlay, PauseOverlay
- **Status footers**: "HANGAR // ROCKET SELECT // {rank}", "SYSTEM PREFERENCES // AUDIO // DATA", "POWERED BY ASHWATH.AI // V1.2.0", "MISSION PAUSED // {ZONE} SECTOR"
- **Gauge value labels**: Raw integer value beside emoji icons, percentage label below bar on all 4 gauges

### Changed
- **HudWidgets**: Widened gauge bars from 6dp → 10dp, replaced solid fills with vertical gradient fills, added 10-segment tick marks, shield shimmer effect, consistent RoundedCornerShape(4.dp)
- **MainMenuScreen**: "COMMAND CENTER" text sizing from `headlineLarge` → `headlineMedium` with tighter `letterSpacing(2.sp)` to prevent wrapping
- **Track backgrounds**: Reverted from zone-accented tint to neutral `SciFiSurface.copy(alpha = 0.4f)`
- **Gauge number visibility**: Raw value 10sp full color, percentage 8sp full color
- **Card borders**: ComboHudBar and ZoneDiscoveryCard borders thinned to 0.5dp with `alpha = 0.4f` for premium look
- **Text shadows**: Added pulsing glow shadows to titles across all screens

### Fixed
- **TitleScreen drone**: Replaced triangle cone with thin radar sweep line, removed "⚠ HUMAN DETECTED" overlay text
- **Text wrapping**: All screen titles now use `headlineMedium` with 2.sp letter spacing to prevent 2-line wrapping on narrow screens

### Removed
- Triangle scan cone from TitleScreen drone (replaced with sweep line)
- Zone-tinted gauge track backgrounds (reverted to neutral dark)

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL across all 4 commits
- All composable signatures unchanged — zero breakage risk for HUD revamp

---

## 2026-06-19

**Sprint / Phase:** Engine Expansion — Core Systems

**Status:** Completed

### Added
- **Projectile Engine**: `Projectile.kt` and `ProjectileManager.kt` for global ranged attack tracking.
- **Input Buffer**: `InputBufferManager.kt` for time-delayed input processing (Chrono-Lag support).
- **Tether Physics**: `Tether.kt` for physical links between the player and anchors.
- **Visual Obstruction**: `globalFogAlpha` state and `drawVisualObstruction` renderer for fog/smoke effects.
- **Engine Documentation**: `docs/design/ENGINE_EXTENSIONS.md`.

### Changed
- **ActiveThreat Refactor**: Updated `update()` and `processInteraction()` signatures to support Entity-to-Entity (E2E) logic.
- **Physics Loop**: Integrated projectile collision and tether tension sub-routines into the sub-stepped physics loop.
- **Canvas Rendering**: Added hooks for projectiles, tethers, and fog layers.
- **DevConfig**: Added toggles for all new engine systems (enabled by default).
- **Player Model**: Added `activeTether` and `inputLatency` state fields.

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL
- Verified baseline gameplay feel is preserved with default (zero) latency.

### Notes
- This sprint provides the technical foundation for Advanced Threats (Sprint C) and Boss Phases (EPIC 6).
- All systems are modular and can be disabled via `DevConfig.kt`.

---

## 2026-06-18

**Sprint / Phase:** Refactor Sprint T4 Recovery

**Branch:** `refactor/t4-recovery`

**Status:** COMPLETE AND STABILIZED

### Added
- `SurvivalManager.kt` — Extracted damage and destruction lifecycle logic.
- `EncounterDirector.kt` — Extracted spawn rules and boss milestones.

### Changed
- `GameScreen.kt`: Delegated 400+ lines of threat interaction to `ActiveThreat.kt`.
- `GameScreen.kt`: Integrated `SurvivalManager` and `EncounterDirector`.
- `ActiveThreat.kt`: Added `processInteraction` for delegated proximity logic.

### Fixed
- **Mission Lifecycle**: Restored `missionManager.selectNextMission()` to fix ceremony loops and combo HUD instability.
- **Spawn Density**: Expanded enemy routing for `UPPER_ATMOSPHERE` and tuned probabilities to development baseline.
- **Thread Safety**: Applied `.toList()` snapshots to all critical StateList iterations to resolve `ConcurrentModificationException`.
- **Continue Recovery**: Fixed `continueRun()` state reset to prevent immediate death-loop on hull destruction.

### Notes
- T4 architecture is now fully validated and production-ready.
- Cumulative game loop reduction: 4,344 → 2,538 lines (−41.6%).

---

## 2026-06-18

**Sprint / Phase:** Refactor Sprint T3 — Logic & Manager Extraction

**Branch:** `refactor/logic-extraction`

**Commit:** `e79068d`

**Status:** Completed

### Added
- `NotificationManager.kt` — Encapsulates notification queue and timer logic
- `FloatingTextManager.kt` — Manages lifecycle of floating status text popups
- `PlatformManager.kt` — Encapsulates platform generation and streak tracking

### Changed
- `GameScreen.kt`: 20+ call sites updated to use `NotificationManager`
- `GameScreen.kt`: 15+ call sites updated to use `FloatingTextManager`
- `ProgressionManager.kt`: Added `saveHighScore`, `checkUnlocks`, and `wipeData` methods
- `GameScreen.kt`: Achievement and high score logic delegated to `ProgressionManager`
- `GameScreen.kt`: Platform generation delegated to `PlatformManager`
- Net reduction: 76 lines (3,109 → 3,033)

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL
- Verified on emulator

### Notes
- This sprint shifted the focus from UI extraction to logic modularization.
- Core game loop remains in `GameScreen.kt` but is now significantly leaner.

---

## 2026-06-18

**Sprint / Phase:** Refactor Sprint T1 — Phase 1

**Branch:** `refactor/ui-extraction`

**Commit:** `fb93e9d`

**Status:** Completed

### Added
- 15 standalone UI composable files extracted from `GameScreen.kt`:
  - Screens: `TitleScreen.kt`, `MainMenuScreen.kt`, `HangarScreen.kt`, `ArchiveScreen.kt`, `SettingsScreen.kt`, `AboutScreen.kt`, `LeaderboardScreen.kt`
  - Overlays: `PauseOverlay.kt`, `TutorialOverlay.kt`, `HelpOverlay.kt`, `UnlockOverlay.kt`, `GameOverOverlay.kt`
  - HUD widgets: `HudWidgets.kt` (8 functions: `AltitudeDisplay`, `FuelGauge`, `HeatGauge`, `ShieldGauge`, `IntegrityGauge`, `ComboHudBar`, `NotificationLayer`, `ZoneDiscoveryCard`)
  - Shared components: `CodexCard.kt`, `Achievements.kt`
- Architecture report: `docs/architecture/Refactor_T1_Phase1.md`
- Phase 2 implementation plan: `docs/architecture/Refactor_T1_Phase2_Plan.md`

### Changed
- `GameScreen.kt`: removed inline `AchievementsList` val (moved to `Achievements.kt`)
- `GameScreen.kt`: removed inline `CodexCard` composable (moved to `CodexCard.kt`)
- Net reduction: 52 lines (4,344 → 4,292)

### Fixed
- Import errors in all 15 extracted files:
  - `graphicsLayer` wrong package path (`foundation.layout` → `ui.graphics`)
  - Missing imports: `border`, `shadow`, `CircleShape`, `statusBarsPadding`, `fillMaxHeight`, `offset`, `safeDrawingPadding`, `sp`
  - Invalid `roundToPx` import removed (function resolves via `Dp` member without explicit import)
  - Duplicate `dp` import in `TitleScreen.kt` removed

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL (zero errors, 2 pre-existing deprecation warnings)
- All 15 extracted files compile alongside `GameScreen.kt`
- Dex artifact produced without issues

### Notes
- GameScreen.kt still contains inline implementations for all screens, overlays, and HUD widgets. The extracted files are defined but not yet called. Phase 2 will perform the replacement.

---

## 2026-06-18

**Sprint / Phase:** Refactor Sprint T1 — Phase 2

**Branch:** `refactor/ui-extraction`

**Commit:** `686bfd0`

**Tags:** `refactor-t1-phase1`, `refactor-t1-phase2`

**Status:** Completed

### Added
- Phase 2 completion report: `docs/architecture/Refactor_T1_Phase2_Report.md`
- `refactor-t1-phase1` and `refactor-t1-phase2` git tags

### Changed
- `GameScreen.kt`: replaced all 7 screen branches with extracted composable calls (TITLE, MAIN_MENU, HANGAR, ARCHIVE, SETTINGS, ABOUT, LEADERBOARD)
- `GameScreen.kt`: replaced 6 HUD widgets with extracted calls (AltitudeDisplay, FuelGauge, HeatGauge, ShieldGauge, IntegrityGauge, ComboHudBar, NotificationLayer, ZoneDiscoveryCard)
- `GameScreen.kt`: deleted duplicate altitude display block
- `GameScreen.kt`: replaced 5 overlays with extracted calls (PauseOverlay, TutorialOverlay, HelpOverlay, UnlockOverlay, GameOverOverlay)
- Net reduction: 966 lines (4,292 → 3,326)

### Fixed
- `ComboHudBar` parameter type mismatch: `getWindowForCombo` required lambda wrapper `{ comboManager.getWindowForCombo(it) }`

### Removed
- `PowerupBadge` composable — unused (zero call sites, 23 lines)

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL (zero errors)
- Brace balance: 681 `{` = 681 `}` ✅
- APK installed and launched on emulator (`Medium_Phone API 35`)
- `adb shell monkey` — `Events injected: 1`

### Notes
- Mission row cards (~124 lines) and floating combo texts (~17 lines) remain inline — deferred past Phase 2
- `MissionType.toIcon()` extension kept — still called by inline mission cards
- Refactor Sprint T1 is fully complete. Branch ready to merge to `main`.

---

## 2026-06-18

**Sprint / Phase:** Refactor Sprint T2 — Phase A (Low Risk)

**Branch:** `refactor/ui-extraction`

**Base Commit:** `686bfd0` (T1 Phase 2)

**Status:** Completed

### Added
- `TopRightUtilityButtons.kt` — Help (`?`) + Pause (`||`) buttons (23 lines)
- `MissionRow.kt` — Mission card row with ceremony stages, progress bars, AnimatedContent (108 lines)
- `FloatingTextsLayer.kt` — Animated floating text overlay (16 lines)
- `LeftGauges` / `RightGauges` appended to `HudWidgets.kt` — Column wrappers for fuel/heat/shield/integrity gauges (22 lines)
- T2 plan: `docs/architecture/Refactor_T2_Plan.md`

### Changed
- `GameScreen.kt`: 5 inline blocks replaced with extracted composable calls
- Net reduction: 147 lines (3,326 → 3,179)

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL (11s, zero errors)
- ADB install — Success
- Emulator launch — Events injected: 1

### Notes
- All 4 extractions use explicit parameter patterns; `BoxScope`-dependent composables accept `Modifier` parameter
- T2B (Canvas effects, ~104 lines) is planned but not started
- Threat entity rendering (~826 lines) remains inline indefinitely

---

## 2026-06-18

**Sprint / Phase:** Refactor Sprint T2 — Phase B (Medium Risk)

**Branch:** `refactor/ui-extraction`

**Base Commit:** `2fe24f1` (T2A)

**Status:** Completed

### Added
- `CanvasEffects.kt` — 8 `DrawScope` extension functions for canvas rendering (168 lines)

### Changed
- `GameScreen.kt`: 8 inline Canvas blocks replaced with extracted function calls
- Net reduction: 70 lines (3,179 → 3,109)

### Extractions
- `drawRealityDistortion` — magenta overlay near Void Anomaly
- `drawSpeedLines` — vertical lines during fast descent
- `drawGround` — brown ground rectangle
- `drawParticles` — sparkle/circle particle rendering
- `drawLandingEffects` — expanding cyan ring circles
- `drawPowerUps` — colored shapes per powerup type
- `drawFlyingRewards` — animated reward items
- `drawImpactFlash` — white stroked border screen flash

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL (5s, zero errors)
- ADB install — Success
- Emulator launch — Success (PID running)

### Notes
- Refactor Sprint T2 is fully complete. T1 + T2 cumulative reduction: 4,344 → 3,109 lines (−28.4%)
- Threat entity rendering (~826 lines) remains inline indefinitely

---

## 2026-06-18

**Sprint / Phase:** Refactor Sprint T2 — Positioning Fixes + Merge

**Branch:** `refactor/ui-extraction`

**Base Commit:** `24a3eb9` (T2B)

**Status:** Completed

### Fixed
- `AltitudeDisplay` — added `modifier` parameter; caller passes `Modifier.align(Alignment.TopCenter)` for correct positioning
- `NotificationLayer` — added `modifier` parameter; positioned at `TopCenter.padding(top = 240.dp)`, text shrunk from `headlineSmall` → `bodyLarge`, `letterSpacing` reduced from `4.sp` → `2.sp`

### Changed
- `OPENCODE.md` — updated branch strategy, completed phases, and next tasks
- `docs/CHANGELOG.md` — added this entry

### Merged
- `refactor/ui-extraction` merged into `development` (`af3d0ae`)
- Git tags `refactor-t1-phase1`, `refactor-t1-phase2`, `refactor-t2` pushed to remote

---

## 2026-06-25

**Sprint / Phase:** EPIC 11 — Ascension (The End) — COMPLETE

**Branch:** `epic11-ascension`

**Status:** Complete ✅

### Added
- **PlayerInputProcessor.kt**: Extracted input logic from GameScreen.kt with `glitchFactor` hook for Singularity boss fight.
- **SingularityRenderer.kt**: Final boss renderer with white-noise core, geometric fragments, and reality rift lines.
- **AscensionOverlay.kt**: Full-screen ceremonial credits with Architect's Log and Hall of Pioneers.
- **Omega Modules**: `MOD_VOID_ENGINE` (infinite fuel) and `MOD_SINGULARITY_CORE` (perfect stability) — Legendary tier.
- **Origin Reset**: Coordinate normalization at 100,000m to prevent floating-point jitter.
- **Eternal Mode**: Capped infinite scaling beyond 100,000m (0.25s min interval, 4x max speed, overflow-safe at ~550,000m).
- **Prestige System**: Permanent +10% hull/shield multiplier per reset, unlocked at 100km.
- **Design Libraries**: Updated ROCKET_LIBRARY.md, AREA_LIBRARY.md, THREAT_LIBRARY.md, ARTIFACT_LIBRARY.md with EPIC 11 content.

### Changed
- **GameScreen.kt**: ASCENSION_PROTOCOL game state handling, origin reset logic, prestige wiring, ascension credits overlay.
- **HudWidgets.kt**: HUD pull animation via `graphicsLayer.translationX` using `hudPullFactor` from Singularity.
- **ThreatAIUpdater.kt**: Singularity AI with HUD Pull cycle (8s), slow tracking, instant DESTROYED on weak point kill.
- **ThreatRegistry.kt**: Registered BOSS_SINGULARITY with SINGULARITY zone.
- **ThreatRenderer.kt**: Registered SingularityRenderer in the registry.
- **ActiveThreat.kt**: Added `hudPullFactor` field for Singularity mechanic.

### Fixed
- **Boss death sequences**: Escape phase corrected per boss (Commander→5, others→4). Added `activeWeakPoints <= 0` guard in 7 boss AIs to prevent timer-based phase override from blocking escape.
- **Star Eater**: Suction reduced 3000→1500. Weak point moved from center to orbiting position (100px radius). Hit detection radius increased 80→120.
- **Boss pursuit speeds**: Increased tracking factors for Void Engine (0.2→0.4), Signal (0.05→0.15, 0.2→0.25), Architect (0.2→0.3), Entropy Core (0.1→0.2).
- **Fuel/Heat gauge alignment**: Numeric values padded to 3 characters to prevent layout shift at 100.
- **HUD zone-adaptive colors**: Removed from FuelGauge — now uses static SciFiGreen (→SciFiRed when low/critical), matching other gauges.
- **Platform colors**: Earth platforms now use warm brown (#795548), Cloud platforms use light cyan (#80DEEA) instead of plain white.
- **Hidden signal `isUnlocked` persistence**: `syncState()` now restores `isUnlocked` from SharedPrefs, fixing unlocked signals showing glitch effect after restart.
- **Claimable mission count**: Dashboard now shows "(N HIDDEN)" hint when claimable count includes hidden signals.

### Gameplay Balance
- **Eternal Mode**: Safe capped scaling prevents overflow at extreme altitudes.
- **Prestige System**: Reset grants +10% hull/shield per level, gated behind 100km completion.

### Technical Debt
- `GameScreen.kt` line count: ~1,943 lines (budget: 2,200) — headroom for future work.
- `PlayerInputProcessor.kt` extracted, reducing GameScreen complexity.

---

## Historical Milestones

The following milestones summarize completed work prior to the changelog's creation. These entries are reconstructed from commit history and were not recorded as changelog events at the time of completion.

### EPIC 4: The Ascension Program

| Sprint | Commit | Summary |
|--------|--------|---------|
| EPIC 4 Start | `e744ae0` | Missions, achievements, and progression system initiated. |
| Sprint A | `6235350` | Mission reliability and early-game feel improvements. |
| Sprint B | `1404aaa` | Combo Renaissance — combo system overhaul and scoring refinements. |
| Sprint C | `86ce562` | Mission UX and communication — mission cards, notifications, player feedback. |
| Sprint D | `17956c0` | Platform evolution — dynamic platform generation and difficulty scaling. |
| Sprint E | `a00c623` | Pre-release candidate stabilization before Ascension Program launch. |
| Sprint F | `fa3101b` | Ascension Program complete — full mission tree, achievement tracking, progression gates, and Codex system operational. |

**Scope:** The Ascension Program introduced the mission system (6 mission types with difficulty tiers), achievement tracking (6 achievements with unlock conditions), player progression gates (rocket unlocks at score thresholds), the Codex (discovery categories with lore entries and per-category completion tracking), and the Zone system (altitude-based zones with distinct visual themes).

### EPIC 5: Survival Protocol

| Sprint | Commit | Summary |
|--------|--------|---------|
| Sprint B | `aee2c37` | Threats, survival economy, and destruction system stable. |

**Scope (in progress):** Survival Protocol introduces the threat system (hostile entities with spawn rules and tier progression), survival economy (shield regeneration delay, hull integrity critical thresholds, overheat management), destruction sequence (3-phase visual breakup with debris and fire effects), and visual redesigns (shield energy armor plates, catastrophic breakup particles, loss-of-control tumble).

*Note: EPIC 5 Sprint A is not reflected in the commit history as a discrete milestone and was subsumed into the Sprint B delivery.*
