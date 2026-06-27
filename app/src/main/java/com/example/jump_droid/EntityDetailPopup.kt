package com.example.jump_droid

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jump_droid.ui.theme.*
import kotlin.random.Random

@Composable
fun EntityDetailPopup(
    detail: EntityDetail,
    unlocked: Boolean,
    onDismiss: () -> Unit
) {
    var glitchSeed by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(300L)
            glitchSeed += 1L
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .clickable(enabled = false, onClick = {})
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (unlocked) detail.discoveryType.title.uppercase() else "SIGNAL LOST",
                    color = if (unlocked) SciFiWhite else SciFiRed.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Box(
                    modifier = Modifier
                        .clickable(
                            onClick = onDismiss,
                            role = Role.Button
                        )
                        .background(SciFiSurface.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .border(BorderStroke(1.dp, SciFiBorder.copy(alpha = 0.5f)), RoundedCornerShape(4.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "CLOSE",
                        color = SciFiCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // Preview Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(SciFiSurface.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .border(BorderStroke(1.dp, SciFiBorder.copy(alpha = 0.1f)), RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EntityPreview(
                        detail = detail,
                        unlocked = unlocked,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Type Badge
                val typeLabel = when {
                    detail.threatId != null -> "THREAT"
                    detail.platformType != null -> "PLATFORM"
                    detail.powerUpType != null -> "POWER-UP"
                    detail.discoveryType.name.startsWith("AREA_") -> "ZONE"
                    detail.discoveryType.name.startsWith("ROCKET_") -> "ROCKET"
                    detail.discoveryType.name.startsWith("LORE_") || detail.discoveryType.name.startsWith("DISCOVERY_") -> "LORE"
                    detail.discoveryType.name.startsWith("ART_") -> "ARTIFACT"
                    detail.discoveryType.name.startsWith("HEAT_") || detail.discoveryType.name.startsWith("OVERHEAT_") || detail.discoveryType.name.startsWith("EFFICIENCY_") -> "MECHANIC"
                    else -> "ENTITY"
                }
                
                Surface(
                    color = SciFiCyan.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, SciFiCyan.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = typeLabel,
                        color = SciFiCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Data Sections
                SectionBlock(
                    label = "STATUS",
                    text = if (unlocked) detail.status else "█ ENCRYPTED SIGNAL",
                    glitched = !unlocked,
                    glitchSeed = glitchSeed,
                    accent = if (unlocked) SciFiGreen else SciFiRed
                )
                
                Spacer(Modifier.height(20.dp))

                SectionBlock(
                    label = "CLASSIFICATION",
                    text = if (unlocked) detail.classification else "█ NO CARRIER",
                    glitched = !unlocked,
                    glitchSeed = glitchSeed,
                    accent = SciFiCyan
                )

                Spacer(Modifier.height(20.dp))

                SectionBlock(
                    label = "ARCHIVE RECORD",
                    text = if (unlocked) detail.archiveRecord else "█ SIGNAL LOST",
                    glitched = !unlocked,
                    glitchSeed = glitchSeed,
                    accent = SciFiWhite
                )

                Spacer(Modifier.height(20.dp))

                SectionBlock(
                    label = "TACTICAL NOTE",
                    text = if (unlocked) detail.tacticalNote else "█ UNAVAILABLE",
                    glitched = !unlocked,
                    glitchSeed = glitchSeed,
                    accent = SciFiGold
                )
                
                Spacer(Modifier.height(32.dp))
            }

            GlobalAdBanner()
        }
    }
}

@Composable
private fun SectionBlock(
    label: String,
    text: String,
    glitched: Boolean,
    glitchSeed: Long,
    accent: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(4.dp, 10.dp).background(accent.copy(alpha = 0.6f)))
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                color = accent.copy(alpha = 0.8f),
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
        
        Spacer(Modifier.height(8.dp))
        
        val displayText = if (glitched) {
            val seed = glitchSeed
            buildString {
                text.forEachIndexed { index, char ->
                    val r = Random(seed * 31 + index)
                    when (r.nextInt(5)) {
                        0 -> append('█')
                        1 -> append('░')
                        2 -> append('▒')
                        3 -> append('▓')
                        else -> append(char)
                    }
                }
            }
        } else {
            text
        }
        
        Text(
            text = displayText,
            color = if (glitched) SciFiRed.copy(alpha = 0.5f) else SciFiWhite.copy(alpha = 0.8f),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 18.sp,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
