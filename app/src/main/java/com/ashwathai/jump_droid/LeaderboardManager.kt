package com.ashwathai.jump_droid

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import android.content.SharedPreferences
import androidx.core.content.edit

data class LeaderboardEntry(
    val rank: Int,
    val displayName: String,
    val highScore: Int,
    val isPlayer: Boolean = false
)

class LeaderboardManager(val loginManager: LoginManager, private val sharedPrefs: SharedPreferences) {
    private val firestore = FirebaseFirestore.getInstance()
    
    // Memory Cache for minimal reads
    private var cachedTopScores: List<LeaderboardEntry>? = null
    private var lastFetchTime: Long = 0
    private val CACHE_DURATION_MS = 5 * 60 * 1000 // 5 Minutes

    private val userId: String?
        get() = loginManager.playerId

    /**
     * Local cache of the best score successfully written to the server.
     * Prevents redundant writes.
     */
    private var localRemoteBest: Int
        get() = sharedPrefs.getInt("remote_best_score", 0)
        set(value) = sharedPrefs.edit { putInt("remote_best_score", value) }

    fun isOnline(): Boolean = loginManager.isSignedIn && userId != null && FirebaseAuth.getInstance().currentUser != null

    suspend fun submitScore(score: Int, localHighScore: Int): Boolean {
        if (!isOnline()) return false
        
        // Write-Squelching: If we haven't beaten our known remote record, don't even talk to the server.
        if (score <= localRemoteBest) {
            Log.d("LeaderboardManager", "Write Squelched: Score $score <= localRemoteBest $localRemoteBest")
            return false
        }

        if (score <= 0) return false
        
        val id = userId ?: return false
        return try {
            // Minimal Write Pattern: Directly set without pre-reading (server-side merge or local squelch handles it)
            firestore.collection("leaderboard").document(id).set(
                mapOf(
                    "displayName" to (loginManager.displayName ?: "Unknown Pilot"),
                    "highScore" to score,
                    "lastUpdated" to Timestamp.now()
                ),
                SetOptions.merge()
            ).await()
            
            localRemoteBest = score // Update squelch threshold
            Log.i("LeaderboardManager", "High-Efficiency Sync: New record $score pushed to fleet terminal.")
            true
        } catch (e: Exception) {
            Log.e("LeaderboardManager", "Sync Error: ${e.message}")
            false
        }
    }

    suspend fun getTopScores(forceRefresh: Boolean = false): List<LeaderboardEntry> {
        val now = System.currentTimeMillis()
        if (!forceRefresh && cachedTopScores != null && (now - lastFetchTime < CACHE_DURATION_MS)) {
            Log.d("LeaderboardManager", "Reading from Local Cache (Minimal Reads)")
            return cachedTopScores!!
        }

        return try {
            val snapshot = firestore.collection("leaderboard")
                .orderBy("highScore", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            
            val myId = userId
            val results = snapshot.documents.mapIndexed { index, doc ->
                val score = (doc.getLong("highScore") ?: 0).toInt()
                LeaderboardEntry(
                    rank = index + 1,
                    displayName = doc.getString("displayName") ?: "Unknown",
                    highScore = score,
                    isPlayer = doc.id == myId
                )
            }
            
            // Update cache
            cachedTopScores = results
            lastFetchTime = now
            results
        } catch (_: Exception) {
            cachedTopScores ?: emptyList()
        }
    }

    /**
     * High-Efficiency Ranking via Count Aggregation.
     * Only 1 "read" operation vs fetching the entire collection.
     */
    suspend fun getPlayerRank(): Pair<Int, Int> {
        if (!isOnline()) return 0 to 0
        return try {
            val id = userId ?: return 0 to 0
            
            // 1. Get player's best score (optimized read)
            val myEntry = firestore.collection("leaderboard").document(id).get().await()
            if (!myEntry.exists()) return 0 to 0
            val myScore = (myEntry.getLong("highScore") ?: 0).toInt()
            
            // 2. Count how many pilots are above me (Aggregate Query - High Efficiency)
            val superiorCount = firestore.collection("leaderboard")
                .whereGreaterThan("highScore", myScore)
                .count()
                .get(AggregateSource.SERVER)
                .await()
                .count
            
            // 3. Get total population count (Aggregate Query)
            val totalCount = firestore.collection("leaderboard")
                .count()
                .get(AggregateSource.SERVER)
                .await()
                .count
            
            (superiorCount.toInt() + 1) to totalCount.toInt()
        } catch (_: Exception) {
            0 to 0
        }
    }
}
