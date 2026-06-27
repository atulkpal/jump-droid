package com.ashwathai.jump_droid

import androidx.compose.ui.graphics.Color
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import com.ashwathai.jump_droid.ui.theme.SciFiGold
import com.ashwathai.jump_droid.ui.theme.SciFiRed
import kotlin.math.max
import kotlin.math.min

/**
 * Manages survival mechanics including damage distribution, shield regeneration,
 * and the catastrophic destruction sequence.
 * Extracted from GameScreen.kt as part of Sprint T4.
 */
class SurvivalManager {

    /**
     * Applies damage to the player, distributing it between shields and hull integrity.
     */
    fun applyDamage(
        amount: Float,
        player: Player,
        isGameOver: Boolean,
        onGameOver: () -> Unit,
        onVisualFeedback: (shake: Float, flash: Float) -> Unit,
        onBurst: (x: Float, y: Float, count: Int, color: Color, speed: Float) -> Unit,
        onPlaySfx: (String) -> Unit = {},
        onVibrate: (HapticManager.HapticType) -> Unit = {}
    ) {
        if (amount <= 0 || isGameOver) return

        // Visual Feedback
        onVisualFeedback((12f + amount * 0.5f).coerceAtMost(40f), 0.7f)

        var remainingDamage = amount

        // EPIC 7: Module Damage Hook
        player.activeModules.forEach {
            remainingDamage = it.onDamageTaken(player, remainingDamage, onVisualFeedback, onBurst)
        }

        // 1. Shield Absorption
        if (player.shield > 0 && !player.infiniteShield) {
            val shieldDamage = min(player.shield, remainingDamage)
            
            // EPIC 7: Module Shield Hit Hook
            player.activeModules.forEach {
                it.onShieldHit(player, shieldDamage, onVisualFeedback, onBurst)
            }

            player.shield -= shieldDamage
            remainingDamage -= shieldDamage

            // Shield Hit Feedback (Cyan)
            onBurst(player.x, player.y, 12, SciFiCyan, 450f)
            onPlaySfx("sfx_hit_shield")
            onVibrate(HapticManager.HapticType.IMPACT_MEDIUM)
        }

        // 2. Integrity Damage
        if (remainingDamage > 0 && !player.invincibleHull) {
            player.integrity = max(0f, player.integrity - remainingDamage)

            // Hull Hit Feedback (Red/Gold sparks)
            onBurst(player.x, player.y, 18, SciFiRed, 650f)
            onBurst(player.x, player.y, 8, SciFiGold, 400f)
            onPlaySfx("sfx_hit_hull")
            onVibrate(HapticManager.HapticType.IMPACT_HEAVY)

            if (player.integrity <= 0) {
                onGameOver()
            }
        }

        // Pause Regen
        player.shieldRegenPauseTimer = Constants.SHIELD_REGEN_DELAY
    }

    /**
     * Updates survival timers, regeneration, and destruction lifecycle.
     */
    fun update(
        dt: Float,
        player: Player,
        gameTime: Long,
        notificationManager: NotificationManager,
        onGameOver: () -> Unit,
        onShake: (Float) -> Unit,
        shieldRegenMultiplier: Float = 1.0f,
        onPlaySfx: (String) -> Unit = {},
        onVibrate: (HapticManager.HapticType) -> Unit = {}
    ) {
        // Shield Regeneration
        if (player.shieldRegenPauseTimer > 0) {
            player.shieldRegenPauseTimer = max(0f, player.shieldRegenPauseTimer - dt)
        } else if (player.shield < player.maxShield) {
            player.shield = min(player.maxShield, player.shield + Constants.SHIELD_REGEN_RATE * shieldRegenMultiplier * dt)
        }

        // Hull Failure & Destruction Sequence
        if (player.integrity <= 0 && player.destructionTimer <= 0) {
            player.destructionTimer = 0.01f // Start sequence
            onShake(30f)
            onPlaySfx("death")
            onVibrate(HapticManager.HapticType.EXPLOSION)
        }

        if (player.destructionTimer > 0) {
            player.destructionTimer += dt
            player.velocityX *= 0.95f
            player.velocityY += 500f * dt // Loss of control, falling

            if (player.destructionTimer > 2.0f) {
                onGameOver()
            }
        }

        // Emergency Warnings (Throttled)
        if (gameTime % 3000 < (dt * 1000).toLong().coerceAtLeast(1L)) {
            if (player.shield > 0 && player.shield < player.maxShield * Constants.SURVIVAL_CRITICAL_THRESHOLD) {
                notificationManager.post("!!! SHIELD CRITICAL !!!", NotificationPriority.CRITICAL)
            }
            if (player.integrity < player.maxIntegrity * Constants.SURVIVAL_CRITICAL_THRESHOLD) {
                notificationManager.post("!!! HULL CRITICAL !!!", NotificationPriority.CRITICAL)
                onPlaySfx("sfx_alarm_critical")
                onVibrate(HapticManager.HapticType.WARNING)
            }
            if (player.fuel < player.maxFuel * 0.2f) {
                onPlaySfx("sfx_alarm_low_fuel")
                onVibrate(HapticManager.HapticType.WARNING)
            }
        }
    }
}
