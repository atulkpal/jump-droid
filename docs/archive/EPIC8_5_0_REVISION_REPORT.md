# Revision Report: Sprint 8.5.0 Baseline Insertion

**Date:** 2026-06-23
**Request:** Insert Sprint 8.5.0 — Baseline Capture before Sprint 8.5.1
**Scope change:** Addition only. No existing sprint removed or renumbered.

---

## Changes Made

### 1. `docs/roadmap/EPIC_8_5_DECOMPOSITION_PLAN.md`
- Added Sprint 8.5.0 section with 6-line item table (tag, build verify, file metrics, arch metrics, screenshots, baseline report)
- Added baseline screenshot risk to risk table
- Added `epic8.5-baseline` tag precondition to dependencies table
- Added baseline tag success criterion (#1)
- Updated estimated order diagram: 8.5.0 inserted at top, all downstream numbers unchanged
- Updated total sessions: 12-17 → 13-18
- Updated dependency chain: `8.5.0 → 8.5.1 → ...`

### 2. `docs/REPORTS/EPIC8_5_PLANNING_REPORT.md`
- Added 8.5.0 row to sprint structure table
- Updated total: "12-17 sessions, 8 sprints" → "13-18 sessions, 9 sprints"
- Added key decision #6: Sprint 8.5.0 establishes known-good baseline
- Updated Next Steps: 8.5.1 → 8.5.0

### 3. `docs/JumpDroid_EPIC_Tracker.md`
- Inserted `Sprint 8.5.0` as first unchecked item in EPIC 8.5 checklist

### 4. `docs/CHANGELOG.md`
- Updated "8-sprint roadmap" → "9-sprint roadmap (8.5.0–8.5.8)"
- Updated "8-sprint structure" → "9-sprint structure (8.5.0–8.5.8)"
- Added revision note documenting the request and all changes

### 5. `docs/INVENTORY.md`
- Added `docs/analysis/EPIC8_5_BASELINE_REPORT.md` as planned deliverable

### 6. `AGENTS.md`
- No changes needed — AGENTS.md references final outcomes (GameScreen < 1,800 lines, etc.) and sprint targets (8.5.5, 8.5.6), none of which changed

---

## Sprint 8.5.0 Scope (as added)

| Item | Deliverable |
|------|-------------|
| Baseline tag strategy | `epic8.5-baseline` tag at current HEAD |
| Build verification | `./gradlew assembleDebug` — BUILD SUCCESSFUL |
| File size inventory | Line counts for all key files |
| Architecture metrics | Imports, function counts, callback parameters per file |
| Screenshot baseline | All screens → `docs/screenshots/EPIC8_BASELINE/` |
| Baseline report | `docs/analysis/EPIC8_5_BASELINE_REPORT.md` |

**Exit criterion:** All baseline data captured, tagged, committed. Zero production code modifications.

---

## Sprint Numbering Verification

| Old | New | Change |
|-----|-----|--------|
| — | 8.5.0 | **Added** — Baseline Capture |
| 8.5.1 | 8.5.1 | Unchanged — Low-Risk Cleanup |
| 8.5.2 | 8.5.2 | Unchanged — HUD Decomposition |
| 8.5.3 | 8.5.3 | Unchanged — Notification & Celebration |
| 8.5.4 | 8.5.4 | Unchanged — Threat Rendering Extraction |
| 8.5.5 | 8.5.5 | Unchanged — Game Engine Boundary |
| 8.5.6 | 8.5.6 | Unchanged — ActiveThreat Strategies |
| 8.5.7 | 8.5.7 | Unchanged — Progression Decomposition |
| 8.5.8 | 8.5.8 | **Preserved per user request** — Navigation Migration |

All 9 sprints fit within EPIC 8.5. No scope removed or deferred.
