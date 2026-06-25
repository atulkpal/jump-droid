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
| **Cryo-Mist** | `HAZ_CRYO_MIST` | Cloud, Upper Atmos, Foundry | **Complete** | Freezes Heat gauge while inside | Heat cannot change; periodic "THERMAL LOCK" text | Cyan radial-gradient mist field; white inner frost core; 8 orbiting hoarfrost rim dots; drifting ice crystals; falling frost particles; vapor wisps; shatter sparks on exit |
| **Mirror Shards** | `HAZ_MIRROR_SHARDS` | Deep Space, Chrono-Rift, The Beyond | **Complete** | Inverts horizontal control while inside | Control inversion for 0.5s; "AXIS INVERTED" notification | Purple/cyan ambient glow field; 6 rotating triangular glass shards with specular highlight sweep; cyan refraction lines; white flash ring when inversion active |
| **Gravity Shear** | `HAZ_GRAVITY_SHEAR` | Deep Space, The Beyond, Ancient Construct | **Complete** | Split vertical forces on either side of midline | Push UP above center, pull DOWN below center (up to 2500f) | Gold V-shaped arrows (3 up, 3 down) scrolling vertically; white horizontal mid-line |

---

## 2. Hostile Entities (Enemies)

| Name | ID | Zone | Status | Core AI Behavior | Effects on Player | Visual Appearance |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Surveyor Probe** | `ENT_SCOUT_DRONE` | Earth+ | **Complete** | Patrol → Detect → Transmit → Escalate → Flee | Detection text; Zone-specific threat summon; Floating text warnings | 4-state color palette (blue patrol, yellow detect, red tracking, pink transmit, orange flee); detailed trapezoid probe body; antenna extends during transmission; signal wave bars; eye pulse rate per state; speed trail when fleeing; existing tracking beam + transmission rings |
| **Sky Ray** | `ENT_CLOUD_SKIMMER` | Cloud Layer | **Complete** | Horizontal majestic glide | Upward Slipstream boost | Teal/green friendly palette (#00E676); upward arrow wing markings; glow trail showing slipstream path; contact sparkle particles on proximity; exaggerated playful wing-flap; trailing energy dots |
| **Aerosol Swarm** | `ENT_SWARM_BOTS` | Cloud/Upper | **Complete** | Shape-shifting swarm | Chaotic movement jitter | 35 particles (up from 12); 3 "queen" bots with red core; ghost trail copies behind each bot; faint swarm boundary ring; collision flash sparks when player inside; 15% cyan spark rate |
| **Defense Node** | `ENT_ORBITAL_SENTRY` | Orbit | **Complete** | Stationary Radar Scan + Projectile Fire | Freezes Combo; Drains Fuel; Periodic BOLT fire | Octagonal turret chassis with gun barrel; laser sight line to player when tracking; 4-bracket lock-on reticle at player position; fuel-drain orange particle stream; rotating snowflake combo-freeze icon |
| **Derelict Echo** | `ENT_CORRUPTED_HULL`| Deep Space | **Complete** | Drifting wreck | Contact spawns random Power-Up | Bronze/gold metallic crate (#FF8F00) with metallic shine; open lid showing power-up icon peeking out; friendly alternating green/yellow beacon glow; gold sparkle particles; attract flow lines from surroundings |
| **Void Tracker** | `ENT_STALKER` | Deep Space | **Complete** | Heat-seeking hunter + Projectile Fire | Scans player; heat buildup; Ranged BOLT fire at high alert | Triangular body (red/orange heats up with alertLevel); body segmentation glow lines; thermal shimmer lines above; alert-level bar (0-100%); scanning eye + heat trail particles + dual scan rings |
| **Cosmic Leviathan** | `ENT_VOID_WHALE` | Deep Space | **Complete** | Drifting behemoth | Slipstream pull + vacuum on proximity | Full whale silhouette with tail fin + pectoral fins; nebula star-field body fill (40 colored dots); scaled up (bodyRadius 160); slipstream direction arrows; void-wake lingering dots; existing glow + star dots + trailing particles |
| **Shadow Entity** | `ENT_VOID_WRAITH` | Void | **Complete** | Phasing ambusher | Phases in/out; only hurts when materialized; drains integrity and fuel | Two-state rendering: materialized (purple aura, full humanoid body with red eyes + pupil tracking, crackling energy lines) vs phased (gray wireframe outline only, occasional glitch rects, 8% alpha) |
| **Heat Bat** | `ENT_HEAT_BAT` | Cloud, Upper Atmos, Foundry | **Complete** | Heat-triggered dive bomber | 10–20 damage; accelerated dive when player heat ≥ 70% | Dark silhouette with red body/wing glow when heat-triggered; heat-distortion haze; ember trail during dive; wing-beat shadow; eyes glow red when heat ≥ 70% |
| **Void Harvester** | `ENT_VOID_HARVESTER` | Orbit, Deep Space, The Beyond | **Complete** | Power-Up predator; seeks and consumes items | Drains 15 fuel on contact; consumes nearby power-ups to heal | Mechanical squid body with 5 segmented animated tentacles; glowing tracking-lens eye; purple ambient field; detection pulse rings; harvest burst particles |
| **Phase Wraith** | `ENT_PHASE_WRAITH` | Void, Stellar Gate, Singularity | **Complete** | Overheat-phase stalker | 15 integrity damage when materialized; only damagable when player overheated | Ethereal cyan humanoid with flickering transparency; afterimage ghost trails; phase-transition ripple ring; white-hot vulnerability glow; whisper particles |
| **Gravity Ram** | `ENT_GRAVITY_RAM` | Ancient Construct, Singularity | **Complete** | Telegraphed kinetic dash charger | 25 integrity damage + massive knockback (3000f down); "KINETIC IMPACT" text | Dark-steel triangular hull with heat-haze engine glow; hull stress cracks during charge; expanding shockwave; ground-scorch trail; red telegraph line with crosshair at destination |

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
| **Command Cruiser** | `MINI_BOSS_COMMANDER`| Cloud Layer, Upper Atmosphere, Orbit, Deep Space, Void | **Complete** | Platform Jamming + Projectile Bursts + Gravity Pulse | Pushes player down; Disables platforms; Fires 3-way BOLT spreads at player in P3+ | Phase-color shift hull (blue P2, red P3+, orange flee); shield bubble when weak points remain; cyan bridge window strip; 2 antennae with red tips; rotating radar dish; fast-flashing hull lights in higher phases; phase-color engine glows; 2 red scanning beams (P3+); 3 magenta weak point squares with rotating white beacon; jam-wave pulsing ring; gravity pulse with debris particles |
| **Thermal Hive** | `MINI_BOSS_THERMAL_HIVE` | Cloud Layer, Upper Atmosphere | **Complete** | Heat-based spawning; spawns Swarm Bots when player heat > 60 | Collision: -20 fuel, +30 heat; swarm summons on high heat | Orange/red heat haze aura; two concentric glow rings; black organic core with heat veins; orbiting swarm particles; swarm spawn burst VFX |
| **Gravity Anchor** | `MINI_BOSS_GRAVITY_ANCHOR` | Deep Space, The Beyond | **Complete** | Escalating downward pull; intensifies over 30s (×3 max) | Downward force up to 12000f at max intensity within 1000f aura | Gold distortion aura; metallic square base with gold border; gold radial core glow; 2–4 expanding gravity rings; 8 orbiting tidal particles |
| **The Forger** | `MINI_BOSS_FORGER` | The Foundry, Orbit | **Complete** | Platform conversion; turns Normal platforms into hazards | 4s jam duration on converted platforms; "FABRICATING HAZARDS" text | Dark gray rectangular body with cyan edge glow; horizontal scanning light bar; 2 animated assembly arms; cyan/orange jam sparks |

---

## 4. Zone Bosses

| Name | ID | Zone | Status | Core Mechanic | Weak Points | Visual Appearance |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **The Gatekeeper** | `BOSS_GATEKEEPER` | Orbit, Deep Space, Void | **Complete** | Rotating radial gaps + 3-way projectiles (P2) + Scout Drone spawning (P3) | 4 Nodes on ring | Massive rotating orbital ring with afterimage ghost rings; green safe-gap / red danger arc coloring; solid energy barrier walls; push-back force lines toward player; weak point nodes with rotating cyan shield; central eye with iris pupil tracking player |
| **Star-Eater** | `BOSS_STAR_EATER` | Deep Space | **Partial** | Power-Up Suction + P2 Hunger Wave / P3 Cosmic Spores | 1 Central Eye | Black/purple suction aura; after-image ghost copy; power-up suction stream trails; 16 dentition energy-teeth ring around maw; dark pulsing core + magenta eye weak point; tendrils that glow brighter when player is near; hunger-meter pulse ring |
| **The Leviathan** | `BOSS_LEVIATHAN` | Deep Space | **Complete** | Tail 3× damage + P2 screen shrink + P3 maw core + slipstream + P2 Spike Bolt / P3 Maw Beam | 3 Segments (even) | 6 organic ellipse segments with armor plate overlay; bioluminescent vein patterns; directional slipstream arrows per segment; head segment with glowing eye; tail whip telegraph lines in phase 3; wall-pressure red edge glow |
| **The Void Engine** | `BOSS_VOID_ENGINE` | Void | **Complete** | P1 anomaly summoning + P2 downward wells + P3 control inversion + gravity shifts + P2 Reality Ripple / P3 3-way Shards | 2 Arm Nodes | Reality-tear jagged rim around aura; 3 rotating arms with ghost afterimages; core instability arc bursts; control-inversion buildup pink screen tint; screen-wide gravity-shift arrows + large background arrow |
| **The Signal** | `BOSS_SIGNAL` | Void | **Complete** | P1 HUD flicker scaling + P2 velocity drain/heal + P3 downward pulse + ghost platforms + P2 Glitch Bolt / P3 Static Beam | 1 Glitching Node | Static-noise TV ring; 20 glitch rectangles (up from 15); screen-tear horizontal bands; binary-rain particle columns; ghost platform preview flicker; decoy Signal copies in phase 3; flickering visibility; magenta glitching weak point |
| **The Architect** | `BOSS_ARCHITECT` | The Beyond, Stellar Gate | **Complete** | Level controller: removes platforms + fires cyan BOLT/WAVE projectiles | 4 cardinal weak points on rotating ring; projectile 12/10 dmg; platform removal slows ascent | Cyan outer glow; rotating diamond core with radial gradient; 4 orbiting sub-diamonds with connecting energy lines; danger pulse at low health |
| **Entropy Core** | `BOSS_ENTROPY_CORE` | Ancient Construct, Singularity | **Complete** | Global multi-drain aura + pylon projectile fire | -10 fuel/s, -5 shield/s, +20 heat/s entropy drain; pylon bolts 8 dmg | Black dark aura; pulsing red energy field; 4 pylons with energy beams; orbiting drain particles; warning pulse at low health |

---

## Documentation Changelog

| Date | Changes |
| :--- | :--- |
| 2026-06-25 | **EPIC 10 Completion**: Added 3 new hazards (Cryo-Mist, Mirror Shards, Gravity Shear), 4 new enemies (Heat Bat, Void Harvester, Phase Wraith, Gravity Ram), 3 new mini-bosses (Thermal Hive, Gravity Anchor, The Forger), and 2 new zone bosses (The Architect, Entropy Core). All 12 threats registered and rendered. Updated all visual descriptions from renderer implementations. |
| 2026-06-22 | **Sprint C Completion**: Structural fix — boss update code moved outside ENEMY-only `when` block. Boss projectile systems added to all 6 bosses (Commander 3-way BOLT P3+, Gatekeeper BOLT P2/BEAM P3, Star-Eater WAVE P2/MISSILE P3, Leviathan BOLT P2/BEAM P3, Void Engine WAVE P2/3-way BOLT P3, Signal BOLT P2/BEAM P3). Enemy projectiles: Defense Node (orange BOLT) + Void Tracker (red BOLT). Zone redistribution: Earth boss-free, Commander spans Cloud–Void, all bosses expanded to native zone + above. Difficulty scaling (HP ×1–3) + threat density scaling. |
| 2026-06-21 | **Sprint C Mechanics Update**: Gatekeeper → Implemented (P2 projectiles, P3 drone spawning). Commander → projectile bursts. Leviathan → tail 3×, screen shrink, maw core. Void Engine → anomaly summoning, downward wells. Signal → flicker scaling, velocity drain/heal, downward pulse. Difficulty scaling implemented. All boss entries and priorities updated. |
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
- [x] **Commander Projectile Bursts:** 3-way BOLT spreads in P3+ via `ProjectileManager`.
- [x] **Gatekeeper Projectiles:** 3-way BOLT patterns in P2 + Scout Drone spawning in P3.
- [x] **Leviathan Tail 3× Damage:** Segments 4–5 deal triple knockback force.
- [x] **Leviathan P2 Screen Shrink:** Inward push from margins proportional to weak points remaining.
- [x] **Leviathan P3 Maw Core:** 80px core zone applies rapid heat buildup.
- [x] **Void Engine P1 Anomalies:** Summons HAZ_VOID_ANOMALY every 5s in phase 2.
- [x] **Void Engine P2 Downward Wells:** Changed lateral shift to constant downward pull in phase 3.
- [x] **Signal P1 Flicker Scaling:** `hudInterferenceTimer` grows with `localTimer` from 2.5s to ~8s.
- [x] **Signal P2 Velocity Drain/Heal:** Drains player velocityX/Y, heals boss 20 HP/s.
- [x] **Signal P3 Downward Pulse:** 4000f/s² downward force after overload.
- [x] **Difficulty Scaling System:** Zone-based HP multiplier (×1.0–×3.0) for bosses at spawn.
- [x] **onSpawnThreat Callback:** Added to `processInteraction` for boss minion spawning.
- [x] **Structural Bug Fix:** Boss update code moved outside `ThreatType.ENEMY` `when` block — Commander (MINI_BOSS) and Gatekeeper (BOSS) now reach their AI logic.
- [x] **Boss Projectile Systems:** All 6 bosses fire projectiles in relevant phases (BOLT, MISSILE, BEAM, WAVE).
- [x] **Enemy Projectiles:** Defense Node fires orange BOLT when locked-on. Void Tracker fires red BOLT at high alertLevel.
- [x] **Zone Redistribution:** Earth boss-free. Commander spans Cloud Layer–Void. All bosses native zone + above. Cloud Commander fallback reduced 60%.
- [ ] **Star-Eater Phase Rewrite:** Full 3-phase rewrite (regen-break → nova+debris → split) — deferred to future boss expansion.
