package com.ashwathai.jump_droid

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.firestore.FirebaseFirestore

class RemoteConfigManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("RemoteConfigPrefs", Context.MODE_PRIVATE)
    private val firestore = FirebaseFirestore.getInstance()
    private val configRef = firestore.collection("server_config").document("remote_config")

    private var onCreditGranted: ((Int) -> Unit)? = null
    private var onAnnouncementReceived: ((String, NotificationPriority) -> Unit)? = null

    fun init(onCreditGranted: (Int) -> Unit, onAnnouncementReceived: (String, NotificationPriority) -> Unit) {
        this.onCreditGranted = onCreditGranted
        this.onAnnouncementReceived = onAnnouncementReceived
        checkRemoteConfig()
    }

    private fun checkRemoteConfig() {
        configRef.get().addOnSuccessListener { snapshot ->
            // --- Credit Bonus Grant ---
            val bonus = snapshot.getLong("credit_bonus_grant")?.toInt() ?: 0
            val appliedRevision = prefs.getInt("applied_revision", 0)
            val currentRevision = snapshot.getLong("revision")?.toInt() ?: 0

            if (bonus > 0 && currentRevision > appliedRevision) {
                onCreditGranted?.invoke(bonus)
                prefs.edit().putInt("applied_revision", currentRevision).apply()
            }

            // --- Remote Announcement ---
            val announcementId = snapshot.getString("announcement_id")
            val announcementMsg = snapshot.getString("announcement_text")
            val priorityStr = snapshot.getString("announcement_priority") ?: "TACTICAL"
            
            if (!announcementId.isNullOrBlank() && !announcementMsg.isNullOrBlank()) {
                val lastSeenId = prefs.getString("last_announcement_id", "")
                if (announcementId != lastSeenId) {
                    val priority = try { NotificationPriority.valueOf(priorityStr) } catch(e: Exception) { NotificationPriority.TACTICAL }
                    onAnnouncementReceived?.invoke(announcementMsg, priority)
                    prefs.edit().putString("last_announcement_id", announcementId).apply()
                }
            }
        }.addOnFailureListener {
            android.util.Log.w("RemoteConfigManager", "Failed to read remote config", it)
        }
    }
}
