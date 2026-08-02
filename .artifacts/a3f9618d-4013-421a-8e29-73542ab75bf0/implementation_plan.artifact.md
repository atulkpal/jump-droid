# Implementation Plan - Live Sale Countdown & UI Synchronization

This plan implements a truly "live" sale countdown timer that updates every minute and ensures it is visible across all promotional surfaces in the game.

## User Review Required

> [!NOTE]
> The sale countdown will now update in real-time (ticking every minute) without needing to restart the app or re-open screens.

## Proposed Changes

### 1. Live Countdown Engine
Update `PurchaseManager.kt` to handle local ticking of the countdown string.

#### [MODIFY] [PurchaseManager.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/PurchaseManager.kt)
- **Store End Time**: Add a private `offerEndTimeMillis` variable.
- **Local Update Logic**: Implement `updateExpiryTextLocally()` which recalculates `offerExpiryText` using the stored end time and current system time.
- **Periodic Update**: Call `refreshProductDetails()` more strategically and ensure the UI drives the "ticking" via a `LaunchedEffect` that calls a new public `updateCountdown()` method.

---

### 2. UI Ticking Integration
Ensure the UI components trigger the countdown update.

#### [MODIFY] [JumpDroidApp Composable](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/MainActivity.kt)
- Add a high-level `LaunchedEffect(Unit)` that runs a loop every 30-60 seconds to call `purchaseManager.updateCountdown()`. This ensures the state is fresh app-wide.

---

### 3. Sale Visibility Verification
Ensure the countdown is present on all relevant screens.

- **Main Menu**: (Already implemented, will verify) Shop button "(SALE!)" indicator.
- **Shop Screen**: (Already implemented, will verify) `DiscountFlyer` on Elite card.
- **About Screen**: (Already implemented, will verify) `DiscountFlyer` in "Support Indie" section.
- **Game Over Screen**: (Already implemented, will verify) `DiscountFlyer` on the new standalone upgrade button.

## Verification Plan

### Manual Verification
1.  **Live Ticking**: Open the Shop or Game Over screen. Wait for a minute boundary (if the sale is under 1 hour, wait for a minute change). Verify the "ENDS IN X MIN" or "ENDS IN Xh Ym" string updates.
2.  **Global Presence**: Navigate through Main Menu -> Shop -> About -> Game Over. Verify the sale indicator and countdown are visible on all four screens.
3.  **Extreme Urgency**: Simulate a sale ending in under 60 minutes. Verify it shows "ENDS IN X MIN" and updates every minute.
