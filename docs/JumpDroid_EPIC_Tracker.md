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

# EPIC 8.5 - Architecture Decomposition

Status: Planned (EPIC 9 deferred until completion)
Detailed Plan: [docs/roadmap/EPIC_8_5_DECOMPOSITION_PLAN.md](roadmap/EPIC_8_5_DECOMPOSITION_PLAN.md)

- [ ] Sprint 8.5.0: Baseline Capture (tag, build verify, file metrics, screenshots, baseline report)
- [ ] Sprint 8.5.1: Low-Risk Cleanup (dead code, zombie fields, pure functions)
- [ ] Sprint 8.5.2: HUD Decomposition (GaugeBar, Starfield, MissionScreen extract)
- [ ] Sprint 8.5.3: Notification & Celebration Extraction (ceremony lifecycle, ComboManager)
- [ ] Sprint 8.5.4: Threat Rendering Extraction (per-type ThreatRenderer interface)
- [ ] Sprint 8.5.5: Game Engine Boundary Creation (physics loop, update orchestration)
- [ ] Sprint 8.5.6: ActiveThreat Strategy Architecture (ThreatBehavior, CollisionSystem, event bus)
- [ ] Sprint 8.5.7: Progression Service Decomposition (IntelligenceNetwork, UnlockService, MissionTracker)
- [ ] Sprint 8.5.8: Navigation Migration (NavHost, 14 screen routes)

## Post-8.5 Target File Sizes
| File | Before | After |
|------|--------|-------|
| GameScreen.kt | 3,901 lines | ~1,500 |
| ActiveThreat.kt | 1,224 lines | ~250 |
| ProgressionManager.kt | 334 lines | ~80 (facade) |
| HudWidgets.kt | 654 lines | ~400 |

## Mission Framework

- [x] Mission System Recovery Audit
- [x] Mission Data Migration (Phase 1)
- [x] Intelligence Network (Phase 2)
- [x] Mission Reward Integration (Phase 3)
- [x] Mission Claim Flow (Phase 4)
- [x] Mission UX & Gameplay Communication (Phase 5)
- [ ] Mission Reward Pipeline

## Core Mission Tracks

- [ ] Flight Time Missions
- [ ] Platform Stay Missions
- [ ] Fuel Efficiency Missions
- [ ] No Heat Missions
- [ ] Combo Missions
- [ ] Discovery Missions
- [ ] Altitude Missions
- [ ] Boss Slayer Missions
- [ ] Momentum Missions
- [ ] Hazard Survival Missions
- [ ] Perfect Run Missions
- [ ] Collection Missions

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

# EPIC 9 - The Archive Expands

**Status: DEFERRED — will commence after EPIC 8.5 Architecture Decomposition completes**

- [ ] Artifact Set Bonuses
- [ ] Discovery Collection Rewards
- [ ] Rare Lore Logs
- [ ] Lost Technology Blueprints
- [ ] Archive Completion Milestones
- [ ] Expedition-Specific Objectives

---

# EPIC 10 - The Outer Reaches (New Biomes)

- [ ] Additional Altitude Zones
- [ ] Deep Space Sectors
- [ ] Ancient Sky Structures
- [ ] Unique Environmental World Events
- [ ] Rare Entity Encounters
- [ ] Narrative Story Discoveries

---

# EPIC 11 - Ascension (The End)

- [ ] Final Peak Zones
- [ ] Legendary Relics
- [ ] Ultimate Boss Encounter
- [ ] Mastery Challenge Ranks
- [ ] 100% Completion Ceremony
- [ ] Ascension Prestige Events

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
