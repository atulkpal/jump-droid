package com.ashwathai.jump_droid

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.games.PlayGames
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginManager(private val activity: Activity) {

    private val auth = FirebaseAuth.getInstance()
    private val prefs: SharedPreferences = activity.getSharedPreferences("LoginPrefs", Activity.MODE_PRIVATE)
    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestProfile()
        .requestIdToken(activity.getString(R.string.default_web_client_id))
        .build()
    private val googleSignInClient = GoogleSignIn.getClient(activity, gso)

    var isSignedIn by mutableStateOf(false)
        private set
    var displayName by mutableStateOf<String?>(null)
        private set
    var playerId by mutableStateOf<String?>(null)
        private set

    fun restoreSession() {
        googleSignInClient.silentSignIn()
            .addOnSuccessListener { account -> setSignedIn(account) }
            .addOnFailureListener {
                val savedId = prefs.getString("player_id", null)
                if (savedId != null) {
                    isSignedIn = true
                    displayName = prefs.getString("player_name", null)
                    playerId = savedId
                    tryUpgradeToPlayGames()
                }
            }
    }

    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    fun onSignInResult(data: Intent?) {
        if (data == null) {
            Log.e("LoginManager", "onSignInResult: Intent is null")
            return
        }
        
        GoogleSignIn.getSignedInAccountFromIntent(data)
            .addOnSuccessListener { account ->
                Log.d("LoginManager", "Sign-in successful: ${account.displayName}")
                setSignedIn(account)
            }
            .addOnFailureListener { e ->
                val statusCode = (e as? com.google.android.gms.common.api.ApiException)?.statusCode ?: -1
                Log.e("LoginManager", "Sign-in failed. Status Code: $statusCode, Message: ${e.message}")
                
                // Fallback to check if we are already signed in or have a last account
                val fallback = GoogleSignIn.getLastSignedInAccount(activity)
                if (fallback != null) {
                    Log.d("LoginManager", "Fallback to last signed in account: ${fallback.displayName}")
                    setSignedIn(fallback)
                } else {
                    isSignedIn = false
                    displayName = null
                    playerId = null
                }
            }
    }

    private fun setSignedIn(account: GoogleSignInAccount) {
        val idToken = account.idToken
        if (idToken != null) {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnSuccessListener {
                    Log.d("LoginManager", "Firebase Auth Successful")
                    finalizeSignIn(account)
                }
                .addOnFailureListener { e ->
                    Log.e("LoginManager", "Firebase Auth Failed: ${e.message}")
                    // Still set signed in locally if Google worked, but Firestore might fail
                    finalizeSignIn(account)
                }
        } else {
            finalizeSignIn(account)
        }
    }

    private fun finalizeSignIn(account: GoogleSignInAccount) {
        isSignedIn = true
        displayName = account.displayName ?: account.email
        Log.d("LoginManager", "Identity Finalized: $displayName")
        playerId = account.id ?: account.email?.lowercase()?.replace("@", "(at)")?.replace(".", "(dot)")
        prefs.edit()
            .putString("player_name", displayName)
            .putString("player_id", playerId)
            .apply()
        tryUpgradeToPlayGames()
    }

    private fun tryUpgradeToPlayGames() {
        Log.d("LoginManager", "Attempting to upgrade to Play Games identity...")
        val gamesSignInClient = PlayGames.getGamesSignInClient(activity)
        
        gamesSignInClient.isAuthenticated.addOnSuccessListener { result ->
            if (result.isAuthenticated) {
                fetchPlayGamesProfile()
            } else {
                Log.d("LoginManager", "PGS not authenticated. Requesting interactive sign-in...")
                gamesSignInClient.signIn().addOnSuccessListener { authResult ->
                    if (authResult.isAuthenticated) {
                        fetchPlayGamesProfile()
                    } else {
                        Log.w("LoginManager", "PGS interactive sign-in failed.")
                    }
                }.addOnFailureListener { e ->
                    Log.e("LoginManager", "PGS sign-in task failed: ${e.message}")
                }
            }
        }
    }

    private fun fetchPlayGamesProfile() {
        PlayGames.getPlayersClient(activity).currentPlayer
            .addOnSuccessListener { player -> handlePlayGamesSuccess(player) }
            .addOnFailureListener { e ->
                Log.e("LoginManager", "Failed to fetch Play Games profile even after auth: ${e.message}")
            }
    }

    private fun handlePlayGamesSuccess(player: com.google.android.gms.games.Player) {
        val gpgName = player.displayName
        Log.d("LoginManager", "Play Games identity found: $gpgName (${player.playerId})")
        
        if (gpgName == displayName) {
            Log.d("LoginManager", "Identity match: GamerTag is same as Google Name ($gpgName)")
        } else {
            Log.d("LoginManager", "Identity upgrade: Changing $displayName -> $gpgName")
        }

        displayName = gpgName
        playerId = player.playerId
        prefs.edit()
            .putString("player_name", player.displayName)
            .putString("player_id", player.playerId)
            .apply()
    }

    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut().addOnSuccessListener {
            isSignedIn = false
            displayName = null
            playerId = null
            prefs.edit()
                .remove("player_name")
                .remove("player_id")
                .apply()
        }
    }
}
