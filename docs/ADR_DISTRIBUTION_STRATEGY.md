# ADR: Distribution Strategy — Closed Beta via GitHub + Google Play

**Status:** Accepted  
**Date:** 2026-07-15  
**Version:** v1.5.2

## Context

Jump Droid v1.5.2 is the first closed beta release. We need a distribution strategy that:
- Enables reliable delivery of debug APK, release APK, and AAB to testers
- Does not expose the app to the general public on Google Play
- Keeps infrastructure overhead near zero

## Decision

We chose a three-artifact distribution model:

| Artifact | Format | Use Case |
|---|---|---|
| Debug APK | `.apk` | Internal dev testing, quick iteration |
| Release APK | `.apk` | Tester sideloading (GitHub Releases) |
| AAB | `.aab` | Google Play Internal Testing track |

### Google Play Track

- **Internal Testing** only — max 100 testers, no public listing
- Production track stays empty until v1.6.0 public launch
- No store listing, no search indexing

### GitHub Releases

- Release APK attached to every tagged release (e.g., `v1.5.2`)
- Debug APK optional, attached for convenience
- All releases link from https://jump-droid.vercel.app

### Signing

- All release artifacts signed with `jump_droid_release.keystore`
- Credentials sourced from environment variables or `keystore.properties` (gitignored)

## Consequences

### Positive
- Full control over tester access
- Zero cost distribution
- Easy rollback (just remove from Internal Testing)
- Clear audit trail via GitHub Releases

### Negative
- Manual upload to Google Play Console required (not CI-automated)
- Testers must opt into Google Play Internal Testing (email collection needed)

## Alternatives Considered

| Alternative | Reason Rejected |
|---|---|
| Public Play Store release | Premature — want controlled feedback first |
| Firebase App Distribution | Overkill for <100 testers; adds another dependency |
| Direct APK only | Misses Play Store testing signals (install metrics, crash reports) |
