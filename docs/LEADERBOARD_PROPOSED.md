# JUMP DROID - LEADERBOARD SYSTEM
## Complete Documentation + Codebase

### File Structure:
- **LeaderboardManager.kt** - Core logic (read/write/unlock)
- **GoogleSignInManager.kt** - OAuth 2.0 for Google Sign-In
- **GoogleSheetsApi.kt** - Retrofit + Sheets API integration
- **LeaderboardScreen.kt** - Compose UI (locked/unlocked states)
- **LeaderboardModels.kt** - Data classes for entries

### Mission System Integration:
- **Mission.kt (updated)** - Add `MISSIONS_COMPLETED`, `UNLOCK_LEADERBOARD`
- **MissionRegistry.kt (updated)** - "Path to Glory" unlock mission

---

## PART 1: MISSION SYSTEM UPDATES

### FILE: Mission.kt (ADDITIONS)
Add these new enum values to your existing `Mission.kt`.

#### 🔽 ADD TO ObjectiveType (inside Mission.kt)
Find the existing `ObjectiveType` enum and add this line:

```kotlin
enum class ObjectiveType {
    // ... existing values ...
    TOTAL_FLIGHT_TIME,
    TOTAL_PLATFORM_TIME,
    ZERO_HEAT_TIME,
    // ... etc ...
    
    MISSIONS_COMPLETED   // 🔢 Count of completed missions (for leaderboard unlock)
}
```

#### 🔽 ADD TO UnlockType (inside Mission.kt)
Find the existing `UnlockType` enum and add this line:

```kotlin
enum class UnlockType {
    REACH_ALTITUDE,
    DEFEAT_BOSS,
    COMPLETE_MISSION,
    UNLOCK_CODEX_ENTRY,
    REACH_BIOME,
    COLLECT_ARTIFACT,
    UNLOCK_LEADERBOARD  // 🏆 For leaderboard access unlock
}
```

#### 🔽 ADD TO MissionCategory (optional - for grouping)
Find the existing `MissionCategory` enum and add:

```kotlin
enum class MissionCategory {
    // ... existing values ...
    FLIGHT_TIME,
    PLATFORM_STAY,
    // ... etc ...
    
    COMMUNITY  // 👥 For leaderboard-related missions
}
```

### FILE: MissionRegistry.kt (ADD THIS MISSION)
Add this mission to your `MissionRegistry.getAllMissions()` list.

```kotlin
// Add to MissionRegistry.getAllMissions():

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
//  LEADERBOARD UNLOCK MISSION (Path to Glory)
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Mission(
    id = "leaderboard_unlock",
    category = MissionCategory.DISCOVERY_HUNTER,
    tier = MissionTier.TIER_1,
    name = "Path to Glory",
    description = "Complete 3 missions to unlock the Global Leaderboard",
    objective = MissionObjective(
        type = ObjectiveType.MISSIONS_COMPLETED,
        targetValue = 3f
    ),
    rewards = Rewards(
        cash = 100,
        unlockable = "leaderboard_access"
    )
),

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
//  FIRST PUBLISH MISSION (Optional - rewards for publishing)
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Mission(
    id = "first_leaderboard_publish",
    category = MissionCategory.COMMUNITY,
    tier = MissionTier.TIER_1,
    name = "World's Finest",
    description = "Publish your first score to the global leaderboard",
    objective = MissionObjective(
        type = ObjectiveType.SCORE_PUBLISHED,
        targetValue = 1f
    ),
    rewards = Rewards(
        cash = 200,
        rocketSkin = "skin_community_champion",
        codexEntry = "codex_leaderboard_initiate"
    ),
    unlockCondition = UnlockCondition(
        type = UnlockType.UNLOCK_LEADERBOARD,
        value = 0f
    )
)
```

### FILE: MissionManager.kt (UPDATE calculateProgress())
Add this case to your `calculateProgress()` when expression.

```kotlin
// Inside MissionManager.calculateProgress(), add this case:

private fun calculateProgress(
    mission: Mission,
    playerState: PlayerState,
    gameStats: GameStats
): Float {
    return when (mission.objective.type) {
        // ... existing cases ...
        ObjectiveType.TOTAL_FLIGHT_TIME -> gameStats.totalFlightTime
        ObjectiveType.MAX_COMBO -> gameStats.maxCombo.toFloat()
        // ... etc ...
        
        ObjectiveType.MISSIONS_COMPLETED -> {
            // Count how many missions are COMPLETED or CLAIMED
            _missionStates.value.count { 
                it.value == MissionState.COMPLETED || it.value == MissionState.CLAIMED 
            }.toFloat()
        }
        
        ObjectiveType.SCORE_PUBLISHED -> {
            // This is incremented when publishScore() is called
            gameStats.scoresPublished.toFloat()
        }
    }
}

// Also add to GameStats data class (in Mission.kt):
data class GameStats(
    // ... existing fields ...
    val scoresPublished: Int = 0
)
```

---

## PART 2: LEADERBOARD DATA MODELS

### FILE: LeaderboardModels.kt
Data classes for leaderboard entries.

```kotlin
// LeaderboardModels.kt
package com.jumpdroid.leaderboard

data class LeaderboardEntry(
    val player: String,
    val score: Int,
    val altitude: Float,
    val timestamp: Long,
    val rank: Int = 0
)

data class LeaderboardConfig(
    val sheetId: String = "your_sheet_id_here",
    val sheetName: String = "Leaderboard",
    val maxEntries: Int = 100
)

data class LeaderboardPublishResult(
    val success: Boolean,
    val message: String,
    val entry: LeaderboardEntry? = null,
    val error: String? = null
)
```

---

## PART 3: GOOGLE SIGN-IN MANAGER

### FILE: GoogleSignInManager.kt
Manages Google Sign-In state for leaderboard publishing.

```kotlin
// GoogleSignInManager.kt
package com.jumpdroid.leaderboard

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class AuthState {
    object SIGNED_OUT : AuthState()
    object LOADING : AuthState()
    data class SIGNED_IN(val account: GoogleSignInAccount) : AuthState()
    data class ERROR(val message: String) : AuthState()
}

class GoogleSignInManager(
    private val activity: Activity
) {
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            .requestEmail()
            .requestId()
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .build()
        GoogleSignIn.getClient(activity, gso)
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.SIGNED_OUT)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // Check if already signed in
        val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(activity)
        if (lastSignedInAccount != null) {
            _authState.value = AuthState.SIGNED_IN(lastSignedInAccount)
        }
    }

    /**
     * Starts the Google Sign-In flow
     */
    fun signIn(): Intent {
        _authState.value = AuthState.LOADING
        return googleSignInClient.signInIntent
    }

    /**
     * Handles the result from the Sign-In intent
     */
    fun handleSignInResult(result: Intent?): Boolean {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result)
            val account = task.getResult(ApiException::class.java)
            _authState.value = AuthState.SIGNED_IN(account)
            true
        } catch (e: ApiException) {
            _authState.value = AuthState.ERROR(e.message ?: "Sign-in failed")
            false
        }
    }

    /**
     * Signs out the current user
     */
    fun signOut() {
        googleSignInClient.signOut()
        _authState.value = AuthState.SIGNED_OUT
    }

    fun isSignedIn(): Boolean {
        return _authState.value is AuthState.SIGNED_IN
    }

    fun getAccessToken(): String? {
        return (_authState.value as? AuthState.SIGNED_IN)?.account?.idToken
    }

    fun getDisplayName(): String {
        return (_authState.value as? AuthState.SIGNED_IN)?.account?.displayName ?: "Player"
    }

    fun getEmail(): String? {
        return (_authState.value as? AuthState.SIGNED_IN)?.account?.email
    }

    fun getPhotoUrl(): String? {
        return (_authState.value as? AuthState.SIGNED_IN)?.account?.photoUrl?.toString()
    }

    fun getUserId(): String? {
        return (_authState.value as? AuthState.SIGNED_IN)?.account?.id
    }
}
```

**Note:** Add this to your `strings.xml`:
`<string name="default_web_client_id">YOUR_WEB_CLIENT_ID_HERE</string>`
Get this from Google Cloud Console > Credentials > OAuth 2.0 Client ID (Web application)

---

## PART 4: GOOGLE SHEETS API

### FILE: GoogleSheetsApi.kt
Reads publicly (no auth) and writes (with OAuth token).

```kotlin
// GoogleSheetsApi.kt
package com.jumpdroid.leaderboard

import android.content.Context
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import com.google.android.gms.auth.api.signin.GoogleSignIn
import java.io.IOException

object GoogleSheetsApi {
    private const val SHEET_ID = "YOUR_SHEET_ID_HERE"  // Replace with your Google Sheet ID
    private const val SHEET_NAME = "Leaderboard"
    private const val RANGE = "$SHEET_NAME!A:D"  // Player, Score, Altitude, Timestamp

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    //  ✅ PUBLIC READ - No Authentication Required
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * Fetch top scores using the public CSV endpoint.
     * This works because the sheet is published to the web.
     */
    suspend fun getTopScores(limit: Int = 100): List<LeaderboardEntry> {
        return try {
            val url = "https://docs.google.com/spreadsheets/d/$SHEET_ID/gviz/tq?tqx=out:csv&sheet=$SHEET_NAME"
            val response = fetchCsv(url)
            parseCsvToEntries(response).take(limit)
        } catch (e: Exception) {
            // Return empty list on error (network issue, sheet not published, etc.)
            emptyList()
        }
    }

    private fun fetchCsv(url: String): String {
        // Use OkHttp or Retrofit to fetch the CSV
        // This is a placeholder - you'll implement this with your networking library
        return """
Player,Score,Altitude,Timestamp
Alex,12500,8200,2026-06-20 14:30:00
Jordan,9800,6500,2026-06-19 10:15:00
Sam,7200,4800,2026-06-18 22:00:00
        """.trimIndent()
    }

    private fun parseCsvToEntries(csv: String): List<LeaderboardEntry> {
        val lines = csv.split("\n")
        if (lines.size < 2) return emptyList()

        return lines.drop(1) // Skip header
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = line.split(",")
                if (parts.size >= 4) {
                    try {
                        LeaderboardEntry(
                            player = parts[0].trim(),
                            score = parts[1].trim().toIntOrNull() ?: 0,
                            altitude = parts[2].trim().toFloatOrNull() ?: 0f,
                            timestamp = parseTimestamp(parts[3].trim())
                        )
                    } catch (e: Exception) {
                        null
                    }
                } else null
            }
            .sortedByDescending { it.score }
    }

    private fun parseTimestamp(dateStr: String): Long {
        // Simple timestamp parsing - you can use SimpleDateFormat or ThreeTenABP
        return System.currentTimeMillis()
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    //  🔒 WRITE - Requires OAuth 2.0 Authentication
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * Appends a new score to the leaderboard sheet.
     * Requires a valid OAuth access token from GoogleSignIn.
     */
    suspend fun appendScore(
        context: Context,
        token: String,
        player: String,
        score: Int,
        altitude: Float
    ): LeaderboardPublishResult {
        return try {
            // Create credential from token
            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                listOf("https://www.googleapis.com/auth/spreadsheets")
            ).apply {
                // We need to use the token directly
                // In practice, we use the GoogleSignInAccount to get the credential
            }

            // Build the Sheets service
            val sheetsService = Sheets.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            ).setApplicationName("Jump Droid").build()

            // Prepare the data to append
            val valueRange = ValueRange().apply {
                values = listOf(
                    listOf(
                        player,
                        score.toString(),
                        altitude.toString(),
                        System.currentTimeMillis().toString()
                    )
                )
            }

            // Execute the append
            val request = sheetsService.spreadsheets().values()
                .append(SHEET_ID, RANGE, valueRange)
            request.valueInputOption = "RAW"

            val response = request.execute()
            val updatedRange = response.updates?.updatedRange ?: "Unknown"
            val updatedRows = response.updates?.updatedRows ?: 0

            LeaderboardPublishResult(
                success = true,
                message = "Score published! $updatedRows row(s) updated",
                entry = LeaderboardEntry(
                    player = player,
                    score = score,
                    altitude = altitude,
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (e: IOException) {
            LeaderboardPublishResult(
                success = false,
                message = "Network error",
                error = e.message
            )
        } catch (e: Exception) {
            LeaderboardPublishResult(
                success = false,
                message = "Failed to publish score",
                error = e.message
            )
        }
    }
}
```

### RETROFIT INTERFACE (Alternative: Use Retrofit for cleaner code)

```kotlin
interface LeaderboardApi {
    @GET("gviz/tq?tqx=out:csv")
    suspend fun getScores(@Query("sheet") sheetName: String = "Leaderboard"): ResponseBody
}

object RetrofitInstance {
    private const val BASE_URL = "https://docs.google.com/spreadsheets/d/YOUR_SHEET_ID_HERE/"
    
    val api: LeaderboardApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(LeaderboardApi::class.java)
    }
}
```

---

## PART 5: LEADERBOARD MANAGER

### FILE: LeaderboardManager.kt
Core logic: read, write, unlock, and state management.

```kotlin
// LeaderboardManager.kt
package com.jumpdroid.leaderboard

import android.content.Context
import com.jumpdroid.missions.MissionManager
import com.jumpdroid.missions.MissionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

sealed class LeaderboardState {
    object LOCKED : LeaderboardState()
    object UNLOCKED : LeaderboardState()
    data class LOADING(val progress: Int = 0) : LeaderboardState()
    data class ERROR(val message: String) : LeaderboardState()
}

class LeaderboardManager(
    private val context: Context,
    private val missionManager: MissionManager,
    private val googleSignInManager: GoogleSignInManager
) {
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    //  State
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private val _uiState = MutableStateFlow<LeaderboardState>(LeaderboardState.LOADING())
    val uiState: StateFlow<LeaderboardState> = _uiState.asStateFlow()

    private val _topScores = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val topScores: StateFlow<List<LeaderboardEntry>> = _topScores.asStateFlow()

    private val _myEntry = MutableStateFlow<LeaderboardEntry?>(null)
    val myEntry: StateFlow<LeaderboardEntry?> = _myEntry.asStateFlow()

    private val _publishResult = MutableStateFlow<LeaderboardPublishResult?>(null)
    val publishResult: StateFlow<LeaderboardPublishResult?> = _publishResult.asStateFlow()

    private var isRefreshing = false

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    //  Access Control - Unlock System
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * Check if the player has unlocked leaderboard access.
     * This is tied to the "Path to Glory" mission.
     */
    fun isLeaderboardAccessUnlocked(): Boolean {
        return missionManager.isMissionComplete("leaderboard_unlock")
    }

    /**
     * Get the progress toward unlocking the leaderboard.
     * Returns the number of completed missions (0-3).
     */
    fun getUnlockProgress(): Int {
        val mission = missionManager.getMission("leaderboard_unlock")
        return if (mission != null) {
            missionManager.getProgress(mission.id).toInt()
        } else 0
    }

    /**
     * Get the total required missions to unlock (always 3).
     */
    fun getUnlockTotal(): Int = 3

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    //  Read - Fetches leaderboard data
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * Refresh the leaderboard data from Google Sheets.
     * This works even if the player is not signed in.
     */
    suspend fun refresh() {
        if (isRefreshing) return
        isRefreshing = true

        try {
            _uiState.value = LeaderboardState.LOADING()

            // Fetch top scores
            val scores = GoogleSheetsApi.getTopScores(100)
            _topScores.value = scores

            // If signed in, fetch my entry
            if (googleSignInManager.isSignedIn()) {
                val myEntry = scores.find { it.player == googleSignInManager.getDisplayName() }
                _myEntry.value = myEntry
            }

            // Update UI state
            _uiState.value = if (isLeaderboardAccessUnlocked()) {
                LeaderboardState.UNLOCKED
            } else {
                LeaderboardState.LOCKED
            }
        } catch (e: Exception) {
            _uiState.value = LeaderboardState.ERROR(e.message ?: "Failed to load leaderboard")
        } finally {
            isRefreshing = false
        }
    }

    /**
     * Get a specific entry by player name.
     */
    fun getPlayerEntry(playerName: String): LeaderboardEntry? {
        return _topScores.value.find { it.player == playerName }
    }

    /**
     * Get the rank of a specific score.
     */
    fun getRank(score: Int): Int {
        return _topScores.value
            .sortedByDescending { it.score }
            .indexOfFirst { it.score >= score } + 1
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    //  Write - Requires Google Sign-In
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * Publish a new score to the leaderboard.
     * Requirements:
     * 1. Leaderboard must be unlocked (Path to Glory mission)
     * 2. Player must be signed in with Google
     */
    suspend fun publishScore(
        score: Int,
        altitude: Float
    ): LeaderboardPublishResult {
        // Check conditions
        if (!isLeaderboardAccessUnlocked()) {
            return LeaderboardPublishResult(
                success = false,
                message = "Complete the 'Path to Glory' mission first!",
                error = "LEADERBOARD_LOCKED"
            )
        }

        if (!googleSignInManager.isSignedIn()) {
            return LeaderboardPublishResult(
                success = false,
                message = "Sign in with Google to publish your score",
                error = "NOT_SIGNED_IN"
            )
        }

        val token = googleSignInManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            return LeaderboardPublishResult(
                success = false,
                message = "Authentication error. Please sign in again.",
                error = "INVALID_TOKEN"
            )
        }

        // Publish to Google Sheets
        val result = GoogleSheetsApi.appendScore(
            context = context,
            token = token,
            player = googleSignInManager.getDisplayName(),
            score = score,
            altitude = altitude
        )

        // Update local state
        _publishResult.value = result
        if (result.success && result.entry != null) {
            _myEntry.value = result.entry
            // Add to local list if not already present
            val updatedScores = (_topScores.value + result.entry)
                .sortedByDescending { it.score }
                .distinctBy { it.player }
                .take(100)
            _topScores.value = updatedScores

            // Trigger mission completion for "World's Finest"
            missionManager.updateScorePublished()
        }

        return result
    }

    /**
     * Get the player's current best score from the leaderboard.
     */
    fun getMyBestScore(): LeaderboardEntry? {
        return _myEntry.value
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    //  UI Helpers
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    fun shouldShowSignInButton(): Boolean {
        return isLeaderboardAccessUnlocked() && !googleSignInManager.isSignedIn()
    }

    fun shouldShowPublishButton(): Boolean {
        return isLeaderboardAccessUnlocked() && googleSignInManager.isSignedIn()
    }
}
```

---

## PART 6: LEADERBOARD UI SCREEN

### FILE: LeaderboardScreen.kt
Compose UI for the leaderboard with locked/unlocked states.

```kotlin
// LeaderboardScreen.kt
package com.jumpdroid.leaderboard.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jumpdroid.leaderboard.*
import com.jumpdroid.missions.MissionManager
import kotlinx.coroutines.launch

@Composable
fun LeaderboardScreen(
    leaderboardManager: LeaderboardManager,
    missionManager: MissionManager,
    googleSignInManager: GoogleSignInManager,
    onNavigateToMissions: () -> Unit
) {
    val uiState by leaderboardManager.uiState.collectAsStateWithLifecycle()
    val topScores by leaderboardManager.topScores.collectAsStateWithLifecycle()
    val isUnlocked = leaderboardManager.isLeaderboardAccessUnlocked()
    val unlockProgress = leaderboardManager.getUnlockProgress()
    val unlockTotal = leaderboardManager.getUnlockTotal()
    val authState by googleSignInManager.authState.collectAsStateWithLifecycle()
    val isSignedIn = authState is AuthState.SIGNED_IN

    val coroutineScope = rememberCoroutineScope()

    // Sign-in launcher
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        googleSignInManager.handleSignInResult(result.data)
        // Refresh after sign-in
        coroutineScope.launch {
            leaderboardManager.refresh()
        }
    }

    // Refresh on first load
    LaunchedEffect(Unit) {
        leaderboardManager.refresh()
    }

    // Refresh when auth state changes
    LaunchedEffect(authState) {
        if (authState is AuthState.SIGNED_IN) {
            leaderboardManager.refresh()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A1A))
            .padding(16.dp)
    ) {
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        //  HEADER
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        LeaderboardHeader(
            isUnlocked = isUnlocked,
            isSignedIn = isSignedIn,
            authState = authState,
            onSignIn = {
                val intent = googleSignInManager.signIn()
                signInLauncher.launch(intent)
            },
            onSignOut = { googleSignInManager.signOut() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        //  CONTENT
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        when (uiState) {
            is LeaderboardState.LOADING -> {
                LoadingState()
            }
            is LeaderboardState.ERROR -> {
                ErrorState(
                    message = (uiState as LeaderboardState.ERROR).message,
                    onRetry = { coroutineScope.launch { leaderboardManager.refresh() } }
                )
            }
            is LeaderboardState.UNLOCKED -> {
                LeaderboardContent(
                    topScores = topScores,
                    isSignedIn = isSignedIn,
                    onPublishScore = {
                        coroutineScope.launch {
                            // In production, use the player's current high score
                            val score = 5000 // Placeholder
                            val altitude = 3000f // Placeholder
                            leaderboardManager.publishScore(score, altitude)
                        }
                    }
                )
            }
            is LeaderboardState.LOCKED -> {
                LeaderboardLockedContent(
                    progress = unlockProgress,
                    total = unlockTotal,
                    onGoToMissions = onNavigateToMissions
                )
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
//  HEADER COMPONENT
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun LeaderboardHeader(
    isUnlocked: Boolean,
    isSignedIn: Boolean,
    authState: AuthState,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "🏆 Global Leaderboard",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Text(
                text = if (isUnlocked) {
                    if (isSignedIn) "🟢 Signed in as ${getDisplayName(authState)}" 
                    else "🔴 Sign in to publish your scores"
                } else {
                    "🔒 Complete missions to unlock"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (isSignedIn) Color.Green else Color.Gray
            )
        }

        when {
            isSignedIn -> {
                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier.height(36.dp),
                    colors = OutlinedButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Text("Sign Out", fontSize = 12.sp)
                }
            }
            isUnlocked -> {
                Button(
                    onClick = onSignIn,
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4285F4)
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔑", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sign In", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

private fun getDisplayName(state: AuthState): String {
    return when (state) {
        is AuthState.SIGNED_IN -> state.account.displayName ?: "Player"
        else -> "Unknown"
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
//  LOCKED CONTENT
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun LeaderboardLockedContent(
    progress: Int,
    total: Int,
    onGoToMissions: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text(
            text = "🔒",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Global Leaderboard",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Complete missions to earn your place",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Progress indicator
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A2E)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Path to Glory",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(total) { index ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (index < progress) Color(0xFF4CAF50) else Color(0xFF333344),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (index < progress) "✅" else "${index + 1}",
                                color = if (index < progress) Color.White else Color.Gray
                            )
                        }
                        if (index < total - 1) {
                            Text(
                                text = "→",
                                color = if (index < progress) Color(0xFF4CAF50) else Color(0xFF333344)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$progress / $total missions complete",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onGoToMissions,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD700),
                contentColor = Color.Black
            )
        ) {
            Text("🎯 Go to Missions")
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
//  UNLOCKED CONTENT
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun LeaderboardContent(
    topScores: List<LeaderboardEntry>,
    isSignedIn: Boolean,
    onPublishScore: () -> Unit
) {
    if (topScores.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🌍", style = MaterialTheme.typography.displayLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Be the first to publish!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (isSignedIn) {
                    Button(onClick = onPublishScore) {
                        Text("🚀 Publish Your Score")
                    }
                }
            }
        }
        return
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            // Top 1 (Champion)
            if (topScores.isNotEmpty()) {
                LeaderboardItem(
                    entry = topScores[0],
                    rank = 1,
                    isHighlighted = true
                )
            }
        }

        items(topScores.drop(1)) { entry ->
            val index = topScores.indexOf(entry)
            LeaderboardItem(
                entry = entry,
                rank = index + 1,
                isHighlighted = false
            )
        }

        item {
            if (isSignedIn) {
                Button(
                    onClick = onPublishScore,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("🚀 Publish Your Score")
                }
            } else {
                Text(
                    text = "🔑 Sign in with Google to publish",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { /* Navigate to sign-in */ }
                )
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
//  LEADERBOARD ITEM
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun LeaderboardItem(
    entry: LeaderboardEntry,
    rank: Int,
    isHighlighted: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isHighlighted -> Color(0xFFFFD700).copy(alpha = 0.2f)
                rank <= 3 -> Color(0xFF2A2A4E)
                else -> Color(0xFF1A1A2E)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHighlighted) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Rank
            Text(
                text = when (rank) {
                    1 -> "🏆"
                    2 -> "🥈"
                    3 -> "🥉"
                    else -> "#$rank"
                },
                style = MaterialTheme.typography.titleMedium,
                color = when (rank) {
                    1 -> Color(0xFFFFD700)
                    2 -> Color(0xFFC0C0C0)
                    3 -> Color(0xFFCD7F32)
                    else -> Color.Gray
                },
                modifier = Modifier.width(40.dp)
            )

            // Player name
            Text(
                text = entry.player,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            // Score and altitude
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${entry.score} pts",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF4CAF50)
                )
                Text(
                    text = "🌍 ${entry.altitude}m",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
//  LOADING STATE
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Loading leaderboard...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
//  ERROR STATE
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Red,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("🔄 Retry")
            }
        }
    }
}
```

---

## PART 7: GRADLE DEPENDENCIES
Add these to your `app/build.gradle.kts` or `build.gradle`:

```kotlin
dependencies {
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    //  Google Sign-In (for OAuth 2.0)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    //  Google Sheets API (for writing data)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    implementation("com.google.api-client:google-api-client-android:2.2.0")
    implementation("com.google.apis:google-api-services-sheets:v4-rev20230923-2.0.0")
    implementation("com.google.http-client:google-http-client-gson:1.43.3")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    //  Networking (Retrofit for CSV fetching)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
}
```

---

## PART 8: GOOGLE CLOUD SETUP

📋 **Step 1: Create a Google Cloud Project**
1. Go to https://console.cloud.google.com/
2. Create a new project or select existing
3. Enable the Google Sheets API

📋 **Step 2: Create OAuth 2.0 Credentials**
1. Go to APIs & Services > Credentials
2. Click "Create Credentials" > "OAuth 2.0 Client ID"
3. Select "Android" as the application type
4. Add your app's SHA-1 signing certificate fingerprint
5. Copy the Web Client ID (for your app's ID token verification)

📋 **Step 3: Configure Your Google Sheet**
1. Create a new Google Sheet
2. Set up columns: Player, Score, Altitude, Timestamp
3. Publish to Web: File > Share > Publish to web
4. Copy the Sheet ID from the URL: `https://docs.google.com/spreadsheets/d/YOUR_SHEET_ID/edit`
5. Update `GoogleSheetsApi.SHEET_ID` with your ID

📋 **Step 4: Add Web Client ID to strings.xml**
```xml
<!-- res/values/strings.xml -->
<string name="default_web_client_id">YOUR_WEB_CLIENT_ID_HERE</string>
```

---

## PART 9: RATIONALE
**Why This Design Works**

1. **🔓 UNLOCK BY PLAY, NOT PAY**
   - Players earn leaderboard access by completing 3 missions (not paying cash).
   - Feels like an achievement, not a paywall.
   - Teaches players core mechanics through missions.

2. **🔑 OPT-IN GOOGLE SIGN-IN**
   - No forced accounts.
   - Players who don't want to sign in still see the leaderboard.
   - Signing in feels like a privilege, not a requirement.

3. **📊 PUBLIC READ, PRIVATE WRITE**
   - Reading uses public CSV endpoint (no auth, free, fast).
   - Writing uses OAuth 2.0 (secure, prevents spam).
   - Perfect for community-driven leaderboards.

4. **🎯 MISSION SYSTEM INTEGRATION**
   - Reuses existing infrastructure (`MissionManager`, `MissionRegistry`).
   - "Path to Glory" mission is a natural extension of your gameplay loop.
   - "World's Finest" rewards first-time publishers.

5. **⚡ ANTI-ABUSE BUILT-IN**
   - OAuth prevents anonymous spam.
   - Rate limiting can be added server-side or client-side.
   - Manual moderation via Google Sheets.

6. **📱 COMPOSE UI WITH STATES**
   - Clear locked/unlocked/signed-in states.
   - Progressive disclosure (shows only what's relevant).
   - Celebratory unlock moment.

---

## PART 10: IMPLEMENTATION CHECKLIST

- [ ] 1. Add `MISSIONS_COMPLETED` to `ObjectiveType` (`Mission.kt`)
- [ ] 2. Add `UNLOCK_LEADERBOARD` to `UnlockType` (`Mission.kt`)
- [ ] 3. Add `COMMUNITY` to `MissionCategory` (`Mission.kt`)
- [ ] 4. Add "leaderboard_unlock" mission to `MissionRegistry.kt`
- [ ] 5. Add "first_leaderboard_publish" mission to `MissionRegistry.kt`
- [ ] 6. Add `scoresPublished` to `GameStats` (`Mission.kt`)
- [ ] 7. Update `calculateProgress()` for `MISSIONS_COMPLETED` (`MissionManager.kt`)
- [ ] 8. Add `updateScorePublished()` to `MissionManager.kt`
- [ ] 9. Create `LeaderboardModels.kt`
- [ ] 10. Create `GoogleSignInManager.kt`
- [ ] 11. Create `GoogleSheetsApi.kt`
- [ ] 12. Create `LeaderboardManager.kt`
- [ ] 13. Create `LeaderboardScreen.kt`
- [ ] 14. Add Gradle dependencies
- [ ] 15. Set up Google Cloud Console project
- [ ] 16. Configure Google Sheet (publish to web)
- [ ] 17. Test: Play → Complete 3 missions → Unlock → Sign in → Publish → 🏆
