# EPIC 10 — The Outer Reaches: Authoritative Execution Plan (HYBRID)

**Author:** Architecture Agent
**Date:** 2026-06-25
**Status:** Active Development
**Prerequisite:** EPIC 9 ✅ — `epic9-complete`
**Branch:** `epic10-theOuterReach`

---

## 0. Design Library Governance

**All content in this EPIC derives from pre-existing `docs/design/` library entries.** Any deviation or invention must be:
1. Traced to a specific Library item (APPROVED or BACKLOG)
2. Documented with rationale for the evolution
3. Approved before implementation

**Rule:** Never invent new threats, platforms, zones, power-ups, or lore without first checking the Design Library. See `AGENTS.md` §12.

---

## 1. Overview

EPIC 10 expands the vertical journey from 15,000m to 100,000m while integrating **pre-existing approved library content** that was never implemented. The zone progression now incorporates **The Foundry** (6,000m) and **Chrono-Rift** (13,000m) from the Area Library alongside the new high-altitude zones.

**Total Zones: 12** (0m → 100,000m)

| Zone | Threshold | Library Source |
|------|-----------|----------------|
| Earth | 0m | Existing |
| Cloud Layer | 500m | Existing |
| Upper Atmosphere | 1,500m | Existing |
| Orbit | 4,000m | Existing |
| **The Foundry** | **6,000m** | **AREA_LIBRARY — APPROVED** |
| Deep Space | 8,000m | Existing |
| **Chrono-Rift** | **13,000m** | **AREA_LIBRARY — APPROVED** |
| The Void | 15,000m | Existing |
| The Beyond | 25,000m | AREA_LIBRARY — Event Horizon (Backlog evolved) |
| Stellar Gate | 45,000m | AREA_LIBRARY — The Foundry (high-altitude evolution) |
| Ancient Construct | 70,000m | New — monolithic fortress concept |
| Singularity | 100,000m | AREA_LIBRARY — Event Horizon (final evolution) |

---

## 2. Sprint Breakdown

### Sprint 10.0: Infrastructure Refactor (The Config Engine) ✅

**Goal:** De-hardcode the encounter logic.

| # | Task | File(s) | Library Origin |
|---|------|---------|----------------|
| 0.1 | Create `ZoneConfig` model | **New:** `ZoneConfig.kt` | Infrastructure |
| 0.2 | Externalize Spawn Tables | **Edit:** `EncounterDirector.kt` | Infrastructure |
| 0.3 | Generic Spawn Iterator | **Edit:** `EncounterDirector.kt` | Infrastructure |

---

### Sprint 10.1: Expansion of the Sky ✅

**Goal:** New altitude zone thresholds and visuals.

| # | Task | File(s) | Library Origin |
|---|------|---------|----------------|
| 1.1 | Add Altitude Thresholds | **Edit:** `Constants.kt` | Infrastructure |
| 1.2 | Update Zone Enum | **Edit:** `AltitudeZone.kt` | Infrastructure |
| 1.3 | New Visual Layers | **Edit:** `ZoneBackgroundRenderer.kt` | Infrastructure |
| 1.4 | Parallax Expansion | **Edit:** `ZoneBackgroundRenderer.kt` | Infrastructure |

---

### Sprint 10.2: Mega-Structures & Flux Platforms ✅

**Goal:** Reality-warping platform types.

| # | Task | File(s) | Library Origin |
|---|------|---------|----------------|
| 2.1 | New Platform Types | **Edit:** `Platform.kt` | PLATFORM_LIBRARY — Conveyor evolved → Flux |
| 2.2 | Flux Teleport Logic | **Edit:** `GameScreen.kt` | PLATFORM_LIBRARY — "Spatial Folding" concept |
| 2.3 | Graviton Well Logic | **Edit:** `GameScreen.kt` | PLATFORM_LIBRARY — Magnetic evolved + Gravity Shear |
| 2.4 | Platform Renderers | **New:** `FluxRenderer.kt`, `GravitonRenderer.kt` | PLATFORM_LIBRARY |

---

### Sprint 10.3: Library Zone Integration ✅

**Goal:** Insert pre-existing approved zones into the progression.

| # | Task | File(s) | Library Origin |
|---|------|---------|----------------|
| 3.1 | Add The Foundry zone | **Edit:** `Constants.kt`, `AltitudeZone.kt`, `Models.kt` | **AREA_LIBRARY — APPROVED** (5k-7k) |
| 3.2 | Add Chrono-Rift zone | **Edit:** `Constants.kt`, `AltitudeZone.kt`, `Models.kt` | **AREA_LIBRARY — APPROVED** (13k-15k) |
| 3.3 | Foundry background visuals | **Edit:** `ZoneBackgroundRenderer.kt` | AREA_LIBRARY — mechanical/industrial theme |
| 3.4 | Chrono-Rift background visuals | **Edit:** `ZoneBackgroundRenderer.kt` | AREA_LIBRARY — time-dilation/glitch theme |
| 3.5 | Zone ambient systems | **Edit:** `AmbientSystem.kt`, `CanvasEffects.kt`, `PlatformRenderer.kt` | AREA_LIBRARY |
| 3.6 | Encounter configs | **Edit:** `EncounterDirector.kt` | THREAT_LIBRARY spawn weights |
| 3.7 | Discovery & Lore entries | **Edit:** `DiscoveryManager.kt`, `LoreLog.kt`, `Models.kt` | AREA_LIBRARY + LORE_LIBRARY |

---

### Sprint 10.4: Library Threats & Entities

**Goal:** Implement approved threats from THREAT_LIBRARY.

| # | Task | File(s) | Library Origin |
|---|------|---------|----------------|
| 4.1 | Cryo-Mist hazard | **New/Edit:** ThreatRegistry, Renderer | ✅ **COMPLETE** |
| 4.2 | Mirror Shards hazard | **New/Edit:** ThreatRegistry, Renderer | ✅ **COMPLETE** |
| 4.3 | Gravity Shear hazard | **New/Edit:** ThreatRegistry, Renderer | ✅ **COMPLETE** |
| 4.4 | Heat Bat enemy | **New/Edit:** ThreatAIUpdater, Renderer | ✅ **COMPLETE** |
| 4.5 | Void Harvester enemy | **New/Edit:** ThreatAIUpdater, Renderer | ✅ **COMPLETE** |
| 4.6 | Phase Wraith enemy | **New/Edit:** ThreatAIUpdater, Renderer | ✅ **COMPLETE** |
| 4.7 | Gravity Ram enemy | **New/Edit:** ThreatAIUpdater, Renderer | ✅ **COMPLETE** |

**⚠ All new threat renderers must be registered in `ThreatRendererRegistry.kt`** — follow the existing pattern (e.g., `register("HAZ_LIGHTNING", ::LightningRenderer)`).

---

### Sprint 10.5: Library Platforms & Power-Ups

**Goal:** Implement approved platform types and power-ups.

| # | Task | File(s) | Library Origin |
|---|------|---------|----------------|
| 5.1 | Conveyor Platform | **Edit:** `Platform.kt`, `PlatformManager.kt`, `PlatformRenderer.kt` | ✅ **COMPLETE** |
| 5.2 | Disguised Platform (Mimic) | **Edit:** `Platform.kt`, `PlatformManager.kt`, `PlatformRenderer.kt` | ✅ **COMPLETE** |
| 5.3 | Kinetic Battery power-up | **Edit:** `PowerUpManager.kt`, `CanvasEffects.kt` | ✅ **COMPLETE** |
| 5.4 | Magnetic Siphon power-up | **Edit:** `PowerUpManager.kt`, `CanvasEffects.kt` | ✅ **COMPLETE** |
| 5.5 | Overdrive Module power-up | **Edit:** `PowerUpManager.kt`, `CanvasEffects.kt` | ✅ **COMPLETE** |

---

### Sprint 10.6: Library Mini-Bosses, Achievements & Lore Finale

**Goal:** Narrative completion and progression milestones.

| # | Task | File(s) | Library Origin |
|---|------|---------|----------------|
| 6.1 | Thermal Hive mini-boss | **New:** Renderer, ThreatRegistry | **THREAT_LIBRARY — APPROVED** |
| 6.2 | Gravity Anchor mini-boss | **New:** Renderer, ThreatRegistry | **THREAT_LIBRARY — APPROVED** |
| 6.3 | The Forger mini-boss | **New:** Renderer, ThreatRegistry | **THREAT_LIBRARY — APPROVED** |
| 6.4 | The Architect major boss | **New:** Renderer, ThreatRegistry | **THREAT_LIBRARY — APPROVED** |
| 6.5 | Entropy Core major boss | **New:** Renderer, ThreatRegistry | **THREAT_LIBRARY — APPROVED** |
| 6.6 | Void Walker achievement | **Edit:** `Achievements.kt` | **ACHIEVEMENT_LIBRARY — APPROVED** |
| 6.7 | Resourceful achievement | **Edit:** `Achievements.kt` | **ACHIEVEMENT_LIBRARY — APPROVED** |
| 6.8 | Untouchable achievement | **Edit:** `Achievements.kt` | **ACHIEVEMENT_LIBRARY — APPROVED** |
| 6.9 | Infinite Ascent achievement | **Edit:** `Achievements.kt` | **ACHIEVEMENT_LIBRARY — APPROVED** |
| 6.10 | Pre-Signal Map artifact | **Edit:** `Models.kt`, `ArtifactSet.kt` | **ARTIFACT_LIBRARY — APPROVED** |
| 6.11 | Biomechanical Shard artifact | **Edit:** `Models.kt`, `ArtifactSet.kt` | **ARTIFACT_LIBRARY — APPROVED** |
| 6.12 | Origins lore log | **Edit:** `LoreLog.kt` | **LORE_LIBRARY — APPROVED** |
| 6.13 | Signal Ghost lore log | **Edit:** `LoreLog.kt` | **LORE_LIBRARY — APPROVED** |
| 6.14 | Grand Ascension set bonus | **Edit:** `ArtifactSet.kt` | New — +25% Global Efficiency for all new zones |

---

## 3. File Inventory

### New Files (10+)
- `ZoneConfig.kt` — ✅ Complete
- `FluxRenderer.kt` — ✅ Complete
- `GravitonRenderer.kt` — ✅ Complete
- `EventDirector.kt` — Sprint 10.7 (Deferred — pure EPIC 10 invention, see note below)
- Cryo-Mist, Mirror Shards, Gravity Shear, Heat Bat, Void Harvester, Phase Wraith, Gravity Ram renderers — Sprint 10.4
- Thermal Hive, Gravity Anchor, The Forger, The Architect, Entropy Core renderers — Sprint 10.6
- Conveyor platform renderer — Sprint 10.5
- Disguised platform renderer — Sprint 10.5

### Modified Files (18+)
- `Constants.kt` — Thresholds ✅
- `AltitudeZone.kt` — Enum expansion ✅
- `ZoneBackgroundRenderer.kt` — Visuals ✅
- `Models.kt` — DiscoveryType ✅
- `EncounterDirector.kt` — Logic refactor ✅
- `DiscoveryManager.kt` — Zone mappings ✅
- `LoreLog.kt` — Narrative ✅
- `Platform.kt` — Types ✅
- `PlatformManager.kt` — Spawn rules ✅
- `PlatformRenderer.kt` — Zone handling ✅
- `FluxRenderer.kt` — New ✅
- `GravitonRenderer.kt` — New ✅
- `GameScreen.kt` — Collision logic ✅
- `AmbientSystem.kt` — Zone ambient ✅
- `CanvasEffects.kt` — Zone color ✅
- `ThreatAIUpdater.kt` — AI — Sprint 10.4
- `ArtifactSet.kt` — Sets — Sprint 10.6
- `Achievements.kt` — Achievements — Sprint 10.6

---

## 4. Acceptance Criteria

- [ ] All 12 Altitude Zones (0m to 100,000m) are reachable and visually unique.
- [ ] **The Foundry** and **Chrono-Rift** are fully integrated (library-approved zones implemented).
- [ ] `EncounterDirector` is fully data-driven with no zone-specific hardcoding.
- [ ] Flux and Graviton platforms function reliably in the new zones.
- [ ] At least 4 library-approved threats from THREAT_LIBRARY are implemented (Sprint 10.4).
- [ ] At least 2 library-approved platforms from PLATFORM_LIBRARY are implemented (Sprint 10.5).
- [ ] At least 2 library-approved power-ups from POWERUP_LIBRARY are implemented (Sprint 10.5).
- [ ] Library-approved mini-bosses and major bosses from THREAT_LIBRARY are implemented (Sprint 10.6).
- [ ] Library-approved achievements from ACHIEVEMENT_LIBRARY are implemented (Sprint 10.6).
- [ ] Lore logs and "Grand Ascension" set bonus integrated.
- [ ] Build green, `GameScreen.kt` remains under 2,200 lines.

---

## 5. Governance Note

**EventDirector** (originally Sprint 10.3): Deferred. The EventDirector system (Meteor Shower, Nebula Fog) is pure invention with no backing in any Design Library. It should only be implemented after all library-approved content is shipped, or with explicit approval and a documented rationale.
