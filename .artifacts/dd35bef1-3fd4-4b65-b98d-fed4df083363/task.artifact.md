# Task List - Hard Mode Isolation (Zen Mode Fixes)

- [x] Hardening Engine Isolation (`EncounterDirector.kt`)
    - [x] Wrap fallback mini-boss spawning in Zen Mode check
    - [x] Wrap boss reinforcements in Zen Mode check
- [x] Silence Ceremonies (`GameEngine.kt`)
    - [x] Suppress `showUnlockEvent` in Zen mode
    - [x] Suppress `checkDiscovery` notifications/ranks in Zen mode
- [x] UI Hardening (`GameOverOverlay.kt`)
    - [x] Hide all continue and credit UI in Zen mode
    - [x] Update header for Zen Mode completion
- [x] Verification
    - [x] Run `gradle_build`
    - [x] Manual verification of Zen mode purity
