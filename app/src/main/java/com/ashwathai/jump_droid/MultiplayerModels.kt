package com.ashwathai.jump_droid

import androidx.compose.ui.graphics.Color
import com.ashwathai.jump_droid.ui.theme.*

enum class RoomStatus {
    LOBBY, STARTING, ACTIVE, ENDED
}

data class MultiplayerRoom(
    val code: String = "",
    val hostId: String = "",
    val guestId: String? = null,
    val hostName: String = "",
    val guestName: String? = null,
    val status: RoomStatus = RoomStatus.LOBBY,
    val seed: Int = 0,
    val startTime: Long = 0L
)

data class PlayerMultiplayerState(
    val x: Float = 0f,
    val y: Float = 0f,
    val vx: Float = 0f,
    val vy: Float = 0f,
    val isThrusting: Boolean = false,
    val rocketType: String = "BALANCED",
    val integrity: Float = 100f,
    val shield: Float = 100f,
    val timestamp: Long = 0L
)

data class GlobalBroadcast(
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val color: Int = 0xFF00E5FF.toInt() // SciFiCyan
)
