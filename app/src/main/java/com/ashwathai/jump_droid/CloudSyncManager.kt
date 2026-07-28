package com.ashwathai.jump_droid

import android.content.SharedPreferences
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class CloudSyncManager(
    private val loginManager: LoginManager,
    private val progressionManager: ProgressionManager,
    private val loadoutManager: LoadoutManager,
    private val sharedPrefs: SharedPreferences
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val prefs = sharedPrefs
    private var lastSaveHash: Int = 0

    private val userId: String?
        get() = loginManager.playerId

    private fun playerDoc() = userId?.let { firestore.collection("players").document(it) }

    fun isCloudEnabled(): Boolean = loginManager.isSignedIn && userId != null && FirebaseAuth.getInstance().currentUser != null

    suspend fun saveToCloud() {
        if (!isCloudEnabled()) return
        val doc = playerDoc() ?: return
        try {
            val progressionData = progressionManager.getCloudData()
            val data = buildMap<String, Any> {
                putAll(progressionData)
                put("equippedModule0", loadoutManager.equippedModuleIds.getOrNull(0) ?: "")
                put("equippedModule1", loadoutManager.equippedModuleIds.getOrNull(1) ?: "")
                put("equippedTrail", prefs.getInt("equipped_trail", 0))
                put("equippedPaint", prefs.getInt("equipped_paint", 0))
                put("sfxVolume", prefs.getFloat("sfx_volume", 0.7f).toDouble())
                put("musicVolume", prefs.getFloat("music_volume", 0.5f).toDouble())
                put("isMuted", prefs.getBoolean("is_muted", false))
                put("displayName", loginManager.displayName ?: loginManager.playerId ?: "Unknown")
            }
            
            // Minimum Write Logic: Hash comparison
            val currentHash = data.hashCode()
            if (currentHash == lastSaveHash) {
                android.util.Log.d("CloudSync", "Data unchanged, skipping write.")
                return 
            }

            val finalData = data.toMutableMap()
            finalData["lastSynced"] = Timestamp.now()
            
            doc.set(finalData, SetOptions.merge()).await()
            lastSaveHash = currentHash
            android.util.Log.d("CloudSync", "Cloud write successful.")
        } catch (_: Exception) {
        }
    }

    suspend fun loadFromCloud(): Boolean {
        if (!isCloudEnabled()) return false
        return try {
            val doc = playerDoc() ?: return false
            val snapshot = doc.get().await()
            if (!snapshot.exists()) return false

            val data = snapshot.data ?: return false

            progressionManager.applyCloudData(data)

            (data["equippedModule0"] as? String)?.let { loadoutManager.equipModule(it, 0) }
            (data["equippedModule1"] as? String)?.let { loadoutManager.equipModule(it, 1) }

            (data["equippedTrail"] as? Long)?.toInt()?.let { prefs.edit().putInt("equipped_trail", it).apply() }
            (data["equippedPaint"] as? Long)?.toInt()?.let { prefs.edit().putInt("equipped_paint", it).apply() }
            (data["sfxVolume"] as? Double)?.toFloat()?.let { prefs.edit().putFloat("sfx_volume", it).apply() }
            (data["musicVolume"] as? Double)?.toFloat()?.let { prefs.edit().putFloat("music_volume", it).apply() }
            (data["isMuted"] as? Boolean)?.let { prefs.edit().putBoolean("is_muted", it).apply() }

            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun syncAll() {
        if (!isCloudEnabled()) return
        loadFromCloud()
        saveToCloud()
    }
}
