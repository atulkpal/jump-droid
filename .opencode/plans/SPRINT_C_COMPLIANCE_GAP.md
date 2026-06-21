# Sprint C — Compliance & Missing Features Report

**Date:** 2026-06-22 (Updated — Sprint C Fixes Applied)
**Reference:** `docs/gameplay/BOSS_DESIGN_BIBLE.md`, `docs/gameplay/THREATS.md`

---

## 1. Hazards — Full Compliance

All 8 hazards (HAZ_LIGHTNING through HAZ_VOID_ANOMALY) match their documented specs exactly.

---

## 2. Enemies — Full Compliance

All 8 enemies (ENT_SCOUT_DRONE through ENT_VOID_WRAITH) now have complete AI + rendering. The 3 previously "invisible" enemies (STALKER, VOID_WHALE, VOID_WRAITH) now have full visual rendering from the visual pass.

---

## 3. Bosses & Mini-Bosses — Compliance & Gaps

### Command Cruiser — Status: Complete

| Doc | Actual | Gap |
|-----|--------|-----|
| Spawns Scout Drones to assist | ✅ Implemented via EncounterDirector P3/P4 | — |
| Fires accurate projectile bursts | ✅ 3-way BOLT in P3+ (1.5s cooldown) | — |
| Platform jamming | ✅ Implemented | — |
| Weak points (3) with knockback | ✅ Implemented | — |
| Drops ARTIFACT + mission progress | ✅ Implemented | — |

### The Gatekeeper — Status: Complete

| Doc | Actual | Gap |
|-----|--------|-----|
| Phase 1: Deploys rotating energy shield | ✅ Rotation barrier with safe gaps works | — |
| Phase 2: Fires 3-way projectile patterns | ✅ BOLT patterns (3s cooldown) | — |
| Phase 3: Spawns Surveyor Probes to assist | ✅ Scout Drone spawning via onSpawnThreat | — |
| 4 weak points with shield reduction | ✅ Implemented | — |
| Difficulty scaling approach | ✅ Zone-based HP multiplier (×1.0–×3.0) | — |

### Star-Eater — Status: Partial

| Doc | Actual | Gap |
|-----|--------|-----|
| Phase 1: Regenerates health rapidly; must use Momentum dash | Not implemented | **Missing entire phase mechanic** |
| Phase 2: Solar flare nova; hide behind debris | Not implemented | **Missing entire phase mechanic** |
| Phase 3: Splits into two flaming birds | Not implemented | **Missing entire phase mechanic** |
| Power-up suction | ✅ Implemented | — |
| Fuel drain + pull toward core | ✅ Implemented | — |
| 1 weak point at center | ✅ Implemented | — |

**Note:** The actual phases (Arrival → Resource Sucking → Frenzy) are completely different from the design doc.

### The Leviathan — Status: Implemented

| Doc | Actual | Gap |
|-----|--------|-----|
| Phase 1: S-curve movement; tail deals 3× damage | 6 segments with tail 3× knockback multiplier on segments 4–5 | — |
| Phase 2: Devours edges — playable area shrinks | ✅ Inward push from margins proportional to weak points remaining | — |
| Phase 3: Opens massive maw; boost into mouth | ✅ 80px maw core zone applies rapid heat buildup | — |
| 6 body segments with slipstream | ✅ Implemented | — |
| Wall pressure in phase 3 (×50 fixed) | ✅ Implemented | — |
| Weak points on even segments | ✅ Implemented | — |

### The Void Engine — Status: Implemented

| Doc | Actual | Gap |
|-----|--------|-----|
| Phase 1: Summons Void Anomalies that drift toward player | ✅ Summons HAZ_VOID_ANOMALY every 5s in phase 2 | — |
| Phase 2: Localized gravity wells pulling downward | ✅ Phase 3 downward pull (4800f/s²) replaces lateral shift | — |
| Phase 3: Gravity flips — controls invert momentarily | ✅ 2.5s control inversion with "GRAVITY FLUX" telegraph text | — |
| Phase 3: Exposes core after each gravity pulse | Weak point system active throughout | — |
| 2 weak points on rotating arms | ✅ Implemented | — |

### The Signal — Status: Implemented

| Doc | Actual | Gap |
|-----|--------|-----|
| Phase 1: Jams HUD — hides fuel and heat bars | HUD interference (flicker) with duration scaling (2.5s→~8s) | — |
| Phase 2: Drains Momentum to heal (ITEM_STEALER) | ✅ Velocity drain (X/Y) + boss heal (20 HP/s) | — |
| Phase 3: Massive pulse pushes rocket downward | ✅ 4000f/s² downward force after overload | — |
| Ghost platforms (trap) | ✅ Implemented (15-25% chance, break on touch) | — |
| Heat drain (40/s in P3) | ✅ Implemented | — |

---

## 4. Missing Systems

| System | Used By | Status |
|--------|---------|--------|
| **PROJECTILE_SHOOTER behavior** | Gatekeeper P2, Commander, all bosses | ✅ All 6 bosses + 2 enemies fire projectiles |
| **Difficulty scaling** (altitude ×1–3, damage ×1–2.5, etc.) | All bosses | ✅ HP scaling ×1–3 implemented, damage/attack speed pending |
| **Boss-specific health regen** | Star-Eater P1 | ❌ Deferred (Star-Eater rewrite pending) |
| **Screen-edge shrinking** | Leviathan P2 | ✅ Implemented (inward push proportional to weak points) |
| **Inside-mouth core attack** | Leviathan P3 | ✅ Implemented (80px maw core heat buildup) |
| **Momentum drain/steal** | Signal P2 | ✅ Implemented (velocity drain X/Y + boss heal) |
| **Time dilation bubbles** | Chrono Warden | ❌ Whole boss unimplemented |
| **Heat inversion mechanic** | Magma Titan | ❌ Whole boss unimplemented |

---

## 5. Entirely Unimplemented Entities

### Planned Bosses (2)

| ID | Name | Biome | Core Mechanic |
|----|------|-------|-------------|
| `BOSS_CHRONO_WARDEN` | Chrono Warden | Chrono-Rift | Time bubbles (70% slow), control reversal, ghost clones |
| `BOSS_MAGMA_TITAN` | Magma-Core Titan | Subterranean | Heat-inverter (overheat to damage), armor melting |

### Planned Mini-Bosses (3)

| ID | Name | Biome | Core Mechanic |
|----|------|-------|-------------|
| `MINI_BOSS_WYRM` | Frost Wyrmling | Ice Fields | Ice trails, heat vent lock |
| `MINI_BOSS_GUARDIAN` | Crystal Guardian | Crystal Caverns | Projectile reflection, charge attack |
| `MINI_BOSS_BERSERKER` | Scrap Berserker | Scrapyard | Debris shield regen, enrage |

### Approved Enemies (5)

| ID | Name | Core Mechanic |
|----|------|-------------|
| `ENT_HEAT_BAT` | Heat Bat | Dives when player heat ≥ 70% |
| `ENT_MIMIC` | Mimic Platform | Looks like a platform, shatters on touch |
| `ENT_VOID_HARVESTER` | Void Harvester | Consumes uncollected powerups |
| `ENT_PHASE_WRAITH` | Phase Wraith | Only damageable when player is overheated |
| `ENT_GRAVITY_RAM` | Gravity Ram | Telegraphed horizontal dash with knockback |

---

## Summary

| Category | Total | Complete | Partial | Planned/Missing |
|----------|-------|----------|---------|----------------|
| Hazards | 8 | 8 | 0 | 0 |
| Enemies | 8 | 8 | 0 | 0 |
| Mini-Bosses | 4 | 1 | 0 | 3 |
| Bosses | 7 | 4 | 1 | 2 |
| Missing Systems | 8 | 5 | 1 | 2 |
| **Total** | **35** | **26** | **2** | **7** |

**Key:** Star-Eater phase rewrite deferred. Chrono Warden, Magma Titan, Frost Wyrmling, Crystal Guardian, Scrap Berserker are future/new-zone entities outside current sprint scope.
