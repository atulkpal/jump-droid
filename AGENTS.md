# Jump Droid — Authoritative Agent Manual

**Last Updated:** 2026-07-02
**Project Status:** Release Polish — Phases 1–8 Complete ✅
**Current Stable Tag:** `release-candidate-1`
**Branch:** `refactor/cleanup`
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
*   **Premium Purchase**: Google Play Store only. Fallback dialog tells user to download from Play Store — no direct `confirmPurchase()` from UI.

### Player Experience Philosophy
*   **The "Flow" State**: Smooth transitions between platforms and rewards.
*   **Mystery & Discovery**: Hidden signals and cryptic lore rewards encourage deep exploration.
*   **Hard but Fair**: High difficulty in upper zones is balanced by robust recovery mechanics (Fuel/Shield drops).

---

## 2. Current Project State

*   **Current Branch**: `refactor/cleanup`
*   **Current Stable Tag**: `epic11-complete`
*   **Current EPIC**: Release Polish (pre-ship sprint)
*   **Current Status**: **RELEASE POLISH COMPLETE**. Phases 1–7 complete. Remaining: performance profiling, store listing, final APK. See `docs/roadmap/RELEASE_POLISH_PLAN.md`.
*   **Active Sprint**: None — ready for Phase 7 Final Build.
*   **Completed Work**:
    *   Phase 1 — Core Game Feel (power-up spawns, combo rewards, boss deaths, mission celebrations, rewarded continue UX)
    *   Phase 2 — Notification Architecture (3-tier priority, unified area, weak notification removal, ad placeholder UI)
    *   Phase 3 — Tutorial Removal + Discovery (tutorial pop-ups removed, learning moved to archives, platform discoveries, unlock celebrations)
    *   Phase 4 — Data Archives + Monetization (archives redesign, AdMob SDK integration, GlobalAdBanner with production ads, RewardedAd helper, PurchaseManager BillingClient rewrite, ShopScreen, cash tracking)
    *   Phase 5 — Audio Pass & Haptics (Production assets loaded, volume normalization, 12-zone BGM mapping, material-based landing SFX, boosted haptics)
    *   Phase 6 — Data Archives Detail View (Deep lore registry, animated entity previews, glitch-noise detail popups for all 100+ entities)
    *   Phase 7 — Bug Bash + Polish (player hitbox collision radius, per-WP `wpDestroyedMask` bitmask tracking, WP hitbox positions aligned to visuals across 11 bosses, Signal ghost platform capping, Gravity Anchor pull safe window, Lightning dissolve effect, alarm loop fix all 3 death paths, shield hit sound for bypass damage, Heat Bat aura, Lightning 25→13, boss HP/WP scaling by difficultyMultiplier, wpInvulnerabilityTimer separation, WP cooldown 0.25s/hit radius 45f, Cloud Zone purple-blue storm clouds, Earth Zone golden hour gradient, Shield Platform restore, Conveyor velocity fix, Zone jump freeze fix, Zone change notification, multi-hit WPs, Heat Bat visibility/damage rebalance)
    *   Phase 7 — Billing + Shop (PurchaseManager BillingClient rewrite, ShopScreen new, ProgressionManager cash tracking, SettingsScreen premium no-toggle-off + data reset buttons, MainActivity/MainMenu shop route + button, AboutScreen V1.5.0 update, Play Store purchase gating, dev menu gated on BuildConfig.DEBUG)
    *   Phase 7 — Boss Density & Score Integrity (score removal from boss kills, milestone threshold rebalance, one-boss-per-frame guard, no boss while boss alive, boss recurrence system, MINI_BOSS music fix, hazard suppression 0.3f→0.1f, Solar Flare filter during bosses, ThreatRegistry.getEntries())
    *   Phase 8 — Firebase Integration & Quality Assurance (Firebase Analytics & Crashlytics integrated, debug-gated Crashlytics test action added to Dev Menu, domain-driven GameAnalytics layer implemented, AdMob hardening with AdConfig centralized IDs and debug/release gating, high-value instrumentation complete)
*   **Current Priorities**: Performance profiling, Play Store listing prep, final APK build.
*   **Next Planned Work**: Performance profiling (frame drops in upper zones / dense threat fields), Play Store listing (screenshots, description, assets), final APK build + testing.

---

## 3. Project Timeline

*   **EPIC 8.5 Architecture Decomposition**: Structural sprint decomposed `GameScreen.kt` (3,901→1,900 lines) and `ActiveThreat.kt` (1,224→123 lines) God Objects. Game engine, threat rendering, threat AI, collision system, ProgressionManager, and HUD components extracted. Aligned mission logic with lifetime statistics. **Note: Deferred decomposition of GameScreen.kt resumed post-EPIC 11.**
*   **EPIC 11: Ascension**: Finalized the vertical journey, meta-boss, and origin reset mechanics.

---

## 4. Major Milestones
...
### **EPIC 8.5 Refactor Execution**
*   **Tracking**: `docs/roadmap/EPIC_8_5_REFACTOR_EXECUTION_TRACKER.md`
*   **Status**: Active Refactor phase.

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
| **Governance** | `AGENTS.md` | `docs/REPORTS/Documentation_Migration_Report.md` |
| **History** | `docs/CHANGELOG.md` | `docs/analysis/EPIC8_UNIFIED_RECOVERY_REPORT.md` |
| **Architecture** | `docs/ARCHITECTURE.md` | `docs/architecture/Refactor_T4_Report.md` |
| **Mission System** | `docs/analysis/MISSION_ARCHITECTURE_AUDIT.md` | `docs/roadmap/EPIC_8_TRACKER.md` |
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
| **Release Polish** | `docs/roadmap/RELEASE_POLISH_PLAN.md` | `docs/roadmap/MONETIZATION_VISION.md` |
| **EPIC 8.5 Planning** | `docs/REPORTS/EPIC8_5_PLANNING_REPORT.md` | (Sprint Planning) |

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

## 12. Design Library First Rule

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
