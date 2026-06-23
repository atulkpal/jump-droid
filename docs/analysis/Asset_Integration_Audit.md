# Asset Integration Audit — EPIC 7 Completion

**Date:** 2026-06-22
**Project:** Jump Droid
**Epic:** 7 — Rocket Evolution
**Objective:** Prepare the codebase for the integration of professional visual assets.

---

## 1. Rocket Class Icons

*   **Current Implementation**: Procedural drawing via `Canvas` in `HangarScreen.kt`.
*   **Current Placeholder Visuals**: A generic round-rect body with a triangle nose and engine flare. Identical for all classes (Explorer, Striker, Heavy, Prototype).
*   **Target Asset Dimensions**: 48x48 dp or 64x64 dp.
*   **Required File Format**: Vector (XML) preferred; high-res PNG (3x/4x) acceptable.
*   **Android Resource Location**: `app/src/main/res/drawable/`
*   **Integration Complexity**: **Low**. Requires replacing the `Canvas` block in the `RocketType` loop with an `Image` or `Icon` component.

---

## 2. Module Category Icons

*   **Current Implementation**: Single-character `Text` components within a `Box` in `LoadoutScreen.kt`.
*   **Current Placeholder Visuals**: 'H' (Hull), 'S' (Shield), 'E' (Engine), 'T' (Thermal/Heat), 'U' (Utility).
*   **Target Asset Dimensions**: 32x32 dp.
*   **Required File Format**: Vector (XML).
*   **Android Resource Location**: `app/src/main/res/drawable/`
*   **Integration Complexity**: **Low**. Replace `Text(module.category.name.take(1))` with `Icon(painterResource(...))`.

---

## 3. Artifact Icons

*   **Current Implementation**: None. Only text descriptions are used in the `ArchiveScreen.kt` and `CodexCard.kt`.
*   **Current Placeholder Visuals**: Empty space.
*   **Target Asset Dimensions**: 64x64 dp (Grid view) / 128x128 dp (Detail view).
*   **Required File Format**: high-res PNG or Vector.
*   **Android Resource Location**: `app/src/main/res/drawable/`
*   **Integration Complexity**: **Medium**. Requires updating the `CodexCard` composable to accept a drawable resource and adding an icon mapping to the `DiscoveryType` enum.

---

## 4. Threat Icons (Bestiary/Tactical)

*   **Current Implementation**: None for UI. Threats are rendered live on the game Canvas using `DrawScope` in `ActiveThreat.kt`.
*   **Current Placeholder Visuals**: None.
*   **Target Asset Dimensions**: 48x48 dp.
*   **Required File Format**: Vector (XML) silhouettes or high-res PNG.
*   **Android Resource Location**: `app/src/main/res/drawable/`
*   **Integration Complexity**: **Medium**. Supports the "Threat Scanner" module functionality. Requires a mapping from `ThreatDefinition` to a UI icon.

---

## 5. Boss Portraits

*   **Current Implementation**: None.
*   **Current Placeholder Visuals**: None.
*   **Target Asset Dimensions**: 128x128 dp or 256x256 dp.
*   **Required File Format**: high-res PNG.
*   **Android Resource Location**: `app/src/main/res/drawable/`
*   **Integration Complexity**: **Low**. Can be added to the `ArchiveScreen` or used in "Boss Arriving" notifications to increase impact.

---

## 6. Mission Icons

*   **Current Implementation**: Emoji strings returned by `MissionType.toIcon()` in `GameScreen.kt`.
*   **Current Placeholder Visuals**: 🚀 (Exploration), 🧱 (Platforming), ❄️ (Survival), 📡 (Discovery), ⚠️ (Boss).
*   **Target Asset Dimensions**: 24x24 dp or 32x32 dp.
*   **Required File Format**: Vector (XML).
*   **Android Resource Location**: `app/src/main/res/drawable/`
*   **Integration Complexity**: **Low**. Requires changing `toIcon()` to return a resource ID (`Int`) and updating the `MissionRow` to use `Icon`.

---

## Prioritized Implementation Order

1.  **Module Category Icons**: (Sprint 7.1-7.4 Polish) Finalizes the "Build" system visual language.
2.  **Rocket Class Icons**: (Sprint 7.5 Polish) Establishes clear class identities in the Hangar.
3.  **Mission Icons**: (Preparation for EPIC 8) Essential for HUD readability and track differentiation.
4.  **Artifact Icons**: (Sprint 7.6 Polish) Validates the value of exploration and collection.
5.  **Threat Icons**: (Utility System Expansion) Required for the tactical feel of the "Threat Scanner".
6.  **Boss Portraits**: (Lore/Archive Expansion) Final aesthetic layer for major encounters.

---

## Final Goal Alignment
The project is architecturally ready to receive these assets. No core logic changes are required for integration; most updates are restricted to the UI/View layer and enum metadata.
