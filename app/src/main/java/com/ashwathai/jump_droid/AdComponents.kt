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

private const val TEST_BANNER_UNIT = "ca-app-pub-4153575596488132/3022346201"

@Composable
fun GlobalAdBanner(isPremiumUser: Boolean = false) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("JumpDroidPrefs", Context.MODE_PRIVATE) }
    val premium = isPremiumUser || prefs.getBoolean("premium_user", false)
    if (premium) return

    AndroidView(
        factory = {
            AdView(context).apply {
                setAdSize(AdSize.SMART_BANNER)
                setAdUnitId(TEST_BANNER_UNIT)
                loadAd(AdRequest.Builder().build())
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

object RewardedAdHelper {
    private const val TEST_REWARDED_UNIT = "ca-app-pub-4153575596488132/5155087899"

    private var loadedAd: com.google.android.gms.ads.rewarded.RewardedAd? = null

    fun load(context: Context) {
        val adRequest = AdRequest.Builder().build()
        com.google.android.gms.ads.rewarded.RewardedAd.load(context, TEST_REWARDED_UNIT, adRequest,
            object : com.google.android.gms.ads.rewarded.RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: com.google.android.gms.ads.rewarded.RewardedAd) {
                    loadedAd = ad
                }
                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    loadedAd = null
                }
            })
    }

    fun show(activity: Activity, onReward: () -> Unit, onFailed: () -> Unit) {
        loadedAd?.let { ad ->
            ad.show(activity) { onReward() }
            loadedAd = null
        } ?: onFailed()
    }
}
