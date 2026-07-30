# Task List - Final Zen Mode Hardening (v2)

- [x] Robust State Reset (`GameEngine.kt`)
    - [x] Refactor `restartGame` to ensure platforms and stats are cleared even if screen dimensions aren't ready.
- [x] Fix Incomplete Mode Propagation (`MainActivity.kt`)
    - [x] Update Pause, Hangar, and Game Over restart lambdas to pass `activeGameMode`.
- [x] Harden Encounter Logic (`EncounterDirector.kt`)
    - [x] Verified redundant guards for all boss/mini-boss spawning paths.
- [x] UI Mode Visibility (`GamePlayScreen.kt`)
    - [x] Wired `ZenModeIndicator` to `activeGameMode`.
    - [x] Hidden mission and achievement cards when in Zen mode.
- [x] One-Life Lockdown (`GameOverOverlay.kt`)
    - [x] Strictly hidden continue section and credits for Zen runs.
- [x] Fix Pause Menu in Zen Mode
    - [x] `GamePlayScreen.kt`: Add `GameState.ZEN` to pause/help button guards.
    - [x] `GamePlayScreen.kt`: Add `BackHandler` for pausing during Zen/Standard gameplay.
- [x] Verification
    - [x] Full `gradle_build` success.
    - [x] Logical proof of mode persistence.
