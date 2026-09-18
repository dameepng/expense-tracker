package com.example.expense_tracker.ui.summary

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.ui.theme.categoryColor
import com.example.expense_tracker.ui.theme.motionScheme

@Composable
fun DonutChart(
    items: List<BreakdownItem>,
    modifier: Modifier = Modifier,
    isIncome: Boolean = false
) {
    var animationPlayed by remember { mutableStateOf(false) }

    LaunchedEffect(items) {
        animationPlayed = true
    }

    val animationProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
        label = "DonutChartAnimation"
    )

    Box(
        modifier = modifier
            .size(DEFAULT_DONUT_SIZE)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // The Chart
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.width * STROKE_WIDTH_FRACTION // 15% of width for the donut ring thickness
            val radius = size.width / 2 - strokeWidth / 2

            if (items.isEmpty()) {
                // Draw empty grey ring
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                return@Canvas
            }

            var startAngle = -90f // Start from top
            val totalSweep = 360f * animationProgress

            items.forEach { item ->
                val sweepAngle = item.percentage * totalSweep
                if (sweepAngle > 0f) {
                    // Add gap if sweep angle is large enough and not 100%
                    val gapAngle = if (sweepAngle < 360f && items.size > 1) 2f else 0f
                    val actualSweep = maxOf(0.1f, sweepAngle - gapAngle) // ensure it doesn't go negative

                    drawArc(
                        color = categoryColor(item.categoryId.toInt(), isIncome),
                        startAngle = startAngle,
                        sweepAngle = actualSweep,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    startAngle += sweepAngle
                }
            }
        }
    }
}

private val DEFAULT_DONUT_SIZE = 160.dp
private const val STROKE_WIDTH_FRACTION = 0.15f
