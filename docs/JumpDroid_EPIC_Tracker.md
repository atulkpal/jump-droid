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

# EPIC 7 - Rocket Evolution (IN PROGRESS)
*Detailed Tracker: [docs/roadmap/EPIC_7_TRACKER.md](roadmap/EPIC_7_TRACKER.md)*

- [x] Rocket Classes (Base 4 classes: Explorer, Striker, Heavy, Prototype)
- [x] Module Framework (Registry, Hooks, LoadoutManager)
- [x] Hull Modules (Reinforced Hull, Impact Dampeners, Self Repair Matrix)
- [x] Shield Modules (Fast Recharge, Emergency Shield, Reflective Shield)
- [x] Engine Modules (Burst Thrusters, Long Burn Thrusters, Vector Thrusters)
- [x] Heat Modules (Cooling Matrix, Thermal Battery, Heat Sink)
- [x] Exploration Modules (Survey Scanner, Artifact Locator, Threat Scanner)
- [x] Support Modules (Auto Repair Drone, Emergency Beacon)
- [ ] Progression & Unlocks (Sprint 7.6)
- [ ] Balance & Validation (Sprint 7.7)

---

# EPIC 8 - Missions & Progression

Status: Functionally Complete — Awaiting Runtime Signoff
Migration Branch: `epic8-mission-migration`
Detailed Tracker: [docs/roadmap/EPIC_8_TRACKER.md](roadmap/EPIC_8_TRACKER.md)

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

## Next (EPIC 9 — Hidden Signals & Dynamic Unlocks)

**Status: IN PROGRESS** ~

## Mission Framework

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
Recover and integrate after EPIC 7 completion.
Do not implement during EPIC 7. Track only.

---

# EPIC 9 - Hidden Signals & Dynamic Unlocks

**Status: COMPLETE ✅**
*Execution Plan: [docs/roadmap/EPIC_9_EXECUTION_PLAN.md](roadmap/EPIC_9_EXECUTION_PLAN.md)*
*Detailed Tracker: [docs/roadmap/EPIC_9_TRACKER.md](roadmap/EPIC_9_TRACKER.md)*

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

**Status: COMPLETE ✅**
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

# EPIC 12 - Fleet Expansion

Status: Planned

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
