# Implementation Plan - GameOver Enhancements & Zen Mode Fix

This plan covers two major requests:
1.  **GameOver / Collection Screen Enhancements**: Adding a session summary and a lore completion bar to the rewards collection screen.
2.  **Zen Mode Unlock Logic**: Fixing the missing persistence for lifetime altitude and combo stats to ensure Zen Mode can be unlocked and its progress tracked.

## Proposed Changes

### [app](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app)

#### [MODIFY] [StatRecorder.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/StatRecorder.kt)
- Add `lifetimeAltitude` and `maxComboEver` properties.
- Update `loadStats` to read `stat_lifetime_altitude` and `stat_max_combo`.
- Update `commitSessionStats` to increment `lifetimeAltitude` by the run's altitude and update `maxComboEver` if the current run's combo is higher.

#### [MODIFY] [ProgressionManager.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/ProgressionManager.kt)
- Update `loadProgression` if necessary (mostly handled by `StatRecorder`).
- Ensure `getZenRequirements()` and `getZenUnlockProgress()` correctly reflect the lifetime stats from `StatRecorder`.
- Update `checkZenModeUnlock` to rely on the updated lifetime stats.

#### [MODIFY] [ExpeditionRewardsOverlay.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/ExpeditionRewardsOverlay.kt)
- Update the `ExpeditionRewardsOverlay` composable to accept `progressionManager` and `sessionStats: GameStats`.
- In the "ALL REWARDS SYNCED" state, replace the simple text with a comprehensive "SESSION SUMMARY":
    - Display key session results: Final Altitude, Bosses Defeated, and Total Score.
    - Display a **Lore Completion Bar**: A visual progress bar showing the percentage of total Codex entries discovered.
    - Display Zen Mode progress if it's still locked.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/MainActivity.kt)
- Pass `engine.progressionManager` and `engine.getGameStats()` to `ExpeditionRewardsOverlay`.

## Verification Plan

### Automated Tests
- Run `gradle_build(":app:assembleDebug")` to ensure no syntax errors.

### Manual Verification
- **GameOver Flow**:
    1.  Die in a run with pending unlocks (e.g., finish a mission).
    2.  Flick through reward cards.
    3.  Verify the final summary screen shows the correct stats, lore percentage, and Zen progress.
- **Zen Mode Unlock**:
    1.  Play several runs to accumulate altitude.
    2.  Verify in `MainMenuScreen` (or the new summary) that Zen progress is increasing.
    3.  Confirm Zen Mode unlocks once all conditions (10k altitude, 5 bosses, 50 combo) are met across runs.
