# Global Terminal (TRM) Audit Report

This report identifies the blockers preventing the Global Terminal (Leaderboard) from functioning and explains why player scores are not appearing.

## 1. Primary Blocker: Missing Google Web Client ID
The critical issue of the missing **Google Web Client ID** has been **RESOLVED**.

- **File**: `app/src/main/res/values/strings.xml`
- **Value**: `17868070038-om0v8iai7vq045rajlm7dc0fkdtlujc0.apps.googleusercontent.com` (Recovered via Firebase CLI)
- **Status**: ✅ FIXED
- **Impact**: Google Sign-In can now generate the required `idToken`, allowing Firebase Authentication to successfully link the Pilot ID.

## 2. UI Misinterpretation: "UPLINK SEVERED"
The "UPLINK SEVERED" message is currently shown whenever the pilot list is empty.

- **Issue**: This message appears both when there is a network/auth error **and** when the database is simply empty (which it is for a new project).
- **Confusion**: Since your scores aren't being submitted (due to Blocker #1), the database remains empty, triggering this "error" screen even if your internet connection is fine.

## 3. Implementation Gap: No Local Fallback
- **Issue**: The Terminal only tries to fetch remote scores. It does not show the player's own local high score if they are offline or if the database is empty.
- **Result**: The screen feels "dead" until the first successful global sync.

---

## Technical Findings

| Component | Status | Observation |
| :--- | :--- | :--- |
| **LoginManager** | 🟠 PARTIAL | Signs into Google locally, but Firebase Auth fails due to missing ID. |
| **LeaderboardManager**| 🔴 BLOCKED | `submitScore` returns early because `isOnline()` is false. |
| **Firestore** | ⚪ UNKNOWN | Collection `leaderboard` is likely empty or inaccessible due to Auth failure. |
| **GameEngine** | ✅ FIXED | Now calls `commitSessionStats` on all game-over states (including falling). |

---

## Required Action Plan

### 1. Configuration (User Action Required)
You MUST provide the real Web Client ID in `strings.xml`.
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Project Settings > General > Your Apps.
3. Download the latest `google-services.json` or find the "Web client ID" in the Google Sign-In configuration.
4. Replace `PLEASE_REPLACE_ME_WITH_REAL_ID` in `strings.xml`.

### 2. Code Enhancements (My Action)
I will implement the following to provide better feedback:
- **Pilot Status HUD**: Show "IDENTITY: LINKED" or "IDENTITY: LOCAL ONLY" in the Terminal.
- **Submission Feedback**: Add logging to the Terminal screen to show the last submission attempt result.
- **Local Entry**: Always show the player's own high score at the top of the list, even if remote sync fails.
- **Error Distinction**: Change "UPLINK SEVERED" to "NO DATA IN SECTOR" if the connection is fine but the list is empty.
