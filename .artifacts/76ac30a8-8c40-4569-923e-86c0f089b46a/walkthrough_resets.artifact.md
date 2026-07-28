# Walkthrough - Robust Reset Protocols

I have implemented two distinct levels of data clearing: **Reset Progress** and **Factory Reset**. This ensures that data no longer "sticks" after a wipe and clearly separates gameplay progression from account-level settings and purchases.

## Key Changes

### 1. Two-Tiered Reset Logic
- **Reset Progress (Gameplay Only)**:
    - Clears all missions, discoveries, artifacts, cash, and credits.
    - **PRESERVES** your "Premium User" status (Remove Ads purchase).
    - Resets your **Ascension Rank** to I and **Mastery Points** to 0.
- **Factory Reset (Total Wipe)**:
    - Clears **EVERYTHING**, including Premium purchases, analytics consent, and server settings.
    - Forces a hard **Sign Out** from Google and Firebase.
    - Ads will return after this operation.

### 2. Multi-File Preference Cleanup
- A Factory Reset now targets all 6 preference files used by the app:
    1. `JumpDroidPrefs` (Progression)
    2. `LoginPrefs` (Auth session)
    3. `jump_droid_prefs` (Dev/Experimental)
    4. `PlayerAnalyticsPrefs` (ID and Consent)
    5. `RemoteConfigPrefs` (Cached server values)
    6. `fcm_prefs` (Cloud messaging)

### 3. State Persistence Fix
- Fixed a bug where **Mastery Points (MP)** and **Rank** would persist after a reset because the internal `ArtifactManager` wasn't being explicitly cleared.
- Added a call to `updateRank()` immediately after a wipe to force the UI to refresh with Rank I / 0 MP.

## Technical Details
- **Selective Cleanup**: Modified `ProgressionManager.wipeData()` to intelligently preserve or destroy the `premium_user` flag based on the reset type.
- **Callback Pattern**: Moved the complex reset logic from the `SettingsScreen` (UI layer) into `MainActivity` (Core layer) for better architecture and safer execution.
- **Navigation Safety**: Both reset types now correctly navigate the player back to the `Title` screen to ensure a clean engine restart.

## Verification Results
- **Reset Progress**: Verified that Cash and MP are cleared but "Ads Removed" remains active.
- **Factory Reset**: Verified that the player is signed out and all settings (including volume and haptics) return to defaults.
- **Insignia Refresh**: Verified the Rank Medallion correctly resets to the Bronze Triangle (Rank I) on the Main Menu after a wipe.
