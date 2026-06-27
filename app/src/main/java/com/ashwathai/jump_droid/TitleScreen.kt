package com.ashwathai.jump_droid

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import com.ashwathai.jump_droid.ui.theme.SciFiGold
import com.ashwathai.jump_droid.ui.theme.SciFiGreen
import com.ashwathai.jump_droid.ui.theme.SciFiRed
import com.ashwathai.jump_droid.ui.theme.SciFiWhite
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private data class TitleStar(
    var x: Float, var y: Float, var speed: Float,
    val baseAlpha: Float, var twinklePhase: Float,
    val size: Float, val color: Color
)

private data class TitleSilhouette(
    var x: Float, var y: Float, var speed: Float,
    val baseY: Float, val type: Int
)

private data class TitleDrone(
    var x: Float, var y: Float, var dir: Float,
    var detected: Boolean = false
)

private val zoneColors = listOf(
    Color(0xFF4CAF50), Color(0xFF00BCD4), Color(0xFF9C27B0),
    Color(0xFFFFD700), Color(0xFF673AB7), Color(0xFFD32F2F)
)

@Composable
fun TitleScreen(onNavigate: (GameState) -> Unit, soundManager: SoundManager? = null) {
    val density = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition(label = "TitleTransition")
    val glowAlpha by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "GlowAlpha")
    val rocketBob by infiniteTransition.animateFloat(0f, -20f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "RocketOffset")
    val flameFlicker by infiniteTransition.animateFloat(0.7f, 1f, infiniteRepeatable(tween(80), RepeatMode.Reverse), label = "FlameFlicker")

    val stars = remember {
        List(120) {
            TitleStar(
                x = Random.nextFloat() * 2000f,
                y = Random.nextFloat() * 2000f,
                speed = 0.2f + Random.nextFloat() * 0.6f,
                baseAlpha = 0.2f + Random.nextFloat() * 0.6f,
                twinklePhase = Random.nextFloat() * 6.28f,
                size = 0.5f + Random.nextFloat() * 2f,
                color = zoneColors[Random.nextInt(zoneColors.size)]
            )
        }
    }

    val silhouettes = remember {
        List(8) {
            TitleSilhouette(
                x = Random.nextFloat() * 2000f,
                y = Random.nextFloat() * 2000f,
                speed = 0.15f + Random.nextFloat() * 0.3f,
                baseY = Random.nextFloat() * 2000f,
                type = Random.nextInt(4)
            )
        }
    }

    val frameTime = remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(50)
            frameTime.value += 50
        }
    }

    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Color(0xFF0a0a1a))
            val ft = frameTime.value / 1000f
            val w = size.width
            val h = size.height

            stars.forEach { s ->
                s.y += s.speed
                if (s.y > h + 10) { s.y = -10f; s.x = Random.nextFloat() * w }
                val twinkle = sin(ft * 2f + s.twinklePhase) * 0.3f + 0.7f
                val alpha = s.baseAlpha * twinkle
                drawCircle(s.color.copy(alpha = alpha), radius = s.size, center = Offset(s.x, s.y))
            }

            silhouettes.forEach { si ->
                si.x -= si.speed
                if (si.x < -100f) { si.x = w + 100f; si.y = si.baseY; if (si.y > h) si.y = Random.nextFloat() * h }
                val sy = si.y + sin(ft * 0.3f + si.x * 0.01f) * 8f
                when (si.type) {
                    0 -> {
                        drawCircle(Color(0xFF2E7D32).copy(alpha = 0.15f), radius = 30f, center = Offset(si.x, sy))
                        drawCircle(Color(0xFF388E3C).copy(alpha = 0.25f), radius = 20f, center = Offset(si.x, sy))
                        drawCircle(Color(0xFF4CAF50).copy(alpha = 0.3f), radius = 10f, center = Offset(si.x, sy))
                    }
                    1 -> {
                        drawOval(Color(0xFF0097A7).copy(alpha = 0.12f), topLeft = Offset(si.x - 35f, sy - 12f), size = Size(70f, 24f))
                        drawOval(Color(0xFF00BCD4).copy(alpha = 0.2f), topLeft = Offset(si.x - 25f, sy - 8f), size = Size(50f, 16f))
                    }
                    2 -> {
                        val satPath = Path().apply {
                            moveTo(si.x - 12f, sy); lineTo(si.x + 12f, sy)
                            moveTo(si.x, sy - 8f); lineTo(si.x, sy + 8f)
                        }
                        drawPath(satPath, Color(0xFFFFD700).copy(alpha = 0.2f), style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))
                        drawCircle(Color(0xFFFFD700).copy(alpha = 0.15f), radius = 4f, center = Offset(si.x, sy))
                    }
                    3 -> {
                        drawCircle(Color(0xFF7B1FA2).copy(alpha = 0.12f), radius = 22f, center = Offset(si.x, sy))
                        drawCircle(Color(0xFF9C27B0).copy(alpha = 0.18f), radius = 14f, center = Offset(si.x, sy))
                    }
                }
            }

            // --- Scanning Drone (cinematic patrol with depth) ---
            val patrolPhase = ft * 0.12f
            val droneX = w * (sin(patrolPhase) * 0.45f + 0.5f)
            val droneZ = sin(patrolPhase * 1.5f) * 0.35f + 0.65f
            val droneY = h * 0.22f + sin(ft * 1.5f + patrolPhase) * 12f
            val droneDir = cos(patrolPhase)
            val droneScale = 0.6f + droneZ * 0.6f

            // Radar sweep line (thin rotating beam)
            val sweepAngle = ft * 2.5f
            val beamLen = 160f * droneScale
            val rocketCenterX = w / 2
            val rocketCenterY = h * 0.28f + rocketBob / density.density
            val dx = rocketCenterX - droneX
            val dy = rocketCenterY - droneY
            val dist = sqrt(dx * dx + dy * dy)
            val angleToRocket = atan2(dy, dx)
            val beamAngle = PI.toFloat() / 2f + sin(sweepAngle) * 0.5f
            val detected = dist < beamLen * 1.5f && abs(angleToRocket - beamAngle) < 0.4f

            // Beam glow
            val beamAlpha = if (detected) 0.4f else 0.12f
            val beamColor = if (detected) SciFiRed else SciFiCyan
            val sweepEndX = droneX + cos(beamAngle) * beamLen
            val sweepEndY = droneY + sin(beamAngle) * beamLen
            for (i in 3 downTo 1) {
                val glowAlpha = beamAlpha * (0.3f / i)
                drawLine(
                    beamColor.copy(alpha = glowAlpha),
                    Offset(droneX, droneY),
                    Offset(sweepEndX, sweepEndY),
                    strokeWidth = 4f * i * droneScale
                )
            }
            drawLine(
                beamColor.copy(alpha = beamAlpha * 1.5f),
                Offset(droneX, droneY),
                Offset(sweepEndX, sweepEndY),
                strokeWidth = 1.5f * droneScale
            )

            // Light column below drone
            drawLine(
                SciFiCyan.copy(alpha = 0.06f),
                Offset(droneX, droneY),
                Offset(droneX, droneY + 40f * droneScale),
                strokeWidth = 3f * droneScale
            )

            // Drone body (scaled by Z-depth)
            val dBodyW = 18f * droneScale
            val dBodyH = 10f * droneScale
            drawRoundRect(
                Color(0xFF2A2A3A),
                topLeft = Offset(droneX - dBodyW / 2, droneY - dBodyH / 2),
                size = Size(dBodyW, dBodyH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f * droneScale)
            )
            drawRoundRect(
                SciFiCyan.copy(alpha = 0.3f),
                topLeft = Offset(droneX - dBodyW / 2, droneY - dBodyH / 2),
                size = Size(dBodyW, dBodyH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f * droneScale),
                style = androidx.compose.ui.graphics.drawscope.Stroke(0.8f * droneScale)
            )

            // Antenna
            val antH = 8f * droneScale
            drawLine(Color(0xFF555575), Offset(droneX, droneY - dBodyH / 2), Offset(droneX, droneY - dBodyH / 2 - antH), strokeWidth = 1.5f * droneScale)
            drawCircle(Color(0xFFFF4444), radius = 2f * droneScale, center = Offset(droneX, droneY - dBodyH / 2 - antH))

            // Blinking light
            val lightOn = (ft * 3f).toInt() % 2 == 0
            drawCircle(
                if (lightOn) Color(0xFF00FF88) else Color(0xFF006633),
                radius = 2f * droneScale,
                center = Offset(droneX, droneY - 2f * droneScale)
            )

            // Thruster (pushes opposite to direction)
            val thrX = droneX - droneDir * 10f * droneScale
            val thrGlow = 0.4f + sin(ft * 8f) * 0.2f
            drawCircle(SciFiRed.copy(alpha = thrGlow), radius = 3f * droneScale, center = Offset(thrX, droneY))
            drawCircle(SciFiRed.copy(alpha = thrGlow * 0.3f), radius = 6f * droneScale, center = Offset(thrX, droneY))

            // Rocket detection glow
            if (detected) {
                val pulse = sin(ft * 12f) * 0.3f + 0.7f
                drawCircle(SciFiRed.copy(alpha = 0.2f * pulse), radius = 24f, center = Offset(rocketCenterX, rocketCenterY))
                drawCircle(SciFiRed.copy(alpha = 0.08f * pulse), radius = 40f, center = Offset(rocketCenterX, rocketCenterY))
            }

            val gx = w / 2
            val gy = h * 0.28f + rocketBob / density.density
            val bodyW = 24f
            val bodyH = 48f
            val noseH = 22f

            drawRoundRect(
                SciFiWhite.copy(alpha = 0.85f),
                topLeft = Offset(gx - bodyW / 2, gy - bodyH / 2),
                size = Size(bodyW, bodyH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
            drawRoundRect(
                SciFiCyan.copy(alpha = 0.2f),
                topLeft = Offset(gx - bodyW / 2 + 3f, gy - bodyH / 2 + 3f),
                size = Size(bodyW - 6f, bodyH - 6f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            drawPath(
                Path().apply { moveTo(gx - bodyW / 2, gy - bodyH / 2); lineTo(gx, gy - bodyH / 2 - noseH); lineTo(gx + bodyW / 2, gy - bodyH / 2); close() },
                SciFiRed
            )
            drawPath(
                Path().apply {
                    moveTo(gx - bodyW / 2 + 4f, gy + bodyH / 2)
                    lineTo(gx - bodyW / 2 - 6f, gy + bodyH / 2 + 14f)
                    lineTo(gx - bodyW / 2 - 2f, gy + bodyH / 2 + 14f)
                    lineTo(gx - bodyW / 2, gy + bodyH / 2 + 8f)
                    close()
                },
                SciFiGold.copy(alpha = 0.7f)
            )
            drawPath(
                Path().apply {
                    moveTo(gx + bodyW / 2 - 4f, gy + bodyH / 2)
                    lineTo(gx + bodyW / 2 + 6f, gy + bodyH / 2 + 14f)
                    lineTo(gx + bodyW / 2 + 2f, gy + bodyH / 2 + 14f)
                    lineTo(gx + bodyW / 2, gy + bodyH / 2 + 8f)
                    close()
                },
                SciFiGold.copy(alpha = 0.7f)
            )

            drawPath(
                Path().apply { moveTo(gx - 5f, gy + bodyH / 2); lineTo(gx, gy + bodyH / 2 + 16f * flameFlicker); lineTo(gx + 5f, gy + bodyH / 2); close() },
                SciFiGold.copy(alpha = 0.9f)
            )
            drawPath(
                Path().apply { moveTo(gx - 3f, gy + bodyH / 2); lineTo(gx, gy + bodyH / 2 + 24f * flameFlicker); lineTo(gx + 3f, gy + bodyH / 2); close() },
                SciFiRed.copy(alpha = 0.8f)
            )
            drawCircle(
                SciFiGold.copy(alpha = 0.15f),
                radius = 18f,
                center = Offset(gx, gy + bodyH / 2 + 16f * flameFlicker)
            )

            // --- Ambient scan rings ---
            val ringPhase = ft * 0.8f
            val ringBase = ringPhase % 1f
            repeat(3) { i ->
                val rp = (ringBase + i * 0.33f) % 1f
                val ringRadius = 30f + rp * 80f
                val ringAlpha = (1f - rp) * 0.15f
                drawCircle(
                    SciFiCyan.copy(alpha = ringAlpha),
                    radius = ringRadius,
                    center = Offset(gx, gy),
                    style = Stroke(width = 1f)
                )
            }

            // --- Floating data particles ---
            repeat(6) { i ->
                val seed = i * 137 + (ft / 0.5f).toInt()
                val prng = Random(seed)
                val px = gx + sin(ft * 0.5f + i * 1.2f) * 60f + prng.nextFloat() * 10f
                val py = gy - 40f + cos(ft * 0.7f + i * 1.8f) * 40f + prng.nextFloat() * 10f
                val pSize = 1f + prng.nextFloat() * 1.5f
                val pColor = listOf(SciFiCyan, SciFiGold, SciFiGreen)[i % 3]
                drawCircle(pColor.copy(alpha = 0.3f + sin(ft * 1.5f + i.toFloat()) * 0.15f), radius = pSize, center = Offset(px, py))
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .safeDrawingPadding()
                .align(Alignment.Center)
        ) {
            Spacer(Modifier.height(120.dp))
            Text(
                text = "JUMP DROID",
                style = MaterialTheme.typography.displayLarge.copy(
                    shadow = Shadow(SciFiCyan.copy(alpha = glowAlpha), Offset(0f, 0f), 24f),
                    letterSpacing = 8.sp
                ),
                color = SciFiWhite,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "THE ASCENSION PROGRAM",
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 6.sp,
                    fontSize = 11.sp
                ),
                color = SciFiWhite.copy(alpha = 0.4f)
            )
            Spacer(Modifier.height(100.dp))
            Button(
                onClick = { 
                    soundManager?.playSfx("sfx_ui_confirm")
                    onNavigate(GameState.MAIN_MENU) 
                },
                modifier = Modifier.width(240.dp).height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SciFiCyan)
            ) {
                Text("INITIATE ASCENT", color = Color.Black, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
            Spacer(Modifier.height(16.dp))
            GlobalAdBanner()
        }

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("THE ASCENSION PROGRAM // EST. 1984", color = SciFiWhite.copy(alpha = 0.3f), letterSpacing = 1.sp, fontSize = 10.sp)
            Spacer(Modifier.height(4.dp))
            Text("POWERED BY ASHWATH.AI // V1.2.0", color = SciFiWhite.copy(alpha = 0.2f), letterSpacing = 1.sp, fontSize = 8.sp)
        }
    }
}
