# Production Release Checklist — Jump Droid

**Last Updated:** 2026-07-16
**Target Version:** v1.5.2

> **No production release may be created until every item in this checklist is checked.**

---

## 1. Firestore Security

- [ ] **Lock Firestore security rules** — remove any `allow read, write: if true;` from Firestore Rules in Firebase Console.
- [ ] **Enable production rules** — deploy rules that restrict access:
  - Write only from authenticated admin context or via Firebase Functions.
  - Read access gated appropriately.
  - Consider `if request.auth != null` for tester-write access if needed.
- [ ] **Verify rules deployment** — ensure rules are active in Firebase Console (not still in "test mode").

## 2. Beta Analytics Verification

- [ ] **Firestore writes succeed** — launch app on a test device, submit registration, confirm document created in `testers/{sanitizedEmail}` Firestore collection.
- [ ] **Session subcollection writes** — verify `testers/{sanitizedEmail}/sessions/{id}` documents are created during gameplay.
- [ ] **No PII in Firebase Analytics** — confirm email/name/phone appear only in Firestore, never in `logEvent` calls.
- [ ] **Analytics events fire** — verify `level_start`, `level_end`, `screen_view`, `ad_impression`, etc. appear in Firebase Analytics DebugView or BigQuery export.

## 3. Crash & Error Monitoring

- [ ] **Crashlytics integration active** — check Firebase Console → Crashlytics for the release build.
- [ ] **Force a crash to verify** — temporarily add a crash path in a dev build, confirm it appears in Crashlytics dashboard within minutes.
- [ ] **Remove crash test code** — restore clean code after verification.

## 4. AdMob Production Ads

- [ ] **AdConfig.kt** — confirm `BuildConfig.DEBUG` switching is correct (test ad unit IDs in debug, production ad unit IDs in release).
- [ ] **Production ad units** — verify real AdMob ad unit IDs are set for release builds (banner, interstitial, rewarded, app open).
- [ ] **Ad serving** — sideload release APK on a test device, confirm live ads render (not "Test Ad" badges).
- [ ] **AdMob account** — confirm AdMob account is active, payment info valid, no policy violations.

## 5. Version & Build Configuration

- [ ] **`versionCode`** — check `app/build.gradle.kts` for correct value (currently 6 for v1.5.2).
- [ ] **`versionName`** — check for correct string (currently `"1.5.2"`).
- [ ] **`minifyEnabled`** — confirm `true` for release builds.
- [ ] **`shrinkResources`** — confirm `true` for release builds.
- [ ] **Signing config** — verify release signing uses environment variables or `keystore.properties`, no hardcoded credentials.
- [ ] **ProGuard/R8 rules** — confirm `proguard-rules.pro` exists and covers Firebase, Firestore, AdMob, Crashlytics keep rules.

## 6. Final Play Console Checklist

- [ ] **AAB uploaded** — `app/build/outputs/bundle/release/app-release.aab` uploaded to Google Play Console.
- [ ] **App signing** — confirm Play App Signing is enabled (or upload key is correct for APK signing).
- [ ] **Store listing** — verify description, screenshots, feature graphic, promo video, and category are complete.
- [ ] **Content rating** — complete content rating questionnaire.
- [ ] **Pricing & distribution** — confirm free vs. paid status, all target countries selected.
- [ ] **In-app products** — if any, confirm SKUs are set up and active.
- [ ] **Test tracks** — promote from Internal Testing → Closed Testing → Open Testing as needed before Production.
- [ ] **Release notes** — write release notes per language (what's new in v1.5.2).
- [ ] **APK size** — confirm AAB size is within acceptable limits (current: ~109 MB).

## 7. Pre-Launch Quality

- [ ] **Clean install** — sideload release APK on a device with no previous version, confirm first-launch flow works.
- [ ] **Upgrade path** — install previous version, then upgrade to new APK, confirm data migration (SharedPreferences) succeeds.
- [ ] **All 3 build artifacts generated** — verify `app-debug.apk`, `app-release.apk`, `app-release.aab` are fresh.
- [ ] **Accessibility** — confirm minimum touch targets, readable contrast, content descriptions for key UI elements.
- [ ] **Landscape lock** — confirm portrait-only lock is active in manifest.

## 8. Post-Release

- [ ] **Git tag** — `git tag -a v1.5.2 -m "v1.5.2 Production Release"` on the merge commit.
- [ ] **GitHub Release** — create GitHub Release with APK attachments and release notes.
- [ ] **CHANGELOG** — confirm `docs/CHANGELOG.md` is up to date.
- [ ] **Monitor** — monitor Crashlytics and Analytics for 48 hours post-release for unexpected issues.
