package com.ashwathai.jump_droid

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

object RewardedInterstitialHelper {
    private var loadedAd: RewardedInterstitialAd? = null

    fun load(context: Context) {
        val adRequest = AdRequest.Builder().build()
        RewardedInterstitialAd.load(context, AdConfig.REWARDED_INTERSTITIAL_UNIT_ID, adRequest,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    loadedAd = ad
                }
                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    loadedAd = null
                }
            })
    }

    fun show(activity: Activity, onReward: (Int) -> Unit) {
        loadedAd?.let { ad ->
            ad.show(activity) { rewardItem ->
                onReward(rewardItem.amount)
            }
            loadedAd = null
            load(activity)
        }
    }
}
