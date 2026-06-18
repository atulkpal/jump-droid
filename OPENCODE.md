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
| `refactor/logic-extraction` | **Active** — Sprint T3 logic extraction |
| `refactor/system-delegation` | Failed — Sprint T4 delegation (reverted) |
| `development` | integration branch for all work (EPIC 5 Sprint C next) |
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
| `docs/architecture/Refactor_T4_Report.md` | T4 completion report (FAILED / REVERTED) |
| `OPENCODE.md` | This file — session context |

## Current Active EPIC

**EPIC 5: Survival Protocol** (Sprint B) — Threats, survival economy, destruction system. Base commit `aee2c37`.

## Current Refactor Status

**Sprint T1 + T2 — Tidying:** Fully complete.

| Phase | Status | Description |
|-------|--------|-------------|
| T1 Phase 1 | ✅ Done | Extracted 15 UI files; fixed imports; resolved conflicts; build passing |
| T1 Phase 2 | ✅ Done | Replaced all inline screens/HUD/overlays with extracted composable calls |
| T2 Phase A | ✅ Done | Extracted 4 low-risk inline composables (TopRightUtilityButtons, MissionRow, FloatingTextsLayer, GaugeWrappers) |
| T2 Phase B | ✅ Done | Extracted 8 Canvas effects into `CanvasEffects.kt` (ground, speed lines, particles, landing rings, powerups, flying rewards, impact flash, reality distortion) |
| T3 | ✅ Done | Extracted `NotificationManager`, `FloatingTextManager`, `PlatformManager`, and expanded `ProgressionManager` |
| T4 | ❌ Failed | Attempted delegation of survival, director, and threat interaction logic (REVERTED) |

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
- ✅ GameScreen.kt reduced from 3,109 → 3,033 lines (−76, −2.4%)
- ✅ Cumulative game loop reduction: 4,344 → 3,033 lines (−30.2%)
- ✅ Positioning regression fixes: `AltitudeDisplay` alignment + `NotificationLayer` text/position (`72594b5`)
- ✅ Merged into `development` (`af3d0ae`)

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

1. **GameScreen.kt at 3,033 lines** — Down from 4,344 (-30.2%). Still large; future sprints may further decompose.
2. **Deprecated `LinearProgressIndicator`** — Used in `GameScreen.kt:3284` and `ArchiveScreen.kt:116`. Should migrate to lambda-based overload.
3. **Threat entity rendering inline** — ~826 lines of Canvas draw code. Deferred indefinitely.
4. **`PowerupBadge` deleted** — Was unused with zero call sites. Removed in Phase 2.
5. **Flat package structure** — All `.kt` files in `com.example.jump_droid`. Future: sub-packages for screens/overlays/hud/managers.
6. **No automated UI tests** — All verification is manual. Extracted composables make future test addition feasible.

## Next Recommended Engineering Tasks

1. Resume EPIC 5 Sprint C work on `development`
2. Move `MissionType.toIcon()` to `MissionType.kt`
3. Migrate deprecated `LinearProgressIndicator` usages
