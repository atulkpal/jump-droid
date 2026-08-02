# Implementation Plan - Global Sale Urgency & Clean UI Architecture

This plan establishes a "Single-Action" UI philosophy across the game, removing confusing nested clickables, and propagates a precise, granular sale countdown timer to all promotion surfaces.

## User Review Required

> [!IMPORTANT]
> - **Unified Upgrade Button**: All promotional surfaces (Game Over, Shop, About) will now use a dedicated, standalone "UPGRADE TO ELITE" button for non-premium users.
> - **Nested Logic Removal**: Nested `clickable` elements inside Action Buttons (like "RE-DEPLOY") are removed to prevent event swallowing and user confusion.
> - **Global Sale Presence**: A sale countdown timer will now appear on the Main Menu (Shop button), Shop Screen, About Screen, and Game Over Screen.
> - **Enhanced Urgency**: Detailed countdowns (Hours/Minutes) will be shown when the sale deadline is near.

## Proposed Changes

### 1. Granular Urgency Engine
Implement a high-precision countdown calculation in the billing manager.

#### [MODIFY] [PurchaseManager.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/PurchaseManager.kt)
- **Precise Expiry Logic**:
    - `> 3 days`: "ENDS IN X DAYS" (Static indicator)
    - `1 - 3 days`: "ENDS IN X DAYS" (High Urgency / Pulsing)
    - `< 24 hours`: "ENDS IN Xh Ym" (Extreme Urgency)
    - `< 1 hour`: "ENDS IN X MIN" (Critical)
- Ensure the `validTimeWindow` is queried on every product refresh.

---

### 2. Clean UI: Single-Action Action Buttons
Refactor the Game Over screen and Zen mode restart flow to eliminate confusion.

#### [MODIFY] [GameOverOverlay.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/GameOverOverlay.kt)
- **Remove Nested Links**: Delete `Modifier.clickable` from inside "RE-DEPLOY" and "CONTINUE" rows.
- **Dedicated Elite CTA**: Add a standalone `EliteUpgradeButton` (SciFiGold styling) above the action block for non-premium users.
- **Zen Restart Fix**: Ensure the "RE-DEPLOY ZEN MODE" button focuses 100% on the restart/ad flow. Remove the "REMOVE ADS WITH PREMIUM" sub-clickable.

---

### 3. Global Sale Propagation
Ensure the sale is visible at every touchpoint.

#### [MODIFY] [MainMenuScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/MainMenuScreen.kt)
- Add a "SALE" badge or a mini countdown timer to the **SHOP** button in the "COMMAND CENTER" list when an offer is active.

#### [MODIFY] [ShopScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/ShopScreen.kt)
- Update the Elite purchase card to show the `DiscountFlyer` with the new granular countdown.

#### [MODIFY] [AboutScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/AboutScreen.kt)
- Add the `DiscountFlyer` to the "SUPPORT INDIE" section near the "GO PREMIUM" button.

#### [MODIFY] [EliteComponents.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/EliteComponents.kt)
- Update `DiscountFlyer` layout to accommodate longer strings (e.g., "ENDS IN 18h 45m") without clipping.

## Verification Plan

### Manual Verification
1.  **Zen Re-Deploy**: Die in Zen mode. Click "RE-DEPLOY". Confirm it restarts/shows an ad and **never** opens the purchase modal directly.
2.  **Upgrade Button**: Confirm a clear "UPGRADE TO ELITE" button exists on Game Over, Shop, and About screens.
3.  **Sale Countdown**:
    - Verify the countdown appears on the Main Menu Shop button.
    - Verify granular timing (h/m) when a sale is expiring within 24h.
4.  **No Nesting**: Click all action buttons to ensure no unexpected modals open from "sub-text" or adjacent areas.
