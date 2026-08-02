package com.ashwathai.jump_droid

import android.app.Activity
import android.content.Context
import androidx.core.content.edit
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Centralized Ad Manager for Jump Droid.
 * Handles pre-loading, frequency capping, and centralized ad orchestration.
 */
object AdManager {
    private var loadedRewardedAd: RewardedAd? = null
    private var isAdLoading = false

    var adsWatchedToday by mutableIntStateOf(0)
    var dailyStreak by mutableIntStateOf(1)
    private var lastAdDate by mutableStateOf("")

    private const val MAX_DAILY_REWARD_ADS = 5

    fun initialize(context: Context) {
        val sharedPrefs = context.getSharedPreferences("JumpDroidAds", Context.MODE_PRIVATE)
        adsWatchedToday = sharedPrefs.getInt("ads_watched_today", 0)
        lastAdDate = sharedPrefs.getString("last_ad_date", "") ?: ""
        dailyStreak = sharedPrefs.getInt("daily_streak", 1)

        refreshDailyCap(context)
        preloadRewardedAd(context)
    }

    fun refreshDailyCap(context: Context) {
        val sharedPrefs = context.getSharedPreferences("JumpDroidAds", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        
        if (today != lastAdDate) {
            // Check if streak is broken
            val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L))
            if (lastAdDate != yesterday && lastAdDate != "") {
                dailyStreak = 1
            } else if (lastAdDate != "") {
                dailyStreak = (dailyStreak + 1).coerceAtMost(5)
            }
            
            adsWatchedToday = 0
            lastAdDate = today
            sharedPrefs.edit {
                putInt("ads_watched_today", 0)
                putString("last_ad_date", today)
                putInt("daily_streak", dailyStreak)
            }
        }
    }

    private fun preloadRewardedAd(context: Context) {
        if (loadedRewardedAd != null || isAdLoading) return
        isAdLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, AdConfig.REWARDED_UNIT_ID, adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    loadedRewardedAd = ad
                    isAdLoading = false
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    loadedRewardedAd = null
                    isAdLoading = false
                }
            })
    }

    fun isRewardedAdReady(): Boolean = loadedRewardedAd != null

    fun showRewardedAd(
        activity: Activity,
        analytics: GameAnalytics,
        countsAgainstCap: Boolean = true,
        onReward: () -> Unit,
        onFailed: () -> Unit = {}
    ) {
        val ad = loadedRewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdImpression() {
                    analytics.logAdImpression("rewarded", AdConfig.REWARDED_UNIT_ID)
                }
                override fun onAdClicked() {
                    analytics.logAdClicked("rewarded", AdConfig.REWARDED_UNIT_ID)
                }
                override fun onAdDismissedFullScreenContent() {
                    loadedRewardedAd = null
                    preloadRewardedAd(activity)
                }
            }
            ad.show(activity) {
                if (countsAgainstCap) {
                    adsWatchedToday++
                    val sharedPrefs = activity.getSharedPreferences("JumpDroidAds", Context.MODE_PRIVATE)
                    sharedPrefs.edit { putInt("ads_watched_today", adsWatchedToday) }
                }
                onReward()
            }
        } else {
            preloadRewardedAd(activity)
            onFailed()
        }
    }

    fun isDailyDropReady(context: Context): Boolean {
        return getDailySupplyDropRemaining(context) <= 0L
    }

    fun getDailyDropRewards(): Pair<Int, Int> {
        val cash = 100 * dailyStreak
        val credits = if (dailyStreak >= 5) 2 else 1
        return credits to cash
    }

    fun canEarnMoreCredits(currentBalance: Int = 0): Boolean {
        return adsWatchedToday < MAX_DAILY_REWARD_ADS && currentBalance < 10
    }

    fun getDailySupplyDropRemaining(context: Context): Long {
        val sharedPrefs = context.getSharedPreferences("JumpDroidAds", Context.MODE_PRIVATE)
        val lastDropTime = sharedPrefs.getLong("last_supply_drop", 0L)
        val now = System.currentTimeMillis()
        val twentyFourHours = 24 * 60 * 60 * 1000L
        return (lastDropTime + twentyFourHours - now).coerceAtLeast(0L)
    }

    fun claimSupplyDrop(context: Context) {
        val sharedPrefs = context.getSharedPreferences("JumpDroidAds", Context.MODE_PRIVATE)
        sharedPrefs.edit().putLong("last_supply_drop", System.currentTimeMillis()).apply()
    }
}
