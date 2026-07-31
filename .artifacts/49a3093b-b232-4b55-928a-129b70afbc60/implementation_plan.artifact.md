# Implementation Plan — EPIC 13: Production Readiness & Store Listing

This plan covers the final "Production Readiness" items for **Jump Droid** to ensure security, stability, and store compliance.

## User Review Required

> [!IMPORTANT]
> **Firestore Security**: I am proposing to enable **Anonymous Authentication**. This allows us to lock Firestore rules to `if request.auth != null` without forcing players to sign in with Google. This is the only way to "Lock" rules while still allowing anonymous testers to write telemetry.

> [!WARNING]
> **ProGuard Optimization**: Enabling `minifyEnabled` and `shrinkResources` can occasionally cause issues with reflection-heavy libraries like Room. I have added stability rules to mitigate this.

## Open Questions
- **Version Alignment**: The user noted that `v2.0.0` is currently on the Play Console. The local build is at `v2.2.0`. I will ensure the metadata reflects the features present in the current local version while maintaining continuity with the Play Console.

## Proposed Changes

### 1. Infrastructure Gating & Security

#### [MODIFY] [firestore.rules](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/website/site/firestore.rules)
*   Remove all `allow read, write: if true` rules.
*   Restrict `testers` and `sessions` to `if request.auth != null`.
*   Allow public `create` only for `betaUsers` and `recruitmentContacts`.
*   Maintain owner-only write for `appConfig`.

#### [MODIFY] [LoginManager.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/LoginManager.kt)
*   Add `signInAnonymously()` method.
*   This will be called on app startup if no user is signed in, ensuring every player has a Firebase UID for secure Firestore writes.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/MainActivity.kt)
*   Invoke `loginManager.signInAnonymously()` if Google sign-in is not active.

---

### 2. Stability & Optimization

#### [MODIFY] [proguard-rules.pro](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/proguard-rules.pro)
*   Add specific `androidx.work` and `androidx.room` keep rules to prevent startup crashes on certain devices (as per the Production Checklist).
*   Add `-dontwarn` for optional dependencies that might be stripped.

---

### 3. Store Metadata & Accessibility

#### [MODIFY] [PLAY_STORE_DESCRIPTION.md](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/docs/marketing/PLAY_STORE_DESCRIPTION.md)
*   Ensure the "What's New" section is ready for **v2.2.0** (Current local build).

#### [MODIFY] [SettingsScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/src/main/java/com/ashwathai/jump_droid/SettingsScreen.kt)
*   Add `contentDescription` to volume sliders and toggle switches for Screen Reader compatibility.

---

## Verification Plan

### Automated Tests
- `gradle_build`: Verify the app compiles with the new ProGuard rules.
- `analyze_file`: Run on `LoginManager.kt` and `MainActivity.kt`.

### Manual Verification
- **Security Check**: Verify Firestore writes still succeed for "Anonymous" testers after rules are locked.
- **Accessibility Check**: Use TalkBack (simulated via `contentDescription` inspection) to verify UI accessibility.
- **AdMob Check**: Verify `AdConfig.kt` is correctly switching to sample IDs in Debug mode.
