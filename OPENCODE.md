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
| `refactor/ui-extraction` | **Active** — Sprint T1 UI extraction from monolithic `GameScreen.kt` |
| `main` | Base branch; contains pre-refactor code |

All Phase 1 extraction work lives on `refactor/ui-extraction`. No merge to `main` yet.

## Documentation Locations

| Path | Contents |
|------|----------|
| `docs/CHANGELOG.md` | Dated engineering events |
| `docs/architecture/Refactor_T1_Phase1.md` | Phase 1 extraction report (completed) |
| `docs/architecture/Refactor_T1_Phase2_Plan.md` | Phase 2 implementation plan |
| `OPENCODE.md` | This file — session context |

## Current Active EPIC

**EPIC 5: Survival Protocol** (Sprint B) — Threats, survival economy, destruction system. Base commit `aee2c37`.

## Current Refactor Status

**Sprint T1 — Tidying:** Phase 1 complete, Phase 2 planned but not started.

| Phase | Status | Description |
|-------|--------|-------------|
| Phase 1 | ✅ Done | Extracted 15 UI files; fixed imports; resolved conflicts; build passing |
| Phase 2 | 📋 Planned | Replace inline screens/HUD/overlays with extracted composable calls |

## Completed Refactor Phases

- ✅ 15 UI composable files extracted from `GameScreen.kt`
- ✅ `AchievementsList` and `CodexCard` inline declarations removed from `GameScreen.kt`
- ✅ All import errors resolved in extracted files
- ✅ `./gradlew assembleDebug` builds with zero errors
- ✅ Architecture report (`Refactor_T1_Phase1.md`) committed
- ✅ Phase 2 plan (`Refactor_T1_Phase2_Plan.md`) committed
- ✅ `CHANGELOG.md` created with historical milestones

## Open Refactor Tasks

- [ ] Phase 2: Replace 7 screen branches with extracted composable calls
- [ ] Phase 2: Replace 6 HUD widgets with extracted composable calls
- [ ] Phase 2: Delete duplicate altitude display
- [ ] Phase 2: Replace 5 overlays with extracted composable calls
- [ ] Phase 2: Handle `PowerupBadge` and `MissionType.toIcon()` cleanup
- [ ] Future: Extract mission row cards (~124 lines) and floating texts (~17 lines) from HUD
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
| `GameScreen.kt` | Core game loop + all state management. Phase 2 will modify inline UI sections only. Do not touch lines 82–2999 (game loop, managers, physics). |
| `RocketRenderer.kt` | EPIC 5 visual redesign in progress (destruction sequence, shield plates). Pre-existing uncommitted changes. |
| Any `Threat*.kt`, `Mission*.kt`, `ComboManager.kt`, `ProgressionManager.kt`, `DiscoveryManager.kt` | Core gameplay managers. Not part of UI extraction scope. |

## Current Known Technical Debt

1. **GameScreen.kt at 4,292 lines** — Phase 2 will reduce to ~3,147 lines. Still large; future sprints may further decompose.
2. **Deprecated `LinearProgressIndicator`** — Used in `GameScreen.kt:3284` and `ArchiveScreen.kt:116`. Should migrate to lambda-based overload.
3. **Mission row cards inline** — 124-line animated mission UI still embedded in `GameScreen.kt`. Deferred past Phase 2.
4. **Floating combo texts inline** — 17-line per-frame rendering inside game loop. Deferred past Phase 2.
5. **`PowerupBadge` appears unused** — 23-line composable in `GameScreen.kt` with no call sites found. Candidate for deletion.
6. **Flat package structure** — All `.kt` files in `com.example.jump_droid`. Future: sub-packages for screens/overlays/hud/managers.
7. **No automated UI tests** — All verification is manual. Extracted composables make future test addition feasible.

## Next Recommended Engineering Tasks

1. Execute Phase 2: replace inline screens/HUD/overlays with extracted calls (per `Refactor_T1_Phase2_Plan.md`)
2. Delete `PowerupBadge` if confirmed unused
3. Move `MissionType.toIcon()` to `MissionType.kt`
4. Migrate deprecated `LinearProgressIndicator` usages
5. After Phase 2: merge `refactor/ui-extraction` to `main`
6. Resume EPIC 5 Sprint C work on `main`
