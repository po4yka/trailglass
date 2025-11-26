package com.po4yka.trailglass.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.po4yka.trailglass.ui.theme.extended
import kotlinx.datetime.DayOfWeek

/** Heatmap showing activity intensity by hour and day of week. */
@Composable
fun ActivityHeatmap(
    data: Map<DayOfWeek, Map<Int, Int>>, // day -> hour -> activity count
    modifier: Modifier = Modifier
) {
    val maxActivity = data.values.flatMap { it.values }.maxOrNull() ?: 1
    val gradientColors = MaterialTheme.colorScheme.extended.gradientColors
    val lowColor = gradientColors.first()
    val highColor = gradientColors.last()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Activity Heatmap",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            // Fixed Day Labels Column
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(top = 20.dp) // Offset for header row
            ) {
                DayOfWeek.entries.forEach { day ->
                    Box(
                        modifier = Modifier.height(20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = day.name.take(3),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            modifier = Modifier.width(36.dp)
                        )
                    }
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState)
            ) {
                // Hour labels (top)
                Row(
                    modifier = Modifier.padding(bottom = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    (0..23).forEach { hour ->
                        val label = when (hour) {
                            0 -> "12am"
                            6 -> "6am"
                            12 -> "12pm"
                            18 -> "6pm"
                            else -> ""
                        }
                        Box(
                            modifier = Modifier.width(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (label.isNotEmpty()) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Heatmap grid
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    DayOfWeek.entries.forEach { day ->
                        val hourData = data[day] ?: emptyMap()
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            (0..23).forEach { hour ->
                                val activity = hourData[hour] ?: 0
                                val intensity = if (maxActivity > 0) activity.toFloat() / maxActivity else 0f

                                Canvas(
                                    modifier =
                                        Modifier
                                            .width(32.dp)
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
                modifier =
                    Modifier
                        .weight(1f)
                        .height(12.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                (0..4).forEach { index ->
                    val intensity = index / 4f
                    Canvas(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
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

            Text(
                "More",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp
            )
        }
    }
}

/** Blend two colors based on intensity. */
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
