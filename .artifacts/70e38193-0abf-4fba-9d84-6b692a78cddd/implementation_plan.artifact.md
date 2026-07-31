# Implementation Plan: Fix Fatal Exception in `RadialGradient`

The user reported a `java.lang.IllegalArgumentException` in `android.graphics.RadialGradient.nativeCreate`, originating from `ScoutDroneRenderer.render`. Preliminary research suggests this is caused by `NaN` or non-finite values in the `center` or `radius` parameters of the `radialGradient` brush.

## Root Cause Analysis
The crash occurs at `com.ashwathai.jump_droid.ScoutDroneRenderer.render(ScoutDroneRenderer.kt:47)`.
Line 47 corresponds to:
```kotlin
brush = Brush.radialGradient(
    colors = listOf(stateColor.copy(alpha = 0.4f), Color.Transparent),
    center = Offset(tx, ty + 20f),
    radius = glowRadius
)
```
If `tx`, `ty`, or `glowRadius` are `NaN` or `Infinite`, the native `RadialGradient` creation fails.
`tx` and `ty` come from `threat.x` and `threat.y`.
Potential sources of `NaN`:
1.  **Division by zero in physics**: `GameEngine.kt` has `player.velocityX += (dx / (screenWidth / 3f))...` which triggers if `screenWidth` is 0 (uninitialized).
2.  **NaN propagation**: If `player.x/y` becomes `NaN`, any threat targeting the player or influenced by player position might also become `NaN`.

## Proposed Changes

### Core Engine Safety

#### [MODIFY] [GameEngine.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/GameEngine.kt)
- Add a guard in `handlePlayerPhysics` to ensure `screenWidth` is greater than 0 before performing division.
- Ensure `dt` is positive and finite.

#### [MODIFY] [ThreatAIUpdater.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/ThreatAIUpdater.kt)
- Add guards when calculating distances and normalized vectors to prevent `NaN` if distance is 0.
- Ensure position updates results are finite.

### Renderer Defensiveness

#### [MODIFY] [ScoutDroneRenderer.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/ScoutDroneRenderer.kt)
- Add a helper to validate that the `Offset` and `radius` are finite before calling `drawCircle` with a `radialGradient`.
- Fallback to a simpler rendering or skip the draw call if invalid data is detected.

## Verification Plan

### Automated Tests
- Create a unit test to verify that `player.velocityX` does not become `NaN` when `screenWidth` is 0.

### Manual Verification
- Deploy and verify the app starts without crashing.
- Check that `ScoutDrone` renders correctly in various states.
