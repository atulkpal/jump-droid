# Production Release Checklist — Jump Droid

**Last Updated:** 2026-08-04
**Target Version:** v2.2.6

> **No production release may be created until every item in this checklist is checked.**

---

## 0. Artifact Archiving (History)

- [ ] **Archive previous artifacts** — Move existing APKs and AABs from `app/build/outputs/` to a persistent history folder: `releases/v<version>/`.
- [ ] **Rename for traceability** — Use the format `debug-<version>-<versionCode>.apk`, `release-<version>-<versionCode>.apk`, and `release-<version>-<versionCode>.aab`.
- [ ] **Verification** — Confirm the `releases/` folder is not in `.gitignore` and is tracked for project history.

## 1. Firestore Security

- [x] **Lock Firestore security rules** — remove any `allow read, write: if true;` from Firestore Rules in Firebase Console.
- [x] **Enable production rules** — deploy rules that restrict access:
  - Write only from authenticated admin context or via Firebase Functions.
  - Read access gated appropriately.
  - Consider `if request.auth != null` for tester-write access if needed.
- [x] **Verify rules deployment** — ensure rules are active in Firebase Console (not still in "test mode").

## 2. Beta Analytics Verification

- [x] **Firestore writes succeed** — launch app on a test device, submit registration, confirm document created in `testers/{sanitizedEmail}` Firestore collection.
- [x] **Session subcollection writes** — verify `testers/{sanitizedEmail}/sessions/{id}` documents are created during gameplay.
- [x] **No PII in Firebase Analytics** — confirm email/name/phone appear only in Firestore, never in `logEvent` calls.
- [x] **Analytics events fire** — verify `level_start`, `level_end`, `screen_view`, `ad_impression`, etc. appear in Firebase Analytics DebugView or BigQuery export.

## 3. Crash & Error Monitoring

- [x] **Crashlytics integration active** — check Firebase Console → Crashlytics for the release build.
- [x] **Force a crash to verify** — temporarily add a crash path in a dev build, confirm it appears in Crashlytics dashboard within minutes.
- [x] **Remove crash test code** — restore clean code after verification.

## 4. AdMob Production Ads

- [x] **AdConfig.kt** — confirm `BuildConfig.DEBUG` switching is correct (test ad unit IDs in debug, production ad unit IDs in release).
- [x] **Production ad units** — verify real AdMob ad unit IDs are set for release builds (banner, interstitial, rewarded, app open).
- [x] **Ad serving** — sideload release APK on a test device, confirm live ads render (not "Test Ad" badges).
- [x] **AdMob account** — confirm AdMob account is active, payment info valid, no policy violations.

## 5. Version & Build Configuration

- [x] **`versionCode`** — check `app/build.gradle.kts` for correct value (currently 14 for v2.2.4).
- [x] **`versionName`** — check for correct string (currently `"2.2.4"`).
- [x] **`minifyEnabled`** — confirm `true` for release builds.
- [x] **`shrinkResources`** — confirm `true` for release builds.
- [x] **Signing config** — verify release signing uses environment variables or `keystore.properties`, no hardcoded credentials.
- [x] **ProGuard/R8 rules** — confirm `proguard-rules.pro` exists and covers Firebase, Firestore, AdMob, Crashlytics keep rules.
- [x] **Startup Stability (R8)** — Confirm `androidx.work` and `androidx.room` keep rules are active to prevent startup crashes on `WorkDatabase` creation.

## 6. Final Play Console Checklist

- [x] **AAB uploaded** — `app/build/outputs/bundle/release/app-release.aab` uploaded to Google Play Console.
- [x] **App signing** — confirm Play App Signing is enabled (or upload key is correct for APK signing).
- [x] **Store listing** — verify description, screenshots, feature graphic, promo video, and category are complete.
- [x] **Content rating** — complete content rating questionnaire.
- [x] **Pricing & distribution** — confirm free vs. paid status, all target countries selected.
- [x] **In-app products** — if any, confirm SKUs are set up and active.
- [x] **Test tracks** — promote from Internal Testing → Closed Testing → Open Testing as needed before Production.
- [x] **Release notes** — write release notes per language (what's new in v2.1.1).
- [x] **APK size** — confirm AAB size is within acceptable limits (current: ~109 MB).

## 7. Pre-Launch Quality

- [x] **Clean install** — sideload release APK on a device with no previous version, confirm first-launch flow works.
- [x] **Upgrade path** — install previous version, then upgrade to new APK, confirm data migration (SharedPreferences) succeeds.
- [x] **All 3 build artifacts generated** — verify `app-debug.apk`, `app-release.apk`, `app-release.aab` are fresh.
- [x] **Accessibility** — confirm minimum touch targets, readable contrast, content descriptions for key UI elements.
- [x] **Landscape lock** — confirm portrait-only lock is active in manifest.

## 8. Post-Release

- [x] **Git tag** — `git tag -a v2.2.4 -m "v2.2.4 Production Release"` on the merge commit.
- [x] **GitHub Release** — create GitHub Release with APK attachments and release notes.
- [x] **CHANGELOG** — confirm `docs/CHANGELOG.md` is up to date.
- [x] **Monitor** — monitor Crashlytics and Analytics for 48 hours post-release for unexpected issues.
