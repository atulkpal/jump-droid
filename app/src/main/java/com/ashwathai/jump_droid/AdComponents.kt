package com.ashwathai.jump_droid

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun GlobalAdBanner(isPremiumUser: Boolean = false) {
    val context = LocalContext.current
    val analytics = LocalAnalytics.current
    val prefs = remember { context.getSharedPreferences("JumpDroidPrefs", Context.MODE_PRIVATE) }
    val premium = if (BuildConfig.DEBUG) false else (isPremiumUser || prefs.getBoolean("premium_user", false))
    if (premium) return

    AndroidView(
        factory = {
            AdView(context).apply {
                setAdSize(AdSize.SMART_BANNER)
                setAdUnitId(AdConfig.BANNER_UNIT_ID)
                adListener = object : com.google.android.gms.ads.AdListener() {
                    override fun onAdImpression() {
                        analytics.logAdImpression("banner", AdConfig.BANNER_UNIT_ID)
                    }
                    override fun onAdClicked() {
                        analytics.logAdClicked("banner", AdConfig.BANNER_UNIT_ID)
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

object RewardedAdHelper {
    private var loadedAd: com.google.android.gms.ads.rewarded.RewardedAd? = null

    fun load(context: Context) {
        val adRequest = AdRequest.Builder().build()
        com.google.android.gms.ads.rewarded.RewardedAd.load(context, AdConfig.REWARDED_UNIT_ID, adRequest,
            object : com.google.android.gms.ads.rewarded.RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: com.google.android.gms.ads.rewarded.RewardedAd) {
                    loadedAd = ad
                }
                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    loadedAd = null
                }
            })
    }

    fun show(activity: Activity, analytics: GameAnalytics, onReward: () -> Unit, onFailed: () -> Unit) {
        loadedAd?.let { ad ->
            analytics.logAdImpression("rewarded", AdConfig.REWARDED_UNIT_ID)
            ad.show(activity) { onReward() }
            loadedAd = null
        } ?: onFailed()
    }
}
