# Production Release Checklist — Jump Droid v1.5.2

## Pre-Build

- [x] `versionCode` bumped (5)
- [x] `versionName` set ("1.5.2")
- [x] All 10 bugs fixed (see CHANGELOG)
- [x] `release` signing config active
- [x] No hardcoded credentials in source
- [x] Build type: `release` (minifyEnabled = true)

## Build

- [x] `./gradlew clean`
- [x] `./gradlew assembleRelease` — generates AAB + APK
- [x] `./gradlew assembleDebug` — generates debug APK

## Verification

- [x] AAB uploads to Google Play Internal Testing
- [x] Release APK installs on clean device (sideload)
- [x] Debug APK installs on dev device
- [x] Existing save data migrates (SharedPreferences unchanged)
- [x] Portrait lock active
- [x] Keep-screen-on during gameplay

## Post-Release

- [x] Tag commit: `git tag -a v1.5.2 -m "v1.5.2 Closed Beta"`
- [x] GitHub Release created with APK attachments
- [x] Website version updated (v1.5.1 → v1.5.2)
- [x] `docs/CHANGELOG.md` updated
- [x] Testers notified via email/Discord
