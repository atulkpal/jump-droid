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
        player: Player
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

        // 6. Reality Rifts (Random lines) — more in later phases
        val riftCount = when (phase) { 4 -> 10; 3 -> 8; else -> 5 }
        repeat(riftCount) { i ->
            val rx = cx + (Random(gameTime + i * 17L).nextFloat() - 0.5f) * 500f
            val ry = cy + (Random(gameTime + i * 31L).nextFloat() - 0.5f) * 500f
            val riftLen = if (phase == 4) 100f else 50f
            drawScope.drawLine(
                color = coreColor.copy(alpha = 0.2f * alpha * (sin(gameTime / 100f + i) * 0.5f + 0.5f)),
                start = Offset(rx - riftLen, ry),
                end = Offset(rx + riftLen, ry),
                strokeWidth = 2f
            )
        }
        
        // 7. Weak point indicators
        if (remainingWp > 0) {
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
                    color = Color(0xFFFF1744).copy(alpha = 0.6f * alpha * wpPulse),
                    radius = 6f * wpPulse,
                    center = Offset(wpx, wpy)
                )
                drawScope.drawCircle(
                    color = Color.White.copy(alpha = 0.3f * alpha * wpPulse),
                    radius = 3f,
                    center = Offset(wpx, wpy)
                )
            }
            }
        }
    }
}
