# EPIC 10 — Sprint 10.4: Library Threats & Entities (Implementation Report)

**Status:** COMPLETE ✅  
**Traceability:** 100% (Aligned with `THREAT_LIBRARY.md`)  
**Date:** 2026-06-25  

---

#### **1. Core Entity Implementation**

| Threat ID | Name | Type | Mechanics | Library Origin |
| :--- | :--- | :--- | :--- | :--- |
| `HAZ_CRYO_MIST` | **Cryo-Mist** | Hazard | **Thermal Lock.** Strongly cools the rocket upon proximity, effectively freezing or resetting the heat bar. | APPROVED (Heat Freeze) |
| `HAZ_MIRROR_SHARDS` | **Mirror Shards** | Hazard | **Axis Inversion.** Inverts the horizontal control vector while the player is within the effect radius. | APPROVED (Axis Invert) |
| `HAZ_GRAVITY_SHEAR`| **Gravity Shear** | Hazard | **Split Force.** Pushes the player UP if above center, and pulls DOWN if below center. | APPROVED (Split Force) |
| `ENT_HEAT_BAT` | **Heat Bat** | Enemy | **Heat Trigger.** Drifts normally but switches to a high-speed "Dive" state when player Heat ≥ 70%. | APPROVED (Heat Dive) |
| `ENT_VOID_HARVESTER`| **Void Harvester** | Enemy | **Resource Predator.** Actively pursues on-screen Power-Ups and "consumes" them to gain health. | APPROVED (Eats Power-Ups) |
| `ENT_PHASE_WRAITH` | **Phase Wraith** | Enemy | **Phase Shift.** Materializes and becomes damageable ONLY when the player is in an "Overheated" state. | APPROVED (Overheat Only) |
| `ENT_GRAVITY_RAM` | **Gravity Ram** | Enemy | **Kinetic Strike.** Executes a heavy, telegraphed vertical charge that deals massive knockback damage. | APPROVED (Telegraphed Charge) |

---

#### **2. Visual & Rendering Systems**

Created and registered 7 specialized renderer files in `ThreatRendererRegistry.kt`:
*   **`CryoMistRenderer`**: Pulsing cyan radial gradient with crystalline sparkles.
*   **`MirrorShardsRenderer`**: Rotating geometric triangles with high-specular white outlines.
*   **`GravityShearRenderer`**: Dynamic arrow indicators showing force direction (Up/Down) relative to the shear line.
*   **`HeatBatRenderer`**: Flapping humanoid/bat silhouette with eyes that glow red when in pursuit.
*   **`VoidHarvesterRenderer`**: Mechanical squid aesthetic with animated pulsing tentacles.
*   **`PhaseWraithRenderer`**: Ethereal, flickering humanoid shape that stabilizes when vulnerable.
*   **`GravityRamRenderer`**: Triangular dark-steel hull with a red engine glow and telegraphed charge-line indicators.

---

#### **3. Architectural Updates**

*   **`Models.kt`**: Added full `DiscoveryType` support for all 7 entities to ensure Codex/Archive completion is possible.
*   **`ThreatAIUpdater.kt`**: Refactored to pass the `Player` object and the list of active `PowerUps`. This enables "Self-Aware" AI (e.g., Harvesters finding items, Bats sensing heat).
*   **`ThreatInteractionProcessor.kt`**: Implemented the physical consequences of interactions (e.g., triggering `controlInversionTimer`, applying split gravity forces, and item deletion).
*   **`EncounterDirector.kt`**: Injected the new entities into the `ZoneConfig` for their respective biomes (**The Foundry**, **Chrono-Rift**, **The Beyond**, etc.).

---

#### **4. Build & Stability**
*   **Build Status**: `BUILD SUCCESSFUL` (Verified via `app:assembleDebug`).
*   **Code Quality**: Ensured all `when` expressions remained exhaustive for the new 12-zone structure.
*   **Line Budget**: Maintained `GameScreen.kt` stability by keeping all new logic in delegated managers and renderers.

---
