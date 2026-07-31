package com.ashwathai.jump_droid

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    var premiumPrice by mutableStateOf("$1.99")
        private set
    var hasOffer by mutableStateOf(false)
        private set
    var offerText by mutableStateOf("")
        private set
    var offerExpiryText by mutableStateOf("")
        private set
    private var bestOfferToken: String? = null

    companion object {
        const val PREMIUM_PRODUCT_ID = "jumpdroid_premium01"
    }

    val isPremiumUser: Boolean get() = prefs.getBoolean("premium_user", false)

    fun initialize() {
        Log.d("PurchaseManager", "Initializing BillingClient...")
        billingClient = BillingClient.newBuilder(appContext)
            .setListener { result, purchases ->
                Log.d("PurchaseManager", "OnPurchasesUpdated: ${result.responseCode} - ${result.debugMessage}")
                if (result.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty()) {
                    for (purchase in purchases) {
                        if (purchase.products.contains(PREMIUM_PRODUCT_ID) && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
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
                    refreshProductDetails()
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
                if (purchase.products.contains(PREMIUM_PRODUCT_ID) && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    prefs.edit().putBoolean("premium_user", true).apply()
                    acknowledgePurchase(purchase)
                }
            }
        }
    }

    fun refreshProductDetails() {
        if (billingClient?.isReady == true) {
            val productParams = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_PRODUCT_ID)
                .setProductType("inapp")
                .build()
            val queryParams = QueryProductDetailsParams.newBuilder()
                .setProductList(listOf(productParams))
                .build()

            billingClient?.queryProductDetailsAsync(queryParams) { result, queryProductDetailsResult ->
                val detailsList = queryProductDetailsResult.productDetailsList
                if (result.responseCode == BillingClient.BillingResponseCode.OK && !detailsList.isNullOrEmpty()) {
                    val productDetails = detailsList[0]
                    val offers = productDetails.oneTimePurchaseOfferDetailsList
                    
                    if (!offers.isNullOrEmpty()) {
                        // Find the cheapest offer
                        val bestOffer = offers.minByOrNull { it.priceAmountMicros }
                        if (bestOffer != null) {
                            premiumPrice = bestOffer.formattedPrice
                            bestOfferToken = bestOffer.offerToken
                            
                            // Native Discount Detection
                            val discountInfo = bestOffer.discountDisplayInfo
                            val fullPriceMicros = bestOffer.fullPriceMicros
                            
                            if (discountInfo != null) {
                                hasOffer = true
                                offerText = "${discountInfo.percentageDiscount}% OFF"
                            } else if (fullPriceMicros != null && fullPriceMicros > bestOffer.priceAmountMicros) {
                                hasOffer = true
                                val percent = ((fullPriceMicros - bestOffer.priceAmountMicros).toDouble() / fullPriceMicros * 100).toInt()
                                offerText = "$percent% OFF"
                            } else {
                                // Fallback to description scan if no native discount fields found
                                val desc = productDetails.description.uppercase()
                                val title = productDetails.name.uppercase()
                                val regex = Regex("(\\d+%)")
                                val match = regex.find(desc) ?: regex.find(title)
                                
                                if (match != null) {
                                    hasOffer = true
                                    offerText = "${match.value} OFF"
                                } else {
                                    hasOffer = false
                                    offerText = ""
                                }

                                // Subtle Urgency Logic
                                val endTime = bestOffer.validTimeWindow?.endTimeMillis ?: 0L
                                if (endTime > 0) {
                                    val remaining = endTime - System.currentTimeMillis()
                                    val days = remaining / (1000 * 60 * 60 * 24)
                                    val hours = remaining / (1000 * 60 * 60)
                                    
                                    offerExpiryText = when {
                                        remaining <= 0 -> ""
                                        days >= 3 -> "" // Too far out, keep it subtle
                                        days >= 1 -> "ENDS IN $days DAYS"
                                        hours >= 1 -> "ENDS IN $hours HOURS"
                                        else -> "ENDING SOON"
                                    }
                                } else {
                                    offerExpiryText = ""
                                }
                            }
                        }
                    }
                    Log.d("PurchaseManager", "ProductDetails: $premiumPrice, hasOffer: $hasOffer, offer: $offerText, expiry: $offerExpiryText")
                }
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, onFallback: () -> Unit) {
        Log.d("PurchaseManager", "Launching purchase flow for '$PREMIUM_PRODUCT_ID'...")
        if (isPremiumUser) return

        if (billingClient?.isReady == true) {
            val productParams = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_PRODUCT_ID)
                .setProductType("inapp")
                .build()
            val queryParams = QueryProductDetailsParams.newBuilder()
                .setProductList(listOf(productParams))
                .build()

            billingClient?.queryProductDetailsAsync(queryParams) { result, queryProductDetailsResult ->
                Log.d("PurchaseManager", "QueryProductDetails: ${result.responseCode}")
                val details = queryProductDetailsResult.productDetailsList
                if (result.responseCode == BillingClient.BillingResponseCode.OK && !details.isNullOrEmpty()) {
                    val flowDetails = BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details[0])
                    
                    bestOfferToken?.let { 
                        flowDetails.setOfferToken(it)
                    }
                    
                    val params = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(listOf(flowDetails.build()))
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
