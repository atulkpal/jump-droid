package com.example.jump_droid

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jump_droid.ui.theme.SciFiBorder
import com.example.jump_droid.ui.theme.SciFiCyan
import com.example.jump_droid.ui.theme.SciFiGold
import com.example.jump_droid.ui.theme.SciFiSurface
import com.example.jump_droid.ui.theme.SciFiWhite

@Composable
fun CodexCard(title: String, description: String, lore: String, unlocked: Boolean) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(
                width = 1.dp,
                color = if (unlocked) SciFiBorder else SciFiBorder.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            ),
        color = if (unlocked) SciFiSurface else SciFiSurface.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = if (unlocked) SciFiCyan else SciFiWhite.copy(alpha = 0.3f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (unlocked) SciFiWhite.copy(alpha = 0.8f) else SciFiWhite.copy(alpha = 0.2f),
                textAlign = TextAlign.Start,
                lineHeight = 16.sp
            )
            if (unlocked && lore.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = lore,
                    style = MaterialTheme.typography.bodySmall,
                    color = SciFiGold.copy(alpha = 0.6f),
                    fontStyle = FontStyle.Italic,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
