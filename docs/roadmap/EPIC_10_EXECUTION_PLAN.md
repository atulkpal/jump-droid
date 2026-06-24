# EPIC 10 — The Outer Reaches: Authoritative Execution Plan

**Author:** Architecture Agent
**Date:** 2026-06-25
**Status:** Ready to Execute
**Prerequisite:** EPIC 9 ✅ — `epic9-complete`
**Branch:** `epic10-outer-reaches`

---

## 1. Overview

EPIC 10 expands the vertical journey beyond the 15,000m "Void" milestone, reaching a final threshold of 100,000m. This expansion introduces data-driven encounter management, reality-warping platform mechanics, and scheduled environmental events.

---

## 2. Sprint Breakdown

### Sprint 10.0: Infrastructure Refactor (The Config Engine)

**Goal:** De-hardcode the encounter logic to allow for infinite zone expansion.

| # | Task | File(s) | Instructions |
|---|------|---------|--------------|
| 0.1 | Create `ZoneConfig` model | **New:** `ZoneConfig.kt` | `data class ZoneConfig(val zone: AltitudeZone, val spawnWeights: Map<String, Float>, val intensity: Float, val bossMilestone: String? = null)`. |
| 0.2 | Externalize Spawn Tables | **Edit:** `EncounterDirector.kt` | Remove all `when(currentZone)` blocks. Replace with a `val zoneConfigs: Map<AltitudeZone, ZoneConfig>` registry. |
| 0.3 | Generic Spawn Iterator | **Edit:** `EncounterDirector.kt` | Refactor `update()` to iterate over the active `ZoneConfig`. Use `intensity` to drive `spawnInterval`. |

---

### Sprint 10.1: Expansion of the Sky

**Goal:** Implement 4 new altitude zones with specialized visuals and thresholds.

| # | Task | File(s) | Instructions |
|---|------|---------|--------------|
| 1.1 | Add Altitude Thresholds | **Edit:** `Constants.kt` | Add `ZONE_THRESHOLD_BEYOND` (25k), `GATE` (45k), `CONSTRUCT` (70k), `SINGULARITY` (100k). |
| 1.2 | Update Zone Enum | **Edit:** `AltitudeZone.kt` | Add `THE_BEYOND`, `STELLAR_GATE`, `ANCIENT_CONSTRUCT`, `SINGULARITY` with localized subtitles. |
| 1.3 | New Visual Layers | **Edit:** `ZoneBackgroundRenderer.kt` | Implement gradients for 4 new zones. "The Beyond": Ethereal Cyan/Purple. "Stellar Gate": Artificial Amber/Black. "Ancient Construct": Monolithic Dark Grey. "Singularity": Inverted High-Contrast. |
| 1.4 | Parallax Expansion | **Edit:** `ZoneBackgroundRenderer.kt` | Add specialized layers: "The Beyond" (Shimmering nebula), "Stellar Gate" (Mega-structure silhouettes), "Ancient Construct" (Geometric monoliths). |

---

### Sprint 10.2: Mega-Structures & Flux Platforms

**Goal:** Reality-warping platform types.

| # | Task | File(s) | Instructions |
|---|------|---------|--------------|
| 2.1 | New Platform Types | **Edit:** `Models.kt` | Add `FLUX` and `GRAVITON` to `PlatformType`. |
| 2.2 | Flux Teleport Logic | **Edit:** `GameEngine.kt` (Collision) | If `platform.type == FLUX`, trigger horizontal teleport to `(screenWidth - player.x)` with 200ms cooldown. |
| 2.3 | Graviton Well Logic | **Edit:** `GameEngine.kt` (Physics) | If `platform.type == GRAVITON`, apply radial acceleration towards `platform.center` (similar to Magnetic but stronger/tighter). |
| 2.4 | Mega-Structure Renderers| **New:** `FluxRenderer.kt`, `GravitonRenderer.kt` | Register in `PlatformRendererRegistry`. |

---

### Sprint 10.3: The Event Director

**Goal:** Scheduled atmospheric hazards.

| # | Task | File(s) | Instructions |
|---|------|---------|--------------|
| 3.1 | Create `EventDirector` | **New:** `EventDirector.kt` | Manages `activeEvent: WorldEvent?`. Events trigger every 2-3 minutes. |
| 3.2 | Meteor Shower | **Edit:** `EventDirector.kt` | Periodically spawns high-speed `PROJECTILE` entities falling vertically with `impactFlash`. |
| 3.3 | Nebula Fog | **Edit:** `GameScreen.kt` | Triggers `globalFogAlpha` increase. Scales down `discoveryRange` by 60%. |
| 3.4 | Event HUD | **Edit:** `HudWidgets.kt` | Add "EVENT ACTIVE" warning banner below altitude display. |

---

### Sprint 10.4: Apex Threats

**Goal:** Elite high-altitude entities.

| # | Task | File(s) | Instructions |
|---|------|---------|--------------|
| 4.1 | Void Phantom AI | **Edit:** `ThreatAIUpdater.kt` | Stays invisible/invulnerable while player is thrusting. Attacks only during "coasting" or "falling" phases. |
| 4.2 | Stellar Harvester AI | **Edit:** `ThreatAIUpdater.kt` | Moves towards the nearest `PowerUp`. If reached, destroys the item and increases its own `health` + `size`. |
| 4.3 | Elite Renderers | **New:** `VoidPhantomRenderer.kt`, `HarvesterRenderer.kt` | Register in `ThreatRendererRegistry`. |

---

### Sprint 10.5: Final Ascension Lore

**Goal:** Narrative completion and endgame sets.

| # | Task | File(s) | Instructions |
|---|------|---------|--------------|
| 5.1 | Terminal Logs | **Edit:** `LoreLog.kt` | Add 10 logs for altitudes 25k to 100k. Focus on "The Source" and the origin of the Signal. |
| 5.2 | Endless Set Bonuses | **Edit:** `ArtifactSet.kt` | Add "Grand Ascension" set (all 4 new zones). Bonus: +25% Global Efficiency. |
| 5.3 | Final Achievement | **Edit:** `Achievements.kt` | "To the Stars": Reach 100,000m without continues. |

---

## 3. File Inventory

### New Files (6+)
- `ZoneConfig.kt` (Encounter data)
- `EventDirector.kt` (World events)
- `FluxRenderer.kt` (Platform visual)
- `GravitonRenderer.kt` (Platform visual)
- `VoidPhantomRenderer.kt` (Enemy visual)
- `HarvesterRenderer.kt` (Enemy visual)

### Modified Files (12+)
- `Constants.kt` (Thresholds)
- `AltitudeZone.kt` (Enum expansion)
- `ZoneBackgroundRenderer.kt` (Visuals)
- `EncounterDirector.kt` (Logic refactor)
- `Models.kt` (Types)
- `GameEngine.kt` (Mechanics)
- `ThreatAIUpdater.kt` (AI)
- `LoreLog.kt` (Narrative)
- `ArtifactSet.kt` (Sets)

---

## 4. Acceptance Criteria

- [ ] All 10 Altitude Zones (0m to 100,000m) are reachable and visually unique.
- [ ] `EncounterDirector` is fully data-driven with no zone-specific hardcoding.
- [ ] Flux and Graviton platforms function reliably in the new zones.
- [ ] World Events (Meteor Shower, Fog) trigger and modify gameplay.
- [ ] Void Phantom and Stellar Harvester exhibit correct AI patterns.
- [ ] Final lore logs and "Grand Ascension" set bonus integrated.
- [ ] Build green, `GameScreen.kt` remains under 2,200 lines.
