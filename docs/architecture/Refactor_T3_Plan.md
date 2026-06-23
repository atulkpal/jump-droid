# Refactor Sprint T3 — Plan: Logic & Manager Extraction from GameScreen.kt

## Metadata

| Field | Value |
|-------|-------|
| **Sprint** | T3 (Tidying - Logic Extraction) |
| **Phases** | 2 (T3A Low Risk, T3B Medium-Low Risk) |
| **Branch** | `refactor/logic-extraction` |
| **Base commit** | `af3d0ae` (Refactor T2 complete) |
| **Goal** | Extract standalone gameplay logic and manager states from `GameScreen.kt` into dedicated classes |
| **Constraints** | Zero behavioral changes; preserve game loop timing; no modifications to physics/collision |
| **Estimated net reduction** | ~350 lines (3,109 → ~2,750) |

---

## Scope

Phase T3 focuses on extracting non-UI logic that currently clutters the `GameScreen.kt` monolithic scope. This includes utility managers (Notifications, Floating Texts) and pure logic functions (Platform Generation, Achievement Audits). No rendering code or core physics loop internals will be modified.

---

## Phase T3A — Low Risk: Manager Encapsulation

### 1. NotificationManager
<<<<<<< HEAD
- Encapsulates the notification queue, timers, and alpha state management.
- **Target API**: `notificationManager.post(message: String)`, `notificationManager.update(dt)`

### 2. FloatingTextManager
- Manages the list of active `FloatingText` instances and their lifecycle (life, position drift).
- **Target API**: `floatingTextManager.add(text: FloatingText)`, `floatingTextManager.update(dt)`

### 3. ProgressionManager Expansion (Achievement Audit)
- Moves the achievement check loop and high score persistence logic from `GameScreen` into the existing `ProgressionManager`.
- **Target API**: `progressionManager.checkUnlocks(score, player)`, `progressionManager.saveHighScore(score)`
=======

| Field | Value |
|-------|-------|
| **Location** | State: ~139–143 \| Update: ~927–941 |
| **Description** | Encapsulates the notification queue, timers, and alpha state management. |
| **Dependencies** | `dt` (for timers) |
| **Target API** | `notificationManager.post(message: String)`, `notificationManager.update(dt)` |

**File:** `NotificationManager.kt`

---

### 2. FloatingTextManager

| Field | Value |
|-------|-------|
| **Location** | State: ~124 \| Update: ~1025–1031 |
| **Description** | Manages the list of active `FloatingText` instances and their lifecycle (life, position drift). |
| **Dependencies** | `dt` |
| **Target API** | `floatingTextManager.add(text: FloatingText)`, `floatingTextManager.update(dt)` |

**File:** `FloatingTextManager.kt`

---

### 3. ProgressionManager Expansion (Achievement Audit)

| Field | Value |
|-------|-------|
| **Location** | `checkUnlock`: ~352–378 \| `saveHighScore`: ~380–385 |
| **Description** | Moves the achievement check loop and high score persistence logic from `GameScreen` into the existing `ProgressionManager`. |
| **Dependencies** | `sharedPrefs`, `Player` stats |
| **Target API** | `progressionManager.checkUnlocks(score, player)`, `progressionManager.saveHighScore(score)` |
>>>>>>> refactor/system-delegation

---

## Phase T3B — Medium-Low Risk: System Delegation

### 4. PlatformManager
<<<<<<< HEAD
- Moves `generatePlatform()` and the associated streak counters (`breakableStreak`, `phaseStreak`, `magneticStreak`).
- **Target API**: `platformManager.generate(score, screenWidth, lastY)`
=======

| Field | Value |
|-------|-------|
| **Location** | State: ~85–87 \| Function: ~443–481 |
| **Description** | Moves `generatePlatform()` and the associated streak counters (`breakableStreak`, `phaseStreak`, `magneticStreak`). |
| **Dependencies** | `score`, `screenWidth`, `Random` |
| **Target API** | `platformManager.generate(score, screenWidth, lastY)` |

**File:** `PlatformManager.kt`

---

## Execution Order

| Step | Action | Estimated Reduction |
|------|--------|---------------------|
| 1 | Extract `NotificationManager.kt` | ~60 lines |
| 2 | Extract `FloatingTextManager.kt` | ~40 lines |
| 3 | Move `checkUnlock` & `saveHighScore` to `ProgressionManager.kt` | ~50 lines |
| 4 | Create `PlatformManager.kt` and move generation logic | ~120 lines |
| 5 | Clean up state declarations and local function calls | ~80 lines |
| **Total** | | **~350 lines** |

---

## Files to Create

| File | Purpose |
|------|---------|
| `NotificationManager.kt` | Notification queue and timer logic |
| `FloatingTextManager.kt` | Floating text list and update logic |
| `PlatformManager.kt` | Platform generation and streak tracking |

## Files to Modify

| File | Change |
|------|--------|
| `GameScreen.kt` | Replace manager state and logic with external calls |
| `ProgressionManager.kt` | Accept `checkUnlock` and `saveHighScore` methods |
| `docs/CHANGELOG.md` | Add T3 entry |
| `OPENCODE.md` | Update line counts and refactor status |
>>>>>>> refactor/system-delegation

---

## Summary

| Metric | Current | After T3 |
|--------|---------|----------|
| GameScreen.kt lines | 3,109 | ~2,750 (−11%) |
| New files created | — | 3 |
| Risk level | — | LOW / MEDIUM-LOW |
| Build verification | — | `assembleDebug` |
