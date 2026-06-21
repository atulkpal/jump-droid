# Jump Droid — OpenCode Context

## Project Overview

Jump Droid is an Android game built with Jetpack Compose. The player pilots a rocket through ascending altitude zones, managing fuel, heat, shields, and hull integrity while dodging threats and completing missions.

- **Language:** Kotlin
- **UI:** Jetpack Compose (Material3)
- **Min SDK:** 26 (Android 8.0)
- **Build:** Gradle with Kotlin DSL
- **Game loop:** `LaunchedEffect` + `withFrameNanos` in `GameScreen.kt`

## Current Branch Strategy

| Branch | Purpose |
|--------|---------|
| `refactor/t4-recovery` | **Active** — Sprint T4 stabilized |
| `refactor/logic-extraction` | Sprint T3 logic extraction (merged) |
| `development` | integration branch for all work (EPIC 5 Sprint C complete) |
| `refactor/ui-extraction` | Completed — Sprint T1 + T2 UI extraction, fully merged into `development` |
| `main` | Base branch; contains pre-refactor code |

Git tags: `refactor-t1-phase1`, `refactor-t1-phase2`, `refactor-t2`

## Documentation Locations

| Path | Contents |
|------|----------|
| `docs/CHANGELOG.md` | Dated engineering events |
| `docs/architecture/Refactor_T1_Phase1.md` | Phase 1 extraction report (completed) |
| `docs/architecture/Refactor_T1_Phase2_Plan.md` | Phase 2 implementation plan |
| `docs/architecture/Refactor_T1_Phase2_Report.md` | Phase 2 completion report (completed) |
| `docs/architecture/Refactor_T2_Plan.md` | T2 plan (T2A low risk + T2B medium risk) |
| `docs/architecture/Refactor_T2A_Report.md` | T2A completion report (completed) |
| `docs/architecture/Refactor_T2B_Report.md` | T2B completion report (completed) |
| `docs/architecture/Refactor_T3_Plan.md` | T3 plan (logic extraction) |
| `docs/architecture/Refactor_T3_Report.md` | T3 completion report (completed) |
| `docs/architecture/Refactor_T4_Plan.md` | T4 plan (system delegation) |
| `docs/architecture/Refactor_T4_Report.md` | T4 completion report (STABILIZED) |
| `docs/design/ENGINE_EXTENSIONS.md` | Engine expansion system details |
| `OPENCODE.md` | This file — session context |

## Current Active EPIC

**EPIC 6: Visual Overhaul** (Sprint 2-3) — Screen polish, HUD revamp, animated starfields, premium feel. Active on `development`.

## Current Refactor Status

**Sprint T1 + T2 — Tidying:** Fully complete.
**Sprint 2-3 — Visual Overhaul:** Complete (premium pass on all screens + HUD).

| Phase | Status | Description |
|-------|--------|-------------|
| T1 Phase 1 | ✅ Done | Extracted 15 UI files; fixed imports; resolved conflicts; build passing |
| T1 Phase 2 | ✅ Done | Replaced all inline screens/HUD/overlays with extracted composable calls |
| T2 Phase A | ✅ Done | Extracted 4 low-risk inline composables (TopRightUtilityButtons, MissionRow, FloatingTextsLayer, GaugeWrappers) |
| T2 Phase B | ✅ Done | Extracted 8 Canvas effects into `CanvasEffects.kt` (ground, speed lines, particles, landing rings, powerups, flying rewards, impact flash, reality distortion) |
| T3 | ✅ Done | Extracted `NotificationManager`, `FloatingTextManager`, `PlatformManager`, and expanded `ProgressionManager` |
| T4 | ✅ Done | System delegation of survival, director, and threat interaction logic (Stabilized) |
| Engine | ✅ Done | Projectiles, Input Buffer, Tethers, Visual Fog, and E2E Expansion |
| Sprint C | ✅ Done | Structural fixes, Boss/Enemy Projectile systems, Zone redistribution |

## Completed Refactor Phases

- ✅ 15 UI composable files extracted from `GameScreen.kt`
- ✅ `AchievementsList` and `CodexCard` inline declarations removed from `GameScreen.kt`
- ✅ All import errors resolved in extracted files
- ✅ `./gradlew assembleDebug` builds with zero errors
- ✅ Architecture report (`Refactor_T1_Phase1.md`) committed
- ✅ Phase 2 plan (`Refactor_T1_Phase2_Plan.md`) committed
- ✅ `CHANGELOG.md` created with historical milestones
- ✅ All 7 screen branches replaced with extracted composable calls
- ✅ All 6 HUD widgets replaced + duplicate altitude deleted
- ✅ All 5 overlays replaced with extracted composable calls
- ✅ `PowerupBadge` removed (unused, zero call sites)
- ✅ Phase 2 completion report (`Refactor_T1_Phase2_Report.md`) committed
- ✅ Git tags `refactor-t1-phase1` and `refactor-t1-phase2` created
- ✅ T2 plan (`Refactor_T2_Plan.md`) created
- ✅ `TopRightUtilityButtons.kt` extracted (help + pause buttons)
- ✅ `MissionRow.kt` extracted (mission card row, 108 lines saved)
- ✅ `FloatingTextsLayer.kt` extracted (floating text overlay, 16 lines saved)
- ✅ `LeftGauges` / `RightGauges` appended to `HudWidgets.kt` (22 lines saved)
- ✅ GameScreen.kt reduced from 3,326 → 3,179 lines (−147, −4.4%)
- ✅ `CanvasEffects.kt` created with 8 `DrawScope` extension functions
- ✅ All 8 Canvas effects extracted from GameScreen.kt (ground, speed lines, particles, landing rings, powerups, flying rewards, impact flash, reality distortion)
- ✅ GameScreen.kt reduced from 3,179 → 3,109 lines (−70, −2.2%)
- ✅ `NotificationManager.kt` created; manual queue logic encapsulated
- ✅ `FloatingTextManager.kt` created; popup lifecycle managed
- ✅ `PlatformManager.kt` created; generation math and streaks moved
- ✅ `ProgressionManager.kt` expanded; achievement/high score logic delegated
- ✅ `SurvivalManager.kt` created; damage and regen logic extracted
- ✅ `EncounterDirector.kt` created; spawning and boss thresholds extracted
- ✅ Massive threat interaction block (400+ lines) delegated to `ActiveThreat.kt`
- ✅ GameScreen.kt reduced from 3,109 → 2,538 lines (−571, −18.4%)
- ✅ Cumulative game loop reduction: 4,344 → 2,538 lines (−41.6%)
- ✅ Positioning regression fixes: `AltitudeDisplay` alignment + `NotificationLayer` text/position (`72594b5`)
- ✅ Merged into `development` (`af3d0ae`)
- ✅ Engine expansion: `ProjectileManager`, `InputBufferManager`, `Tether` physics, and `Fog` rendering implemented
- ✅ `ActiveThreat` signature updated for E2E interaction support
- ✅ **Visual Overhaul Sprint 2-3** (`35e03ce`): 9 threat rendering fixes, animated emoji gauge icons, main menu rewrite with animated starfield/scan radar/pulsing borders
- ✅ **Premium Screen Pass** (`47ee28e`): TitleScreen scanning drone with Z-axis depth + radar sweep beam, animated starfields with twinkle on Hangar/Settings/About/GameOver/Pause screens, text wrapping fixes, status footers, text shadows
- ✅ **HUD Revamp** (`afbecdc`, `47d4191`): Wider gauge bars (10dp), gradient fills, numeric value + percentage labels, segment ticks, shield shimmer, thinner premium card borders, neutral track backgrounds
- ✅ **Sprint C Completion** (`a1accfb`): Structural fix for boss AI, projectile systems for all 6 bosses and 2 enemies, zone redistribution, difficulty scaling, and 11 entity bug fixes.

## Open Refactor Tasks

- [ ] Future: Add package structure (separate `screens/`, `overlays/`, `hud/` packages)

## Coding Standards

- **No comments in code** — let the code speak. Use documentation files for explanations.
- **Minimal imports** — only import what is used.
- **Composables** — receive explicit parameters; no God-objects.
- **Same-package references** — extracted composables remain in `com.example.jump_droid`; no cross-package imports needed for Phase 2.
- **Compose conventions** — use `@Composable` functions, `remember` for state, `Modifier` chaining.

## Commit Conventions

```
<type>: <short description>

Types:
  Refactor   — structural change with no behavioral impact
  Docs       — documentation only (CHANGELOG, architecture reports)
  Fix        — bug or import fix
  Feat       — new feature (EPIC work)
```

## Changelog Process

Every notable engineering event gets a dated entry in `docs/CHANGELOG.md` with: Date, Sprint/Phase, Branch, Commit, Status, Added, Changed, Fixed, Validation, Notes.

## Files That Should Not Be Modified Casually

| File | Reason |
|------|--------|
| `GameScreen.kt` | Core game loop + all state management. T1+T2 complete (3,109 lines). Do not touch lines 82–2500 (game loop, managers, physics). |
| `RocketRenderer.kt` | EPIC 5 visual redesign in progress (destruction sequence, shield plates). Pre-existing uncommitted changes. |
| Any `Threat*.kt`, `Mission*.kt`, `ComboManager.kt`, `ProgressionManager.kt`, `DiscoveryManager.kt` | Core gameplay managers. Not part of UI extraction scope. |

## Current Known Technical Debt

1. **GameScreen.kt at ~3,900 lines** — Threat rendering additions and visual polish. Still large; future sprints may further decompose.
2. **Deprecated `LinearProgressIndicator`** — Used in ArchiveScreen. Should migrate to lambda-based overload.
3. **Threat entity rendering inline** — ~920 lines of Canvas draw code. Deferred indefinitely.
4. **`PowerupBadge` deleted** — Was unused with zero call sites. Removed in Phase 2.
5. **Flat package structure** — All `.kt` files in `com.example.jump_droid`. Future: sub-packages for screens/overlays/hud/managers.
6. **No automated UI tests** — All verification is manual. Extracted composables make future test addition feasible.

## Next Recommended Engineering Tasks

1. EPIC 5 Sprint C is complete on `development`
2. Future sprints: gameplay balance pass, Star-Eater phase rewrite, new zone bosses
3. Move `MissionType.toIcon()` to `MissionType.kt`
4. Migrate deprecated `LinearProgressIndicator` usages
