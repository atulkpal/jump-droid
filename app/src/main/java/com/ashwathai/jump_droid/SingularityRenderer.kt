package com.ashwathai.jump_droid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class SingularityRenderer : ThreatRenderer {
    override fun render(
        drawScope: DrawScope,
        threat: ActiveThreat,
        cameraY: Float,
        alpha: Float,
        gameTime: Long,
        player: Player,
        context: android.content.Context?
    ) {
        val cx = threat.x
        val cy = threat.y - cameraY
        val phase = threat.phase
        val localTimer = threat.localTimer
        val scanPulse = threat.scanPulse
        val rotation = threat.rotation
        val remainingWp = threat.activeWeakPoints.toFloat() / threat.maxWeakPoints.coerceAtLeast(1)
        
        // Phase color
        val coreColor = when {
            phase == 4 -> Color(0xFFFF1744)
            phase == 3 -> Color(0xFFD500F9)
            else -> Color.White
        }
        
        // 1. Background aura — intensifies with fewer weak points
        val auraRadius = 120f + (1f - remainingWp) * 60f
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    coreColor.copy(alpha = 0.15f * alpha * scanPulse),
                    Color(0xFF1A237E).copy(alpha = 0.05f * alpha),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = auraRadius
            ),
            radius = auraRadius,
            center = Offset(cx, cy)
        )

        // 2. Laser target indicator — rotates and flashes before firing
        if (threat.projectileCooldown < 0.5f && phase >= 2) {
            val laserAngle = rotation * (kotlin.math.PI.toFloat() / 180f)
            val laserLen = 500f
            val flashAlpha = (sin(gameTime / 30f) * 0.5f + 0.5f) * 0.6f * alpha
            drawScope.drawLine(
                color = coreColor.copy(alpha = flashAlpha * 0.3f),
                start = Offset(cx, cy),
                end = Offset(cx + cos(laserAngle) * laserLen, cy + sin(laserAngle) * laserLen),
                strokeWidth = 8f
            )
            if (phase >= 3) {
                val laserAngle2 = laserAngle + 1.57f
                drawScope.drawLine(
                    color = coreColor.copy(alpha = flashAlpha * 0.2f),
                    start = Offset(cx, cy),
                    end = Offset(cx + cos(laserAngle2) * laserLen, cy + sin(laserAngle2) * laserLen),
                    strokeWidth = 8f
                )
            }
        }
        
        // 3. Shifting White Noise Core
        val coreRadius = when (phase) {
            4 -> 50f + scanPulse * 30f
            3 -> 55f + scanPulse * 25f
            else -> 60f + scanPulse * 20f
        }
        drawScope.drawCircle(
            color = coreColor.copy(alpha = 0.8f * alpha),
            radius = coreRadius,
            center = Offset(cx, cy)
        )
        drawScope.drawCircle(
            color = Color.White.copy(alpha = 0.3f * alpha * (sin(gameTime / 80f) * 0.3f + 0.7f)),
            radius = coreRadius * 0.6f,
            center = Offset(cx, cy)
        )
        
        // 4. Geometric Fragments — more chaotic in later phases
        val fragCount = when (phase) { 4 -> 20; 3 -> 16; else -> 12 }
        repeat(fragCount) { i ->
            val angle = (gameTime / (400f + 100f * (1f - remainingWp))) + (i * (6.28f / fragCount))
            val dist = 100f + sin(gameTime / (200f + 100f * (1f - remainingWp)) + i) * 30f
            val fx = cx + cos(angle) * dist
            val fy = cy + sin(angle) * dist
            val fragSize = if (phase == 4) 15f else 10f
            
            drawScope.rotate(degrees = (gameTime / 8f) + i * 30f, pivot = Offset(fx, fy)) {
                drawScope.drawRect(
                    color = coreColor.copy(alpha = 0.5f * alpha * (sin(gameTime / 150f + i) * 0.3f + 0.7f)),
                    topLeft = Offset(fx - fragSize, fy - fragSize),
                    size = Size(fragSize * 2, fragSize * 2),
                    style = Stroke(width = 2f)
                )
            }
        }
        
        // 5. Phase 4: attack ring — spinning circle of energy
        if (phase == 4) {
            val ringAlpha = (sin(gameTime / 50f) * 0.3f + 0.7f) * alpha
            drawScope.drawCircle(
                color = Color(0xFFFF1744).copy(alpha = ringAlpha * 0.2f),
                radius = auraRadius,
                center = Offset(cx, cy),
                style = Stroke(width = 4f)
            )
            drawScope.drawCircle(
                color = Color.White.copy(alpha = ringAlpha * 0.5f),
                radius = auraRadius - 5f,
                center = Offset(cx, cy),
                style = Stroke(width = 1f)
            )
        }

        // D1: Digital Glitch Geometry (Wireframe flickering)
        if (Random(gameTime).nextFloat() < 0.15f) {
            val wireColor = coreColor.copy(alpha = 0.4f * alpha)
            drawScope.rotate(rotation * 1.5f, pivot = Offset(cx, cy)) {
                drawScope.drawRect(
                    color = wireColor,
                    topLeft = Offset(cx - 80f, cy - 80f),
                    size = Size(160f, 160f),
                    style = Stroke(width = 1.5f)
                )
            }
        }

        val riftCount = when (phase) { 4 -> 12; 3 -> 8; else -> 5 }
        // D2: Reality Fracture (Color-inverting screen tears)
        repeat(riftCount) { i ->
            val r = Random(gameTime + i * 37L)
            val rx = cx + (r.nextFloat() - 0.5f) * 600f
            val ry = cy + (r.nextFloat() - 0.5f) * 600f
            val riftLen = if (phase == 4) 150f else 80f
            
            val fractureColor = if (r.nextBoolean()) coreColor else Color.White
            drawScope.drawLine(
                color = fractureColor.copy(alpha = 0.3f * alpha),
                start = Offset(rx - riftLen, ry + (r.nextFloat()-0.5f) * 10f),
                end = Offset(rx + riftLen, ry + (r.nextFloat()-0.5f) * 10f),
                strokeWidth = 3f
            )
            // Secondary glitch line
            if (phase >= 3) {
                drawScope.drawLine(
                    color = Color.Black.copy(alpha = 0.5f * alpha),
                    start = Offset(rx - riftLen, ry),
                    end = Offset(rx + riftLen, ry),
                    strokeWidth = 1f
                )
            }
        }

        // D3: The Event Horizon (Screen-swallowing dark halo)
        if (phase == 4) {
            val horizonPulse = sin((gameTime / 120f).toDouble()).toFloat() * 0.1f + 0.9f
            drawScope.drawCircle(
                brush = Brush.radialGradient(
                    0.0f to Color.Black,
                    0.4f to Color.Black,
                    0.7f to Color(0xFF1A237E).copy(alpha = 0.6f * alpha),
                    1.0f to Color.Transparent,
                    center = Offset(cx, cy),
                    radius = 800f * horizonPulse
                ),
                radius = 800f * horizonPulse,
                center = Offset(cx, cy)
            )
        }
        
        // 7. Weak point indicators
        if (remainingWp > 0) {
            val wpGlow = 0.5f + 0.5f * (1f - (threat.health / threat.definition.baseHealth).coerceIn(0f, 1f))
            val wpAngleStep = 360f / threat.maxWeakPoints.coerceAtLeast(1)
            repeat(threat.maxWeakPoints) { i ->
                if ((threat.wpDestroyedMask and (1 shl i)) == 0) {
                val wpAngle = (gameTime / 600f) * (180f / kotlin.math.PI.toFloat()) + i * wpAngleStep
                val wpRad = wpAngle * (kotlin.math.PI.toFloat() / 180f)
                val wpDist = 80f
                val wpx = cx + cos(wpRad) * wpDist
                val wpy = cy + sin(wpRad) * wpDist
                val wpPulse = sin(gameTime / 200f + i * 2f) * 0.3f + 0.7f
                drawScope.drawCircle(
                    color = Color(0xFFFF1744).copy(alpha = 0.6f * alpha * wpPulse * wpGlow),
                    radius = 6f * wpPulse,
                    center = Offset(wpx, wpy)
                )
                drawScope.drawCircle(
                    color = Color.White.copy(alpha = 0.3f * alpha * wpPulse * wpGlow),
                    radius = 3f,
                    center = Offset(wpx, wpy)
                )
            }
            }
        }
    }
}
