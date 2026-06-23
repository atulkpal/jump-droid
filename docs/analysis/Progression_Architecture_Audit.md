# Progression & Ownership Architecture Audit — Sprint 7.6

**Date:** 2026-06-22
**Project:** Jump Droid
**Objective:** Establish a robust ownership model for Rocket Modules and unify progression systems.

---

## 1. Current State Analysis

### Module Storage
- **In-Memory**: `Player.activeModules` (List of instances) and `Player.moduleCooldowns` (Map of state).
- **Equipped Persistence**: `LoadoutManager` saves/loads IDs for Slot 1 and Slot 2 into `SharedPreferences` using keys `equipped_module_0` and `equipped_module_1`.
- **Ownership Persistence**: **NONE**. All registered modules are currently visible and selectable in the `LoadoutScreen`.

### Module Unlocking
- **Logic**: No logic currently restricts selection.
- **Metadata**: The `Module` interface contains an `UnlockRequirement` data class (Type, Threshold, Description) added in Sprint 7.2.
- **Auto-Unlocks**: The system is prepared for auto-unlocks based on Score, Altitude, Discovery, Mission, and Artifact count.

### Existing Persistence (SharedPreferences)
- **ProgressionManager**: `highScore`, `discovery_<TYPE>`, `unlock_<ROCKET>`, `achievement_<ID>`, `max_integrity`, `max_shield`, and `art_<ID>_*` (Artifact records).
- **DiscoveryManager**: `discovery_<TYPE>`.

### Existing Progression Hooks
- `ProgressionManager.checkUnlocks()`: Audits score-based rocket and lore unlocks.
- `DiscoveryManager.discover()`: Audits one-time area and threat discoveries.
- `GameScreen.checkDiscovery()`: Central gateway for triggering progression updates.

---

## 2. Expected Reward Sources (EPIC 7 & 8)
1.  **Boss Rewards**: Granting specific modules on first-kill or milestone.
2.  **Discovery Milestones**: Granting modules when N entries in a category are found.
3.  **Artifact Milestones**: Granting modules for finding specific artifacts or sets.
4.  **Mission Rewards**: Granting modules as loot from EPIC 8's tiered mission system.

---

## 3. Risks & Technical Debt
- **State Duplication**: Storing `isUnlocked` booleans for every module ID would duplicate the `equipped` state.
- **Derived State**: Currently, "Available" is implicitly "In Registry". We need to transition to "Available" = "In Owned Set".
- **Mission Branch Drift**: The mission system prototype had its own persistence; we must ensure this ownership system is the single source of truth for EPIC 8.

---

## 4. Recommendations

### R1: Implement `ownedModules` Set
Store a `Set<String>` of IDs in `SharedPreferences`. This simplifies ownership to a binary state.
- `sharedPrefs.getStringSet("owned_modules", emptySet())`

### R2: Centralize Reward Handling
Extend `ProgressionManager` to handle module granting. This ensures that all permanent account changes (Score, Rockets, Achievements, Modules) go through one manager.

### R3: Derive Presentation State
- **LOCKED**: Not in `ownedModules`.
- **UNLOCKED**: In `ownedModules`.
- **EQUIPPABLE**: In `ownedModules` AND matches current slot type (if slots become typed).

### R4: Auto-Unlock Evaluation
`ProgressionManager.checkUnlocks()` should iterate through the `ModuleRegistry` and check `UnlockRequirement` for any module not yet in the owned set.

### R5: Notification Pipeline
Add a hook to `ProgressionManager` to trigger an "UNLOCK CELEBRATION" overlay or notification when a module is added to the owned set.
