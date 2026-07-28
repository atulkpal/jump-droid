# EPIC 12 Phase 8 — Technical Foundation Implementation Plan

This plan covers the refactoring and profiling tasks for Phase 8.

## User Review Required

> [!IMPORTANT]
> **Task 8.3 (StarfieldBackground Extraction)** is already complete in the current codebase. `StarfieldBackground.kt` exists and is used across 7 screens. No further action is needed for this task.

> [!WARNING]
> **Task 8.2 (ProgressionManager Decomposition)** involves splitting a core class used by many systems. I propose keeping `ProgressionManager` as a coordinator/facade to maintain backward compatibility while moving logic into focused domain services.

## Proposed Changes

### [Task 8.2] ProgressionManager Decomposition
`ProgressionManager.kt` is currently ~457 lines. I propose splitting it into the following internal services:

#### [NEW] [ArtifactManager.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/ArtifactManager.kt)
- Responsibility: Artifact records, set bonus evaluation, and stat multipliers (Fuel/Shield/Heat/Thrust).

#### [NEW] [ModuleInventory.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/ModuleInventory.kt)
- Responsibility: Tracking owned modules and grant logic.

#### [NEW] [MissionTracker.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/MissionTracker.kt)
- Responsibility: Completion/Claim tracking, mission progress persistence.

#### [NEW] [StatRecorder.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/StatRecorder.kt)
- Responsibility: Lifetime stats accumulation and session commits.

#### [NEW] [UnlockService.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/UnlockService.kt)
- Responsibility: Requirement evaluation for modules, lore, rockets, and achievements.

#### [MODIFY] [ProgressionManager.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/ProgressionManager.kt)
- Refactor to delegate to the new services while remaining the primary entry point for `GameEngine` and UI.

---

### [Task 8.4] Navigation Migration (Discovery Phase)
Current `NavHost` in `MainActivity.kt` covers all top-level screens. Overlays are currently state-driven inside `GamePlayScreen.kt`.

**Potential NavHost Dialog Routes:**
- `pause` — Currently `GameState.PAUSED`
- `game_over` — Currently `GameState.GAMEOVER`
- `unlock` — Currently `GameState.UNLOCK`
- `tutorial` — Currently `GameState.TUTORIAL`

**Recommendation:**
- Migrating `GameOverOverlay` and `PauseOverlay` to `navController.navigate` routes would allow for cleaner back-stack management and potentially deep-linking to specific results.

---

### [Task 8.5] Performance Profiling (Analysis Phase)
Identified potential bottlenecks in `WorldRenderer.kt` and `ZoneBackgroundRenderer.kt`:

1. **RepeatingParallaxLayer**: Uses lambdas with multiple `drawCircle`/`drawOval` calls in loops.
   - *Optimization:* Use `drawPoints` where possible for stars. Cache static silhouettes in `ImageBitmap` or `Picture`.
2. **drawParticles**: Iterates through all particles.
   - *Optimization:* Use a single `drawPoints` call for simple particles. Prune inactive particles more aggressively.
3. **drawPowerUps**: Multiple `rotate`/`scale` and `Path` operations per frame.
   - *Optimization:* Cache Power-Up visuals in `ImageBitmap` or `drawCache`.
4. **drawTether**: Random segment generation happens every frame in the draw loop.
   - *Optimization:* Move random generation out of `DrawScope` if possible or use a more efficient pattern.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` after any refactor.
- (Optional) Add unit tests for `MissionTracker` and `ArtifactManager` to ensure persistence logic is unchanged.

### Manual Verification
- Verify High Score, Cash, and Credits persist correctly after `ProgressionManager` split.
- Verify Mission progress is still tracked.
- Verify all overlays still appear correctly in `GameScreen`.
