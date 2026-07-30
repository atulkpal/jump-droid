# Implementation Plan - Zen Music Refinement

This plan refines the Zen Mode music menu by grouping repeated tracks into "Albums" and adding a motivational footer to encourage exploration.

## Proposed Changes

### 1. UI Refinement: Music Album Grouping

#### [MODIFY] [GamePlayScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/GamePlayScreen.kt)
- Update `ZenMusicSelector`:
    - Define a mapping of track resource names to "Album Names".
    - Group tracks:
        - `bgm_earth`, `bgm_clouds` -> **"PLANETARY GLIDE"**
        - `bgm_atmosphere`, `bgm_orbit` -> **"STRATOSPHERIC"**
        - `bgm_foundry`, `bgm_space` -> **"INDUSTRIAL VOID"**
        - `bgm_chrono`, `bgm_void` -> **"TEMPORAL RIFT"**
        - `bgm_beyond`, `bgm_gate`, `bgm_construct` -> **"ANCIENT ECHOES"**
        - `bgm_singularity` -> **"SINGULARITY"**
    - Ensure only one entry per Album appears in the menu if any track in that album is unlocked.
    - Add a footer at the bottom of the music list: **"KEEP EXPLORING HIGHER TO UNLOCK MORE MUSIC!"** in a subtle, italicized SciFiCyan style.
    - This footer only appears if not all 12 tracks have been discovered yet.

## Verification Plan

### Automated Tests
- `gradle_build` to verify syntax.

### Manual Verification
1.  **Album Check**: Start a Zen run with Earth and Clouds unlocked. Verify only "PLANETARY GLIDE" appears (instead of two separate entries).
2.  **Motivational Footer**: Scroll to the bottom of the music menu. Verify the "KEEP EXPLORING..." message is visible.
3.  **Playback**: Clicking an Album name correctly plays the associated BGM.
