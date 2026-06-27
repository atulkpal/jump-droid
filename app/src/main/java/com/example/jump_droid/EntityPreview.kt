package com.example.jump_droid

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.example.jump_droid.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val GLITCH_GREEN = Color(0xFF00FF41)
private val GLITCH_PINK = Color(0xFFFF00FF)
private val STATIC_WHITE = Color(0xFFE0E0E0)

@Composable
fun EntityPreview(
    detail: EntityDetail,
    unlocked: Boolean,
    modifier: Modifier = Modifier
) {
    var gameTime by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(16L)
            gameTime += 16L
        }
    }

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        if (!unlocked) {
            drawGlitchEffect(cx, cy, gameTime)
            return@Canvas
        }

        val threatId = detail.threatId
        val platType = detail.platformType
        val puType = detail.powerUpType

        when {
            threatId != null -> drawThreatPreview(threatId, cx, cy, gameTime)
            platType != null -> drawPlatformPreview(platType, cx, cy, gameTime)
            puType != null -> drawPowerUpPreview(puType, cx, cy, gameTime)
            else -> drawFallbackShape(detail.discoveryType, cx, cy, gameTime)
        }
    }
}

private fun DrawScope.drawThreatPreview(threatId: String, cx: Float, cy: Float, gameTime: Long) {
    val def = ThreatRegistry.getById(threatId) ?: return
    val mockThreat = ActiveThreat(
        instanceId = "preview",
        definition = def,
        initialX = cx,
        initialY = cy
    )
    mockThreat.lifetime = gameTime / 1000f
    mockThreat.state = ThreatState.ACTIVE

    val renderer = ThreatRendererRegistry.forId(threatId)
    renderer?.render(
        drawScope = this,
        threat = mockThreat,
        cameraY = 0f,
        alpha = 1f,
        gameTime = gameTime,
        player = Player(initialX = cx, initialY = cy)
    )
}

private fun DrawScope.drawPlatformPreview(platType: PlatformType, cx: Float, cy: Float, gameTime: Long) {
    val platform = Platform(
        initialX = cx - 100f,
        y = cy - 12f,
        width = 200f,
        type = platType
    )
    PlatformRenderer().render(
        drawScope = this,
        platform = platform,
        currentZone = AltitudeZone.EARTH,
        cameraY = 0f,
        gameTime = gameTime
    )
}

private fun DrawScope.drawPowerUpPreview(puType: PowerUpType, cx: Float, cy: Float, gameTime: Long) {
    val powerUp = PowerUp(
        x = cx,
        y = cy,
        type = puType
    )
    drawPowerUps(
        powerUps = listOf(powerUp),
        cameraY = 0f,
        gameTime = gameTime
    )
}

private fun DrawScope.drawFallbackShape(type: DiscoveryType, cx: Float, cy: Float, gameTime: Long) {
    val name = type.name
    val pulse = sin(gameTime / 200f) * 0.3f + 0.7f

    when {
        name.startsWith("AREA_") -> {
            val zoneColor = zoneColorForType(type)
            drawCircle(zoneColor.copy(alpha = 0.3f), radius = 50f, center = Offset(cx, cy))
            drawCircle(zoneColor.copy(alpha = 0.5f * pulse), radius = 35f, center = Offset(cx, cy), style = Stroke(width = 2f))
            drawCircle(zoneColor.copy(alpha = 0.8f), radius = 15f, center = Offset(cx, cy))
        }
        name.startsWith("MODULE_") -> {
            val hexPath = Path().apply {
                moveTo(cx + 30f * cos(0f), cy + 30f * sin(0f))
                for (i in 1..6) {
                    val angle = i * (kotlin.math.PI.toFloat() / 3f)
                    lineTo(cx + 30f * cos(angle), cy + 30f * sin(angle))
                }
                close()
            }
            drawPath(hexPath, SciFiCyan.copy(alpha = 0.4f))
            drawPath(hexPath, SciFiCyan.copy(alpha = 0.8f * pulse), style = Stroke(width = 2f))
            drawCircle(SciFiCyan, radius = 6f, center = Offset(cx, cy))
        }
        name.startsWith("MECHANIC_") -> {
            val diamond = Path().apply {
                moveTo(cx, cy - 35f)
                lineTo(cx + 35f, cy)
                lineTo(cx, cy + 35f)
                lineTo(cx - 35f, cy)
                close()
            }
            drawPath(diamond, SciFiGold.copy(alpha = 0.4f))
            drawPath(diamond, SciFiGold.copy(alpha = 0.8f * pulse), style = Stroke(width = 2f))
            drawCircle(SciFiGold, radius = 5f, center = Offset(cx, cy))
        }
        name.startsWith("LOG_") -> {
            val docW = 50f
            val docH = 60f
            drawRoundRect(Color(0xFF2A2A2A), Offset(cx - docW / 2f, cy - docH / 2f), Size(docW, docH), CornerRadius(4f, 4f))
            drawRoundRect(SciFiWhite.copy(alpha = 0.5f * pulse), Offset(cx - docW / 2f, cy - docH / 2f), Size(docW, docH), CornerRadius(4f, 4f), style = Stroke(width = 1.5f))
            val lineCount = 4
            for (i in 0 until lineCount) {
                val lx = cx - docW / 3f
                val ly = cy - docH / 4f + i * (docH / (lineCount + 1))
                val lw = docW * (0.5f + i * 0.08f)
                drawLine(SciFiWhite.copy(alpha = 0.3f), Offset(lx, ly), Offset(lx + lw, ly), strokeWidth = 2f)
            }
        }
        name.startsWith("ROCKET_") || name.startsWith("SKIN_") -> {
            val rPath = Path().apply {
                moveTo(cx, cy - 35f)
                lineTo(cx + 15f, cy + 5f)
                lineTo(cx + 15f, cy + 20f)
                lineTo(cx - 15f, cy + 20f)
                lineTo(cx - 15f, cy + 5f)
                close()
            }
            drawPath(rPath, SciFiWhite.copy(alpha = 0.3f))
            drawPath(rPath, SciFiWhite.copy(alpha = 0.7f * pulse), style = Stroke(width = 2f))
            drawCircle(SciFiRed.copy(alpha = 0.6f), radius = 8f, center = Offset(cx, cy + 22f))
        }
        name.startsWith("THEME_") -> {
            drawCircle(SciFiPurple.copy(alpha = 0.3f), radius = 50f, center = Offset(cx, cy))
            drawCircle(SciFiPurple.copy(alpha = 0.5f * pulse), radius = 35f, center = Offset(cx, cy), style = Stroke(width = 3f))
            drawCircle(SciFiPurple, radius = 8f, center = Offset(cx, cy))
        }
        name.startsWith("ACHIEVEMENT_") -> {
            val starPath = Path().apply {
                for (i in 0 until 10) {
                    val angle = i * (kotlin.math.PI.toFloat() / 5f) - kotlin.math.PI.toFloat() / 2f
                    val r = if (i % 2 == 0) 35f else 15f
                    if (i == 0) moveTo(cx + r * cos(angle), cy + r * sin(angle))
                    else lineTo(cx + r * cos(angle), cy + r * sin(angle))
                }
                close()
            }
            drawPath(starPath, SciFiGold.copy(alpha = 0.4f))
            drawPath(starPath, SciFiGold.copy(alpha = 0.9f * pulse), style = Stroke(width = 2f))
        }
        name.startsWith("ARTIFACT_") -> {
            val octPath = Path().apply {
                for (i in 0 until 8) {
                    val angle = i * (kotlin.math.PI.toFloat() / 4f)
                    val r = if (i % 2 == 0) 40f else 25f
                    if (i == 0) moveTo(cx + r * cos(angle), cy + r * sin(angle))
                    else lineTo(cx + r * cos(angle), cy + r * sin(angle))
                }
                close()
            }
            drawPath(octPath, SciFiRed.copy(alpha = 0.3f))
            drawPath(octPath, SciFiRed.copy(alpha = 0.7f * pulse), style = Stroke(width = 2f))
            drawCircle(Color(0xFF9C27B0).copy(alpha = 0.6f * pulse), radius = 10f, center = Offset(cx, cy))
        }
        else -> {
            drawCircle(Color(0xFF3A3A3A), radius = 40f, center = Offset(cx, cy))
            drawCircle(SciFiWhite.copy(alpha = 0.4f * pulse), radius = 40f, center = Offset(cx, cy), style = Stroke(width = 1.5f))
        }
    }
}

private fun zoneColorForType(type: DiscoveryType): Color = when (type) {
    DiscoveryType.AREA_CLOUDS -> SciFiCyan
    DiscoveryType.AREA_ATMOSPHERE -> Color(0xFF4FC3F7)
    DiscoveryType.AREA_ORBIT -> Color(0xFF1A237E)
    DiscoveryType.AREA_SPACE -> Color(0xFF311B92)
    DiscoveryType.AREA_FOUNDRY -> SciFiGold
    DiscoveryType.AREA_CHRONO_RIFT -> SciFiPurple
    DiscoveryType.AREA_VOID -> SciFiRed
    DiscoveryType.AREA_BEYOND -> Color(0xFFFF4081)
    DiscoveryType.AREA_GATE -> Color(0xFF00E5FF)
    DiscoveryType.AREA_CONSTRUCT -> Color(0xFF76FF03)
    DiscoveryType.AREA_SINGULARITY -> Color(0xFFFFFFFF)
    else -> SciFiWhite
}

private fun DrawScope.drawGlitchEffect(cx: Float, cy: Float, gameTime: Long) {
    val scanAlpha = sin(gameTime / 80f) * 0.3f + 0.5f
    val glitchSeed = (gameTime / 200).toInt()

    drawRect(Color.Black, Offset(cx - 60f, cy - 60f), Size(120f, 120f))

    val barCount = (sin(gameTime / 300f) * 3f + 5f).toInt()
    repeat(barCount) { i ->
        val rng = Random(glitchSeed + i * 7)
        val barX = cx - 60f + rng.nextFloat() * 120f
        val barY = cy - 60f + rng.nextFloat() * 120f
        val barW = rng.nextFloat() * 80f + 10f
        val barH = rng.nextFloat() * 8f + 2f
        val barColor = when (rng.nextInt(3)) {
            0 -> GLITCH_GREEN
            1 -> GLITCH_PINK
            else -> STATIC_WHITE
        }
        drawRect(barColor.copy(alpha = rng.nextFloat() * 0.6f + 0.2f), Offset(barX, barY), Size(barW, barH))
    }

    repeat(3) { i ->
        val sy = cy - 60f + (gameTime / 50f + i * 40f) % 120f
        drawLine(GLITCH_GREEN.copy(alpha = 0.3f * scanAlpha), Offset(cx - 60f, sy), Offset(cx + 60f, sy), strokeWidth = 1f)
    }

    if ((gameTime / 100) % 3 == 0L) {
        drawRect(Color.White.copy(alpha = 0.15f), Offset(cx - 60f, cy - 60f), Size(120f, 120f))
    }

    val textStr = "NO SIGNAL"
    drawContext.canvas.nativeCanvas.drawText(
        textStr,
        cx,
        cy + 4f,
        android.graphics.Paint().apply {
            color = android.graphics.Color.argb((128 * scanAlpha).toInt(), 0, 255, 65)
            textSize = 16f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
        }
    )
}
