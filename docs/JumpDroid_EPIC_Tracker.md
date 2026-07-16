# Jump Droid - EPIC Tracking Sheet

## Status Legend

- [ ] Not Started
- [~] In Progress
- [x] Complete
- [!] Needs Rework
- [-] Deferred

---

# EPIC 5 - Survival Protocol (COMPLETE)

## Core Survival

- [x] Shield System (Segmented absorb layer)
- [x] Shield Regeneration (Regen delay after hit)
- [x] Shield Delay Logic (4s delay)
- [x] Shield Visuals (Armor plate overlay)
- [x] Hull Integrity (Primary life pool)
- [x] Hull Damage States (Critical thresholds)
- [x] Hull Destruction Sequence (2.5s physical breakup)
- [x] Damage Framework (Distributed logic)
- [x] Recovery Framework (Power-up spawning)

## Environmental Hazards (Full Catalog)

- [x] HAZ_RADIATION (Shield drain)
- [x] HAZ_SOLAR_FLARE (Heat wave)
- [x] HAZ_EMP (Regen pause)
- [x] HAZ_GRAVITY (Movement pull)
- [x] HAZ_VOID_ANOMALY (Distortion)
- [x] HAZ_LIGHTNING (Structural damage)
- [x] HAZ_TURBULENCE (Horizontal wind)
- [x] HAZ_DEBRIS (Collision damage)

## Status Effects (Implemented vs Planned)

- [x] Combo Freeze (Defense Node)
- [x] HUD Interference (Signal / Void Anomaly)
- [x] Control Inversion (Void Engine)
- [x] Engine Overheat Lockout (Core Heat system)
- [ ] Radiation Sickness (Future)
- [ ] Corruption Infection (Future)
- [ ] Shield Suppression (Planned)

## Survival Economy

- [x] Combo Rewards (Rewards at 5/10/15/20)
- [x] Shield Capsules
- [x] Hull Repair Drops
- [x] Emergency Fuel Drops
- [ ] Rare Survival Resources (Planned)

## Survival Balancing

- [x] Damage Tuning (Sprint C pass)
- [x] Shield Tuning
- [x] Hull Tuning
- [x] Reward Tuning

---

# EPIC 6 - Hostile Skies (COMPLETE)

## Enemy Systems

- [x] Enemy Framework (ActiveThreat delegation)
- [x] Enemy Spawning (EncounterDirector weights)
- [x] Threat Scaling (Zone-based difficulty multipliers)
- [x] Enemy Projectile System (BOLT, MISSILE, BEAM, WAVE)
- [x] Minion Spawning Architecture (onSpawnThreat)

## Friendly Entities

- [x] Sky Ray (ENT_CLOUD_SKIMMER - Updraft)
- [x] Derelict Echo (ENT_CORRUPTED_HULL - Salvage)

## Hostile Entities (Standard)

- [x] Surveyor Probe (ENT_SCOUT_DRONE - Scout behavior)
- [x] Aerosol Swarm (ENT_SWARM_BOTS)
- [x] Defense Node (ENT_ORBITAL_SENTRY - Ranged)
- [x] Void Tracker (ENT_STALKER - Ranged)
- [x] Shadow Entity (ENT_VOID_WRAITH)
- [x] Cosmic Leviathan (ENT_VOID_WHALE)

## Boss Encounters (The Guardians)

- [x] MINI_BOSS_COMMANDER (Complete)
- [x] BOSS_GATEKEEPER (Complete)
- [x] BOSS_LEVIATHAN (Complete)
- [x] BOSS_VOID_ENGINE (Complete)
- [x] BOSS_SIGNAL (Complete)
- [~] BOSS_STAR_EATER (Partial - Phase rewrite deferred)

---

# EPIC 7 - Rocket Evolution (COMPLETE)

- [x] Rocket Classes (Base 4 classes: Explorer, Striker, Heavy, Prototype)
- [x] Module Framework (Registry, Hooks, LoadoutManager)
- [x] Hull Modules (Reinforced Hull, Impact Dampeners, Self Repair Matrix)
- [x] Shield Modules (Fast Recharge, Emergency Shield, Reflective Shield)
- [x] Engine Modules (Burst Thrusters, Long Burn Thrusters, Vector Thrusters)
- [x] Heat Modules (Cooling Matrix, Thermal Battery, Heat Sink)
- [x] Exploration Modules (Survey Scanner, Artifact Locator, Threat Scanner)
- [x] Support Modules (Auto Repair Drone, Emergency Beacon)
- [x] Progression & Unlocks (Sprint 7.6)
- [x] Balance & Validation (Sprint 7.7)

---

# EPIC 8 - Missions & Progression

**Status: COMPLETE ✅**

# EPIC 8.5 - Architecture Decomposition & Mission Alignment

**Status: COMPLETE ✅** (Rollback tag: `epic8.5-aligned`, Commit: `9363434` + Mission Fixes)

- [x] Sprint 8.5.0: Baseline Capture
- [x] Sprint 8.5.1: Low-Risk Cleanup
- [x] Sprint 8.5.2: HUD Decomposition
- [x] Sprint 8.5.3: State/Ceremony Extraction
- [x] Sprint 8.5.4: Threat Rendering Extraction
- [ ] Sprint 8.5.5: Game Engine Boundary Creation — **DEFERRED to EPIC 12**
- [x] Sprint 8.5.6: ActiveThreat Decomposition
- [ ] Sprint 8.5.7: Progression Service Decomposition — **DEFERRED to EPIC 12**
- [ ] Sprint 8.5.8: Navigation Migration — **DEFERRED to EPIC 12**
- [x] Sprint 8.5.9: Mission Progression Alignment (Lifetime stats, bug fixes, new landing track)

## Post-8.5 File Sizes (Actual)

| File | Before | After | Reduction |
|------|--------|-------|-----------|
| GameScreen.kt | 3,901 lines | 2,011 | **48%** |
| ActiveThreat.kt | 1,224 lines | 123 | **90%** |
| HudWidgets.kt | 654 lines | 576 | 12% |
| GameEngine.kt | — | 110 (new) | N/A |

## EPIC 8 — Mission Framework (Deferred / Stretch Items)

**Status:** Core complete ✅ — items below are stretch goals for future EPICs

- [x] Mission System Recovery Audit
- [x] Mission Data Migration (Phase 1)
- [x] Intelligence Network (Phase 2)
- [x] Mission Reward Integration (Phase 3)
- [x] Mission Claim Flow (Phase 4)
- [x] Mission UX & Gameplay Communication (Phase 5)
- [x] Mission Reward Pipeline Alignment
- [x] Lifetime Statistic Integration (New!)

## Core Mission Tracks

- [x] Flight Time Missions (Aligned with lifetime)
- [x] Platform Stay Missions (Aligned with lifetime)
- [x] Fuel Efficiency Missions
- [x] No Heat Missions (Targeted reset logic)
- [x] Combo Missions
- [x] Discovery Missions
- [x] Altitude Missions
- [x] Boss Slayer Missions (Aligned with lifetime)
- [x] Momentum Missions
- [x] Hazard Survival Missions (Aligned with lifetime)
- [x] Perfect Run Missions
- [x] Collection Missions (Aligned with lifetime)
- [x] Platform Landing Missions (New Track!)

## Tiered Progression

- [ ] Tier 1 Mission Set
- [ ] Tier 2 Mission Set
- [ ] Tier 3 Mission Set
- [ ] Tier 4 Mission Set

## Hidden Missions

- [ ] Secret Mission Framework
- [ ] Hidden Achievement Missions
- [ ] Discovery-Based Unlocks

## Reward Systems

- [ ] Module Rewards
- [ ] Artifact Rewards
- [ ] Archive Rewards
- [ ] Cosmetic Rewards
- [ ] Fleet Expansion Rewards

## Integration

- [ ] Rocket Evolution Integration
- [ ] Archive Integration
- [ ] Discovery Integration
- [ ] Boss Reward Integration

## Existing Assets

- [x] Mission Design Document
- [x] 48 Tiered Mission Structure
- [x] Hidden Mission Concepts
- [x] Prototype UI Branch
- [x] Prototype Mission Manager
- [x] Prototype Persistence System

## Notes

Existing work located in: feature/mission-system
All core mission items implemented. Stretch items above deferred to future EPICs.

---

# EPIC 9 - Hidden Signals & Dynamic Unlocks

**Status: COMPLETE ✅**
*Execution Plan: [docs/roadmap/EPIC_9_EXECUTION_PLAN.md](roadmap/EPIC_9_EXECUTION_PLAN.md)*

- [x] Sprint 9.1: Glitch UI + Cryptic Hints + Reveal Ceremony
- [x] Sprint 9.2: Dynamic Unlock Engine (AND/OR, 17 modules)
- [x] Sprint 9.3: Artifact Set Bonuses
- [x] Sprint 9.4: Rare Lore Logs (10 logs, viewer)
- [x] Sprint 9.5: Blueprints & Stabilization

---

# EPIC 10 - The Outer Reaches (New Biomes)

**Status:** COMPLETE ✅
*Planning Document: [docs/roadmap/EPIC_10_PLANNING.md](roadmap/EPIC_10_PLANNING.md)*

- [x] Task 10.0: Infrastructure Refactor (Config Engine)
- [x] Sprint 10.1: Expansion of the Sky (4 New Zones)
- [x] Sprint 10.2: Ancient Structures & Flux Platforms
- [x] Sprint 10.3: Environmental Events System (Meteor Showers, etc.)
- [x] Sprint 10.4: Library Threats & Entities (7 New Threats)
- [x] Sprint 10.5: Library Platforms & Power-Ups (CONVEYOR, MIMIC, 3 New Power-Ups)
- [x] Sprint 10.6: Visual Fidelity, Bosses, Achievements & Lore Finale (renderer upgrades, 5 bosses, 2 artifacts + set bonus, 2 lore logs, 4 achievements, encounter integration, doc refresh)

---

# EPIC 11 - Ascension (The End)

**Status: COMPLETE ✅ — Merged to `development`**
*Execution Plan: [docs/roadmap/EPIC_11_EXECUTION_PLAN.md](roadmap/EPIC_11_EXECUTION_PLAN.md)*

## Sprint 11.0 — Infrastructure & Documentation
- [x] PlayerInputProcessor extraction (dedicated input processor with glitchFactor hook)
- [x] Design library updates (ROCKET_LIBRARY, AREA_LIBRARY, THREAT_LIBRARY, ARTIFACT_LIBRARY)
- [x] EPIC Tracker updated

## Sprint 11.1 — The Meta-Boss & Spatial Warping
- [x] Origin Reset Logic (coordinate normalization at 100,000m)
- [x] The Singularity AI (HUD Pull mechanic, slow tracking)
- [x] Control Glitch Hook (intermittent thrust drops + horizontal drift)
- [x] SingularityRenderer.kt (white-noise core, geometric fragments, reality rifts)
- [x] HUD Animations (graphicsLayer offsets in HudWidgets.kt)

## Sprint 11.2 — The Ascension Protocol (The Ending)
- [x] GameState.ASCENSION_PROTOCOL
- [x] AscensionOverlay.kt (Architect's Log + Hall of Pioneers credits)
- [x] DISCOVERY_THE_END and ART_ARCHITECT_SIGNATURE

## Sprint 11.3 — Prestige & Infinite Ascent
- [x] Prestige System (+10% hull/shield per reset, button in Main Menu at 100km)
- [x] Omega Modules (MOD_VOID_ENGINE — infinite fuel, MOD_SINGULARITY_CORE — perfect stability)
- [x] Eternal Mode (capped scaling beyond 100,000m, 0.25s min interval, 4x max speed)

## Sprint 11-BG — Visual Enhancement & Bug Fixes
- [x] Boss death sequences fixed (escape phase per boss + AI timer guard)
- [x] Star Eater rebalanced (suction 3000→1500, weak point orbiting, hit radius 80→120)
- [x] Boss pursuit speeds increased (Void Engine, Signal, Architect, Entropy Core, Signal P3)
- [x] Fuel/Heat gauge alignment padded to 3 digits
- [x] HUD zone-adaptive colors removed from FuelGauge
- [x] Platform colors for Earth/Cloud zones (brown/cyan instead of plain white)
- [x] Hidden signal isUnlocked persistence fixed in syncState()
- [x] Claimable dashboard now shows hidden count hint

---

# Release Polish — Pre-Ship Sprint

**Status:** **v1.5.2 RELEASED** ✅ — Closed Beta Release
**Plan:** [docs/roadmap/RELEASE_POLISH_PLAN.md](roadmap/RELEASE_POLISH_PLAN.md)

## Phase 1 - Core Game Feel (COMPLETE)
- [x] 2. Power-up spawn redesign
- [x] 3. Combo reward animation
- [x] 4. Boss death sequence
- [x] 5. Mission completion celebration
- [x] 16. Rewarded continue UX

## Phase 2 - Notification Architecture (COMPLETE)
- [x] 8. Notification system audit (3-tier priority)
- [x] 9. Unified notification area
- [x] 10. Improve gameplay notifications
- [x] 11. Remove weak notifications
- [x] 17. Banner ad placeholder UI

## Phase 3 - Tutorial Removal + Discovery (COMPLETE)
- [x] 1. Remove tutorial pop-ups
- [x] 7. Move learning to archives
- [x] 12. Platform discovery messages
- [x] 13. Unlock celebration

## Phase 4 - Data Archives + Monetization (COMPLETE)
- [x] 6. Redesign Data Archives
- [x] 18. Premium purchase + SDK integration (Test Ads Wired)

## Phase 5 - Audio Pass & Haptics (COMPLETE)
- [x] 14. Audio pass (production assets loaded, volume normalized, 12-zone BGM mapping, material-based landing SFX)
- [x] 19. Haptic feedback (wiring complete, GameEngine activated, boosted haptics)

## Phase 6 - Archive Entity Detail Popups (COMPLETE)
- [x] 20. Entity detail popup + preview renders (full lore registry, animated previews, glitch-noise detail popups)

## Phase 7 - Release Preparation (COMPLETE)
- [x] Bug bash: hitbox/WP fixes applied (collision radius 28f, per-WP wpDestroyedMask, 11 bosses aligned)
- [x] Cloud/Earth visual polish (dark purple gradient, golden hour Earth, purple-blue storm clouds)
- [x] Alarm loop fix (all 3 death paths: applyDamage, destruction timer, fell off screen)
- [x] Shield hit sound + haptics for HAZ_RADIATION / BOSS_ENTROPY_CORE drain bypass
- [x] Heat Bat always-visible cyan aura + damage rebalance (10/5)
- [x] Lightning damage rebalanced (25→13, ~26% base shield)
- [x] Boss HP/WP scaled by difficultyMultiplier
- [x] wpInvulnerabilityTimer separated from body invulnerability
- [x] Boss balance: WP cooldown 0.25s, WP hit radius 45f
- [x] Billing integration: PurchaseManager.kt rewritten with BillingClient + fallback dialog
- [x] ShopScreen: currency exchange with premium purchase card, cash balance, V2 placeholders
- [x] ProgressionManager: totalCash persistence with grantReward accumulation
- [x] SettingsScreen: premium no-toggle-off, disabled ADS REMOVED ✓ state, RESET PROGRESS / FACTORY RESET buttons
- [x] MainActivity/MainMenu: shop route + SHOP button added
- [x] AboutScreen: updated to V1.5.0 with cash balance display
- [x] Shield Platform (STABILITY→SHLD): velocityY=0f, shield fully restored, "SHIELDS RESTORED" text
- [x] Conveyor Platform fix: velocityX=150f continuous push (no bounce)
- [x] Zone jump freeze fix: jumpToZone() full cleanup matching restartGame()
- [x] Zone change notification: TACTICAL notification + ZoneDiscoveryCard auto-fade
- [x] Multi-hit WPs: wpHitCounts tiered by difficulty (1-4 hits per WP), partial-hit purple burst
- [x] Heat Bat visibility: cyan aura 0.06→0.15, wing-beat shadow 0.08→0.15, eye glow 0.5→0.7, white silhouette outline
- [x] Boss kill score removed: all 3 onScoreUpdate(1000) calls removed — score = altitude only
- [x] Milestone threshold rebalance: 8 thresholds increased for even late-game spacing
- [x] One boss per frame guard + no boss while boss alive check
- [x] Boss Recurrence system: ~3s timer, previously-defeated bosses + zone-eligible mini-bosses, 5-25% chance, 1.3× difficulty
- [x] Boss music fix: setBossActive checks BOSS || MINI_BOSS (4 mini-bosses now play bgm_boss)
- [x] Hazard suppression 0.3f→0.1f during bosses, Solar Flare filtered out
- [x] Data reset safety: RESET PROGRESS (preserves premium) + FACTORY RESET (wipes all)
- [x] Dev menu gated on BuildConfig.DEBUG: cheatsEnabled = BuildConfig.DEBUG
- [x] Play Store purchase gating: debug=confirm dialog, release="PLAY STORE REQUIRED" info dialog
- [x] ThreatRegistry.getEntries() added for recurrence pool filtering
- [-] Performance profiling: frame drops in upper zones / dense threat fields (deferred to EPIC 12)
- [-] Play Store listing prep: screenshots, description, assets (deferred to EPIC 12)
- [-] Final APK build + testing (deferred to EPIC 12)

## Phase 8 - Firebase Integration & Quality Assurance (COMPLETE)
- [x] Firebase Analytics integration (GameAnalytics domain abstraction)
- [x] Firebase Crashlytics integration (auto-init, debug-gated test crash)
- [x] GameAnalytics layer: domain-driven strongly-typed events
- [x] AdConfig: centralized unit IDs with debug/release switching
- [x] AdMob hardening (GlobalAdBanner, RewardedAdHelper migrated to AdConfig)
- [x] High-value instrumentation (game_start, game_over, zone_changed, boss_encounter, ad_impression, etc.)
- [x] Dependency injection via LocalAnalytics CompositionLocal

## Release Engineering (COMPLETE)
- [x] Repository audit + cleanup (dead code, version fix, redirect stubs, root clutter)
- [x] Documentation reconciliation (AGENTS, CHANGELOG, README, INVENTORY, EPIC_Tracker, RELEASE_POLISH_PLAN)
- [x] Merge refactor/cleanup → development → master
- [x] Tag v1.5.0
- [x] GitHub Release published
- [x] Signed Play Store AAB produced

---

# Analytics V0 — Beta Tester Analytics (IMPLEMENTED)

**Version:** v1.5.2 (versionCode 6)
**Primary Doc:** `docs/ANALYTICS.md` (Beta Analytics V0 section)
**Migration Guide:** See `docs/ANALYTICS.md` → Migration Guide

## Beta Tester Tracking (v1.5.2)
- [x] Firestore `testers/{sanitizedEmail}` collection with 15 fields
- [x] Firestore `testers/{sanitizedEmail}/sessions/{id}` per-session history
- [x] First-launch registration dialog (email required, name/phone optional)
- [x] Email validation via `Patterns.EMAIL_ADDRESS`
- [x] SharedPreferences persistence (never ask again)
- [x] `PlayerAnalyticsManager` decorator on `GameAnalytics`

## Gameplay Telemetry
- [x] Session gameplay time via game start/stop timers
- [x] Today gameplay time (daily reset)
- [x] Session count tracking
- [x] App open time (foreground/background lifecycle)

## Data Reuse
- [x] `totalGameplayTime` from Intelligence Network (`stat_lifetime_flight_time`)
- [x] `highestScore` from Intelligence Network (`highScore`)
- [x] Ad counters via `logAdImpression`/`logAdClicked` interception

## Future (Analytics V2)
- [ ] Parameter symmetry fix (`mission_category` in `mission_completed`, `boss_name` in `boss_defeated`)
- [ ] Rewarded ad impression timing (move to real impression callback)
- [ ] Overlay screen tracking (Pause, Help, Tutorial, GameOver, etc.)
- [ ] GDPR privacy consent dialog
- [ ] Firebase user properties (`player_id`, `tester_cohort`)

---

# Analytics & Ads Governance

| System | Primary Document |
|--------|-----------------|
| **Event Catalog** | `docs/ANALYTICS.md` |
| **Full Audit** | `docs/analysis/ANALYTICS_AUDIT.md` |
| **AdMob Config** | `AdConfig.kt` |
| **Analytics Interface** | `GameAnalytics.kt` |

---

# EPIC 12 - Fleet Expansion

**Status:** Planned

## Fleet Framework

- [ ] Rocket Class Framework (Explorer, Striker, Heavy, Prototype)
- [ ] Multiple Chassis per Class
- [ ] Chassis Selection System
- [ ] Chassis Progression System
- [ ] Fleet Collection System

## Explorer Fleet

- [ ] Pathfinder
- [ ] Nomad
- [ ] Surveyor

## Striker Fleet

- [ ] Interceptor
- [ ] Raptor
- [ ] Phantom

## Heavy Fleet

- [ ] Atlas
- [ ] Bulwark
- [ ] Leviathan

## Prototype Fleet

- [ ] X-01
- [ ] X-07
- [ ] Singularity

## Progression

- [ ] Chassis Unlock Progression
- [ ] Fleet Collection Rewards
- [ ] Fleet Mastery System
- [ ] Fleet Completion Achievements

## Cosmetics

- [ ] Class-Specific Visual Themes
- [ ] Alternate Hull Configurations
- [ ] Engine Trail Customization
- [ ] Paint Scheme System
- [ ] Premium Cosmetic Variants

## Design Rules

- [ ] Classes define gameplay identity
- [ ] Chassis define stat profiles
- [ ] Modules define customization
- [ ] No pay-to-win progression
- [ ] All gameplay-affecting ships unlockable through play

## Dependencies

Requires:
- EPIC 7 - Rocket Evolution
- EPIC 8 - Missions & Progression
- EPIC 9 - The Archive Expands
- EPIC 10 - The Outer Reaches
- EPIC 11 - Ascension

Track only. Do not implement.

---

# Tasks Discovered During Development (Sprint C Archive)

- [x] **Structural AI Fix**: Decoupled Boss update loops from Enemy loops to allow complex AI to run.
- [x] **Escalation Network**: Implemented Scout Drone -> EncounterDirector -> Hazard summons logic.
- [x] **Zone Gating**: Milestone bosses now respect `allowedZones` for both milestone and fallback spawns.
- [x] **Cloud Spawn Reduction**: Tuned early game difficulty by reducing Commander fallback spawns in Cloud Layer by 60%.
- [x] **Intensity Scaling**: EncounterDirector now applies `intensityFactor` to global spawn timers and caps.
- [x] **Callback API**: Added `onSpawnProjectile` and `onSpawnThreat` to decouple logic from the renderer.
- [x] **Weak Point Mechanics**: Documented the collision-based attack system in the Boss Design Bible.
