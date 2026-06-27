package com.ashwathai.jump_droid

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

class PurchaseManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("JumpDroidPrefs", Context.MODE_PRIVATE)

    val isPremiumUser: Boolean get() = prefs.getBoolean("premium_user", false)

    fun setPremiumUser(value: Boolean) {
        prefs.edit().putBoolean("premium_user", value).apply()
    }

    fun initialize() {}

    fun launchPurchaseFlow(activity: Activity) {
        setPremiumUser(true)
    }
}
