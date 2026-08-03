# Implementation Plan - Jump Droid v2.2.6 Release

Official release of Jump Droid v2.2.6, incorporating all changes from `feature/epic14-polish-monetization`. This plan follows the **Git Branch Policy** and **Production Release Checklist** defined in the project constitution.

## User Review Required

> [!IMPORTANT]
> **Versioning Approval**: As per Section 12 of `AGENTS.md`, explicit approval is required for `versionCode` and version bumps.
> - **Proposed Version Name**: `2.2.6`
> - **Proposed Version Code**: `15`

> [!WARNING]
> **Artifact Overwrite**: Existing artifacts will be renamed to `*-2.2.4-14.*` before new builds are generated.

## Proposed Changes

### Phase 1: Branch Management
Following Section 14 of `AGENTS.md`:
1. **Create Release Branch**: Create `release/v2.2.6` from `feature/epic14-polish-monetization`.
2. **Commit Changes**: All release-specific changes (versioning, documentation) will be committed to this branch.

### Phase 2: Artifact Archiving
Before generating new builds, rename existing artifacts for traceability:
- `app-debug.apk` → `debug-2.2.4-14.apk`
- `app-release.apk` → `release-2.2.4-14.apk`
- `app-release.aab` → `release-2.2.4-14.aab`

### Phase 3: Build Configuration & Execution
1. **[MODIFY] [build.gradle.kts](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/app/build.gradle.kts)**: Update `versionCode` to `15` and `versionName` to `"2.2.6"`.
2. **Generate Artifacts**:
   - `./gradlew assembleDebug` (Debug APK)
   - `./gradlew assembleRelease` (Release APK)
   - `./gradlew bundleRelease` (Release AAB)

### Phase 4: Documentation & Verification
1. **[MODIFY] [RELEASES.md](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/docs/RELEASES.md)**: Add entry for `v2.2.6`.
2. **[MODIFY] [AGENTS.md](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/AGENTS.md)**: Update current version and stable tag.
3. **Verify Production Readiness**: Complete all items in `docs/PRODUCTION_CHECKLIST.md`.
4. **Run Tests**: `./gradlew :app:testDebugUnitTest`.

### Phase 5: Merge & Tagging
1. **Merge to Master**: Merge `release/v2.2.6` into `master` via simulated Pull Request.
2. **Create Tag**: Tag the merge commit on `master` as `v2.2.6`.

## Verification Plan

### Automated Tests
- `./gradlew :app:testDebugUnitTest`

### Manual Verification
- Verify version strings in the generated AAB using `bundletool` or by checking the build logs.
- Verify the "About" screen in the new Debug APK.
