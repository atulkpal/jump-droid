# Implementation Plan - Procedural Layer 1 & Asset-Based Parallax

Refine the parallax system to use procedural generation for the base layer (Layer 1) while utilizing assets for Layers 2-6.

## User Review Required

> [!IMPORTANT]
> I will implement Layer 1 as the existing procedural gradient. Layers 2-6 will be loaded from assets.
> I will clear the existing procedural layers (silhouettes, clouds) when Asset Mode is enabled to prevent visual overlapping with the new asset pack.

## Proposed Changes

### [Component] Background Rendering

#### [MODIFY] [ParallaxSystem.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/ParallaxSystem.kt)
- Add `clearLayers(zone: AltitudeZone)` to `ParallaxManager` to allow switching between procedural and asset-based sets.

#### [MODIFY] [ZoneBackgroundRenderer.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/ZoneBackgroundRenderer.kt)
- Update `ensureAssetLayers` to:
    - Skip Layer 1 (index 0) during asset registration.
    - Call `parallaxManager.clearLayers(zone)` before adding asset layers to ensure a clean state for the new pack.
    - Adjust `zIndex` for asset layers (Layer 2 starts at `zIndex = -19`).

## Verification Plan

### Manual Verification
- Launch the game in Asset Mode.
- Verify that the background starts with the procedural gradient.
- Verify that Layers 2-6 (Mountains, Islands, etc.) appear correctly on top of the gradient.
- Verify that previous procedural silhouettes (like the black mountains from the default Earth setup) are gone when assets are loaded.
