# EPIC 13 — Production Readiness Task List

- [x] **Technical Hardening**
    - [x] Implement Anonymous Auth in `LoginManager.kt`
    - [x] Initialize Anonymous Auth in `MainActivity.kt` (Via `restoreSession` update)
    - [x] Deploy locked `firestore.rules` (Updated locally)
    - [x] Update `proguard-rules.pro` with Room/WorkManager stability rules
- [x] **Store Readiness**
    - [x] Update `PLAY_STORE_DESCRIPTION.md` for v2.2.0
    - [x] Accessibility pass for `SettingsScreen.kt`
- [x] **Verification**
    - [x] Run `gradle_build` to verify ProGuard/R8 integrity
    - [x] Verify Firestore write path (simulated)
