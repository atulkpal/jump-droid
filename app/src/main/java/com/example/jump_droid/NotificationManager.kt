package com.example.jump_droid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class NotificationPriority {
    CRITICAL,
    TACTICAL,
    FLAVOR
}

data class NotificationEntry(
    val message: String,
    val priority: NotificationPriority,
    val duration: Float = 2.0f,
    val color: Color = Color.White
) {
    val isHighAlert: Boolean get() = message.contains("!!!") || message.contains(">>>")
}

class NotificationManager {
    private val internalQueue = mutableListOf<NotificationEntry>()
    val queue: List<NotificationEntry> get() = internalQueue.toList()
    var active by mutableStateOf<NotificationEntry?>(null)
    var alpha by mutableFloatStateOf(0f)
    var timer by mutableFloatStateOf(0f)

    private fun priorityIndex(p: NotificationPriority): Int = p.ordinal

    fun post(message: String, priority: NotificationPriority = NotificationPriority.TACTICAL, duration: Float = 2.0f, color: Color = Color.White) {
        // Dedup: skip if same message is already in queue
        if (internalQueue.any { it.message == message }) return
        if (active?.message == message) return

        val entry = NotificationEntry(message, priority, duration, color)
        val insertionIndex = internalQueue.indexOfLast { priorityIndex(it.priority) >= priorityIndex(priority) } + 1
        internalQueue.add(insertionIndex.coerceIn(0, internalQueue.size), entry)
    }

    fun update(dt: Float) {
        if (active != null) {
            timer -= dt
            if (timer <= 0f) {
                alpha -= dt * 2f
                if (alpha <= 0f) {
                    active = null
                }
            }
        }

        if (active == null && internalQueue.isNotEmpty()) {
            active = internalQueue.removeAt(0)
            alpha = 1f
            timer = active!!.duration
        }
    }

    fun showImmediately(message: String, initialAlpha: Float = 1.0f, priority: NotificationPriority = NotificationPriority.CRITICAL, duration: Float = 2.0f, color: Color = Color.White) {
        active?.let { current ->
            if (priorityIndex(priority) < priorityIndex(current.priority)) {
                // Preempt: push current back to queue front
                internalQueue.add(0, current)
            } else {
                // Same or lower priority — queue this one instead
                return
            }
        }
        active = NotificationEntry(message, priority, duration, color)
        alpha = initialAlpha
        timer = duration
    }

    fun clear() {
        internalQueue.clear()
        active = null
        alpha = 0f
        timer = 0f
    }
}
