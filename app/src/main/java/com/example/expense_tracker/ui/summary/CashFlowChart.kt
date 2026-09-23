package com.example.expense_tracker.ui.summary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
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
        value == 0L -> "0"
        absValue >= 1_000_000 -> "${sign}${absValue / 1_000_000}jt"
        absValue >= 1_000 -> "${sign}${absValue / 1_000}rb"
        else -> value.toString()
    }
}

internal fun calculateNiceYTicks(rawMin: Long, rawMax: Long): List<Long> {
    if (rawMin >= 0) {
        val maxTarget = if (rawMax <= 0L) 300_000_000L else rawMax
        val roughStep = (maxTarget / 2.5).coerceAtLeast(1.0)
        val step = niceStep(roughStep.toLong())
        val topTick = step * 3
        return listOf(topTick, step * 2, step, 0L)
    } else {
        val maxMagnitude = maxOf(abs(rawMin), abs(rawMax), 1L)
        val step = niceStep((maxMagnitude / 2).coerceAtLeast(1L))
        val positiveTicks = (1..2).map { it * step }.filter { it <= maxMagnitude * 1.3 }
        val negativeTicks = (1..2).map { -it * step }.filter { -it >= -maxMagnitude * 1.3 }
        return (positiveTicks.reversed() + listOf(0L) + negativeTicks)
    }
}

internal fun niceStep(value: Long): Long {
    if (value <= 0) return 100_000_000L
    var exp = 1L
    var temp = value
    while (temp >= 10) {
        temp /= 10
        exp *= 10
    }
    val mult = when {
        temp <= 1 -> 1L
        temp <= 2 -> 2L
        temp <= 5 -> 5L
        else -> 10L
    }
    return mult * exp
}

private data class ProcessedPoint(
    val dateMillis: Long,
    val dateLabel: String,
    val cumulativeNet: Long,
    val dailyIncome: Long,
    val dailyExpense: Long
)

/**
 * Cash Flow Chart matching 1:1 with design spec:
 * - 4 distinct Y ticks (e.g. 300jt, 200jt, 100jt, 0)
 * - Smooth mint green cubic bezier curve with gradient area fill
 * - Glowing aura ring on active point with white center
 * - Floating tooltip bubble positioned directly above the selected dot
 * - Solid 0 baseline with vertical dashed guideline to active point
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
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = remember(axisColor) {
        TextStyle(
            color = if (isDark) Color(0xFF8E95A3) else axisColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal
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

    // Selected point index: default to 0 to match screenshot showing first point selected
    var selectedIndex by remember { mutableStateOf<Int?>(0) }

    val accessibilityDesc = stringResource(
        R.string.chart_accessibility_desc,
        periodDescription,
        CurrencyFormatter.format(totalIncome),
        CurrencyFormatter.format(totalExpense),
        CurrencyFormatter.format(netCashFlow)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(DEFAULT_CHART_HEIGHT)
            .semantics { contentDescription = accessibilityDesc }
            .pointerInput(points) {
                detectTapGestures(
                    onPress = { offset ->
                        val leftMarginPx = 38.dp.toPx()
                        val startPadPx = 24.dp.toPx()
                        val endPadPx = 12.dp.toPx()
                        val plotWidth = size.width - leftMarginPx - startPadPx - endPadPx
                        if (plotWidth > 0 && points.isNotEmpty()) {
                            val touchX = offset.x
                            val nearestIdx = points.indices.minByOrNull { i ->
                                val px = if (points.size == 1) {
                                    leftMarginPx + startPadPx + plotWidth / 2f
                                } else {
                                    leftMarginPx + startPadPx + i * (plotWidth / (points.size - 1))
                                }
                                abs(px - touchX)
                            } ?: 0
                            selectedIndex = nearestIdx
                        }
                    }
                )
            }
            .pointerInput(points) {
                detectHorizontalDragGestures(
                    onDragEnd = { /* retain selection */ },
                    onDragCancel = { /* retain selection */ },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        val leftMarginPx = 38.dp.toPx()
                        val startPadPx = 24.dp.toPx()
                        val endPadPx = 12.dp.toPx()
                        val plotWidth = size.width - leftMarginPx - startPadPx - endPadPx
                        if (plotWidth > 0 && points.isNotEmpty()) {
                            val touchX = change.position.x
                            val nearestIdx = points.indices.minByOrNull { i ->
                                val px = if (points.size == 1) {
                                    leftMarginPx + startPadPx + plotWidth / 2f
                                } else {
                                    leftMarginPx + startPadPx + i * (plotWidth / (points.size - 1))
                                }
                                abs(px - touchX)
                            } ?: 0
                            selectedIndex = nearestIdx
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val totalWidth = size.width
            val totalHeight = size.height

            val leftMargin = 38.dp.toPx()
            val bottomMargin = 26.dp.toPx()
            val topPadding = 14.dp.toPx()

            val chartWidth = totalWidth - leftMargin
            val chartHeight = totalHeight - bottomMargin - topPadding

            val values = points.map { it.cumulativeNet }
            val rawMax = values.maxOrNull() ?: 0L
            val rawMin = values.minOrNull() ?: 0L

            val yTicks = calculateNiceYTicks(rawMin, rawMax)
            val plotMax = yTicks.first()
            val plotMin = yTicks.last()
            val plotRange = (plotMax - plotMin).coerceAtLeast(1L).toFloat()

            fun getY(value: Long): Float {
                val normalized = (value - plotMin) / plotRange
                return topPadding + chartHeight - (normalized * chartHeight)
            }

            val yZero = getY(0L).coerceIn(topPadding, topPadding + chartHeight)

            // ── Draw Y-Axis Gridlines & Labels ──
            val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()), 0f)

            yTicks.forEach { tickVal ->
                val y = getY(tickVal)
                val isZero = tickVal == 0L

                // Grid line
                drawLine(
                    color = if (isZero) {
                        if (isDark) Color(0xFF374151) else Color(0xFFD1D5DB)
                    } else {
                        if (isDark) Color(0x22FFFFFF) else gridColor.copy(alpha = 0.35f)
                    },
                    start = Offset(leftMargin, y),
                    end = Offset(totalWidth, y),
                    strokeWidth = if (isZero) 1.2.dp.toPx() else 1.dp.toPx(),
                    pathEffect = if (isZero) null else dashedEffect
                )

                // Label on the left
                val textLayout = textMeasurer.measure(
                    text = formatAxisValue(tickVal),
                    style = labelStyle
                )
                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(0f, y - textLayout.size.height / 2f)
                )
            }

            // ── Points X Positions ──
            val startPad = 24.dp.toPx()
            val endPad = 12.dp.toPx()
            val plotWidth = (totalWidth - leftMargin - startPad - endPad).coerceAtLeast(1f)

            fun getPointX(index: Int): Float {
                return if (points.size <= 1) {
                    leftMargin + startPad + plotWidth / 2f
                } else {
                    leftMargin + startPad + index * (plotWidth / (points.size - 1))
                }
            }

            // ── Build Path for Trend Curve & Subtle Fill ──
            val linePath = Path()
            val areaPath = Path()

            val entryX = leftMargin
            val firstPtX = getPointX(0)
            val firstPtY = getY(points[0].cumulativeNet)
            val entryY = (firstPtY + yZero) * 0.5f

            // Start curve from left margin with a natural lead-in
            linePath.moveTo(entryX, entryY)
            linePath.cubicTo(
                entryX + (firstPtX - entryX) * 0.45f, entryY - (entryY - firstPtY) * 0.15f,
                entryX + (firstPtX - entryX) * 0.75f, firstPtY,
                firstPtX, firstPtY
            )

            areaPath.moveTo(entryX, yZero)
            areaPath.lineTo(entryX, entryY)
            areaPath.cubicTo(
                entryX + (firstPtX - entryX) * 0.45f, entryY - (entryY - firstPtY) * 0.15f,
                entryX + (firstPtX - entryX) * 0.75f, firstPtY,
                firstPtX, firstPtY
            )

            for (i in 1 until points.size) {
                val prevX = getPointX(i - 1)
                val prevY = getY(points[i - 1].cumulativeNet)
                val currX = getPointX(i)
                val currY = getY(points[i].cumulativeNet)

                val cpX = prevX + (currX - prevX) / 2f
                linePath.cubicTo(cpX, prevY, cpX, currY, currX, currY)
                areaPath.cubicTo(cpX, prevY, cpX, currY, currX, currY)
            }

            val lastPtX = getPointX(points.size - 1)
            areaPath.lineTo(lastPtX, yZero)
            areaPath.lineTo(entryX, yZero)
            areaPath.close()

            val trendColor = if ((points.lastOrNull()?.cumulativeNet ?: 0L) >= 0) {
                if (isDark) Color(0xFF34D399) else Color(0xFF0F8A58)
            } else {
                if (isDark) Color(0xFFF87171) else Color(0xFFC5221F)
            }

            // 1. Draw gradient area fill
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        trendColor.copy(alpha = 0.28f),
                        trendColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    startY = topPadding,
                    endY = yZero
                )
            )

            // 2. Draw smooth trend stroke
            drawPath(
                path = linePath,
                color = trendColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // ── Draw Dots and X-Axis Date Labels ──
            val currentIdx = (selectedIndex ?: 0).coerceIn(0, points.size - 1)

            points.indices.forEach { idx ->
                val px = getPointX(idx)
                val py = getY(points[idx].cumulativeNet)
                val isSelected = (idx == currentIdx)

                if (isSelected) {
                    // Vertical dashed guideline down to 0 baseline
                    drawLine(
                        color = Color(0xFF6B7280).copy(alpha = 0.5f),
                        start = Offset(px, py + 8.dp.toPx()),
                        end = Offset(px, yZero),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashedEffect
                    )

                    // Outer aura glow ring
                    drawCircle(
                        color = trendColor.copy(alpha = 0.25f),
                        radius = 9.dp.toPx(),
                        center = Offset(px, py)
                    )

                    // Mid solid ring
                    drawCircle(
                        color = trendColor,
                        radius = 5.dp.toPx(),
                        center = Offset(px, py)
                    )

                    // Inner crisp white center dot
                    drawCircle(
                        color = Color.White,
                        radius = 2.5.dp.toPx(),
                        center = Offset(px, py)
                    )
                } else {
                    // Solid green dot
                    drawCircle(
                        color = trendColor,
                        radius = 3.5.dp.toPx(),
                        center = Offset(px, py)
                    )
                }

                // X-Axis date label directly below 0 baseline
                val dateLayout = textMeasurer.measure(
                    text = points[idx].dateLabel,
                    style = labelStyle
                )
                drawText(
                    textLayoutResult = dateLayout,
                    topLeft = Offset(
                        x = (px - dateLayout.size.width / 2f).coerceIn(leftMargin, totalWidth - dateLayout.size.width),
                        y = yZero + 8.dp.toPx()
                    )
                )
            }

            // ── Floating Tooltip Bubble directly above Active Point ──
            if (currentIdx in points.indices) {
                val selX = getPointX(currentIdx)
                val selY = getY(points[currentIdx].cumulativeNet)
                val selPoint = points[currentIdx]

                val dateText = textMeasurer.measure(
                    text = selPoint.dateLabel,
                    style = TextStyle(
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                )

                val amountStr = if (isBalanceVisible) {
                    CurrencyFormatter.format(selPoint.cumulativeNet)
                } else {
                    "Rp •••••••"
                }

                val amountText = textMeasurer.measure(
                    text = amountStr,
                    style = TextStyle(
                        color = trendColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                val padH = 12.dp.toPx()
                val padV = 8.dp.toPx()
                val bubbleInnerW = maxOf(dateText.size.width, amountText.size.width)
                val bubbleW = bubbleInnerW + padH * 2
                val bubbleH = dateText.size.height + amountText.size.height + padV * 2 + 2.dp.toPx()

                val caretH = 5.dp.toPx()
                val caretW = 8.dp.toPx()
                val dotGap = 12.dp.toPx()

                val boxBottom = selY - dotGap - caretH
                val boxTop = boxBottom - bubbleH
                val boxLeft = (selX - bubbleW / 2f).coerceIn(leftMargin, totalWidth - bubbleW)
                val boxRight = boxLeft + bubbleW

                val tooltipPath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            left = boxLeft,
                            top = boxTop,
                            right = boxRight,
                            bottom = boxBottom,
                            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                        )
                    )
                    val caretCenterX = selX.coerceIn(boxLeft + 12.dp.toPx(), boxRight - 12.dp.toPx())
                    moveTo(caretCenterX - caretW / 2f, boxBottom)
                    lineTo(caretCenterX, selY - dotGap)
                    lineTo(caretCenterX + caretW / 2f, boxBottom)
                    close()
                }

                // Bubble container
                drawPath(
                    path = tooltipPath,
                    color = if (isDark) Color(0xFF1E222A) else Color(0xFFFFFFFF)
                )

                // Bubble outline
                drawPath(
                    path = tooltipPath,
                    color = if (isDark) Color(0xFF2A303C) else Color(0xFFE5E7EB),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Tooltip text
                drawText(
                    textLayoutResult = dateText,
                    topLeft = Offset(boxLeft + padH, boxTop + padV)
                )
                drawText(
                    textLayoutResult = amountText,
                    topLeft = Offset(boxLeft + padH, boxTop + padV + dateText.size.height + 2.dp.toPx())
                )
            }
        }
    }
}

private val DEFAULT_CHART_HEIGHT = 180.dp


