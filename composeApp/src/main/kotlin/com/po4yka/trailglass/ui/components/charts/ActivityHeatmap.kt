package com.po4yka.trailglass.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.po4yka.trailglass.ui.theme.extended
import kotlinx.datetime.DayOfWeek

/**
 * Heatmap showing activity intensity by hour and day of week.
 */
@Composable
fun ActivityHeatmap(
    data: Map<DayOfWeek, Map<Int, Int>>, // day -> hour -> activity count
    modifier: Modifier = Modifier
) {
    val maxActivity = data.values.flatMap { it.values }.maxOrNull() ?: 1
    val gradientColors = MaterialTheme.colorScheme.extended.gradientColors
    val lowColor = gradientColors.first()
    val highColor = gradientColors.last()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Activity Heatmap",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Hour labels (top)
        Row(
            modifier = Modifier.padding(start = 40.dp)
        ) {
            listOf("6am", "12pm", "6pm", "12am").forEachIndexed { index, label ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = if (index == 0) Alignment.CenterStart else Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp
                    )
                }
            }
        }

        // Heatmap grid
        DayOfWeek.entries.forEach { day ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Day label
                Text(
                    text = day.name.take(3),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    modifier = Modifier.width(36.dp)
                )

                // Hour cells
                val hourData = data[day] ?: emptyMap()
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    (0..23).forEach { hour ->
                        val activity = hourData[hour] ?: 0
                        val intensity = if (maxActivity > 0) activity.toFloat() / maxActivity else 0f

                        Canvas(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .height(20.dp)
                        ) {
                            val color = blendColors(lowColor, highColor, intensity)
                            drawRoundRect(
                                color = color,
                                topLeft = Offset.Zero,
                                size = Size(size.width, size.height),
                                cornerRadius = CornerRadius(2.dp.toPx())
                            )
                        }
                    }
                }
            }
        }

        // Legend
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Less",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp
            )

            Row(
                modifier = Modifier.weight(1f).height(12.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                (0..4).forEach { index ->
                    val intensity = index / 4f
                    Canvas(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        val color = blendColors(lowColor, highColor, intensity)
                        drawRoundRect(
                            color = color,
                            topLeft = Offset.Zero,
                            size = Size(size.width, size.height),
                            cornerRadius = CornerRadius(2.dp.toPx())
                        )
                    }
                }
            }

            Text(
                "More",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp
            )
        }
    }
}

/**
 * Blend two colors based on intensity.
 */
private fun blendColors(
    low: Color,
    high: Color,
    intensity: Float
): Color {
    val clampedIntensity = intensity.coerceIn(0f, 1f)
    return Color(
        red = low.red + (high.red - low.red) * clampedIntensity,
        green = low.green + (high.green - low.green) * clampedIntensity,
        blue = low.blue + (high.blue - low.blue) * clampedIntensity,
        alpha = 1f
    )
}
