# Bug Fix: java.lang.ClassCastException in EliteUpgradeDialog

This plan addresses a fatal crash occurring during purchase flows where a `ContextThemeWrapper` is incorrectly cast to an `Activity`.

## User Review Required

> [!IMPORTANT]
> **Safe Context Handling:** I am replacing all direct `as Activity` casts with a safe `findActivity()` extension that traverses the `ContextWrapper` chain. This is a critical stability fix for devices that wrap contexts (e.g., Pixel 9 Pro or devices using specific theme wrappers).

## Proposed Changes

### 1. Stability Fixes

#### [MODIFY] [ShopScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/ShopScreen.kt)
- Replace `context as Activity` with `context.findActivity()`.
- Add null-safety guard for the purchase flow trigger.

#### [MODIFY] [EliteComponents.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/EliteComponents.kt)
- Standardize the `confirmButton` logic to use `findActivity()`.
- Ensure no other unsafe casts exist in the dialog lambdas.

#### [MODIFY] [MainMenuScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/MainMenuScreen.kt)
- Update `AddCreditDialog` to use safe activity retrieval for rewarded ad triggers.

## Verification Plan

### Automated Tests
- Run `gradle_build` to ensure the project compiles with the new safety logic.

### Manual Verification
1. **Purchase Flow Check:** Open the Shop and trigger a purchase. Verify the dialog opens without crashing.
2. **Elite Upgrade Check:** Open the Elite Upgrade dialog from the Game Over screen and click "Upgrade." Verify no `ClassCastException` occurs.
