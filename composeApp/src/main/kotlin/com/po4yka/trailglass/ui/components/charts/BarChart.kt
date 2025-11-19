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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Simple bar chart component for statistics visualization.
 */
@Composable
fun BarChart(
    data: List<BarData>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    showValues: Boolean = true,
    maxBarHeight: Float = 200f
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOf { it.value }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Bars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { item ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Value text
                    if (showValues) {
                        Text(
                            text = item.formattedValue ?: item.value.toInt().toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    // Bar
                    Canvas(
                        modifier = Modifier
                            .width(32.dp)
                            .height((maxBarHeight * (item.value / maxValue)).dp)
                    ) {
                        drawRect(
                            color = item.color ?: barColor,
                            topLeft = Offset.Zero,
                            size = Size(size.width, size.height)
                        )
                    }

                    // Label
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

data class BarData(
    val label: String,
    val value: Float,
    val formattedValue: String? = null,
    val color: Color? = null
)
