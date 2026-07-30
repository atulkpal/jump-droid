# Implementation Plan - Update Notification & Production Build

This plan implements a remote version-check system to notify players of updates and generates the required production artifacts for the Google Play Console.

## User Review Required

> [!IMPORTANT]
> **Triggering the Message**: Once you upload the AAB to the Play Store and it's approved, you must update the Firestore document at `server_config/remote_config` with `latest_version_code = 13` (or higher) to trigger the "Update Available" message on existing devices.

## Proposed Changes

### 1. Update Notification Logic

#### [MODIFY] [RemoteConfigManager.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/RemoteConfigManager.kt)
- Add version comparison logic.
- Compare local `BuildConfig.VERSION_CODE` with remote `latest_version_code`.
- If an update is detected, invoke the announcement callback with the message: **"UPDATE AVAILABLE // New modes and tactical enhancements are live on the Play Store!"**

---

### 2. Production Artifact Generation

#### [BUILD] **Debug APK**
- Execute: `gradle_build("app:assembleDebug")`
- Output: `app/build/outputs/apk/debug/app-debug.apk`

#### [BUILD] **Release APK**
- Execute: `gradle_build("app:assembleRelease")`
- Output: `app/build/outputs/apk/release/app-release.apk`

#### [BUILD] **Android App Bundle (AAB)**
- Execute: `gradle_build("app:bundleRelease")`
- Output: `app/build/outputs/bundle/release/app-release.aab`

---

## Verification Plan

### Automated Tests
- `gradle_build` will verify syntax and successful compilation of all artifacts.

### Manual Verification
1.  **Build Check**: Verify that the three files exist at their expected paths.
2.  **Version Logic**: Verify that if we (temporarily) mock a higher remote version, the "Update Available" notification appears in the game HUD.
