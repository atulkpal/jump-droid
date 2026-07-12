package com.ashwathai.jump_droid

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

class WorldRenderer {
    private val backgroundRenderer = ZoneBackgroundRenderer()
    private val rocketRenderer = RocketRenderer()
    private val platformRenderer = PlatformRenderer()

    fun render(drawScope: DrawScope, engine: GameEngine) {
        val score = engine.score
        val gameTime = engine.gameTime
        val cameraY = engine.cameraY
        val screenShake = engine.screenShake
        val impactFlashAlpha = engine.impactFlashAlpha
        val player = engine.player
        val altitudeManager = engine.altitudeManager
        val screenWidth = engine.screenWidth
        val screenHeight = engine.screenHeight
        val groundY = engine.groundY

        val shakeX = if (screenShake > 0) (Random.nextFloat() - 0.5f) * screenShake else 0f
        val shakeY = if (screenShake > 0) (Random.nextFloat() - 0.5f) * screenShake else 0f

        drawScope.translate(shakeX, shakeY) {
            // 1. Background
            backgroundRenderer.render(this, score, altitudeManager.currentZone, cameraY, gameTime)
            
            // 2. Ground (before everything else in-game)
            val isPlaying = engine.gameState == GameState.PLAYING || engine.gameState == GameState.ASCENSION_PROTOCOL || engine.gameState == GameState.PAUSED || engine.gameState == GameState.GAMEOVER
            if (isPlaying && altitudeManager.currentZone.ordinal < AltitudeZone.ORBIT.ordinal) {
                drawGround(groundY, cameraY, screenWidth, screenHeight, altitudeManager.currentZone)
            }

            // 3. Ambient
            engine.ambientManager.render(this, cameraY, gameTime)

            // 4. Platforms
            engine.platforms.forEach { 
                platformRenderer.render(this, it, altitudeManager.currentZone, cameraY, gameTime) 
            }

            // 5. Particles (Rich rendering)
            drawParticles(engine.particles, cameraY, gameTime)

            // 6. Landing Effects (Ring animation)
            drawLandingEffects(engine.landingEffects, cameraY, altitudeManager.currentZone)

            // 7. Projectiles (Rich rendering)
            drawProjectiles(engine.projectileManager.projectiles, cameraY, gameTime)

            // 8. Tether (Visual link)
            drawTether(player.activeTether, player.x, player.y, cameraY, gameTime)

            // 9. Flying Rewards (Combo reward animation)
            drawFlyingRewards(engine.flyingRewards)

            // 10. Threats
            engine.threatManager.activeThreats.forEach { threat ->
                ThreatRendererRegistry.forId(threat.definition.id)?.render(this, threat, cameraY, 1.0f, gameTime, player)
                drawBossHealthBar(threat, cameraY)
            }

            // 11. PowerUps
            drawPowerUps(engine.powerUpManager.powerUps, cameraY, gameTime)

            // 12. Rocket
            rocketRenderer.render(this, player, engine.effectiveThrust, engine.effectiveTarget, cameraY, gameTime)
        }

        // 13. Reality Distortion (Void zone visual)
        drawScope.drawRealityDistortion(
            engine.threatManager.activeThreats,
            player.x, player.y,
            drawScope.size,
            altitudeManager.currentZone,
            score
        )

        // 12. Visual Obstruction (Radial fog)
        drawScope.drawVisualObstruction(
            engine.globalFogAlpha,
            player.x, player.y,
            cameraY,
            drawScope.size
        )

        // 13. Speed Lines
        drawScope.drawSpeedLines(
            player.velocityY,
            screenWidth, screenHeight,
            altitudeManager.currentZone
        )

        // 14. Post-Processing (Impact Flash)
        if (impactFlashAlpha > 0f) {
            drawScope.drawRect(Color.White.copy(alpha = impactFlashAlpha.coerceIn(0f, 1f)), size = drawScope.size)
        }
    }
}
