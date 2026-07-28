# Sign-In Failure Audit

**Last Updated:** 2026-07-26
**Status:** RESOLVED ✅
**Component:** `LoginManager.kt` (EPIC 12, Phase 7.1)

---

## 1. Overview

The Google Sign-In / Play Games identity flow had a silent failure where the account chooser would launch but the result was never captured. This has been resolved by fixing the result handling logic and correctly configuring the Firebase/Google Cloud identity providers.

---

## 2. Root Cause Analysis

1.  **Incorrect Result Parsing**: `LoginManager.kt` was calling `silentSignIn()` inside `onSignInResult` instead of using `GoogleSignIn.getSignedInAccountFromIntent(data)`. This caused the app to ignore the interactive sign-in result.
2.  **Missing SDK Initialization**: `PlayGamesSdk.initialize(this)` was missing from `MainActivity.onCreate`, which is mandatory for the Play Games V2 SDK.
3.  **Missing OAuth Clients**: The `google-services.json` file lacked `oauth_client` entries for the Android platform, meaning the backend wasn't configured to trust the app's SHA-1 fingerprints.

---

## 3. Resolution Steps

### Step 1: Code Fixes
- **`MainActivity.kt`**: Added `PlayGamesSdk.initialize(this)`.
- **`LoginManager.kt`**: Rewrote `onSignInResult` to use `GoogleSignIn.getSignedInAccountFromIntent(data)`. Added robust logging for `ApiException` status codes.

### Step 2: Fingerprint Configuration
- Extracted Debug and Release SHA-1 fingerprints using `keytool`.
  - **Debug SHA-1**: `B2:86:C7:33:3E:F8:44:23:79:CC:D2:FC:A9:D7:AD:F6:B7:15:3C:E7`
  - **Release SHA-1**: `24:DB:FD:76:C7:5D:1E:46:5C:91:74:87:0A:58:50:70:7C:F4:BD:53`
- Added these fingerprints to the Firebase Console via CLI:
  ```bash
  firebase apps:android:sha:create <app_id> <sha1>
  ```

### Step 3: Configuration Sync
- Downloaded the updated `google-services.json` containing the new `oauth_client` definitions and replaced the local file.

---

## 4. Verification Results

- **Sign-in Flow**: Interactive account picker now correctly triggers the `onSignInResult` callback and populates the `LoginManager` state.
- **UI Reflection**: The Main Menu now correctly displays "SIGNED IN: [Display Name]" and retrieves the `playerId` from Play Games.
- **Build Status**: Verified with `assembleDebug`.
