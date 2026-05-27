package com.deviceinsight.pro.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.deviceinsight.pro.presentation.theme.ChartPalette

data class PieSlice(val label: String, val value: Float, val color: Color)

/** Donut chart with a side legend. */
@Composable
fun DonutChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(0.0001f)
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(140.dp)) {
            val stroke = 28.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)
            var startAngle = -90f
            slices.forEach { slice ->
                val sweep = 360f * (slice.value / total)
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Butt)
                )
                startAngle += sweep
            }
        }
        Column(
            Modifier.padding(start = 16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            slices.take(6).forEach { slice ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(10.dp).clip(RoundedCornerShape(2.dp))
                    ) {
                        Canvas(Modifier.size(10.dp)) { drawRect(slice.color) }
                    }
                    Text(
                        text = slice.label,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }
    }
}

/** Simple vertical bar chart. [values] are scaled to the maximum value. */
@Composable
fun BarChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    labels: List<String> = emptyList()
) {
    val max = (values.maxOrNull() ?: 0f).coerceAtLeast(0.0001f)
    Column(modifier) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            if (values.isEmpty()) return@Canvas
            val gap = 4.dp.toPx()
            val barWidth = (size.width - gap * (values.size - 1)) / values.size
            values.forEachIndexed { index, v ->
                val barHeight = size.height * (v / max)
                val left = index * (barWidth + gap)
                drawRect(
                    color = barColor,
                    topLeft = Offset(left, size.height - barHeight),
                    size = Size(barWidth, barHeight)
                )
            }
        }
        if (labels.isNotEmpty()) {
            Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                labels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/** Builds a palette-colored pie series from label/value pairs. */
fun buildSlices(items: List<Pair<String, Float>>): List<PieSlice> =
    items.mapIndexed { i, (label, value) ->
        PieSlice(label, value, ChartPalette[i % ChartPalette.size])
    }
