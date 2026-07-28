package com.ashwathai.jump_droid

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.QueryPurchasesParams

class PurchaseManager(private val appContext: Context) {
    private val prefs: SharedPreferences = appContext.getSharedPreferences("JumpDroidPrefs", Context.MODE_PRIVATE)
    private var billingClient: BillingClient? = null
    private var reconnectCount = 0
    private val MAX_RECONNECT = 3

    val isPremiumUser: Boolean get() = prefs.getBoolean("premium_user", false)

    fun initialize() {
        Log.d("PurchaseManager", "Initializing BillingClient...")
        billingClient = BillingClient.newBuilder(appContext)
            .setListener { result, purchases ->
                Log.d("PurchaseManager", "OnPurchasesUpdated: ${result.responseCode} - ${result.debugMessage}")
                if (result.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty()) {
                    for (purchase in purchases) {
                        if (purchase.products.contains("remove_ads") && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            acknowledgePurchase(purchase)
                            prefs.edit().putBoolean("premium_user", true).apply()
                        }
                    }
                }
            }
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .enableAutoServiceReconnection()
            .build()

        connectToBilling()
    }

    private fun connectToBilling() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                Log.d("PurchaseManager", "BillingSetupFinished: ${result.responseCode} - ${result.debugMessage}")
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    reconnectCount = 0
                    restorePurchases()
                } else if (reconnectCount < MAX_RECONNECT) {
                    reconnectCount++
                    Log.w("PurchaseManager", "Billing setup failed. Retry $reconnectCount/$MAX_RECONNECT")
                }
            }
            override fun onBillingServiceDisconnected() {
                Log.w("PurchaseManager", "Billing service disconnected.")
            }
        })
    }

    private fun restorePurchases() {
        Log.d("PurchaseManager", "Restoring purchases...")
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
        Log.d("PurchaseManager", "Launching purchase flow for 'remove_ads'...")
        if (isPremiumUser) return

        if (billingClient?.isReady == true) {
            val productParams = QueryProductDetailsParams.Product.newBuilder()
                .setProductId("remove_ads")
                .setProductType("inapp")
                .build()
            val queryParams = QueryProductDetailsParams.newBuilder()
                .setProductList(listOf(productParams))
                .build()

            billingClient?.queryProductDetailsAsync(queryParams) { result, queryProductDetailsResult ->
                Log.d("PurchaseManager", "QueryProductDetails: ${result.responseCode}")
                val details = queryProductDetailsResult.productDetailsList
                if (result.responseCode == BillingClient.BillingResponseCode.OK && !details.isNullOrEmpty()) {
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
                    Log.e("PurchaseManager", "Failed to query product details or empty list: ${result.debugMessage}")
                    onFallback()
                }
            }
        } else {
            Log.w("PurchaseManager", "Billing client NOT ready. Attempting reconnection...")
            connectToBilling()
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
