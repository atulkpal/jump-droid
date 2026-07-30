# Task List - Final Zen Mode Hardening

- [x] Rename and Refactor Engine State
    - [x] `GameEngine.kt`: Rename `gameMode` to `activeGameMode` and update all usages.
- [x] Harden Encounter Logic
    - [x] `EncounterDirector.kt`: Add redundant guards for all boss/mini-boss spawning paths.
- [x] Silence UI Ceremonies
    - [x] `GameEngine.kt`: Suppress achievement and discovery UI triggers in Zen mode.
- [x] Implement Visual mode indicator
    - [x] `HudWidgets.kt`: Create `ZenModeIndicator` component.
    - [x] `GamePlayScreen.kt`: Integrate indicator into HUD layer.
- [x] Overhaul GameOver UI
    - [x] `GameOverOverlay.kt`: Hide continue section, credits, and ad-links in Zen mode.
    - [x] `GameOverOverlay.kt`: Add "RE-DEPLOY ZEN MODE" button and mode-specific header.
- [x] Verification
    - [x] Final `gradle_build`.
    - [x] Manual confirmation of boss-free Zen gameplay.
