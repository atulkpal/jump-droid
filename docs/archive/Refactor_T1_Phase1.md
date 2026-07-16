# Refactor Sprint T1 — Phase 1: UI Composable Extraction

## Metadata

| Field | Value |
|-------|-------|
| **Sprint** | T1 (Tidying) |
| **Phase** | 1 of 2 |
| **Branch** | `refactor/ui-extraction` |
| **Base commit** | `aee2c37` (EPIC 5 Sprint B — Threats, Survival Economy and Destruction System stable) |
| **Goal** | Extract low-risk UI composables from the monolithic `GameScreen.kt` into separate files, preserving all visuals and behavior exactly |
| **Status** | **Phase 1 complete** — 15 files created, build passing, GameScreen.kt reduced by 52 lines |
| **Estimated Phase 2 reduction** | ~1,650 lines (from 4,292 → ~2,600, a 39% reduction) |

---

## Motivation

`GameScreen.kt` weighed **4,344 lines / 275 KB** — a single file containing all game screens, overlays, HUD widgets, and the core game loop. This made it difficult to: navigate, reason about dependencies, test in isolation, and onboard new contributors. The file mixed unrelated concerns (title screen, pause overlay, fuel gauge, threat spawning) in a single scope.

The extraction follows a **Composable-per-file** pattern where each extracted function receives only the state it needs as explicit parameters — no God-object, no hidden global state.

---

## Files Created (15 new `.kt` files)

All files reside in `app/src/main/java/com/example/jump_droid/` and share the package `com.example.jump_droid`.

### Screens

| File | Lines | Entry Point | Signature |
|------|-------|-------------|-----------|
| `TitleScreen.kt` | 86 | `@Composable fun TitleScreen(onNavigate: (GameState) -> Unit)` | Animated rocket canvas, glow-pulsing title, "INITIATE ASCENT" button |
| `MainMenuScreen.kt` | 96 | `@Composable fun MainMenuScreen(onLaunch: () -> Unit, onContinue: () -> Boolean, onNavigate: (GameState) -> Unit, highScore: Int, currentRocket: RocketType, rocketInventory: Set<RocketType>, sharedPrefs: SharedPreferences)` | Menu buttons (Continue, New Flight, Hangar, Archive, Leaderboard, Settings, About), equipped-rocket badge, surface controls |
| `AboutScreen.kt` | 61 | `@Composable fun AboutScreen(onDismiss: () -> Unit)` | Credits / information screen with scrollable text |
| `LeaderboardScreen.kt` | 64 | `@Composable fun LeaderboardScreen(onDismiss: () -> Unit)` | High-score leaderboard with rank cards |
| `SettingsScreen.kt` | 75 | `@Composable fun SettingsScreen(sharedPrefs: SharedPreferences, onWipeData: () -> Unit, onDismiss: () -> Unit)` | Volume sliders, haptic toggle, controls cheatsheet, achievements progress |
| `HangarScreen.kt` | 166 | `@Composable fun HangarScreen(player: Player, highScore: Int, progressionManager: ProgressionManager, sharedPrefs: SharedPreferences, onNavigate: (GameState) -> Unit, onBack: () -> Unit)` | Rocket selection grid, stats panel, equip/upgrade/buy actions |
| `ArchiveScreen.kt` | 138 | `@Composable fun ArchiveScreen(sharedPrefs: SharedPreferences, discoveryManager: DiscoveryManager, progressionManager: ProgressionManager, onNavigate: (GameState) -> Unit, onBack: () -> Unit)` | Tabbed Codex browser per discovery category + achievements list |

### Overlays

| File | Lines | Entry Point | Signature |
|------|-------|-------------|-----------|
| `PauseOverlay.kt` | 128 | `@Composable fun PauseOverlay(showDevMenu: Boolean, infiniteFuel: Boolean, disableHeat: Boolean, cheatsEnabled: Boolean, onToggleDevMenu: () -> Unit, onToggleFuel: () -> Unit, onToggleHeat: () -> Unit, onResume: () -> Unit, onRestart: () -> Unit, onQuit: () -> Unit)` | Pause menu with resume/restart/quit + dev menu toggles |
| `GameOverOverlay.kt` | 149 | `@Composable fun GameOverOverlay(score: Int, highScore: Int, progressionManager: ProgressionManager, continuesUsed: Int, onContinue: () -> Unit, onRestart: () -> Unit, onQuit: () -> Unit)` | End-of-run stats (score, altitude, cause, rewards, continue option) |
| `TutorialOverlay.kt` | 136 | `@Composable fun TutorialOverlay(activeDiscovery: DiscoveryType?, onAcknowledge: () -> Unit)` | Tutorial/guidance cards for new discoveries |
| `UnlockOverlay.kt` | 63 | `@Composable fun UnlockOverlay(unlockedRocket: RocketType?, onConfirm: () -> Unit)` | Rocket unlock announcement with glow animation |
| `HelpOverlay.kt` | 90 | `@Composable fun HelpOverlay(onDismiss: () -> Unit)` | Interactive help with discoverable lore entries |

### Shared Components

| File | Lines | Entry Point | Notes |
|------|-------|-------------|-------|
| `CodexCard.kt` | 68 | `@Composable fun CodexCard(title: String, description: String, lore: String, unlocked: Boolean)` | Reusable card rendering for Codex entries; imported by both GameScreen.kt and ArchiveScreen.kt |
| `Achievements.kt` | 10 | `val AchievementsList = listOf(...)` | Moved from inline `val` in GameScreen.kt; 6 achievement definitions |
| `HudWidgets.kt` | 372 | 8 composable functions (see table below) | All HUD gauge and status overlay functions |

### HudWidgets.kt — 8 Composable Functions

| Function | Parameters | Purpose |
|----------|------------|---------|
| `AltitudeDisplay(score, highScore)` | `Int, Int` | Altitude counter with best marker |
| `FuelGauge(fuel, maxFuel, gameTime)` | `Float, Float, Long` | Animated wave fuel gauge with low-fuel blink |
| `HeatGauge(heat, maxHeat, isOverheated, gameTime)` | `Float, Float, Boolean, Long` | Heat gradient gauge with overheat pulse |
| `ShieldGauge(shield, maxShield, isShieldCritical, gameTime)` | `Float, Float, Boolean, Long` | Shield bar with critical blink |
| `IntegrityGauge(integrity, maxIntegrity, isHullCritical, gameTime)` | `Float, Float, Boolean, Long` | Hull integrity bar with critical blink |
| `ComboHudBar(currentCombo, bestComboThisRun, comboTarget, comboTimeRemaining, getWindowForCombo, screenWidth)` | `Int, Int, Int, Long, (Int) -> Long, Float` | Combo tracker with segmented progress bar |
| `NotificationLayer(notifications, gameTime)` | `List<Notification>, Long` | Floating notification messages |
| `ZoneDiscoveryCard(activeDiscovery, transitionProgress, gameTime, screenWidthDp, screenHeightDp, onDismiss)` | `DiscoveryType?, Float, Long, ...` | Zone discovery announcement card |

---

## Files Modified

| File | Change | Lines |
|------|--------|-------|
| `GameScreen.kt` | Removed inline `AchievementsList` val (was ~line 73) | −7 lines |
| `GameScreen.kt` | Removed inline `CodexCard` composable (was ~line 4303) | −45 lines |
| **Total removed** | | **−52 lines** |

The deleted declarations are now provided by `Achievements.kt` and `CodexCard.kt` respectively.

---

## Import Errors Found & Fixed During Phase 1

The extracted files were generated with incorrect or missing imports in several places. All issues were resolved before the build succeeded.

| File | Symbol | Problem | Fix Applied |
|------|--------|---------|-------------|
| `CodexCard.kt` | `sp` | Missing import `androidx.compose.ui.unit.sp` | Added import |
| `HudWidgets.kt` | `graphicsLayer` | Wrong package: `foundation.layout.graphicsLayer` does not exist | Changed to `import androidx.compose.ui.graphics.graphicsLayer` |
| `HudWidgets.kt` | `statusBarsPadding` | Missing import | Added `import androidx.compose.foundation.layout.statusBarsPadding` |
| `HudWidgets.kt` | `fillMaxHeight` | Missing import | Added `import androidx.compose.foundation.layout.fillMaxHeight` |
| `HudWidgets.kt` | `shadow` | Missing import | Added `import androidx.compose.ui.draw.shadow` |
| `TitleScreen.kt` | `offset` | Missing import | Added `import androidx.compose.foundation.layout.offset` |
| `TitleScreen.kt` | `safeDrawingPadding` | Missing import | Added `import androidx.compose.foundation.layout.safeDrawingPadding` |
| `TitleScreen.kt` | `roundToPx` | Import does not exist as standalone in this Compose version | Removed invalid import; function resolves via `Dp` member |
| `TitleScreen.kt` | `dp` | Duplicate import | Removed duplicate |
| `HangarScreen.kt` | `border` | Missing import (`BorderStroke` was present but not `border` modifier) | Added `import androidx.compose.foundation.border` |
| `HelpOverlay.kt` | `border` | Missing import | Added `import androidx.compose.foundation.border` |
| `UnlockOverlay.kt` | `border` | Missing import | Added `import androidx.compose.foundation.border` |
| `TutorialOverlay.kt` | `CircleShape` | Missing import | Added `import androidx.compose.foundation.shape.CircleShape` |
| `TutorialOverlay.kt` | `shadow` | Missing import | Added `import androidx.compose.ui.draw.shadow` |
| `TutorialOverlay.kt` | `border` | Missing import | Added `import androidx.compose.foundation.border` |

---

## Conflicts Resolved

| Symbol | File 1 (removed) | File 2 (kept) | Resolution |
|--------|------------------|---------------|------------|
| `AchievementsList` | `GameScreen.kt:73` (inline `val`) | `Achievements.kt:3` (extracted `val`) | Deleted inline definition; same-package reference resolves automatically |
| `CodexCard` | `GameScreen.kt:4303` (inline `@Composable fun`) | `CodexCard.kt:27` (extracted `@Composable fun`) | Deleted inline definition; same-package reference resolves automatically |

---

## Architecture Decisions

### Parameter Design
Each extracted composable receives **only the state it needs** as explicit parameters. No God-object or monolithic state holder is passed around. The exception is `HangarScreen`, which receives the mutable `Player` object directly because it needs to **write** rocket selection (rather than adding a separate callback layer).

### Context Access
Android `Context` is obtained via `LocalContext.current` inside composables rather than threaded through as a parameter.

### Package Co-location
All composables remain in the same package (`com.example.jump_droid`) as `GameScreen.kt`. No new package structure is introduced — this is a flat extraction, not a re-architecture. Package restructuring is reserved for a future sprint.

### Grouping Strategy
- **Screens** (full-page composables that replace `when(gameState)` branches): one file each
- **Overlays** (semi-transparent layers on top of the game): one file each
- **HUD widgets** (gauges, combo bar, notifications, zone cards): grouped into `HudWidgets.kt` because they share identical Canvas drawing patterns and import sets
- **Reusable components** (`CodexCard`): separated to avoid circular imports between GameScreen.kt and ArchiveScreen.kt

### What Was NOT Extracted
- Core game-loop logic (`LaunchedEffect` with `withFrameNanos`)
- Game state management (`var gameState by remember { mutableStateOf(...) }`)
- Physics, threats, platform rendering, rocket rendering
- Missions, combos, progression, damage systems
- Save/load logic
- Audio management

All of these remain in `GameScreen.kt` and are out of scope for Sprint T1.

---

## Pre-existing Issues Discovered

During import resolution, two pre-existing issues in the original `GameScreen.kt` were identified but not fixed:

1. **`roundToPx` usage without import** — `GameScreen.kt:3016` calls `rocketOffset.dp.roundToPx()` without any import for `roundToPx`. This resolves through Compose's implicit `Dp` member resolution, but the mechanism is fragile and version-dependent. The same pattern in `TitleScreen.kt` was fixed by removing the invalid standalone import (the function resolves without explicit import in this Compose version).

2. **Deprecated `LinearProgressIndicator`** — Both `GameScreen.kt:3284` and `ArchiveScreen.kt:116` use the overload `LinearProgressIndicator(progress: Float, ...)` which is deprecated. The replacement is an overload that takes `progress` as a lambda. This was not changed in Phase 1.

3. **Braces in game-loop section** — A previously identified issue (2 extra `}` at the end of the game-loop `LaunchedEffect`/`withFrameNanos` section) existed in an earlier extraction attempt but was reverted. The current unmodified `GameScreen.kt` does not have this brace imbalance.

---

## Build Verification

```
> Task :app:compileDebugKotlin PASSED
> Task :app:dexBuilderDebug PASSED
BUILD SUCCESSFUL in 20s
30 actionable tasks: 1 executed, 29 up-to-date
```

All 15 new files compile clean. Two deprecation warnings exist (unrelated `LinearProgressIndicator` usage) but no errors.

---

## GameScreen.kt Size Reduction

| Metric | Before | After | Delta |
|--------|--------|-------|-------|
| Total lines | 4,344 | 4,292 | **−52 (−1.2%)** |
| File size | 275 KB | 266 KB | **−9 KB (−3.3%)** |
| Inline composables remaining | ~1,650 lines | ~1,650 lines | Not yet replaced |

Phase 2 will replace the remaining inline implementations, estimated to remove ~1,650 more lines and leave GameScreen.kt at approximately **2,600 lines (a 39% reduction)**.

---

## Phase 2 Roadmap (Not Yet Started)

The following tasks are scoped for Phase 2 but have **not** been performed:

1. **Replace 7 state-case blocks** — In `GameScreen()`, replace the body of each `when(gameState)` branch with a call to the corresponding extracted composable (e.g., `GameState.TITLE -> TitleScreen(onNavigate = { gameState = it })`)

2. **Replace PLAYING-block HUD** — Inside the `GameState.PLAYING` branch, replace the inline gauge code block with a `Row { AltitudeDisplay(...); FuelGauge(...); HeatGauge(...); ... }`

3. **Replace 5 overlays** — Replace the inline `if (showPause) { ... }`, `if (gameOver) { ... }`, etc. blocks with calls to `PauseOverlay(...)`, `GameOverOverlay(...)`, etc.

4. **Remove inline utilities** — Remove inline `PowerupBadge` and `MissionType.toIcon()` definitions (remaining in GameScreen.kt) if they are not called elsewhere after extraction

5. **Brace balance verification** — After edits, verify `{` / `}` count to avoid the imbalance seen in the earlier failed extraction attempt

---

## File Manifest

```
app/src/main/java/com/example/jump_droid/
  TitleScreen.kt        (new,    86 lines)
  MainMenuScreen.kt     (new,    96 lines)
  AboutScreen.kt        (new,    61 lines)
  LeaderboardScreen.kt  (new,    64 lines)
  SettingsScreen.kt     (new,    75 lines)
  HelpOverlay.kt        (new,    90 lines)
  CodexCard.kt          (new,    68 lines)
  Achievements.kt       (new,    10 lines)
  HangarScreen.kt       (new,   166 lines)
  ArchiveScreen.kt      (new,   138 lines)
  PauseOverlay.kt       (new,   128 lines)
  TutorialOverlay.kt    (new,   136 lines)
  UnlockOverlay.kt      (new,    63 lines)
  GameOverOverlay.kt    (new,   149 lines)
  HudWidgets.kt         (new,   372 lines)
  GameScreen.kt         (modified, -52 lines)
```
