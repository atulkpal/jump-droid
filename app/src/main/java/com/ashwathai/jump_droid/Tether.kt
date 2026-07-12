package com.ashwathai.jump_droid

class Tether(
    val anchorX: Float,
    val anchorY: Float,
    val maxLength: Float = 300f,
    val stiffness: Float = 0.5f,
    var isActive: Boolean = true
) {
    fun applyPhysics(player: Player, sdt: Float, cameraY: Float) {
        if (!isActive || !DevConfig.ENABLE_TETHER_PHYSICS) return

        val dx = player.x - anchorX
        val dy = (player.y - cameraY) - anchorY
        val dist = kotlin.math.sqrt(dx * dx + dy * dy)

        if (dist > maxLength) {
            val pullForce = (dist - maxLength) * stiffness
            val unitX = dx / dist
            val unitY = dy / dist

            player.velocityX -= unitX * pullForce * sdt * 60f
            player.velocityY -= unitY * pullForce * sdt * 60f
        }
    }
}
