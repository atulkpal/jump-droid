# Refactor Sprint T2 — Phase B Report: Canvas Effects Extraction

## Metadata

| Field | Value |
|-------|-------|
| **Sprint** | T2 (Tidying) |
| **Phase** | B (Medium Risk) |
| **Branch** | `refactor/ui-extraction` |
| **Base commit** | `2fe24f1` (T2A) |
| **Goal** | Extract remaining inline Canvas draw calls from `GameScreen.kt` into `DrawScope` extension functions |
| **Constraints** | Zero behavioral changes; visual parity; no gameplay/state/manager modifications |
| **Actual reduction** | 70 lines (3,179 → 3,109) |

---

## Changes Made

### Files Created

| File | Purpose | Lines |
|------|---------|-------|
| `CanvasEffects.kt` | 8 `DrawScope` extension functions | 168 |

### Files Modified

| File | Change |
|------|--------|
| `GameScreen.kt` | 8 inline Canvas blocks replaced with function calls |

### Extractions

| Function | Description | Inline Lines | Risk |
|----------|-------------|-------------|------|
| `drawRealityDistortion` | Magenta overlay near Void Anomaly threat | 13 | MEDIUM |
| `drawSpeedLines` | Vertical white lines during fast descent | 15 | LOW |
| `drawGround` | Brown ground rectangle | 1 | LOW |
| `drawParticles` | Sparkle/circle particle rendering | 17 | LOW |
| `drawLandingEffects` | Expanding cyan ring circles | 9 | LOW |
| `drawPowerUps` | Colored shapes per powerup type | 22 | LOW |
| `drawFlyingRewards` | Animated reward items flying to player | 20 | LOW |
| `drawImpactFlash` | White stroked border screen flash | 7 | LOW |

All functions are `DrawScope` extension functions in `CanvasEffects.kt`, called directly inside the main Canvas block.

---

## Metrics

| Metric | Before T2B | After T2B | Delta |
|--------|-----------|-----------|-------|
| GameScreen.kt lines | 3,179 | 3,109 | **−70 (−2.2%)** |
| Total `.kt` files | 18 | 19 | +1 |
| Brace balance | ✅ | ✅ | Compiler-verified |

### Cumulative Reduction (T1 + T2A + T2B)

| Phase | Lines Removed | Remaining | % of Original (4,344) |
|-------|--------------|-----------|----------------------|
| T1 Phase 1 | 52 | 4,292 | 98.8% |
| T1 Phase 2 | 966 | 3,326 | 76.5% |
| T2A | 147 | 3,179 | 73.2% |
| T2B | 70 | **3,109** | **71.6%** |
| **Total** | **1,235** | **3,109** | **−28.4%** |

---

## Validation

| Check | Result |
|-------|--------|
| `./gradlew assembleDebug` | **BUILD SUCCESSFUL** (5s, 0 errors) |
| ADB install | **Success** |
| Emulator launch | **Success** (`am start` → PID running) |

---

## Remaining Inline Code

| Item | Lines | Status |
|------|-------|--------|
| Threat entity rendering | ~826 | Deferred indefinitely |

---

## Notes

- All 8 functions are `DrawScope` extension functions, which means they can call `drawRect`, `drawCircle`, `drawLine`, etc. directly without a separate `drawScope` parameter.
- `drawSpeedLines` uses `kotlin.random.Random` for per-frame random line positions — same as the original inline code.
- `drawRealityDistortion` queries the `ActiveThreat` list with an explicit lambda parameter type to resolve Kotlin type inference.
- `drawGround` uses `Constants.ROCKET_HEIGHT` imported directly from the constants object.
- `drawFlyingRewards` uses `DrawScope.scale()` as a direct receiver call (not fully-qualified) since it's inside a `DrawScope` extension function.
