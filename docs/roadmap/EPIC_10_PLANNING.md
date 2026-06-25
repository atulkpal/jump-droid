# EPIC 10 — The Outer Reaches: Library-Aligned Planning

**Goal:** Expand the vertical journey beyond the Void while implementing pre-existing **approved library content** from `docs/design/`. No invention without library traceability.

---

## 1. Altitude Zones (12 total — 2 new Library zones integrated)

| Zone | Threshold | Visual Theme | Library Source |
| :--- | :--- | :--- | :--- |
| Earth | 0m | Home world | Existing |
| Cloud Layer | 500m | Stormy skies | Existing |
| Upper Atmosphere | 1,500m | Thinning edge | Existing |
| Orbit | 4,000m | Orbital wreckage | Existing |
| **The Foundry** | **6,000m** | **Ancient automated factory** | **AREA_LIBRARY — APPROVED** |
| Deep Space | 8,000m | Silent sea | Existing |
| **Chrono-Rift** | **13,000m** | **Fractured time stream** | **AREA_LIBRARY — APPROVED** |
| The Void | 15,000m | Reality anomaly | Existing |
| The Beyond | 25,000m | Ethereal nebula | Event Horizon (Backlog → evolved) |
| Stellar Gate | 45,000m | High-tech mega-structures | Foundry theme evolved |
| Ancient Construct | 70,000m | Monolithic geometry | New — fortress concept |
| Singularity | 100,000m | Reality-warping white | Event Horizon final evolution |

---

## 2. New Gameplay Mechanics

### Ancient Sky Structures
- **Flux Platforms**: Teleport the player horizontally. (Originates from PLATFORM_LIBRARY Conveyor → Spatial Folding evolution)
- **Graviton Platforms**: Create a localized gravity well. (Originates from PLATFORM_LIBRARY Magnetic evolution + Gravity Shear threat)

### Library Threats (THREAT_LIBRARY — APPROVED)
- **Cryo-Mist**: Freezes Heat bar while inside. ⭐⭐
- **Mirror Shards**: Inverts horizontal axis. ⭐⭐⭐⭐
- **Gravity Shear**: Split force — top pushes up, bottom pulls down. ⭐⭐⭐
- **Heat Bat**: Dives when player Heat ≥ 70%. ⭐⭐⭐
- **Void Harvester**: Eats Power-Ups. ⭐⭐⭐ (renamed from planned "Stellar Harvester" to match library)
- **Phase Wraith**: Damagable only when Overheated. ⭐⭐⭐⭐
- **Gravity Ram**: Telegraphed dash with massive knockback. ⭐⭐⭐

### Library Mini-Bosses (THREAT_LIBRARY — APPROVED)
- **Thermal Hive**: Spawns Swarm Bots when player Heat is high. (Atmosphere)
- **Gravity Anchor**: Downward pull increases every 10s. (Deep Space)
- **The Forger**: Converts Normal platforms to Ice/Breakable. (Orbit / Foundry)

### Library Major Bosses (THREAT_LIBRARY — APPROVED)
- **The Architect**: Level IS the boss — adds/removes platforms. (The Foundry)
- **Entropy Core**: Global Fuel/Shield/Heat drain. Destroy 4 pylons to win. (Deep Space)

### Library Platforms (PLATFORM_LIBRARY — APPROVED)
- **Conveyor Platform**: Moving belt pushes player horizontally.
- **Disguised Platform (Mimic)**: Looks normal, shatters on landing dealing 15 integrity damage.

### Library Power-Ups (POWERUP_LIBRARY — APPROVED)
- **Kinetic Battery**: Landing impact → shield recharge or thrust burst.
- **Magnetic Siphon**: Auto-pulls nearby power-ups toward rocket.
- **Overdrive Module**: Massive thrust but continuous integrity damage.

---

## 3. Sprint Roadmap

### Sprint 10.0: Infrastructure Refactor ✅
- [x] Create `ZoneConfig` model
- [x] Externalize spawn tables
- [x] Generic spawn iterator

### Sprint 10.1: Expansion of the Sky ✅
- [x] Add altitude thresholds
- [x] New zone enum entries
- [x] New visual and parallax layers
- [x] DiscoveryType and LoreLog registries

### Sprint 10.2: Mega-Structures & Flux ✅
- [x] FLUX and GRAVITON platform types
- [x] Flux teleport logic
- [x] Graviton well logic
- [x] Platform renderers

### Sprint 10.3: Library Zone Integration ✅
- [x] **The Foundry** zone (6,000m) — from AREA_LIBRARY
- [x] **Chrono-Rift** zone (13,000m) — from AREA_LIBRARY
- [x] Background visuals, ambient systems, encounter configs
- [x] Discovery & lore entries

### Sprint 10.4: Library Threats & Entities ✅
- [x] Implement Cryo-Mist, Mirror Shards, Gravity Shear hazards
- [x] Implement Heat Bat, Void Harvester, Phase Wraith, Gravity Ram enemies
- [x] Register renderers in ThreatRendererRegistry

### Sprint 10.5: Library Platforms & Power-Ups ✅
- [x] Implement Conveyor Platform
- [x] Implement Disguised Platform (Mimic)
- [x] Implement Kinetic Battery, Magnetic Siphon, Overdrive Module power-ups

### Sprint 10.6: Library Bosses, Achievements & Lore Finale ✅
- [x] Implement Thermal Hive, Gravity Anchor, The Forger mini-bosses
- [x] Implement The Architect, Entropy Core major bosses
- [x] Implement Void Walker, Resourceful, Untouchable, Infinite Ascent achievements
- [x] Add Pre-Signal Map, Biomechanical Shard artifacts
- [x] Add Origins, Signal Ghost lore logs
- [x] Define "Grand Ascension" set bonus (+25% Global Efficiency)

---

## 4. Governance

**Rule:** Every new gameplay element MUST trace to a Design Library entry. If a concept has no library precedent, it must be proposed in a documented rationale and approved before implementation. See `AGENTS.md` §12.

**EventDirector** (Meteor Shower, Nebula Fog): Deferred as pure invention — no library backing. Implement only after all approved library content ships.
