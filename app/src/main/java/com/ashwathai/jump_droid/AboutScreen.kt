package com.ashwathai.jump_droid

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.ashwathai.jump_droid.ui.theme.SciFiPurple
import com.ashwathai.jump_droid.ui.theme.SciFiRed
import com.ashwathai.jump_droid.ui.theme.SciFiSurface
import com.ashwathai.jump_droid.ui.theme.SciFiWhite

@Composable
fun AboutScreen(
    purchaseManager: PurchaseManager?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showUpgradeDialog by remember { mutableStateOf(false) }
    val isPremium = purchaseManager?.isPremiumUser ?: false

    Surface(Modifier.fillMaxSize(), color = SciFiBackground) {
        Box {
            StarfieldBackground(Modifier.fillMaxSize(), starCount = 40, alphaRange = 0.15f..0.55f, starColor = SciFiCyan)
            
            Column(Modifier.padding(24.dp).verticalScroll(rememberScrollState()).safeDrawingPadding()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_station_inf),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "SYSTEM PROTOCOL",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            shadow = Shadow(SciFiCyan.copy(alpha = 0.4f), blurRadius = 12f)
                        ),
                        color = SciFiCyan,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Core Identity Card
                ProtocolCard(title = "MISSION OBJECTIVE") {
                    Text(
                        text = "Jump Droid is a precision vertical exploration simulator. Master modular loadouts, navigate hostile biomes, and ascend to the Singularity. Built with Jetpack Compose.",
                        color = SciFiWhite.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                // External Links
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://jump-droid.vercel.app"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(0.8f).height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SciFiCyan.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, SciFiCyan.copy(alpha = 0.4f))
                    ) {
                        Text("OFFICIAL WEBSITE", color = SciFiCyan, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Support Indie CTA
                Surface(
                    color = SciFiGold.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SciFiGold.copy(alpha = 0.2f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "SUPPORT INDIE DEVELOPMENT",
                            color = SciFiGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Jump Droid is a passion project. If you enjoy the flight, consider supporting us by removing ads.",
                            color = SciFiWhite.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { 
                                if (!isPremium) showUpgradeDialog = true 
                            },
                            enabled = !isPremium,
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPremium) SciFiGreen.copy(alpha = 0.2f) else SciFiGold,
                                contentColor = if (isPremium) SciFiGreen else Color.Black
                            ),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    if (isPremium) "ELITE STATUS ACTIVE" else "GO PREMIUM",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                                if (!isPremium && purchaseManager?.hasOffer == true) {
                                    Spacer(Modifier.width(8.dp))
                                    DiscountFlyer(
                                        text = purchaseManager.offerText,
                                        urgencyText = purchaseManager.offerExpiryText
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                ProtocolCard(title = "TECHNICAL DATA") {
                    TechRow("VERSION", BuildConfig.VERSION_NAME)
                    TechRow("ENGINE", "ASHWATH.AI PROTOTYPE")
                    TechRow("FRAMEWORK", "JETPACK COMPOSE")
                    TechRow("ZONES", "12 UNIQUE BIOMES")
                    TechRow("THREATS", "100+ ENTITIES")
                }

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = SciFiButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                    border = BorderStroke(1.dp, SciFiBorder)
                ) {
                    Text("DISMISS", color = SciFiWhite, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                
                Spacer(Modifier.height(16.dp))
                Text(
                    "POWERED BY ASHWATH.AI // 2026",
                    color = SciFiWhite.copy(alpha = 0.2f),
                    fontSize = 8.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            if (showUpgradeDialog) {
                EliteUpgradeDialog(
                    purchaseManager = purchaseManager,
                    onDismiss = { showUpgradeDialog = false }
                )
            }
        }
    }
}

@Composable
private fun ProtocolCard(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            title,
            color = SciFiCyan.copy(alpha = 0.6f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(8.dp))
        Surface(
            color = SciFiSurface.copy(alpha = 0.4f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, SciFiBorder.copy(alpha = 0.1f))
        ) {
            Column(Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun TechRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = SciFiWhite.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = SciFiWhite.copy(alpha = 0.7f), fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
    }
}
