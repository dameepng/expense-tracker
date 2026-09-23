package com.example.expense_tracker.ui.summary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expense_tracker.R
import com.example.expense_tracker.ui.CurrencyFormatter
import com.example.expense_tracker.ui.theme.chartAxisColor
import com.example.expense_tracker.ui.theme.chartGridColor
import com.example.expense_tracker.ui.theme.financialNegativeColor
import com.example.expense_tracker.ui.theme.financialPositiveColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

internal fun formatAxisValue(value: Long): String {
    val absValue = abs(value)
    val sign = if (value < 0) "-" else ""
    return when {
        absValue >= 1_000_000 -> "${sign}${absValue / 1_000_000}jt"
        absValue >= 1_000 -> "${sign}${absValue / 1_000}rb"
        else -> value.toString()
    }
}

private data class ProcessedPoint(
    val dateMillis: Long,
    val dateLabel: String,
    val cumulativeNet: Long,
    val dailyIncome: Long,
    val dailyExpense: Long
)

/**
 * Modern, quiet, interactive Cash Flow Chart.
 * 
 * Semantics:
 * Displays cumulative net cash flow (Income minus Expense) across the selected period,
 * anchored to a zero baseline so the user instantly sees cash flow trajectory.
 * 
 * Features:
 * - Touch scrubber: drag/tap across chart to inspect specific date and exact cumulative value.
 * - Subtle grid lines and high WCAG contrast axis labels.
 * - Hardware-accelerated native Compose text measurement.
 * - Screen reader TalkBack accessibility support.
 */
@Composable
fun CashFlowChart(
    dailyCashFlow: List<DailyCashFlow>,
    modifier: Modifier = Modifier,
    netCashFlow: Long = 0L,
    totalIncome: Long = 0L,
    totalExpense: Long = 0L,
    isBalanceVisible: Boolean = true,
    periodDescription: String = stringResource(R.string.filter_month)
) {
    if (dailyCashFlow.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(DEFAULT_CHART_HEIGHT)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_expense_data),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val positiveColor = financialPositiveColor()
    val negativeColor = financialNegativeColor()
    val gridColor = chartGridColor()
    val axisColor = chartAxisColor()

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = remember(axisColor) {
        TextStyle(
            color = axisColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }

    // Pre-process cumulative net cash flow points
    val dateFormat = remember { SimpleDateFormat("d MMM", Locale.forLanguageTag("id-ID")) }
    val points = remember(dailyCashFlow) {
        var runningSum = 0L
        dailyCashFlow.map { flow ->
            runningSum += (flow.income - flow.expense)
            ProcessedPoint(
                dateMillis = flow.dateMillis,
                dateLabel = dateFormat.format(Date(flow.dateMillis)),
                cumulativeNet = runningSum,
                dailyIncome = flow.income,
                dailyExpense = flow.expense
            )
        }
    }

    // Selected point index for touch scrubbing
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val activePoint = selectedIndex?.let { points.getOrNull(it) } ?: points.lastOrNull()

    val accessibilityDesc = stringResource(
        R.string.chart_accessibility_desc,
        periodDescription,
        CurrencyFormatter.format(totalIncome),
        CurrencyFormatter.format(totalExpense),
        CurrencyFormatter.format(netCashFlow)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = accessibilityDesc }
    ) {
        // Active point indicator header / tooltip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if ((activePoint?.cumulativeNet ?: 0L) >= 0) positiveColor else negativeColor,
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = activePoint?.dateLabel ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = if (isBalanceVisible) {
                    val amount = activePoint?.cumulativeNet ?: 0L
                    val prefix = if (amount > 0) "+" else ""
                    "$prefix${CurrencyFormatter.format(amount)}"
                } else {
                    stringResource(R.string.privacy_hidden_amount)
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if ((activePoint?.cumulativeNet ?: 0L) >= 0) positiveColor else negativeColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Main Chart Canvas with touch scrubber
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(DEFAULT_CHART_HEIGHT)
                .pointerInput(points) {
                    detectTapGestures(
                        onPress = { offset ->
                            val leftMarginPx = 42.dp.toPx()
                            val chartWidth = size.width - leftMarginPx
                            if (chartWidth > 0 && points.isNotEmpty()) {
                                val touchX = (offset.x - leftMarginPx).coerceIn(0f, chartWidth)
                                val fraction = touchX / chartWidth
                                val idx = (fraction * (points.size - 1)).toInt().coerceIn(0, points.size - 1)
                                selectedIndex = idx
                            }
                        }
                    )
                }
                .pointerInput(points) {
                    detectHorizontalDragGestures(
                        onDragEnd = { /* retain selection on drag end */ },
                        onDragCancel = { selectedIndex = null },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            val leftMarginPx = 42.dp.toPx()
                            val chartWidth = size.width - leftMarginPx
                            if (chartWidth > 0 && points.isNotEmpty()) {
                                val touchX = (change.position.x - leftMarginPx).coerceIn(0f, chartWidth)
                                val fraction = touchX / chartWidth
                                val idx = (fraction * (points.size - 1)).toInt().coerceIn(0, points.size - 1)
                                selectedIndex = idx
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val totalWidth = size.width
                val totalHeight = size.height

                val leftMargin = 42.dp.toPx()
                val bottomMargin = 22.dp.toPx()
                val topPadding = 12.dp.toPx()

                val chartWidth = totalWidth - leftMargin
                val chartHeight = totalHeight - bottomMargin - topPadding

                val values = points.map { it.cumulativeNet }
                val rawMax = values.maxOrNull() ?: 0L
                val rawMin = values.minOrNull() ?: 0L

                // Ensure zero baseline is in range
                val maxVal = maxOf(0L, rawMax)
                val minVal = minOf(0L, rawMin)
                val range = (maxVal - minVal).coerceAtLeast(1L)
                val headroom = (range * 0.12f).toLong().coerceAtLeast(1L)
                val plotMax = maxVal + headroom
                val plotMin = minVal - headroom
                val plotRange = (plotMax - plotMin).toFloat()

                fun getY(value: Long): Float {
                    val normalized = (value - plotMin) / plotRange
                    return topPadding + chartHeight - (normalized * chartHeight)
                }

                // ── Y Axis Gridlines and Labels (3 ticks: Max, 0/mid, Min) ──
                val yTicks = if (minVal < 0 && maxVal > 0) {
                    listOf(plotMax - headroom, 0L, plotMin + headroom)
                } else {
                    listOf(plotMax - headroom, (plotMax - headroom + plotMin + headroom) / 2, plotMin + headroom)
                }

                val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 6.dp.toPx()), 0f)

                yTicks.forEach { tickVal ->
                    val y = getY(tickVal)
                    val isZero = tickVal == 0L
                    drawLine(
                        color = if (isZero) gridColor.copy(alpha = 0.4f) else gridColor,
                        start = Offset(leftMargin, y),
                        end = Offset(totalWidth, y),
                        strokeWidth = if (isZero) 1.5.dp.toPx() else 1.dp.toPx(),
                        pathEffect = if (isZero) null else dashedEffect
                    )

                    val textLayout = textMeasurer.measure(
                        text = formatAxisValue(tickVal),
                        style = labelStyle
                    )
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(0f, y - textLayout.size.height / 2f)
                    )
                }

                // ── X Axis Date Labels (3-4 evenly spaced) ──
                val stepX = if (points.size > 1) chartWidth / (points.size - 1) else chartWidth
                val xTickIndices = if (points.size <= 4) {
                    points.indices.toList()
                } else {
                    val count = 4
                    (0 until count).map { i -> i * (points.size - 1) / (count - 1) }
                }

                xTickIndices.forEach { idx ->
                    val x = leftMargin + idx * stepX
                    val textLayout = textMeasurer.measure(
                        text = points[idx].dateLabel,
                        style = labelStyle
                    )
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(
                            x = (x - textLayout.size.width / 2f).coerceIn(leftMargin, totalWidth - textLayout.size.width),
                            y = totalHeight - bottomMargin + 4.dp.toPx()
                        )
                    )
                }

                // ── Build Path for Trend Line and Subtle Area Fill ──
                val chartBottomY = getY(0L).coerceIn(topPadding, topPadding + chartHeight)
                val linePath = Path()
                val areaPath = Path()

                val startX = leftMargin
                val startY = getY(points[0].cumulativeNet)

                linePath.moveTo(startX, startY)
                areaPath.moveTo(startX, chartBottomY)
                areaPath.lineTo(startX, startY)

                if (points.size == 1) {
                    val endX = leftMargin + chartWidth
                    linePath.lineTo(endX, startY)
                    areaPath.lineTo(endX, startY)
                    areaPath.lineTo(endX, chartBottomY)
                } else {
                    for (i in 1 until points.size) {
                        val prevX = leftMargin + (i - 1) * stepX
                        val prevY = getY(points[i - 1].cumulativeNet)
                        val currX = leftMargin + i * stepX
                        val currY = getY(points[i].cumulativeNet)

                        val cpX = prevX + (currX - prevX) / 2f
                        linePath.cubicTo(cpX, prevY, cpX, currY, currX, currY)
                        areaPath.cubicTo(cpX, prevY, cpX, currY, currX, currY)
                    }
                    val lastX = leftMargin + (points.size - 1) * stepX
                    areaPath.lineTo(lastX, chartBottomY)
                }
                areaPath.close()

                val trendColor = if ((points.lastOrNull()?.cumulativeNet ?: 0L) >= 0) positiveColor else negativeColor

                // Area Fill
                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(trendColor.copy(alpha = 0.16f), Color.Transparent),
                        startY = topPadding,
                        endY = topPadding + chartHeight
                    )
                )

                // Trend Line
                drawPath(
                    path = linePath,
                    color = trendColor,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // ── Selected Scrubber Guide & Indicator ──
                val currentIdx = selectedIndex ?: (points.size - 1)
                if (currentIdx in points.indices) {
                    val selX = leftMargin + currentIdx * stepX
                    val selY = getY(points[currentIdx].cumulativeNet)

                    // Vertical guide
                    drawLine(
                        color = axisColor.copy(alpha = 0.45f),
                        start = Offset(selX, topPadding),
                        end = Offset(selX, topPadding + chartHeight),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashedEffect
                    )

                    // Outer dot
                    drawCircle(
                        color = trendColor,
                        radius = 6.dp.toPx(),
                        center = Offset(selX, selY)
                    )
                    // Inner white dot
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = Offset(selX, selY)
                    )
                }
            }
        }
    }
}

private val DEFAULT_CHART_HEIGHT = 160.dp

