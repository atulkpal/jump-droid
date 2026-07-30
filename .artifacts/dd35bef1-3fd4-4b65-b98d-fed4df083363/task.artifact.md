# Task List - Zen & Multiplayer Overhaul

- [ ] Data Layer: Mode-Specific Statistics
    - [ ] `StatRecorder.kt`: Add Zen and MP fields, persistence, and sync logic.
    - [ ] `ProgressionManager.kt`: Expose fields and add commit methods.
- [ ] Main Menu: Ceremonious Action UI
    - [ ] `MainMenuScreen.kt`: Move launch buttons below toggle.
    - [ ] `MainMenuScreen.kt`: Refactor `ZenCommandConsole` to status-only.
    - [ ] `MainMenuScreen.kt`: Implement `AnimatedVisibility` for new buttons.
- [ ] Gameplay: Mode Isolation
    - [ ] `GameEngine.kt`: Disable bosses, missions, and achievements in Zen mode.
    - [ ] `GameEngine.kt`: Disable continue logic for Zen mode.
    - [ ] `GamePlayScreen.kt`: Implement Zen "Restart" flow.
- [ ] Telemetry: Split Log View
    - [ ] `LeaderboardScreen.kt`: Implement Split Log (Standard vs. Zen).
    - [ ] `LeaderboardScreen.kt`: Add Zen Max Combo highlight.
    - [ ] `LeaderboardScreen.kt`: Add MP stats to Global Telemetry.
- [ ] Verification & Build
    - [ ] Run `gradle_build`.
    - [ ] Verify UI and data integrity.
