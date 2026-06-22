# Jump Droid — Authoritative Agent Manual

**Last Updated:** 2026-06-22
**Project Status:** EPIC 8 Recovery Stable
**Current Stable Tag:** `epic8-recovery-stable`
**Commit:** `afbc562`

---

## 1. Project Overview

### Game Vision
Jump Droid is an advanced vertical exploration simulator built with Jetpack Compose. It focuses on precision propulsion, modular ship building, and high-intensity atmospheric discovery. The player controls a droid-piloted rocket ascending through increasingly hostile atmospheric layers to reach "The Void."

### Core Design Philosophy
*   **Physics-First**: Movement and collision are the heart of the experience. The loop uses 4 sub-steps per frame for absolute reliability.
*   **Modular Progression**: The rocket is not a vehicle; it is a "Build." Players customize their experience via Classes and Modules.
*   **Tactical HUD**: Critical information (Fuel, Heat, Shield, Hull) is prioritized. Clutter is aggressively minimized.

### Monetization Philosophy
*   **Ethical Revenue**: No pay-to-win. No purchasable power or progression. No mission skips.
*   **Cosmetic Focus**: Revenue is driven by high-quality rocket skins, engine trails, and UI themes.
*   **Supportive Ads**: Optional rewarded continues and non-obstructive menu banners only.

### Player Experience Philosophy
*   **The "Flow" State**: Smooth transitions between platforms and rewards.
*   **Mystery & Discovery**: Hidden signals and cryptic lore rewards encourage deep exploration.
*   **Hard but Fair**: High difficulty in upper zones is balanced by robust recovery mechanics (Fuel/Shield drops).

---

## 2. Current Project State

*   **Current Branch**: `epic8-mission-migration`
*   **Current Stable Tag**: `epic8-recovery-stable`
*   **Stable Commit**: `afbc562`
*   **Current EPIC**: EPIC 8 — Missions & Progression
*   **Current Status**: **STABILIZED**. The mission architecture has been recovered from legacy prototypes and unified with the production engine.
*   **Known Issues**: 
    *   Hangar mission counter requires one final sync fix with the new Set-based logic.
    *   Visual noise during high-combo streaks (excessive floating text).
*   **Current Priorities**: Documentation closure and institutional memory preservation.
*   **Next Planned Work**: EPIC 8 Phase 6 (Hidden Signals & Dynamic Unlocks).

---

## 3. Project Timeline

*   **EPIC 4: The Ascension Program**: Established core mission tree, achievement tracking, and the Codex system.
*   **EPIC 5: Survival Protocol**: Introduced the Shield/Hull health model, environmental hazards, and the catastrophic destruction sequence.
*   **EPIC 6: Hostile Skies**: Framework for advanced AI entities, projectile systems, and multi-phase Boss encounters.
*   **EPIC 7: Rocket Evolution**: Transformed the rocket into a customizable "Build" with Rocket Classes and a 17-module Registry.
*   **EPIC 8: Missions & Progression (Recovery)**: Reconciled legacy prototype data with the production engine. Implemented the **Intelligence Network** for real-time stat tracking.

---

## 4. Major Milestones

### **EPIC 7 Complete**
*   **Tag**: `epic7-complete`
*   **Commit**: `575ee89`
*   **Outcome**: Rocket evolution finalized. Modifier Layer pattern established for module hooks.

### **EPIC 8 Recovery Sprint**
*   **Tag**: `epic8-recovery-stable`
*   **Commit**: `afbc562`
*   **Outcome**: Mission architecture recovered. 12-track dashboard established. Protocol screen separated. HUD simplified. Governance introduced.

---

## 5. Major Decisions

| Decision | Reason | Consequences |
| :--- | :--- | :--- |
| **12-Track Mission System** | 48 individual cards were overwhelming. | Cards now "evolve" through tiers (Rookie->God) in a single slot. |
| **Protocol Screen Separation** | Lore and credits cluttered the objective-focused Mission Log. | Lore moved to `AboutScreen.kt` ("SYSTEM PROTOCOL"). |
| **No Pay-to-Win** | Preserve skill-based vertical exploration integrity. | All power modules must be earned via gameplay/missions. |
| **Mission Dashboard** | Provide high-level summary (Completion %, Claims). | Faster UX for checking account status. |
| **SharedPreferences Unification** | DataStore transition in EPIC 8 prototype caused sync debt. | Reverted to `SharedPreferences` as the Single Source of Truth. |
| **Combo Ring Top-Left** | Avoid overlap with system UI and Pause/Help buttons. | Cleanest possible HUD for high-speed flight. |

---

## 6. Master Documentation Index

| System | Primary Document | Supporting Documents |
| :--- | :--- | :--- |
| **Governance** | `AGENTS.md` | `docs/REPORTS/Doc_Migration.md` |
| **History** | `docs/CHANGELOG.md` | `docs/analysis/MISSION_RECOVERY_AUDIT.md` |
| **Architecture** | `docs/ARCHITECTURE.md` | `docs/architecture/Refactor_T4_Report.md` |
| **Mission System** | `docs/analysis/MISSION_ARCHITECTURE_AUDIT.md` | `docs/roadmap/EPIC_8_MIGRATION_PLAN.md` |
| **Progression** | `docs/analysis/Progression_Architecture_Audit.md` | `ProgressionManager.kt` |
| **Threat System** | `docs/gameplay/THREATS.md` | `docs/THREAT_MASTER_TABLE.md` |
| **Rocket System** | `docs/design/ROCKET_LIBRARY.md` | `docs/analysis/Rocket_Class_Audit.md` |
| **UI / UX** | `docs/analysis/EPIC7_VISUAL_REGRESSION_AUDIT.md` | `docs/analysis/Phase_8_5_Report.md` |
| **Monetization** | `docs/roadmap/MONETIZATION_VISION.md` | (Strategy Only) |
| **Assets** | `docs/analysis/Asset_Integration_Audit.md` | `docs/analysis/Asset_Readiness_Report.md` |
| **Roadmaps** | `docs/JumpDroid_EPIC_Tracker.md` | `docs/roadmap/EPIC_8_TRACKER.md` |

---

## 7. Documentation Map
`AGENTS.md` (Authority)
↓
`docs/JumpDroid_EPIC_Tracker.md` (High-level Status)
↓
`docs/analysis/` (Technical Context & Rationale)
↓
`docs/design/` (Gameplay Content Specs)

---

## 8. Operational Standards

### Workflow: Maker / Checker
*   **Maker**: Implements logic or UI changes based on vision.
*   **Checker**: Validates against the constitutional design, audits for regressions.

### Verification Preferences
1.  **Gradle Build**: Mandatory after any structural or dependency change.
2.  **Runtime Verification**: Mandatory for all logic. Manually launch and navigate to verify.
3.  **Visual Validation**: Mandatory for UI. Provide **BEFORE** and **AFTER** screenshots saved to `docs/screenshots/`.

### Documentation Preservation Policy
*   **No Silent Deletions**: Consolidation is encouraged; removal of knowledge is prohibited.
*   **Map Before Move**: Document the migration of any information in the `Migration Report`.

---

## 9. Conflict Reporting
If a user request conflicts with the **Project Vision**, **Approved Architecture**, or **Governance Rules**:
1.  **Stop** implementation immediately.
2.  Produce a **Conflict Report** explaining the mismatch.
3.  Request explicit approval before proceeding.

---

## 10. Agent File Policy
Agent-specific files (e.g. `agent-opencode.md`) are **OPTIONAL**. They may supplement this manual but cannot override the rules established here.

---

## 11. Onboarding Flow
1.  Read **`AGENTS.md`** (this file) for constitution and memory.
2.  Consult **`docs/JumpDroid_EPIC_Tracker.md`** for current milestone status.
3.  Review **`docs/INVENTORY.md`** to locate relevant technical specs.
4.  Execute a **`gradle_build`** to ensure a stable environment.
