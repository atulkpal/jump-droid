package com.example.jump_droid

import android.content.SharedPreferences
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.example.jump_droid.ui.theme.SciFiBackground
import com.example.jump_droid.ui.theme.SciFiBorder
import com.example.jump_droid.ui.theme.SciFiCyan
import com.example.jump_droid.ui.theme.SciFiGold
import com.example.jump_droid.ui.theme.SciFiRed
import com.example.jump_droid.ui.theme.SciFiSurface
import com.example.jump_droid.ui.theme.SciFiWhite
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun SettingsScreen(
    sharedPrefs: SharedPreferences,
    onWipeData: () -> Unit,
    onReturn: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SettingsTransition")
    val pulseAlpha by infiniteTransition.animateFloat(0.5f, 1f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "PulseAlpha")

    Surface(Modifier.fillMaxSize(), color = SciFiBackground) {
        Box {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                repeat(20) {
                    val x = Random.nextFloat() * w
                    val y = Random.nextFloat() * h
                    drawCircle(SciFiCyan.copy(alpha = 0.04f), radius = 0.5f + Random.nextFloat(), center = Offset(x, y))
                }
                drawCircle(SciFiCyan.copy(alpha = 0.03f), radius = 70f, center = Offset(w * 0.8f, h * 0.3f))
            }

            Column(Modifier.padding(32.dp).safeDrawingPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("SYSTEM SETTINGS", style = MaterialTheme.typography.headlineLarge, color = SciFiCyan, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Spacer(Modifier.height(48.dp))
                Text("MASTER AUDIO", color = SciFiWhite.copy(alpha = 0.7f), letterSpacing = 1.sp)
                Spacer(Modifier.height(16.dp))
                Box(Modifier.width(200.dp).height(4.dp).background(SciFiSurface, CircleShape)) {
                    Box(Modifier.fillMaxWidth(0.8f).fillMaxHeight().background(SciFiCyan.copy(alpha = pulseAlpha), CircleShape))
                }
                Spacer(Modifier.height(64.dp))
                Button(
                    onClick = {
                        sharedPrefs.edit { clear() }
                        onWipeData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SciFiRed.copy(alpha = 0.2f), contentColor = SciFiRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SciFiRed.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("WIPE TELEMETRY DATA", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onReturn,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SciFiBorder)
                ) {
                    Text("RETURN", color = SciFiWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
