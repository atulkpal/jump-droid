package com.ashwathai.jump_droid

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.jump_droid.ui.theme.SciFiBorder
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import com.ashwathai.jump_droid.ui.theme.SciFiGold
import com.ashwathai.jump_droid.ui.theme.SciFiPurple
import com.ashwathai.jump_droid.ui.theme.SciFiSurface
import com.ashwathai.jump_droid.ui.theme.SciFiWhite

@Composable
fun TutorialOverlay(
    activeDiscovery: DiscoveryType?,
    onAcknowledge: () -> Unit
) {
    if (activeDiscovery == null) return

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).clickable {}, contentAlignment = Alignment.Center) {
        if (activeDiscovery.category == "ARTIFACTS") {
            val infiniteTransition = rememberInfiniteTransition(label = "ArtifactGlow")
            val glowScale by infiniteTransition.animateFloat(1f, 1.5f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "GlowScale")
            Box(Modifier.size(300.dp).graphicsLayer(scaleX = glowScale, scaleY = glowScale).background(SciFiPurple.copy(alpha = 0.1f), CircleShape))
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SciFiSurface,
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 400.dp)
                .safeDrawingPadding()
                .shadow(20.dp, RoundedCornerShape(20.dp), spotColor = if (activeDiscovery.category == "ARTIFACTS") SciFiPurple else SciFiCyan)
                .border(1.dp, if (activeDiscovery.category == "ARTIFACTS") SciFiPurple.copy(alpha = 0.5f) else SciFiBorder, RoundedCornerShape(20.dp))
        ) {
            Column(
                Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val isLore = activeDiscovery.category == "LORE" || activeDiscovery.category == "ARTIFACTS"
                val isArtifact = activeDiscovery.category == "ARTIFACTS"
                Text(
                    text = if (isArtifact) "ARTIFACT RECOVERED" else if (isLore) "INTEL RECOVERED" else "NEW DISCOVERY",
                    color = if (isArtifact) SciFiPurple else if (isLore) SciFiPurple else SciFiCyan,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "DATABASE UPDATED",
                    color = SciFiWhite.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    text = activeDiscovery.title.uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = SciFiWhite,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = activeDiscovery.description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = SciFiWhite.copy(alpha = 0.9f),
                    lineHeight = 22.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )

                if (isArtifact) {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "PERMANENT PROGRESS RECORDED",
                        color = SciFiGold,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onAcknowledge,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isArtifact) SciFiPurple else SciFiCyan)
                ) {
                    Text("ACKNOWLEDGE", fontWeight = FontWeight.Bold, color = if (isArtifact) Color.White else Color.Black, letterSpacing = 1.sp)
                }
                Spacer(Modifier.height(8.dp))
                GlobalAdBanner()
            }
        }
    }
}
