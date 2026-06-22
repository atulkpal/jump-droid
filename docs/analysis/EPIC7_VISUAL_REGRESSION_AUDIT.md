# EPIC 7 Visual Regression Audit

**Date:** 2026-06-22
**Project:** Jump Droid
**Objective:** Identify lost UI polish and styling during the Mission System migration.

---

## 1. Observations

### **Main Menu (Command Center)**
*   **Regression**: Buttons have returned to a basic rectangular shape with thin borders.
*   **EPIC 7 Standard**: Buttons should feature "cut corner" geometric shapes and inner glow effects.
*   **Current State**: High-functionality, Low-aesthetic.

### **Hangar Screen**
*   **Inconsistency**: The "MISSIONS" widget reports "26 DONE", while the Mission Log reports "8 / 43". This indicates a split in state between `ProgressionManager` and `MissionManager`.
*   **Layout**: Navigation buttons (Archive, Loadout, Missions) are plain text without icons, deviating from the "Premium Screen Polish" goals.

### **Gameplay HUD**
*   **Combo Timer**: Currently at `TopEnd` (Top Right). Intended vision: **Top Left** to clear the "Pause/Help" buttons.
*   **Mission Clutter**: Although `MissionRow` is commented out, a 70dp `AnimatedVisibility` gap remains under the score, creating dead space.

### **Mission Log**
*   **Density**: Cards are too large, showing only ~4 at a time. Intended: Compact dashboard.
*   **Data Sync**: Completed missions show "0/X" progress because the `currentProgress` metric is not yet persistent.

---

## 2. Recovery Plan
1.  **Re-align Buttons**: Apply Sci-Fi geometric shapes to all `Main Menu` and `Hangar` navigation.
2.  **Move Combo Timer**: Relocate to `TopStart` (Top Left) with appropriate padding.
3.  **Unify Mission Counters**: Use `MissionManager.getAllMissions().count { it.isClaimed }` as the single source for the Hangar badge.
4.  **Compact Grid**: Transition from large cards to the "12 Evolving Tracks" design to reduce visual fatigue.
