# Release v2.2.4 Implementation Plan

The goal is to release the production APK for version v2.2.4 on GitHub. According to the project's constitutional documents (`AGENTS.md` and `docs/PRODUCTION_CHECKLIST.md`), a specific workflow must be followed to ensure stability and compliance.

## User Review Required

> [!IMPORTANT]
> **Production Checklist Status**: The `docs/PRODUCTION_CHECKLIST.md` for v2.2.4 currently has many unverified items (Firestore rules, Beta Analytics, Crashlytics validation, etc.). Proceeding with the release assumes these manual verifications have been completed by the developer.

> [!WARNING]
> **Branching Policy**: I will be creating a `release/v2.2.4` branch to perform the final build and merge, as committing directly to `master` is prohibited.

## Proposed Changes

### Release Preparation

#### [MODIFY] [PRODUCTION_CHECKLIST.md](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/docs/PRODUCTION_CHECKLIST.md)
Update the checklist to reflect the final release steps.

#### [NEW] release_notes.md (Temporary)
Create a temporary file containing the release notes extracted from `CHANGELOG.md` for use with the GitHub CLI.

### Build and Deployment

1.  **Branching**: Create `release/v2.2.4` from the current `development` branch.
2.  **Build**: Execute `./gradlew :app:assembleRelease` to generate the signed production APK.
3.  **Merge**: Prepare the merge into `master`.
4.  **Tagging**: Tag the release commit as `v2.2.4`.
5.  **GitHub Release**: Use `gh release create` to upload the APK to GitHub with the prepared notes.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleRelease` to ensure the build is successful and artifacts are generated.
- Verify the existence of `app/build/outputs/apk/release/app-release.apk`.

### Manual Verification
- The user should verify the live release on GitHub once the process is complete.
