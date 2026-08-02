package com.ashwathai.jump_droid

/**
 * Authoritative configuration for AdMob unit IDs.
 * Automatically switches between Google sample IDs for debug and Production IDs for release.
 */
object AdConfig {
    const val APP_ID = "ca-app-pub-4153575596488132~9366217108"
    
    private const val PROD_BANNER_ID = "ca-app-pub-4153575596488132/9930957765"
    private const val PROD_REWARDED_ID = "ca-app-pub-4153575596488132/5256949651"
    private const val PROD_CONTINUE_REWARDED_ID = "ca-app-pub-4153575596488132/5155087899"
    private const val PROD_APP_OPEN_ID = "ca-app-pub-4153575596488132/2822358007"
    private const val PROD_REWARDED_INTERSTITIAL_ID = "ca-app-pub-4153575596488132/7507708475"
    private const val PROD_NATIVE_ID = "ca-app-pub-4153575596488132/8865288103"

    // Google Sample IDs for Safe Testing
    private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"
    private const val TEST_APP_OPEN_ID = "ca-app-pub-3940256099942544/9257395921"
    private const val TEST_REWARDED_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/5354046379"
    private const val TEST_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110"

    val BANNER_UNIT_ID: String
        get() = if (BuildConfig.DEBUG) TEST_BANNER_ID else PROD_BANNER_ID

    val REWARDED_UNIT_ID: String
        get() = if (BuildConfig.DEBUG) TEST_REWARDED_ID else PROD_REWARDED_ID

    val CONTINUE_REWARDED_UNIT_ID: String
        get() = if (BuildConfig.DEBUG) TEST_REWARDED_ID else PROD_CONTINUE_REWARDED_ID

    val APP_OPEN_UNIT_ID: String
        get() = if (BuildConfig.DEBUG) TEST_APP_OPEN_ID else PROD_APP_OPEN_ID

    val REWARDED_INTERSTITIAL_UNIT_ID: String
        get() = if (BuildConfig.DEBUG) TEST_REWARDED_INTERSTITIAL_ID else PROD_REWARDED_INTERSTITIAL_ID

    val NATIVE_UNIT_ID: String
        get() = if (BuildConfig.DEBUG) TEST_NATIVE_ID else PROD_NATIVE_ID
}
