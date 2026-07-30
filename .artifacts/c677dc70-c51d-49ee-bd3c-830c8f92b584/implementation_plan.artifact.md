# Website "Live Data" Mocking Plan

The user likes the `/new` concept but is seeing "no data" because the Firestore collections are either empty or the connection is missing in the local environment. This plan adds robust mock data fallbacks to the API and UI.

## Proposed Changes

### [API Enhancement]

#### [MODIFY] [route.ts (stats)](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/website/site/app/api/community/stats/route.ts)
- Add a check for `testersSnap.empty`.
- If empty or if `?mock=true` is passed, return "Legendary" community stats:
  - Total Distance: 4,850,200m
  - Bosses Defeated: 12,402
  - Active Pilots: 842
  - Mission Time: 1,250h

#### [MODIFY] [route.ts (activity)](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/website/site/app/api/community/activity/route.ts)
- Add a check for `activitySnap.empty`.
- If empty or `?mock=true` is passed, return a list of "Simulation" activities:
  - "Pilot ASH*** reached 100,000m (Ascension Protocol)"
  - "Boss Star-Eater defeated by Pilot JON***"
  - "New Fleet Record: 50x Combo by Pilot ZED***"
  - "Deep Space Signal decoded in Zone 8"

### [UI Enhancement]

#### [MODIFY] [PilotFeed.tsx](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/website/site/app/new/components/PilotFeed.tsx)
- Add a "TRANSMISSION LIVE" blinking indicator to make it feel more active.
- Ensure it shows "Awaiting Signal..." if data is still loading.

#### [MODIFY] [CommunityStats.tsx](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/website/site/app/new/components/CommunityStats.tsx)
- Add an "Establishing Connection..." loading state with skeleton pulses.

## Verification Plan

### Automated Tests
- `npm run build` to ensure no regressions.

### Manual Verification
- Access `/new?mock=true` to force the simulation data.
- Verify that the page feels "alive" even without a database connection.
