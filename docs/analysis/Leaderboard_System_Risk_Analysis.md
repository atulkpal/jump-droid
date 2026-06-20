# Jump Droid: Global Leaderboard System - Risk Analysis

This report analyzes the security, technical, and gameplay risks associated with the proposed Google Sheets-based leaderboard system.

---

## 1. Security & Integrity Risks

### Vulnerability to Cheating (High Risk)
*   **The Issue:** The system uses Google Sheets as a database. While writing requires OAuth 2.0, the actual *payload* (the score) is generated on the client device.
*   **The Danger:** Malicious players can easily decompile the APK, find the Sheets API endpoints, and send fake scores directly using their own valid OAuth tokens.
*   **Mitigation:** Implementation of server-side validation is impossible with Google Sheets alone. You should implement basic **Score Checksums** (hashing the score with a secret salt) to make it harder to spoof requests.

### OAuth Token Management (Medium Risk)
*   **The Issue:** The design relies on client-side OAuth management.
*   **The Danger:** If access tokens are logged or cached insecurely on the device, they could be intercepted.
*   **Mitigation:** Ensure `GoogleSignInOptions` are configured with `requestIdToken` and use secure storage for any sensitive session data.

---

## 2. Technical & Operational Risks

### Scalability Limits (High Risk)
*   **The Issue:** The design targets 10,000 entries for Google Sheets.
*   **The Danger:** Google Sheets performance degrades drastically after ~50k rows. Additionally, "Publish to Web" (the CSV method) can have a delay of several minutes before updates appear.
*   **Mitigation:** This is acceptable for a "MVP" (Minimum Viable Product), but if the game gains popularity, a migration to Firebase Realtime Database or a custom SQL backend will be required.

### Concurrency Issues (Medium Risk)
*   **The Issue:** Multiple players writing to the same sheet simultaneously.
*   **The Danger:** The Google Sheets `append` API is generally atomic, but high-frequency writes can hit Project Quotas (40-60 requests per minute).
*   **Mitigation:** Implement client-side "Back-off" logic (wait and retry) and only allow one publish per run to reduce traffic.

---

## 3. Gameplay & UX Risks

### Demotivation (Medium Risk)
*   **The Issue:** Top-tier players dominating the board.
*   **The Danger:** New players might see impossible-looking scores and feel they can never compete.
*   **Mitigation:** Implement **Seasonal Resets** and "My Rank" highlighting so players focus on their relative position rather than just the global #1.

### Google Sign-In Friction (Low Risk)
*   **The Issue:** Forcing a login.
*   **The Danger:** Players are often wary of "Sign in with Google" prompts in casual games.
*   **Mitigation:** The current design correctly makes this **Opt-In** and **Read-Only by default**. This significantly lowers the risk of player abandonment.

---

## 4. Implementation Readiness Audit
Currently, the project is **not yet ready** for this system:
1.  **Missing Dependencies:** `Retrofit`, `OkHttp`, and `Play Services Auth` are not in `app/build.gradle.kts`.
2.  **Placeholder State:** The current `LeaderboardScreen.kt` is a simple "Offline" placeholder.
3.  **Permissions:** `INTERNET` permission must be added to `AndroidManifest.xml`.

### Final Recommendation
The system is **well-designed for a small-scale community game**, but highly vulnerable to high-score spoofing. If competitive integrity is critical, Google Sheets should eventually be replaced with a backend capable of verifying gameplay logic.
