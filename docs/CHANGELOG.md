# Changelog

All notable changes to this project are recorded as dated engineering events.

---

## 2026-06-18

**Sprint / Phase:** Refactor Sprint T4 Recovery

**Branch:** `refactor/t4-recovery`

**Status:** COMPLETE AND STABILIZED

### Added
- `SurvivalManager.kt` — Extracted damage and destruction lifecycle logic.
- `EncounterDirector.kt` — Extracted spawn rules and boss milestones.

### Changed
- `GameScreen.kt`: Delegated 400+ lines of threat interaction to `ActiveThreat.kt`.
- `GameScreen.kt`: Integrated `SurvivalManager` and `EncounterDirector`.
- `ActiveThreat.kt`: Added `processInteraction` for delegated proximity logic.

### Fixed
- **Mission Lifecycle**: Restored `missionManager.selectNextMission()` to fix ceremony loops and combo HUD instability.
- **Spawn Density**: Expanded enemy routing for `UPPER_ATMOSPHERE` and tuned probabilities to development baseline.
- **Thread Safety**: Applied `.toList()` snapshots to all critical StateList iterations to resolve `ConcurrentModificationException`.
- **Continue Recovery**: Fixed `continueRun()` state reset to prevent immediate death-loop on hull destruction.

### Notes
- T4 architecture is now fully validated and production-ready.
- Cumulative game loop reduction: 4,344 → 2,538 lines (−41.6%).

---

## 2026-06-18

**Sprint / Phase:** Refactor Sprint T3 — Logic & Manager Extraction

**Branch:** `refactor/logic-extraction`

**Commit:** `e79068d`

**Status:** Completed

### Added
- `NotificationManager.kt` — Encapsulates notification queue and timer logic
- `FloatingTextManager.kt` — Manages lifecycle of floating status text popups
- `PlatformManager.kt` — Encapsulates platform generation and streak tracking

### Changed
- `GameScreen.kt`: 20+ call sites updated to use `NotificationManager`
- `GameScreen.kt`: 15+ call sites updated to use `FloatingTextManager`
- `ProgressionManager.kt`: Added `saveHighScore`, `checkUnlocks`, and `wipeData` methods
- `GameScreen.kt`: Achievement and high score logic delegated to `ProgressionManager`
- `GameScreen.kt`: Platform generation delegated to `PlatformManager`
- Net reduction: 76 lines (3,109 → 3,033)

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL
- Verified on emulator

### Notes
- This sprint shifted the focus from UI extraction to logic modularization.
- Core game loop remains in `GameScreen.kt` but is now significantly leaner.

---

## 2026-06-18

**Sprint / Phase:** Refactor Sprint T1 — Phase 1

**Branch:** `refactor/ui-extraction`

**Commit:** `fb93e9d`

**Status:** Completed

### Added
- 15 standalone UI composable files extracted from `GameScreen.kt`:
  - Screens: `TitleScreen.kt`, `MainMenuScreen.kt`, `HangarScreen.kt`, `ArchiveScreen.kt`, `SettingsScreen.kt`, `AboutScreen.kt`, `LeaderboardScreen.kt`
  - Overlays: `PauseOverlay.kt`, `TutorialOverlay.kt`, `HelpOverlay.kt`, `UnlockOverlay.kt`, `GameOverOverlay.kt`
  - HUD widgets: `HudWidgets.kt` (8 functions: `AltitudeDisplay`, `FuelGauge`, `HeatGauge`, `ShieldGauge`, `IntegrityGauge`, `ComboHudBar`, `NotificationLayer`, `ZoneDiscoveryCard`)
  - Shared components: `CodexCard.kt`, `Achievements.kt`
- Architecture report: `docs/architecture/Refactor_T1_Phase1.md`
- Phase 2 implementation plan: `docs/architecture/Refactor_T1_Phase2_Plan.md`

### Changed
- `GameScreen.kt`: removed inline `AchievementsList` val (moved to `Achievements.kt`)
- `GameScreen.kt`: removed inline `CodexCard` composable (moved to `CodexCard.kt`)
- Net reduction: 52 lines (4,344 → 4,292)

### Fixed
- Import errors in all 15 extracted files:
  - `graphicsLayer` wrong package path (`foundation.layout` → `ui.graphics`)
  - Missing imports: `border`, `shadow`, `CircleShape`, `statusBarsPadding`, `fillMaxHeight`, `offset`, `safeDrawingPadding`, `sp`
  - Invalid `roundToPx` import removed (function resolves via `Dp` member without explicit import)
  - Duplicate `dp` import in `TitleScreen.kt` removed

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL (zero errors, 2 pre-existing deprecation warnings)
- All 15 extracted files compile alongside `GameScreen.kt`
- Dex artifact produced without issues

### Notes
- GameScreen.kt still contains inline implementations for all screens, overlays, and HUD widgets. The extracted files are defined but not yet called. Phase 2 will perform the replacement.

---

## 2026-06-18

**Sprint / Phase:** Refactor Sprint T1 — Phase 2

**Branch:** `refactor/ui-extraction`

**Commit:** `686bfd0`

**Tags:** `refactor-t1-phase1`, `refactor-t1-phase2`

**Status:** Completed

### Added
- Phase 2 completion report: `docs/architecture/Refactor_T1_Phase2_Report.md`
- `refactor-t1-phase1` and `refactor-t1-phase2` git tags

### Changed
- `GameScreen.kt`: replaced all 7 screen branches with extracted composable calls (TITLE, MAIN_MENU, HANGAR, ARCHIVE, SETTINGS, ABOUT, LEADERBOARD)
- `GameScreen.kt`: replaced 6 HUD widgets with extracted calls (AltitudeDisplay, FuelGauge, HeatGauge, ShieldGauge, IntegrityGauge, ComboHudBar, NotificationLayer, ZoneDiscoveryCard)
- `GameScreen.kt`: deleted duplicate altitude display block
- `GameScreen.kt`: replaced 5 overlays with extracted calls (PauseOverlay, TutorialOverlay, HelpOverlay, UnlockOverlay, GameOverOverlay)
- Net reduction: 966 lines (4,292 → 3,326)

### Fixed
- `ComboHudBar` parameter type mismatch: `getWindowForCombo` required lambda wrapper `{ comboManager.getWindowForCombo(it) }`

### Removed
- `PowerupBadge` composable — unused (zero call sites, 23 lines)

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL (zero errors)
- Brace balance: 681 `{` = 681 `}` ✅
- APK installed and launched on emulator (`Medium_Phone API 35`)
- `adb shell monkey` — `Events injected: 1`

### Notes
- Mission row cards (~124 lines) and floating combo texts (~17 lines) remain inline — deferred past Phase 2
- `MissionType.toIcon()` extension kept — still called by inline mission cards
- Refactor Sprint T1 is fully complete. Branch ready to merge to `main`.

---

## 2026-06-18

**Sprint / Phase:** Refactor Sprint T2 — Phase A (Low Risk)

**Branch:** `refactor/ui-extraction`

**Base Commit:** `686bfd0` (T1 Phase 2)

**Status:** Completed

### Added
- `TopRightUtilityButtons.kt` — Help (`?`) + Pause (`||`) buttons (23 lines)
- `MissionRow.kt` — Mission card row with ceremony stages, progress bars, AnimatedContent (108 lines)
- `FloatingTextsLayer.kt` — Animated floating text overlay (16 lines)
- `LeftGauges` / `RightGauges` appended to `HudWidgets.kt` — Column wrappers for fuel/heat/shield/integrity gauges (22 lines)
- T2 plan: `docs/architecture/Refactor_T2_Plan.md`

### Changed
- `GameScreen.kt`: 5 inline blocks replaced with extracted composable calls
- Net reduction: 147 lines (3,326 → 3,179)

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL (11s, zero errors)
- ADB install — Success
- Emulator launch — Events injected: 1

### Notes
- All 4 extractions use explicit parameter patterns; `BoxScope`-dependent composables accept `Modifier` parameter
- T2B (Canvas effects, ~104 lines) is planned but not started
- Threat entity rendering (~826 lines) remains inline indefinitely

---

## 2026-06-18

**Sprint / Phase:** Refactor Sprint T2 — Phase B (Medium Risk)

**Branch:** `refactor/ui-extraction`

**Base Commit:** `2fe24f1` (T2A)

**Status:** Completed

### Added
- `CanvasEffects.kt` — 8 `DrawScope` extension functions for canvas rendering (168 lines)

### Changed
- `GameScreen.kt`: 8 inline Canvas blocks replaced with extracted function calls
- Net reduction: 70 lines (3,179 → 3,109)

### Extractions
- `drawRealityDistortion` — magenta overlay near Void Anomaly
- `drawSpeedLines` — vertical lines during fast descent
- `drawGround` — brown ground rectangle
- `drawParticles` — sparkle/circle particle rendering
- `drawLandingEffects` — expanding cyan ring circles
- `drawPowerUps` — colored shapes per powerup type
- `drawFlyingRewards` — animated reward items
- `drawImpactFlash` — white stroked border screen flash

### Validation
- `./gradlew assembleDebug` — BUILD SUCCESSFUL (5s, zero errors)
- ADB install — Success
- Emulator launch — Success (PID running)

### Notes
- Refactor Sprint T2 is fully complete. T1 + T2 cumulative reduction: 4,344 → 3,109 lines (−28.4%)
- Threat entity rendering (~826 lines) remains inline indefinitely

---

## 2026-06-18

**Sprint / Phase:** Refactor Sprint T2 — Positioning Fixes + Merge

**Branch:** `refactor/ui-extraction`

**Base Commit:** `24a3eb9` (T2B)

**Status:** Completed

### Fixed
- `AltitudeDisplay` — added `modifier` parameter; caller passes `Modifier.align(Alignment.TopCenter)` for correct positioning
- `NotificationLayer` — added `modifier` parameter; positioned at `TopCenter.padding(top = 240.dp)`, text shrunk from `headlineSmall` → `bodyLarge`, `letterSpacing` reduced from `4.sp` → `2.sp`

### Changed
- `OPENCODE.md` — updated branch strategy, completed phases, and next tasks
- `docs/CHANGELOG.md` — added this entry

### Merged
- `refactor/ui-extraction` merged into `development` (`af3d0ae`)
- Git tags `refactor-t1-phase1`, `refactor-t1-phase2`, `refactor-t2` pushed to remote

---

## Historical Milestones

The following milestones summarize completed work prior to the changelog's creation. These entries are reconstructed from commit history and were not recorded as changelog events at the time of completion.

### EPIC 4: The Ascension Program

| Sprint | Commit | Summary |
|--------|--------|---------|
| EPIC 4 Start | `e744ae0` | Missions, achievements, and progression system initiated. |
| Sprint A | `6235350` | Mission reliability and early-game feel improvements. |
| Sprint B | `1404aaa` | Combo Renaissance — combo system overhaul and scoring refinements. |
| Sprint C | `86ce562` | Mission UX and communication — mission cards, notifications, player feedback. |
| Sprint D | `17956c0` | Platform evolution — dynamic platform generation and difficulty scaling. |
| Sprint E | `a00c623` | Pre-release candidate stabilization before Ascension Program launch. |
| Sprint F | `fa3101b` | Ascension Program complete — full mission tree, achievement tracking, progression gates, and Codex system operational. |

**Scope:** The Ascension Program introduced the mission system (6 mission types with difficulty tiers), achievement tracking (6 achievements with unlock conditions), player progression gates (rocket unlocks at score thresholds), the Codex (discovery categories with lore entries and per-category completion tracking), and the Zone system (altitude-based zones with distinct visual themes).

### EPIC 5: Survival Protocol

| Sprint | Commit | Summary |
|--------|--------|---------|
| Sprint B | `aee2c37` | Threats, survival economy, and destruction system stable. |

**Scope (in progress):** Survival Protocol introduces the threat system (hostile entities with spawn rules and tier progression), survival economy (shield regeneration delay, hull integrity critical thresholds, overheat management), destruction sequence (3-phase visual breakup with debris and fire effects), and visual redesigns (shield energy armor plates, catastrophic breakup particles, loss-of-control tumble).

*Note: EPIC 5 Sprint A is not reflected in the commit history as a discrete milestone and was subsumed into the Sprint B delivery.*
