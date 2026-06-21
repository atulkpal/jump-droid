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

- [x] Surveyor Probe (ENT_SCOUT_DRONE - Scout behavior)
- [x] Sky Ray (ENT_CLOUD_SKIMMER - Updraft)
- [x] Derelict Echo (ENT_CORRUPTED_HULL - Salvage)

## Hostile Entities (Standard)

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

# EPIC 7 - Rocket Evolution (NEXT)

- [x] Rocket Classes (Base 4 classes: Balanced, Scout, Tank, Experimental)
- [ ] Hull Upgrade Modules
- [ ] Shield Capacitor Upgrades
- [ ] Engine Performance Tuning
- [ ] Fuel Injection Systems
- [ ] Heat Sink Management
- [ ] Utility Slot Modules
- [ ] Cosmetic Skins / Trails

---

# EPIC 8 - The Archive Expands

- [ ] Artifact Set Bonuses
- [ ] Discovery Collection Rewards
- [ ] Rare Lore Logs
- [ ] Lost Technology Blueprints
- [ ] Archive Completion Milestones
- [ ] Expedition-Specific Objectives

---

# EPIC 9 - The Outer Reaches (New Biomes)

- [ ] Additional Altitude Zones
- [ ] Deep Space Sectors
- [ ] Ancient Sky Structures
- [ ] Unique Environmental World Events
- [ ] Rare Entity Encounters
- [ ] Narrative Story Discoveries

---

# EPIC 10 - Ascension (The End)

- [ ] Final Peak Zones
- [ ] Legendary Relics
- [ ] Ultimate Boss Encounter
- [ ] Mastery Challenge Ranks
- [ ] 100% Completion Ceremony
- [ ] Ascension Prestige Events

---

# Tasks Discovered During Development (Sprint C Archive)

- [x] **Structural AI Fix**: Decoupled Boss update loops from Enemy loops to allow complex AI to run.
- [x] **Escalation Network**: Implemented Scout Drone -> EncounterDirector -> Hazard summons logic.
- [x] **Zone Gating**: Milestone bosses now respect `allowedZones` for both milestone and fallback spawns.
- [x] **Cloud Spawn Reduction**: Tuned early game difficulty by reducing Commander fallback spawns in Cloud Layer by 60%.
- [x] **Intensity Scaling**: EncounterDirector now applies `intensityFactor` to global spawn timers and caps.
- [x] **Callback API**: Added `onSpawnProjectile` and `onSpawnThreat` to decouple logic from the renderer.
