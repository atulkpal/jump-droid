# Jump Droid — Authoritative Agent Manual

**Last Updated:** 2026-07-13
**Current Version:** v1.5.1 — Release Polish Update
**Project Status:** EPIC 11 — Ascension (The End) — COMPLETE ✅ — Published to GitHub
**Current Stable Tag:** `v1.5.1`
**Branch:** `master`
**Base Commit:** `HEAD`

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

*   **Current Branch**: `master`
*   **Current Stable Tag**: `v1.5.1`
*   **Current Version**: v1.5.1 — Release Polish Update
*   **Current EPIC**: EPIC 11 — Ascension (The End) — **COMPLETE ✅**
*   **Current Status**: **v1.5.1 PUBLISHED**. Release Polish Update: portrait lock, keep-screen-on during gameplay. All EPIC 11 content preserved. See `docs/releases/v1.5.1_PUBLICATION_REPORT.md`.
*   **Known Issues**: 
    *   Visual noise during high-combo streaks (excessive floating text) — non-blocking, deferred.
    *   GameScreen.kt at ~2,080 lines — extraction continued via GameEngine refactoring into `refactor/cleanup` branch.
*   **Current Priorities**: Future EPIC planning / EPIC 12.
*   **Next Planned Work**: EPIC 12 — Fleet Expansion (planned).

---

## 3. Project Timeline

*   **EPIC 4: The Ascension Program**: Established core mission tree, achievement tracking, and the Codex system.
*   **EPIC 5: Survival Protocol**: Introduced the Shield/Hull health model, environmental hazards, and the catastrophic destruction sequence.
*   **EPIC 6: Hostile Skies**: Framework for advanced AI entities, projectile systems, and multi-phase Boss encounters.
*   **EPIC 7: Rocket Evolution**: Transformed the rocket into a customizable "Build" with Rocket Classes and a 17-module Registry.
*   **EPIC 8: Missions & Progression (Recovery)**: Reconciled legacy prototype data with the production engine. Implemented the **Intelligence Network** for real-time stat tracking.
*   **EPIC 8.5: Architecture Decomposition**: Structural sprint decomposed `GameScreen.kt` (3,901→2,011 lines) and `ActiveThreat.kt` (1,224→123 lines) God Objects. Game engine, threat rendering, threat AI, collision system, ProgressionManager, and HUD components extracted. Aligned mission logic with lifetime statistics.
*   **EPIC 9: Hidden Signals & Dynamic Unlocks**: Secret missions, dynamic unlock engine (AND/OR), artifact set bonuses, lore logs, blueprints.
*   **EPIC 10: The Outer Reaches**: 4 new zones, Flux/Graviton platforms, Foundry & Chrono-Rift, 12 new threats, 3 power-ups, 5 library boss encounters, full visual fidelity upgrade.
*   **EPIC 11: Ascension (The End)**: The Singularity meta-boss, origin reset, ascension ceremony, prestige system, Omega Modules, Eternal Mode, PlayerInputProcessor extraction, GameEngine state container, SoundManager audio system, comprehensive bug fixes.

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

### **EPIC 8.5 Architecture Decomposition**
*   **Tag**: `epic8.5-structured`
*   **Commit**: `9363434`
*   **Outcome**: GameScreen.kt reduced from 3,901 to 2,011 lines (48% reduction). ActiveThreat.kt reduced from 1,224 to 123 lines (90% reduction). 26 threat renderers extracted. Mission system purified: `MissionRow.kt` deleted, `isNew` removed, `CeremonyStage` collapsed, `checkCompletion()` made pure. HUD components extracted: `StarfieldBackground`, `GaugeBar`, `HudContext`. ProgressionService interface decouples MissionManager from ProgressionManager. GameEngine state container created.

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
| **EPIC 8 Stabilization** | `docs/REPORTS/EPIC8_STABILIZATION_REPORT.md` | `docs/REPORTS/EPIC8_TECHNICAL_QA_REPORT.md` |
| **EPIC 8 Polish** | `docs/REPORTS/EPIC8_POLISH_SPRINT_REPORT.md` | `docs/screenshots/EPIC8_POLISH/` |
| **EPIC 8 Validation** | `docs/REPORTS/EPIC8_VALIDATION_REPORT.md` | `docs/analysis/EPIC8_MISSION_EVENT_AUDIT.md` |
| **EPIC 8.5 Decomposition** | `docs/roadmap/EPIC_8_5_DECOMPOSITION_PLAN.md` | `docs/analysis/EPIC8_TECH_DEBT_AUDIT.md` |
| **Tech Debt Audit** | `docs/analysis/EPIC8_TECH_DEBT_AUDIT.md` | `docs/roadmap/EPIC_8_5_DECOMPOSITION_PLAN.md` |
| **EPIC 8.5 Planning** | `docs/REPORTS/EPIC8_5_PLANNING_REPORT.md` | (Sprint Planning) |
| **EPIC 8.5 Decomposition** | `docs/roadmap/EPIC_8_5_DECOMPOSITION_PLAN.md` | `docs/analysis/EPIC8_TECH_DEBT_AUDIT.md` |

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

---

## 13. Signing & Release Security

### Credential Resolution Order
The release signing config in `app/build.gradle.kts` resolves credentials in this priority:
1. **Environment variables**: `STORE_FILE`, `STORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`
2. **`keystore.properties`** (file fallback, gitignored)

### Git Protection
| Asset | Ignored By | Tracked? |
|---|---|---|
| `app/jump_droid_release.keystore` | `*jump_droid_release.keystore` | ❌ |
| `keystore.properties` | `keystore.properties` | ❌ |
| `keystore.properties.example` | N/A (template only) | ✅ |
| `.gradle/` (config cache) | `.gradle` | ❌ |
| `app/build/` | `app/debug/`, `app/release/` | ❌ |

### Hardcoded Credentials
None. All credential lookups go through environment variables or file I/O. The repository is safe to make public.

---

## 14. Design Library First Rule

**All gameplay content MUST originate from the Design Libraries in `docs/design/`.**

### The Rule
Before implementing ANY new:
- Threat (hazard/entity/boss)
- Platform type
- Power-Up
- Altitude Zone
- Lore entry
- Achievement
- Artifact

**STOP. Check the relevant Library file first.**

| Content Type | Library File |
|---|---|
| Threats (hazards, enemies, bosses) | `docs/design/THREAT_LIBRARY.md` |
| Platforms | `docs/design/PLATFORM_LIBRARY.md` |
| Power-Ups | `docs/design/POWERUP_LIBRARY.md` |
| Zones | `docs/design/AREA_LIBRARY.md` |
| Lore | `docs/design/LORE_LIBRARY.md` |
| Artifacts | `docs/design/ARTIFACT_LIBRARY.md` |
| Achievements | `docs/design/ACHIEVEMENT_LIBRARY.md` |
| Rockets | `docs/design/ROCKET_LIBRARY.md` |

### Priority Order
1. **APPROVED** items → Implement as-is (highest priority)
2. **BACKLOG** items → May implement with rationale
3. **REJECTED** items → Do not implement
4. **New invention** → Only if no Library entry exists AND a written rationale is provided AND approved

### If You Must Invent
If no existing Library entry covers the needed design:
1. Write a **Rationale Document** explaining why existing entries are insufficient
2. Trace the closest Library concept and explain the evolution
3. Submit for approval before writing any code

**Violations:** Any agent or engineer implementing content without library traceability will have their changes reverted and must re-submit with proper documentation.
