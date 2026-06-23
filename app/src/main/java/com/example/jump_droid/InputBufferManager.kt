package com.example.jump_droid

import androidx.compose.ui.geometry.Offset
import java.util.LinkedList

data class ThrustEvent(
    val timestamp: Long,
    val isThrusting: Boolean,
    val target: Offset
)

class InputBufferManager {
    private val eventQueue = LinkedList<ThrustEvent>()
    
    private var lastRecordedThrust = false
    private var lastRecordedTarget = Offset.Zero

    fun recordInput(isThrusting: Boolean, target: Offset, gameTime: Long) {
        if (!DevConfig.ENABLE_INPUT_BUFFER) {
            lastRecordedThrust = isThrusting
            lastRecordedTarget = target
            return
        }
        
        eventQueue.add(ThrustEvent(gameTime, isThrusting, target))
    }

    fun getEffectiveThrust(gameTime: Long, latencySeconds: Float): Pair<Boolean, Offset> {
        if (!DevConfig.ENABLE_INPUT_BUFFER || latencySeconds <= 0f) {
            // Clean up queue if no latency
            eventQueue.clear()
            return lastRecordedThrust to lastRecordedTarget
        }

        val latencyMillis = (latencySeconds * 1000).toLong()
        val targetTime = gameTime - latencyMillis

        // Find the most recent event that is older than targetTime
        var effectiveEvent: ThrustEvent? = null
        val iterator = eventQueue.iterator()
        while (iterator.hasNext()) {
            val event = iterator.next()
            if (event.timestamp <= targetTime) {
                effectiveEvent = event
                iterator.remove() // Remove processed events
            } else {
                break // Events are ordered by time
            }
        }

        if (effectiveEvent != null) {
            lastRecordedThrust = effectiveEvent.isThrusting
            lastRecordedTarget = effectiveEvent.target
        }

        return lastRecordedThrust to lastRecordedTarget
    }

    fun clear() {
        eventQueue.clear()
        lastRecordedThrust = false
        lastRecordedTarget = Offset.Zero
    }
}
