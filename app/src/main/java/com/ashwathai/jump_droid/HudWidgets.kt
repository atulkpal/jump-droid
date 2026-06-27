package com.ashwathai.jump_droid

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.jump_droid.ui.theme.SciFiBorder
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import com.ashwathai.jump_droid.ui.theme.SciFiGold
import com.ashwathai.jump_droid.ui.theme.SciFiGreen
import com.ashwathai.jump_droid.ui.theme.SciFiPurple
import com.ashwathai.jump_droid.ui.theme.SciFiRed
import com.ashwathai.jump_droid.ui.theme.SciFiSurface
import com.ashwathai.jump_droid.ui.theme.SciFiWhite
import kotlin.math.PI
import kotlin.math.sin

private val zoneGaugeAccents = mapOf(
    AltitudeZone.EARTH to SciFiGreen,
    AltitudeZone.CLOUD_LAYER to SciFiCyan,
    AltitudeZone.UPPER_ATMOSPHERE to SciFiPurple,
    AltitudeZone.ORBIT to SciFiGold,
    AltitudeZone.DEEP_SPACE to SciFiPurple,
    AltitudeZone.VOID to SciFiRed
)

@Composable
fun AltitudeDisplay(
    modifier: Modifier = Modifier,
    score: Int, highScore: Int,
    zone: AltitudeZone = AltitudeZone.EARTH
) {
    val zoneAccent = zoneGaugeAccents[zone] ?: SciFiCyan
    Column(
        modifier = modifier
            .statusBarsPadding()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                shadow = Shadow(zoneAccent.copy(alpha = 0.3f), blurRadius = 15f)
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
    hud: HudContext
) {
    val gaugeHeight = (120f + (maxFuel - 100f) * 0.6f).coerceIn(100f, 250f).dp
    val isLow = fuel < 20f
    val isInterfered = hud.interferenceTimer > 0f
    val noiseVal = if (isInterfered) ((sin(hud.gameTime / 100.0) * 0.5 + 0.5) * 0.8).toFloat() else 1f
    val dropColor = if (isLow) SciFiRed else SciFiGreen
    val fuelBounce = rememberInfiniteTransition(label = "FuelBounce").animateFloat(0f, 3f, infiniteRepeatable(tween(800, easing = androidx.compose.animation.core.FastOutSlowInEasing), RepeatMode.Reverse), label = "FuelBounceVal")
    val ratio = (fuel / maxFuel).coerceIn(0f, 1f)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "\u26FD",
                fontSize = 10.sp,
                modifier = Modifier.graphicsLayer {
                    alpha = if (isLow) ((hud.gameTime / 200) % 2).toFloat() else 0.8f
                    translationY = fuelBounce.value
                },
                color = dropColor,
                style = MaterialTheme.typography.labelSmall.copy(
                    shadow = Shadow(dropColor.copy(alpha = 0.4f), blurRadius = 8f)
                )
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text = "${fuel.toInt()}".padStart(3, ' '),
                color = dropColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        GaugeBar(
            value = ratio,
            color = dropColor,
            gameTime = hud.gameTime,
            isInterfered = isInterfered,
            noiseVal = noiseVal,
            gaugeHeight = gaugeHeight,
            onDrawFill = { fillAlpha, fillHeight ->
                val waveOffset = (hud.gameTime / 300f) % (2 * PI.toFloat())
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
                drawPath(path = path, brush = Brush.verticalGradient(
                    (size.height - fillHeight) to dropColor.copy(alpha = fillAlpha),
                    size.height to dropColor.copy(alpha = fillAlpha * 0.3f)
                ))
            }
        )
        Text(
            text = "${(ratio * 100).toInt()}%",
            color = dropColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun HeatGauge(
    heat: Float,
    maxHeat: Float,
    isOverheated: Boolean,
    hud: HudContext
) {
    val gaugeHeight = (120f + (maxHeat - 100f) * 0.6f).coerceIn(100f, 250f).dp
    val isInterfered = hud.interferenceTimer > 0f
    val noiseVal = if (isInterfered) ((sin(hud.gameTime / 100.0 + 2.0) * 0.5 + 0.5) * 0.8).toFloat() else 1f
    val heatFlicker = rememberInfiniteTransition(label = "HeatFlicker").animateFloat(0.88f, 1.12f, infiniteRepeatable(tween(120, easing = LinearEasing), RepeatMode.Reverse), label = "HeatFlickerVal")
    val heatRatio = (heat / maxHeat).coerceIn(0f, 1f)
    val heatColor = when {
        isOverheated -> SciFiRed
        heatRatio > 0.8f -> SciFiRed
        heatRatio > 0.5f -> SciFiGold
        else -> SciFiCyan
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isOverheated) "\u26A0\uFE0F" else "\uD83D\uDD25",
                fontSize = if (isOverheated) 12.sp else 10.sp,
                modifier = Modifier.graphicsLayer {
                    alpha = if (isOverheated) ((hud.gameTime / 150) % 2).toFloat() else if (isInterfered && noiseVal < 0.2f) 0f else 0.8f
                    scaleX = heatFlicker.value
                    scaleY = heatFlicker.value
                },
                color = if (isOverheated) SciFiRed else SciFiGold,
                style = MaterialTheme.typography.labelSmall.copy(
                    shadow = Shadow(
                        if (isOverheated) SciFiRed.copy(alpha = 0.6f) else SciFiGold.copy(alpha = 0.4f),
                        blurRadius = 10f
                    )
                )
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text = "${heat.toInt()}".padStart(3, ' '),
                color = heatColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        GaugeBar(
            value = heatRatio,
            color = heatColor,
            gameTime = hud.gameTime,
            isInterfered = isInterfered,
            noiseVal = noiseVal,
            gaugeHeight = gaugeHeight,
            interferencePhase = 1f,
            onDrawFill = { fillAlpha, fillHeight ->
                val gradientBrush = Brush.verticalGradient(
                    (size.height - fillHeight) to heatColor.copy(alpha = fillAlpha),
                    size.height to heatColor.copy(alpha = fillAlpha * 0.2f)
                )
                drawRect(brush = gradientBrush, topLeft = Offset(0f, size.height - fillHeight), size = Size(size.width, fillHeight))
            }
        )
        Text(
            text = "${(heatRatio * 100).toInt()}%",
            color = heatColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun ShieldGauge(
    shield: Float,
    maxShield: Float,
    isShieldCritical: Boolean,
    hud: HudContext
) {
    val gaugeHeight = (120f + (maxShield - 50f) * 1.2f).coerceIn(100f, 250f).dp
    val isInterfered = hud.interferenceTimer > 0f
    val noiseVal = if (isInterfered) ((sin(hud.gameTime / 100.0 + 3.0) * 0.5 + 0.5) * 0.8).toFloat() else 1f
    val shieldSway = rememberInfiniteTransition(label = "ShieldSway").animateFloat(-4f, 4f, infiniteRepeatable(tween(1200, easing = androidx.compose.animation.core.FastOutSlowInEasing), RepeatMode.Reverse), label = "ShieldSwayVal")
    val shieldRatio = (shield / maxShield).coerceIn(0f, 1f)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "\uD83D\uDEE1\uFE0F",
                fontSize = 12.sp,
                modifier = Modifier.graphicsLayer {
                    alpha = if (isShieldCritical) ((hud.gameTime / 200) % 2).toFloat() else if (isInterfered && noiseVal < 0.2f) 0f else 0.8f
                    rotationZ = shieldSway.value
                },
                color = if (isShieldCritical) SciFiRed else SciFiCyan,
                style = MaterialTheme.typography.labelSmall.copy(
                    shadow = Shadow(
                        if (isShieldCritical) SciFiRed.copy(alpha = 0.6f) else SciFiCyan.copy(alpha = 0.4f),
                        blurRadius = 10f
                    )
                )
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text = "${shield.toInt()}",
                color = if (isShieldCritical) SciFiRed else SciFiCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        val shieldColor = if (isShieldCritical) SciFiRed else SciFiCyan
        GaugeBar(
            value = shieldRatio,
            color = shieldColor,
            gameTime = hud.gameTime,
            isInterfered = isInterfered,
            noiseVal = noiseVal,
            gaugeHeight = gaugeHeight,
            interferencePhase = 2f,
            onDrawFill = { fillAlpha, fillHeight ->
                val gradientBrush = Brush.verticalGradient(
                    (size.height - fillHeight) to shieldColor.copy(alpha = fillAlpha),
                    size.height to shieldColor.copy(alpha = fillAlpha * 0.2f)
                )
                drawRect(brush = gradientBrush, topLeft = Offset(0f, size.height - fillHeight), size = Size(size.width, fillHeight))
                val shimmerX = (hud.gameTime / 30f) % (size.width * 2f) - size.width
                drawRect(Color.White.copy(alpha = 0.12f * fillAlpha), topLeft = Offset(shimmerX, 0f), size = Size(size.width * 0.3f, size.height))
            }
        )
        Text(
            text = "${(shieldRatio * 100).toInt()}%",
            color = if (isShieldCritical) SciFiRed else SciFiCyan,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun IntegrityGauge(
    integrity: Float,
    maxIntegrity: Float,
    isHullCritical: Boolean,
    hud: HudContext
) {
    val gaugeHeight = (120f + (maxIntegrity - 100f) * 0.6f).coerceIn(100f, 250f).dp
    val isInterfered = hud.interferenceTimer > 0f
    val noiseVal = if (isInterfered) ((sin(hud.gameTime / 100.0 + 4.0) * 0.5 + 0.5) * 0.8).toFloat() else 1f
    val heartBeat = rememberInfiniteTransition(label = "HeartBeat").animateFloat(0f, 1f, infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Restart), label = "HeartBeatVal")
    val heartScale = 1f + 0.15f * sin(heartBeat.value * 2f * PI.toFloat()).toFloat().coerceAtLeast(0f) * (1f - (heartBeat.value % 0.3f / 0.3f).coerceIn(0f, 1f))
    val integrityRatio = (integrity / maxIntegrity).coerceIn(0f, 1f)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "\u2764\uFE0F",
                fontSize = 12.sp,
                modifier = Modifier.graphicsLayer {
                    alpha = if (isHullCritical) ((hud.gameTime / 200) % 2).toFloat() else if (isInterfered && noiseVal < 0.2f) 0f else 0.8f
                    scaleX = heartScale
                    scaleY = heartScale
                },
                color = if (isHullCritical) SciFiRed else SciFiGreen,
                style = MaterialTheme.typography.labelSmall.copy(
                    shadow = Shadow(
                        if (isHullCritical) SciFiRed.copy(alpha = 0.6f) else SciFiGreen.copy(alpha = 0.4f),
                        blurRadius = 10f
                    )
                )
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text = "${integrity.toInt()}",
                color = if (isHullCritical) SciFiRed else SciFiGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        val integrityColor = if (isHullCritical) SciFiRed else SciFiGreen
        GaugeBar(
            value = integrityRatio,
            color = integrityColor,
            gameTime = hud.gameTime,
            isInterfered = isInterfered,
            noiseVal = noiseVal,
            gaugeHeight = gaugeHeight,
            interferencePhase = 3f,
            onDrawFill = { fillAlpha, fillHeight ->
                val gradientBrush = Brush.verticalGradient(
                    (size.height - fillHeight) to integrityColor.copy(alpha = fillAlpha),
                    size.height to integrityColor.copy(alpha = fillAlpha * 0.2f)
                )
                drawRect(brush = gradientBrush, topLeft = Offset(0f, size.height - fillHeight), size = Size(size.width, fillHeight))
            },
            onDrawExtra = {
                if (integrityRatio < 0.25f && !isInterfered) {
                    repeat(2) { i ->
                        val crackX = size.width * (0.3f + i * 0.4f)
                        val crackPath = Path().apply {
                            moveTo(crackX, size.height * (1f - integrityRatio))
                            lineTo(crackX + 2f, size.height * (1f - integrityRatio) + size.height * 0.1f)
                            lineTo(crackX - 1f, size.height * (1f - integrityRatio) + size.height * 0.2f)
                        }
                        drawPath(crackPath, SciFiRed.copy(alpha = 0.6f), style = Stroke(1f))
                    }
                }
            }
        )
        Text(
            text = "${(integrityRatio * 100).toInt()}%",
            color = if (isHullCritical) SciFiRed else SciFiGreen,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun ComboDisplay(
    modifier: Modifier = Modifier,
    currentCombo: Int,
    comboTimeRemaining: Long,
    getWindowForCombo: (Int) -> Long,
    zone: AltitudeZone = AltitudeZone.EARTH
) {
    if (currentCombo <= 0) return

    val timerRatio = (comboTimeRemaining.toFloat() / getWindowForCombo(currentCombo)).coerceIn(0f, 1f)
    
    val ringColor = when {
        timerRatio > 0.6f -> SciFiGreen
        timerRatio > 0.3f -> SciFiGold
        timerRatio > 0.15f -> Color(0xFFFF9800) // Orange
        else -> SciFiRed
    }

    Box(
        modifier = modifier
            .size(52.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background Ring (Scanner/Radar style)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                style = Stroke(width = 2.dp.toPx())
            )
            
            // Outer faint pulse
            drawCircle(
                color = ringColor.copy(alpha = 0.1f * timerRatio),
                radius = size.minDimension / 2,
                style = Stroke(width = 1.dp.toPx())
            )
        }
        
        // Progress Ring (Shrinking Arc)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = ringColor.copy(alpha = 0.8f),
                startAngle = -90f,
                sweepAngle = 360f * timerRatio,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Butt)
            )
            
            // Scanner Sweep (Radar effect)
            val sweepAngle = (System.currentTimeMillis() % 2000) / 2000f * 360f
            drawArc(
                color = Color.White.copy(alpha = 0.2f),
                startAngle = sweepAngle - 20f,
                sweepAngle = 40f,
                useCenter = true,
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "x$currentCombo",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Black,
                    shadow = Shadow(ringColor.copy(alpha = 0.4f), blurRadius = 6f)
                ),
                color = SciFiWhite,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun NotificationLayer(
    modifier: Modifier = Modifier,
    activeNotification: NotificationEntry?,
    notificationAlpha: Float,
    queue: List<NotificationEntry>,
    screenWidth: Float,
    zone: AltitudeZone = AltitudeZone.EARTH,
    maxStack: Int = 3
) {
    val displayEntries = buildList {
        if (activeNotification != null) add(activeNotification)
        addAll(queue.take(maxStack - 1))
    }
    if (displayEntries.isEmpty()) return

    val zoneAccent = zoneGaugeAccents[zone] ?: SciFiCyan
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        displayEntries.forEachIndexed { index, entry ->
            val alpha = if (index == 0) notificationAlpha else 0.6f
            val priorityColor = when (entry.priority) {
                NotificationPriority.CRITICAL -> SciFiRed
                NotificationPriority.TACTICAL -> zoneAccent
                NotificationPriority.FLAVOR -> zoneAccent.copy(alpha = 0.7f)
            }
            val shadowColor = when (entry.priority) {
                NotificationPriority.CRITICAL -> SciFiRed.copy(alpha = 0.5f)
                NotificationPriority.TACTICAL -> zoneAccent.copy(alpha = 0.4f)
                NotificationPriority.FLAVOR -> zoneAccent.copy(alpha = 0.2f)
            }
            Text(
                text = entry.message,
                modifier = Modifier.graphicsLayer(alpha = alpha).widthIn(max = screenWidth.dp * 0.9f),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (entry.priority == NotificationPriority.FLAVOR) FontWeight.Medium else FontWeight.Black,
                    letterSpacing = if (entry.priority == NotificationPriority.FLAVOR) 1.sp else 2.sp,
                    shadow = Shadow(shadowColor, blurRadius = 15f),
                    fontSize = when (entry.priority) {
                        NotificationPriority.CRITICAL -> 16.sp
                        NotificationPriority.TACTICAL -> 14.sp
                        NotificationPriority.FLAVOR -> 12.sp
                    }
                ),
                color = priorityColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LeftGauges(
    modifier: Modifier = Modifier,
    fuel: Float, maxFuel: Float,
    heat: Float, maxHeat: Float, isOverheated: Boolean,
    hud: HudContext
) {
    Column(
        modifier = modifier
            .padding(start = 16.dp)
            .graphicsLayer {
                alpha = 0.85f
                // EPIC 11: Singularity Pull
                translationX = (120.dp * hud.hudPullFactor).toPx()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        FuelGauge(fuel = fuel, maxFuel = maxFuel, hud = hud)
        HeatGauge(heat = heat, maxHeat = maxHeat, isOverheated = isOverheated, hud = hud)
    }
}

@Composable
fun RightGauges(
    modifier: Modifier = Modifier,
    shield: Float, maxShield: Float,
    integrity: Float, maxIntegrity: Float,
    hud: HudContext
) {
    Column(
        modifier = modifier
            .padding(end = 16.dp)
            .graphicsLayer {
                alpha = 0.85f
                // EPIC 11: Singularity Pull
                translationX = (-120.dp * hud.hudPullFactor).toPx()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        ShieldGauge(shield = shield, maxShield = maxShield, isShieldCritical = shield < maxShield * 0.25f, hud = hud)
        IntegrityGauge(integrity = integrity, maxIntegrity = maxIntegrity, isHullCritical = integrity < maxIntegrity * 0.25f, hud = hud)
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
            .border(0.5.dp, SciFiBorder.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
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
