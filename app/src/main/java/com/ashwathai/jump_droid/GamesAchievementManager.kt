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
        /**
         * Mapping from local IDs to Google Play Games Achievement IDs.
         * IMPORTANT: Replace these with actual IDs from the Google Play Console before publishing.
         */
        val GPG_MAP = mapOf(
            "first_launch" to "CgkIuL-H2_8EEAIQAQ",
            "sky_breaker" to "CgkIuL-H2_8EEAIQAg",
            "orbital_pilot" to "CgkIuL-H2_8EEAIQAw",
            "deep_space" to "CgkIuL-H2_8EEAIQBA",
            "combo_master" to "CgkIuL-H2_8EEAIQBQ",
            "thermal_survivor" to "CgkIuL-H2_8EEAIQBg",
            "depth_walker" to "CgkIuL-H2_8EEAIQBw",
            "resourceful" to "CgkIuL-H2_8EEAIQCA",
            "untouchable" to "CgkIuL-H2_8EEAIQCQ",
            "infinite_ascent" to "CgkIuL-H2_8EEAIQCg"
        )
    }
}
