# EPIC 11 — Ascension (The End): Authoritative Execution Plan

**Author:** Architecture Agent
**Date:** 2026-06-25
**Status:** APPROVED (with 7 amendments from architectural review)
**Branch:** `epic11-ascension`
**Prerequisites:** EPIC 10 ✅ — `epic10-complete` tag

---

## 0. Design Library Governance

STRICT ADHERENCE RULE: All gameplay content must be traced to `docs/design/` libraries.

| Content | Library Trace |
|---------|---------------|
| The Singularity | `THREAT_LIBRARY.md` (§4, BACKLOG — Major Boss 4) |
| Point Zero / Event Horizon | `AREA_LIBRARY.md` (§B, BACKLOG — Future Area 3: Event Horizon) |
| Omega Relics | `ARTIFACT_LIBRARY.md` (§ Legendary Tier) |
| MOD_VOID_ENGINE / MOD_SINGULARITY_CORE | `ROCKET_LIBRARY.md` (must be added before coding) |

---

## 1. Overview

EPIC 11 serves as the final narrative and mechanical conclusion of Jump Droid. It introduces the final destination (100,000m+), a meta-fictional final boss encounter, and the "Prestige" loop required for long-term player retention.

### Final Zone Specification: Point Zero

- **Threshold:** 100,000m (origin-reset to 0 after transition)
- **Theme:** Non-Euclidean white-noise space
- **Mechanic:** Physics Flux — Gravity and Thrust constants fluctuate by ±20% every 1,000m, requiring the player to adapt their propulsion rhythm.

---

## 2. Amendments Accepted from Review

The following 7 amendments from the EPIC 10 architectural review are incorporated into this plan:

| # | Amendment | Implementation |
|---|-----------|----------------|
| A1 | **Sprint 11.1 split** — Origin Reset and Boss AI are independent | Moved 1.3 (Control Glitch) to Sprint 11.1; extracted 11.0 infra sprint |
| A2 | **GameScreen.kt budget** — currently ~1,900 lines, 2,200 budget too tight for glitch logic | Sprint 11.0 extracts `PlayerInputProcessor.kt` before any GameScreen additions |
| A3 | **Design Library First** — Omega Modules need ROCKET_LIBRARY.md entries before coding | Sprint 11.0 includes library doc updates |
| A4 | **Missing doc updates** — AREA_LIBRARY.md, THREAT_LIBRARY.md, ARTIFACT_LIBRARY.md | Sprint 11.0 covers all library doc changes |
| A5 | **Acceptance Criteria missing build verification** | AC includes `gradle_build` success and no new deprecation warnings |
| A6 | **Eternal Mode scaling cap** — infinite scaling will hit Float overflow | `minSpawnInterval` floor + `maxSpeed` ceiling + graceful degradation at ~500,000m |
| A7 | **Sprint renumbering** — 11.0 infra sprint shifts remaining sprints | See sprint breakdown below |

---

## 3. Sprint Breakdown

### Sprint 11.0: Infrastructure & Documentation

**Goal:** Prepare the codebase for EPIC 11 additions without exceeding GameScreen budget. Update all design libraries.

| # | Task | Target File(s) | Description |
|---|------|----------------|-------------|
| 0.1 | **Extract PlayerInputProcessor** | New: `PlayerInputProcessor.kt`; Modified: `GameScreen.kt` | Extract player input processing (glitch factor, control inversion, thrust) from GameScreen into dedicated processor. Keeps GameScreen under 2,200 lines. |
| 0.2 | **Update Design Libraries** | `docs/design/ROCKET_LIBRARY.md`, `AREA_LIBRARY.md`, `THREAT_LIBRARY.md`, `ARTIFACT_LIBRARY.md` | Add MOD_VOID_ENGINE, MOD_SINGULARITY_CORE to Rocket Library. Add Point Zero to Area Library. Promote The Singularity from BACKLOG to APPROVED in Threat Library. Add "The Architect's Signature" artifact to Artifact Library. |
| 0.3 | **Architecture Review** | All | Verify no regressions from extraction. `gradle_build` green. |

### Sprint 11.1: The Meta-Boss & Spatial Warping

**Goal:** Implement the final mechanical challenge and technical stability for extreme altitudes.

| # | Task | Target File(s) | Description |
|---|------|----------------|-------------|
| 1.1 | **Origin Reset Logic** | `GameEngine.kt` | At 100,000m, trigger a coordinate shift where cameraY and playerY are normalized to 0 to prevent floating-point jitter. Must occur during ASCENSION_PROTOCOL transition with active threats cleared. |
| 1.2 | **The Singularity AI** | `ThreatAIUpdater.kt` | Implement "HUD Pull" logic. Boss periodically pulls HUD elements toward the center of the screen, creating visual obstruction. |
| 1.3 | **Control Glitch Hook** | `PlayerInputProcessor.kt` (extracted in 11.0) | Add `glitchFactor` to player input processing that causes intermittent 50ms delays or horizontal drift during the boss fight. |
| 1.4 | **Final Boss Renderer** | `SingularityRenderer.kt` | A shifting, formless mass of white noise and geometric fragments. Registered in `ThreatRendererRegistry`. |
| 1.5 | **HUD Widget Pull Animation** | `HudWidgets.kt` | Use Offset animations rather than modifying absolute layout when Singularity pulls HUD elements. Requires confirming `HudWidgets.kt` exists or extracting HUD rendering into it. |

### Sprint 11.2: The Ascension Protocol (The Ending)

**Goal:** Narrative closure and 100% completion ceremony.

| # | Task | Target File(s) | Description |
|---|------|----------------|-------------|
| 2.1 | **Ceremony State** | `GameState.kt` | Add `GameState.ASCENSION_PROTOCOL`. A non-interactive cinematic state triggered after defeating The Singularity. |
| 2.2 | **The Final Truth UI** | `AscensionOverlay.kt` | A full-screen text/visual crawl delivering the final narrative logs and the "Hall of Pioneers" (Credits). |
| 2.3 | **Epilogue Discoveries** | `Models.kt` | Add `DISCOVERY_THE_END` and the final artifact: "The Architect's Signature." |

### Sprint 11.3: Prestige & Infinite Ascent

**Goal:** Establishing the end-game loop and "Omega" power scaling.

| # | Task | Target File(s) | Description |
|---|------|----------------|-------------|
| 3.1 | **Prestige System** | `ProgressionManager.kt` | Allow "Ascension Reset." Player resets altitude/unlocks for a permanent +10% multiplier to Hull or Shield. |
| 3.2 | **Omega Modules** | `ModuleRegistry.kt` | Implement 2 Legendary modules: `MOD_VOID_ENGINE` (Infinite Fuel) and `MOD_SINGULARITY_CORE` (Perfect Stability). **Only equipable in Eternal Mode or Prestige Tiers.** |
| 3.3 | **Eternal Mode Scaling** | `EncounterDirector.kt` | Beyond 100,000m, enable "Eternal Mode" where enemy density and speed scale infinitely. **Capped:** `minSpawnInterval` floor, `maxSpeed` ceiling, graceful degradation at ~500,000m. |

---

## 4. Technical & Compatibility Notes

### Challenges & Solutions

| Challenge | Solution |
|-----------|----------|
| **HUD Stability** — Singularity boss moves UI gauges | Use `Offset` animations in `HudWidgets.kt` rather than modifying absolute layout |
| **Origin Reset** — Coordinate shift mid-run can break projectile/threat tracking | Reset must occur during `GameState.ASCENSION_PROTOCOL` transition while active threats are cleared |
| **Omega Modules** — Game-breaking power | Only equipable in "Eternal Mode" or "Prestige Tiers" |
| **Eternal Mode Overflow** — Infinite scaling hits Float limit | `minSpawnInterval` floor, `maxSpeed` ceiling, graceful degradation at ~500,000m |

### Compatibility

- Uses the `GameStats` and Intelligence Network framework from EPIC 10.
- Zero changes required to `Platform.kt` or `ActiveThreat.kt` base structures.
- Compatible with all current Rocket Classes (Striker, Heavy, etc.).
- `PlayerInputProcessor.kt` extraction reduces GameScreen.kt line count, clearing budget for glitch logic.

---

## 5. Acceptance Criteria

- [ ] `gradle_build` succeeds with no new deprecation warnings (pre-existing `LinearProgressIndicator` in `ArchiveScreen.kt` is acceptable).
- [ ] Origin Reset logic successfully prevents visual jitter above 100,000m.
- [ ] The Singularity boss triggers HUD distortion and control glitches as specified.
- [ ] Ascension Ceremony (Credits/Narrative) triggers upon boss defeat.
- [ ] Prestige System correctly resets progress and applies permanent multipliers.
- [ ] Final "Epilogue" Lore Log is readable in the Archive.
- [ ] Eternal Mode has capped scaling with graceful degradation.
- [ ] GameScreen.kt remains under the 2,200-line budget.
- [ ] All design library files are updated before code changes begin.
