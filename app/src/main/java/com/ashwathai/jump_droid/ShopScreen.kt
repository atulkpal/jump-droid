package com.ashwathai.jump_droid

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.jump_droid.ui.theme.SciFiBackground
import com.ashwathai.jump_droid.ui.theme.SciFiBorder
import com.ashwathai.jump_droid.ui.theme.SciFiButtonShape
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import com.ashwathai.jump_droid.ui.theme.SciFiGold
import com.ashwathai.jump_droid.ui.theme.SciFiGreen
import com.ashwathai.jump_droid.ui.theme.SciFiRed
import com.ashwathai.jump_droid.ui.theme.SciFiSurface
import com.ashwathai.jump_droid.ui.theme.SciFiWhite

@Composable
fun ShopScreen(
    progressionManager: ProgressionManager,
    purchaseManager: PurchaseManager?,
    soundManager: SoundManager?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isPremium by remember { mutableStateOf(purchaseManager?.isPremiumUser ?: false) }
    val cashBalance = progressionManager.getCashBalance()
    var showDebugPurchaseDialog by remember { mutableStateOf(false) }
    var showStoreDialog by remember { mutableStateOf(false) }

    Surface(Modifier.fillMaxSize(), color = SciFiBackground) {
        Box {
            StarfieldBackground(Modifier.fillMaxSize(), starCount = 30, alphaRange = 0.1f..0.4f, starColor = SciFiCyan)
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                drawCircle(SciFiGold.copy(alpha = 0.04f), radius = 60f, center = Offset(w * 0.3f, h * 0.2f))
                drawCircle(SciFiCyan.copy(alpha = 0.03f), radius = 80f, center = Offset(w * 0.7f, h * 0.8f))
            }

            Column(
                Modifier.padding(24.dp).verticalScroll(rememberScrollState()).safeDrawingPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_btn_shop),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "CURRENCY EXCHANGE",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            shadow = Shadow(SciFiCyan.copy(alpha = 0.3f), blurRadius = 10f)
                        ),
                        color = SciFiCyan,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "BALANCE: $cashBalance",
                        color = SciFiGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.width(6.dp))
                    Image(
                        painter = painterResource(id = R.drawable.ic_currency_jc),
                        contentDescription = "JC",
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    "JUMP CREDITS",
                    color = SciFiWhite.copy(alpha = 0.3f),
                    fontSize = 9.sp,
                    letterSpacing = 3.sp
                )
                Spacer(Modifier.height(32.dp))

                Surface(
                    color = SciFiSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isPremium) SciFiGreen.copy(alpha = 0.5f) else SciFiGold.copy(alpha = 0.5f))
                ) {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_premium_star),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                colorFilter = ColorFilter.tint(if (isPremium) SciFiGreen else SciFiGold)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                if (isPremium) "FLEET COMMAND ELITE" else "PREMIUM UPGRADE",
                                color = if (isPremium) SciFiGreen else SciFiGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        if (!isPremium) {
                            Spacer(Modifier.height(12.dp))
                            EliteBenefitsList()
                        } else {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "All advertisements have been removed. Thank you for your support!",
                                color = SciFiWhite.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                soundManager?.playSfx("sfx_ui_click")
                                if (!isPremium) {
                                    purchaseManager?.launchPurchaseFlow(context as Activity) {
                                        if (BuildConfig.DEBUG) showDebugPurchaseDialog = true else showStoreDialog = true
                                    }
                                }
                            },
                            enabled = !isPremium,
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPremium) SciFiGreen.copy(alpha = 0.15f) else SciFiGold.copy(alpha = 0.2f),
                                contentColor = if (isPremium) SciFiGreen else SciFiGold,
                                disabledContainerColor = SciFiGreen.copy(alpha = 0.15f),
                                disabledContentColor = SciFiGreen.copy(alpha = 0.5f)
                            ),
                            border = BorderStroke(1.dp, if (isPremium) SciFiGreen.copy(alpha = 0.3f) else SciFiGold.copy(alpha = 0.5f))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Text(
                                    if (isPremium) "ELITE STATUS ACTIVE ✓" else "UPGRADE TO ELITE (${purchaseManager?.premiumPrice ?: "$1.99"})",
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                if (!isPremium && (purchaseManager?.hasOffer == true)) {
                                    Spacer(Modifier.width(12.dp))
                                    DiscountFlyer(
                                        text = purchaseManager.offerText,
                                        urgencyText = purchaseManager.offerExpiryText,
                                        severity = purchaseManager.urgencySeverity
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))

                // --- Continue Credit Purchase ---
                val currentRate = progressionManager.getCurrentCreditRate()
                Surface(
                    color = SciFiSurface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, SciFiCyan.copy(alpha = 0.3f))
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("BANK CONTINUE CREDITS", color = SciFiCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
                            Text("$currentRate JC = 1 CREDIT (${progressionManager.creditBalance}/${progressionManager.maxCredits})", color = SciFiWhite.copy(alpha = 0.4f), fontSize = 9.sp)
                        }
                        Button(
                            onClick = {
                                if (progressionManager.spendCash(currentRate)) {
                                    progressionManager.addCredits(1)
                                    soundManager?.playSfx("sfx_ui_confirm")
                                } else {
                                    soundManager?.playSfx("sfx_ui_error")
                                }
                            },
                            enabled = progressionManager.totalCash >= currentRate && progressionManager.creditBalance < progressionManager.maxCredits,
                            colors = ButtonDefaults.buttonColors(containerColor = SciFiCyan.copy(alpha = 0.2f), contentColor = SciFiCyan),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("PURCHASE", fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "COSMETIC REQUISITION",
                    color = SciFiGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(12.dp))

                // Trails
                Text("ENGINE TRAILS", color = SciFiWhite.copy(alpha = 0.3f), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    maxItemsInEachRow = 2
                ) {
                    EngineTrailRegistry.trails.filter { !it.isDefault }.forEach { trail ->
                        Box(Modifier.fillMaxWidth(0.48f).padding(bottom = 10.dp)) {
                            CosmeticPurchaseCard(
                                name = trail.name,
                                price = trail.price,
                                isUnlocked = progressionManager.isTrailUnlocked(trail.id),
                                accentColor = trail.trailColor,
                                iconRes = R.drawable.ic_shop_trails,
                                onPurchase = {
                                    if (progressionManager.spendCash(trail.price)) {
                                        progressionManager.unlockTrail(trail.id)
                                        soundManager?.playSfx("sfx_ui_confirm")
                                    } else {
                                        soundManager?.playSfx("sfx_ui_error")
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                // Paint Schemes
                Text("PAINT SCHEMES", color = SciFiWhite.copy(alpha = 0.3f), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    maxItemsInEachRow = 2
                ) {
                    PaintRegistry.paints.filter { !it.isDefault }.forEach { paint ->
                        Box(Modifier.fillMaxWidth(0.48f).padding(bottom = 10.dp)) {
                            CosmeticPurchaseCard(
                                name = paint.name,
                                price = paint.price,
                                isUnlocked = progressionManager.isPaintUnlocked(paint.id),
                                accentColor = paint.accentColor,
                                iconRes = R.drawable.ic_shop_skins,
                                onPurchase = {
                                    if (progressionManager.spendCash(paint.price)) {
                                        progressionManager.unlockPaint(paint.id)
                                        soundManager?.playSfx("sfx_ui_confirm")
                                    } else {
                                        soundManager?.playSfx("sfx_ui_error")
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = {
                        soundManager?.playSfx("sfx_ui_click")
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = SciFiButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                    border = BorderStroke(1.dp, SciFiBorder)
                ) {
                    Text("BACK TO COMMAND", color = SciFiWhite, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "V2.2.3 — ZEN MASTERY UPDATE",
                    color = SciFiWhite.copy(alpha = 0.15f),
                    fontSize = 8.sp,
                    letterSpacing = 2.sp
                )
            }

            if (showDebugPurchaseDialog) {
                AlertDialog(
                    onDismissRequest = { showDebugPurchaseDialog = false },
                    title = { Text("Purchase Remove Ads?", color = SciFiWhite, fontWeight = FontWeight.Bold) },
                    text = { Text("Remove all ads for a one-time payment of \$1.99.", color = SciFiWhite.copy(alpha = 0.8f)) },
                    confirmButton = {
                        TextButton(onClick = {
                            purchaseManager?.confirmPurchase()
                            isPremium = true
                            showDebugPurchaseDialog = false
                        }) { Text("PURCHASE", color = SciFiGold, fontWeight = FontWeight.Bold) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDebugPurchaseDialog = false }) { Text("CANCEL", color = SciFiWhite.copy(alpha = 0.5f)) }
                    },
                    containerColor = SciFiSurface,
                    titleContentColor = SciFiWhite,
                    textContentColor = SciFiWhite.copy(alpha = 0.8f)
                )
            }
            if (showStoreDialog) {
                AlertDialog(
                    onDismissRequest = { showStoreDialog = false },
                    title = { Text("PLAY STORE REQUIRED", color = SciFiGold, fontWeight = FontWeight.Bold) },
                    text = { Text("Premium purchase is only available through the Google Play Store.\n\nDownload Jump Droid from the Play Store to remove ads.", color = SciFiWhite.copy(alpha = 0.8f)) },
                    confirmButton = {
                        TextButton(onClick = { showStoreDialog = false }) { Text("DISMISS", color = SciFiGold, fontWeight = FontWeight.Bold) }
                    },
                    containerColor = SciFiSurface,
                    titleContentColor = SciFiGold,
                    textContentColor = SciFiWhite.copy(alpha = 0.8f)
                )
            }
        }
    }
}


@Composable
private fun CosmeticPurchaseCard(
    name: String,
    price: Int,
    isUnlocked: Boolean,
    accentColor: Color,
    iconRes: Int,
    onPurchase: () -> Unit
) {
    Surface(
        color = SciFiSurface.copy(alpha = 0.5f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, if (isUnlocked) accentColor.copy(alpha = 0.3f) else SciFiBorder.copy(alpha = 0.1f))
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(if (isUnlocked) accentColor else SciFiWhite.copy(alpha = 0.3f))
            )
            Spacer(Modifier.height(8.dp))
            Text(
                name,
                color = if (isUnlocked) SciFiWhite else SciFiWhite.copy(alpha = 0.4f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                if (isUnlocked) "UNLOCKED" else "LOCKED",
                color = if (isUnlocked) SciFiGreen.copy(alpha = 0.6f) else SciFiRed.copy(alpha = 0.4f),
                fontSize = 7.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(12.dp))
            if (!isUnlocked) {
                Button(
                    onClick = onPurchase,
                    colors = ButtonDefaults.buttonColors(containerColor = SciFiGold.copy(alpha = 0.2f), contentColor = SciFiGold),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth().height(28.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text("$price JC", fontSize = 8.sp, fontWeight = FontWeight.Black)
                }
            } else {
                Box(
                    Modifier.fillMaxWidth().height(28.dp).background(SciFiGreen.copy(alpha = 0.1f), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("OWNED", color = SciFiGreen, fontSize = 8.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
