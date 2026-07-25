package com.ashwathai.jump_droid

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.jump_droid.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun BossArrivalOverlay(
    event: BossArrivalEvent,
    timer: Float,
    gameTime: Long
) {
    val progress = (1f - timer / 4f).coerceIn(0f, 1f)
    val dimAlpha = (0.6f * (1f - progress * 0.5f)).coerceIn(0f, 1f)
    val textAlpha = (progress * 3f).coerceIn(0f, 1f)
    val scalePulse = rememberInfiniteTransition(label = "BossScalePulse").animateFloat(0.95f, 1.05f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "BossScalePulseVal")

    val zoneColor = when (event.zone) {
        AltitudeZone.EARTH -> SciFiGreen
        AltitudeZone.CLOUD_LAYER -> SciFiCyan
        AltitudeZone.UPPER_ATMOSPHERE -> SciFiGold
        AltitudeZone.ORBIT -> SciFiGold
        AltitudeZone.THE_FOUNDRY -> SciFiRed
        AltitudeZone.DEEP_SPACE -> SciFiPurple
        AltitudeZone.CHRONO_RIFT -> SciFiPurple
        AltitudeZone.VOID -> SciFiRed
        AltitudeZone.THE_BEYOND -> SciFiGold
        AltitudeZone.STELLAR_GATE -> SciFiCyan
        AltitudeZone.ANCIENT_CONSTRUCT -> SciFiGold
        AltitudeZone.SINGULARITY -> Color(0xFFD500F9)
    }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = dimAlpha)), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2
            val cy = h / 2

            val ringRadius = 80f + progress * 200f
            drawCircle(zoneColor.copy(alpha = 0.15f * (1f - progress)), radius = ringRadius, center = Offset(cx, cy), style = Stroke(width = 2f))
            drawCircle(zoneColor.copy(alpha = 0.1f * (1f - progress)), radius = ringRadius * 0.7f, center = Offset(cx, cy), style = Stroke(width = 1f))

            repeat(12) { i ->
                val angle = (i * 30f + gameTime * 0.05f)
                val r = 120f + progress * 150f
                val px = cx + cos(angle * 0.01745f) * r
                val py = cy + sin(angle * 0.01745f) * r
                drawCircle(zoneColor.copy(alpha = 0.2f * (1f - progress) * scalePulse.value), radius = 3f, center = Offset(px, py))
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                "WARNING",
                color = zoneColor.copy(alpha = textAlpha),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                fontSize = 14.sp,
                letterSpacing = 6.sp
            )

            Spacer(Modifier.height(16.dp))

            Box(
                Modifier.size(80.dp).background(zoneColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.size(50.dp)) {
                    val bx = size.width / 2
                    val by = size.height / 2
                    val s = 3f
                    val bodyH = 20f * s
                    val bodyW = 8f * s

                    drawRoundRect(
                        zoneColor.copy(alpha = 0.6f),
                        topLeft = Offset(bx - bodyW / 2, by - bodyH / 2),
                        size = Size(bodyW, bodyH),
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                    val nose = Path().apply {
                        moveTo(bx - bodyW / 2, by - bodyH / 2)
                        lineTo(bx, by - bodyH / 2 - 14f * s)
                        lineTo(bx + bodyW / 2, by - bodyH / 2)
                        close()
                    }
                    drawPath(nose, zoneColor.copy(alpha = 0.6f))
                    val leftFin = Path().apply {
                        moveTo(bx - bodyW / 2, by - 10f)
                        lineTo(bx - bodyW / 2 - 8f * s, by + bodyH / 2)
                        lineTo(bx - bodyW / 2, by + bodyH / 2)
                        close()
                    }
                    drawPath(leftFin, zoneColor.copy(alpha = 0.5f))
                    val rightFin = Path().apply {
                        moveTo(bx + bodyW / 2, by - 10f)
                        lineTo(bx + bodyW / 2 + 8f * s, by + bodyH / 2)
                        lineTo(bx + bodyW / 2, by + bodyH / 2)
                        close()
                    }
                    drawPath(rightFin, zoneColor.copy(alpha = 0.5f))
                    drawCircle(Color.White.copy(alpha = 0.4f), radius = 3f, center = Offset(bx, by - 5f))
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                event.bossName.uppercase(),
                color = zoneColor.copy(alpha = textAlpha),
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                fontSize = 20.sp,
                letterSpacing = 3.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.graphicsLayer { scaleX = scalePulse.value; scaleY = scalePulse.value }
            )

            if (progress < 0.8f) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "INCOMING HOSTILE",
                    color = SciFiWhite.copy(alpha = textAlpha * 0.4f),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 4.sp
                )
            }
        }
    }
}
