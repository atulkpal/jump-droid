package com.example.jump_droid

import android.app.Activity
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jump_droid.ui.theme.SciFiButtonShape
import com.example.jump_droid.ui.theme.SciFiBorder
import com.example.jump_droid.ui.theme.SciFiCyan
import com.example.jump_droid.ui.theme.SciFiGold
import com.example.jump_droid.ui.theme.SciFiPurple
import com.example.jump_droid.ui.theme.SciFiRed
import com.example.jump_droid.ui.theme.SciFiSurface
import com.example.jump_droid.ui.theme.SciFiWhite
import kotlin.math.PI
@Composable
fun MainMenuScreen(
    onLaunch: () -> Unit,
    onNavigate: (GameState) -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "MenuTransition")
    val scanAngle by infiniteTransition.animateFloat(0f, 360f, infiniteRepeatable(tween(4000, easing = androidx.compose.animation.core.LinearEasing), RepeatMode.Restart), label = "ScanAngle")
    val borderPulse by infiniteTransition.animateFloat(0.4f, 1f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "BorderPulse")
    val titleGlow by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "TitleGlow")

    Box(Modifier.fillMaxSize().background(Color(0xFF0a0a1a)), contentAlignment = Alignment.Center) {
        StarfieldBackground(Modifier.fillMaxSize(), starCount = 60, alphaRange = 0.2f..0.7f)
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawCircle(SciFiCyan.copy(alpha = 0.04f), radius = 60f, center = Offset(w * 0.15f, h * 0.12f))
            drawCircle(SciFiPurple.copy(alpha = 0.03f), radius = 80f, center = Offset(w * 0.85f, h * 0.88f))
            drawCircle(SciFiGold.copy(alpha = 0.02f), radius = 50f, center = Offset(w * 0.5f, h * 0.5f))

            val cx = w / 2
            val cy = h * 0.28f
            val scanRad = 100f
            val sa = scanAngle * (PI.toFloat() / 180f)
            drawArc(
                brush = Brush.sweepGradient(listOf(SciFiCyan.copy(alpha = 0.12f), Color.Transparent)),
                startAngle = scanAngle - 30f,
                sweepAngle = 60f,
                useCenter = true,
                topLeft = Offset(cx - scanRad, cy - scanRad),
                size = androidx.compose.ui.geometry.Size(scanRad * 2, scanRad * 2)
            )
            drawCircle(SciFiCyan.copy(alpha = 0.06f), radius = scanRad, center = Offset(cx, cy), style = Stroke(width = 1f))
            drawCircle(SciFiCyan.copy(alpha = 0.04f), radius = scanRad * 0.7f, center = Offset(cx, cy), style = Stroke(width = 0.5f))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.safeDrawingPadding().width(280.dp)
        ) {
            Spacer(Modifier.height(40.dp))

            Text(
                text = "COMMAND CENTER",
                style = MaterialTheme.typography.headlineMedium.copy(
                    shadow = Shadow(SciFiCyan.copy(alpha = titleGlow * 0.5f), blurRadius = 18f)
                ),
                color = SciFiWhite,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "SELECT MISSION PARAMETERS",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 3.sp),
                color = SciFiCyan.copy(alpha = 0.4f)
            )
            Spacer(Modifier.height(48.dp))

            val menuButtons = listOf(
                "LAUNCH" to { onLaunch() },
                "HANGAR" to { onNavigate(GameState.HANGAR) },
                "MISSIONS" to { onNavigate(GameState.MISSIONS) },
                "ARCHIVE" to { onNavigate(GameState.ARCHIVE) },
                "TERMINAL" to { onNavigate(GameState.LEADERBOARD) },
                "PROTOCOL" to { onNavigate(GameState.ABOUT) },
                "SETTINGS" to { onNavigate(GameState.SETTINGS) }
            )

            menuButtons.forEachIndexed { index, (label, action) ->
                val accentColor = when (index) {
                    0 -> SciFiCyan
                    1 -> SciFiGold
                    2 -> SciFiCyan
                    3 -> SciFiPurple
                    4 -> SciFiGold
                    else -> SciFiCyan
                }
                Button(
                    onClick = action,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = SciFiButtonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SciFiSurface,
                        contentColor = SciFiWhite
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = borderPulse))
                ) {
                    Text(label, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, fontSize = 12.sp)
                }
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { (context as? Activity)?.finish() },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = SciFiButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SciFiRed.copy(alpha = 0.15f),
                    contentColor = SciFiRed
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, SciFiRed.copy(alpha = 0.4f))
            ) {
                Text("ABORT", fontWeight = FontWeight.Bold, letterSpacing = 2.sp, fontSize = 12.sp)
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "ALL SYSTEMS NOMINAL",
                color = SciFiCyan.copy(alpha = 0.3f),
                fontSize = 9.sp,
                letterSpacing = 2.sp
            )
        }
    }
}
