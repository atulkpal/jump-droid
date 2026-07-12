package com.ashwathai.jump_droid

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Domain-driven interface for game analytics.
 * The game logic speaks in Jump Droid terminology (e.g. Missions, Bosses, Rockets).
 */
interface GameAnalytics {
    fun logGameStart(rocket: RocketType)
    fun logGameOver(score: Int, zone: AltitudeZone, rocket: RocketType, reason: String)
    fun logZoneChanged(zone: AltitudeZone)
    fun logMissionStarted(mission: Mission)
    fun logMissionCompleted(mission: Mission)
    fun logBossSpawned(boss: ThreatDefinition, zone: AltitudeZone)
    fun logBossDefeated(boss: ThreatDefinition, zone: AltitudeZone)
    fun logRocketUnlocked(rocket: RocketType)
    fun logModuleEquipped(moduleId: String, slot: Int)
    fun logScreenView(screenName: String, screenClass: String)
    fun logAdImpression(adType: String, adUnitId: String)
    fun logAdClicked(adType: String, adUnitId: String)
}

/**
 * Firebase implementation of [GameAnalytics].
 * Maps domain events to standard Firebase Analytics events where appropriate.
 */
class FirebaseGameAnalytics(context: Context) : GameAnalytics {
    private val firebase = FirebaseAnalytics.getInstance(context)

    override fun logGameStart(rocket: RocketType) {
        val params = Bundle().apply {
            putString("rocket_type", rocket.name)
            putString("rocket_title", rocket.title)
        }
        // Map to standard level_start
        firebase.logEvent(FirebaseAnalytics.Event.LEVEL_START, params)
    }

    override fun logGameOver(score: Int, zone: AltitudeZone, rocket: RocketType, reason: String) {
        val params = Bundle().apply {
            putInt(FirebaseAnalytics.Param.SCORE, score)
            putString(FirebaseAnalytics.Param.LEVEL_NAME, zone.zoneName)
            putString("rocket_type", rocket.name)
            putString("failure_reason", reason)
        }
        // Map to standard level_end
        firebase.logEvent(FirebaseAnalytics.Event.LEVEL_END, params)
    }

    override fun logZoneChanged(zone: AltitudeZone) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.LEVEL_NAME, zone.zoneName)
        }
        firebase.logEvent("zone_changed", params)
    }

    override fun logMissionStarted(mission: Mission) {
        val params = Bundle().apply {
            putString("mission_id", mission.id)
            putString("mission_type", mission.type.name)
            putString("mission_category", mission.category.name)
        }
        firebase.logEvent("mission_started", params)
    }

    override fun logMissionCompleted(mission: Mission) {
        val params = Bundle().apply {
            putString("mission_id", mission.id)
            putString("mission_type", mission.type.name)
        }
        firebase.logEvent("mission_completed", params)
    }

    override fun logBossSpawned(boss: ThreatDefinition, zone: AltitudeZone) {
        val params = Bundle().apply {
            putString("boss_id", boss.id)
            putString("boss_name", boss.name)
            putString("zone", zone.zoneName)
        }
        firebase.logEvent("boss_spawned", params)
    }

    override fun logBossDefeated(boss: ThreatDefinition, zone: AltitudeZone) {
        val params = Bundle().apply {
            putString("boss_id", boss.id)
            putString("zone", zone.zoneName)
        }
        firebase.logEvent("boss_defeated", params)
    }

    override fun logRocketUnlocked(rocket: RocketType) {
        val params = Bundle().apply {
            putString("rocket_id", rocket.name)
        }
        firebase.logEvent("rocket_unlocked", params)
    }

    override fun logModuleEquipped(moduleId: String, slot: Int) {
        val params = Bundle().apply {
            putString("module_id", moduleId)
            putInt("slot_index", slot)
        }
        firebase.logEvent("module_equipped", params)
    }

    override fun logScreenView(screenName: String, screenClass: String) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
        // Use standard screen_view event
        firebase.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)
    }

    override fun logAdImpression(adType: String, adUnitId: String) {
        val params = Bundle().apply {
            putString("ad_type", adType)
            putString("ad_unit_id", adUnitId)
        }
        firebase.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION, params)
    }

    override fun logAdClicked(adType: String, adUnitId: String) {
        val params = Bundle().apply {
            putString("ad_type", adType)
            putString("ad_unit_id", adUnitId)
        }
        firebase.logEvent("ad_clicked", params)
    }
}
