package com.ashwathai.jump_droid

import android.app.Activity
import com.google.android.gms.games.PlayGames

class GamesAchievementManager(activity: Activity) {
    private val achievementsClient = PlayGames.getAchievementsClient(activity)

    fun unlock(localId: String) {
        val gpgId = GPG_MAP[localId] ?: return
        try {
            achievementsClient.unlock(gpgId)
        } catch (_: Exception) {
        }
    }

    fun increment(localId: String, steps: Int = 1) {
        val gpgId = GPG_MAP[localId] ?: return
        try {
            achievementsClient.increment(gpgId, steps)
        } catch (_: Exception) {
        }
    }

    companion object {
        val GPG_MAP = mapOf(
            "first_launch" to "PLEASE_REPLACE_ME_first_launch",
            "sky_breaker" to "PLEASE_REPLACE_ME_sky_breaker",
            "orbital_pilot" to "PLEASE_REPLACE_ME_orbital_pilot",
            "deep_space" to "PLEASE_REPLACE_ME_deep_space",
            "combo_master" to "PLEASE_REPLACE_ME_combo_master",
            "thermal_survivor" to "PLEASE_REPLACE_ME_thermal_survivor",
            "depth_walker" to "PLEASE_REPLACE_ME_depth_walker",
            "resourceful" to "PLEASE_REPLACE_ME_resourceful",
            "untouchable" to "PLEASE_REPLACE_ME_untouchable",
            "infinite_ascent" to "PLEASE_REPLACE_ME_infinite_ascent"
        )
    }
}
