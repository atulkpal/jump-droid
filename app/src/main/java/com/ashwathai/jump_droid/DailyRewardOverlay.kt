package com.ashwathai.jump_droid

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.jump_droid.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun DailyRewardOverlay(
    streak: Int,
    credits: Int,
    cash: Int,
    onClaim: () -> Unit,
    soundManager: SoundManager?,
    hapticManager: HapticManager? = null
) {
    var revealed by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "RewardGlow")
    val glowAlpha by infiniteTransition.animateFloat(0.3f, 0.7f, infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "Glow")
    
    val scale by animateFloatAsState(if (revealed) 1f else 0.5f, tween(800, easing = FastOutSlowInEasing))
    val alpha by animateFloatAsState(if (revealed) 1f else 0f, tween(800))

    LaunchedEffect(Unit) {
        delay(300)
        revealed = true
        soundManager?.playSfx("sfx_fanfare_mission")
        hapticManager?.vibrate(HapticManager.HapticType.SUCCESS)
        delay(400)
        hapticManager?.vibrate(HapticManager.HapticType.TICK)
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        // Background Glow
        Box(Modifier.size(400.dp).graphicsLayer(alpha = glowAlpha).background(Brush.radialGradient(listOf(SciFiGold.copy(alpha = 0.2f), Color.Transparent)), CircleShape))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
        ) {
            Text(
                "DAILY SUPPLY DROP",
                style = MaterialTheme.typography.displaySmall,
                color = SciFiGold,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )
            
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SciFiGold.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, SciFiGold.copy(alpha = 0.5f)),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    "DAY $streak STREAK ACTIVE",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    color = SciFiGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            Spacer(Modifier.height(32.dp))

            // Animated Crate / Reward Visual
            Box(Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val cx = size.width / 2
                    val cy = size.height / 2
                    val time = System.currentTimeMillis() / 1000f
                    
                    rotate(sin(time) * 5f, pivot = Offset(cx, cy)) {
                        val cratePath = Path().apply {
                            moveTo(cx - 60f, cy - 60f)
                            lineTo(cx + 60f, cy - 60f)
                            lineTo(cx + 80f, cy + 60f)
                            lineTo(cx - 80f, cy + 60f)
                            close()
                        }
                        drawPath(cratePath, SciFiSurface)
                        drawPath(cratePath, SciFiGold, style = Stroke(3f))
                        
                        // Crate Details
                        drawLine(SciFiGold, Offset(cx - 30f, cy - 30f), Offset(cx - 40f, cy + 30f), strokeWidth = 2f)
                        drawLine(SciFiGold, Offset(cx + 30f, cy - 30f), Offset(cx + 40f, cy + 30f), strokeWidth = 2f)
                    }
                }
                
                Icon(
                    painter = painterResource(id = R.drawable.ic_currency_cr),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp).offset(y = (-20).dp),
                    tint = SciFiGold
                )
            }

            Spacer(Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                RewardItem("+$credits", "CREDITS", SciFiGold)
                RewardItem("+$cash", "CASH", SciFiGreen)
            }

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = { 
                    soundManager?.playSfx("sfx_collect_item")
                    onClaim() 
                },
                modifier = Modifier.fillMaxWidth(0.8f).height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SciFiGold, contentColor = Color.Black)
            ) {
                Text("ESTABLISH LINK & CLAIM", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
private fun RewardItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 28.sp, fontWeight = FontWeight.Black)
        Text(label, color = color.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
    }
}
