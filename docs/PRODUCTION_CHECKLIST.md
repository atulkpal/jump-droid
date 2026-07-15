# Production Release Checklist — Jump Droid v1.5.2

## Pre-Build

- [x] `versionCode` bumped (4)
- [x] `versionName` set ("1.5.2")
- [x] All 10 bugs fixed (see CHANGELOG)
- [x] `release` signing config active
- [x] No hardcoded credentials in source
- [x] Build type: `release` (minifyEnabled = true)

## Build

- [x] `./gradlew clean`
- [ ] `./gradlew assembleRelease` — generates AAB + APK
- [ ] `./gradlew assembleDebug` — generates debug APK

## Verification

- [ ] AAB uploads to Google Play Internal Testing
- [ ] Release APK installs on clean device (sideload)
- [ ] Debug APK installs on dev device
- [ ] Existing save data migrates (SharedPreferences unchanged)
- [ ] Portrait lock active
- [ ] Keep-screen-on during gameplay

## Post-Release

- [ ] Tag commit: `git tag -a v1.5.2 -m "v1.5.2 Closed Beta"`
- [ ] GitHub Release created with APK attachments
- [ ] Website version updated (v1.5.1 → v1.5.2)
- [ ] `docs/CHANGELOG.md` updated
- [ ] Testers notified via email/Discord
