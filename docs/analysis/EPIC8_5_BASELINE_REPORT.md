# EPIC 8.5 Baseline Report

**Date:** 2026-06-23
**Tag:** `epic8.5-baseline` (commit `8b55b6c`)
**Branch:** `epic8.5`
**Build:** `./gradlew assembleDebug` — BUILD SUCCESSFUL (33s, 12 executed)

---

## File Size Inventory

| File | Lines | Risk |
|------|-------|------|
| GameScreen.kt | 3,577 | 🔴 CRITICAL |
| ActiveThreat.kt | 1,183 | 🔴 CRITICAL |
| HudWidgets.kt | 651 | 🟡 Moderate |
| ProgressionManager.kt | 229 | 🟡 Moderate |
| EncounterDirector.kt | 227 | 🟡 Moderate |
| ComboManager.kt | 115 | 🟢 Low |
| MissionManager.kt | 112 | 🟢 Low |
| MissionRow.kt | 133 | 🔵 DEAD CODE |
| ModuleRegistry.kt | 419 | 🟢 Low |
| ZoneBackgroundRenderer.kt | 558 | 🟢 Low |
| CanvasEffects.kt | 483 | 🟢 Low |
| TitleScreen.kt | 364 | 🟢 Low |
| PauseOverlay.kt | 211 | 🟢 Low |
| SurvivalManager.kt | 99 | 🟢 Low |
| ThreatManager.kt | 56 | 🟢 Low |

## Codebase Profile

| Metric | Value |
|--------|-------|
| Total Kotlin files | 66 |
| Total lines of code | 12,795 |
| Median file size | 68 |
| Top-3-file % of codebase | 42% (3,577 + 1,183 + 651 = 5,411 / 12,795) |

## Import Counts

| File | Imports |
|------|---------|
| GameScreen.kt | 70 |
| HudWidgets.kt | 60 |
| ActiveThreat.kt | 9 |
| ProgressionManager.kt | 10 |

## Function Counts

| File | Functions |
|------|-----------|
| GameScreen.kt | 16 (15 local + 1 top-level) |
| ActiveThreat.kt | 2 (update + processInteraction) |
| ProgressionManager.kt | 10 |

## Key Architecture Metrics

| Metric | Line/Value |
|--------|-----------|
| Game loop inline (withFrameNanos) | L722–1622 (~900 lines) |
| Canvas rendering inline | L1623–3700 (~2,077 lines) |
| Threat rendering (inline in Canvas, est.) | ~1,900 lines |
| `when(gameState)` branches | L3701–3907 (~207 lines, 14 states) |
| `processInteraction` callback params | 15 explicit + 2 defaulted = 17 total |
| `CeremonyStage` values | 5 (NONE, GLOW, COMPLETED_TEXT, REWARD_SPAWNED, REPLACING) |
| Actual used values | 2 (GLOW, REPLACING) |
| `remember` blocks in GameScreen | 27 |
| Dead code files | 1 (MissionRow.kt, 133 lines) |
| Starfield copy-paste instances | 6 |
| `isNew` zombie refs in Mission.kt | Present |
| ProgressionManager responsibilities | 7+ domains |

## Post-EPIC 8.5 Target Comparison

| Metric | Baseline | Target (Post-8.5) |
|--------|----------|-------------------|
| GameScreen.kt lines | 3,577 | <1,800 |
| ActiveThreat.kt lines | 1,183 | <350 |
| ProgressionManager.kt lines | 229 | <100 |
| HudWidgets.kt lines | 651 | <450 |
| Total files | 66 | ~117 |
| Total lines | 12,795 | ~14,500 |
| Median file size | 68 | <70 |
| GameScreen imports | 70 | <40 |
| ActiveThreat callback params | 17 | ≤3 (event bus) |
| Inline game loop (lines) | ~900 | 0 |
| Inline Canvas threat rendering (lines) | ~1,900 | 0 |
| `when(gameState)` branches | 14 | 0 |
| Starfield copy-paste | 6 | 0 |
| Dead code files | 1 | 0 |
| ProgressionManager resp. | 7+ | ≤4 |

## Validation Evidence
- `epic8.5-baseline` tag: committed at `8b55b6c`
- Build: `BUILD SUCCESSFUL in 33s` — 37 actionable tasks, 12 executed
- Working tree: clean (matches HEAD)
- Pre-existing deprecation: 1 (ArchiveScreen.kt:116 — `LinearProgressIndicator` deprecated overload)
