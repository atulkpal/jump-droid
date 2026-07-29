# Final Polish & Juice Plan — Jump Droid v2.0.0

This plan focuses on "Juice" and "Tactile Feel" — the final 5% of polish that makes an app feel premium and responsive.

## Proposed Changes

### 1. Tactile & Auditory UI Polish

#### [MODIFY] [HapticManager.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/HapticManager.kt)
- Add `TICK` optimization: ensure it's a very short, crisp vibration.

#### [MODIFY] [TitleScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/TitleScreen.kt), [MainMenuScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/MainMenuScreen.kt), [HangarScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/HangarScreen.kt), [SettingsScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/SettingsScreen.kt)
- Standardize UI interactions:
  - Add `hapticManager.vibrate(HapticType.TICK)` to **all** button clicks.
  - Use `soundManager.playSfx("sfx_ui_back")` when closing popups or returning to previous screens.
  - Use `soundManager.playSfx("sfx_ui_confirm")` only for major transitions (e.g., "Launch", "Purchase").

### 2. High-Impact Game Feedback

#### [MODIFY] [GameEngine.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/GameEngine.kt)
- **Signal Discovery Weight**: Add `soundManager.duck(1500L)` and a subtle screen shake (`screenShake = 8f`) when a new signal/artifact is archived.
- **Achievement/Blueprint Weight**: Add `hapticManager.vibrate(HapticType.SUCCESS)` and `soundManager.duck(2000L)` for Achievements and Rocket Blueprints.
- **Rank Display**: Improve formatting of the rank string in HUD/Menus.

### 3. UI Content Polish

#### [MODIFY] [MainMenuScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/MainMenuScreen.kt)
- Fix rank display text to include a colon and uppercase: `"RANK: ${progressionManager.currentRank.title.uppercase()}"`.

## Verification Plan

### Manual Verification
- **Haptic Check**: Run the app and tap every button in the Main Menu, Hangar, and Settings. Each should provide a crisp tactile "tick".
- **Audio Check**: Confirm "Back" sounds different from "Click". Verify "Ducking" triggers when a mission is completed or an artifact is found.
- **Visual Check**: Observe the subtle screen shake on major events to ensure it's not disorienting.
