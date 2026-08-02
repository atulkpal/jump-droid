# Credit Acquisition Modal Icon Polish

This plan addresses the "crazy colors" regression in the Credit Acquisition modal by unifying the icon styling with the rest of the application.

## User Review Required

> [!IMPORTANT]
> **Icon Tinting Removal:** I will remove the manual `SciFiGold` tint from the currency icons in the `AddCreditDialog`. This allows the vector icons to use their original designed colors, ensuring consistency with the top status bar and removing the "crazy" high-contrast effect.

## Proposed Changes

### 1. Main Menu: Credit Dialog Refinement

#### [MODIFY] [MainMenuScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/MainMenuScreen.kt)
- Remove `tint = SciFiGold` from all instances of `ic_currency_cr` and `ic_currency_jc` within the `AddCreditDialog`.
- Update the dialog's conversion button to use standard `Image` or untinted `Icon` for the currency symbols.
- Ensure the "Daily Protocol Limit" text remains subtle and doesn't compete with the primary icons.

### 2. UI Consistency Pass

#### [MODIFY] [GameOverOverlay.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/GameOverOverlay.kt)
- Review and ensure that currency indicators are consistent across the Game Over screen.

## Verification Plan

### Manual Verification
1. **Modal Check:** Open the Credit Acquisition modal and verify that the Credits (Gold) and Cash (Green/Gold) icons look natural and identical to their status bar counterparts.
2. **Visual Consistency:** Compare the icons in the Main Menu, Hangar, and Game Over screen to ensure a unified currency identity.
