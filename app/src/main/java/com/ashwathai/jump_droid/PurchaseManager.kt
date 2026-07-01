package com.ashwathai.jump_droid

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

class PurchaseManager(private val appContext: Context) {
    private val prefs: SharedPreferences = appContext.getSharedPreferences("JumpDroidPrefs", Context.MODE_PRIVATE)
    private var billingClient: BillingClient? = null

    val isPremiumUser: Boolean get() = prefs.getBoolean("premium_user", false)

    fun initialize() {
        billingClient = BillingClient.newBuilder(appContext)
            .setListener { result, purchases ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty()) {
                    for (purchase in purchases) {
                        if (purchase.products.contains("remove_ads") && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            acknowledgePurchase(purchase)
                            prefs.edit().putBoolean("premium_user", true).apply()
                        }
                    }
                }
            }
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    restorePurchases()
                }
            }
            override fun onBillingServiceDisconnected() {}
        })
    }

    private fun restorePurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType("inapp").build()
        ) { _, purchases ->
            for (purchase in purchases) {
                if (purchase.products.contains("remove_ads") && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    prefs.edit().putBoolean("premium_user", true).apply()
                    acknowledgePurchase(purchase)
                }
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, onFallback: () -> Unit) {
        if (isPremiumUser) return

        if (billingClient?.isReady == true) {
            val productParams = QueryProductDetailsParams.Product.newBuilder()
                .setProductId("remove_ads")
                .setProductType("inapp")
                .build()
            val queryParams = QueryProductDetailsParams.newBuilder()
                .setProductList(listOf(productParams))
                .build()

            billingClient?.queryProductDetailsAsync(queryParams) { result, details ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK && details.isNotEmpty()) {
                    val params = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(
                            listOf(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(details[0])
                                    .build()
                            )
                        )
                        .build()
                    billingClient?.launchBillingFlow(activity, params)
                } else {
                    onFallback()
                }
            }
        } else {
            onFallback()
        }
    }

    fun confirmPurchase() {
        prefs.edit().putBoolean("premium_user", true).apply()
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            billingClient?.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
            ) { _ -> }
        }
    }
}
