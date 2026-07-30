# Website Staging Evolution — v2.2.3 Walkthrough

I have implemented the new interactive landing page at `/new`. This design transforms the Jump Droid web presence into a more immersive, data-driven experience.

## Key Features

### 1. Parallax Zone Ascension
- **Location**: `app/new/components/ZoneAscension.tsx`
- **Behavior**: A scroll-driven journey through all 12 atmospheric zones.
- **Visuals**: Background colors and glow effects transition dynamically as you "climb." Floating threat SVGs appear in their respective zones.

### 2. Live Pilot Transmission Feed
- **Location**: `app/new/components/PilotFeed.tsx`
- **Behavior**: A real-time marquee ticker that displays recent activities from the beta community.
- **Logic**: Fetches the latest entries from the `activityLog` via the new `/api/community/activity` endpoint.

### 3. Interactive Fleet Hangar
- **Location**: `app/new/components/FleetHangar.tsx`
- **Behavior**: Allows users to preview all 12 rocket variants.
- **Intelligence**: Features a dynamic Radar Chart that visualizes the Thrust, Shield, Heat, Hull, and Speed stats for each chassis.

### 4. Community Mastery Dashboard
- **Location**: `app/new/components/CommunityStats.tsx`
- **Behavior**: Displays aggregated fleet metrics like "Total Distance Climbed" and "Active Pilots."
- **Source**: Powered by the new `/api/community/stats` server-side route.

## Technical Details

- **Isolation**: All new components are nested in `app/new/components/` to ensure zero impact on the production site.
- **Build Verified**: The project successfully compiles with zero warnings.
- **Staging URL**: Access the new design at `https://[your-domain]/new` once deployed.

> [!TIP]
> To replace the old site with the new one, simply swap the contents of `app/page.tsx` with `app/new/page.tsx`.

> [!IMPORTANT]
> The "Live" features require `FIREBASE_SERVICE_ACCOUNT_KEY` to be configured in Vercel to fetch real data from Firestore.
