# EPIC 8 Runtime Verification — Navigation & State Audit

**Date:** 2026-06-22
**Project:** Jump Droid
**Status:** Audit Complete (Issues Identified)

---

## 1. Screenshot Evidence

| Screen | Path (Intended) | Observations |
| :--- | :--- | :--- |
| **Title Screen** | `docs/screenshots/EPIC8_AUDIT/title.png` | Aesthetic intact. |
| **Main Menu** | `docs/screenshots/EPIC8_AUDIT/main_menu.png` | **REGRESSION**: Buttons lost geometric cut corners. |
| **Hangar Screen** | `docs/screenshots/EPIC8_AUDIT/hangar.png` | **BUG**: "MISSIONS" widget shows 26 DONE (incorrect). |
| **Mission Log** | `docs/screenshots/EPIC8_AUDIT/mission_log.png` | **BUG**: Displays 8/43. Incorrect Design (48-card grid instead of 12-track dashboard). |
| **About Screen** | `docs/screenshots/EPIC8_AUDIT/about.png` | **MISMATCH**: Screen is titled "MISSION DATA" but contains Lore/Credits. |
| **Gameplay HUD** | `docs/screenshots/EPIC8_AUDIT/gameplay.png` | **REGRESSION**: Combo timer at Top-Right (should be Top-Left). |

---

## 2. Navigation Audit

*   **Main Menu → MISSION DATA**: Correct routing to `GameState.ABOUT`, but the destination title "MISSION DATA" is a naming collision with the actual Mission Log.
*   **Hangar → MISSIONS**: Correct routing to `GameState.MISSIONS`.
*   **Hangar → LOADOUT**: Correct routing to `GameState.LOADOUT`. (Verified coordinates: `752, 374` correctly triggers edit mode).
*   **About → Missions**: No direct link exists. Both are top-level destinations.

---

## 3. Mission Counter Audit

**Observed State:**
*   **Hangar Widget**: "26 DONE"
*   **Mission Log**: "8 / 43"

**Root Cause Analysis:**
*   `ProgressionManager.missionsCompleted` is stored as a raw `Int` in `sharedPrefs` ("missions_completed").
*   The Mission Log calculates its count from the size of `ProgressionManager.completedMissionIds` (Set<String>).
*   **Legacy Data**: The Int count includes progress from a previous mission system build, while the Set only contains IDs recognized by the *new* `MissionRegistry`.

---

## 4. Root Cause Analysis Summary

### **A. Mission Screen Visibility**
The screen is **functional but divergent**. It renders 43 individual cards because the filter for "Lowest Uncompleted Tier per Track" has not been implemented. It feels "Empty" of the intended vision.

### **B. Visual Regression**
During the migration, `MainMenuScreen.kt` was updated with standard `Button` components and `RoundedCornerShape(4.dp)`. This overwrote the specialized "geometric cut" styling from EPIC 7.

### **C. HUD Clutter**
The `newMissionOverlay` logic in `GameScreen.kt` is still active. It triggers a legacy mission card whenever an unstarted mission is detected at run start.

---

## 5. Proposed Fixes

1.  **Unify Source of Truth**: Force `missionsCompleted` to be a derived property of `completedMissionIds.size` to resolve Hangar desync.
2.  **Rename About Screen**: Change UI title from "MISSION DATA" to "SYSTEM PROTOCOL" or "ABOUT".
3.  **Restore Geometric Buttons**: Re-implement the custom `Shape` for all menu and hangar navigation.
4.  **Implement Track Filter**: Update `MissionScreen.kt` to group missions by track and only show the next available tier.
5.  **Disable HUD Cards**: Completely remove `newMissionOverlay` and transition to transient notifications.
