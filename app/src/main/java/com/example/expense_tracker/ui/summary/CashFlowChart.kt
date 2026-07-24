package com.example.expense_tracker.ui.summary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatAxisValue(value: Long): String {
    return when {
        value >= 1_000_000 -> "${value / 1_000_000}jt"
        value >= 1_000 -> "${value / 1_000}k"
        else -> value.toString()
    }
}

@Composable
fun CashFlowChart(
    dailyCashFlow: List<DailyCashFlow>,
    modifier: Modifier = Modifier
) {
    if (dailyCashFlow.isEmpty()) {
        Box(modifier = modifier.height(180.dp))
        return
    }

    val incomeColor = Color(0xFF10B981)
    val expenseColor = Color(0xFF94A3B8) // Lighter slate for better visibility
    val gridColor = Color(0xFFE2E8F0)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    val density = LocalDensity.current
    val labelSizeSp = 10.sp
    val labelSizePx = with(density) { labelSizeSp.toPx() }

    // Pre-compute date labels
    val dateFormat = remember { SimpleDateFormat("d MMM", Locale("id", "ID")) }
    val dateLabels = remember(dailyCashFlow) {
        if (dailyCashFlow.size <= 1) {
            dailyCashFlow.mapIndexed { index, data ->
                Pair(index, dateFormat.format(Date(data.dateMillis)))
            }
        } else {
            // Pick 3-4 evenly spaced labels
            val count = minOf(4, dailyCashFlow.size)
            val indices = (0 until count).map { i ->
                if (count <= 1) 0
                else i * (dailyCashFlow.size - 1) / (count - 1)
            }
            indices.map { idx ->
                Pair(idx, dateFormat.format(Date(dailyCashFlow[idx].dateMillis)))
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(top = 12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            val totalWidth = size.width
            val totalHeight = size.height

            // Margins for axis labels
            val leftMargin = 40.dp.toPx()
            val bottomMargin = 24.dp.toPx()
            val topPadding = 8.dp.toPx()

            val chartWidth = totalWidth - leftMargin
            val chartHeight = totalHeight - bottomMargin - topPadding

            val maxAmount = dailyCashFlow.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(1L) ?: 1L

            // Calculate nice Y axis steps (3 levels: 0, mid, max)
            val ySteps = 3
            val stepValue = maxAmount.toFloat() / (ySteps - 1)

            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#94A3B8")
                textSize = labelSizePx
                isAntiAlias = true
            }

            // ── Draw Y Axis Labels & Gridlines ──
            for (i in 0 until ySteps) {
                val value = (stepValue * i).toLong()
                val y = topPadding + chartHeight - (chartHeight * i.toFloat() / (ySteps - 1))

                // Gridline
                drawLine(
                    color = gridColor,
                    start = Offset(leftMargin, y),
                    end = Offset(totalWidth, y),
                    strokeWidth = 1.dp.toPx()
                )

                // Label
                val label = formatAxisValue(value)
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    0f,
                    y + labelSizePx / 3,
                    textPaint
                )
            }

            // ── Draw X Axis Labels ──
            val stepX = if (dailyCashFlow.size > 1) chartWidth / (dailyCashFlow.size - 1) else chartWidth

            @Suppress("UNCHECKED_CAST")
            val labelPairs = dateLabels as? List<Pair<Int, String>>
            if (labelPairs != null) {
                val xTextPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#94A3B8")
                    textSize = labelSizePx
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                labelPairs.forEach { (idx, label) ->
                    val x = leftMargin + idx * stepX
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        x,
                        totalHeight - 2.dp.toPx(),
                        xTextPaint
                    )
                }
            }

            // ── Draw Chart Lines ──
            val incomePath = Path()
            val expensePath = Path()
            val incomeAreaPath = Path()
            val expenseAreaPath = Path()

            val chartBottom = topPadding + chartHeight

            incomeAreaPath.moveTo(leftMargin, chartBottom)
            expenseAreaPath.moveTo(leftMargin, chartBottom)

            dailyCashFlow.forEachIndexed { index, data ->
                val x = leftMargin + index * stepX
                val incomeY = topPadding + chartHeight - (data.income.toFloat() / maxAmount) * chartHeight
                val expenseY = topPadding + chartHeight - (data.expense.toFloat() / maxAmount) * chartHeight

                if (index == 0) {
                    incomePath.moveTo(x, incomeY)
                    expensePath.moveTo(x, expenseY)
                    incomeAreaPath.lineTo(x, incomeY)
                    expenseAreaPath.lineTo(x, expenseY)
                } else {
                    val prevX = leftMargin + (index - 1) * stepX
                    val prevIncomeY = topPadding + chartHeight - (dailyCashFlow[index - 1].income.toFloat() / maxAmount) * chartHeight
                    val prevExpenseY = topPadding + chartHeight - (dailyCashFlow[index - 1].expense.toFloat() / maxAmount) * chartHeight

                    val cp1x = prevX + (x - prevX) / 2

                    incomePath.cubicTo(cp1x, prevIncomeY, cp1x, incomeY, x, incomeY)
                    expensePath.cubicTo(cp1x, prevExpenseY, cp1x, expenseY, x, expenseY)

                    incomeAreaPath.cubicTo(cp1x, prevIncomeY, cp1x, incomeY, x, incomeY)
                    expenseAreaPath.cubicTo(cp1x, prevExpenseY, cp1x, expenseY, x, expenseY)
                }
            }

            val lastX = leftMargin + (dailyCashFlow.size - 1) * stepX
            incomeAreaPath.lineTo(lastX, chartBottom)
            incomeAreaPath.close()

            expenseAreaPath.lineTo(lastX, chartBottom)
            expenseAreaPath.close()

            // Draw Area Gradients
            drawPath(
                path = incomeAreaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(incomeColor.copy(alpha = 0.25f), Color.Transparent),
                    startY = topPadding,
                    endY = chartBottom
                )
            )

            drawPath(
                path = expenseAreaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(expenseColor.copy(alpha = 0.15f), Color.Transparent),
                    startY = topPadding,
                    endY = chartBottom
                )
            )

            // Draw Lines
            drawPath(
                path = incomePath,
                color = incomeColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            drawPath(
                path = expensePath,
                color = expenseColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
