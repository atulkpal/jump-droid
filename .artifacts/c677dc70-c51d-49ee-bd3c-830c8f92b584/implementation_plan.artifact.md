# Website Content Overhaul — v2.2.3 Update

The current website for Jump Droid reflects version v1.5.2 and misses many major features introduced in EPICs 10, 11, 12, and 13. This plan outlines the update to reflect the "Zen Mastery & Impossible Uplink" era (v2.2.3).

## Proposed Changes

### [Website Data]
Update the core data structure to reflect the current game state.

#### [MODIFY] [site-content.ts](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/website/site/app/data/site-content.ts)
- Update `HERO` tagline to "ZEN MASTERY & IMPOSSIBLE UPLINK".
- Update `HERO` description: "8 atmospheric zones" → "12 atmospheric zones".
- Update `HERO.features`:
    - Replace "Celestial Conflict" with "Zen & Impossible Modes" (mentioning the new ways to play).
    - Add/update features to mention "Fleet Expansion" and "Mastery Points".
- Update `ENTITY_DESCRIPTIONS`:
    - Add descriptions for new zones: `THE_FOUNDRY`, `CHRONO_RIFT`, `THE_BEYOND`, `STELLAR_GATE`, `ANCIENT_CONSTRUCT`, `SINGULARITY`.
    - Add descriptions for new bosses: `STAR_EATER`, `ENTROPY_CORE`, `THE_ARCHITECT`, `THE_SINGULARITY`.
    - Add descriptions for new threats/hazards: `PHASE_WRAITH`, `VOID_HARVESTER`, `GRAVITY_RAM`, `SHIELD_PLATFORM`.
- Update `GAME_CATEGORIES`:
    - `platforms`: Change title to "18+ Platform Types" (reflecting the expanded library).
    - `bosses`: Change title to "12 Boss Encounters". Add the new bosses to the entity list.
    - `rockets`: Change title to "12 Fleet Variants".
    - `threats`: Change title to "36+ Threat Variants".
- Update `DOWNLOADS`: version `v1.5.2` → `v2.2.3`.

#### [MODIFY] [constants.ts](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/website/site/lib/constants.ts)
- Update `FEATURES` list to include "Zen Mode" and "Fleet Mastery".
- Ensure screenshot captions match the latest gameplay focus.

### [Website Layout & Components]
Minor adjustments to accommodate the new content.

#### [MODIFY] [HeroSignal.tsx](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/website/site/app/components/screens/HeroSignal.tsx)
- Check if any hardcoded version strings or counts exist.

#### [MODIFY] [GameplayCards.tsx](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/website/site/app/components/screens/GameplayCards.tsx)
- Ensure the cycling entity list includes the new visual types if SVGs are available.

## Verification Plan

### Automated Tests
- `npm run build` in `website/site` to ensure no TypeScript or build errors after data changes.

### Manual Verification
- Check the local development server to verify that the new descriptions and titles render correctly.
- Verify that the "Zen Mode" and "12 Zones" mentions appear in the Hero section.
- Ensure the version number is updated in the download section.
