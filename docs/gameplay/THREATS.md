# Jump Droid: Threat Catalog (Single Source of Truth)

This document serves as the authoritative definition and implementation tracker for all hostile entities in Jump Droid.

---

## 1. Environmental Hazards

| Name | ID | Zone | Implementation Status | Core Mechanic | Effects on Player |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Lightning Storm** | `HAZ_LIGHTNING` | Cloud+ | **Complete** | Telegraph -> Strike cycle | Shield/Integrity damage |
| **Turbulence Front** | `HAZ_TURBULENCE` | Cloud+ | **Complete** | Horizontal wind force | Movement (Knockback) |
| **Debris Field** | `HAZ_DEBRIS` | Upper Atmos+ | **Complete** | Natural drift/tumbling | Integrity damage (High) |
| **Radiation Zone** | `HAZ_RADIATION` | Orbit+ | **Complete** | Pulsing area effect | Continuous Shield drain |
| **Solar Flare** | `HAZ_SOLAR_FLARE` | Orbit+ | **Complete** | Moving heat wave | Rapid Heat increase |
| **EMP Pulse** | `HAZ_EMP` | Orbit+ | **Complete** | Expanding ring | Disables Shield Regen |
| **Gravity Distortion** | `HAZ_GRAVITY` | Deep Space+ | **Partial** | Center-drifting anomaly | Pushes down; inc. Fuel cost |
| **Void Anomaly** | `HAZ_VOID_ANOMALY` | Void | **Prototype** | Spatial warping | Movement pull; HUD flicker |

### Detailed Definitions: Hazards

#### Lightning Storm (`HAZ_LIGHTNING`)
- **Spawn Rules:** Allowed in Cloud Layer, Upper Atmosphere, Orbit, Void. Spawn chance 0.45.
- **Behavior:** Strikes every 4 seconds. Telegraphs with a yellow pulse before a jagged bolt hits.
- **Effects:** If hit without shield: 10 integrity damage. With shield: 25 shield damage + 2s regen pause.
- **Sprint C Goal:** Improve visual "lazy follow" to make dodging more skill-based.

#### Turbulence Front (`HAZ_TURBULENCE`)
- **Spawn Rules:** Allowed in Cloud Layer, Upper Atmosphere, Orbit, Void. Spawn chance 0.45.
- **Behavior:** Drifting weather front.
- **Effects:** Applies strong horizontal force (1200f) and vertical jitter.
- **Sprint C Goal:** Add "Wind Streak" particles to show force direction.

#### Void Anomaly (`HAZ_VOID_ANOMALY`)
- **Spawn Rules:** (Currently missing from Registry).
- **Behavior:** Intense spatial pull toward center.
- **Effects:** Pulls player and applies visual distortion.
- **Sprint C Goal:** Register in `ThreatRegistry` for the Void zone.

---

## 2. Hostile Entities (Enemies)

| Name | ID | Zone | Implementation Status | Core AI Behavior | Effects on Player |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Surveyor Probe** | `ENT_SCOUT_DRONE` | Earth+ | **Complete** | Detect -> Track -> Call help | HUD Lock-on; Spawns more |
| **Sky Ray** | `ENT_CLOUD_SKIMMER` | Cloud Layer | **Partial** | Horizontal glide | Pushes player up (Slipstream) |
| **Aerosol Swarm** | `ENT_SWARM_BOTS` | Cloud/Upper | **Complete** | Jittery wander/surge | Chaotic Movement jitter |
| **Defense Node** | `ENT_ORBITAL_SENTRY` | Orbit | **Complete** | Stationary Radar Scan | Combo freeze; Fuel drain |
| **Derelict Echo** | `ENT_CORRUPTED_HULL`| Deep Space | **Complete** | Drifting wreckage | **Positive**: Drops Salvage |
| **Void Tracker** | `ENT_STALKER` | Deep Space | **Registry Only**| N/A | None |
| **Cosmic Leviathan** | `ENT_VOID_WHALE` | Deep Space | **Registry Only**| N/A | None |
| **Shadow Entity** | `ENT_VOID_WRAITH` | Void | **Registry Only**| N/A | None |

### Detailed Definitions: Enemies

#### Surveyor Probe (`ENT_SCOUT_DRONE`)
- **Spawn Rules:** Earth, Cloud, Upper Atmos, Orbit. Spawn chance 0.4.
- **Behavior:** Horizontal patrol. Detects player at 400f range. Tracks with a red scanning beam.
- **Attacks:** Calls reinforcements after 5 seconds of tracking.
- **Sprint C Goal:** Add "Flee" behavior once reinforcements are called.

#### Defense Node (`ENT_ORBITAL_SENTRY`)
- **Spawn Rules:** Orbit only. Spawn chance 0.12.
- **Behavior:** Stationary rotating node. Periodic radar pulses (150f radius).
- **Effects:** On pulse contact: Freezes combo counter (1s) and drains 10 fuel/s.

---

## 3. Mini-Bosses

| Name | ID | Zone | Implementation Status | Core Mechanic | Weak Points |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Command Cruiser** | `MINI_BOSS_COMMANDER`| Orbit | **Complete** | Platform Jamming | 3 |

#### Command Cruiser (`MINI_BOSS_COMMANDER`)
- **Behaviors:** 5-phase encounter (Arrival -> Observation -> Pressure -> Final Pursuit -> Departure).
- **Attacks:** 
  - **Gravity Pulse:** Pushes player down rapidly.
  - **Platform Jamming:** Disables the 2 nearest platforms, preventing landing for 2 seconds.
  - **Fuel Drain:** Passive proximity drain.
- **Defense:** 3 communication arrays. Must be hit to trigger retreat.

---

## 4. Zone Bosses

| Name | ID | Zone | Implementation Status | Core AI Behavior | Defensive Mechanics |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **The Gatekeeper** | `BOSS_GATEKEEPER` | Orbit | **Partial** | Rotating radial gaps | Energy barriers (Heat gain) |
| **Star-Eater** | `BOSS_STAR_EATER` | Deep Space | **Partial** | Suction Pull | Consumes Power-Ups |
| **The Leviathan** | `BOSS_LEVIATHAN` | Deep Space | **Partial** | Sinusoidal thrashing | Segmented body collision |
| **The Void Engine** | `BOSS_VOID_ENGINE` | Void | **Prototype** | Lateral gravity shifts | High-speed rotation |
| **The Signal** | `BOSS_SIGNAL` | Void | **Prototype** | Ghost platforms | HUD Interference / Glitching |

### Detailed Definitions: Bosses

#### The Signal (`BOSS_SIGNAL`)
- **Behavior:** Creates "Ghost Platforms" that look real but have 0.05s break time (disappear instantly).
- **Attacks:** HUD flicker and false notifications ("SIGNAL LOSS...").
- **Weak Point:** 1 glitching node that shifts position.

---

## Sprint C Recommendations (Top Priority)

1. **Implement Missing Enemies:** Write AI for `STALKER` (heat-seeking) and `VOID_WHALE` to populate Deep Space.
2. **Boss Refinement:** Move `BOSS_VOID_ENGINE` and `BOSS_SIGNAL` from Prototype to Partial by adding more aggressive physics interference.
3. **Projectile System:** Establish `ProjectileManager` to allow `ORBITAL_SENTRY` and `COMMANDER` to shoot energy bolts.
4. **Registry Cleanup:** Add `HAZ_VOID_ANOMALY` to `ThreatRegistry.kt`.
