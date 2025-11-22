package com.po4yka.trailglass.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.min

/**
 * Simple pie chart component for percentage visualization.
 */
@Composable
fun PieChart(
    data: List<PieData>,
    modifier: Modifier = Modifier,
    showLegend: Boolean = true
) {
    if (data.isEmpty()) return

    val total = data.sumOf { it.value.toDouble() }.toFloat()
    if (total == 0f) return

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pie chart
        Canvas(
            modifier =
                Modifier
                    .size(200.dp)
                    .align(Alignment.CenterHorizontally)
        ) {
            val canvasSize = min(size.width, size.height)
            val radius = canvasSize / 2f
            val strokeWidth = radius * 0.3f
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            var startAngle = -90f // Start from top

            data.forEach { item ->
                val sweepAngle = (item.value / total) * 360f

                // Draw arc
                drawArc(
                    color = item.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft =
                        Offset(
                            centerX - radius + strokeWidth / 2,
                            centerY - radius + strokeWidth / 2
                        ),
                    size =
                        Size(
                            (radius - strokeWidth / 2) * 2,
                            (radius - strokeWidth / 2) * 2
                        ),
                    style = Stroke(width = strokeWidth)
                )

                startAngle += sweepAngle
            }
        }

        // Legend
        if (showLegend) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                data.forEach { item ->
                    val percentage = (item.value / total) * 100
                    LegendItem(
                        color = item.color,
                        label = item.label,
                        value = "${percentage.toInt()}%"
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    value: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color indicator
        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(color = color)
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}

data class PieData(
    val label: String,
    val value: Float,
    val color: Color
)
