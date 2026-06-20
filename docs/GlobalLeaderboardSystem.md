# Jump Droid - Global Leaderboard System
## Design Document
**Version:** 1.0  
**Date:** June 20, 2026  
**Status:** Draft / Ready for Implementation  
**Author:** Jump Droid Development Team  

---

## 1. Vision Statement
"To connect Jump Droid players worldwide through friendly competition, creating a community-driven experience where achievements are celebrated and every pilot's journey is recognized."

The Global Leaderboard System transforms Jump Droid from a solitary climbing experience into a connected community. Players can see how they rank against others, earn recognition for their skills, and feel motivated to push further. The system is designed to be inclusive, opt-in, and rewarding – never forced or paywalled.

---

## 2. Core Principles

| Principle | Description |
| :--- | :--- |
| **Earned, Not Bought** | Leaderboard access is unlocked by completing 3 missions – never by paying in-game cash. This feels like an achievement, not a paywall. |
| **Opt-In Participation** | Players can view the leaderboard freely. Publishing scores requires optional Google Sign-In – no forced accounts. |
| **Privacy First** | No personal data is stored beyond the player's chosen display name and their score. |
| **Read-Only by Default** | The leaderboard is always visible to everyone. Writing requires authentication to prevent spam. |
| **Community-Centric** | The leaderboard encourages friendly competition and gives players a reason to keep climbing. |

---

## 3. Player Experience (The Journey)

### 3.1 The New Player Experience

| Stage | What the Player Sees | What It Means |
| :--- | :--- | :--- |
| **1. Locked** | 🔒 "Complete 3 missions to unlock the Global Leaderboard" | The leaderboard is visible but locked – creates curiosity and a clear goal. |
| **2. Progress** | "2/3 missions complete" with visual progress bar | Clear feedback on how close they are to unlocking. |
| **3. Unlocked** | 🎉 "You've earned your place among the stars!" | A celebration moment – the player feels proud. |
| **4. Sign-In Prompt** | "Sign in with Google to publish your scores" | Optional step – feels like a privilege, not a requirement. |
| **5. Published** | "Your name now appears on the board!" | The player sees their name among others – instant gratification. |

### 3.2 The Experienced Player Experience

| Feature | Description |
| :--- | :--- |
| **View Rankings** | See top 100 players worldwide, sorted by score. |
| **Find My Rank** | See where you stand among the global community. |
| **Publish Scores** | Submit your best run to the leaderboard (requires sign-in). |
| **Track Progress** | See how your score compares to your previous best. |
| **Earn Rewards** | Complete the "World's Finest" mission for publishing your first score. |

### 3.3 Visual Flow Diagram

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                         PLAYER JOURNEY FLOW                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌───────────┐ │
│  │   New Player │───▶│  Complete 3  │───▶│  Unlock      │───▶│  View     │ │
│  │   Sees 🔒    │    │  Missions    │    │  Leaderboard │    │  Rankings │ │
│  └──────────────┘    └──────────────┘    └──────────────┘    └───────────┘ │
│                                                                             │
│         ┌─────────────────────────────────────────────────────────┐        │
│         │              OPTIONAL: SIGN IN WITH GOOGLE              │        │
│         └─────────────────────────────────────────────────────────┘        │
│                              │                                              │
│                              ▼                                              │
│         ┌─────────────────────────────────────────────────────────┐        │
│         │              PUBLISH YOUR SCORE                        │        │
│         │   "Your name now appears on the Global Leaderboard!"   │        │
│         └─────────────────────────────────────────────────────────┘        │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Functional Requirements

### 4.1 Mission Integration

| Requirement | Priority | Description |
| :--- | :--- | :--- |
| **Add MISSIONS_COMPLETED objective type** | High | Count completed missions for the unlock condition. |
| **Add leaderboard_unlock mission** | High | Complete 3 missions → Unlock leaderboard access. |
| **Add first_leaderboard_publish mission** | Medium | Publish first score → Earn rewards. |
| **Track scores published in GameStats** | Medium | Count how many times a player has published. |

### 4.2 Leaderboard Display

| Requirement | Priority | Description |
| :--- | :--- | :--- |
| **Fetch top 100 scores** | High | Read public Google Sheet CSV feed. |
| **Display rankings with ranks** | High | Show rank, player name, score, altitude. |
| **Highlight top 3** | Medium | Gold, silver, bronze styling. |
| **Show "My Rank"** | Medium | Highlight the player's own entry. |
| **Refresh button** | Low | Manual refresh of leaderboard data. |

### 4.3 Google Sign-In Integration

| Requirement | Priority | Description |
| :--- | :--- | :--- |
| **Google Sign-In button** | High | Appears only when leaderboard is unlocked. |
| **OAuth 2.0 authentication** | High | Secure token-based authentication. |
| **Display signed-in state** | High | Show "Signed in as [name]" in header. |
| **Sign-out functionality** | Medium | Allow players to sign out. |
| **Token refresh** | Low | Handle expired tokens gracefully. |

### 4.4 Score Publishing

| Requirement | Priority | Description |
| :--- | :--- | :--- |
| **Publish current best score** | High | Submit score to Google Sheets. |
| **Validate score** | High | Reject obviously fake scores (e.g., > 999999). |
| **Rate limiting** | Medium | Max 1 publish per hour per player. |
| **Duplicate prevention** | Low | Avoid publishing the same score twice. |
| **Error handling** | High | Show meaningful errors (network, auth, etc.). |

---

## 5. Non-Functional Requirements

| Requirement | Priority | Description |
| :--- | :--- | :--- |
| **Performance** | High | Leaderboard loads in < 2 seconds. |
| **Security** | High | OAuth 2.0 for writes; no API keys exposed. |
| **Offline Support** | Medium | Show cached leaderboard when offline. |
| **Scalability** | Low | Google Sheets handles up to 10,000 entries. |
| **Accessibility** | Medium | Screen reader support for leaderboard UI. |
| **Localization** | Low | Future support for multiple languages. |

---

## 6. System Architecture

### 6.1 Component Diagram

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                         SYSTEM ARCHITECTURE                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         PLAYER DEVICE                               │   │
│  ├─────────────────────────────────────────────────────────────────────┤   │
│  │                                                                     │   │
│  │  ┌─────────────────┐   ┌─────────────────────────────────────────┐ │   │
│  │  │  MissionManager │──▶│  "Path to Glory" (Complete 3 missions)  │ │   │
│  │  │  (Existing)     │   │  → unlocks leaderboard_access            │ │   │
│  │  └─────────────────┘   └─────────────────────────────────────────┘ │   │
│  │                                                                     │   │
│  │  ┌─────────────────────┐   ┌─────────────────────────────────────┐ │   │
│  │  │  GoogleSignInManager │──▶│  OAuth 2.0 Token (optional)        │ │   │
│  │  │  (New)              │   │  Only if player signs in             │ │   │
│  │  └─────────────────────┘   └─────────────────────────────────────┘ │   │
│  │                                                                     │   │
│  │  ┌─────────────────────────────────────────────────────────────────┐ │   │
│  │  │  LeaderboardManager (New)                                      │ │   │
│  │  │  ├── displayTopScores()  ← Always works (public read)         │ │   │
│  │  │  ├── publishScore()      ← Only if signed in                 │ │   │
│  │  │  └── isLeaderboardUnlocked() ← Check mission completion      │ │   │
│  │  └─────────────────────────────────────────────────────────────────┘ │   │
│  │                                                                     │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      GOOGLE SHEETS                                 │   │
│  ├─────────────────────────────────────────────────────────────────────┤   │
│  │  ┌─────────────────────────────────────────────────────────────────┐ │   │
│  │  │  "Leaderboard" Sheet (public read, write with OAuth)           │ │   │
│  │  │  ┌──────────┬──────────┬──────────┬───────────────────────┐   │ │   │
│  │  │  │ player   │ score    │ altitude │ timestamp             │   │ │   │
│  │  │  ├──────────┼──────────┼──────────┼───────────────────────┤   │ │   │
│  │  │  │ "Alex"   │ 12500    │ 8200m    │ 2026-06-20 14:30:00  │   │ │   │
│  │  │  │ "Jordan" │ 9800     │ 6500m    │ 2026-06-19 10:15:00  │   │ │   │
│  │  │  │ "Sam"    │ 7200     │ 4800m    │ 2026-06-18 22:00:00  │   │ │   │
│  │  └──────────┴──────────┴──────────┴───────────────────────┘   │ │   │
│  │  └─────────────────────────────────────────────────────────────────┘ │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 6.2 Data Flow

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                         DATA FLOW                                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ✅ READ PATH (Public)                                                     │
│  ┌─────────────┐    ┌─────────────────┐    ┌──────────────────────────┐   │
│  │  Player     │───▶│  Leaderboard    │───▶│  Google Sheets (CSV)     │   │
│  │  Device     │    │  Manager        │    │  Published to Web        │   │
│  └─────────────┘    └─────────────────┘    └──────────────────────────┘   │
│                                                                             │
│  🔒 WRITE PATH (OAuth Required)                                            │
│  ┌─────────────┐    ┌─────────────────┐    ┌──────────────────────────┐   │
│  │  Player     │───▶│  Google Sign-In │───▶│  OAuth 2.0 Token        │   │
│  │  Device     │    │  (Optional)     │    │                          │   │
│  └─────────────┘    └─────────────────┘    └───────────┬──────────────┘   │
│                                                         │                  │
│                                                         ▼                  │
│                              ┌──────────────────────────────────────────┐   │
│                              │  Google Sheets API (Append)             │   │
│                              │  Writes: Player, Score, Altitude, Time │   │
│                              └──────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 7. Implementation Plan

### Phase 1: Mission System Updates
| Task | File | Description |
| :--- | :--- | :--- |
| **1.1** | `Mission.kt` | Add `MISSIONS_COMPLETED` to `ObjectiveType` |
| **1.2** | `Mission.kt` | Add `UNLOCK_LEADERBOARD` to `UnlockType` |
| **1.3** | `Mission.kt` | Add `COMMUNITY` to `MissionCategory` |
| **1.4** | `Mission.kt` | Add `scoresPublished` to `GameStats` |
| **1.5** | `MissionRegistry.kt` | Add "Path to Glory" mission |
| **1.6** | `MissionRegistry.kt` | Add "World's Finest" mission |
| **1.7** | `MissionManager.kt` | Update `calculateProgress()` for `MISSIONS_COMPLETED` |
| **1.8** | `MissionManager.kt` | Add `updateScorePublished()` method |

### Phase 2: Leaderboard Core Implementation
| Task | File | Description |
| :--- | :--- | :--- |
| **2.1** | `LeaderboardModels.kt` | Create data classes (`LeaderboardEntry`, `LeaderboardPublishResult`) |
| **2.2** | `GoogleSignInManager.kt` | Implement OAuth 2.0 sign-in flow |
| **2.3** | `GoogleSheetsApi.kt` | Implement public CSV read |
| **2.4** | `GoogleSheetsApi.kt` | Implement OAuth-protected write |
| **2.5** | `LeaderboardManager.kt` | Implement core logic (unlock, fetch, publish) |
| **2.6** | `LeaderboardManager.kt` | Add state management (`Flow`) |

### Phase 3: UI Implementation
| Task | File | Description |
| :--- | :--- | :--- |
| **3.1** | `LeaderboardScreen.kt` | Build locked state UI |
| **3.2** | `LeaderboardScreen.kt` | Build unlocked state UI |
| **3.3** | `LeaderboardScreen.kt` | Build sign-in/publish UI |
| **3.4** | `LeaderboardScreen.kt` | Add loading and error states |
| **3.5** | `Integration` | Connect to navigation |

### Phase 4: Google Cloud Setup
| Task | Description |
| :--- | :--- |
| **4.1** | Create Google Cloud Project |
| **4.2** | Enable Google Sheets API |
| **4.3** | Create OAuth 2.0 credentials (Android) |
| **4.4** | Get Web Client ID |
| **4.5** | Add to `strings.xml` |
| **4.6** | Create and publish Google Sheet |
| **4.7** | Add Sheet ID to `GoogleSheetsApi.kt` |

---

## 8. Technical Decisions & Rationale

### 8.1 Why Google Sheets?
| Alternative | Decision | Rationale |
| :--- | :--- | :--- |
| **Firebase** | ❌ Rejected | Adds complexity and cost for a simple leaderboard. |
| **Custom Backend**| ❌ Rejected | Overkill for MVP; requires server maintenance. |
| **Google Sheets** | ✅ Selected | Free, simple, supports public read + OAuth write. |

### 8.2 Why Google Sign-In?
| Alternative | Decision | Rationale |
| :--- | :--- | :--- |
| **Anonymous** | ❌ Rejected | No way to prevent spam or identify players. |
| **Custom Auth** | ❌ Rejected | Building auth from scratch is error-prone. |
| **Google Sign-In**| ✅ Selected | Trusted, secure, familiar to players. |

### 8.3 Why Unlock with Missions (Not Cash)?
| Alternative | Decision | Rationale |
| :--- | :--- | :--- |
| **Pay Cash** | ❌ Rejected | "Pay to achieve" feels exploitative. |
| **Complete Missions**| ✅ Selected | Earned through gameplay; teaches mechanics. |

### 8.4 Why Read CSV (Not API)?
| Alternative | Decision | Rationale |
| :--- | :--- | :--- |
| **Sheets API (Read)**| ❌ Rejected | Requires authentication; more complex. |
| **Published CSV** | ✅ Selected | Zero auth, fast, simple. |

---

## 9. Anti-Abuse Measures

| Measure | Implementation | Why |
| :--- | :--- | :--- |
| **OAuth Required** | Google Sign-In | Prevents anonymous spam. |
| **Score Validation** | Max score limit | Rejects impossible scores (e.g., 999,999). |
| **Rate Limiting** | 1 publish/hour | Prevents score spamming. |
| **Duplicate Prev.** | Check last score | Avoids flooding the sheet. |
| **Manual Mod.** | Sheet Owner | Last line of defence. |

---

## 10. Success Metrics

| Metric | Target | How to Measure |
| :--- | :--- | :--- |
| **Unlock Rate** | > 60% of players | Track `leaderboard_unlock` mission completion. |
| **Sign-In Rate** | > 30% of unlocked | Track Google Sign-In events. |
| **Publish Rate** | > 20% of unlocked | Track `publishScore()` calls. |
| **Leaderboard Views**| > 50% of players | Track screen visits. |
| **Mission Comp.** | > 80% complete "Path to Glory" | Track mission progress. |

---

## 11. Future Enhancements
- **Friends Leaderboard**: See only friends' scores.
- **Seasonal Rankings**: Reset leaderboard periodically.
- **Regional Leaderboards**: NA, EU, Asia, etc.
- **Achievement Badges**: Badges for top 10, top 100, etc.
- **Score History**: Track player's score over time.

---

## 12. Glossary
- **OAuth 2.0**: Open standard for access delegation (Google Sign-In).
- **CSV**: Comma-Separated Values (text format for leaderboard data).
- **Mission**: A goal in the game that rewards the player.
- **Leaderboard**: A ranked list of players by score.
- **Opt-In**: The player must choose to participate.
- **Payload**: The data sent in a network request.

---

## 13. Appendix: Google Sheet Configuration

### Sheet Structure
| Column | Type | Description |
| :--- | :--- | :--- |
| **A: Player** | String | Player's display name. |
| **B: Score** | Number | Player's score (higher is better). |
| **C: Altitude** | Number | Altitude reached during the run. |
| **D: Timestamp** | String | ISO-8601 or Unix timestamp. |

### Publishing Settings
```text
File > Share > Publish to web
→ Select sheet: "Leaderboard"
→ Format: "Comma-separated values (.csv)"
→ Click "Publish"
→ Copy the generated URL
```

### Sheet ID Extraction
```text
URL: https://docs.google.com/spreadsheets/d/YOUR_SHEET_ID/edit
                                            ▲
                                            └─── Copy this part
```

---

## 14. Change Log
| Version | Date | Author | Changes |
| :--- | :--- | :--- | :--- |
| **1.0** | June 20, 2026 | Jump Droid Team | Initial creation. |

---
**End of Document**
