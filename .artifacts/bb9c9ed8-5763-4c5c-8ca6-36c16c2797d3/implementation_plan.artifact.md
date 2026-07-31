# Implementation Plan - Subtle Urgency & Offer Expiry

This plan implements a "Subtle Urgency" system by querying the discount end date from Google Play and displaying a countdown (e.g., "Ends in 2 days") to drive conversion.

## User Review Required

> [!IMPORTANT]
> **Urgency Threshold**: The "Ends in X days" message will only appear if the sale is scheduled to end within the next **3 days**. If the sale is permanent or has no end date, no urgency message will be shown to keep the HUD clean.

## Proposed Changes

### 1. Purchase Manager - Expiry Logic

#### [MODIFY] [PurchaseManager.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/PurchaseManager.kt)
- Add `offerExpiryText` as a public state variable.
- Update `refreshProductDetails()`:
    - Access `bestOffer.validTimeWindow`.
    - Retrieve `endTimeMillis`.
    - Calculate time remaining: `endTimeMillis - System.currentTimeMillis()`.
    - If remaining time is < 3 days, format a string: "ENDS IN 2 DAYS", "ENDS TOMORROW", or "ENDS IN 5H".
    - Log the expiry for debugging.

### 2. UI - Subtle Urgency Integration

#### [MODIFY] [EliteComponents.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/EliteComponents.kt)
- Update `DiscountFlyer`:
    - Add an optional `urgencyText` parameter.
    - If `urgencyText` is present, display it in a smaller, secondary badge or as a sub-text with a subtle blinking animation.

#### [MODIFY] [SettingsScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/SettingsScreen.kt)
#### [MODIFY] [ShopScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/ShopScreen.kt)
- Pass `purchaseManager.offerExpiryText` into the `DiscountFlyer`.

## Verification Plan

### Automated Tests
- `gradle_build :app:assembleDebug`

### Manual Verification
- **Expiry Detection**: Temporarily mock the `endTimeMillis` to be 24 hours in the future in code. Verify the UI shows "ENDS TOMORROW".
- **Visual Polish**: Ensure the urgency text is subtle enough not to be annoying but clear enough to be noticed.
