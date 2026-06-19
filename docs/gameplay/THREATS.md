# Jump Droid: Threat Catalog (Single Source of Truth)

This document serves as the authoritative definition and implementation tracker for all hostile entities in Jump Droid.

---

## 1. Environmental Hazards

| Name | ID | Zone | Status | Core Mechanic | Effects on Player |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Lightning Storm** | `HAZ_LIGHTNING` | Cloud+ | **Complete** | Telegraph -> Strike cycle | Shield/Integrity damage |
| **Turbulence Front** | `HAZ_TURBULENCE` | Cloud+ | **Complete** | Horizontal wind force | Horizontal knockback + vertical jitter |
| **Debris Field** | `HAZ_DEBRIS` | Upper Atmos+ | **Complete** | Natural drift/tumbling | High Integrity damage on collision |
| **Radiation Zone** | `HAZ_RADIATION` | Orbit+ | **Complete** | Pulsing area effect | Continuous Shield drain |
| **Solar Flare** | `HAZ_SOLAR_FLARE` | Orbit+ | **Complete** | Moving plasma wave | Rapid Heat increase |
| **EMP Pulse** | `HAZ_EMP` | Orbit+ | **Complete** | Expanding ring | Disables Shield Regeneration |
| **Gravity Distortion** | `HAZ_GRAVITY` | Deep Space+ | **Partial** | Center-drifting anomaly | Vertical pull; Increases Fuel cost |
| **Void Anomaly** | `HAZ_VOID_ANOMALY` | Void | **Prototype** | Intense spatial pull | Heavy movement pull; HUD flicker |

---

## 2. Hostile Entities (Enemies)

| Name | ID | Zone | Status | Core AI Behavior | Effects on Player |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Surveyor Probe** | `ENT_SCOUT_DRONE` | Earth+ | **Complete** | Patrol → Detect → Transmit → Escalate → Flee | Detection text; Zone-specific threat summon; Floating text warnings |
| **Sky Ray** | `ENT_CLOUD_SKIMMER` | Cloud Layer | **Partial** | Horizontal majestic glide | Upward Slipstream boost |
| **Aerosol Swarm** | `ENT_SWARM_BOTS` | Cloud/Upper | **Complete** | Shape-shifting swarm | Chaotic movement jitter |
| **Defense Node** | `ENT_ORBITAL_SENTRY` | Orbit | **Complete** | Stationary Radar Scan | Freezes Combo; Drains Fuel |
| **Derelict Echo** | `ENT_CORRUPTED_HULL`| Deep Space | **Complete** | Drifting wreck | Contact spawns random Power-Up |
| **Void Tracker** | `ENT_STALKER` | Deep Space | **Registry** | Heat-seeking hunter | *Not yet implemented* |
| **Cosmic Leviathan** | `ENT_VOID_WHALE` | Deep Space | **Registry** | Massive slow collider | *Not yet implemented* |
| **Shadow Entity** | `ENT_VOID_WRAITH` | Void | **Registry** | Phasing horror | *Not yet implemented* |

---

### Scout Drone — Design Details

| Field | Detail |
| :--- | :--- |
| **Role** | Reconnaissance, Threat Escalation, Player Pressure |
| **Status** | **IMPLEMENTED** |

#### Behavior Flow

```
Patrol ──(player enters 400px range)──▶ Detect Player
  │
  ├──▶ "HUMAN PRESENCE DETECTED" (white + red glow floating text)
  ├──▶ Begin Transmission (5s countdown)
  │      └── eye pulse accelerates, scanPulse rises with progress
  │
  ├──▶ Transmission Complete
  │      ├── "SIGNAL TRANSMITTED" (red glow floating text)
  │      └── Zone message (red glow floating text, e.g. "SUMMONING REINFORCEMENTS")
  │
  ├──▶ Summon Zone-Specific Threat (1.5s delay)
  ├──▶ Flee (rapid upward escape at -400px/s, 3s then self-destruct)
  └──▶ Burst particles (pink)
```

#### Player Counterplay

| Action | Result |
| :--- | :--- |
| Destroy drone during transmission | Escalation prevented, no threat summoned |
| Leave detection range (400px) | Transmission resets, detection text vanishes |
| Ignore drone | Zone-specific threat spawns after 5s + 1.5s delay |

#### Zone Escalation Table

| Zone | Summoned Threat | Floating Text |
| :--- | :--- | :--- |
| Earth | *None* (message only — natural EncounterDirector handles spawns) | `SUMMONING REINFORCEMENTS` |
| Cloud Layer | `HAZ_LIGHTNING` (400px left/right of player) | `SUMMONING STORM` |
| Upper Atmosphere | `HAZ_TURBULENCE` (from nearest screen edge) | `SUMMONING TURBULENCE` |
| Orbit | `HAZ_EMP` (at drone position) | `ACTIVATING DEFENSE PROTOCOL` |
| Deep Space | `HAZ_GRAVITY` (random offset from player) | `SUMMONING ANOMALY` |
| Void | `HAZ_VOID_ANOMALY` (at drone position) | `REALITY BREACH DETECTED` |

#### Design Notes

- The Scout Drone is **not a direct combat threat**. It deals no damage.
- Its purpose is to **create urgency**, **force target prioritization**, and **punish passive play**.
- The player should view the drone as a **spotter and forward observer**, not a damage dealer.
- Floating text is tethered to the drone (follows its world position).
- "HUMAN PRESENCE DETECTED" text **vanishes immediately** when the player leaves detection range.
- Messages use red-glow styling (`labelLarge` / 14sp, SciFiRed or White with red shadow) for readability against dark space backgrounds.

#### Associated Systems

| System | Purpose |
| :--- | :--- |
| `FloatingTextManager` | Drone-anchored text lifecycle and position sync |
| `FloatingTextsLayer` | Compose rendering of floating text with red glow shadow |
| `ActiveThreat.processInteraction` | Transmission countdown, escalation trigger, flee initiation |
| `GameScreen.onEscalationEvent` | Zone lookup, threat spawn countdown, zone message creation |
| `spawnEscalationThreat` | Instantiates the zone-appropriate threat after 1.5s delay |
| `EncounterDirector` | Natural spawn of scout drones (max 2), HUD sensor notifications |
| `NotificationManager` | "SURVEYOR PROBE DETECTED" / "AEROSOL SWARM DETECTED" alerts |

---

## 3. Mini-Bosses

| Name | ID | Zone | Status | Core Mechanic | Effects on Player |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Command Cruiser** | `MINI_BOSS_COMMANDER`| Orbit | **Complete** | Platform Jamming | Pushes player down; Disables platforms |

---

## 4. Zone Bosses

| Name | ID | Zone | Status | Core Mechanic | Weak Points |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **The Gatekeeper** | `BOSS_GATEKEEPER` | Orbit | **Partial** | Rotating radial gaps | 4 Nodes on ring |
| **Star-Eater** | `BOSS_STAR_EATER` | Deep Space | **Partial** | Power-Up Suction | 1 Central Eye |
| **The Leviathan** | `BOSS_LEVIATHAN` | Deep Space | **Partial** | Segmented Thrashing | 3 Segments (1, 3, 5) |
| **The Void Engine** | `BOSS_VOID_ENGINE` | Void | **Prototype** | Lateral Gravity Shifts | 2 Arm Nodes |
| **The Signal** | `BOSS_SIGNAL` | Void | **Prototype** | Ghost Platforms | 1 Glitching Node |

---

## Sprint C Development Priorities

- [x] **Registry Sync:** Register `HAZ_VOID_ANOMALY` in `ThreatRegistry.kt`.
- [ ] **Logic Implementation:** Implement AI for `ENT_STALKER` (Heat-seeking) and `ENT_VOID_WHALE`.
- [x] **Prototype Promotion:** Upgrade `BOSS_VOID_ENGINE` with control inversion and `BOSS_SIGNAL` with HUD alpha-flicker.
- [x] **Polish:** Add "Flee" behavior to `ENT_SCOUT_DRONE` after calling reinforcements.
- [x] **Scout Drone Escalation:** Implement transmission countdown, zone-specific summoning, floating text warnings, and drone-anchored tethered text.
