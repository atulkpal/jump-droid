# Implementation Plan - Hard Mode Isolation (Zen Mode Fixes)

This plan fixes the leakage of bosses, achievements, and continues into Zen Mode, ensuring a truly "Peaceful Glide" experience.

## User Review Required

> [!IMPORTANT]
> **Zen Mode Purity**: I am applying stricter guards across the engine to ensure that **no bosses** (including random mini-boss fallbacks) can spawn in Zen mode.
>
> **Zero Interruptions**: Achievements and discovery ceremonies will be completely silenced during Zen runs. These will remain exclusive to the Standard mode.

## Proposed Changes

### 1. Engine & Logic (Isolation)

#### [MODIFY] [EncounterDirector.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/EncounterDirector.kt)
- Wrap the following spawning paths in `if (gameMode != GameMode.ZEN)`:
    - **Fallback Mini-Bosses**: Prevents random mini-bosses from appearing when no main boss is active.
    - **Reinforcements**: Prevents bosses (if any somehow spawned) from summoning escorts or hazards.
    - **Recurrence Logic**: (Already partially guarded, but will double-check).

#### [MODIFY] [GameEngine.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/GameEngine.kt)
- **Silence Ceremonies**: Update `showUnlockEvent` to return early if `currentMode == GameMode.ZEN`. This prevents any achievement or mission completion popups.
- **Discovery Guard**: Update `checkDiscovery` to skip rank updates and notification/ceremony triggers if in Zen mode. (Discoveries will still be recorded in the background but won't interrupt flight).

---

### 2. UI Layer (GameOver Cleanup)

#### [MODIFY] [GameOverOverlay.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/GameOverOverlay.kt)
- **Total Continue Suppression**: Ensure the entire "Credits" and "Ad-Link" UI is hidden when `isZenMode` is true.
- **Header Refinement**: Update the "COMMUNICATION LOST" header to "EXPEDITION COMPLETE" or similar when in Zen mode to differentiate it from the "Failure" feel of standard mode.

## Verification Plan

### Automated Tests
- `gradle_build` to verify UI hierarchy remains valid.

### Manual Verification
1.  **Zen Run (10km+)**: Fly past 10,000m in Zen mode. Verify zero bosses appear.
2.  **Achievement Test**: Perform an action that would trigger an achievement (e.g., land on a new platform type). Verify no popup appear.
3.  **Death Flow**: Crash the ship. Verify no "Continue" buttons or "Credits" are visible; only the "Re-Deploy" and "Return to Base" options should remain.
