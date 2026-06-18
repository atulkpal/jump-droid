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
- Encapsulates the notification queue, timers, and alpha state management.
- **Target API**: `notificationManager.post(message: String)`, `notificationManager.update(dt)`

### 2. FloatingTextManager
- Manages the list of active `FloatingText` instances and their lifecycle (life, position drift).
- **Target API**: `floatingTextManager.add(text: FloatingText)`, `floatingTextManager.update(dt)`

### 3. ProgressionManager Expansion (Achievement Audit)
- Moves the achievement check loop and high score persistence logic from `GameScreen` into the existing `ProgressionManager`.
- **Target API**: `progressionManager.checkUnlocks(score, player)`, `progressionManager.saveHighScore(score)`

---

## Phase T3B — Medium-Low Risk: System Delegation

### 4. PlatformManager
- Moves `generatePlatform()` and the associated streak counters (`breakableStreak`, `phaseStreak`, `magneticStreak`).
- **Target API**: `platformManager.generate(score, screenWidth, lastY)`

---

## Summary

| Metric | Current | After T3 |
|--------|---------|----------|
| GameScreen.kt lines | 3,109 | ~2,750 (−11%) |
| New files created | — | 3 |
| Risk level | — | LOW / MEDIUM-LOW |
| Build verification | — | `assembleDebug` |
