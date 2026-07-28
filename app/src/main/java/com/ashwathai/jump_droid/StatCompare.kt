package com.ashwathai.jump_droid

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.jump_droid.ui.theme.SciFiCyan
import com.ashwathai.jump_droid.ui.theme.SciFiGold
import com.ashwathai.jump_droid.ui.theme.SciFiPurple
import com.ashwathai.jump_droid.ui.theme.SciFiWhite
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private val AXIS_COUNT = 5

data class RocketStats(
    val thrust: Float,
    val fuel: Float,
    val thermal: Float,
    val integrity: Float,
    val maneuverability: Float
)

fun typeColor(type: RocketType): Color {
    return when (type) {
        RocketType.BALANCED -> SciFiWhite
        RocketType.SCOUT -> SciFiGold
        RocketType.TANK -> SciFiCyan
        RocketType.EXPERIMENTAL -> SciFiPurple
    }
}

private fun DrawScope.drawPentagon(
    cx: Float,
    cy: Float,
    radius: Float,
    angles: List<Float>,
    outlineColor: Color,
    fillColor: Color?,
    strokeWidth: Float = 2f
) {
    val path = Path()
    for (i in 0..AXIS_COUNT) {
        val idx = i % AXIS_COUNT
        val x = cx + radius * cos(angles[idx])
        val y = cy + radius * sin(angles[idx])
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    if (fillColor != null) drawPath(path, fillColor)
    drawPath(path, outlineColor, style = Stroke(width = strokeWidth))
}

@Composable
fun PentagonChart(
    stats: RocketStats,
    color: Color,
    modifier: Modifier = Modifier
) {
    val values = listOf(stats.thrust, stats.fuel, stats.thermal, stats.integrity, stats.maneuverability)
    val maxVal = 2.0f // Scale normalization

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = minOf(cx, cy) * 0.75f

        val angles = (0 until AXIS_COUNT).map { i ->
            -PI.toFloat() / 2f + i * (2f * PI.toFloat() / AXIS_COUNT)
        }

        // Concentric grid pentagons
        for (level in 1..4) {
            drawPentagon(
                cx, cy, radius * level / 4f, angles,
                outlineColor = SciFiWhite.copy(alpha = 0.08f),
                fillColor = null,
                strokeWidth = 1f
            )
        }

        // Axis lines
        for (i in 0 until AXIS_COUNT) {
            val ax = cx + radius * cos(angles[i])
            val ay = cy + radius * sin(angles[i])
            drawLine(SciFiWhite.copy(alpha = 0.12f), Offset(cx, cy), Offset(ax, ay), strokeWidth = 1f)
        }

        // Data pentagon
        val dataValues = values.mapIndexed { i, value ->
            val r = radius * (value / maxVal).coerceIn(0.1f, 1f)
            Offset(cx + r * cos(angles[i]), cy + r * sin(angles[i]))
        }

        // Draw data polygon manually for fill
        val dataPath = Path()
        dataValues.forEachIndexed { i, pt ->
            if (i == 0) dataPath.moveTo(pt.x, pt.y) else dataPath.lineTo(pt.x, pt.y)
        }
        dataPath.close()
        drawPath(dataPath, color.copy(alpha = 0.25f))
        drawPath(dataPath, color, style = Stroke(width = 2.5f))

        // Data points
        dataValues.forEach { pt ->
            drawCircle(color, radius = 4f, center = pt)
            drawCircle(SciFiWhite.copy(alpha = 0.5f), radius = 1.5f, center = pt)
        }
    }
}

@Composable
fun StatLegend(
    rocketType: RocketType,
    allTypes: List<RocketType> = RocketType.entries,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        allTypes.forEach { type ->
            val isActive = type == rocketType
            val legendColor = typeColor(type)

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Canvas(Modifier.size(8.dp)) {
                    drawCircle(legendColor)
                }
                Spacer(Modifier.size(6.dp))
                Text(
                    text = type.title.uppercase(),
                    color = if (isActive) SciFiCyan else SciFiWhite.copy(alpha = 0.5f),
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 9.sp
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${(type.thrustMult * 100).toInt()}% / ${(type.fuelMult * 100).toInt()}% / ${(type.heatMult * 100).toInt()}%",
                    color = SciFiWhite.copy(alpha = 0.25f),
                    fontSize = 7.sp
                )
            }
        }
    }
}
