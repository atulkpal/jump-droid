package com.example.jump_droid

import android.app.Activity
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jump_droid.ui.theme.SciFiBorder
import com.example.jump_droid.ui.theme.SciFiCyan
import com.example.jump_droid.ui.theme.SciFiRed
import com.example.jump_droid.ui.theme.SciFiSurface
import com.example.jump_droid.ui.theme.SciFiWhite

@Composable
fun MainMenuScreen(
    onLaunch: () -> Unit,
    onNavigate: (GameState) -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.safeDrawingPadding().width(280.dp)
        ) {
            Text(
                text = "COMMAND CENTER",
                style = MaterialTheme.typography.headlineLarge,
                color = SciFiWhite,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )
            Spacer(Modifier.height(48.dp))

            val menuButtons = listOf(
                "LAUNCH" to { onLaunch() },
                "HANGAR" to { onNavigate(GameState.HANGAR) },
                "ARCHIVE" to { onNavigate(GameState.ARCHIVE) },
                "TERMINAL" to { onNavigate(GameState.LEADERBOARD) },
                "MISSION DATA" to { onNavigate(GameState.ABOUT) },
                "SETTINGS" to { onNavigate(GameState.SETTINGS) }
            )

            menuButtons.forEach { (label, action) ->
                Button(
                    onClick = action,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SciFiSurface,
                        contentColor = SciFiWhite
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SciFiBorder)
                ) {
                    Text(label, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { (context as? Activity)?.finish() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SciFiRed.copy(alpha = 0.2f),
                    contentColor = SciFiRed
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, SciFiRed.copy(alpha = 0.5f))
            ) {
                Text("ABORT MISSION", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
        }
    }
}
