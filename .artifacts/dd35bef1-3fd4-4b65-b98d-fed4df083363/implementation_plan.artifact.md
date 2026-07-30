# Implementation Plan - Zen & Multiplayer Overhaul (Phase 2)

This plan overhauls the game modes to strictly separate Zen Mode (peaceful flight) and Uplink Protocol (Multiplayer), implements ceremonies for new mode unlocks, and enables high-fidelity mode-specific tracking.

## User Review Required

> [!IMPORTANT]
> **Zen Mode Isolation**: Zen Mode is now a "Peaceful Glide" mode. It will have **no bosses**, isolated scores/combos, and no continue logic. Every death in Zen mode requires a fresh start (via Ad or Premium).
>
> **Main Menu UI**: Launch buttons for Zen and Multiplayer will appear ceremoniously **below** the Command Center toggle once unlocked, matching the scale of the primary Launch button.

## Proposed Changes

### 1. Data Layer: Advanced Mode Tracking

#### [MODIFY] [StatRecorder.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/StatRecorder.kt)
- Add storage for:
    - **Zen Mode**: `zenTopRuns` (List of Top 3), `zenMaxCombo`.
    - **Multiplayer**: `mpGamesPlayed`, `mpWins`, `mpLosses`.
- Update `loadStats`, `commitSessionStats`, and `clear` to handle these new fields.

#### [MODIFY] [ProgressionManager.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/ProgressionManager.kt)
- Expose the new Zen and Multiplayer metrics.
- Add commit methods: `commitZenStats(score, combo)` and `commitMpResult(won: Boolean)`.

---

### 2. Main Menu: Ceremonious Mode Unlocks

#### [MODIFY] [MainMenuScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/MainMenuScreen.kt)
- **Primary Actions Area**:
    - Add a new section below the `COMMAND CENTER` toggle.
    - Implement **Full-Scale Launch Buttons** for Zen and Multiplayer.
    - **Zen Button**: Purple theme, visible only when unlocked.
    - **Uplink Button**: Cyan theme, visible only when unlocked.
    - Wrap in `AnimatedVisibility` for a slide/fade entry ceremony.
- **Tactical Console Refactor**:
    - Revert the console to a pure **Calibration & Intelligence** readout.
    - It will exclusively show unlock requirements and calibration progress.
    - If a mode is authorized, the console for that tier will display a "PROTOCOL ACTIVE" status message instead of a button.

---

### 3. Gameplay: Peaceful Glide (Zen) & Mastery (MP)

#### [MODIFY] [EncounterDirector.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/EncounterDirector.kt)
- Update `update()` logic to completely skip boss milestone checks and recurrence spawns when `gameMode == ZEN`.

#### [MODIFY] [GameEngine.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/GameEngine.kt)
- **Zen Mode Rules**:
    - Disable achievement triggers and mission progression during Zen runs.
    - Disable the REVIVE/CONTINUE logic. Hull destruction in Zen mode leads directly to the summary screen.
    - Ensure Zen runs use the `commitZenStats` path for persistence.
- **Multiplayer Scoring**:
    - Implement distinct scoring logic for VS matches to be surfaced in global telemetry.

---

### 4. Telemetry: Side-by-Side Intelligence

#### [MODIFY] [LeaderboardScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/LeaderboardScreen.kt)
- **Split Expedition Log**:
    - Redesign the historical log as a two-column view (Standard on Left, Zen on Right).
    - Each column displays its respective **Top 3** scores.
- **Mode Highlights Row**:
    - Add a footer to the logs showing **ZEN MAX COMBO** to track long-term glide mastery.
- **Global Telemetry**:
    - Add a dedicated "MULTIPLAYER COMMAND" section with **GAMES PLAYED**, **W/L RECORD**, and **RANKING**.

## Verification Plan

### Automated Tests
- `gradle_build` to ensure code integrity.

### Manual Verification
1.  **Zen Flow**: Unlock Zen via debug button. Verify the new purple launch button appears below the toggle. Start a run and confirm **no bosses** appear and **death is final** (no continue).
2.  **Stat Isolation**: Verify Zen scores appear only in the Zen column of the Fleet Terminal and don't affect the standard Best Ascent.
3.  **Console Status**: Confirm the Tactical Console correctly shows "PROTOCOL AUTHORIZED" without the redundant launch button.
