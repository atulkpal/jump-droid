# Jump Droid - Boss Documentation

## Overview

This document details all major and mini-bosses currently implemented or planned in Jump Droid. Bosses are zone guardians that appear at specific altitudes, featuring unique mechanics, multiple phases, and weak point systems.

---

## Table of Contents

1. [Major Bosses](#major-bosses)
2. [Mini-Bosses](#mini-bosses)
3. [Boss Behaviors Reference](#boss-behaviors-reference)
4. [Weak Point System](#weak-point-system)
5. [Difficulty Scaling](#difficulty-scaling)
6. [Design vs Implementation Notes](#design-vs-implementation-notes)
7. [Potential Overlaps](#potential-overlaps)
8. [Summary Implementation Matrix](#summary-implementation-matrix)

---

## Major Bosses

### 1. The Gatekeeper
**ID:** `BOSS_GATEKEEPER`  
**Status:** Partial  
**Biome:** Upper Atmosphere / Space Boundary  
**Difficulty:** ⭐⭐⭐ (Medium)  
**Weak Points:** 4

| Phase | Description |
| :--- | :--- |
| **Phase 1** | Deploys a rotating energy shield. Must destroy weak points to lower shield. |
| **Phase 2** | Fires 3-way projectile patterns (PROJECTILE_SHOOTER behavior). |
| **Phase 3** | Spawns Surveyor Probes to assist. Speeds up projectile frequency. |

**Strategy:** Focus on weak points in phase 1. Use momentum dashes to dodge projectile spreads. Clear adds before focusing on damage.

---

### 2. Star Eater
**ID:** `BOSS_STAR_EATER`  
**Status:** Partial  
**Biome:** Deep Space / Stellar Nursery  
**Difficulty:** ⭐⭐⭐⭐ (Hard)  
**Weak Points:** 1

| Phase | Description |
| :--- | :--- |
| **Phase 1** | Regenerates health rapidly. Must use **max-charged Momentum dash** to break regeneration. |
| **Phase 2** | Creates a solar flare nova. Hide behind debris to avoid massive heat spike. |
| **Phase 3** | Splits into two flaming birds. Both must be destroyed within **5 seconds** of each other. |

**Strategy:** Save Momentum charge for phase 1. Memorize debris spawn locations for phase 2. Target both birds equally to avoid respawn.

---

### 3. The Leviathan
**ID:** `BOSS_LEVIATHAN`  
**Status:** Partial  
**Biome:** Void / Deep Space Edge  
**Difficulty:** ⭐⭐⭐⭐⭐ (Very Hard)  
**Weak Points:** 3 (Tail deals 3× damage)

| Phase | Description |
| :--- | :--- |
| **Phase 1** | S-curve movement (VOID_SERPENT behavior). Only tail takes full damage. |
| **Phase 2** | Devours edges of screen — **playable area shrinks** over time. |
| **Phase 3** | Opens massive maw. Must boost *into* mouth, hit core from inside, then dash out before it closes. |

**Strategy:** Chase the tail during phase 1. Stay centered in phase 2 to maintain mobility. Execute the inside-core attack quickly — hesitation is fatal.

---

### 4. Void Engine
**ID:** `BOSS_VOID_ENGINE`  
**Status:** Prototype  
**Biome:** Void / Quantum Fluctuation Zone  
**Difficulty:** ⭐⭐⭐⭐ (Hard)  
**Weak Points:** 2

| Phase | Description |
| :--- | :--- |
| **Phase 1** | Summons Void Anomalies that drift toward player (WIND_MAKER behavior). |
| **Phase 2** | Creates localized gravity wells that **pull downward**; must boost upward to counter. |
| **Phase 3** | Gravity flips — controls invert momentarily. Exposes core after each gravity pulse. |

**Strategy:** Destroy anomalies before they multiply. Save fuel for phase 2 boost-fighting. Time attacks right after gravity flips.

---

### 5. The Signal
**ID:** `BOSS_SIGNAL`  
**Status:** Prototype  
**Biome:** Edge of Reality / Chrono-Rift  
**Difficulty:** ⭐⭐⭐⭐⭐ (Very Hard)  
**Weak Points:** 1

| Phase | Description |
| :--- | :--- |
| **Phase 1** | Jams HUD — **hides fuel and heat bars** (SIGNAL_JAMMER behavior). |
| **Phase 2** | Drains Momentum to heal (ITEM_STEALER behavior). |
| **Phase 3** | Massive pulse pushes rocket downward. Must **boost against pulse** to reach core. |

**Strategy:** Memorize your fuel/heat rhythm before HUD scrambles. Use Momentum before phase 2 so it has less to steal. Full boost + upward momentum is required for phase 3.

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
**Biome:** Upper Atmosphere  
**Weak Points:** 3

| Behavior | Description |
| :--- | :--- |
| Spawns Scout Drones to assist. |
| Fires accurate projectile bursts. |
| Retreats when weak points are destroyed. |

**Strategy:** Clear drones first. Target weak points systematically.

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

1. **Altitude** — higher zones are harder.
2. **Player progress** — Codex completion increases challenge.
3. **Combo streaks** — longer combos may trigger harder boss phases.
4. **Rocket class** — Tank receives less speed penalty; Scout deals more weak-point damage.

### Scaling Table

| Factor | Scaling Multiplier |
| :--- | :--- |
| Base Health | ×1.0 (altitude 0) to ×3.0 (altitude max) |
| Damage Output | ×1.0 to ×2.5 |
| Attack Speed | ×1.0 to ×2.0 |
| Phase Transition Threshold | ×1.0 to ×1.5 (later transitions) |

---

# Design vs Implementation Notes

Current implementation in `ActiveThreat.kt` and `ThreatRegistry.kt` differs from original design in several key areas:

*   **The Signal:** Currently uses **Ghost Platforms** (trap platforms that vanish on touch) as its primary mechanic.
*   **Star-Eater:** Currently uses **Power-Up Suction** (pulling items into its core to deny the player) as its primary mechanic.
*   **Commander Unit:** Currently uses **Platform Jamming** (disabling nearby platforms) as its primary mechanic.
*   **Projectile System:** A global projectile system does **not currently exist** in the game engine. Bosses using `PROJECTILE_SHOOTER` currently rely on alternative proximity or area effects.
*   **Phase Logic:** Actual implemented phases in `ActiveThreat.kt` often focus on movement patterns and proximity interaction rather than complex bullet hell patterns or control reversals.

---

# Potential Overlaps

Purpose: Record concepts that may overlap thematically and could require future review.

**ENT_VOID_WHALE**
*   **Status:** Registry Only
*   **Theme overlap:** With `BOSS_LEVIATHAN`
*   **Future decision:**
    *   Keep separate
    *   Promote to Mini-Boss
    *   Merge concepts into Leviathan
*   **Current state:** No decision made.

**HAZ_VOID_ANOMALY**
*   **Status:** Prototype
*   **Theme overlap:** With `BOSS_VOID_ENGINE`
*   **Future decision:**
    *   Keep as Hazard spawned by boss
    *   Integrate as direct boss ability
*   **Current state:** Functional but orphaned from registry.

---

# Summary Implementation Matrix

| Name | Type | Status | Zone | Notes |
| :--- | :--- | :--- | :--- | :--- |
| **Command Cruiser** | Mini-Boss | **Complete** | Orbit | Platform Jamming implemented |
| **The Gatekeeper** | Boss | **Partial** | Orbit | Gaps and barriers functional; missing projectiles |
| **Star-Eater** | Boss | **Partial** | Deep Space | Power-Up suction implemented; missing split phase |
| **The Leviathan** | Boss | **Partial** | Deep Space | Segmented body and thrash logic functional |
| **Void Engine** | Boss | **Prototype** | Void | Gravity shifts implemented; design-only control inversion |
| **The Signal** | Boss | **Prototype** | Void | Ghost platforms implemented; HUD jam is design-only |
| **Chrono Warden** | Boss | **Planned** | Chrono-Rift | Design concept only |
| **Magma-Core Titan** | Boss | **Planned** | Subterranean | Design concept only |
| **Frost Wyrmling** | Mini-Boss | **Planned** | Ice Fields | Design concept only |
| **Crystal Guardian** | Mini-Boss | **Planned** | Caverns | Design concept only |
| **Scrap Berserker** | Mini-Boss | **Planned** | Scrapyard | Design concept only |

---

## Changelog

| Version | Changes |
| :--- | :--- |
| **v0.9** | Synchronized with THREATS.md audit. Added Status fields and Implementation Matrix. |
| **v0.8** | Added Chrono Warden, Magma-Core Titan. Updated Signal behavior. |
| **v0.7** | All major bosses implemented. Mini-bosses active. |
| **v0.6** | Boss framework (Behavior enums, Weak Points). |

---

*Documentation generated from `ThreatDefinition.kt`, `ActiveThreat.kt`, and `BossBehavior.kt`.*
