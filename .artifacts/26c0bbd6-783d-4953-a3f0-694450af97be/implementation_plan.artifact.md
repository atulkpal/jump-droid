# Implementation Plan - Google Play Billing Library v9.1.0 Upgrade

This plan outlines the steps to upgrade the Google Play Billing Library (PBL) from version 7.1.0 to the latest stable version 9.1.0. This upgrade is required to comply with Google Play's upcoming deprecation deadlines.

## User Review Required

> [!IMPORTANT]
> This upgrade includes breaking changes in the `BillingClient` initialization and `queryProductDetailsAsync` callback signature introduced in PBL 8.0.0.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/gradle/libs.versions.toml)
- Update `billing` version from `"7.1.0"` to `"9.1.0"`.

---

### Billing Logic

#### [MODIFY] [PurchaseManager.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/PurchaseManager.kt)
- **Initialization**:
    - Replace the deprecated `.enablePendingPurchases()` with `.enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())`.
    - Add `.enableAutoServiceReconnection()` to the `BillingClient` builder to simplify connection management.
- **Product Details Query**:
    - Update the `queryProductDetailsAsync` callback to handle the new `QueryProductDetailsResult` object instead of a direct `List<ProductDetails>`.
    - Access the product details list via `queryProductDetailsResult.productDetailsList`.
- **Imports**: Add necessary imports for `PendingPurchasesParams` and `QueryProductDetailsResult`.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to verify that the project compiles with the new library version.
- Run `./gradlew test` to ensure existing logic remains sound.

### Manual Verification
- Verify that the app launches and the `initialize()` method of `PurchaseManager` completes without crashes (check Logcat).
- If possible, verify that "Premium" status still works as intended (it uses `SharedPreferences`, so it should be fine).
