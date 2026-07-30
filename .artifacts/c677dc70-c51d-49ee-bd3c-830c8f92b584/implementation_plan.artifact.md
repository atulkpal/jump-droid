# Website Design Evolution — Staging Plan (/new)

This plan outlines the creation of a new interactive landing page at `/new`. To keep the project organized, all new logic and components will be isolated within the `app/new/` directory.

## User Review Required

> [!IMPORTANT]
> All new files will be nested under `website/site/app/new/`. This includes components, logic, and the main page entry.
>
> [!CAUTION]
> Integrating live community data requires that the Firebase project and `NEXT_PUBLIC_APP_URL` are correctly configured in the target environment (Vercel).

## Proposed Changes

### [Isolated Structure]

#### [NEW] [page.tsx](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/website/site/app/new/page.tsx)
- The entry point for the new design.

#### [NEW] [PilotFeed.tsx](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/website/site/app/new/components/PilotFeed.tsx)
- Live transmission ticker.

#### [NEW] [ZoneAscension.tsx](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/website/site/app/new/components/ZoneAscension.tsx)
- Vertical scroll parallax journey through 12 zones.

#### [NEW] [FleetHangar.tsx](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/website/site/app/new/components/FleetHangar.tsx)
- Interactive rocket variant selector and radar chart.

#### [NEW] [CommunityStats.tsx](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/website/site/app/new/components/CommunityStats.tsx)
- Aggregated metrics display.

### [Data & API]

#### [MODIFY] [site-content.ts](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/website/site/app/data/site-content.ts)
- Add `ZONE_THEMES` definition.

#### [NEW] [route.ts](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/website/site/app/api/community/stats/route.ts)
- API to fetch aggregated Firestore data.

## Verification Plan

### Automated Tests
- `npm run build` to ensure isolation works and no regressions occur.

### Manual Verification
- Access `/new` and verify all nested components load correctly.
- Verify that background transitions and interactive elements are scoped to the `/new` path.

## Verification Plan

### Automated Tests
- `npm run build` in `website/site` to ensure no regressions in existing pages or type errors in new components.

### Manual Verification
- Access `/new` on local development server.
- Verify parallax scroll behavior in `ZoneAscension`.
- Verify stat radar chart updates in `FleetHangar`.
- Check `PilotFeed` with mock data to ensure scrolling is smooth.
