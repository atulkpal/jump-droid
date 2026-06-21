# Sprint C — Compliance & Missing Features Report

**Date:** 2026-06-21
**Reference:** `docs/gameplay/BOSS_DESIGN_BIBLE.md`, `docs/gameplay/THREATS.md`

---

## 1. Hazards — Full Compliance

All 8 hazards (HAZ_LIGHTNING through HAZ_VOID_ANOMALY) match their documented specs exactly.

---

## 2. Enemies — Full Compliance

All 8 enemies (ENT_SCOUT_DRONE through ENT_VOID_WRAITH) now have complete AI + rendering. The 3 previously "invisible" enemies (STALKER, VOID_WHALE, VOID_WRAITH) now have full visual rendering from the visual pass.

---

## 3. Bosses & Mini-Bosses — Compliance & Gaps

### Command Cruiser — Status: Complete (with gaps)

| Doc | Actual | Gap |
|-----|--------|-----|
| Spawns Scout Drones to assist | Not implemented | Missing ability |
| Fires accurate projectile bursts | Not implemented (ProjectileManager exists but unused by boss code) | Missing ability |
| Platform jamming | ✅ Implemented | — |
| Weak points (3) with knockback | ✅ Implemented | — |
| Drops ARTIFACT + mission progress | ✅ Implemented | — |

### The Gatekeeper — Status: Partial

| Doc | Actual | Gap |
|-----|--------|-----|
| Phase 1: Deploys rotating energy shield | ✅ Rotation barrier with safe gaps works | — |
| Phase 2: Fires 3-way projectile patterns | Not implemented | **Missing entire phase mechanic** |
| Phase 3: Spawns Surveyor Probes to assist | Not implemented | **Missing entire phase mechanic** |
| 4 weak points with shield reduction | ✅ Implemented | — |
| Difficult scaling approach | Not implemented | Missing system |

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

### The Leviathan — Status: Implemented (with gaps)

| Doc | Actual | Gap |
|-----|--------|-----|
| Phase 1: S-curve movement; tail deals 3× damage | Has 6 segments but no tail damage multiplier | **Missing tail multiplier** |
| Phase 2: Devours edges — playable area shrinks | Not implemented | **Missing entire phase mechanic** |
| Phase 3: Opens massive maw; boost into mouth | Not implemented | **Missing entire phase mechanic** |
| 6 body segments with slipstream | ✅ Implemented | — |
| Wall pressure in phase 3 (×50 fixed) | ✅ Implemented | — |
| Weak points on even segments | ✅ Implemented | — |

### The Void Engine — Status: Implemented (with gaps)

| Doc | Actual | Gap |
|-----|--------|-----|
| Phase 1: Summons Void Anomalies that drift toward player | Not implemented | **Missing phase mechanic** |
| Phase 2: Localized gravity wells pulling downward | Has lateral gravity shift instead (horizontal, not downward) | **Phase mechanic diverges from design** |
| Phase 3: Gravity flips — controls invert momentarily | ✅ Implemented (with GRAVITY FLUX telegraph) | — |
| Phase 3: Exposes core after each gravity pulse | Not explicitly implemented | Missing opportunity |
| 2 weak points on rotating arms | ✅ Implemented | — |

### The Signal — Status: Implemented (with gaps)

| Doc | Actual | Gap |
|-----|--------|-----|
| Phase 1: Jams HUD — hides fuel and heat bars | Has HUD interference (flicker) but doesn't fully hide bars | **Mechanic is weaker than design** |
| Phase 2: Drains Momentum to heal (ITEM_STEALER) | Not implemented | **Missing entire phase mechanic** |
| Phase 3: Massive pulse pushes rocket downward | Not implemented (has heat drain instead) | **Missing phase mechanic** |
| Ghost platforms (trap) | ✅ Implemented (15-25% chance) | — |
| Heat drain (40/s in P3) | ✅ Implemented | — |

---

## 4. Missing Systems

| System | Used By | Status |
|--------|---------|--------|
| **PROJECTILE_SHOOTER behavior** | Gatekeeper P2, Commander | ProjectileManager exists (Engine Expansion) but no boss uses it |
| **Difficulty scaling** (altitude ×1–3, damage ×1–2.5, etc.) | All bosses | Not implemented anywhere |
| **Boss-specific health regen** | Star-Eater P1 | Not implemented |
| **Screen-edge shrinking** | Leviathan P2 | Not implemented |
| **Inside-mouth core attack** | Leviathan P3 | Not implemented |
| **Momentum drain/steal** | Signal P2 | Not implemented |
| **Time dilation bubbles** | Chrono Warden | Whole boss unimplemented |
| **Heat inversion mechanic** | Magma Titan | Whole boss unimplemented |

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

| Category | Total | Complete | Partial | Missing |
|----------|-------|----------|---------|---------|
| Hazards | 8 | 8 | 0 | 0 |
| Enemies | 8 | 8 | 0 | 0 |
| Mini-Bosses | 4 | 1 | 0 | 3 |
| Bosses | 7 | 0 | 5 | 2 |
| Missing Systems | 8 | 0 | 0 | 8 |
| **Total** | **35** | **17** | **5** | **13** |
