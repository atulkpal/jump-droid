package com.ashwathai.jump_droid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TopRightUtilityButtons(
    modifier: Modifier = Modifier,
    gameState: GameState,
    onHelp: () -> Unit,
    onPause: () -> Unit
) {
    Row(
        modifier = modifier
            .padding(16.dp)
            .statusBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onHelp,
            modifier = Modifier.size(36.dp),
            contentPadding = PaddingValues(0.dp),
            shape = CircleShape
        ) { Text("?", fontWeight = FontWeight.Bold) }

        if (gameState == GameState.PLAYING) {
            Button(
                onClick = onPause,
                modifier = Modifier.size(36.dp),
                contentPadding = PaddingValues(0.dp),
                shape = CircleShape
            ) { Text("||", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
        }
    }
}
