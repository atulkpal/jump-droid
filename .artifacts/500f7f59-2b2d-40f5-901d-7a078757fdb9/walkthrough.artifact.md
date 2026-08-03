# Bug Fix: ClassCastException Resolution

I have successfully resolved the fatal `java.lang.ClassCastException` occurring during purchase and ad flows.

## Root Cause & Solution

> [!IMPORTANT]
> **Context Wrapping:** On many modern Android devices (like the Pixel 9 Pro), the `LocalContext` provided by Compose is often a `ContextThemeWrapper` rather than a direct `Activity`. Casting this context directly to `Activity` caused the application to crash.

- **[FIXED] Safe Activity Retrieval:** I have replaced all direct `as Activity` casts with a safe `findActivity()` extension function. This helper recursively unwraps the `ContextWrapper` chain to find the underlying `Activity`.
- **[HARDENED] Null Safety:** Added null-safety guards to all purchase and ad triggers to ensure they only fire when a valid Activity is present.

## Files Modified

- **[ShopScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/ShopScreen.kt):** Replaced unsafe cast in the main purchase button.
- **[EliteComponents.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/EliteComponents.kt):** Standardized `EliteUpgradeDialog` to use safe context unwrapping.
- **[MainMenuScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/MainMenuScreen.kt):** Updated `AddCreditDialog` for crash-free rewarded ad triggers.
- **[ArchiveScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/ArchiveScreen.kt):** Fixed safe activity retrieval for lore restoration ads.

## Verification Results

### Automated Verification
- **Build Status:** `assembleDebug` completed successfully.
- **Dependency Check:** Confirmed `findActivity()` is globally accessible via the same package.

### Manual Verification
- Verified purchase flow stability on devices using themed wrappers.
- Confirmed no regressions in standard navigation.

---
**Status:** COMPLETE ✅
