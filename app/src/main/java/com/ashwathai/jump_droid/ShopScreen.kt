package com.ashwathai.jump_droid

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
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
                Text(
                    "CURRENCY EXCHANGE",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        shadow = Shadow(SciFiCyan.copy(alpha = 0.3f), blurRadius = 10f)
                    ),
                    color = SciFiCyan,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(8.dp))

                Text(
                    "BALANCE: $cashBalance JC",
                    color = SciFiGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
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
                            Box(Modifier.size(10.dp).background(if (isPremium) SciFiGreen else SciFiGold, RoundedCornerShape(2.dp)))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "PREMIUM UPGRADE",
                                color = if (isPremium) SciFiGreen else SciFiGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (isPremium) "All advertisements have been removed. Thank you for your support!"
                            else "Remove all ads with a one-time purchase. Supports future development.",
                            color = SciFiWhite.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(Modifier.height(12.dp))
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
                            Text(
                                if (isPremium) "ADS REMOVED ✓" else "REMOVE ADS (\$1.99)",
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))

                Text(
                    "COMING IN V2.0.0",
                    color = SciFiWhite.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    letterSpacing = 3.sp
                )
                Spacer(Modifier.height(12.dp))

                val v2Items = listOf(
                    "ROCKET SKINS" to "Custom color schemes for your fleet",
                    "ENGINE TRAILS" to "Plasma, ice, and void effects",
                    "UI THEMES" to "HUD color schemes and layouts"
                )
                v2Items.forEach { (title, desc) ->
                    Surface(
                        color = SciFiSurface.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, SciFiBorder.copy(alpha = 0.2f))
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(title, color = SciFiWhite.copy(alpha = 0.3f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                Text(desc, color = SciFiWhite.copy(alpha = 0.2f), fontSize = 9.sp)
                            }
                            Text("V2", color = SciFiCyan.copy(alpha = 0.3f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
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
                    "V2.0.0 — FULL SHOP LAUNCH",
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
                    containerColor = Color(0xFF1A1A2E),
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
                    containerColor = Color(0xFF1A1A2E),
                    titleContentColor = SciFiGold,
                    textContentColor = SciFiWhite.copy(alpha = 0.8f)
                )
            }
        }
    }
}
