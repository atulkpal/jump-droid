# Jump Droid: Threat Catalog (Single Source of Truth)

This document serves as the authoritative definition and implementation tracker for all hostile entities in Jump Droid.

---

## 1. Environmental Hazards

| Name | ID | Zone | Status | Core Mechanic | Effects on Player | Visual Appearance |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Lightning Storm** | `HAZ_LIGHTNING` | Cloud+ | **Complete** | Telegraph -> Strike cycle | Shield/Integrity damage | Cumulonimbus anvil cloud (Path shape); cloud swells and brightens yellow→white during telegraph; dashed strike-zone circle on ground; branched fork lightning (3 jagged lines); electric arc particles crackling around cloud perimeter; expanding impact flash |
| **Turbulence Front** | `HAZ_TURBULENCE` | Cloud+ | **Complete** | Horizontal wind force | Horizontal knockback + vertical jitter | Arrow-tipped streak clusters showing wind direction; vortex swirl at center (logarithmic spiral paths); strength-graded streak count (10-40); large wind-direction arrow at center |
| **Debris Field** | `HAZ_DEBRIS` | Upper Atmos+ | **Complete** | Natural drift/tumbling | High Integrity damage on collision | Unified sharp-jagged debris polygon (3 random variants); orange glow on leading edge; spark particles from rotation axis; speed blur ghost copies; red danger pulse circle when player on collision course |
| **Radiation Zone** | `HAZ_RADIATION` | Orbit+ | **Complete** | Pulsing area effect | Continuous Shield drain | Massive purple aura + boundary ring + core (existing); 5 energy-siphon tendrils reaching toward player when inside; Geiger-counter green burst rects; screen static overlay on proximity |
| **Solar Flare** | `HAZ_SOLAR_FLARE` | Orbit+ | **Complete** | Moving plasma wave | Rapid Heat increase | Wavy flame-front top edge (animated sine Path); 6 flame tongue tendrils reaching ahead; heat-distortion shimmer; 15 ember particles; white screen flash when wave passes player |
| **EMP Pulse** | `HAZ_EMP` | Orbit+ | **Complete** | Expanding ring | Disables Shield Regeneration | Expanding cyan shockwave shown as shrinking arc segments (remaining time indicator); 4 electrical arcs jumping across ring circumference; shield-broken X icon when ring passes player; cyan spark trail behind ring edge |
| **Gravity Distortion** | `HAZ_GRAVITY` | Deep Space+ | **Complete** | Center-drifting anomaly | Vertical pull; Increases Fuel cost | 4 lensing rings + black core + cyan warping ring (existing); 8 downward-pointing gravity arrows from core; background star stretch (tidal distortion); 6 sucked particles with trail lines |
| **Void Anomaly** | `HAZ_VOID_ANOMALY` | Void | **Complete** | Intense spatial pull | Heavy movement pull; HUD flicker | Deeper magenta radial aura (was pink); jagged space-tear rim (12-segment jittered Path); 3 expanding ring pulses; 10 inward-spiraling vortex particles; 4 tidal pull lines from anomaly to screen edges |

---

## 2. Hostile Entities (Enemies)

| Name | ID | Zone | Status | Core AI Behavior | Effects on Player | Visual Appearance |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Surveyor Probe** | `ENT_SCOUT_DRONE` | Earth+ | **Complete** | Patrol → Detect → Transmit → Escalate → Flee | Detection text; Zone-specific threat summon; Floating text warnings | 4-state color palette (blue patrol, yellow detect, red tracking, pink transmit, orange flee); detailed trapezoid probe body; antenna extends during transmission; signal wave bars; eye pulse rate per state; speed trail when fleeing; existing tracking beam + transmission rings |
| **Sky Ray** | `ENT_CLOUD_SKIMMER` | Cloud Layer | **Complete** | Horizontal majestic glide | Upward Slipstream boost | Teal/green friendly palette (#00E676); upward arrow wing markings; glow trail showing slipstream path; contact sparkle particles on proximity; exaggerated playful wing-flap; trailing energy dots |
| **Aerosol Swarm** | `ENT_SWARM_BOTS` | Cloud/Upper | **Complete** | Shape-shifting swarm | Chaotic movement jitter | 35 particles (up from 12); 3 "queen" bots with red core; ghost trail copies behind each bot; faint swarm boundary ring; collision flash sparks when player inside; 15% cyan spark rate |
| **Defense Node** | `ENT_ORBITAL_SENTRY` | Orbit | **Complete** | Stationary Radar Scan | Freezes Combo; Drains Fuel | Octagonal turret chassis with gun barrel; laser sight line to player when tracking; 4-bracket lock-on reticle at player position; fuel-drain orange particle stream; rotating snowflake combo-freeze icon |
| **Derelict Echo** | `ENT_CORRUPTED_HULL`| Deep Space | **Complete** | Drifting wreck | Contact spawns random Power-Up | Bronze/gold metallic crate (#FF8F00) with metallic shine; open lid showing power-up icon peeking out; friendly alternating green/yellow beacon glow; gold sparkle particles; attract flow lines from surroundings |
| **Void Tracker** | `ENT_STALKER` | Deep Space | **Implemented** | Heat-seeking hunter | Scans player; aggression scales with thrust; heat buildup | Triangular body (red/orange heats up with alertLevel); body segmentation glow lines; thermal shimmer lines above; alert-level bar (0-100%); scanning eye + heat trail particles + dual scan rings |
| **Cosmic Leviathan** | `ENT_VOID_WHALE` | Deep Space | **Implemented** | Drifting behemoth | Slipstream pull + vacuum on proximity | Full whale silhouette with tail fin + pectoral fins; nebula star-field body fill (40 colored dots); scaled up (bodyRadius 160); slipstream direction arrows; void-wake lingering dots; existing glow + star dots + trailing particles |
| **Shadow Entity** | `ENT_VOID_WRAITH` | Void | **Implemented** | Phasing ambusher | Phases in/out; only hurts when materialized; drains integrity and fuel | Two-state rendering: materialized (purple aura, full humanoid body with red eyes + pupil tracking, crackling energy lines) vs phased (gray wireframe outline only, occasional glitch rects, 8% alpha) |

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

| Name | ID | Zone | Status | Core Mechanic | Effects on Player | Visual Appearance |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Command Cruiser** | `MINI_BOSS_COMMANDER`| Orbit | **Complete** | Platform Jamming | Pushes player down; Disables platforms | Phase-color shift hull (blue P2, red P3+, orange flee); shield bubble when weak points remain; cyan bridge window strip; 2 antennae with red tips; rotating radar dish; fast-flashing hull lights in higher phases; phase-color engine glows; 2 red scanning beams (P3+); 3 magenta weak point squares with rotating white beacon; jam-wave pulsing ring; gravity pulse with debris particles |

---

## 4. Zone Bosses

| Name | ID | Zone | Status | Core Mechanic | Weak Points | Visual Appearance |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **The Gatekeeper** | `BOSS_GATEKEEPER` | Orbit | **Partial** | Rotating radial gaps | 4 Nodes on ring | Massive rotating orbital ring with afterimage ghost rings; green safe-gap / red danger arc coloring; solid energy barrier walls; push-back force lines toward player; weak point nodes with rotating cyan shield; central eye with iris pupil tracking player |
| **Star-Eater** | `BOSS_STAR_EATER` | Deep Space | **Partial** | Power-Up Suction | 1 Central Eye | Black/purple suction aura; after-image ghost copy; power-up suction stream trails; 16 dentition energy-teeth ring around maw; dark pulsing core + magenta eye weak point; tendrils that glow brighter when player is near; hunger-meter pulse ring |
| **The Leviathan** | `BOSS_LEVIATHAN` | Deep Space | **Implemented** | Segmented body + slipstream; wall pressure in phase 3 | 3 Segments (even) | 6 organic ellipse segments with armor plate overlay; bioluminescent vein patterns; directional slipstream arrows per segment; head segment with glowing eye; tail whip telegraph lines in phase 3; wall-pressure red edge glow |
| **The Void Engine** | `BOSS_VOID_ENGINE` | Void | **Implemented** | Reality warping; gravity shifts; control inversion in phase 3 | 2 Arm Nodes | Reality-tear jagged rim around aura; 3 rotating arms with ghost afterimages; core instability arc bursts; control-inversion buildup pink screen tint; screen-wide gravity-shift arrows + large background arrow |
| **The Signal** | `BOSS_SIGNAL` | Void | **Implemented** | Ghost platform deception; HUD interference; heat drain in phase 3 | 1 Glitching Node | Static-noise TV ring; 20 glitch rectangles (up from 15); screen-tear horizontal bands; binary-rain particle columns; ghost platform preview flicker; decoy Signal copies in phase 3; flickering visibility; magenta glitching weak point |

---

## Documentation Changelog

| Date | Changes |
| :--- | :--- |
| 2026-06-20 | Added Visual Appearance column to all threat tables. Updated statuses: Sky Ray → Complete, Gravity Distortion → Complete, Void Anomaly → Complete. Flagged 3 invisible enemies (ENT_STALKER, ENT_VOID_WHALE, ENT_VOID_WRAITH) as missing rendering. |
| 2026-06-20 | **Visual Overhaul v1.0**: Complete rework of all 24 threat renderings to communicate abilities at a glance. Added rendering for 3 previously invisible enemies. Split HAZ_GUST/HAZ_CROSSWIND/HAZ_THERMAL from no-op stubs. See `threat_visual_overhaul_2026-06-20_0240.md` for full changelog. |

---

## Sprint C Development Priorities

- [x] **Registry Sync:** Register `HAZ_VOID_ANOMALY` in `ThreatRegistry.kt`.
- [x] **Logic Implementation:** Implement AI for `ENT_STALKER` (Heat-seeking), `ENT_VOID_WRAITH` (Phasing), and `ENT_VOID_WHALE` (Slipstream).
- [x] **Prototype Promotion:** Upgrade `BOSS_VOID_ENGINE` with control inversion and `BOSS_SIGNAL` with HUD alpha-flicker.
- [x] **Polish:** Add "Flee" behavior to `ENT_SCOUT_DRONE` after calling reinforcements.
- [x] **Scout Drone Escalation:** Implement transmission countdown, zone-specific summoning, floating text warnings, and drone-anchored tethered text.
- [x] **BOSS_LEVIATHAN Completion:** Enhanced slipstream forces, wall pressure in phase 3, scanPulse visual sync.
- [x] **BOSS_VOID_ENGINE Completion:** Reality warp burst particles, stronger gravity shifts, more frequent control inversion.
- [x] **BOSS_SIGNAL Completion:** Increased ghost platform frequency, "MIRAGE" deception text, stronger HUD interference.
