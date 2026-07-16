# Refactor Sprint T2 — Plan: Extract Remaining Inline UI from GameScreen.kt

## Metadata

| Field | Value |
|-------|-------|
| **Sprint** | T2 (Tidying) |
| **Phases** | 2 (T2A Low Risk, T2B Medium Risk) |
| **Branch** | `refactor/ui-extraction` |
| **Base commit** | `686bfd0` (Phase 2 complete) |
| **Branch base** | `aee2c37` (EPIC 5 Sprint B) |
| **Goal** | Extract remaining inline Compose widgets and Canvas effects from `GameScreen.kt` into standalone files |
| **Constraints** | Zero behavioral changes; visual parity; no gameplay/state/manager modifications |
| **Deferred** | Threat entity rendering (~826 lines) — too coupled, high risk |
| **Estimated net reduction** | ~268 lines (3,326 → ~3,058) |

---

## Scope

Phase T2 touches exactly **one file**: `app/src/main/java/com/example/jump_droid/GameScreen.kt`.

Every change is a **structural replacement** — inline code is removed and replaced by a call to a new extracted composable or draw-function. No game logic, state management, physics, rendering, or manager code is touched. No existing extracted files are modified.

---

## Phase T2A — Low Risk: Pure Compose Widgets

### Characterization

Standard Compose widget trees (`Text`, `Surface`, `Column`, `Row`, `Button`, `AnimatedVisibility`, `AnimatedContent`). No Canvas drawScope calls. No game loop timing dependencies. Each extraction receives game state as explicit read-only parameters and emits callback lambdas for user interaction.

### Estimated reduction: ~164 lines

### 1. TopRightUtilityButtons

| Field | Value |
|-------|-------|
| **Location** | Lines 3044–3066 |
| **Size** | 23 lines |
| **Description** | Two small circular buttons aligned to top-end: Help (`?`) and Pause (`\|\|`). Pause button is conditionally visible only during `GameState.PLAYING`. |
| **Dependencies** | `gameState` (reads), `isThrusting` (sets false on click) |
| **Risk** | LOW — pure UI composition, no game state mutations passed through |

**Extraction:**
```kotlin
@Composable
fun TopRightUtilityButtons(
    gameState: GameState,
    onHelp: () -> Unit,
    onPause: () -> Unit
)
```

**File:** `TopRightUtilityButtons.kt`

---

### 2. Gauge Column Wrappers

| Field | Value |
|-------|-------|
| **Location** | Lines 3072–3082 (left), 3085–3095 (right) |
| **Size** | 22 lines total (11 each) |
| **Description** | Two `Column` containers that wrap the existing Fuel/Heat gauges (left) and Shield/Integrity gauges (right). Each column has `graphicsLayer(alpha = 0.85f)` and `Arrangement.spacedBy(20.dp)`. |
| **Dependencies** | `player.fuel`, `player.maxFuel`, `player.heat`, `player.maxHeat`, `player.isOverheated`, `player.shield`, `player.maxShield`, `player.integrity`, `player.maxIntegrity`, `gameTime` |
| **Risk** | LOW — pure column wrappers, no logic, no interaction |

**Extraction:**
```kotlin
@Composable
fun LeftGauges(
    fuel: Float, maxFuel: Float,
    heat: Float, maxHeat: Float, isOverheated: Boolean,
    gameTime: Long
)

@Composable
fun RightGauges(
    shield: Float, maxShield: Float,
    integrity: Float, maxIntegrity: Float,
    gameTime: Long
)
```

**File:** `HudWidgets.kt` (append to existing file, consistent with HUD extraction pattern)

---

### 3. MissionRow

| Field | Value |
|-------|-------|
| **Location** | Lines 3117–3219 |
| **Size** | 103 lines |
| **Description** | Horizontal row of mission cards. Each card is a `Surface` showing: `toIcon()`, rotating name/description via `AnimatedContent`, progress text, and a thin progress bar. Ceremony stages (`GLOW`, `COMPLETED_TEXT`, `REWARD_SPAWNED`, `REPLACING`) control card border color, visibility, and content. The outer `AnimatedVisibility` (lines 3099–3107) and wrapping `Column`/`Row` are part of the same semantic unit. |
| **Dependencies** | `missionManager.activeMissions`, `globalShowObjective`, `MissionType` enum, `CeremonyStage` enum, `Mission` data class |
| **Risk** | MEDIUM — reads mission state and ceremony stage; pure UI composition but many edge conditions per card |

**Extract as:**
```kotlin
@Composable
fun MissionRow(
    missions: List<Mission>,
    globalShowObjective: Boolean
)
```

**File:** `MissionRow.kt`

---

### 4. FloatingTextsLayer

| Field | Value |
|-------|-------|
| **Location** | Lines 3240–3255 |
| **Size** | 16 lines |
| **Description** | Iterates `floatingTexts` list and renders each as a `Text` composable with: offset position (`ft.x`, `ft.y - cameraY`), fading alpha (`(ft.life/1.0f).coerceIn(0f, 1f)`), pulsing scale for critical items, and shadow. |
| **Dependencies** | `floatingTexts: List<FloatingText>`, `cameraY` |
| **Risk** | LOW — simple list iteration with per-item rendering; no state mutations |

**Extraction:**
```kotlin
@Composable
fun FloatingTextsLayer(
    texts: List<FloatingText>,
    cameraY: Float
)
```

**File:** `FloatingTextsLayer.kt`

---

## Phase T2B — Medium Risk: Canvas Effects

### Characterization

Inside the `Canvas(modifier = Modifier.fillMaxSize()) { }` drawScope (starting at line 2015). Each entry draws within a `DrawScope` context. Extraction requires passing the `DrawScope` receiver (or `this`) and the relevant state. These are extension functions on `DrawScope` rather than `@Composable` functions.

### Estimated reduction: ~104 lines

### 5. Ground Rectangle

| Field | Value |
|-------|-------|
| **Location** | Line 2067 |
| **Size** | 1 line |
| **Description** | Single `drawRect(Color(0xFF795548))` at ground level. |
| **Dependencies** | `groundY`, `cameraY`, `screenWidth`, `screenHeight`, `ROCKET_HEIGHT` |
| **Risk** | LOW — trivial one-liner |

**Extraction:**
```kotlin
fun DrawScope.drawGround(
    groundY: Float, cameraY: Float,
    screenWidth: Float, screenHeight: Float
)
```

---

### 6. Speed Lines

| Field | Value |
|-------|-------|
| **Location** | Lines 2050–2064 |
| **Size** | 15 lines |
| **Description** | When `abs(player.velocityY) / 1200f > 0.4f`, draws 8 random vertical white lines with alpha and length proportional to speed ratio. |
| **Dependencies** | `player.velocityY`, `screenWidth`, `screenHeight` |
| **Risk** | LOW — deterministic draw function; reads one velocity float |

**Extraction:**
```kotlin
fun DrawScope.drawSpeedLines(
    velocityY: Float,
    screenWidth: Float, screenHeight: Float
)
```

---

### 7. Impact Flash Overlay

| Field | Value |
|-------|-------|
| **Location** | Lines 2990–2996 |
| **Size** | 7 lines |
| **Description** | White stroked rectangle across full canvas when `impactFlashAlpha > 0`. Creates screen flash on damage/events. |
| **Dependencies** | `impactFlashAlpha`, `size` |
| **Risk** | LOW — single variable + drawRect |

**Extraction:**
```kotlin
fun DrawScope.drawImpactFlash(
    alpha: Float,
    size: Size
)
```

---

### 8. Reality Distortion Overlay

| Field | Value |
|-------|-------|
| **Location** | Lines 2033–2045 |
| **Size** | 13 lines |
| **Description** | When `HAZ_VOID_ANOMALY` is active and player is within 1000f, draws a magenta translucent overlay with intensity proportional to proximity. |
| **Dependencies** | `threatManager.activeThreats`, `player.x`, `player.y`, `size` |
| **Risk** | MEDIUM — queries threat manager list; needs null-safety for the find |

**Extraction:**
```kotlin
fun DrawScope.drawRealityDistortion(
    threats: List<Threat>,
    playerX: Float, playerY: Float,
    size: Size
)
```

---

### 9. Particles Layer

| Field | Value |
|-------|-------|
| **Location** | Lines 2069–2085 |
| **Size** | 17 lines |
| **Description** | Iterates `particles` list. Particles > 5f render as cross-shaped sparkle stars with time-based flicker; smaller particles render as circles. |
| **Dependencies** | `particles: List<Particle>`, `cameraY`, `gameTime` |
| **Risk** | LOW — simple iteration; reads particle state |

**Extraction:**
```kotlin
fun DrawScope.drawParticles(
    particles: List<Particle>,
    cameraY: Float,
    gameTime: Long
)
```

---

### 10. Landing Effect Rings

| Field | Value |
|-------|-------|
| **Location** | Lines 2086–2094 |
| **Size** | 9 lines |
| **Description** | Expands cyan ring circles for each landing effect, with alpha and radius animated by `life` timer. |
| **Dependencies** | `landingEffects: List<LandingEffect>`, `cameraY` |
| **Risk** | LOW — deterministic draw; reads list + camera position |

**Extraction:**
```kotlin
fun DrawScope.drawLandingEffects(
    effects: List<LandingEffect>,
    cameraY: Float
)
```

---

### 11. PowerUp Layer

| Field | Value |
|-------|-------|
| **Location** | Lines 2934–2955 |
| **Size** | 22 lines |
| **Description** | Iterates `powerUps` list and renders colored shapes per type: circles for ARTIFACT/SHIELD_CAPSULE/HULL_REPAIR, rectangles for others. Survival capsules have pulsing white ring. |
| **Dependencies** | `powerUps: List<PowerUp>`, `cameraY`, `gameTime` |
| **Risk** | LOW — straightforward branch-on-type drawing |

**Extraction:**
```kotlin
fun DrawScope.drawPowerUps(
    powerUps: List<PowerUp>,
    cameraY: Float,
    gameTime: Long
)
```

---

### 12. Flying Rewards Layer

| Field | Value |
|-------|-------|
| **Location** | Lines 2958–2977 |
| **Size** | 20 lines |
| **Description** | Interpolates reward position by `fr.progress`, picks color by `ComboReward` type, draws scaled circle with inner white dot. |
| **Dependencies** | `flyingRewards: List<FlyingReward>` |
| **Risk** | LOW — pure draw function |

**Extraction:**
```kotlin
fun DrawScope.drawFlyingRewards(
    rewards: List<FlyingReward>
)
```

---

### Combined Canvas Effects File

All T2B extractions share the same drawScope dependency pattern and belong in a single file:

**File:** `CanvasEffects.kt` (package `com.example.jump_droid`)

The GameScreen.kt Canvas block would replace each inline block with a single line call, e.g.:
```
drawGround(groundY, cameraY, screenWidth, screenHeight)
drawSpeedLines(player.velocityY, screenWidth, screenHeight)
drawImpactFlash(impactFlashAlpha, size)
...
```

---

## Execution Order

### T2A (Low Risk) — Sequential edits, one commit

| Step | Action | Lines removed |
|------|--------|--------------|
| 1 | Extract `TopRightUtilityButtons.kt` | 23 |
| 2 | Append `LeftGauges` / `RightGauges` to `HudWidgets.kt` | 22 |
| 3 | Extract `MissionRow.kt` | 103 |
| 4 | Extract `FloatingTextsLayer.kt` | 16 |
| — | Commit `refactor-t2a` | **164 total** |

### T2B (Medium Risk) — Sequential edits, one commit

| Step | Action | Lines removed |
|------|--------|--------------|
| 1 | Create `CanvasEffects.kt` with all 8 extension functions | — |
| 2 | Replace each inline Canvas block with function call | 104 |
| — | Commit `refactor-t2b` | **104 total** |

---

## Files to Create

| File | Phase | Purpose |
|------|-------|---------|
| `TopRightUtilityButtons.kt` | T2A | Help + Pause buttons |
| `MissionRow.kt` | T2A | Horizontal mission card row |
| `FloatingTextsLayer.kt` | T2A | Floating status text overlay |
| `CanvasEffects.kt` | T2B | 8 DrawScope extension functions |
| `docs/architecture/Refactor_T2A_Report.md` | After T2A | Completion report |
| `docs/architecture/Refactor_T2B_Report.md` | After T2B | Completion report |

## Files to Modify

| File | Phase | Change |
|------|-------|--------|
| `GameScreen.kt` | Both | Replace inline blocks with extracted calls |
| `HudWidgets.kt` | T2A | Append `LeftGauges` / `RightGauges` |
| `docs/CHANGELOG.md` | Both | Add dated entries |
| `OPENCODE.md` | Both | Update status and line counts |

---

## Deferred (Indefinitely)

| Item | Lines | Reason |
|------|-------|--------|
| Threat entity rendering | ~826 | Extremely coupled (20+ types, gameTime animation, lifecycle phases, random seed per instance). ~10× the size of all T2 candidates combined. Extracting would introduce high risk of visual regressions with no meaningful coupling reduction — the data flow (threatManager → draw) remains unchanged regardless of where the draw code lives. |

---

## Summary

| Metric | Current | After T2A | After T2B |
|--------|---------|-----------|-----------|
| GameScreen.kt lines | 3,326 | ~3,162 (−5%) | ~3,058 (−8%) |
| New files created | — | 3 | 4 |
| Risk level | — | LOW | MEDIUM |
| Build verification | — | `assembleDebug` | `assembleDebug` |
| APK test | — | Emulator launch | Emulator launch |
