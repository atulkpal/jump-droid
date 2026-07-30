# Implementation Plan - Zen Mode Hardening (Final)

This plan applies "Nuke it from orbit" guards to Zen Mode to ensure a 100% boss-free, achievement-free, and continue-free experience, while adding clear visual feedback during gameplay.

## User Review Required

> [!IMPORTANT]
> **Active Mode State**: I am renaming the internal mode state to `activeGameMode` to eliminate any possible shadowing or ambiguity in the logic.
>
> **Encounter Hardening**: The `EncounterDirector` will now have explicit, redundant guards for all boss-spawning paths.

## Proposed Changes

### 1. Data & Logic Hardening

#### [MODIFY] [GameEngine.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/GameEngine.kt)
- Rename `gameMode` property to `activeGameMode` for absolute clarity.
- Update `showUnlockEvent` to return early if `activeGameMode == ZEN`.
- Update `checkDiscovery` to skip ALL UI/Progression side effects if `activeGameMode == ZEN`.
- Update `update()` loop to strictly skip mission progress if `activeGameMode == ZEN`.
- Update `endRun()` to correctly route to `commitZenStats` and prevent boss-kill attribution if in Zen mode.
- Update `restartGame()` to correctly set `activeGameMode`.

#### [MODIFY] [EncounterDirector.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/EncounterDirector.kt)
- Add explicit, separate guards for:
    - **Milestone Bosses**: Skip entirely in Zen mode.
    - **Fallback Mini-Bosses**: Skip entirely in Zen mode.
    - **Boss Recurrence**: Skip entirely in Zen mode.
    - **Reinforcements**: Skip entirely in Zen mode.

---

### 2. UI Hardening & Visual Feedback

#### [NEW] [HudWidgets.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/HudWidgets.kt)
- Add `ZenModeIndicator()`: A minimalist, centered HUD element.
- Display "ZEN MODE // PEACEFUL GLIDE" in SciFiPurple with a slow, calming pulse animation.
- Positioned right below the primary Altitude Display.

#### [MODIFY] [GamePlayScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/GamePlayScreen.kt)
- Integrate the `ZenModeIndicator` into the HUD layer when `engine.activeGameMode == ZEN`.

#### [MODIFY] [GameOverOverlay.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/GameOverOverlay.kt)
- Explicitly hide the **Entire Continue Section** and **Credit Row** if `isZenMode` is true.
- Add a purple-themed "RE-DEPLOY ZEN MODE" button that replaces the standard restart button to provide clear mode feedback.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/MainActivity.kt)
- Ensure `isZenMode` parameter for `GameOverOverlay` is correctly wired to `engine.activeGameMode`.

## Verification Plan

### Automated Tests
- `gradle_build` to verify syntax and consistency.

### Manual Verification
1.  **Zen Purity Test**: Reach 20,000m in Zen mode. Verify 0 bosses, 0 mini-bosses, and 0 achievement popups.
2.  **Visual Indicator**: During Zen gameplay, verify the purple "ZEN MODE" indicator is visible below the altitude.
3.  **Death Protocol**: Crash in Zen mode. Verify the Game Over screen has NO continue buttons, NO credits, and shows "ZEN EXPEDITION ENDED".
4.  **Mode Switch**: Start a standard run. Verify bosses and achievements work normally.
