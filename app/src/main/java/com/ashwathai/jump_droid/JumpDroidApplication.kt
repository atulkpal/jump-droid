package com.ashwathai.jump_droid

import android.app.Application
import com.google.android.gms.ads.MobileAds

class JumpDroidApplication : Application() {
    lateinit var appOpenAdManager: AppOpenAdManager

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) {}
        appOpenAdManager = AppOpenAdManager(this)
    }
}
