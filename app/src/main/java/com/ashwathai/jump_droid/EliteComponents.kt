package com.ashwathai.jump_droid

import android.app.Activity
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.jump_droid.ui.theme.*

@Composable
fun EliteBenefitsList() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EliteBenefitItem("AD-FREE COMMAND", "Remove all intermittent and optional ads.")
        EliteBenefitItem("ELITE IDENT", "Exclusive Supporter badge on your profile.")
        EliteBenefitItem("PRIORITY ACCESS", "Early testing of experimental engine assets.")
    }
}

@Composable
private fun EliteBenefitItem(title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            "★",
            color = SciFiGold,
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, color = SciFiWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(desc, color = SciFiWhite.copy(alpha = 0.6f), fontSize = 10.sp)
        }
    }
}

@Composable
fun DiscountFlyer(text: String, urgencyText: String = "", severity: Int = 0) {
    val infiniteTransition = rememberInfiniteTransition(label = "FlyerTransition")
    
    val pulseDuration = when (severity) {
        3 -> 400  // Critical: Fast
        2 -> 800  // High: Medium
        else -> 1500 // Normal: Slow
    }

    val alpha by infiniteTransition.animateFloat(
        initialValue = if (severity >= 2) 0.5f else 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(pulseDuration), RepeatMode.Reverse),
        label = "Alpha"
    )

    val bgColor = when (severity) {
        3 -> SciFiRed
        2 -> SciFiOrange
        else -> SciFiGold
    }

    val textColor = when (severity) {
        3 -> Color.White
        else -> Color.Black
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            color = bgColor.copy(alpha = alpha),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                maxLines = 1
            )
        }
        if (urgencyText.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = urgencyText,
                color = if (severity >= 3) SciFiRed.copy(alpha = alpha) else SciFiGold.copy(alpha = alpha),
                fontSize = 7.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun EliteUpgradeDialog(
    purchaseManager: PurchaseManager?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SciFiSurface,
        titleContentColor = SciFiGold,
        textContentColor = SciFiWhite.copy(alpha = 0.8f),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_premium_star),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    colorFilter = ColorFilter.tint(SciFiGold)
                )
                Spacer(Modifier.width(12.dp))
                Text("FLEET COMMAND ELITE", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                
                if (purchaseManager?.hasOffer == true) {
                    Spacer(Modifier.weight(1f))
                    DiscountFlyer(
                        text = purchaseManager.offerText,
                        urgencyText = purchaseManager.offerExpiryText,
                        severity = purchaseManager.urgencySeverity
                    )
                }
            }
        },
        text = {
            Column {
                EliteBenefitsList()
                Spacer(Modifier.height(16.dp))
                Text(
                    "One-time purchase supports all future engine development and zone expansions.",
                    fontSize = 10.sp,
                    color = SciFiGold.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val activity = context.findActivity()
                    if (activity != null) {
                        purchaseManager?.launchPurchaseFlow(activity) {
                            // Fallback logic handled by the caller or inside PurchaseManager
                        }
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SciFiGold),
                shape = RoundedCornerShape(4.dp)
            ) {
                val price = purchaseManager?.premiumPrice ?: "Rs 200"
                Text("UPGRADE ($price)", color = Color.Black, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("LATER", color = SciFiWhite.copy(alpha = 0.5f))
            }
        }
    )
}
