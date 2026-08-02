package com.ashwathai.jump_droid

import androidx.compose.animation.AnimatedVisibility
import android.view.animation.OvershootInterpolator
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.runtime.key
import kotlin.random.Random
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
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
    score: Int, 
    altitude: Int,
    highScore: Int,
    zone: AltitudeZone = AltitudeZone.EARTH
) {
    val zoneAccent = zoneGaugeAccents[zone] ?: SciFiCyan
    
    // Pulse animation on score change
    val scoreScale = remember { Animatable(1f) }
    var prevScore by remember { mutableIntStateOf(score) }
    
    LaunchedEffect(score) {
        if (score > prevScore) {
            scoreScale.snapTo(1.2f)
            scoreScale.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
        }
        prevScore = score
    }

    Column(
        modifier = modifier
            .statusBarsPadding()
            .padding(top = 8.dp)
            .graphicsLayer(scaleX = scoreScale.value, scaleY = scoreScale.value),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Total Score
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                shadow = Shadow(zoneAccent.copy(alpha = 0.3f), blurRadius = 15f)
            ),
            color = SciFiWhite
        )
        
        // Physical Altitude
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${altitude}m",
                style = MaterialTheme.typography.labelSmall,
                color = zoneAccent,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "BEST: $highScore",
                style = MaterialTheme.typography.labelSmall,
                color = SciFiWhite.copy(alpha = 0.5f),
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun ZenModeIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "ZenIndicator")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    Column(
        modifier = modifier.graphicsLayer(alpha = pulse),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ZEN MODE // PEACEFUL GLIDE",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                shadow = Shadow(SciFiPurple.copy(alpha = 0.5f), blurRadius = 8f)
            ),
            color = SciFiPurple,
            fontSize = 9.sp
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
            Image(
                painter = painterResource(id = R.drawable.ic_hud_fuel),
                contentDescription = null,
                modifier = Modifier
                    .size(13.dp)
                    .graphicsLayer {
                        alpha = if (isLow) ((hud.gameTime / 200) % 2).toFloat() else 0.85f
                        translationY = fuelBounce.value
                    }
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "${fuel.toInt()}",
                color = dropColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    shadow = Shadow(dropColor.copy(alpha = 0.4f), blurRadius = 8f)
                )
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
    val isWarning = heatRatio > 0.7f
    val warningPulse = rememberInfiniteTransition(label = "HeatWarningPulse").animateFloat(0.5f, 1f, infiniteRepeatable(tween(400), RepeatMode.Reverse), label = "HeatWarningPulseVal")
    val heatColor = when {
        isOverheated -> SciFiRed
        heatRatio > 0.8f -> SciFiRed
        heatRatio > 0.5f -> SciFiGold
        else -> SciFiCyan
    }
    val dangerMarks = if (heatRatio > 0.5f) listOf(0.7f, 0.9f) else emptyList()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                if (isWarning && !isOverheated) SciFiRed.copy(alpha = warningPulse.value * 0.08f)
                else Color.Transparent,
                RoundedCornerShape(6.dp)
            )
            .padding(2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_hud_heat),
                contentDescription = null,
                modifier = Modifier
                    .size(14.dp)
                    .graphicsLayer {
                        alpha = if (isOverheated) ((hud.gameTime / 150) % 2).toFloat() else if (isInterfered && noiseVal < 0.2f) 0f else 0.85f
                        scaleX = if (isWarning) heatFlicker.value * (1f + warningPulse.value * 0.12f) else heatFlicker.value
                        scaleY = if (isWarning) heatFlicker.value * (1f + warningPulse.value * 0.12f) else heatFlicker.value
                    }
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "${heat.toInt()}",
                color = heatColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    shadow = Shadow(
                        if (isOverheated) SciFiRed.copy(alpha = 0.5f) else if (isWarning) SciFiRed.copy(alpha = warningPulse.value * 0.4f) else SciFiGold.copy(alpha = 0.3f),
                        blurRadius = if (isWarning) 12f else 8f
                    )
                )
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
            dangerThresholds = dangerMarks,
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
            color = if (isWarning) SciFiRed else heatColor,
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
    val shieldSway = rememberInfiniteTransition(label = "ShieldSway").animateFloat(-6f, 6f, infiniteRepeatable(tween(1500, easing = androidx.compose.animation.core.FastOutSlowInEasing), RepeatMode.Reverse), label = "ShieldSwayVal")
    val shieldRatio = (shield / maxShield).coerceIn(0f, 1f)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_hud_shield),
                contentDescription = null,
                modifier = Modifier
                    .size(14.dp)
                    .graphicsLayer {
                        alpha = if (isShieldCritical) ((hud.gameTime / 200) % 2).toFloat() else if (isInterfered && noiseVal < 0.2f) 0f else 0.85f
                        rotationZ = shieldSway.value
                    }
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "${shield.toInt()}",
                color = if (isShieldCritical) SciFiRed else SciFiCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    shadow = Shadow(
                        if (isShieldCritical) SciFiRed.copy(alpha = 0.5f) else SciFiCyan.copy(alpha = 0.3f),
                        blurRadius = 8f
                    )
                )
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
    val heartScale = 1f + 0.2f * sin(heartBeat.value * 2f * PI.toFloat()).toFloat().coerceAtLeast(0f) * (1f - (heartBeat.value % 0.3f / 0.3f).coerceIn(0f, 1f))
    val integrityRatio = (integrity / maxIntegrity).coerceIn(0f, 1f)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_hud_hull),
                contentDescription = null,
                modifier = Modifier
                    .size(14.dp)
                    .graphicsLayer {
                        alpha = if (isHullCritical) ((hud.gameTime / 200) % 2).toFloat() else if (isInterfered && noiseVal < 0.2f) 0f else 0.85f
                        scaleX = heartScale
                        scaleY = heartScale
                    }
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "${integrity.toInt()}",
                color = if (isHullCritical) SciFiRed else SciFiGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    shadow = Shadow(
                        if (isHullCritical) SciFiRed.copy(alpha = 0.5f) else SciFiGreen.copy(alpha = 0.3f),
                        blurRadius = 8f
                    )
                )
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
    }
}

@Composable
fun CodexQuickAccess(
    discoveryManager: DiscoveryManager,
    onNavigateArchive: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        Box(
            Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(SciFiSurface.copy(alpha = 0.85f))
                .border(1.dp, SciFiBorder.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .clickable { expanded = !expanded },
            contentAlignment = Alignment.Center
        ) {
            Text("C", color = SciFiCyan, fontSize = 14.sp, fontWeight = FontWeight.Black)
        }

        if (expanded) {
            val totalEntries = DiscoveryType.entries.size
            val discoveredCount = DiscoveryType.entries.count { discoveryManager.isDiscovered(it) }
            val unviewedCount = DiscoveryType.entries.count { discoveryManager.isDiscovered(it) && !discoveryManager.isViewed(it) }

            val categories = DiscoveryType.entries.map { it.category }.distinct()

            Column(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .widthIn(max = 240.dp)
                    .background(SciFiSurface.copy(alpha = 0.95f), RoundedCornerShape(8.dp))
                    .border(1.dp, SciFiBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text("CODEX", color = SciFiCyan, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Text("$discoveredCount / $totalEntries discovered", color = SciFiWhite.copy(alpha = 0.6f), fontSize = 9.sp)
                if (unviewedCount > 0) {
                    Text("$unviewedCount NEW", color = SciFiGold, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(6.dp))

                categories.forEach { cat ->
                    val catEntries = DiscoveryType.entries.filter { it.category == cat }
                    val catDiscovered = catEntries.count { discoveryManager.isDiscovered(it) }
                    val catNew = catEntries.count { discoveryManager.isDiscovered(it) && !discoveryManager.isViewed(it) }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(cat, color = SciFiWhite.copy(alpha = 0.7f), fontSize = 8.sp, fontWeight = FontWeight.Medium)
                        Row {
                            Text("$catDiscovered/${catEntries.size}", color = SciFiWhite.copy(alpha = 0.5f), fontSize = 8.sp)
                            if (catNew > 0) {
                                Spacer(Modifier.width(4.dp))
                                Text("NEW", color = SciFiGold, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    "ARCHIVE >",
                    color = SciFiPurple.copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable { onNavigateArchive() }
                )
            }
        }
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
    val zoneAccent = zoneGaugeAccents[zone] ?: SciFiCyan
    
    val ringColor = when {
        timerRatio > 0.6f -> SciFiGreen
        timerRatio > 0.3f -> SciFiGold
        timerRatio > 0.15f -> Color(0xFFFF9800) // Orange
        else -> SciFiRed
    }

    // Pulse animation on combo increase
    val ringScale = remember { Animatable(1f) }
    var prevCombo by remember { mutableIntStateOf(0) }
    var hasShownHint by remember { mutableStateOf(false) }
    val hintAlpha = remember { Animatable(0f) }
    LaunchedEffect(currentCombo) {
        if (currentCombo > prevCombo && prevCombo > 0) {
            ringScale.snapTo(1.35f)
            ringScale.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
        }
        if (currentCombo >= 3 && !hasShownHint) {
            hasShownHint = true
            hintAlpha.snapTo(1f)
            hintAlpha.animateTo(0f, tween(4000, easing = FastOutSlowInEasing))
        }
        prevCombo = currentCombo
    }

    Box(
        modifier = modifier
            .size(52.dp)
            .graphicsLayer(scaleX = ringScale.value, scaleY = ringScale.value),
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

            // Pulse flash ring on combo increase
            val pulseAlpha = ((ringScale.value - 1f) / 0.35f).coerceIn(0f, 1f) * 0.6f
            if (pulseAlpha > 0.05f) {
                drawCircle(
                    color = Color.White.copy(alpha = pulseAlpha),
                    radius = size.minDimension / 2 + 4f,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
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

        if (hintAlpha.value > 0.01f) {
            Text(
                text = "LAND ON DIFFERENT PLATFORMS TO BUILD COMBO",
                color = zoneAccent.copy(alpha = hintAlpha.value * 0.8f),
                fontSize = 7.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .offset(x = 62.dp, y = (-4).dp)
                    .graphicsLayer(alpha = hintAlpha.value)
                    .widthIn(max = 110.dp),
                textAlign = TextAlign.Start
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
    maxStack: Int = 1
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
    fuelProvider: () -> Float, maxFuel: Float,
    heatProvider: () -> Float, maxHeat: Float, isOverheatedProvider: () -> Boolean,
    hud: HudContext
) {
    Column(
        modifier = modifier
            .padding(start = 16.dp)
            .graphicsLayer {
                alpha = 0.85f
                translationX = (120.dp * hud.hudPullFactor).toPx()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        FuelGauge(fuel = fuelProvider(), maxFuel = maxFuel, hud = hud)
        HeatGauge(heat = heatProvider(), maxHeat = maxHeat, isOverheated = isOverheatedProvider(), hud = hud)
    }
}

@Composable
fun RightGauges(
    modifier: Modifier = Modifier,
    shieldProvider: () -> Float, maxShield: Float,
    integrityProvider: () -> Float, maxIntegrity: Float,
    hud: HudContext
) {
    val shield = shieldProvider()
    val integrity = integrityProvider()
    val isShieldCritical = shield < maxShield * 0.25f
    val isHullCritical = integrity < maxIntegrity * 0.25f
    Column(
        modifier = modifier
            .padding(end = 16.dp)
            .graphicsLayer {
                alpha = 0.85f
                translationX = (-120.dp * hud.hudPullFactor).toPx()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ShieldGauge(shield = shield, maxShield = maxShield, isShieldCritical = isShieldCritical, hud = hud)
        IntegrityGauge(integrity = integrity, maxIntegrity = maxIntegrity, isHullCritical = isHullCritical, hud = hud)
    }
}

@Composable
fun MissionProgressCard(
    activeMissions: List<Mission>,
    modifier: Modifier = Modifier
) {
    val nearComplete = activeMissions.firstOrNull {
        !it.isCompleted && it.targetValue > 0 &&
        it.currentProgress.toFloat() / it.targetValue >= 0.95f
    } ?: return

    val pct = (nearComplete.currentProgress.toFloat() / nearComplete.targetValue).coerceIn(0f, 1f)

    val rewardText = nearComplete.rewards.firstOrNull { it !is MissionReward.None }?.let { reward ->
        when (reward) {
            is MissionReward.Cash -> "${reward.amount} \u0024"
            is MissionReward.ModuleUnlock -> "MODULE"
            is MissionReward.Artifact -> "ARTIFACT"
            is MissionReward.PowerUp -> reward.type.name.replace("_", " ").take(8)
            is MissionReward.Unlock -> "ROCKET"
            is MissionReward.Achievement -> "ACHIEVEMENT"
            is MissionReward.None -> null
        }
    }

    Surface(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .background(SciFiSurface, RoundedCornerShape(12.dp))
            .border(0.5.dp, SciFiBorder.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        color = SciFiSurface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("MISSION PROGRESS", color = SciFiWhite.copy(alpha = 0.4f), fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(Modifier.height(2.dp))
                Text(nearComplete.name.uppercase(), color = SciFiCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (rewardText != null) {
                Box(
                    Modifier.padding(horizontal = 8.dp).background(SciFiGold.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(rewardText, color = SciFiGold, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }
            Box(
                Modifier.size(36.dp).padding(4.dp)
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val sweep = pct * 360f
                    drawArc(SciFiWhite.copy(alpha = 0.1f), 0f, 360f, false, style = Stroke(width = 3f))
                    drawArc(SciFiCyan, -90f, sweep, false, style = Stroke(width = 3f))
                }
                Text("${(pct * 100).toInt()}%", fontSize = 8.sp, color = SciFiWhite, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun AchievementDeck(
    pendingUnlocks: List<UnlockEvent>,
    modifier: Modifier = Modifier
) {
    if (pendingUnlocks.isEmpty()) return

    val infiniteTransition = rememberInfiniteTransition(label = "DeckPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy((-16).dp) // Tighter stacked effect
    ) {
        pendingUnlocks.takeLast(3).forEachIndexed { i, event ->
            val scale = 1f - (2 - i) * 0.05f
            
            key(event) {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(500, easing = { OvershootInterpolator(1.2f).getInterpolation(it) })
                    ) + fadeIn()
                ) {
                    Surface(
                        color = SciFiSurface.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(2.dp),
                        border = BorderStroke(0.5.dp, event.accentColor.copy(alpha = pulseAlpha)),
                        modifier = Modifier
                            .width(100.dp)
                            .height(32.dp)
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                alpha = if (i == pendingUnlocks.takeLast(3).lastIndex) 1f else 0.7f
                            )
                    ) {
                        // Category-specific flourish
                        val flourishAlpha = if (i == pendingUnlocks.takeLast(3).lastIndex) (pulseAlpha - 0.6f) / 0.4f else 0f
                        Canvas(Modifier.fillMaxSize()) {
                            when (event) {
                                is UnlockEvent.Mission -> {
                                    // Heat glow at edges
                                    drawRect(
                                        brush = Brush.horizontalGradient(listOf(SciFiRed.copy(alpha = 0.15f * flourishAlpha), Color.Transparent)),
                                        size = Size(20f, size.height)
                                    )
                                }
                                is UnlockEvent.Discovery -> {
                                    // Digital glitch line
                                    drawLine(SciFiCyan.copy(alpha = 0.3f * flourishAlpha), Offset(Random.nextFloat() * size.width, 0f), Offset(Random.nextFloat() * size.width, size.height), 1f)
                                }
                                else -> {}
                            }
                        }

                        Row(
                            Modifier.padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when(event) {
                                    is UnlockEvent.Mission -> "\u2638" // Biohazard/Mission
                                    is UnlockEvent.Discovery -> "\u25C8" // Diamond/Signal
                                    is UnlockEvent.Rocket -> "\u25B3" // Blueprint
                                    else -> "\u272A" // Star
                                },
                                color = event.accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = event.title,
                                color = SciFiWhite,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
