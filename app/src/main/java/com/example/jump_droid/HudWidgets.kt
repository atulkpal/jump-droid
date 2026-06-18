package com.example.jump_droid

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jump_droid.ui.theme.SciFiBorder
import com.example.jump_droid.ui.theme.SciFiCyan
import com.example.jump_droid.ui.theme.SciFiGold
import com.example.jump_droid.ui.theme.SciFiGreen
import com.example.jump_droid.ui.theme.SciFiPurple
import com.example.jump_droid.ui.theme.SciFiRed
import com.example.jump_droid.ui.theme.SciFiSurface
import com.example.jump_droid.ui.theme.SciFiWhite
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun AltitudeDisplay(score: Int, highScore: Int) {
    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                shadow = Shadow(SciFiCyan.copy(alpha = 0.3f), blurRadius = 15f)
            ),
            color = SciFiWhite
        )
        Text(
            text = "ASCENSION DATA: BEST $highScore",
            style = MaterialTheme.typography.labelSmall,
            color = SciFiWhite.copy(alpha = 0.5f),
            fontSize = 10.sp,
            letterSpacing = 2.sp
        )
    }
}

@Composable
fun FuelGauge(
    fuel: Float,
    maxFuel: Float,
    gameTime: Long
) {
    val gaugeHeight = (120f + (maxFuel - 100f) * 0.6f).coerceIn(100f, 250f).dp
    val isLow = fuel < 20f
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "\u26FD",
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 4.dp).graphicsLayer(alpha = if (isLow) (gameTime / 200 % 2).toFloat() else 0.8f)
        )
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(gaugeHeight)
                .background(SciFiSurface.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                .border(0.5.dp, SciFiBorder.copy(alpha = 0.3f), RoundedCornerShape(2.dp)),
            contentAlignment = Alignment.BottomCenter
        ) {
            val ratio = (fuel / maxFuel).coerceIn(0f, 1f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                clipPath(Path().apply { addRoundRect(RoundRect(Rect(Offset.Zero, size), CornerRadius(2.dp.toPx()))) }) {
                    val fillHeight = size.height * ratio
                    val waveOffset = (gameTime / 300f) % (2 * PI.toFloat())
                    val path = Path().apply {
                        moveTo(0f, size.height - fillHeight)
                        for (x in 0..size.width.toInt()) {
                            val y = (size.height - fillHeight) + sin(x / 4f + waveOffset) * 2f
                            lineTo(x.toFloat(), y)
                        }
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    drawPath(path = path, color = (if (fuel > maxFuel * 0.25f) SciFiGreen else SciFiRed).copy(alpha = 0.9f))
                }
            }
        }
    }
}

@Composable
fun HeatGauge(
    heat: Float,
    maxHeat: Float,
    isOverheated: Boolean,
    gameTime: Long
) {
    val gaugeHeight = (120f + (maxHeat - 100f) * 0.6f).coerceIn(100f, 250f).dp
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            if (isOverheated) "\u26A0\uFE0F" else "\uD83D\uDD25",
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 4.dp).graphicsLayer(alpha = if (isOverheated) (gameTime / 150 % 2).toFloat() else 0.8f)
        )
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(gaugeHeight)
                .background(SciFiSurface.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                .border(0.5.dp, SciFiBorder.copy(alpha = 0.3f), RoundedCornerShape(2.dp)),
            contentAlignment = Alignment.BottomCenter
        ) {
            val heatRatio = (heat / maxHeat).coerceIn(0f, 1f)
            val heatColor = when {
                isOverheated -> SciFiRed
                heatRatio > 0.8f -> SciFiRed
                heatRatio > 0.5f -> SciFiGold
                else -> SciFiCyan
            }
            Canvas(modifier = Modifier.fillMaxSize()) {
                val fillHeight = size.height * heatRatio
                drawRect(color = heatColor.copy(alpha = 0.9f), topLeft = Offset(0f, size.height - fillHeight), size = Size(size.width, fillHeight))
            }
        }
    }
}

@Composable
fun ShieldGauge(
    shield: Float,
    maxShield: Float,
    isShieldCritical: Boolean,
    gameTime: Long
) {
    val gaugeHeight = (120f + (maxShield - 50f) * 1.2f).coerceIn(100f, 250f).dp
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "\uD83D\uDEE1\uFE0F",
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 4.dp).graphicsLayer(alpha = if (isShieldCritical) (gameTime / 200 % 2).toFloat() else 0.8f)
        )
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(gaugeHeight)
                .background(SciFiSurface.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                .border(0.5.dp, (if (isShieldCritical) SciFiRed else SciFiBorder).copy(alpha = 0.3f), RoundedCornerShape(2.dp)),
            contentAlignment = Alignment.BottomCenter
        ) {
            val shieldRatio = (shield / maxShield).coerceIn(0f, 1f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    color = SciFiCyan.copy(alpha = 0.9f),
                    topLeft = Offset(0f, size.height * (1f - shieldRatio)),
                    size = Size(size.width, size.height * shieldRatio)
                )
            }
        }
    }
}

@Composable
fun IntegrityGauge(
    integrity: Float,
    maxIntegrity: Float,
    isHullCritical: Boolean,
    gameTime: Long
) {
    val gaugeHeight = (120f + (maxIntegrity - 100f) * 0.6f).coerceIn(100f, 250f).dp
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "\u2764\uFE0F",
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 4.dp).graphicsLayer(alpha = if (isHullCritical) (gameTime / 200 % 2).toFloat() else 0.8f)
        )
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(gaugeHeight)
                .background(SciFiSurface.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                .border(0.5.dp, (if (isHullCritical) SciFiRed else SciFiBorder).copy(alpha = 0.3f), RoundedCornerShape(2.dp)),
            contentAlignment = Alignment.BottomCenter
        ) {
            val integrityRatio = (integrity / maxIntegrity).coerceIn(0f, 1f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    color = SciFiGreen.copy(alpha = 0.9f),
                    topLeft = Offset(0f, size.height * (1f - integrityRatio)),
                    size = Size(size.width, size.height * integrityRatio)
                )
            }
        }
    }
}

@Composable
fun ComboHudBar(
    currentCombo: Int,
    bestComboThisRun: Int,
    comboTarget: Int,
    comboTimeRemaining: Long,
    getWindowForCombo: (Int) -> Long,
    screenWidth: Float
) {
    Surface(
        color = SciFiSurface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(0.9f).shadow(6.dp, RoundedCornerShape(12.dp)),
        border = androidx.compose.foundation.BorderStroke(1.dp, SciFiBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    "BEST THIS RUN: x$bestComboThisRun",
                    color = SciFiWhite.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 7.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    "COMBO x$currentCombo",
                    color = if (currentCombo >= comboTarget) SciFiGold else SciFiWhite,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
            }

            val timerRatio = if (currentCombo > 0)
                (comboTimeRemaining.toFloat() / getWindowForCombo(currentCombo)).coerceIn(0f, 1f)
            else 0f

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .background(SciFiWhite.copy(alpha = 0.1f), CircleShape)
            ) {
                val barColor = if (timerRatio > 0.3f) SciFiCyan else SciFiRed
                Box(
                    modifier = Modifier
                        .fillMaxWidth(timerRatio)
                        .fillMaxHeight()
                        .background(barColor, CircleShape)
                )
            }

            Text(
                "TARGET: x$comboTarget",
                color = SciFiWhite.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun NotificationLayer(
    activeNotification: String?,
    notificationAlpha: Float,
    screenWidth: Float
) {
    if (activeNotification != null) {
        val isHighAlert = activeNotification.contains("!!!") || activeNotification.contains(">>>")
        Text(
            text = activeNotification,
            modifier = Modifier.graphicsLayer(alpha = notificationAlpha).widthIn(max = screenWidth.dp * 0.9f),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                shadow = Shadow(if (isHighAlert) SciFiRed.copy(alpha = 0.5f) else Color.Black, blurRadius = 15f)
            ),
            color = if (isHighAlert) SciFiRed else SciFiWhite,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun LeftGauges(
    modifier: Modifier = Modifier,
    fuel: Float, maxFuel: Float,
    heat: Float, maxHeat: Float, isOverheated: Boolean,
    gameTime: Long
) {
    Column(
        modifier = modifier
            .padding(start = 16.dp)
            .graphicsLayer(alpha = 0.85f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        FuelGauge(fuel = fuel, maxFuel = maxFuel, gameTime = gameTime)
        HeatGauge(heat = heat, maxHeat = maxHeat, isOverheated = isOverheated, gameTime = gameTime)
    }
}

@Composable
fun RightGauges(
    modifier: Modifier = Modifier,
    shield: Float, maxShield: Float,
    integrity: Float, maxIntegrity: Float,
    gameTime: Long
) {
    Column(
        modifier = modifier
            .padding(end = 16.dp)
            .graphicsLayer(alpha = 0.85f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ShieldGauge(shield = shield, maxShield = maxShield, isShieldCritical = shield < maxShield * 0.25f, gameTime = gameTime)
        IntegrityGauge(integrity = integrity, maxIntegrity = maxIntegrity, isHullCritical = integrity < maxIntegrity * 0.25f, gameTime = gameTime)
    }
}

@Composable
fun ZoneDiscoveryCard(
    activeEvent: DiscoveryEvent?,
    score: Int
) {
    if (activeEvent !is DiscoveryEvent.Zone) return
    val zone = activeEvent.zone
    val titleText = when (zone) {
        AltitudeZone.CLOUD_LAYER -> "CLOUD LAYER REACHED"
        AltitudeZone.ORBIT -> "SPACE REACHED"
        AltitudeZone.VOID -> "VOID ENTERED"
        else -> zone.zoneName.uppercase()
    }

    val accentColor = when (zone) {
        AltitudeZone.ORBIT -> SciFiGold
        AltitudeZone.VOID -> SciFiRed
        AltitudeZone.DEEP_SPACE -> SciFiPurple
        else -> SciFiCyan
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(SciFiSurface, RoundedCornerShape(16.dp))
            .padding(horizontal = 32.dp, vertical = 24.dp)
            .border(1.dp, SciFiBorder, RoundedCornerShape(16.dp))
    ) {
        Text(
            text = titleText,
            style = MaterialTheme.typography.headlineMedium.copy(
                shadow = Shadow(accentColor.copy(alpha = 0.5f), blurRadius = 20f),
                letterSpacing = 4.sp
            ),
            color = SciFiWhite,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = zone.subtitle.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = accentColor.copy(alpha = 0.8f),
            letterSpacing = 6.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
