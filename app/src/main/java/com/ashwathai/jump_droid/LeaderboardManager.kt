package com.ashwathai.jump_droid

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

data class LeaderboardEntry(
    val rank: Int,
    val displayName: String,
    val highScore: Int,
    val isPlayer: Boolean = false
)

class LeaderboardManager(val loginManager: LoginManager) {
    private val firestore = FirebaseFirestore.getInstance()

    private val userId: String?
        get() = loginManager.playerId

    fun isOnline(): Boolean = loginManager.isSignedIn && userId != null && FirebaseAuth.getInstance().currentUser != null

    suspend fun submitScore(score: Int, localHighScore: Int): Boolean {
        Log.d("LeaderboardManager", "Submitting score: $score (Local High: $localHighScore)")
        if (!isOnline()) {
            Log.w("LeaderboardManager", "Submission failed: Not online")
            return false
        }
        if (score <= 0) return false
        if (score < localHighScore) {
            Log.d("LeaderboardManager", "Score $score is less than local high score $localHighScore. Skipping.")
            return false 
        }
        val id = userId ?: return false
        return try {
            val ref = firestore.collection("leaderboard").document(id)
            val existing = ref.get().await()
            val remoteBest = if (existing.exists()) (existing.getLong("highScore") ?: 0).toInt() else 0
            if (score <= remoteBest) {
                Log.d("LeaderboardManager", "Score $score not better than remote best $remoteBest. Skipping.")
                return false
            }
            ref.set(
                mapOf(
                    "displayName" to (loginManager.displayName ?: loginManager.playerId ?: "Unknown"),
                    "highScore" to score,
                    "lastUpdated" to Timestamp.now()
                ),
                SetOptions.merge()
            ).await()
            Log.i("LeaderboardManager", "Score $score successfully submitted to Global Terminal.")
            true
        } catch (e: Exception) {
            Log.e("LeaderboardManager", "Score submission failed: ${e.message}")
            false
        }
    }

    suspend fun getTopScores(limit: Int = 50): List<LeaderboardEntry> {
        return try {
            val snapshot = firestore.collection("leaderboard")
                .orderBy("highScore", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            val myId = userId
            snapshot.documents.mapIndexed { index, doc ->
                val name = doc.getString("displayName") ?: "Unknown"
                val score = (doc.getLong("highScore") ?: 0).toInt()
                LeaderboardEntry(
                    rank = index + 1,
                    displayName = name,
                    highScore = score,
                    isPlayer = doc.id == myId
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getPlayerRank(): Pair<Int, Int> {
        if (!isOnline()) return 0 to 0
        return try {
            val id = userId ?: return 0 to 0
            val myEntry = firestore.collection("leaderboard").document(id).get().await()
            if (!myEntry.exists()) return 0 to 0
            val myScore = (myEntry.getLong("highScore") ?: 0).toInt()
            if (myScore <= 0) return 0 to 0
            val rankSnapshot = firestore.collection("leaderboard")
                .orderBy("highScore", Query.Direction.DESCENDING)
                .get()
                .await()
            val rank = rankSnapshot.documents.indexOfFirst { it.id == id } + 1
            rank to rankSnapshot.size()
        } catch (_: Exception) {
            0 to 0
        }
    }
}
