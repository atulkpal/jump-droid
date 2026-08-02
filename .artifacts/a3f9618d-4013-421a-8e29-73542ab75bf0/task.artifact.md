# Task List: Live Countdown & UI Sync

- [x] Add `offerEndTimeMillis` and `updateCountdown()` to `PurchaseManager.kt`
- [x] Implement periodic update loop in `MainActivity.kt` (`JumpDroidApp`)
- [x] Verify `DiscountFlyer` is correctly hooked up on all screens:
    - [x] Main Menu (Shop button sale status + countdown text)
    - [x] Shop Screen (Elite Card)
    - [x] About Screen (Support Indie section)
    - [x] Game Over Screen (Dedicated Upgrade Button)
- [x] Implement granular "DAYS, HOURS, MINS" format in `PurchaseManager.kt`
- [x] Implement high-frequency "SECONDS" ticking in last hour
- [x] Implement different styling for 3, 2, and 1 day left in `EliteComponents.kt`
- [x] Manual verification of the "live" ticking behavior
