# Walkthrough - Subtle Urgency & Offer Expiry

I have implemented a subtle urgency system that displays a countdown when a discount is about to expire, driving conversion without being intrusive.

## Changes Made

### 1. Offer Expiry Logic
- **Valid Time Window**: Updated `PurchaseManager.kt` to retrieve the `validTimeWindow` from the best identified offer.
- **Smart Countdown**: Implemented logic to calculate the remaining time. To maintain the game's clean aesthetic, the urgency text only appears if the sale is ending in less than **3 days**.
- **Dynamic Formatting**: The app now generates contextual strings like:
    - `"ENDS IN 2 DAYS"`
    - `"ENDS IN 5 HOURS"`
    - `"ENDING SOON"` (if less than 1 hour remains)

### 2. Enhanced Discount Flyer
- **Urgency Label**: Updated the `DiscountFlyer` in `EliteComponents.kt` to display the expiry text directly below the discount badge (e.g., below "20% OFF").
- **Consistent Animations**: The urgency text inherits the subtle pulsing animation of the main badge to draw just the right amount of attention.

### 3. Global Integration
- **Sync Across Screens**: Ensured the urgency text is passed to the flyers in the **Shop**, **Settings**, and the **Elite Upgrade Dialog**.
- **Real-time Updates**: The expiry status is refreshed every time the app re-syncs with the Play Store on launch.

## Verification Results

### Automated Tests
- `gradle_build :app:assembleDebug` passed successfully.

### Manual Verification Steps
1. **Trigger Urgency**: In the Google Play Console, set your sale to end in 48 hours.
2. **Refresh App**: Clear the Play Store cache and restart Jump Droid.
3. **Check UI**: Verify that the red flyer now includes a secondary gold label saying `"ENDS IN 2 DAYS"`.
4. **Subtle Mode**: Set the sale to end in 5 days; verify the secondary label disappears, leaving only the discount percentage.
