package com.ashwathai.jump_droid

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.jump_droid.ui.theme.SciFiGold
import com.ashwathai.jump_droid.ui.theme.SciFiPurple
import com.ashwathai.jump_droid.ui.theme.SciFiSurface
import com.ashwathai.jump_droid.ui.theme.SciFiWhite

@Composable
fun ArtifactLoreOverlay(
    type: DiscoveryType,
    timer: Float,
    totalDuration: Float
) {
    val fadeAlpha by animateFloatAsState(
        targetValue = if (timer < 0.5f) 0f else 1f,
        animationSpec = tween(300),
        label = "LoreFade"
    )

    Box(
        Modifier.fillMaxWidth().padding(top = 120.dp).alpha(fadeAlpha),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            Modifier
                .widthIn(max = 360.dp)
                .background(SciFiSurface.copy(alpha = 0.95f), RoundedCornerShape(12.dp))
                .border(1.dp, SciFiPurple.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "ARTIFACT RECOVERED",
                    color = SciFiPurple,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    type.title.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = SciFiWhite,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    "\"${type.lore}\"",
                    color = SciFiWhite.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    "RECORDED TO ARCHIVE",
                    color = SciFiGold.copy(alpha = 0.6f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
