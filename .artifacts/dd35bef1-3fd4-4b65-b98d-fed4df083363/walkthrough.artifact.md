# Walkthrough - Versioning Protocol Hardening

I have permanently hardened the versioning rules in the project's constitutional manual to ensure no future agent makes unauthorized changes to the build configuration.

## Changes Made

### 1. Hardened Manual (`AGENTS.md`)
- **New Section 12**: Inserted a dedicated **"Versioning Protocol (CRITICAL)"** section.
- **High-Visibility Caution**: Added a large `[!CAUTION]` block that explicitly prohibits modifying `versionCode` or `versionName` in `build.gradle.kts` without verbatim user approval.
- **Explicit Rules**:
    - **No Assumption**: Logic or documentation updates do not imply a version change is needed.
    - **Sync Prohibition**: Automatic synchronization between logs and code is strictly forbidden.
    - **State-Reporting-Only**: Mismatches must be reported to the user as a status update.
    - **Approval Gating**: Requires a "Wait for Proceed" flow for every version touch.

## Verification Results

### Manual Verification
- **AGENTS.md Audit**: Verified that the new section is prominent and the language is unambiguous. Any agent onboarding will now encounter this rule as a primary operational standard.
