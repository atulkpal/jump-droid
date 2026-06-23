# Refactor Sprint T1 — Phase 2 Plan: Replace Inline Composables with Extracted Calls

## Metadata

| Field | Value |
|-------|-------|
| **Sprint** | T1 (Tidying) |
| **Phase** | 2 of 2 |
| **Branch** | `refactor/ui-extraction` |
| **Base commit** | `fb93e9d` (Phase 1 complete) |
| **Goal** | Replace all remaining inline composable implementations in `GameScreen.kt` with calls to the 15 files extracted in Phase 1 |
| **Constraints** | Zero behavioral changes; visual parity; no gameplay/state/manager modifications |
| **Risk level** | Medium (many coordinated edits; brace-balance sensitive) |
| **Estimated net reduction** | ~1,145 lines (4,292 → ~3,147) |

---

## Scope

Phase 2 touches exactly **one file**: `app/src/main/java/com/example/jump_droid/GameScreen.kt`.

Every change is a **structural replacement** — inline code is removed and replaced by a call to an already-extracted, already-compiling composable function. No extracted files are modified. No new files are created. No game logic, state management, physics, rendering, or manager code is touched.

---

## 1. Screen Replacements

### Current State (lines 3000–3398)

The `when(gameState)` block at line 3000 contains 7 single-state branches. Each branch is a fully inlined composable UI that duplicates the corresponding extracted file. The 8th branch (`GameState.PLAYING, GAMEOVER, TUTORIAL, PAUSED, HELP, UNLOCK`) at line 3400 is handled separately (see sections 2–3).

### Replacement Plan

For each branch, delete the inline body and insert a single composable call.

| Branch | Inline Lines | Inline Size | Replace With |
|--------|-------------|-------------|--------------|
| `GameState.TITLE` | 3002–3038 | 37 lines | `TitleScreen(onNavigate = { gameState = it })` |
| `GameState.MAIN_MENU` | 3040–3095 | 55 lines | `MainMenuScreen(onLaunch = { restartGame() }, onNavigate = { gameState = it }, onExit = { (context as? Activity)?.finish() })` |
| `GameState.HANGAR` | 3097–3215 | 118 lines | `HangarScreen(player = player, highScore = highScore, progressionManager = progressionManager, sharedPrefs = sharedPrefs, onNavigate = { gameState = it })` |
| `GameState.ARCHIVE` | 3217–3305 | 88 lines | `ArchiveScreen(sharedPrefs = sharedPrefs, discoveryManager = discoveryManager, progressionManager = progressionManager, onNavigate = { gameState = it })` |
| `GameState.SETTINGS` | 3307–3336 | 29 lines | `SettingsScreen(sharedPrefs = sharedPrefs, onWipeData = { ... }, onReturn = { gameState = GameState.MAIN_MENU })` |
| `GameState.ABOUT` | 3338–3367 | 29 lines | `AboutScreen(onDismiss = { gameState = GameState.MAIN_MENU })` |
| `GameState.LEADERBOARD` | 3369–3398 | 29 lines | `LeaderboardScreen(onDismiss = { gameState = GameState.MAIN_MENU })` |

### Order of Edits

Replace branches **bottom-up** (LEADERBOARD → ABOUT → ... → TITLE) so that line numbers of earlier branches remain stable during editing. If using a single pass, replace from bottom to top.

### Parameter Mapping

Every parameter required by the extracted screens has a corresponding variable, manager, or computation already in scope inside `GameScreen()`:

- `gameState` — `remember { mutableStateOf(GameState.TITLE) }` (line 91)
- `player` — `remember { Player() }` (line 108)
- `highScore` — `remember { mutableIntStateOf(...) }` (line 79)
- `progressionManager` — `remember { ProgressionManager() }` (line 97)
- `discoveryManager` — `remember { DiscoveryManager() }` (line 96)
- `sharedPrefs` — `remember { context.getSharedPreferences(...) }` (line 77)
- `restartGame()` — local function (line 603)
- `context` — `LocalContext.current` (line 75)

No new variables or computations need to be introduced.

---

## 2. HUD Replacements

### Current State (lines 3427–3915)

Inside the multi-state branch at line 3400, when `gameState == GameState.PLAYING`, a large inline HUD section renders 8 visual elements. Six of these have extracted counterparts in `HudWidgets.kt`.

### Replacement Plan

Replace the inline HUD elements at their current locations with composable calls. The enclosing layout `{ Box(Modifier.fillMaxSize()) { ... } }` structure is preserved.

| HUD Element | Inline Lines | Extracted Function | Call |
|-------------|-------------|-------------------|------|
| Altitude display | 3429–3453 | `AltitudeDisplay(score, highScore)` | `AltitudeDisplay(score = score, highScore = highScore)` |
| Altitude display (duplicate copy) | 3455–3481 | **None — delete entirely** | This is an exact duplicate of lines 3429–3453; remove without replacement |
| Fuel gauge | 3483–3527 | `FuelGauge(fuel, maxFuel, gameTime)` | `FuelGauge(fuel = player.fuel, maxFuel = player.maxFuel, gameTime = gameTime)` |
| Heat gauge | 3529–3558 | `HeatGauge(heat, maxHeat, isOverheated, gameTime)` | `HeatGauge(heat = player.heat, maxHeat = player.maxHeat, isOverheated = player.isOverheated, gameTime = gameTime)` |
| Shield gauge | 3560–3595 | `ShieldGauge(shield, maxShield, isShieldCritical, gameTime)` | `ShieldGauge(shield = player.shield, maxShield = player.maxShield, isShieldCritical = player.shield < player.maxShield * 0.25f, gameTime = gameTime)` |
| Integrity gauge | 3597–3624 | `IntegrityGauge(integrity, maxIntegrity, isHullCritical, gameTime)` | `IntegrityGauge(integrity = player.integrity, maxIntegrity = player.maxIntegrity, isHullCritical = player.integrity < player.maxIntegrity * 0.25f, gameTime = gameTime)` |
| Mission row cards | 3626–3749 | **NO extracted file** | *Keep inline — not yet extracted* |
| Combo HUD bar | 3752–3812 | `ComboHudBar(currentCombo, bestComboThisRun, comboTarget, comboTimeRemaining, getWindowForCombo, screenWidth)` | `ComboHudBar(currentCombo = comboManager.currentCombo, bestComboThisRun = comboManager.bestComboThisRun, comboTarget = comboManager.comboTarget, comboTimeRemaining = comboManager.comboTimeRemaining, getWindowForCombo = comboManager.getWindowForCombo, screenWidth = screenWidth)` |
| Notification layer | 3815–3841 | `NotificationLayer(activeNotification, notificationAlpha, screenWidth)` | `NotificationLayer(activeNotification = activeNotification, notificationAlpha = notificationAlpha, screenWidth = screenWidth)` |
| Floating combo texts | 3845–3861 | **NO extracted file** | *Keep inline — not yet extracted* |
| Zone discovery card | 3863–3914 | `ZoneDiscoveryCard(activeEvent, score)` | `ZoneDiscoveryCard(activeEvent = discoveryManager.activeEvent, score = score)` |

### Not-Yet-Extracted HUD Content

Two HUD sections totaling ~141 lines remain inline after Phase 2:

- **Mission row cards** (lines 3626–3749, ~124 lines) — Animated mission progress cards with per-tab scrolling, transitions, and discovery integration. Higher visual complexity; deferred to future sprint.
- **Floating combo texts** (lines 3845–3861, ~17 lines) — Per-frame `drawText` calls that render score popups. Intimately coupled to the game loop's frame timing; deferred to future sprint.

---

## 3. Overlay Replacements

### Current State (lines 3917–4254)

Inside the multi-state branch at line 3400, five `if (gameState == ...)` blocks render overlays. Each has an extracted counterpart.

### Replacement Plan

| Overlay | Condition | Inline Lines | Replace With |
|---------|-----------|-------------|--------------|
| Pause | `gameState == GameState.PAUSED` | 3917–3991 | `PauseOverlay(showDevMenu, infiniteFuel, disableHeat, DevConfig.CHEATS_ENABLED, onToggleDevMenu = { showDevMenu = !showDevMenu }, onJumpToZone = { jumpToZone(it) }, onSpawnDevThreat = { spawnDevThreat(it) }, onToggleInfiniteFuel = { infiniteFuel = !infiniteFuel }, onToggleDisableHeat = { disableHeat = !disableHeat }, onUnlockAll = { unlockAll() }, onResume = { gameState = GameState.PLAYING }, onRestart = { restartGame() }, onMainMenu = { gameState = GameState.MAIN_MENU })` |
| Tutorial | `gameState == GameState.TUTORIAL && activeDiscovery != null` | 3993–4076 | `TutorialOverlay(activeDiscovery = activeDiscovery, onAcknowledge = { gameState = GameState.PLAYING; activeDiscovery = null })` |
| Help | `gameState == GameState.HELP` | 4077–4124 | `HelpOverlay(onDismiss = { gameState = GameState.PLAYING })` |
| Unlock | `gameState == GameState.UNLOCK && unlockedRocket != null` | 4125–4148 | `UnlockOverlay(unlockedRocket = unlockedRocket, onConfirm = { gameState = GameState.PLAYING; unlockedRocket = null })` |
| Game over | `gameState == GameState.GAMEOVER` | 4149–4254 | `GameOverOverlay(score = score, highScore = highScore, progressionManager = progressionManager, continuesUsed = continuesUsed, onContinue = { continueRun() }, onRestart = { restartGame() }, onMainMenu = { gameState = GameState.MAIN_MENU })` |

### Parameter Mapping

All parameters map to existing in-scope state:

| Overlay Param | In-Scope Source |
|--------------|-----------------|
| `showDevMenu` | `var showDevMenu` (line 137) |
| `infiniteFuel` | `var infiniteFuel` (line 135) |
| `disableHeat` | `var disableHeat` (line 136) |
| `cheatsEnabled` | `DevConfig.CHEATS_ENABLED` |
| `activeDiscovery` | `var activeDiscovery` (line 92) |
| `unlockedRocket` | `var unlockedRocket` (line 93) |
| `score` / `highScore` / `continuesUsed` | State vars (lines 79–82) |
| `progressionManager` | Manager instance (line 97) |
| `jumpToZone()` / `spawnDevThreat()` / `unlockAll()` / `restartGame()` / `continueRun()` | Local functions (lines 509, 528, 574, 603, 426) |

---

## 4. Remaining Inline Utilities

Two inline utility definitions remain after all screen/HUD/overlay replacements:

| Function | Lines | Status |
|----------|-------|--------|
| `MissionType.toIcon(): String` | 4260–4267 (8 lines) | Extension function; could be moved to `MissionType.kt`. Low priority — pure data mapping. |
| `PowerupBadge(label, color, seconds)` | 4269–4291 (23 lines) | Appears unused in `GameScreen.kt` (no call sites found). If confirmed unused, delete. Otherwise extract to its own file. |

**Recommendation:** Handle as a cleanup pass after main replacements are verified.

---

## 5. Estimated GameScreen Reduction

### Lines Removed by Section

| Section | Gross Inline Lines | Replacement Lines Added | Net Removed |
|---------|-------------------|----------------------|-------------|
| 7 screen branches (lines 3000–3398) | 385 | 7 | 378 |
| HUD gauges (6 of 8 replaced) | 156 | 6 | 150 |
| Duplicate altitude display (deleted) | 27 | 0 | 27 |
| 5 overlays (lines 3917–4254) | 337 | 5 | 332 |
| Mission row cards | 124 | *not replaced* | 0 |
| Floating combo texts | 17 | *not replaced* | 0 |
| Utility cleanup (PowerupBadge + toIcon) | 31 | 1 | 30 |
| **Totals** | **1,077** | **19** | **~917 (gross)** |

Wait — the HUD elements are nested inside the large PLAYING branch (lines 3400–4254). The `mission row cards` (lines 3626–3749) and `floating combo texts` (lines 3845–3861) are **inside** the HUD range, so the gross removal isn't additive in the way the table above suggests. Let me recalculate more carefully:

### Corrected Estimate

| Group | Line Range | Total Lines | Replaced | Lines After |
|-------|-----------|-------------|----------|-------------|
| 7 screen branches | 3000–3398 | 398 | All | ~7 |
| Multi-state branch header | 3400 (1 line) | 1 | No | 1 |
| PLAYING HUD + overlays (includes mission cards and floating texts) | 3401–4254 | 854 | 6 HUD widgets + 5 overlays + duplicate altitude ~517 removed, mission cards + floating texts ~141 kept | ~496 (854 – 517 + ~5 replacement calls + mission cards + floating texts) |
| Screen branch closing braces | varies | ~7 | No | ~7 |
| Top-level utilities | 4260–4291 | 32 | 1 (PowerupBadge) | ~9 (toIcon only) |
| **Totals** | | **~4,292** | **~1,145** | **~3,147** |

### Final Estimate

| Metric | Value |
|--------|-------|
| Current GameScreen.kt | 4,292 lines |
| Gross lines removed | ~1,174 (7 screens + 6 HUD widgets + duplicate altitude + 5 overlays + cleanup) |
| Replacement calls added | ~29 |
| **Net removed** | **~1,145** |
| **Estimated after Phase 2** | **~3,147 lines (27% reduction)** |
| Remaining shared content | ~265 lines (mission cards + floating texts + toIcon + utilities) |

---

## 6. Validation Strategy

### Build Verification (Primary Gate)

```
./gradlew assembleDebug
```

Must pass with **zero errors**. Two pre-existing deprecation warnings (`LinearProgressIndicator`) are acceptable.

### Brace Balance Check

After each group of edits (screens, then HUD, then overlays), verify the `{` / `}` count:

```powershell
# Count opening braces
(Get-Content GameScreen.kt | Select-String -Pattern "{" | Measure-Object -Line).Lines

# Count closing braces
(Get-Content GameScreen.kt | Select-String -Pattern "}" | Measure-Object -Line).Lines
```

The counts must match. A Phase 1 pre-analysis revealed that the previous extraction attempt left the file at depth **−2** (2 extra `}`). Phase 2 must avoid this by counting before and after each edit batch.

### Visual Parity Verification

1. Build and install the debug APK
2. Manually navigate through all game states:
   - TITLE → MAIN_MENU → each screen (HANGAR, ARCHIVE, SETTINGS, ABOUT, LEADERBOARD)
   - MAIN_MENU → PLAYING (verify HUD gauges, combo bar, notifications, zone card)
   - During PLAYING: trigger PAUSED, TUTORIAL, UNLOCK, HELP, GAMEOVER overlays
3. Check that visual appearance, layout, colors, animations, and text match the original inline rendering

### Automated Test Gap

This project has no automated UI tests. Manual verification is the only option. If UI tests are added in a future sprint, this refactor makes them dramatically easier to write (composables can be tested in isolation).

---

## 7. Rollback Strategy

### Per-Batch Rollback

Edits are organized into 3 independent batches:

| Batch | Scope | Lines Affected | Rollback |
|-------|-------|---------------|----------|
| **Batch A** | 7 screen branches (bottom-up: LEADERBOARD → TITLE) | 3000–3398 | `git checkout -- GameScreen.kt` and redo if any batch fails |
| **Batch B** | HUD widgets + duplicate altitude (inside PLAYING) | 3429–3624, 3752–3914 | Roll back Batch A + B together (both inside multi-state branch) |
| **Batch C** | 5 overlays (inside PLAYING) | 3917–4254 | Roll back Batches B + C together (all inside multi-state branch) |

If a later batch fails, roll back all batches applied so far: `git checkout fb93e9d -- GameScreen.kt`

### Commit Strategy (Recommended)

One commit per batch, on a separate working branch:

```bash
git checkout -b refactor/ui-extraction-phase2-screens
# apply Batch A
git commit -m "Phase 2: Replace 7 screen branches with extracted composable calls"

git checkout refactor/ui-extraction
git merge refactor/ui-extraction-phase2-screens

git checkout -b refactor/ui-extraction-phase2-hud
# apply Batch B
git commit -m "Phase 2: Replace HUD widgets with extracted composable calls"

git checkout refactor/ui-extraction
git merge refactor/ui-extraction-phase2-hud

# repeat for Batch C (overlays)
git checkout -b refactor/ui-extraction-phase2-overlays
# apply Batch C
git commit -m "Phase 2: Replace 5 overlays with extracted composable calls"
git checkout refactor/ui-extraction
git merge refactor/ui-extraction-phase2-overlays
```

Each batch is independently buildable and testable. If a batch breaks the build, it can be rolled back without affecting completed batches.

### Full Reset

```bash
git checkout fb93e9d -- app/src/main/java/com/example/jump_droid/GameScreen.kt
```

Restores GameScreen.kt to the Phase 1 state. All extracted files remain untouched.

---

## 8. Dependencies & Risks

### Dependencies

- **Phase 1 completion** ✅ — all 15 extracted files exist, compile, and import correctly
- **`DevConfig.CHEATS_ENABLED`** — referenced by `PauseOverlay.kt` but not inside GameScreen.kt's scope; confirmed accessible as it's a top-level constant in the same package

### Risks

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Brace imbalance from copy-paste errors | Medium | High — build failure | Count braces before/after each edit batch; prefer `edit` tool over manual replacement |
| Missing import in GameScreen.kt for extracted function name | Low | Medium — build failure | Run build after each batch; GameScreen.kt and extracted files share the same package, so no imports needed for same-package references |
| Accidental deletion of non-UI code (managers, game loop) | Low | High — subtle behavioral bug | Only touch content within identified inline ranges; never modify game-loop, manager init, or state declaration sections (lines 82–2999) |
| Parameter mismatch between extracted signature and inline call | Low | Medium — build failure | Verified all extracted signatures against in-scope variables in section 1–3 parameter tables above |

---

## 9. Edit Procedure (Detailed)

### Batch A: Screen Branches

For each of the 7 branches, in **reverse order** (bottom-up to preserve line numbers):

1. Read the exact inline content of the branch (start `->` to the `}\n\n` before the next branch)
2. Use `edit` tool with `oldString` matching the exact branch content, replacing with a single composable call followed by `return@BoxWithConstraints` or the appropriate enclosing-block return
3. Run `./gradlew assembleDebug` to verify

**Special case: `SettingsScreen` `onWipeData`** — the inline wipe logic resets `highScore` and `player.rocketType` which are state variables in `GameScreen` scope. The callback closure captures these:

```kotlin
SettingsScreen(
    sharedPrefs = sharedPrefs,
    onWipeData = {
        sharedPrefs.edit { clear() }
        highScore = 0
        player.rocketType = RocketType.BALANCED
        gameState = GameState.TITLE
    },
    onReturn = { gameState = GameState.MAIN_MENU }
)
```

### Batch B: HUD Widgets

1. Locate the `Row(Modifier.fillMaxSize().padding(end = 8.dp), ...)` that contains the altitude/fuel/heat on the left and shield/integrity on the right (~line 3427)
2. Replace each inline gauge block with the corresponding composable call, preserving layout ordering
3. Delete the duplicate altitude block entirely (lines 3455–3481)
4. Leave mission row cards and floating texts untouched
5. Replace combo bar, notification layer, zone card with calls

### Batch C: Overlays

1. Locate each `if (gameState == GameState.XXX)` block (lines 3917–4254)
2. Replace the block body with the corresponding composable call
3. No structural changes to the `if` conditions

---

## 10. Success Criteria

- [ ] `./gradlew assembleDebug` passes with zero errors
- [ ] `{` / `}` brace count is balanced (original delta = 0)
- [ ] `GameScreen.kt` reduced from 4,292 lines to approximately 3,147 lines
- [ ] All 7 navigation screens render identically to pre-refactor
- [ ] All 6 HUD widgets render identically to pre-refactor
- [ ] All 5 overlays render identically to pre-refactor
- [ ] Game loop, physics, threats, progression, missions, combos, damage, save/load unaffected
- [ ] Commit history is clean with 3 incremental commits (screens, HUD, overlays)

---

## Appendix A: Inline Code Inventory

```
GameScreen.kt structure (main sections only):
  Lines    80–107    State declarations (gameState, score, player, managers)
  Lines   108–2,999  Game loop, physics, managers (NOT TOUCHED)
  Lines 3,000–3,398  7 screen branches (TO REPLACE — 398 lines → 7)
  Lines 3,400        8th branch header (PLAYING+GAMEOVER+TUTORIAL+PAUSED+HELP+UNLOCK)
  Lines 3,401–3,426  Top-right utility buttons (keep inline)
  Lines 3,427–3,914  HUD section (TO REPLACE IN PART — 6 widgets → calls)
  Lines 3,915–3,916  Blank/separator
  Lines 3,917–4,254  5 overlay blocks (TO REPLACE — 337 lines → 5 calls)
  Lines 4,255–4,259  Closing braces of PLAYING branch
  Lines 4,260–4,291  Top-level utility functions (TO CLEANUP)
```

## Appendix B: Extracted Composable Parameter Cross-Reference

See Phase 1 report ([Refactor_T1_Phase1.md](Refactor_T1_Phase1.md)) for full parameter tables of all 15 extracted files.
