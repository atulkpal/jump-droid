package com.ashwathai.jump_droid

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color

class ProjectileManager {
    val projectiles = mutableStateListOf<Projectile>()

    fun spawn(
        x: Float,
        y: Float,
        vx: Float,
        vy: Float,
        type: ProjectileType,
        owner: ProjectileOwner,
        damage: Float,
        color: Color = Color.Yellow,
        size: Float = 10f,
        life: Float = 5.0f
    ) {
        if (!DevConfig.ENABLE_PROJECTILE_ENGINE) {
            return
        }
        projectiles.add(Projectile(x, y, vx, vy, type, owner, damage, color, size, life))
    }

    fun update(dt: Float) {
        val iterator = projectiles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.update(dt)
            if (p.life <= 0f) {
                iterator.remove()
            }
        }
    }

    fun processPlayerCollision(
        player: Player,
        onHit: (Projectile) -> Unit
    ) {
        if (player.destructionTimer > 0f) return
        
        val iterator = projectiles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            if (p.owner == ProjectileOwner.PLAYER) continue

            val dx = player.x - p.x
            val dy = player.y - p.y
            val distSq = dx * dx + dy * dy
            val radius = p.size + 15f // Simple radius check
            
            if (distSq < radius * radius) {
                onHit(p)
                iterator.remove()
            }
        }
    }

    fun clear() {
        projectiles.clear()
    }
}
