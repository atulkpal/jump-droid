# Refactor Sprint T2 — Phase A Report: Low-Risk Composable Extraction

## Metadata

| Field | Value |
|-------|-------|
| **Sprint** | T2 (Tidying) |
| **Phase** | A (Low Risk) |
| **Branch** | `refactor/ui-extraction` |
| **Base commit** | `686bfd0` (T1 Phase 2) |
| **Goal** | Extract remaining inline pure-Compose widgets from `GameScreen.kt` |
| **Constraints** | Zero behavioral changes; visual parity; no gameplay/state/manager modifications |
| **Actual reduction** | 147 lines (3,326 → 3,179) |

---

## Changes Made

### Files Created

| File | Composable | Lines Saved | Risk |
|------|-----------|-------------|------|
| `TopRightUtilityButtons.kt` | `TopRightUtilityButtons` | 23 | LOW |
| `MissionRow.kt` | `MissionRow` | 108 | MEDIUM |
| `FloatingTextsLayer.kt` | `FloatingTextsLayer` | 16 | LOW |

### Files Modified

| File | Change |
|------|--------|
| `HudWidgets.kt` | Appended `LeftGauges` and `RightGauges` composables (22 lines saved) |
| `GameScreen.kt` | Replaced 5 inline blocks with extracted composable calls |

### Extraction Details

**TopRightUtilityButtons** — Help (`?`) and Pause (`||`) circular buttons at top-right. Toggles visibility of pause button based on `gameState`. Receives `onHelp`/`onPause` callbacks. Uses `Modifier` parameter pattern to avoid `BoxScope` coupling.

**LeftGauges / RightGauges** — Column wrappers around existing Fuel/Heat and Shield/Integrity gauges. Previously inline with `Modifier.align(CenterStart/CenterEnd)`. Extracted with `modifier` parameter for parent positioning.

**MissionRow** — Horizontal row of mission cards (103+ lines). Each card shows: ceremony stage glow/border, mission type icon, rotating name/description via `AnimatedContent`, progress text, and thin progress bar. Supports `CeremonyStage` states: `GLOW`, `COMPLETED_TEXT`, `REWARD_SPAWNED`, `REPLACING`. The `AnimatedVisibility` wrapper and `ComboHudBar` remain inline.

**FloatingTextsLayer** — Iterates `FloatingText` list and renders each as animated `Text` with offset positioning, fading alpha, and pulsing scale for critical items. Pure rendering, no state mutations.

---

## Metrics

| Metric | Before T2A | After T2A | Delta |
|--------|-----------|-----------|-------|
| GameScreen.kt lines | 3,326 | 3,179 | **−147 (−4.4%)** |
| Total `.kt` files | 15 | 18 | +3 |
| Brace balance | ✅ | ✅ | Compiler-verified |

### Cumulative Reduction (T1 + T2A)

| Phase | Lines Removed | Remaining | % of Original (4,344) |
|-------|--------------|-----------|----------------------|
| T1 Phase 1 | 52 | 4,292 | 98.8% |
| T1 Phase 2 | 966 | 3,326 | 76.5% |
| T2A | 147 | **3,179** | **73.2%** |
| **Total** | **1,165** | **3,179** | **−26.8%** |

---

## Validation

| Check | Result |
|-------|--------|
| `./gradlew assembleDebug` | **BUILD SUCCESSFUL** (11s, 0 errors) |
| ADB install | **Success** |
| Emulator launch | **Events injected: 1** |
| Brace balance | ✅ Kotlin compiler accepted all brace pairs |

---

## Remaining Inline Code

| Item | Lines | Status |
|------|-------|--------|
| Canvas effects (8 items) | ~104 | T2B — planned |
| Threat entity rendering | ~826 | Deferred indefinitely |

## Notes

- The `Modifier` parameter pattern was adopted for `TopRightUtilityButtons`, `LeftGauges`, and `RightGauges` to avoid requiring `BoxScope` receiver in the extracted composables. The caller passes `Modifier.align(...)` from the parent `Box` scope.
- `FloatingTextsLayer` tracks the full list of active floating texts; the game loop still manages text lifecycle (drift, fade, removal) — only the rendering was extracted.
- No extracted composable modifies game state or manager objects.
