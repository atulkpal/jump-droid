package com.ashwathai.jump_droid

/**
 * Authoritative configuration for AdMob unit IDs.
 * Automatically switches between Google sample IDs for debug and Production IDs for release.
 */
object AdConfig {
    const val APP_ID = "ca-app-pub-4153575596488132~9366217108"
    
    private const val PROD_BANNER_ID = "ca-app-pub-4153575596488132/3022346201"
    private const val PROD_REWARDED_ID = "ca-app-pub-4153575596488132/5155087899"

    // Google Sample IDs for Safe Testing
    private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    val BANNER_UNIT_ID: String
        get() = if (BuildConfig.DEBUG) TEST_BANNER_ID else PROD_BANNER_ID

    val REWARDED_UNIT_ID: String
        get() = if (BuildConfig.DEBUG) TEST_REWARDED_ID else PROD_REWARDED_ID
}
