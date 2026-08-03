package com.ashwathai.jump_droid

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.LayoutInflater
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.ashwathai.jump_droid.ui.theme.*

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun NativeIntegratedAd(isPremiumUser: Boolean = false) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("JumpDroidPrefs", Context.MODE_PRIVATE) }
    val premium = if (BuildConfig.DEBUG) false else (isPremiumUser || prefs.getBoolean("premium_user", false))
    if (premium) return

    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    LaunchedEffect(Unit) {
        val adLoader = com.google.android.gms.ads.AdLoader.Builder(context, AdConfig.NATIVE_UNIT_ID)
            .forNativeAd { ad -> nativeAd = ad }
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    nativeAd?.let { ad ->
        AndroidView(
            factory = { ctx ->
                val adView = LayoutInflater.from(ctx).inflate(R.layout.ad_unified, null) as NativeAdView
                populateNativeAdView(ad, adView)
                adView
            },
            update = { adView -> populateNativeAdView(ad, adView) },
            modifier = Modifier.fillMaxWidth().height(100.dp)
        )
    } ?: Box(
        modifier = Modifier.fillMaxWidth().height(100.dp)
            .background(SciFiSurface, RoundedCornerShape(8.dp))
            .border(1.dp, SciFiBorder.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SYSTEM LINK STABILIZING...", color = SciFiWhite.copy(alpha = 0.2f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(modifier = Modifier.width(100.dp).height(2.dp), color = SciFiCyan.copy(alpha = 0.1f), trackColor = Color.Transparent)
        }
    }
}

private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
    adView.headlineView = adView.findViewById(R.id.ad_headline)
    adView.bodyView = adView.findViewById(R.id.ad_body)
    adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)

    (adView.headlineView as TextView).text = nativeAd.headline
    (adView.bodyView as TextView).text = nativeAd.body
    (adView.callToActionView as android.widget.Button).text = nativeAd.callToAction

    adView.setNativeAd(nativeAd)
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
            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdImpression() {
                    analytics.logAdImpression("rewarded", AdConfig.REWARDED_UNIT_ID)
                }
                override fun onAdClicked() {
                    analytics.logAdClicked("rewarded", AdConfig.REWARDED_UNIT_ID)
                }
            }
            ad.show(activity) { onReward() }
            loadedAd = null
        } ?: onFailed()
    }
}
