package com.ashwathai.jump_droid

import android.content.SharedPreferences
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.ashwathai.jump_droid.ui.theme.SciFiBackground
import com.ashwathai.jump_droid.ui.theme.SciFiBorder
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import com.ashwathai.jump_droid.ui.theme.SciFiGold
import com.ashwathai.jump_droid.ui.theme.SciFiGreen
import com.ashwathai.jump_droid.ui.theme.SciFiRed
import com.ashwathai.jump_droid.ui.theme.SciFiSurface
import com.ashwathai.jump_droid.ui.theme.SciFiWhite
import kotlin.math.sin

@Composable
fun SettingsScreen(
    sharedPrefs: SharedPreferences,
    soundManager: SoundManager? = null,
    hapticManager: HapticManager? = null,
    purchaseManager: PurchaseManager? = null,
    onWipeData: () -> Unit,
    onReturn: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SettingsTransition")
    val pulseAlpha by infiniteTransition.animateFloat(0.5f, 1f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "PulseAlpha")
    val borderPulse by infiniteTransition.animateFloat(0.4f, 1f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "BorderPulse")

    val frameTime = remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(50)
            frameTime.value += 50
        }
    }

    val context = LocalContext.current
    Surface(Modifier.fillMaxSize(), color = SciFiBackground) {
        Box {
            StarfieldBackground(Modifier.fillMaxSize(), starCount = 40, alphaRange = 0.15f..0.55f, starColor = SciFiCyan)
            Canvas(Modifier.fillMaxSize()) {
                val ft = frameTime.value / 1000f
                val w = size.width
                val h = size.height

                drawCircle(SciFiCyan.copy(alpha = 0.03f), radius = 70f, center = Offset(w * 0.8f, h * 0.3f))

                // Pulsing audio wave
                val waveBase = h * 0.48f
                val waveAmp = 4f + sin(ft * 3f) * 2f
                for (i in 0..19) {
                    val barX = w * 0.35f + (i * (w * 0.3f / 19f))
                    val barH = waveAmp * (0.3f + sin(ft * 4f + i * 0.8f) * 0.7f)
                    drawRect(
                        SciFiCyan.copy(alpha = 0.15f + barH / 20f),
                        topLeft = Offset(barX, waveBase - barH),
                        size = androidx.compose.ui.geometry.Size(w * 0.012f, barH * 2f)
                    )
                }
            }

            Column(Modifier.padding(32.dp).safeDrawingPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "SYSTEM SETTINGS",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        shadow = Shadow(SciFiCyan.copy(alpha = 0.4f), blurRadius = 12f)
                    ),
                    color = SciFiCyan,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(32.dp))
                Text("SOUND EFFECTS", color = SciFiWhite.copy(alpha = 0.7f), letterSpacing = 2.sp, fontSize = 10.sp)
                Spacer(Modifier.height(8.dp))
                AudioSlider(
                    value = soundManager?.sfxVolume ?: 0.7f,
                    onValueChange = { soundManager?.sfxVolume = it },
                    accent = SciFiCyan
                )
                Spacer(Modifier.height(16.dp))
                Text("MUSIC", color = SciFiWhite.copy(alpha = 0.7f), letterSpacing = 2.sp, fontSize = 10.sp)
                Spacer(Modifier.height(8.dp))
                AudioSlider(
                    value = soundManager?.musicVolume ?: 0.5f,
                    onValueChange = { soundManager?.musicVolume = it },
                    accent = SciFiGold
                )
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(0.6f), horizontalArrangement = Arrangement.Center) {
                    Button(
                        onClick = { 
                            if (soundManager != null) {
                                soundManager.isMuted = !soundManager.isMuted
                                soundManager.playSfx("sfx_ui_click")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (soundManager?.isMuted == true) SciFiRed.copy(alpha = 0.3f) else SciFiCyan.copy(alpha = 0.2f),
                            contentColor = if (soundManager?.isMuted == true) SciFiRed else SciFiCyan
                        ),
                        modifier = Modifier.height(36.dp).weight(1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(if (soundManager?.isMuted == true) "MUTED" else "MUTE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    val hapticEnabled = sharedPrefs.getBoolean("haptic_enabled", true)
                    Button(
                        onClick = { 
                            val newState = !hapticEnabled
                            sharedPrefs.edit { putBoolean("haptic_enabled", newState) }
                            soundManager?.playSfx("sfx_ui_click")
                            if (newState) {
                                hapticManager?.vibrate(HapticManager.HapticType.SUCCESS)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!hapticEnabled) SciFiRed.copy(alpha = 0.3f) else SciFiCyan.copy(alpha = 0.2f),
                            contentColor = if (!hapticEnabled) SciFiRed else SciFiCyan
                        ),
                        modifier = Modifier.height(36.dp).weight(1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(if (!hapticEnabled) "HAPTIC OFF" else "HAPTIC ON", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(16.dp))
                val isPremium = purchaseManager?.isPremiumUser ?: sharedPrefs.getBoolean("premium_user", false)
                Button(
                    onClick = {
                        soundManager?.playSfx("sfx_ui_click")
                        if (isPremium) {
                            purchaseManager?.setPremiumUser(false)
                        } else {
                            purchaseManager?.launchPurchaseFlow(context as android.app.Activity)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPremium) SciFiGreen.copy(alpha = 0.2f) else SciFiGold.copy(alpha = 0.2f),
                        contentColor = if (isPremium) SciFiGreen else SciFiGold
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isPremium) SciFiGreen.copy(alpha = 0.5f) else SciFiGold.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isPremium) "ADS REMOVED ✓" else "UPGRADE: REMOVE ADS", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        soundManager?.playSfx("sfx_ui_click")
                        sharedPrefs.edit { clear() }
                        onWipeData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SciFiRed.copy(alpha = 0.2f), contentColor = SciFiRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SciFiRed.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("WIPE TELEMETRY DATA", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Spacer(Modifier.height(16.dp))
                GlobalAdBanner()
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        soundManager?.playSfx("sfx_ui_click")
                        onReturn()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SciFiBorder.copy(alpha = borderPulse))
                ) {
                    Text("RETURN", color = SciFiWhite, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Spacer(Modifier.height(8.dp))
                Text("SYSTEM PREFERENCES // AUDIO // DATA", color = SciFiWhite.copy(alpha = 0.2f), letterSpacing = 1.sp, fontSize = 8.sp)
            }
        }
    }
}

@Composable
private fun AudioSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    accent: Color = SciFiCyan
) {
    Row(
        Modifier.fillMaxWidth(0.6f).height(24.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(10) { i ->
            val step = (i + 1) / 10f
            val isActive = step <= value
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (isActive) accent.copy(alpha = 0.6f) else Color(0xFF333333),
                        RoundedCornerShape(3.dp)
                    )
                    .clickable { onValueChange(step) }
            )
        }
    }
}
