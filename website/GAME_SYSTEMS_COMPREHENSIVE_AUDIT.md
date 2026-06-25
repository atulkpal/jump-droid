# Jump Droid: Comprehensive Game Systems Audit
**Date:** 2026-06-23  
**Status:** IMPLEMENTED FEATURES ONLY  
**Focus:** Actual gameplay mechanics and systems in the codebase

---

## Executive Summary

Jump Droid is a **vertical exploration and survival platformer** (not an endless score chaser). Players pilot experimental rockets through six altitude zones, managing physics-based resources (fuel, heat, shield, hull) while surviving environmental hazards, enemies, and boss encounters. The core fantasy: *"What is above me? What have I not discovered?"*

**Core Loop:** Touch to thrust → manage resources → land on platforms → build combo → ascend higher → survive threats → unlock discoveries.

**Current Implementation Status:** All core systems implemented and playable. EPIC 7 (Rocket Evolution) complete. Advanced features (Chrono-Rift, future zones) planned but not in codebase.

---

## 1. Core Gameplay Loop

### Main Game States
- **TITLE**: Cinematic drone patrol with radar scanning effects
- **MAIN_MENU**: Command center (continues, new game, settings)
- **HANGAR**: Rocket selection and loadout
- **LOADOUT**: Module equipment (2-slot system)
- **PLAYING**: Active gameplay with HUD
- **GAMEOVER**: Results screen with stats
- **ARCHIVE**: Codex viewer (43 entries)
- **TUTORIAL/HELP**: Control guides

### Per-Frame Loop Execution Order
```
1. Calculate dt (delta time)
2. Update utility managers:
   - NotificationManager (alert queue)
   - FloatingTextManager (popup damage numbers, warnings)
   - ProjectileManager (ranged attacks)
   - InputBufferManager (time-delayed inputs for chrono effects)
   - DiscoveryManager (discovery event timers)
   - ComboManager (combo tracking)
   - MissionManager (objective cycling)

3. Update Director & Environment:
   - EncounterDirector (threat spawning rules, boss milestones)
   - ThreatManager (enemy AI, hazard movement)
   - AmbientManager (atmospheric effects)

4. Survival System Update:
   - SurvivalManager (damage distribution, shield regen, hull tracking)

5. PHYSICS LOOP (4 sub-steps per frame):
   a. Update platform positions (moving platforms)
   b. Apply player input → thrust calculation with rocket modifiers
   c. Apply physical tether constraints (if active)
   d. Projectile collision detection
   e. Threat interaction (damage, knockback, effects)
   f. Power-up collision & magnetic pull
   g. AABB collision detection (player ↔ platforms)
   h. Apply gravity, thrust, steering, air friction, damping

6. Score calculation & zone transition detection
7. Platform lifecycle (recycle old, generate new)
8. Visual effects lifecycle (particles, landing effects, ceremonies)
9. Canvas rendering (background, particles, threats, platforms, HUD)
```

### Basic Input Model
- **Touch anywhere on screen** = Thrust direction (up to 2s max per hold)
- **Touch release** = Stop thrusting
- **Screen boundaries** = Safe zones for directional thrust (edges auto-steer left/right)
- **Input Buffer** (dev feature): Can delay input response by N seconds for experimental time-manipulation zones

---

## 2. Progression Mechanics

### Progression System: Ascension Ranks

**Rank Calculation:**
- Total Discovery score = (# discoveredItems) + (# artifacts × 3) + (# zones × 5)
- **Explorer I**: 0+ points (default)
- **Explorer II**: 10+ points
- **Explorer III**: 25+ points
- **Explorer IV**: 40+ points
- **Explorer V**: 60+ points

**Permanent Upgrades:**
- Rank progression grants permanent baseline improvements to survival stats
- Artifacts collected unlock permanent modules in progression system
- Mission completion tracked globally

### High Score Tracking
- Per-run maximum altitude is score (0–25000+)
- Top score persists across sessions
- Leaderboard screen displays high score with zone reached

### Rocket Class Unlocks

| Class | Unlock Trigger | Thrust | Fuel | Heat | Native Trait | Unlocked Score |
|-------|---|---|---|---|---|---|
| **Explorer** (Balanced) | Default | 1.0x | 1.0x | 1.0x | Sensor Array (+20% discovery range) | — |
| **Striker** (Scout) | Reach 2000 score | 1.25x | 0.7x | 0.9x | Target Lock (weak point precision) | 2000 |
| **Heavy** (Tank) | Reach 5000 score | 0.85x | 1.5x | 0.8x | Kinetic Mass (impact shockwaves) | 5000 |
| **Prototype** (Experimental) | Reach 10000 score | 1.5x | 1.0x | 1.4x | Overclocked Core (steer in overheat) | 10000 |

---

## 3. Unique Mechanics: Fuel, Heat, & Physics

### Fuel System
- **Base Capacity:** 100 units
- **Upgrade Max:** 300 units
- **Consumption Rate:** 27 units/second of active thrust (3x more efficient than early Sprint E balance)
- **Recharge Rate:** 40 units/second when not thrusting
- **Rocket Multipliers:**
  - Scout: 0.7× capacity (70 units base)
  - Tank: 1.5× capacity (150 units base)
  - Experimental: 1.0× capacity (100 units base)
- **Recovery Sources:** Fuel platforms (+50 units), fuel tank power-ups, on-platform idle
- **Fuel Mechanics:** Thrust duration is directly limited by current fuel; running out stops all upward acceleration

### Heat System
- **Base Capacity:** 100 units
- **Upgrade Max:** 300 units (future-proofed)
- **Generation Rate:** 13 units/second of active thrust (3x slower than early Sprint E balance)
- **Cooling Rate:** 20 units/second when not thrusting
- **Overheat Lockout:** When heat ≥ 100, engine shuts down for 2 seconds
- **Overheat Penalty:** Velocity control locks (steering blocked for most rockets)
- **Rocket Multipliers:**
  - Scout: 0.9× heat generation (11.7 units/sec)
  - Tank: 0.8× heat generation (10.4 units/sec)
  - Experimental: 1.4× heat generation (18.2 units/sec)
- **Recovery Sources:** Cooling platforms (−30 heat), heat sink power-ups (−100 instant), idle passive regen

### Physics-Based Movement

#### Base Physics Constants
- **Gravity:** 1800 units/s² (constant downward acceleration)
- **Base Thrust Power:** 4000 units/s² (acceleration when fuel available)
- **Rocket Size:** 40×70 pixels
- **Horizontal Damping:** 0.85× per physics substep (air resistance)
- **Air Friction:** 0.98× per frame (velocity decay)
- **Landing Bounce Velocity:** −150 px/s (upward recovery on platform land)

#### Movement Mechanics
1. **Thrust** = Player applies upward acceleration (limited by fuel/heat)
2. **Steering** = Horizontal input at screen edges applies counter-rotation
3. **Gravity** = Constant 1800 units/s² pull downward
4. **Air Friction** = Velocity multiplied by 0.98 each frame
5. **Platform Inheritance** = Landing on moving platform copies platform velocity
6. **Bounce Recovery** = Landing applies −150 px/s upward impulse for repositioning

#### Advanced Physics
- **Tether/Joint Physics:** Player can be anchored to world points; creates tension-based restoring force (electrical visual effect)
- **Platform Magnetic Fields:** Magnetic platforms create proximity-based attraction/repulsion
- **Projectile Collision:** Circle-to-AABB collision detection for incoming fire
- **Entity Knockback:** Threats and hazards apply directional force on contact

#### Steering Authority
- **Explorer/Striker/Tank:** Full steering control in normal flight
- **Experimental (Overclocked Core):** Can maintain steering authority while overheated (unique trait)
- **Stability Platform Buff:** 10-second duration of improved steering (multiplier-based)

---

## 4. Rocket Classes & Their Differences

### 1. Explorer (Balanced Rocket)
- **Visual:** Sleek white fuselage, red fins, cyan cockpit
- **Strengths:** No weaknesses; starting class baseline
- **Weaknesses:** No exceptional strengths
- **Trait:** **Sensor Array** — Native +20% discovery range (artifacts visible at greater altitude distance)
- **Play Style:** Balanced approach; suitable for learning mechanics
- **Unlock:** Default

### 2. Striker (Scout Rocket)
- **Visual:** Slender gold fuselage, aggressive fin geometry
- **Strengths:** 1.25× thrust (rapid ascent), 0.9× heat generation (cooler operation)
- **Weaknesses:** 0.7× fuel capacity (shorter runs without refuel stops)
- **Trait:** **Target Lock** — Precision strikes on boss weak points (tracking reticle, easier weak point hits)
- **Play Style:** Aggressive, speedrun-focused; skilled pilot required
- **Unlock:** Score 2000

### 3. Heavy (Tank Rocket)
- **Visual:** Heavy industrial gray fuselage, reinforced plating
- **Strengths:** 1.5× fuel capacity (extended range), 0.8× heat generation (efficient)
- **Weaknesses:** 0.85× thrust (sluggish acceleration), heavier steering feel
- **Trait:** **Kinetic Mass** — Impact shockwaves on weak point destruction (damages nearby threats)
- **Play Style:** Endurance-focused; long climbs, attrition-based survival
- **Unlock:** Score 5000

### 4. Prototype (Experimental Rocket)
- **Visual:** Glossy purple hull, glowing plasma conduits
- **Strengths:** 1.5× thrust power (extreme acceleration), maintains steering while overheated
- **Weaknesses:** 1.4× heat generation (requires thermal management), dangerous for new players
- **Trait:** **Overclocked Core** — Retain steering authority during overheat lockout (violates normal physics constraints)
- **Play Style:** High-risk/high-reward; mastery of heat timing required
- **Unlock:** Score 10000

---

## 5. Boss System: Phases, Mechanics, & Weak Points

### Boss Spawn Milestones
Bosses spawn at altitude thresholds and are tied to score progression. Each zone may contain multiple bosses based on player progression.

| Boss ID | Name | Spawn Score | Zone | Phases | Weak Points |
|---------|------|---|---|---|---|
| `MINI_BOSS_COMMANDER` | Command Cruiser | 1500 | Cloud+ | 3+ | 3 (rotating squares) |
| `BOSS_GATEKEEPER` | The Gatekeeper | 4000 | Orbit+ | 3 | 4 (ring nodes) |
| `BOSS_LEVIATHAN` | The Leviathan | 7000 | Deep Space+ | 3 | 3 (body segments) |
| `BOSS_STAR_EATER` | Star-Eater | 10000 | Deep Space | 3 | 1 (central eye) |
| `BOSS_VOID_ENGINE` | Void Engine | 15000 | Void | 3 | 2 (arm tips) |
| `BOSS_SIGNAL` | The Signal | 18000 | Void | 3 | 1 (glitching core) |

### Detailed Boss Mechanics

#### 1. Command Cruiser (MINI_BOSS_COMMANDER)
**Status:** Complete  
**Difficulty:** ⭐⭐⭐ (Medium)

| Phase | Mechanic | Challenge |
|-------|----------|-----------|
| **P1** | Platform Jamming + Radar Scan | Platforms become impassable; must navigate around or destroy turret |
| **P2** | 3-way BOLT projectile spreads (every 2s) | Projectiles must be dodged or blocked; pattern spacing increases |
| **P3+** | Combines all + Gravity Pulse | Adds downward force; projectile frequency increases |

**Weak Points:** 3 (magenta squares on hull, rotating cyan beacons)  
**Strategy:** Focus weak points → reduce projectile frequency → avoid gravity pulse → finish off

---

#### 2. The Gatekeeper (BOSS_GATEKEEPER)
**Status:** Complete  
**Difficulty:** ⭐⭐⭐ (Medium)

| Phase | Mechanic | Challenge |
|-------|----------|-----------|
| **P1** | Rotating energy shield with safe gaps | Green safe zones rotate; must time passage through openings |
| **P2** | Fires 3-way BOLT patterns (every 2s) | Projectiles at variable heights; pattern becomes tighter as weak points are destroyed |
| **P3** | Spawns Scout Drones for support (every 5s) | Adds another threat layer; must balance drone destruction with weak point focus |

**Weak Points:** 4 (cyan shield rings on orbital path)  
**Visual:** Massive rotating ring with ghost afterimage from high RPM; solid energy barrier walls; push-back force lines  
**Strategy:** Identify safe gaps in phase 1 → dodge projectiles while hitting weak points → manage drones in phase 3

---

#### 3. The Leviathan (BOSS_LEVIATHAN)
**Status:** Complete  
**Difficulty:** ⭐⭐⭐⭐⭐ (Very Hard)

| Phase | Mechanic | Challenge |
|-------|----------|-----------|
| **P1** | S-curve movement (VOID_SERPENT behavior) | Tail segments 4–5 deal 3× knockback damage vs other segments |
| **P2** | Screen shrink (playable area contracts) | Proportional to weak points remaining; player pushed inward from screen margins |
| **P3** | Maw core (80px zone) + wall pressure | Standing in maw core rapidly heats player; screen edge applies massive downward force |

**Weak Points:** 3 segments (even-numbered body segments 0, 2, 4; tail segments 4–5 deal 3× damage on destruction)  
**Visual:** 6 organic ellipse segments with armor plating, bioluminescent vein patterns, directional slipstream arrows, red eye on head  
**Strategy:** Chase tail in P1 → stay centered in P2 to maintain screen space → avoid maw core in P3 → timing-based weak point destruction

---

#### 4. Star-Eater (BOSS_STAR_EATER)
**Status:** Partial Implementation  
**Difficulty:** ⭐⭐⭐⭐ (Hard)

| Phase | Mechanic | Challenge |
|-------|----------|-----------|
| **P1** | Rapid health regeneration | Must use max-charged Momentum dash to break regen cycle (or sustained high damage) |
| **P2** | Solar flare nova wave | Hide behind debris to avoid massive heat spike (~50+ heat damage) |
| **P3** | Splits into two flaming birds | Both must be destroyed within 5 seconds or respawn; divides player attention |

**Weak Points:** 1 (central magenta eye with white pupil)  
**Visual:** Black/purple suction aura, 16 energy teeth around maw rim, pulsing core, dark tendrils that glow near player  
**Strategy:** Save momentum charge for P1 → memorize debris spawn locations for P2 → target both birds equally in P3

---

#### 5. Void Engine (BOSS_VOID_ENGINE)
**Status:** Complete  
**Difficulty:** ⭐⭐⭐⭐ (Hard)

| Phase | Mechanic | Challenge |
|-------|----------|-----------|
| **P1** | Lateral gravity shifts + Void Anomaly summoning (every 5s) | Screen-wide gravity pushes sideways; must avoid anomalies and counter gravity |
| **P2** | Downward gravity wells (4800 f/s² constant) | Must boost upward to counter; projectile patterns (WAVE) added |
| **P3** | Intensified gravity (7200 f/s²) + control inversion | Control inversion telegraphed with GRAVITY FLUX warning + flash; harder gravity to overcome |

**Weak Points:** 2 (magenta circles at rotating arm tips)  
**Visual:** Pink reality-warp aura, jagged reality tears, 3 rotating arms with ghost afterimages, core instability arc bursts, screen-wide gravity arrows  
**Strategy:** Destroy anomalies before multiplication → fuel management for boost-fighting → time attacks after gravity flips

---

#### 6. The Signal (BOSS_SIGNAL)
**Status:** Complete  
**Difficulty:** ⭐⭐⭐⭐⭐ (Very Hard)

| Phase | Mechanic | Challenge |
|-------|----------|-----------|
| **P1** | HUD jamming (flicker increases over time) | Fuel/heat bars briefly hidden during flicker; must memorize fuel/heat rhythm |
| **P2** | Drains player velocity to heal (20 HP/s) | Momentum is stolen; ghost platform spawn rate increases; must sustain damage before phase 2 depletes reserves |
| **P3** | OVERLOAD: downward pulse (4000 f/s²) + horizontal velocity damping | Must boost against pulse to survive; velocity heavily dampened; control inversion possible |

**Weak Points:** 1 (glitching core with pulsing alpha, magenta color)  
**Visual:** Flickers in/out (10% invisible chance), 20 glitch rectangles, screen-tear bands, binary-rain particles, ghost platform previews, decoy copies in P3  
**Strategy:** Memorize fuel/heat before HUD scrambles → burst damage before P2 drains momentum → full boost + upward momentum required for P3

---

### Weak Point System
- **Destruction Mechanics:**
  - Each weak point has health pool (varies by boss)
  - Player must collide with weak point while thrusting/moving upward
  - Multiple hits required (5–20 collisions depending on boss)
  - Weak point destruction triggers visual feedback (explosion, disintegration)

- **Rocket Class Integration:**
  - **Striker (Target Lock):** Precision mode highlights weak points with reticle; increased collision detection radius
  - **Heavy (Kinetic Mass):** Weak point destruction triggers impact shockwave that damages nearby threats
  - **Explorer (Sensor Array):** Weak points visible at greater distance before actual boss encounter

---

## 6. Zone Systems (Earth → Void)

### Zone Thresholds & Progression

| Zone | Altitude Range | Theme | Visual Identity |
|------|---|---|---|
| **EARTH** | 0–500m | Home world (tutorial) | Blue-to-white sky, distant mountains, rolling hills |
| **CLOUD_LAYER** | 500–1500m | Stormy weather | Cyan-to-blue, dense moving clouds, wind effects |
| **UPPER_ATMOSPHERE** | 1500–4000m | Thinning sky edge | Deep purple-to-black, faint stars, nebulae |
| **ORBIT** | 4000–8000m | Low Earth orbit wreckage | Pitch black, high star density, Earth's curvature |
| **DEEP_SPACE** | 8000–15000m | Uncharted territory | Absolute black, pulsing blue/purple nebulae |
| **VOID** | 15000m+ | Reality anomaly | Pure black, distortion ripples, impossible stars |

### Zone Hazards & Encounters

#### Earth (0–500m)
- **Enemies:** Surveyor Probe (scout reconnaissance)
- **Hazards:** None (tutorial zone)
- **Bosses:** None
- **Platforms:** Standard, Fuel, Cooling
- **Discovery:** Standard Platform, Fuel Tank
- **Zone Mechanic:** None (baseline gameplay)

#### Cloud Layer (500–1500m)
- **Enemies:** Sky Ray (friendly slipstream provider), Aerosol Swarm (chaotic jitter)
- **Hazards:** Lightning Storm (telegraph → strike with shield damage), Turbulence Front (wind knockback + jitter)
- **Bosses:** None (Command Cruiser if score ≥ 1500)
- **Platforms:** Ice (slippery), Moving (oscillating), Boost
- **Discovery:** Ice Platform, Moving Platform
- **Zone Modifier:** Introduction of environmental forces; slippery physics challenge

#### Upper Atmosphere (1500–4000m)
- **Enemies:** Aerosol Swarm (reinforced), Surveyor Probe (escalation)
- **Hazards:** Debris Field (high integrity damage, tumbling)
- **Bosses:** Command Cruiser (1500+)
- **Platforms:** Breakable (timed collapse), Phase (flicker timing), Stability, Fuel, Cooling
- **Discovery:** Breakable Platform, Phase Platform, Utility Platforms
- **Zone Modifier:** Resource management becomes critical; temporary landing spots introduce challenge

#### Orbit (4000–8000m)
- **Enemies:** Defense Node (orbital sentry with lock-on), Surveyor Probe
- **Hazards:** EMP Pulse (shield regen disable), Solar Flare (heat spike), Radiation Zone (shield drain)
- **Bosses:** The Gatekeeper (4000+)
- **Platforms:** Magnetic (gravity field), Boost, Phase
- **Discovery:** Artifact Beacon, Various platform types
- **Zone Modifier:** Radar scans; massive heat spikes from solar radiation

#### Deep Space (8000–15000m)
- **Enemies:** Void Tracker (heat-seeking hunter), Cosmic Leviathan (slipstream pull), Derelict Echo (power-up container)
- **Hazards:** Gravity Distortion (center-drifting anomaly with vertical pull + fuel cost), Radiation Zone
- **Bosses:** Star-Eater (10000+), The Leviathan (7000+)
- **Platforms:** Magnetic, Boost, Phase
- **Discovery:** Unknown Alloy, Flight Recorder
- **Zone Modifier:** Massive gravity-shifting entities; resource competition; slipstream push-zones

#### The Void (15000m+)
- **Enemies:** Shadow Entity (phasing ambusher), Scout Drone
- **Hazards:** Void Anomaly (intense spatial pull + HUD flicker)
- **Bosses:** Void Engine (15000+), The Signal (18000+)
- **Platforms:** Magnetic, Phase, Trap (fake platforms spawned by boss)
- **Discovery:** Drone Core
- **Zone Modifier:** Reality-warping mechanics; control inversion possible; HUD interference

---

## 7. Discovery/Codex System

### Total Entries: 43 Across 8 Categories

#### Category Breakdown

| Category | Count | Examples |
|----------|-------|----------|
| **PLATFORMS** | 10 | Standard Platform, Ice Platform, Breakable Platform, Magnetic Platform, etc. |
| **POWERUPS** | 4 | Fuel Tank, Turbo Booster, Efficiency Module, Heat Sink |
| **MECHANICS** | 2 | Engine Heat, Overheated System |
| **AREAS** | 6 | Earth, Cloud Layer, Upper Atmosphere, Orbit, Deep Space, The Void |
| **ROCKETS** | 4 | Balanced, Scout, Tank, Experimental |
| **LORE** | 4 | The Ascension Program, The First Signal, The Lost Fleet, The Ascension Logs |
| **ARTIFACTS** | 4 | Flight Recorder, Unknown Alloy, Encrypted Beacon, Drone Core |
| **THREATS** | 9 | Lightning Storm, Debris Field, Radiation Zone, Solar Flare, Turbulence Front, Gravity Distortion, EMP Pulse, Void Anomaly + 5 bosses/enemies |

### Discovery Mechanics

- **Zone Entry:** Automatically discover zone upon reaching its altitude threshold
- **Artifact Collection:** Random discovery when collecting power-up spawned by combo or mission reward
- **Enemy Encounter:** Automatically discover enemy types upon being damaged or seeing them
- **Mission/Exploration:** Some entries unlock via story missions or reaching altitude milestones

### Archive Screen
- **Access:** From main menu (persistent UI button)
- **Display:** 43-entry searchable/filterable list
- **Unlock Tracking:** Marks discovered vs undiscovered entries
- **Lore Viewing:** Full descriptions, discovery date, zone discovered, times found (artifacts)

### Persistent Storage
- **SharedPreferences** storage: `discovery_[DiscoveryType]` boolean flag
- **Artifact Records:** Tracks first discovery date, times found, highest altitude, zone discovered
- **Per-Session:** Discovery events trigger 4-second celebration overlay

---

## 8. Threat/Enemy Systems

### Threat Categories

#### Environmental Hazards (8 Total)

| Hazard | Zone | Mechanic | Damage | Visual |
|--------|------|----------|--------|--------|
| **Lightning Storm** | Cloud+ | Telegraph (2s) → Strike | Shield damage | Cumulonimbus swells, branched fork lightning, impact flash |
| **Turbulence Front** | Cloud+ | Persistent wind force | Knockback | Arrow streaks, vortex swirl, strength-graded count |
| **Debris Field** | Upper Atmos+ | Drifting tumble | High integrity damage | Sharp jagged polygons, orange glow, spark particles |
| **Radiation Zone** | Orbit+ | Pulsing area effect | Shield drain (5 units/s) | Purple aura, 5 energy tendrils, Geiger-counter bursts |
| **Solar Flare** | Orbit+ | Moving plasma wave | Rapid heat increase | Wavy flame-front, 6 flame tendrils, heat shimmer |
| **EMP Pulse** | Orbit+ | Expanding ring | Disable shield regen (3s) | Cyan shockwave, 4 electrical arcs, shield icon flash |
| **Gravity Distortion** | Deep Space+ | Center-drifting pull | Vertical pull + fuel cost increase | 4 lensing rings, 8 downward arrows, star stretch |
| **Void Anomaly** | Void | Intense spatial pull | Heavy movement pull, HUD flicker | Magenta aura, jagged space tears, spiraling particles |

#### Enemies (8 Total)

| Enemy | Zone | Status | Mechanic | Damage |
|-------|------|--------|----------|--------|
| **Surveyor Probe** | Earth+ | Complete | Patrol → Detect → Transmit → Escalate → Flee | Detection + zone threat summoning |
| **Sky Ray** | Cloud Layer | Complete | Friendly majestic glide | Upward slipstream boost (0 damage) |
| **Aerosol Swarm** | Cloud/Upper | Complete | Shape-shifting swarm (35 particles) | Chaotic movement jitter |
| **Defense Node** | Orbit | Complete | Stationary radar scan + projectile fire | 8 damage/bolt; freeze combo; drain fuel |
| **Derelict Echo** | Deep Space | Complete | Drifting wreck | Spawns power-up on contact (0 direct damage) |
| **Void Tracker** | Deep Space | Complete | Heat-seeking hunter + projectile fire | 8 damage/bolt; scans at high alertLevel |
| **Cosmic Leviathan** | Deep Space | Complete | Drifting behemoth with slipstream | Slipstream pull + vacuum on proximity |
| **Shadow Entity** | Void | Complete | Phasing ambusher | Only damages when materialized; drains fuel/integrity |

#### Mini-Bosses (1 Total)

| Boss | Zone | Status | Phases | Weak Points |
|------|------|--------|--------|------------|
| **Command Cruiser** | Cloud–Void | Complete | 3 | 3 |

#### Zone Bosses (6 Total)
*See Section 5 above for detailed mechanics*

### Threat Spawn Rules

**Base Spawn Interval:** 0.8–3.0 seconds (scales by zone intensity)

**Zone Intensity Multipliers:**
- Earth: 1.0×
- Cloud Layer: 1.3×–1.5×
- Upper Atmosphere: 1.6×–2.0×
- Orbit: 2.0×–3.0×
- Deep Space: 2.5×–4.0×
- Void: 3.0×–5.0×

**Threat Caps Per Zone:**
- Max Hazards: 1–3 (zone dependent)
- Max Scout Drones: 1–5 (zone dependent)
- Max Enemies: 2–5 (zone dependent)
- Boss Presence: Slows normal spawn rate to 30% while active

**Difficulty Scaling:**
- Boss HP multiplier: 1.0–3.0× based on zone altitude
- Threat density scales with intensity factor
- Later zones have longer projectile patterns, faster attack rates

---

## 9. Resource Management Triangle

### The Four Interacting Systems

#### 1. Fuel
- **Limits:** Thrust duration (max 2–5 seconds of continuous thrust depending on load)
- **Consumption:** 27 units/sec of active thrust
- **Recovery:** 40 units/sec passive regeneration OR platform/power-up pickup
- **Trade-off:** Scout rocket sacrifices capacity for thrust speed; Tank rocket prioritizes endurance

#### 2. Heat
- **Limits:** Engine efficiency (overheat at 100 units = 2s lockout)
- **Generation:** 13 units/sec active thrust
- **Cooling:** 20 units/sec passive regeneration OR platform/power-up pickup
- **Trade-off:** Experimental rocket generates dangerous heat (18.2 units/sec); Tank/Scout are cooler

#### 3. Shield
- **Capacity:** 50 units base (upgradeable)
- **Regen Rate:** 1.0 unit/sec after 4s delay (recently took damage)
- **Absorption:** Shields absorb incoming damage before hull
- **Trade-off:** Requires managing safe downtime to recover

#### 4. Hull Integrity
- **Capacity:** 100 units base (upgradeable)
- **Threshold:** Game over if 0
- **Destruction Sequence:** 2.5-second dissolution animation before actual game over (velocity loss + falling)
- **Trade-off:** Once damaged, only recovers via hull repair power-ups or modules

### Decision Triangle in Practice

**Scenario 1: Approaching a storm (Lightning Storm hazard)**
- **Fuel question:** Can I thrust through, or do I need to idle on a platform to recharge?
- **Heat question:** Will the storm's duration/damage force me into overheat lockout during dodge?
- **Shield question:** If I get hit, will my shield absorb it, or will I take hull damage?
- **Hull question:** If I take hull damage, do I have a hull repair power-up, or is this run over?

**Scenario 2: Boss encounter**
- **Fuel management:** Burst upward to hit weak points (high fuel drain) vs. conservative approach
- **Heat management:** Aggressive pursuit requires more thrust (higher heat) vs. risk of overheat lockout
- **Shield management:** Stay mobile to avoid projectiles (shield passive regen stops) vs. sit on platform (shield recovers)
- **Hull management:** If hit during aggressive play, do I have emergency shield or hull repair modules?

---

## 10. Meta-Progression Systems

### Ownership-Based Module System

#### Module Framework
- **Total Modules:** 17 unique modules across 5 categories
- **Equip Slots:** 2 (2-slot loadout)
- **Persistence:** SharedPreferences tracks owned module IDs
- **Unlock Model:** Automatic granting based on artifact collection, score milestones, mission completion

#### Module Categories

**Hull Modules (3):**
1. Reinforced Hull — +25% max integrity
2. Impact Dampeners — −20% all incoming damage
3. Self Repair Matrix — Slowly repairs hull when out of combat (2 HP/5s)

**Shield Modules (3):**
1. Fast Recharge — 50% faster shield recovery delay
2. Emergency Shield — Restore partial shields when hull critical (60s cooldown)
3. Reflective Shield — High-quality shockwave visual (reflects damage to off-screen threats planned)

**Engine Modules (3):**
1. Burst Thrusters — Temporary thrust surge ability
2. Long Burn Thrusters — Extended burn duration at cost of throttle
3. Vector Thrusters — Enhanced steering authority

**Heat Modules (3):**
1. Cooling Matrix — Passive heat dissipation improvement
2. Thermal Battery — Stores excess heat for later release
3. Heat Sink — Instant heat removal on demand

**Utility Modules (5):**
1. Survey Scanner — Extends discovery range
2. Artifact Locator — Highlights nearby artifacts
3. Threat Scanner — Reveals enemy positions
4. Auto Repair Drone — Companion entity for autonomous repairs
5. Emergency Beacon — Last resort survival signal

#### Module Hook System
Each module can inject behavior into game systems:

```
onEquip(player)                 // Initialization
onUnequip(player)               // Cleanup
onDamageTaken(amount) → float   // Damage reduction
onShieldHit(amount)             // Shield-specific effects
onThrust(dt) → float            // Thrust multiplier
onFuelConsume(dt) → float       // Fuel efficiency
onSteer(dt) → float             // Steering authority
onHeatChange(heat) → float      // Heat generation
onCooling(dt) → float           // Cooling rate
onLanding(platform)             // Landing event
onArtifactCollected()           // Discovery event
onUpdate(dt)                    // Per-frame update
onDraw(drawScope)               // Custom rendering
```

### Permanent Progression

**Tracked Across Sessions:**
- High score (altitude record)
- Artifacts collected (with first discovery date)
- Missions completed (global count)
- Modules owned (automatic unlock on milestones)
- Ascension rank (based on total discoveries)
- Permanent stat upgrades (max integrity, max shield from rank progression)

---

## 11. Mission System

### Mission Structure
- **Active Missions:** Max 3 concurrent (one per core track)
- **Mission Types:** 4 core tracks + 2 bonus tracks
- **Total Missions:** 15 unique templates across all tracks

### Core Mission Tracks

#### Track 1: Exploration (Altitude Milestones)
1. **"Reach Cloud Layer"** (exp_clouds) — Altitude 500m → Reward: Turbo Booster
2. **"Upper Atmosphere"** (exp_atmo) — Altitude 1500m → Reward: Efficiency Module
3. **"Reach Orbit"** (exp_orbit) — Altitude 4000m → Reward: Unknown Alloy artifact
4. **"Deep Space"** (exp_space) — Altitude 8000m → Reward: Turbo Booster
5. **"The Void"** (exp_void) — Altitude 15000m → Reward: Encrypted Beacon artifact

#### Track 2: Platforming (Landing Challenges)
1. **"Basic Landing"** (plat_land_5) — Land on 5 platforms → Reward: Fuel Tank
2. **"Acrobat"** (plat_moving) — Land on 3 moving platforms → Reward: Turbo Booster
3. **"Platform Veteran"** (plat_land_20) — Land on 20 platforms → Reward: Heat Sink
4. **"Springboard"** (plat_boost) — Use 3 boost platforms → Reward: Efficiency Module
5. **"Sky Dweller"** (plat_land_50) — Land on 50 platforms → Reward: Fuel Tank (×2)

#### Track 3: Survival (Endurance Challenges)
1. **"Flight Time"** (surv_air_30) — Stay airborne 30s → Reward: Fuel Tank
2. **"Cool Engine"** (surv_cool) — Avoid overheat 60s → Reward: Heat Sink
3. **"Sky Master"** (surv_air_60) — Stay airborne 60s → Reward: Heat Sink
4. **"Thermal Ace"** (surv_cool_2) — Avoid overheat 120s → Reward: Efficiency Module

#### Track 4: Discovery (Event Triggers)
1. **"Sky Biologist"** (disc_ray) — Discover Sky Ray → Reward: Drone Core artifact

#### Track 5: Boss Encounters (Boss Survival)
1. **"Cruiser Survivor"** (boss_cruiser) — Survive Command Cruiser → Reward: Encrypted Beacon artifact

### Mission Mechanics
- **Progress Tracking:** Absolute value for altitude/time, incremental for counts
- **Ceremony Animation:** Mission completion triggers celebration overlay (3s duration)
- **Auto-Cycling:** When mission completes, system auto-selects next template from same track
- **Prevention:** Cannot repeat mission within same run (tracks completed IDs)
- **Reward Distribution:** Missions grant power-ups, artifacts, or permanent stat upgrades
- **Notification:** Completion shows floating text + permanent notification queue entry

---

## 12. Combo System & Survival Economy

### Combo Mechanics

#### Combo Activation
- **Trigger:** Landing on ANY platform (sequential landings required)
- **Time Window:** 4–2 seconds between landings (shrinks as combo increases)
- **Break Condition:** Missing window OR taking hull damage (not shield damage)

#### Combo Tiers & Rewards

| Combo Count | Tier Name | Time Window | Reward(s) |
|---|---|---|---|
| **5–7** | BASIC | 4.0s | +50 fuel, shield capsule at combo 5 |
| **8–11** | IMPROVED | 3.5s | Turbo Booster power-up |
| **12–15** | ADVANCED | 3.0s | Altitude Booster, hull repair at combo 15 |
| **16–20** | ELITE | 2.0s | Artifact power-up, hull repair at combo 20 |
| **21+** | LEGENDARY | 2.0s | Artifact Recorder discovery |

#### Immediate Survival Rewards (On-Landing)
- **Combo 5:** Shield Capsule (+20 shield)
- **Combo 10:** Shield Capsule (+20 shield)
- **Combo 15:** Hull Repair power-up
- **Combo 20:** Hull Repair power-up
- **Combo 25+:** Every 5th combo triggers calculated reward

#### Combo Break Mechanics
- **On Break:** If combo ≥ 5, triggers "Combo Complete" celebration (3s overlay)
- **Display:** Shows final combo count + reward earned
- **Next Target:** Auto-increments target (e.g., if achieved 10, next target is 11)
- **Best Streak:** Tracks best combo achieved this run

### Survival Economy (Fuel, Shield, Hull)

#### The Feedback Loop
1. **Player Lands → Combo Increases** (low difficulty)
2. **Combo Milestone Reached (5, 10, 15, 20+)** → Immediate survival capsule drops
3. **Player Collects Capsule** → Shield or hull restored
4. **Player Continues Climbing** → Sustains run longer
5. **Enemy Damage/Hazard** → Forces decision: break combo or risk death?

#### Power-Up Types
- **Shield Capsule:** +20 shield (spawned at combos 5, 10)
- **Hull Repair:** +25 hull (spawned at combos 15, 20+)
- **Turbo Booster:** +1.25× thrust (combo milestone 8–11)
- **Efficiency Module:** −25% fuel consumption (combo milestone 12–15)
- **Fuel Tank:** +30 fuel (various combos)
- **Heat Sink:** −100 heat (various combos)
- **Artifact:** Random artifact discovery (combo 16+)

---

## 13. Implemented Features vs. Planned

### ✅ COMPLETE & PLAYABLE

**Core Systems:**
- [x] Full physics-based flight model with fuel/heat/shield/hull
- [x] 4 rocket classes with distinct multipliers + unique traits
- [x] 10 platform types with unique behaviors
- [x] Zone progression system (6 zones, altitude-based)
- [x] Scoring system (altitude = score)

**Threats:**
- [x] 8 environmental hazards (all with visual telegraphs)
- [x] 8 enemy types (complete AI for all)
- [x] 1 mini-boss (Command Cruiser, full phases)
- [x] 6 zone bosses (all with 3-phase progression, weak point destruction)
- [x] Projectile engine (BOLT, MISSILE, BEAM, WAVE types)

**Progression:**
- [x] 43-entry discovery/codex system
- [x] 4 artifact types with persistent tracking
- [x] 5-tier Ascension Rank system
- [x] 4 rocket unlock milestones (scores 0, 2K, 5K, 10K)
- [x] 17-module system with 2-slot loadout
- [x] 15-mission system with 3 active tracks
- [x] High score tracking + leaderboard screen

**Mechanics:**
- [x] Combo system with 5 tiers + survival reward economy
- [x] Shield regeneration with 4s delay
- [x] Hull destruction sequence (2.5s)
- [x] Overheat lockout (2s) + steering lock (except Experimental trait)
- [x] Platform landing bounce + velocity inheritance
- [x] Moving platform support

**Visual/UI:**
- [x] HUD widgets (fuel, heat, shield, hull gauges)
- [x] Zone discovery celebrations (4s overlay)
- [x] Combo completion display (3s overlay)
- [x] Mission progress tracking (3-mission display)
- [x] FloatingText system (damage numbers, warnings)
- [x] Particle effects (landing, explosions, environment)

**Advanced Features:**
- [x] Input buffer system (for future chrono effects)
- [x] Tether/joint physics (anchor points)
- [x] Module hook system (damage, thrust, fuel, cooling, steering)
- [x] Visual obstruction system (fog/darkness)
- [x] Entity-to-entity interaction (threats can target power-ups)

### 🚧 PLANNED/BACKLOG

**Future Zones:**
- [ ] The Foundry (automated manufacturing, laser grids, crusher blocks)
- [ ] Chrono-Rift (time dilation, control inversion, ghost echoes)
- [ ] Event Horizon (final challenge zone, gravity anomalies)

**Future Bosses:**
- [ ] Chrono Warden (time dilation bubbles, reversed controls, ghost clones)
- [ ] Magma-Core Titan (heat inversion, mandatory overheat, cooling sequence)
- [x] ~~Solar Nexus~~ (not in current backlog)

**Future Rocket Classes:**
- [ ] Stealth Hull (reduced enemy detection, evasion-focused)
- [ ] Kinetic Reflector (brawler, damage on collision)
- [ ] Drone Carrier (companion drones for automation)

**Future Enemies:**
- [ ] Automated Welders (foundry zone)
- [ ] Security Drones (foundry zone)
- [ ] Ghost Echoes (chrono-rift zone)

**Future Achievements:**
- [ ] Void Walker (reach 15000m)
- [ ] Resourceful (collect all 4 power-up types in one run)
- [ ] Untouchable (reach 2000m without hull damage)
- [ ] Artifact Hunter (collect all 4 artifact types)
- [ ] Infinite Ascent (reach 25000m)

**Future Platforms:**
- [ ] Laser Grid (foundry concept, damage on contact)
- [ ] Crusher Block (moving hazard, lethal pressure)
- [ ] Time-Dilation Field (slows rocket by 70%, chrono-rift only)

**Visual Enhancements:**
- [ ] Advanced shaders for reality distortion (Void zone)
- [ ] Reflective Shield real damage application (to off-screen threats)
- [ ] Secondary stage zoom/camera effect on boss arrival

**Streamer/Content Features:**
- [ ] Replay system
- [ ] Custom difficulty modifiers
- [ ] Challenge leaderboards

---

## 14. Technical Architecture

### Rendering Model
- **Engine:** Jetpack Compose Canvas (DrawScope-based)
- **No Third-Party Engine:** All rendering is custom-drawn via Canvas primitives (rectangles, circles, paths)
- **Rendering Order:** Background → Parallax → Hazards → Enemies → Bosses → Platforms → Projectiles → VFX → HUD

### Game Loop
- **Driver:** `LaunchedEffect { withFrameNanos { ... } }`
- **Update Rate:** 60 FPS (native screen refresh)
- **Physics Sub-steps:** 4 per frame (allows for fast projectiles without missing collisions)

### Persistence Model
- **Storage:** SharedPreferences (Android standard)
- **Data Saved:**
  - High score
  - Artifacts collected (with metadata)
  - Modules owned
  - Missions completed (count)
  - Discoveries (boolean flags)
  - Ascension rank (calculated from discoveries)
  - Loadout (equipped module IDs)
  - Rocket selection

### Performance Optimizations
- **Platform Recycling:** Old platforms removed from world when far below camera; new ones generated ahead
- **Threat Culling:** Threats outside screen bounds are skipped for rendering (but not physics)
- **Particle Pooling:** Particles removed when life ≤ 0
- **State Consolidation:** Player state in single data class with mutable state holders

---

## 15. Key Numbers & Balancing Constants

### Physics
| Constant | Value | Notes |
|----------|-------|-------|
| Gravity | 1800 f/s² | Constant downward acceleration |
| Base Thrust | 4000 f/s² | When fuel available |
| Horizontal Damping | 0.85× | Per substep |
| Air Friction | 0.98× | Per frame |
| Landing Bounce | −150 px/s | Upward impulse on platform contact |

### Fuel & Heat
| Constant | Value | Notes |
|----------|-------|-------|
| Base Fuel Capacity | 100 units | Rocket multipliers: Scout 0.7×, Tank 1.5× |
| Fuel Consumption | 27 units/sec | 3× more efficient (Sprint E balance) |
| Fuel Recharge | 40 units/sec | Passive regeneration when not thrusting |
| Base Heat Capacity | 100 units | Rocket multipliers apply |
| Heat Generation | 13 units/sec | 3× slower (Sprint E balance) |
| Heat Cooling | 20 units/sec | Passive regeneration when not thrusting |
| Overheat Lockout | 2.0 sec | Duration of engine shutdown |

### Survival
| Constant | Value | Notes |
|----------|-------|-------|
| Base Integrity | 100 units | Hull health |
| Base Shield | 50 units | Damage absorption |
| Shield Regen Rate | 1.0 unit/sec | After 4s delay |
| Shield Regen Delay | 4.0 sec | Pause after taking damage |
| Destruction Timer | 2.5 sec | Hull failure animation duration |
| Critical Threshold | 25% | Alert when below this |

### Combo Timing
| Combo Range | Window | Reward |
|---|---|---|
| 5–7 | 4.0 sec | Fuel + shield |
| 8–11 | 3.5 sec | Turbo Booster |
| 12–15 | 3.0 sec | Altitude Boost + hull |
| 16–20 | 2.0 sec | Artifact + hull |
| 21+ | 2.0 sec | Legendary |

### Zone Difficulty Multipliers
| Zone | HP Mult | Spawn Intensity | Threat Cap |
|------|---------|---|---|
| Earth | 1.0× | 1.0× | Low |
| Cloud | 1.3× | 1.3–1.5× | Low–Med |
| Upper Atmos | 1.6× | 1.6–2.0× | Medium |
| Orbit | 2.0× | 2.0–3.0× | Medium–High |
| Deep Space | 2.5× | 2.5–4.0× | High |
| Void | 3.0× | 3.0–5.0× | Very High |

---

## 16. Summary: What Jump Droid Actually Is

### Core Identity
Jump Droid is a **vertical arcade exploration game** where players must climb higher zones while managing interconnected survival resources (fuel, heat, shield, hull). The primary loop is: **Thrust → Dodge hazards → Land on platforms → Build combo → Collect power-ups → Ascend**.

### What Makes It Unique
1. **Boss Encounters:** Not seen in other vertical climbers — multi-phase bosses with destructible weak points, telegraphed attacks, and phase-specific mechanics
2. **Resource Triangle:** Four interacting systems (fuel, heat, shield, hull) create richer decision-making than single-resource games
3. **Combo → Survival Feedback Loop:** Platforming skill directly converts to survival supplies (shield/hull restoration)
4. **Discovery/Lore:** 43-entry codex system + 4 artifacts tell a sci-fi story (The Great Signal, lost expeditions)
5. **Rocket Classes + Modules:** Deep customization layer with 4 rockets + 17 modules allowing varied playstyles

### Scope: Compact but Feature-Rich
- **Development Time:** Multiple sprints (T1–T4 architecture refactors, E1–E7 epic additions)
- **Codebase:** ~66 Kotlin files, 1 activity, Canvas-based rendering
- **Content:** 6 zones, 14+ threats, 6 bosses, 43 discoveries, 15 missions, 4 rocket classes, 17 modules
- **No External Engine:** Custom physics, rendering, and game loop

### Play Experience
- **New Player:** "What's above me? Let me learn controls and climb higher"
- **Experienced Player:** "Can I optimize my combo chain and beat this boss with different rocket loadout?"
- **Veteran Player:** "Can I find all 43 codex entries, collect all artifacts, and reach Ascension Rank V?"

The game successfully delivers on its core fantasy: **vertical exploration and mastery-based ascension.**

---

## Appendix: File Organization

| System | Files |
|--------|-------|
| **Core Game Loop** | GameScreen.kt, MainActivity.kt |
| **Physics/Movement** | InputBufferManager.kt, Tether.kt, ProjectileManager.kt |
| **Player State** | Models.kt, Player class |
| **Platforms** | PlatformManager.kt, PlatformRenderer.kt, Platform.kt |
| **Threats** | ThreatManager.kt, ThreatRegistry.kt, ActiveThreat.kt, ThreatDefinition.kt, ThreatSpawnRules.kt, ThreatType.kt, ThreatTier.kt, Boss.kt |
| **Survival** | SurvivalManager.kt |
| **Progression** | ProgressionManager.kt, Mission.kt, MissionManager.kt, MissionRegistry.kt, DiscoveryManager.kt |
| **Modules** | Module.kt, ModuleRegistry.kt, LoadoutManager.kt |
| **Combos** | ComboManager.kt |
| **UI/Rendering** | HudWidgets.kt, CanvasEffects.kt, PlatformRenderer.kt, RocketRenderer.kt, ZoneBackgroundRenderer.kt, FloatingTextManager.kt, NotificationManager.kt |
| **Screens** | HangarScreen.kt, LoadoutScreen.kt, ArchiveScreen.kt, LeaderboardScreen.kt, PauseOverlay.kt, GameOverOverlay.kt, SettingsScreen.kt |
| **Configuration** | Constants.kt, AltitudeZone.kt, DevConfig.kt |

---

**End of Audit**
