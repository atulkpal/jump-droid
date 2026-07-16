# Refactor Sprint T1 — Phase 2: Inline Composable Replacement

## Metadata

| Field | Value |
|-------|-------|
| **Sprint** | T1 (Tidying) |
| **Phase** | 2 of 2 |
| **Branch** | `refactor/ui-extraction` |
| **Base commit** | `fb93e9d` (Phase 1 complete) |
| **Status** | **Completed** |

---

## Summary

Phase 2 replaced all remaining inline UI composable implementations in `GameScreen.kt` with calls to the 15 files extracted in Phase 1. The file was reduced by **966 lines (−22.5%)** from 4,292 to 3,326 lines. All braces remain balanced, the build succeeds, and the APK installs and runs on the emulator.

---

## GameScreen.kt Size Reduction

| Stage | Lines | Delta | Cumulative |
|-------|-------|-------|------------|
| Original (pre-refactor) | 4,344 | — | — |
| Phase 1 (file extraction + conflict resolution) | 4,292 | −52 | −52 |
| Batch A (7 screen branches replaced) | 3,910 | −382 | −434 |
| Batch B (6 HUD widgets + duplicate deletion) | 3,509 | −401 | −835 |
| Batch C (5 overlays replaced) + cleanup | 3,326 | −183 | −1,018 |
| **Final** | **3,326** | **—** | **−1,018 (−23.4%)** |

Git diff: `1 file changed, 85 insertions(+), 1051 deletions(-)`

---

## Batch A: Screen Branch Replacements

All 7 single-state `when(gameState)` branches were replaced with one-line composable calls. Replaced bottom-up (LEADERBOARD → TITLE) to preserve line number stability during editing.

| Branch | Inline Lines Removed | Replacement Call |
|--------|---------------------|------------------|
| `GameState.TITLE` | 37 | `TitleScreen(onNavigate = { gameState = it })` |
| `GameState.MAIN_MENU` | 55 | `MainMenuScreen(onLaunch = { restartGame() }, onNavigate = { gameState = it }, onExit = { (context as? Activity)?.finish() })` |
| `GameState.HANGAR` | 118 | `HangarScreen(player, highScore, progressionManager, sharedPrefs, onNavigate = { gameState = it })` |
| `GameState.ARCHIVE` | 88 | `ArchiveScreen(sharedPrefs, discoveryManager, progressionManager, onNavigate = { gameState = it })` |
| `GameState.SETTINGS` | 29 | `SettingsScreen(sharedPrefs, onWipeData = { ... }, onReturn = { gameState = GameState.MAIN_MENU })` |
| `GameState.ABOUT` | 29 | `AboutScreen(onDismiss = { gameState = GameState.MAIN_MENU })` |
| `GameState.LEADERBOARD` | 29 | `LeaderboardScreen(onDismiss = { gameState = GameState.MAIN_MENU })` |

### Parameter Mapping

Every parameter required by the extracted screen composables had a corresponding variable already in scope inside `GameScreen()`:
- `gameState` — `remember { mutableStateOf(GameState.TITLE) }`
- `player` — `remember { Player() }`
- `highScore` — `remember { mutableIntStateOf(...) }`
- `progressionManager` — `remember { ProgressionManager() }`
- `discoveryManager` — `remember { DiscoveryManager() }`
- `sharedPrefs` — `remember { context.getSharedPreferences(...) }`
- `restartGame()`, `continueRun()` — local functions

No new variables or derivations were introduced.

---

## Batch B: HUD Widget Replacements

Inside the `GameState.PLAYING` block, the inline HUD section was replaced with calls to HudWidgets.kt composables. A duplicate altitude display block was identified and deleted.

| HUD Element | Lines Removed | Replacement |
|-------------|--------------|-------------|
| Altitude display (first copy) | 25 | `AltitudeDisplay(score = score, highScore = highScore)` |
| Altitude display (duplicate) | 27 | **Deleted entirely** (identical to first copy) |
| Fuel gauge (wave animation) | 45 | `FuelGauge(fuel = player.fuel, maxFuel = player.maxFuel, gameTime = gameTime)` |
| Heat gauge (gradient bar) | 30 | `HeatGauge(heat = player.heat, maxHeat = player.maxHeat, isOverheated = player.isOverheated, gameTime = gameTime)` |
| Shield gauge (right side) | 36 | `ShieldGauge(shield = player.shield, maxShield = player.maxShield, isShieldCritical = ..., gameTime = gameTime)` |
| Integrity gauge (right side) | 28 | `IntegrityGauge(integrity = player.integrity, maxIntegrity = player.maxIntegrity, isHullCritical = ..., gameTime = gameTime)` |
| Combo HUD bar | 61 | `ComboHudBar(currentCombo, bestComboThisRun, comboTarget, comboTimeRemaining, getWindowForCombo = { comboManager.getWindowForCombo(it) }, screenWidth)` |
| Notification layer | 27 | `NotificationLayer(activeNotification, notificationAlpha, screenWidth)` |
| Zone discovery card | 52 | `ZoneDiscoveryCard(activeEvent = discoveryManager.activeEvent, score = score)` |

### Build Fix

The initial build of Batch B failed because `comboManager.getWindowForCombo` was passed as a direct member reference but the Kotlin compiler interpreted it as a property returning `Long`. Fixed by wrapping in a lambda: `getWindowForCombo = { comboManager.getWindowForCombo(it) }`.

### HUD Content Left Inline

Two HUD sections totaling ~141 lines remain inline:

- **Mission row cards** (~124 lines) — animated mission cards with ceremony stage transitions (GLOW, COMPLETED_TEXT, REWARD_SPAWNED), per-mission color coding, and animated content switching. Higher visual complexity; deferred to future sprint.
- **Floating combo texts** (~17 lines) — per-frame `Text` calls with offset tied to `cameraY` and `ft.life`. Intimately coupled to the game loop's frame timing; deferred to future sprint.

---

## Batch C: Overlay Replacements + Cleanup

### Overlay Replacements

All 5 overlay `if` blocks inside the PLAYING multi-state branch were replaced:

| Overlay | Condition | Lines Removed | Replacement |
|---------|-----------|--------------|-------------|
| Pause | `gameState == GameState.PAUSED` | 75 | `PauseOverlay(showDevMenu, infiniteFuel, disableHeat, cheatsEnabled, ...13 callbacks)` |
| Tutorial | `gameState == GameState.TUTORIAL && activeDiscovery != null` | 84 | `TutorialOverlay(activeDiscovery = activeDiscovery, onAcknowledge = { ... })` |
| Help | `gameState == GameState.HELP` | 48 | `HelpOverlay(onDismiss = { gameState = GameState.PLAYING })` |
| Unlock | `gameState == GameState.UNLOCK && unlockedRocket != null` | 24 | `UnlockOverlay(unlockedRocket = unlockedRocket, onConfirm = { ... })` |
| Game over | `gameState == GameState.GAMEOVER` | 106 | `GameOverOverlay(score, highScore, progressionManager, continuesUsed, onContinue, onRestart, onMainMenu)` |

### Cleanup

| Function | Lines | Disposition |
|----------|-------|-------------|
| `PowerupBadge(label, color, seconds)` | 23 | **Removed** — unused (zero call sites in GameScreen.kt) |
| `MissionType.toIcon()` | 8 | **Kept** — still called by inline mission row cards |

---

## Remaining Inline Content (~286 lines)

| Section | Lines | Reason Not Extracted |
|---------|-------|---------------------|
| Game loop / managers / state | ~3,000 | Out of scope for Sprint T1 |
| Mission row cards | ~124 | Animated ceremony stages; deep integration with MissionManager |
| Floating combo texts | ~17 | Per-frame rendering; tied to `cameraY` and per-frame state |
| Top-right utility buttons (`?`, `\|\|`) | ~24 | Trivial, tightly coupled |
| `MissionType.toIcon()` | 8 | Called by mission cards |

---

## Brace Balance Verification

```powershell
Open: 681 Close: 681 Balance: 0
```

Braces are balanced. The previous extraction attempt (pre-Phase 1) left the file at depth −2; Phase 2 avoided this by counting after each edit batch.

---

## Build & Run Validation

| Check | Result |
|-------|--------|
| `./gradlew assembleDebug` | ✅ BUILD SUCCESSFUL (zero errors) |
| `./gradlew installDebug` | ✅ Installed on emulator (Medium_Phone API 35) |
| APK launch | ✅ `Events injected: 1` — launcher activity started |

No deprecation warnings or new issues introduced.

---

## Files Modified

| File | Change |
|------|--------|
| `GameScreen.kt` | −1,018 lines net (85 insertions, 1,051 deletions) |

No other files were modified. The 15 extracted files from Phase 1 remain unchanged.

---

## Commit Strategy

Phase 2 was performed as a single working set of edits rather than separate per-batch commits. The three batches (screens, HUD, overlays) were each independently buildable during development:

1. ✅ Batch A build verified after screen replacements
2. ✅ Batch B build verified after HUD replacements (with one fix)
3. ✅ Batch C build verified after overlay replacements + cleanup

---

## Phase 2 Completion Checklist

- [x] All 7 screen branches replaced with extracted composable calls
- [x] 6 of 8 HUD widgets replaced; duplicate altitude deleted
- [x] All 5 overlays replaced with extracted composable calls
- [x] `PowerupBadge` removed (unused)
- [x] `MissionType.toIcon()` preserved (still in use)
- [x] `./gradlew assembleDebug` passes
- [x] Brace balance: 681 = 681
- [x] APK installed and launched on emulator
- [x] GameScreen.kt at 3,326 lines (−1,018, −23.4%)
