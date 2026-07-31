# Pre-Release Polish & Zen Mode Implementation

This plan covers a series of improvements intended to elevate the player experience, fix a persistent mission bug, clarify the distinction between Missions and Achievements, and introduce a new Zen Mode.

## User Review Required

> [!IMPORTANT]
> **Zen Mode Music Selection**: I will implement a music selector that appears only in Zen Mode. Players can choose from tracks they have unlocked through progression.

> [!IMPORTANT]
> **Mission Debriefs**: Each mission will now have a "debrief" text shown upon completion/claim. I will use existing lore fragments to populate these to ensure consistency with the world-building.

## Proposed Changes

---

### [Component] Core Game Logic & State

#### [MODIFY] [Models.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/Models.kt)
- Add `ZEN` to `GameState` enum.
- Add `GameMode` enum: `STANDARD`, `ZEN`.
- Add `debrief: String` field to `Mission` class.

#### [MODIFY] [GameEngine.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/GameEngine.kt)
- Add `gameMode: GameMode` property.
- Update `spawnThreats` and `spawnBoss` logic to skip if `gameMode == GameMode.ZEN`.
- Update `restartGame` to accept an optional `GameMode`.
- Ensure Zen Mode is truly endless and "no pressure".

---

### [Component] Mission System Fixes & Artwork

#### [MODIFY] [MissionScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/MissionScreen.kt)
- **Bug Fix**: Add missing `LANDINGS` and `OVERHEAT` categories to appropriate tracks.
    - `LANDINGS` -> `Ground Support`
    - `OVERHEAT` -> `Aeronautics`
- **Artwork**: Add background watermarks (insignias) to `TimelineNode` cards.
- **Debriefs**: Display the new `debrief` text in the card when a mission is completed or claimed.

#### [MODIFY] [MissionRegistry.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/MissionRegistry.kt)
- Populate `debrief` strings for all 48 missions using high-quality lore snippets.

---

### [Component] Continue Screen & Rewards UI

#### [MODIFY] [GameOverOverlay.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/GameOverOverlay.kt)
- Refactor the reward claim UI to use a **Stacked Deck** of cards.
- Implement **Swipe-to-Dismiss** (Tinder-style) for claiming rewards.
- Add animations for claimed rewards (Coins/Skins/Unlocks) flying towards their respective HUD counters or menus.

---

### [Component] Gameplay Variation

#### [MODIFY] [PlatformManager.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/PlatformManager.kt)
- Increase procedural variation in `MOVING` platforms.
- Randomize speed more aggressively.
- Introduce a 20% chance for "Wrap-around" platforms (they don't bounce off edges but teleport to the other side).

#### [MODIFY] [GameEngine.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/GameEngine.kt)
- Implement wrap-around logic for platforms marked as such.

---

### [Component] Zen Mode & Music Progression

#### [MODIFY] [ProgressionManager.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/ProgressionManager.kt)
- Add persistence for `unlockedMusicTracks` (Set of resource IDs).
- Logic to unlock new tracks based on milestones (Score, Missions, Rocket Unlocks).

#### [MODIFY] [SoundManager.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/SoundManager.kt)
- Add `playSpecificTrack(resId: Int)` for Zen Mode.
- celebration effect when a first-time music unlock occurs.

#### [MODIFY] [MainMenuScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/MainMenuScreen.kt)
- Add "ZEN MODE" button.

#### [MODIFY] [GamePlayScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/GamePlayScreen.kt)
- Add a music selector overlay that only appears in Zen Mode.

---

### [Component] Lore & Documentation

#### [NEW] [docs/design/MISSIONS_VS_ACHIEVEMENTS.artifact.md](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/docs/design/MISSIONS_VS_ACHIEVEMENTS.artifact.md)
- Formal audit and documentation of the distinction between the two systems.

#### [MODIFY] [ArchiveScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/ArchiveScreen.kt)
- Polish the "LOGS" and "LORE" presentation to be more visually engaging, encouraging players to re-read unlocked content.

## Verification Plan

### Automated Tests
- `gradle_build` to ensure all structural changes are valid.
- Unit tests for `MissionManager` to verify the claimable count bug is fixed.

### Manual Verification
- Deploy to device/emulator.
- Navigate to Mission Log and verify all claimable missions are visible.
- Launch Zen Mode and test the music selector.
- Reach a milestone and verify the music unlock celebration.
- Trigger Game Over and test the stacked card swipe-to-claim interaction.
