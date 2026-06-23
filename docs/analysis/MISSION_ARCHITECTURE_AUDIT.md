# Mission Architecture Audit — EPIC 8

**Date:** 2026-06-22
**Status:** Audit Complete
**Objective:** Reconcile legacy mission content with the new production architecture.

---

## 1. Inventory Summary

### Total Count
*   **Total Missions**: 50 (43 Public + 7 Hidden)
*   **Total Public Tracks**: 14 logic-based categories.
*   **Target Player-Facing Tracks**: 12 unified progression verticals.

### Logic Categories (The "How")
The current implementation utilizes 14 logic categories to track specific gameplay behaviors:
1.  **FLIGHT_TIME**: Continuous airborne duration.
2.  **PLATFORM_STAY**: Total time spent on surfaces.
3.  **NO_HEAT**: Duration without engine overheat.
4.  **FUEL_EFFICIENCY**: Recovery of fuel canisters.
5.  **COMBO_STREAK**: Reaching specific multiplier milestones.
6.  **BOSS_SLAYER**: Destruction of major threats.
7.  **DISCOVERY_HUNTER**: Completion of Codex entries.
8.  **ALTITUDE_CLIMBER**: Vertical progression milestones.
9.  **MOMENTUM_MASTER**: Speed/Velocity retention.
10. **HAZARD_SURVIVOR**: Withstanding environmental impacts.
11. **PERFECT_RUN**: Avoiding all damage for a set duration.
12. **COLLECTOR**: Gathering permanent artifacts.
13. **BOOST_CHAMPION**: Strategic dash usage.
14. **COMBO_PRO**: Maintaining high combos over time.

---

## 2. Progression Track Mapping (The "What")
To meet the design vision of 12 player-facing tracks, we will merge overlapping logic categories into single evolving cards.

| Progression Track | Tier Chain | Related Logic Categories |
| :--- | :--- | :--- |
| **Aeronautics** | Rookie -> God | FLIGHT_TIME, NO_HEAT |
| **Ground Support** | Rookie -> God | PLATFORM_STAY |
| **Resource Management**| Rookie -> God | FUEL_EFFICIENCY |
| **Combo Mastery** | Rookie -> God | COMBO_STREAK, COMBO_PRO |
| **Elite Combat** | Rookie -> Master | BOSS_SLAYER |
| **Surveying** | Rookie -> Master | DISCOVERY_HUNTER |
| **Ascension Path** | Rookie -> God | ALTITUDE_CLIMBER |
| **Kinetic Control** | Rookie -> Master | MOMENTUM_MASTER, BOOST_CHAMPION |
| **Reinforcement** | Rookie -> Master | HAZARD_SURVIVOR |
| **Precision Flight** | Rookie -> Master | PERFECT_RUN |
| **Archeology** | Rookie -> Master | COLLECTOR |
| **Hidden Ops** | Standalone | (All Hidden IDs) |

---

## 3. Tier Chain Analysis
*   **Chain Structure**: Most tracks advance through 3-4 Tiers (Rookie, Experienced, Master, God).
*   **Evolving Card Logic**: The current architecture creates 4 separate objects for a chain. To support the "One Card Evolving" vision, the Mission Manager will be updated to only present the *lowest uncompleted/unclaimed* mission in a category to the UI.

---

## 4. Discrepancy Reconciliation
The mismatch between "12 Tracks" and "48 Missions" is intentional:
*   **Tracks** are the UI Containers.
*   **Missions** are the Content.
*   **Categories** are the Logic.

**Conclusion**: 12 Tracks + 14 Categories + 50 Missions can perfectly coexist. The Track serves as the UI grouping for the Categories, which in turn evaluate the individual Tiered Missions.
