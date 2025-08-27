package com.example.homemedicinechest.features.stats


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.homemedicinechest.data.db.DailyRow
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DailyBarChart(
    daily: List<DailyRow>,
    modifier: Modifier = Modifier,
    showTitle: Boolean = false,
    barColorTaken: Color = MaterialTheme.colorScheme.primary,
    barColorSkipped: Color = MaterialTheme.colorScheme.error,
    labelColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
    height: Int = 180
) {
    if (daily.isEmpty()) {
        Text("Нет данных за выбранный период", style = MaterialTheme.typography.bodyMedium)
        return
    }

    val df = SimpleDateFormat("dd.MM", Locale.getDefault())
    val labels = daily.map { d -> df.format(Date(d.day * 86_400_000L)) }
    val valuesTaken = daily.map { it.taken }
    val valuesSkipped = daily.map { it.skipped }
    val maxVal = (valuesTaken zip valuesSkipped).maxOf { it.first + it.second }.coerceAtLeast(1)

    Column(modifier = modifier) {
        if (showTitle) {
            Text("По дням", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
                .padding(bottom = 6.dp) // место под подписи оси X
        ) {
            val n = daily.size
            val leftPad = 8.dp.toPx()
            val rightPad = 8.dp.toPx()
            val bottomPad = 20.dp.toPx() // это «пустое место» под подписями — всё ок
            val topPad = 8.dp.toPx()

            val chartWidth = size.width - leftPad - rightPad
            val chartHeight = size.height - topPad - bottomPad

            val barSpace = chartWidth / n
            val barWidth = barSpace * 0.5f
            val halfBar = barWidth / 2f
            val yBase = size.height - bottomPad

            repeat(n) { i ->
                val xCenter = leftPad + barSpace * i + barSpace / 2f
                val skippedH = (valuesSkipped[i].toFloat() / maxVal) * chartHeight
                val takenH = (valuesTaken[i].toFloat() / maxVal) * chartHeight

                val x0 = xCenter - halfBar
                val x1 = xCenter + halfBar

                val ySkippedTop = yBase - skippedH
                drawRect(
                    color = barColorSkipped,
                    topLeft = Offset(x0, ySkippedTop),
                    size = androidx.compose.ui.geometry.Size(x1 - x0, skippedH)
                )
                val yTakenTop = ySkippedTop - takenH
                drawRect(
                    color = barColorTaken,
                    topLeft = Offset(x0, yTakenTop),
                    size = androidx.compose.ui.geometry.Size(x1 - x0, takenH)
                )
            }

            // подписи X, показываем реже чтобы не слиплось
            val every = when {
                n > 24 -> 6
                n > 14 -> 4
                n > 8  -> 2
                else -> 1
            }
            for (i in 0 until n step every) {
                val xCenter = leftPad + barSpace * i + barSpace / 2f
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = labelColor.toArgb()
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 10.dp.toPx()
                        isAntiAlias = true
                    }
                    drawText(labels[i], xCenter, size.height - 4.dp.toPx(), paint)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendItem(color = barColorTaken, title = "Принято")
            LegendItem(color = barColorSkipped, title = "Пропущено")
        }
    }
}

@Composable
private fun LegendItem(color: Color, title: String) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(modifier = Modifier.size(10.dp)) { drawRect(color = color) }
        Text(title, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun AdherenceLineChart(
    daily: List<DailyRow>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    height: Int = 140
) {
    if (daily.isEmpty()) return

    val points = daily.map { row ->
        val total = (row.taken + row.skipped).coerceAtLeast(1)
        row.taken.toFloat() / total
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .padding(vertical = 6.dp)
    ) {
        val leftPad = 8.dp.toPx()
        val rightPad = 8.dp.toPx()
        val topPad = 8.dp.toPx()
        val bottomPad = 8.dp.toPx()

        val w = size.width - leftPad - rightPad
        val h = size.height - topPad - bottomPad

        val n = points.size
        if (n < 2) return@Canvas

        val path = Path()
        for (i in 0 until n) {
            val x = leftPad + (i / (n - 1f)) * w
            val y = topPad + (1f - points[i]) * h
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = lineColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
        )
        // точки
        for (i in 0 until n) {
            val x = leftPad + (i / (n - 1f)) * w
            val y = topPad + (1f - points[i]) * h
            drawCircle(color = lineColor, radius = 3.5f, center = Offset(x, y))
        }
    }
}