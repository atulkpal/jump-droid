# Jump Droid - Boss & Enemy Documentation

## Overview

This document details all major bosses, mini-bosses, and standard enemies currently implemented or planned in Jump Droid. These entities are zone guardians and obstacles that appear at specific altitudes, featuring unique mechanics, multiple phases, and weak point systems.

---

## Table of Contents

1. [Major Bosses](#major-bosses)
2. [Mini-Bosses](#mini-bosses)
3. [Standard Enemies](#standard-enemies)
4. [Boss Behaviors Reference](#boss-behaviors-reference)
5. [Weak Point System](#weak-point-system)
6. [Difficulty Scaling](#difficulty-scaling)
7. [Design vs Implementation Notes](#design-vs-implementation-notes)
8. [Potential Overlaps](#potential-overlaps)
9. [Summary Implementation Matrix](#summary-implementation-matrix)

---

## Major Bosses

### 1. The Gatekeeper
**ID:** `BOSS_GATEKEEPER`  
**Status:** Complete  
**Biome:** Upper Atmosphere / Space Boundary  
**Difficulty:** ⭐⭐⭐ (Medium)  
**Weak Points:** 4  
**Visual Appearance:** Massive rotating orbital ring with afterimage ghost rings from rapid rotation. Safe gaps highlighted in green glow with solid energy barrier walls; danger arcs in red with high-contrast edges. Push-back force lines radiate from barriers toward player. Weak point nodes have rotating cyan shield rings. Central eye tracks player with iris pupil movement. Full-screen arrival dimming overlay.

| Phase | Description |
| :--- | :--- |
| **Phase 1** | Deploys a rotating energy shield. Must destroy weak points to lower shield. |
| **Phase 2** | Fires 3-way BOLT projectile patterns every 2s via ProjectileManager. |
| **Phase 3** | Spawns Scout Drones to assist. Continues wall-pressure pull toward center. |

**Strategy:** Focus on weak points in phase 1. Use momentum dashes to dodge projectile spreads. Clear adds before focusing on damage.

---

### 2. Star Eater
**ID:** `BOSS_STAR_EATER`  
**Status:** Partial  
**Biome:** Deep Space / Stellar Nursery  
**Difficulty:** ⭐⭐⭐⭐ (Hard)  
**Weak Points:** 1  
**Visual Appearance:** Black/purple radial gradient suction aura with after-image ghost copy offset behind. Power-up suction stream trails pulling from surroundings. 16 energy dentition-teeth around the maw rim. Dark pulsing core with magenta eye weak point (white pupil). 12 purple/red tendrils that glow brighter when player is near. Hunger-meter pulsing ring around core.

| Phase | Description |
| :--- | :--- |
| **Phase 1** | Regenerates health rapidly. Must use **max-charged Momentum dash** to break regeneration. |
| **Phase 2** | Creates a solar flare nova. Hide behind debris to avoid massive heat spike. |
| **Phase 3** | Splits into two flaming birds. Both must be destroyed within **5 seconds** of each other. |

**Strategy:** Save Momentum charge for phase 1. Memorize debris spawn locations for phase 2. Target both birds equally to avoid respawn.

---

### 3. The Leviathan
**ID:** `BOSS_LEVIATHAN`  
**Status:** Complete  
**Biome:** Void / Deep Space Edge  
**Difficulty:** ⭐⭐⭐⭐⭐ (Very Hard)  
**Weak Points:** 3 (Tail segments 4–5 deal 3× damage)  
**Visual Appearance:** 6 organic ellipse body segments with armor plate overlay, arranged vertically with sine-wave offset. Each segment has bioluminescent vein patterns and directional slipstream arrows showing push direction. Head segment has a glowing red eye. Weak points (magenta circles with white dot) on even segments (0, 2, 4). Tail whip telegraph lines extend from last 2 segments in phase 3. Wall-pressure red edge glow on screen sides when player is near boundaries.

| Phase | Description |
| :--- | :--- |
| **Phase 1** | S-curve movement (VOID_SERPENT behavior). Tail segments (4–5) deal 3× knockback force. |
| **Phase 2** | Devours edges of screen — **playable area shrinks** proportionally to weak points remaining. Player pushed inward from margins. |
| **Phase 3** | Opens massive maw (80px core zone). Standing inside the core rapidly heats the player. If pushed to screen edge, massive downward force applied. |

**Strategy:** Chase the tail during phase 1. Stay centered in phase 2 to maintain mobility. Avoid the maw core in phase 3.

---

### 4. Void Engine
**ID:** `BOSS_VOID_ENGINE`  
**Status:** Complete  
**Biome:** Void / Quantum Fluctuation Zone  
**Difficulty:** ⭐⭐⭐⭐ (Hard)  
**Weak Points:** 2  
**Visual Appearance:** Pink reality-warp radial gradient aura with jagged reality-tear rim. 3 rotating arms (#880E4F bars with white border) at 120° spacing, each with ghost afterimage copies for rotation trail. Core instability white arc bursts (frequency increases with damage). Magenta weak point circles at arm tips. Screen-wide gravity-shift direction arrows (large background arrow + edge indicators). Control-inversion buildup pink screen tint overlay.

| Phase | Description |
| :--- | :--- |
| **Phase 1** | Lateral gravity shifts push player sideways. Summons Void Anomalies every 5s that drift toward player. |
| **Phase 2** | Creates **downward gravity wells** (4800f/s² constant downward force); must boost upward to counter. Control inversion triggers randomly at ~1.2% chance per frame (GRAVITY FLUX telegraph + flash). |
| **Phase 3** | Gravity lateral shifts intensify (7200f/s²). Control inversion with full GRAVITY FLUX warning + screen flash + burst. |

**Strategy:** Destroy anomalies before they multiply. Save fuel for phase 2 boost-fighting. Time attacks right after gravity flips.

---

### 5. The Signal
**ID:** `BOSS_SIGNAL`  
**Status:** Complete  
**Biome:** Edge of Reality / Chrono-Rift  
**Difficulty:** ⭐⭐⭐⭐⭐ (Very Hard)  
**Weak Points:** 1  
**Visual Appearance:** Flickers in and out of visibility (10% chance invisible per frame, 30% in phase 3). 20 randomly positioned red/white glitch rectangles (up from 15). Screen-tear horizontal offset bands. Binary-rain particle columns (0/1 symbols). Ghost platform preview flicker outlines. Decoy Signal copies that dissolve in phase 3. Static-noise TV ring aura. Magenta glitching weak point circle with pulsing alpha. Large faint white scanning pulse ring.

| Phase | Description |
| :--- | :--- |
| **Phase 1** | Jams HUD — **increases flicker duration** over time (starts short at 2.5s, grows with phase duration up to ~8s). Fuel/heat bars briefly hidden during flicker. |
| **Phase 2** | Drains player velocity (momentum) to heal (20 HP/s). Ghost platform spawn rate increases. |
| **Phase 3** | OVERLOAD initiated after weak points destroyed. Massive **downward pulse** (4000f/s²) pushes rocket down. Horizontal velocity heavily damped. Must **boost against pulse** to survive. |

**Strategy:** Memorize your fuel/heat rhythm before HUD scrambles. Use burst damage before phase 2 so it has less to steal. Full boost + upward momentum is required for phase 3.

---

### 6. Chrono Warden
**ID:** `BOSS_CHRONO_WARDEN`  
**Status:** Planned  
**Biome:** Chrono-Rift Anomaly  
**Difficulty:** ⭐⭐⭐⭐⭐ (Very Hard)  
**Weak Points:** 3

| Phase | Description |
| :--- | :--- |
| **Phase 1** | Deploys Time-Dilation Bubbles that **slow your rocket speed by 70%**. |
| **Phase 2** | Fires Tachyon Beam — **reverses Up/Down controls** for 4 seconds (TIME_DILATOR behavior). |
| **Phase 3** | Summons ghost clones. Only the glowing real one takes damage. |

**Strategy:** Avoid bubbles at all costs. Build muscle memory for inverted controls. Study clone patterns — the real one has a faint glow pulse.

---

### 7. Magma-Core Titan
**ID:** `BOSS_MAGMA_TITAN`  
**Status:** Planned  
**Biome:** Subterranean Magma Vents  
**Difficulty:** ⭐⭐⭐⭐ (Hard)  
**Weak Points:** 4

| Phase | Description |
| :--- | :--- |
| **Phase 1** | Encased in cooled volcanic rock. **Immune to cold hits** — must overheat engine to melt armor (HEAT_INVERTER behavior). |
| **Phase 2** | Melted armor reveals core. Releases massive heat wave — **must Vent instantly** to survive. |
| **Phase 3** | Core hardens again. Repeated overheat/damage cycles required. |

**Strategy:** Deliberately overheat your engine in phase 1. Time your Vent perfectly in phase 2 — too early or too late is fatal. Maintain heat balance in phase 3.

---

## Mini-Bosses

### 1. Commander Unit
**ID:** `MINI_BOSS_COMMANDER`  
**Status:** Complete  
**Biome:** Cloud Layer / Upper Atmosphere / Orbit / Deep Space / Void  
**Weak Points:** 3  
**Visual Appearance:** Large cruiser (300x120px) with phase-color shift: blue hull (P2), red hull (P3+), orange hull (flee). Cyan shield bubble when weak points remain. Bridge tower with phase-color window strip. 2 antennae with red tips. Rotating radar dish. Fast-flashing yellow hull lights (rate increases with phase). Phase-color engine glows (cyan/red/orange). 2 red scanning beam triangles (P3+). 3 magenta weak point squares with rotating white beacon. Jam-wave pulsing cyan ring. Gravity pulse ring with debris particles.

| Behavior | Description |
| :--- | :--- |
| Spawns Scout Drones to assist (via EncounterDirector in P3/P4). |
| Fires 3-way BOLT projectile bursts at player every 1.5s in P3+. |
| Gravity pulse ring pushes player upward on contact. |
| Jam-wave disables up to 2 nearby platforms. |
| Retreats when weak points are destroyed. |

**Strategy:** Clear drones first. Target weak points systematically. Dodge projectile bursts while avoiding gravity pulses.

---

### 2. Frost Wyrmling
**ID:** `MINI_BOSS_WYRM`  
**Status:** Planned  
**Biome:** Ice Fields / Mesosphere  
**Weak Points:** 2

| Behavior | Description |
| :--- | :--- |
| Trails slowing ice clouds (ICE_CONVERTER behavior). |
| Passing through ice locks **Heat Venting** for 3 seconds. |
| Speeds up as health decreases. |

**Strategy:** Dodge ice trails. Time vents before engaging. Use quick burst damage.

---

### 3. Crystal Guardian
**ID:** `MINI_BOSS_GUARDIAN`  
**Status:** Planned  
**Biome:** Crystal Caverns  
**Weak Points:** 3

| Behavior | Description |
| :--- | :--- |
| Each weak point destroyed reduces movement speed. |
| Reflects projectiles until weak points are shattered. |
| Charges when all weak points are gone. |

**Strategy:** Destroy weak points one by one. Dodge charges. Finish with combo attacks.

---

### 4. Scrap Berserker
**ID:** `MINI_BOSS_BERSERKER`  
**Status:** Planned  
**Biome:** Scrapyard / Debris Ring  
**Weak Points:** 2

| Behavior | Description |
| :--- | :--- |
| Assembles debris shield that regenerates (PLATFORM_CONSUMER). |
| Must **boost through** shield to break it. |
| Enrages at low health — faster and more aggressive. |

**Strategy:** Use Momentum dash to break shield. Save evasion boosts for enrage phase.

---

## Standard Enemies

The following entities represent standard hostile units encountered throughout the ascent.

| Name | ID | Status | Role | Zone | Key Behavior | Visual Appearance |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Surveyor Probe** | `ENT_SCOUT_DRONE` | **Complete** | Scout | Earth+ | Detects player and calls reinforcements. | Rectangular body (gray patrolling, dark red tracking); engine glow; red tracking beam; pulsing eye; 2 antennae; pink transmission rings; anchored text overhead |
| **Sky Ray** | `ENT_CLOUD_SKIMMER` | **Complete** | Support | Cloud Layer | Glides horizontally; provides upward boost. | Manta ray path shape (light cyan, 40% alpha) with wing-flap; cyan slipstream lines; 3 trailing energy bubbles |
| **Aerosol Swarm** | `ENT_SWARM_BOTS` | **Complete** | Area Denial | Cloud/Upper | Jittery movement; creates chaotic patterns. | 12 small white circles orbiting chaotically; occasional cyan spark |
| **Defense Node** | `ENT_ORBITAL_SENTRY` | **Complete** | Controller | Orbit | Periodic radar scan; freezes combo and drains fuel; fires orange BOLT projectile (2s cooldown). | Rotating square chassis (#37474F); cyan core; expanding cyan radar scan ring |
| **Derelict Echo** | `ENT_CORRUPTED_HULL` | **Complete** | Salvage | Deep Space | Drifting wreck; contact spawns random Power-Up. | Rotating dark gray hull (20x40px) with secondary piece; green pulsing signal circle |
| **Void Tracker** | `ENT_STALKER` | **Complete** | Hunter | Deep Space | Heat-seeking stalker; aggression scales with player thrust; fires red BOLT projectile (1.5s cooldown, alertLevel > 0.5). | Triangular body (red/orange heats up with alertLevel); body segmentation glow lines; thermal shimmer lines above; alert-level bar (0-100%); scanning eye + heat trail particles + dual scan rings |
| **Cosmic Leviathan** | `ENT_VOID_WHALE` | **Complete** | Juggernaut | Deep Space | Drifting behemoth; slipstream pull + vacuum on proximity. | Full whale silhouette with tail fin + pectoral fins; nebula star-field body fill (40 colored dots); slipstream direction arrows; void-wake lingering dots |
| **Shadow Entity** | `ENT_VOID_WRAITH` | **Complete** | Horror | Void | Phases in/out; damages integrity and fuel only when materialized. | Two-state rendering: materialized (purple aura, full humanoid body with red eyes + pupil tracking, crackling energy lines) vs phased (gray wireframe outline only, occasional glitch rects, 8% alpha) |
| **Heat Bat** | `ENT_HEAT_BAT` | **APPROVED** | Ambusher | Atmosphere | Dives at player when Heat is high (>=70%). | Dark silhouette (design concept) |
| **Mimic Platform** | `ENT_MIMIC` | **APPROVED** | Deceiver | Global | Perfectly resembles a platform; shatters on touch. | Identical to a normal platform with 1-pixel glitch every 5s (design concept) |
| **Void Harvester** | `ENT_VOID_HARVESTER` | **APPROVED** | Predator | Deep/Void | Actively consumes uncollected Power-Ups. | Mechanical squid (design concept) |
| **Phase Wraith** | `ENT_PHASE_WRAITH` | **APPROVED** | Stalker | Void | Only damageable when player is Overheated. | Blue humanoid (design concept) |
| **Gravity Ram** | `ENT_GRAVITY_RAM` | **APPROVED** | Controller | Deep Space | Telegraphed horizontal dash with high knockback. | Triangular ship (design concept) |

---

## Boss Behaviors Reference

| Behavior | Enum | Description | Status | Used By |
| :--- | :--- | :--- | :--- | :--- |
| Platform Consumer | `PLATFORM_CONSUMER` | Destroys platforms beneath player | **Partial** | Gatekeeper, Berserker |
| Projectile Shooter | `PROJECTILE_SHOOTER` | Fires ranged attacks | **Registry Only** | Gatekeeper, Commander |
| Ice Converter | `ICE_CONVERTER` | Freezes area; blocks venting | **Planned** | Frost Wyrmling |
| Wind Maker | `WIND_MAKER` | Creates wind currents; affects momentum | **Prototype** | Void Engine |
| Item Stealer | `ITEM_STEALER` | Steals powerups from player | **Prototype** | Signal, Leech |
| Void Serpent | `VOID_SERPENT` | S-curve movement; tail weak point | **Partial** | Leviathan |
| Time Dilator | `TIME_DILATOR` | Time bubbles; control reversal | **Planned** | Chrono Warden |
| Heat Inverter | `HEAT_INVERTER` | Requires overheating to damage | **Planned** | Magma-Core Titan |
| Signal Jammer | `SIGNAL_JAMMER` | HUD scrambling; momentum drain | **Prototype** | Signal |

---

## Weak Point System

All bosses and mini-bosses have one or more weak points. Destroying all weak points is typically required to progress to the next phase.

### Interaction Mechanics

Since Jump Droid does not currently feature a dedicated weapon system, **weak points are attacked via physical collision**. The player must skillfully pilot their rocket into the specific coordinates of a weak point to damage it.

*   **Collision Detection**: The game checks the distance between the center of the rocket and the calculated position of each active weak point.
*   **Hit Radius**: 
    *   `BOSS_STAR_EATER`: 80 pixels.
    *   All other Bosses/Mini-Bosses: 50 pixels.
*   **Attack Feedback**:
    *   **Knockback**: On a successful hit, the player is automatically knocked upward (`velocityY = -400f`) to prevent multi-hit frame overlapping.
    *   **Invulnerability**: The player receives a brief **0.5s invulnerability window** to allow for safe repositioning.
    *   **Visuals**: A purple energy burst (`onBurst`) and "WEAK POINT DESTROYED" floating text appear.
*   **Destruction**: Once a weak point is hit once, it is destroyed. When `activeWeakPoints` reaches zero, the boss typically transitions to a retreat or final phase.

| Boss | Weak Points | Damage Multiplier | Special Effect | Status |
| :--- | :--- | :--- | :--- | :--- |
| Gatekeeper | 4 | ×1.5 | Each destroyed reduces shield | **Implemented** |
| Star Eater | 1 | ×2.0 | Only way to stop regeneration | **Implemented** |
| Leviathan | 3 (tail) | ×3.0 (tail) | Tail is only vulnerable spot | **Implemented** |
| Void Engine | 2 | ×1.8 | Exposed after gravity pulses | **Implemented** |
| Signal | 1 | ×2.5 | Core exposed after pulse | **Implemented** |
| Chrono Warden | 3 | ×1.6 | Clones have fake weak points | **Design Only** |
| Magma-Core Titan | 4 | ×2.0 | Only when overheated | **Design Only** |
| Commander | 3 | ×1.4 | Each reduces drone spawns | **Implemented** |
| Wyrmling | 2 | ×1.5 | Destroyed weak points slow it | **Design Only** |
| Guardian | 3 | ×1.3 | Each reduces speed | **Design Only** |
| Berserker | 2 | ×1.7 | Shield regeneration pauses | **Design Only** |

---

## Difficulty Scaling

Boss health, damage, and attack speed scale with:

1. **Altitude** — higher zones apply a difficulty multiplier to boss base health.
2. **Player progress** — Codex completion increases challenge. *(Planned)*
3. **Combo streaks** — longer combos may trigger harder boss phases. *(Planned)*
4. **Rocket class** — Tank receives less speed penalty; Scout deals more weak-point damage. *(Planned)*

### Scaling Table

| Factor | Scaling Multiplier | Implementation |
| :--- | :--- | :--- |
| Base Health | ×1.0 (Earth) to ×3.0 (Void) | **Implemented** — `difficultyMultiplier` applied at spawn in EncounterDirector. |
| Damage Output | ×1.0 to ×2.5 | **Planned** — to be applied via `difficultyMultiplier` in processInteraction. |
| Attack Speed | ×1.0 to ×2.0 | **Planned** — to be used for phase transition timer scaling. |
| Phase Transition Threshold | ×1.0 to ×1.5 | **Planned** — later transitions in higher zones. |

### Zone Multiplier Table

| Zone | Multiplier |
| :--- | :--- |
| Earth | ×1.0 |
| Cloud Layer | ×1.3 |
| Upper Atmosphere | ×1.6 |
| Orbit | ×2.0 |
| Deep Space | ×2.5 |
| Void | ×3.0 |

Multiplier is computed in `EncounterDirector.update()` and passed to `ThreatManager.spawnThreat()` as `difficultyMultiplier`. Boss HP at spawn = `definition.baseHealth * difficultyMultiplier`.

---

# Design vs Implementation Notes

Current implementation in `ActiveThreat.kt` and `ThreatRegistry.kt` differs from original design in several key areas:

*   **The Signal:** Currently uses **Ghost Platforms** (trap platforms that vanish on touch) + **velocity drain/heal** + **downward pulse** + **HUD flicker duration scaling** + **Glitch Bolt (P2) / Static Beam (P3) projectiles** as its primary mechanics.
*   **Star-Eater:** Currently uses **Power-Up Suction** (pulling items into its core to deny the player) as its primary mechanic. Phases 1–3 are pending full rewrite per design bible (regen-break → nova + debris → split).
*   **Commander Unit:** Currently uses **Platform Jamming** + **projectile bursts** + **gravity pulse** as its primary mechanics.
*   **Projectile System:** A global projectile system exists in `ProjectileManager.kt` and is used by all 6 bosses (Commander, Gatekeeper, Star-Eater, Leviathan, Void Engine, Signal) plus 2 enemies (Defense Node, Void Tracker). Bosses fire phase-specific projectiles via `onSpawnProjectile` callback.
*   **Phase Logic:** Actual implemented phases in `ActiveThreat.kt` combine movement patterns, proximity interaction, projectile fire, and minion spawning. Sprint C added projectile support via `onSpawnProjectile` and `onSpawnThreat` callbacks.
*   **Structural Fix:** Boss update code was originally inside the `ThreatType.ENEMY ->` `when` branch, meaning Commander (MINI_BOSS) and Gatekeeper (BOSS) never executed their AI logic. Fixed in Sprint C by moving entity-specific handlers outside the `when` block.

---

# Potential Overlaps

Purpose: Record concepts that may overlap thematically and could require future review.

**ENT_VOID_WHALE**
*   **Status:** Implemented (Visual Overhaul v1.0 — rendered)
*   **Theme overlap:** With `BOSS_LEVIATHAN`
*   **Future decision:**
    *   Keep separate
    *   Promote to Mini-Boss
    *   Merge concepts into Leviathan
*   **Current state:** Has AI (slipstream + vacuum), registered in ThreatRegistry, spawnable from dev menu. Full whale silhouette rendering with nebula star-field body, tail fin, pectoral fins, slipstream arrows, void-wake dots.

**HAZ_VOID_ANOMALY**
*   **Status:** Complete
*   **Theme overlap:** With `BOSS_VOID_ENGINE`
*   **Future decision:**
    *   Keep as Hazard spawned by boss
    *   Integrate as direct boss ability
*   **Current state:** Fully registered in ThreatRegistry, has AI (pull + jitter), rendering (pink glow + rings + full-screen distortion), and natural spawn rules in EncounterDirector.

---

# Summary Implementation Matrix

| Name | Type | Status | Zone | Notes | Visual Appearance |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Command Cruiser** | Mini-Boss | **Complete** | Cloud Layer, Upper Atmosphere, Orbit, Deep Space, Void | Platform Jamming + projectile bursts + gravity pulse + drone spawning | Large dark cruiser with bridge, antennae, radar dish, hull lights, engine glows, scanning beams, weak point squares |
| **The Gatekeeper** | Boss | **Complete** | Orbit, Deep Space, Void | Rotating barriers + 3-way projectiles (P2 BOLT / P3 BEAM) + Scout Drone spawning (P3) | Massive rotating ring with 4 cyan/red barrier arcs, magenta weak point nodes, central pulsing eye |
| **Star-Eater** | Boss | **Partial** | Deep Space, Void | Power-Up suction + P2 Hunger Wave (WAVE 12dmg/4s) / P3 Cosmic Spores (MISSILE 15dmg/2.5s); missing full phase rewrite | Black/purple suction aura, spiraling magenta particles, dark core with eye weak point, 12 tendrils |
| **The Leviathan** | Boss | **Complete** | Deep Space, Void | Tail 3× damage + P2 screen shrink + P3 maw core + slipstream + P2 Spike Bolt / P3 Maw Beam | 6 connected blue segments with cyan outlines, slipstream lines, 3 magenta weak points |
| **Void Engine** | Boss | **Complete** | Void | P1 anomalies + P2 downward wells + P3 control inversion + gravity shifts + P2 Reality Ripple / P3 3-way Shards | Pink radial aura, 3 rotating arms with weak point tips, white energy arcs, shift direction arrows |
| **The Signal** | Boss | **Complete** | Void | P1 HUD flicker scaling + P2 velocity drain/heal + P3 downward pulse + ghost platforms + P2 Glitch Bolt / P3 Static Beam | Flickering visibility, red/white glitch rectangles, magenta weak point, large scanning pulse ring |
| **Chrono Warden** | Boss | **Planned** | Chrono-Rift | Design concept only |
| **Magma-Core Titan** | Boss | **Planned** | Subterranean | Design concept only |
| **Frost Wyrmling** | Mini-Boss | **Planned** | Ice Fields | Design concept only |
| **Crystal Guardian** | Mini-Boss | **Planned** | Caverns | Design concept only |
| **Scrap Berserker** | Mini-Boss | **Planned** | Scrapyard | Design concept only |

---

## Changelog

| Version | Changes |
| :--- | :--- |
| **v1.3** | **Sprint C Completion**: Structural fix — boss update code moved outside ENEMY-only `when` block. Boss projectile systems added to all 6 bosses (Commander 3-way BOLT P3+, Gatekeeper BOLT P2/BEAM P3, Star-Eater WAVE P2/MISSILE P3, Leviathan BOLT P2/BEAM P3, Void Engine WAVE P2/3-way BOLT P3, Signal BOLT P2/BEAM P3). Enemy projectiles: Defense Node (orange BOLT) + Void Tracker (red BOLT). Zone redistribution: Earth boss-free, Commander spans Cloud–Void, all bosses expanded to native zone + above. Difficulty + threat density scaling finalized. |
| **v1.2** | **Sprint C Mechanics**: Gatekeeper → Implemented (P2 projectiles, P3 drone spawning). Commander → projectile bursts in P3+. Leviathan → tail 3× damage, P2 screen shrink, P3 maw core heat. Void Engine → P1 anomaly summoning, P2 downward gravity wells (was lateral). Signal → P1 flicker duration scaling, P2 velocity drain/heal, P3 downward pulse. Difficulty Scaling → zone-based HP multiplier implemented (×1.0–×3.0). `onSpawnThreat` callback added for boss minion spawning. Documentation updated for all changes. |
| **v1.1** | **Visual Overhaul v1.0**: Complete rework of all boss/mini-boss visual appearances to communicate abilities at a glance. Gatekeeper: green/red safe-gap coloring, solid barrier walls, iris-tracking eye. Star-Eater: dentition ring, power-up suction streams, hunger-meter. Leviathan: organic ellipse segments, directional slipstream arrows, wall-pressure glow. Void Engine: reality-tear rim, arm afterimages, inversion buildup tint. Signal: screen-tear bands, binary rain, decoy copies, static-noise ring. Commander: phase-color shift hull, shield bubble, rotating beacon weak points, jam-wave ring. |
| **v1.0** | Added Visual Appearance field to all implemented bosses, mini-bosses, and standard enemies. Updated statuses: Sky Ray → Complete, Gravity Distortion → Complete, Void Anomaly → Complete. Documented 3 invisible enemies (STALKER, VOID_WHALE, VOID_WRAITH) as known rendering gaps. |
| **v0.95** | Added Standard Enemies tabular section. Merged implementation and approved concept rosters. |
| **v0.9** | Synchronized with THREATS.md audit. Added Status fields and Implementation Matrix. |
| **v0.8** | Added Chrono Warden, Magma-Core Titan. Updated Signal behavior. |
| **v0.7** | All major bosses implemented. Mini-bosses active. |
| **v0.6** | Boss framework (Behavior enums, Weak Points). |

---

*Documentation generated from `ThreatDefinition.kt`, `ActiveThreat.kt`, and `BossBehavior.kt`.*
