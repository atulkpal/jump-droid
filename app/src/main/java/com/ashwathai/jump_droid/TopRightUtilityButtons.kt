package com.ashwathai.jump_droid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import com.ashwathai.jump_droid.ui.theme.SciFiSurface

@Composable
fun TopRightUtilityButtons(
    modifier: Modifier = Modifier,
    gameState: GameState,
    onPause: () -> Unit
) {
    Row(
        modifier = modifier
            .padding(16.dp)
            .statusBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (gameState == GameState.PLAYING || gameState == GameState.ZEN) {
            Button(
                onClick = onPause,
                modifier = Modifier.size(36.dp),
                contentPadding = PaddingValues(0.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = SciFiSurface.copy(alpha = 0.5f))
            ) { Text("||", fontWeight = FontWeight.Black, fontSize = 14.sp, color = SciFiCyan) }
        }
    }
}
