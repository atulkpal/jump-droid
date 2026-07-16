package com.ashwathai.jump_droid

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Date

/**
 * Decorator that wraps [GameAnalytics] with Firestore-backed tester analytics.
 *
 * Tracks per-tester fields and per-session history in Firestore.
 * Designed to be easily removed (delete this file + remove dependency) or
 * evolved into Analytics V2 (see [Migration Guide]).
 *
 * ## Firestore Schema
 * - `testers/{sanitizedEmail}` — Tester profile + lifetime aggregate fields.
 * - `testers/{sanitizedEmail}/sessions/{sessionId}` — Per-session history (exactly one per completed game).
 *
 * ## Session Lifecycle (Beta V0)
 * - Born on [logGameStart] when a game begins (or on [onAppForeground] if app was cold-started).
 * - Updated on [logGameOver] with game result data, persisted locally for crash resilience.
 * - Synced periodically (60s) if dirty. Finalized on game→main_menu transition or app background.
 * - One completed game (Play → Game Over/Abort) = one analytics session for all closed beta metrics.
 *
 * ## Migration to Analytics V2
 * 1. Promote `testers` collection to `players` with consent gating.
 * 2. Add user properties to Firebase Analytics via `setUserProperty()`.
 * 3. Promote local counters to real-time Firebase events where needed.
 * 4. Add GDPR/privacy consent dialog before any data collection.
 * 5. Remove the `sessions` subcollection or promote to a BigQuery export.
 */
class PlayerAnalyticsManager(
    context: Context,
    private val analytics: GameAnalytics
) : GameAnalytics {

    companion object {
        private const val TAG = "BetaAnalytics"
        private const val PERIODIC_SYNC_MS = 60_000L
        private const val BG_TIMEOUT_MS = 30_000L
    }

    // --- SharedPreferences ---
    private val prefs: SharedPreferences =
        context.getSharedPreferences("PlayerAnalyticsPrefs", Context.MODE_PRIVATE)

    private val appPrefs: SharedPreferences =
        context.getSharedPreferences("JumpDroidPrefs", Context.MODE_PRIVATE)

    private val firestore = FirebaseFirestore.getInstance()

    // --- Periodic sync ---
    private val syncHandler = Handler(Looper.getMainLooper())
    private var syncRequired: Boolean = false
    private val periodicSyncRunnable = object : Runnable {
        override fun run() {
            if (syncRequired && sessionId.isNotEmpty()) {
                syncSessionToFirestore()
                syncRequired = false
            }
            syncHandler.postDelayed(this, PERIODIC_SYNC_MS)
        }
    }

    // --- Background timeout timer ---
    private val bgTimeoutRunnable = Runnable {
        if (sessionId.isNotEmpty()) {
            finalizePreviousSession()
        }
    }

    // --- Registration ---
    val isConsented: Boolean get() = prefs.contains("prompt_shown")
    val isRegistered: Boolean get() = prefs.contains("tester_email")

    fun registerTester(email: String, name: String, phone: String) {
        val cleanEmail = email.trim().lowercase()
        require(Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) { "Invalid email" }
        prefs.edit()
            .putString("tester_email", cleanEmail)
            .putString("tester_name", name.trim())
            .putString("tester_phone", phone.trim())
            .putBoolean("prompt_shown", true)
            .apply()
        syncTesterTotalsToFirestore()
    }

    fun skipRegistration() {
        prefs.edit().putBoolean("prompt_shown", true).apply()
    }

    // --- Lifecycle hooks (called from MainActivity) ---

    private var appForegroundTime: Long = 0L
    private var gameStartTime: Long = 0L
    private var todayDate: String = ""
    private var lastScreenRoute: String = ""

    // Session state (memory + persisted locally for crash resilience)
    private var sessionId: String = ""
    private var sessionStartTime: Long = 0L
    private var sessionGameplayTime: Float = 0f
    private var sessionBannerImpressions: Int = 0
    private var sessionRewardAdsWatched: Int = 0
    private var sessionContinuesUsed: Int = 0
    private var sessionHighestScore: Int = 0
    private var sessionGamesPlayed: Int = 0
    private val sessionRocketTypes: MutableSet<String> = mutableSetOf()
    private var sessionZoneReached: String = ""
    private var sessionFinalScore: Int = 0
    private var sessionOutcome: String = ""
    private var sessionGameActive: Boolean = false
    private var sessionAppOpenTimeAtBg: Float = 0f
    private var sessionPausedTime: Long = 0L

    // Incremental app open time tracking
    private var lastAppOpenSync: Long = 0L

    fun onAppForeground() {
        val now = System.currentTimeMillis()
        appForegroundTime = now
        // Check for date change — finalize previous session if it crossed midnight
        val dateStr = formatDate()
        val dateChanged = todayDate.isNotEmpty() && todayDate != dateStr

        if (dateChanged && sessionId.isNotEmpty()) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Date changed, finalizing session $sessionId")
            finalizePreviousSession()
        }

        if (dateChanged) {
            todayDate = dateStr
            prefs.edit().putFloat("today_app_open_time", 0f).putFloat("today_gameplay_time", 0f).apply()
        }

        // Cancel background timeout (user returned before 30s)
        syncHandler.removeCallbacks(bgTimeoutRunnable)

        if (sessionId.isEmpty()) {
            // Crash recovery or timeout finalized the previous session
            if (prefs.contains("pending_session_id")) {
                restoreSessionFromPrefs()
                finalizePreviousSession()
            }

            // Start new session
            initNewSession()
        } else {
            // Resuming existing session (user returned within 30s or during ad)
            lastAppOpenSync = now
            startPeriodicSync()
        }
    }

    fun onAppBackground() {
        val now = System.currentTimeMillis()
        stopPeriodicSync()

        // Start background timeout — if user doesn't return within 30s, finalize session
        syncHandler.removeCallbacks(bgTimeoutRunnable)
        syncHandler.postDelayed(bgTimeoutRunnable, BG_TIMEOUT_MS)

        // Capture remaining game time if a game was in progress
        if (sessionGameActive) {
            val gameElapsed = (now - gameStartTime) / 1000f
            sessionGameplayTime += gameElapsed
            sessionGameActive = false
        }

        // Accumulate remaining app open time since last sync (captures foreground time accurately)
        accumulateAppOpenTimeSince(now)

        // Save app open time and pause time for finalization (crash or date change only)
        sessionAppOpenTimeAtBg = (now - sessionStartTime) / 1000f
        if (sessionGameplayTime > sessionAppOpenTimeAtBg) {
            if (BuildConfig.DEBUG) Log.w(TAG, "Clamping gameplayTime $sessionGameplayTime to appOpenTime $sessionAppOpenTimeAtBg")
            sessionGameplayTime = sessionAppOpenTimeAtBg
        }
        sessionPausedTime = now

        // Persist all session state for crash resilience
        persistSessionToPrefs()
    }

    private fun finalizePreviousSession() {
        if (!isRegistered || sessionId.isEmpty()) return
        if (BuildConfig.DEBUG) Log.d(TAG, "finalizePreviousSession | sessionId=$sessionId")

        val endTime = if (sessionPausedTime > 0L) sessionPausedTime else System.currentTimeMillis()

        val appOpenTime = when {
            sessionAppOpenTimeAtBg > 0f -> sessionAppOpenTimeAtBg
            sessionPausedTime > 0L -> (sessionPausedTime - sessionStartTime) / 1000f
            else -> sessionGameplayTime   // crash during gameplay — use gameplay as lower bound
        }
        val clampedGameplay = if (sessionGameplayTime > appOpenTime) {
            if (BuildConfig.DEBUG) Log.w(TAG, "Clamping gameplayTime $sessionGameplayTime to appOpenTime $appOpenTime")
            appOpenTime
        } else {
            sessionGameplayTime
        }

        // Accumulate totals locally (ensures tester totals = sum of sessions)
        accumulateSessionTotals(clampedGameplay, endTime)

        // Write session document
        syncSessionToFirestore(
            endTime = endTime,
            appOpenTime = appOpenTime,
            gameplayTime = clampedGameplay,
            status = "COMPLETED"
        )

        syncTesterTotalsToFirestore()
        syncRequired = false

        // Clear session
        sessionId = ""
        prefs.edit().remove("pending_session_id").apply()
        if (BuildConfig.DEBUG) Log.d(TAG, "Session finalized & cleared")
    }

    private fun initNewSession() {
        val now = System.currentTimeMillis()
        sessionId = java.text.SimpleDateFormat("yyyyMMdd_HHmmss_SSS", java.util.Locale.US)
            .format(Date())
        if (BuildConfig.DEBUG) Log.d(TAG, "initNewSession $sessionId")
        sessionStartTime = now
        sessionGameplayTime = 0f
        sessionBannerImpressions = 0
        sessionRewardAdsWatched = 0
        sessionContinuesUsed = 0
        sessionHighestScore = 0
        sessionGamesPlayed = 0
        sessionRocketTypes.clear()
        sessionZoneReached = ""
        sessionFinalScore = 0
        sessionOutcome = ""
        sessionGameActive = false
        sessionAppOpenTimeAtBg = 0f
        sessionPausedTime = 0L
        lastAppOpenSync = now
        syncRequired = false

        prefs.edit().putString("pending_session_id", sessionId).apply()
        startPeriodicSync()
    }

    private fun accumulateSessionTotals(gameplayTime: Float, now: Long) {
        // Remaining app open time since last incremental sync — avoids double-counting
        val appOpenDelta = if (lastAppOpenSync > 0) {
            (now - lastAppOpenSync) / 1000f
        } else {
            (now - sessionStartTime) / 1000f
        }

        val totalAppOpen = prefs.getFloat("total_app_open_time", 0f)
        val todayAppOpen = prefs.getFloat("today_app_open_time", 0f)
        val totalGameplay = prefs.getFloat("local_total_gameplay_time", 0f)
        val todayGameplay = prefs.getFloat("today_gameplay_time", 0f)
        val totalSessions = prefs.getInt("total_sessions", 0)

        prefs.edit()
            .putFloat("total_app_open_time", totalAppOpen + appOpenDelta)
            .putFloat("today_app_open_time", todayAppOpen + appOpenDelta)
            .putFloat("local_total_gameplay_time", totalGameplay + gameplayTime)
            .putFloat("today_gameplay_time", todayGameplay + gameplayTime)
            .putInt("total_sessions", totalSessions + 1)
            .apply()
    }

    // --- Periodic sync ---

    private fun startPeriodicSync() {
        syncHandler.removeCallbacks(periodicSyncRunnable)
        syncHandler.postDelayed(periodicSyncRunnable, PERIODIC_SYNC_MS)
    }

    private fun stopPeriodicSync() {
        syncHandler.removeCallbacks(periodicSyncRunnable)
    }

    // --- GameAnalytics decorator ---

    override fun logGameStart(rocket: RocketType) {
        analytics.logGameStart(rocket)

        // If no active session (finalized on main_menu transition), start a new one
        if (sessionId.isEmpty()) {
            initNewSession()
        }

        gameStartTime = System.currentTimeMillis()
        sessionGameActive = true
        sessionGamesPlayed++
        sessionRocketTypes.add(rocket.name)
    }

    override fun logGameOver(score: Int, zone: AltitudeZone, rocket: RocketType, reason: String) {
        val now = System.currentTimeMillis()
        val elapsed = (now - gameStartTime) / 1000f
        sessionGameActive = false
        sessionGameplayTime += elapsed

        // Sync app open time incrementally — guarantees gameplayTime ≤ appOpenTime at every step
        accumulateAppOpenTimeSince(now)

        // Update session running metrics
        if (score > sessionHighestScore) sessionHighestScore = score
        sessionFinalScore = score
        sessionOutcome = reason
        sessionZoneReached = zone.zoneName

        // Persist local state for crash resilience
        persistSessionToPrefs()

        // Mark dirty for periodic sync
        syncRequired = true

        analytics.logGameOver(score, zone, rocket, reason)
    }

    override fun logZoneChanged(zone: AltitudeZone) = analytics.logZoneChanged(zone)

    override fun logMissionStarted(mission: Mission) = analytics.logMissionStarted(mission)

    override fun logMissionCompleted(mission: Mission) = analytics.logMissionCompleted(mission)

    override fun logBossSpawned(boss: ThreatDefinition, zone: AltitudeZone) =
        analytics.logBossSpawned(boss, zone)

    override fun logBossDefeated(boss: ThreatDefinition, zone: AltitudeZone) =
        analytics.logBossDefeated(boss, zone)

    override fun logRocketUnlocked(rocket: RocketType) = analytics.logRocketUnlocked(rocket)

    override fun logModuleEquipped(moduleId: String, slot: Int) =
        analytics.logModuleEquipped(moduleId, slot)

    override fun logScreenView(screenName: String, screenClass: String) {
        analytics.logScreenView(screenName, screenClass)

        // Detect game → main_menu: player aborted the run, finalize the session immediately
        if (lastScreenRoute == "game" && screenName == "main_menu" && sessionId.isNotEmpty()) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Detected game→main_menu transition, finalizing session $sessionId")
            // Temporarily set app open time to cover the game period if not already computed
            if (sessionPausedTime == 0L) {
                val now = System.currentTimeMillis()
                sessionAppOpenTimeAtBg = (now - sessionStartTime) / 1000f
                sessionPausedTime = now
            }
            finalizePreviousSession()
        }

        lastScreenRoute = screenName
    }

    override fun logAdImpression(adType: String, adUnitId: String) {
        analytics.logAdImpression(adType, adUnitId)
        when (adType) {
            "banner" -> {
                incrementCounter("banner_impressions")
                sessionBannerImpressions++
            }
            "rewarded" -> {
                incrementCounter("rewarded_ads_watched")
                sessionRewardAdsWatched++
            }
        }
        syncRequired = true
    }

    override fun logAdClicked(adType: String, adUnitId: String) {
        analytics.logAdClicked(adType, adUnitId)
        if (adType == "rewarded") {
            incrementCounter("continues_used")
            sessionContinuesUsed++
        }
        syncRequired = true
    }

    // --- Internal accumulators ---

    private fun incrementCounter(key: String) {
        val current = prefs.getInt(key, 0)
        prefs.edit().putInt(key, current + 1).apply()
    }

    private fun accumulateAppOpenTimeSince(now: Long) {
        if (lastAppOpenSync > 0) {
            val elapsed = (now - lastAppOpenSync) / 1000f
            val total = prefs.getFloat("total_app_open_time", 0f)
            val today = prefs.getFloat("today_app_open_time", 0f)
            prefs.edit()
                .putFloat("total_app_open_time", total + elapsed)
                .putFloat("today_app_open_time", today + elapsed)
                .apply()
        }
        lastAppOpenSync = now
    }

    // --- Crash resilience ---

    private fun persistSessionToPrefs() {
        prefs.edit()
            .putString("pending_session_id", sessionId)
            .putLong("pending_session_start", sessionStartTime)
            .putLong("pending_last_app_open_sync", lastAppOpenSync)
            .putFloat("pending_gameplay_time", sessionGameplayTime)
            .putInt("pending_banner_impressions", sessionBannerImpressions)
            .putInt("pending_rewarded_ads", sessionRewardAdsWatched)
            .putInt("pending_continues_used", sessionContinuesUsed)
            .putInt("pending_highest_score", sessionHighestScore)
            .putInt("pending_games_played", sessionGamesPlayed)
            .putString("pending_rocket_types", sessionRocketTypes.joinToString(","))
            .putString("pending_zone_reached", sessionZoneReached)
            .putInt("pending_final_score", sessionFinalScore)
            .putString("pending_outcome", sessionOutcome)
            .putFloat("pending_app_open_time_at_bg", sessionAppOpenTimeAtBg)
            .putLong("pending_session_paused_time", sessionPausedTime)
            .apply()
    }

    private fun restoreSessionFromPrefs() {
        sessionId = prefs.getString("pending_session_id", "") ?: ""
        sessionStartTime = prefs.getLong("pending_session_start", 0L)
        lastAppOpenSync = prefs.getLong("pending_last_app_open_sync", 0L)
        sessionGameplayTime = prefs.getFloat("pending_gameplay_time", 0f)
        sessionBannerImpressions = prefs.getInt("pending_banner_impressions", 0)
        sessionRewardAdsWatched = prefs.getInt("pending_rewarded_ads", 0)
        sessionContinuesUsed = prefs.getInt("pending_continues_used", 0)
        sessionHighestScore = prefs.getInt("pending_highest_score", 0)
        sessionGamesPlayed = prefs.getInt("pending_games_played", 0)
        val rocketStr = prefs.getString("pending_rocket_types", "") ?: ""
        sessionRocketTypes.clear()
        if (rocketStr.isNotEmpty()) {
            sessionRocketTypes.addAll(rocketStr.split(","))
        }
        sessionZoneReached = prefs.getString("pending_zone_reached", "") ?: ""
        sessionFinalScore = prefs.getInt("pending_final_score", 0)
        sessionOutcome = prefs.getString("pending_outcome", "") ?: ""
        sessionAppOpenTimeAtBg = prefs.getFloat("pending_app_open_time_at_bg", 0f)
        sessionPausedTime = prefs.getLong("pending_session_paused_time", 0L)
        sessionGameActive = false
    }

    // --- Firestore sync ---

    private fun sanitizeEmail(email: String): String {
        return email.lowercase().replace("@", "(at)").replace(".", "(dot)")
    }

    private fun syncSessionToFirestore(
        endTime: Long? = null,
        appOpenTime: Float? = null,
        gameplayTime: Float? = null,
        status: String? = null
    ) {
        if (!isRegistered || sessionId.isEmpty()) return
        val email = prefs.getString("tester_email", "") ?: return
        val writeType = if (status == "COMPLETED") "FINAL" else "PERIODIC"
        if (BuildConfig.DEBUG) Log.d(TAG, "FIREBASE WRITE $writeType | sessionId=$sessionId | gameplayTime=$gameplayTime | status=$status")
        val docId = sanitizeEmail(email)

        val sessionData = hashMapOf<String, Any>()

        if (endTime != null && appOpenTime != null && gameplayTime != null) {
            // Final write with complete data
            sessionData["sessionStart"] = Timestamp(Date(sessionStartTime))
            sessionData["sessionEnd"] = Timestamp(Date(endTime))
            sessionData["appOpenTime"] = appOpenTime
            sessionData["gameplayTime"] = gameplayTime
            sessionData["bannerImpressions"] = sessionBannerImpressions
            sessionData["rewardAdsWatched"] = sessionRewardAdsWatched
            sessionData["continuesUsed"] = sessionContinuesUsed
            sessionData["highestScore"] = sessionHighestScore
            sessionData["gamesPlayed"] = sessionGamesPlayed
            sessionData["rocketTypes"] = sessionRocketTypes.toList()
            sessionData["zoneReached"] = sessionZoneReached
            sessionData["finalScore"] = sessionFinalScore
            sessionData["outcome"] = sessionOutcome
            sessionData["versionName"] = BuildConfig.VERSION_NAME
            sessionData["versionCode"] = BuildConfig.VERSION_CODE
            if (status != null) sessionData["sessionStatus"] = status
        } else {
            // Partial update (periodic sync) — only send what we have
            sessionData["sessionStart"] = Timestamp(Date(sessionStartTime))
            sessionData["gameplayTime"] = sessionGameplayTime
            sessionData["bannerImpressions"] = sessionBannerImpressions
            sessionData["rewardAdsWatched"] = sessionRewardAdsWatched
            sessionData["continuesUsed"] = sessionContinuesUsed
            sessionData["highestScore"] = sessionHighestScore
            sessionData["gamesPlayed"] = sessionGamesPlayed
            sessionData["rocketTypes"] = sessionRocketTypes.toList()
            sessionData["zoneReached"] = sessionZoneReached
            sessionData["finalScore"] = sessionFinalScore
            sessionData["outcome"] = sessionOutcome
            sessionData["versionName"] = BuildConfig.VERSION_NAME
            sessionData["versionCode"] = BuildConfig.VERSION_CODE
        }

        firestore.collection("testers")
            .document(docId)
            .collection("sessions")
            .document(sessionId)
            .set(sessionData, SetOptions.merge())
            .addOnFailureListener { e ->
                if (BuildConfig.DEBUG) Log.e(TAG, "Failed to sync session $sessionId", e)
            }
    }

    private fun syncTesterTotalsToFirestore() {
        if (!isRegistered) return
        val email = prefs.getString("tester_email", "") ?: return
        val docId = sanitizeEmail(email)

        val data = hashMapOf(
            "email" to email,
            "name" to (prefs.getString("tester_name", "") ?: ""),
            "phone" to (prefs.getString("tester_phone", "") ?: ""),
            "lastSeen" to Timestamp.now(),
            "versionName" to BuildConfig.VERSION_NAME,
            "versionCode" to BuildConfig.VERSION_CODE,
            "totalAppOpenTime" to prefs.getFloat("total_app_open_time", 0f),
            "todayAppOpenTime" to prefs.getFloat("today_app_open_time", 0f),
            "totalGameplayTime" to prefs.getFloat("local_total_gameplay_time", 0f),
            "todayGameplayTime" to prefs.getFloat("today_gameplay_time", 0f),
            "totalSessions" to prefs.getInt("total_sessions", 0),
            "highestScore" to appPrefs.getInt("highScore", 0),
            "rewardAdsWatched" to prefs.getInt("rewarded_ads_watched", 0),
            "bannerImpressions" to prefs.getInt("banner_impressions", 0),
            "continuesUsed" to prefs.getInt("continues_used", 0)
        )

        firestore.collection("testers")
            .document(docId)
            .set(data, SetOptions.merge())
            .addOnFailureListener { e ->
                if (BuildConfig.DEBUG) Log.e(TAG, "Failed to sync tester totals for $docId", e)
            }
    }

    // --- Helpers ---

    private fun formatDate(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(Date())
    }
}
