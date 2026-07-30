# Task List - Zen & Multiplayer Overhaul

- [x] Data Layer: Mode-Specific Statistics
    - [x] `StatRecorder.kt`: Add Zen and MP fields, persistence, and sync logic.
    - [x] `ProgressionManager.kt`: Expose fields and add commit methods.
- [x] Main Menu: Ceremonious Action UI
    - [x] `MainMenuScreen.kt`: Move launch buttons below toggle.
    - [x] `MainMenuScreen.kt`: Refactor `ZenCommandConsole` to status-only.
    - [x] `MainMenuScreen.kt`: Implement `AnimatedVisibility` for new buttons.
- [x] Gameplay: Mode Isolation
    - [x] `GameEngine.kt`: Disable bosses, missions, and achievements in Zen mode.
    - [x] `GameEngine.kt`: Disable continue logic for Zen mode.
    - [x] `GamePlayScreen.kt`: Implement Zen "Restart" flow.
- [x] Telemetry: Split Log View
    - [x] `LeaderboardScreen.kt`: Implement Split Log (Standard vs. Zen).
    - [x] `LeaderboardScreen.kt`: Add Zen Max Combo highlight.
    - [x] `LeaderboardScreen.kt`: Add MP stats to Global Telemetry.
- [x] Verification & Build
    - [x] Run `gradle_build`.
    - [x] Verify UI and data integrity.
