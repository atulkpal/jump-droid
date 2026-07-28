package com.ashwathai.jump_droid

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.firestore.FirebaseFirestore

class RemoteConfigManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("RemoteConfigPrefs", Context.MODE_PRIVATE)
    private val firestore = FirebaseFirestore.getInstance()
    private val configRef = firestore.collection("server_config").document("remote_config")

    private var onCreditGranted: ((Int) -> Unit)? = null

    fun init(onCreditGranted: (Int) -> Unit) {
        this.onCreditGranted = onCreditGranted
        checkRemoteConfig()
    }

    private fun checkRemoteConfig() {
        configRef.get().addOnSuccessListener { snapshot ->
            val bonus = snapshot.getLong("credit_bonus_grant")?.toInt() ?: 0
            val appliedRevision = prefs.getInt("applied_revision", 0)
            val currentRevision = snapshot.getLong("revision")?.toInt() ?: 0

            if (bonus > 0 && currentRevision > appliedRevision) {
                onCreditGranted?.invoke(bonus)
                prefs.edit().putInt("applied_revision", currentRevision).apply()
            }
        }.addOnFailureListener {
            android.util.Log.w("RemoteConfigManager", "Failed to read remote config", it)
        }
    }
}
