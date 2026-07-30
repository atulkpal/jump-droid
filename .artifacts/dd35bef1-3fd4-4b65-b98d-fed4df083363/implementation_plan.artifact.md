# Implementation Plan - UI Refinement & Impossible Uplink Unlock

This plan removes the redundant Zen Mode indicator and updates the Multiplayer "Uplink" unlock criteria to a temporary "impossible" threshold.

## Proposed Changes

### 1. Engine & Progression

#### [MODIFY] [StatRecorder.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/StatRecorder.kt)
- Add `lifetimeCombosOver50` state and persistence logic to track elite performance runs.

#### [MODIFY] [ProgressionManager.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/ProgressionManager.kt)
- Update `checkMultiplayerUnlock`: Require 3 runs with a 50x combo.
- Update `getMultiplayerRequirements`: Reflect the new "3x 50 Combo" goal.
- Update `getMultiplayerUnlockProgress`: Calculate progress based on the 50x combo count.

---

### 2. UI Refinement

#### [MODIFY] [GamePlayScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/GamePlayScreen.kt)
- Remove the `ZenModeIndicator` call from the HUD layer. The unique music menu is now the primary visual differentiator for Zen mode.

## Verification Plan

### Automated Tests
- `gradle_build` to ensure all stat references are correct.

### Manual Verification
1.  **Zen Purity**: Start a Zen run. Verify the top "ZEN MODE" text is gone, but the music menu remains.
2.  **Terminal Check**: Open the Main Menu and check the Uplink Command Console. Verify it now displays "COMBOS (50x) // 0 / 3" as a requirement.
