package com.example.expense_tracker.ui.summary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun CashFlowChart(
    dailyCashFlow: List<DailyCashFlow>,
    modifier: Modifier = Modifier
) {
    if (dailyCashFlow.isEmpty()) {
        Box(modifier = modifier.height(150.dp))
        return
    }

    val incomeColor = Color(0xFF10B981) // Green
    val expenseColor = Color(0xFF1E293B) // Dark Navy / Slate

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(vertical = 16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
            val width = size.width
            val height = size.height

            val maxAmount = dailyCashFlow.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(1L) ?: 1L
            val stepX = if (dailyCashFlow.size > 1) width / (dailyCashFlow.size - 1) else width

            val incomePath = Path()
            val expensePath = Path()

            val incomeAreaPath = Path()
            val expenseAreaPath = Path()

            incomeAreaPath.moveTo(0f, height)
            expenseAreaPath.moveTo(0f, height)

            dailyCashFlow.forEachIndexed { index, data ->
                val x = index * stepX
                val incomeY = height - (data.income.toFloat() / maxAmount) * height * 0.8f // 0.8 to leave some top padding
                val expenseY = height - (data.expense.toFloat() / maxAmount) * height * 0.8f

                if (index == 0) {
                    incomePath.moveTo(x, incomeY)
                    expensePath.moveTo(x, expenseY)
                    incomeAreaPath.lineTo(x, incomeY)
                    expenseAreaPath.lineTo(x, expenseY)
                } else {
                    // Smooth curves using cubic bezier could be done here, but straight lines are easier for a start
                    // Let's use simple lines for now, or bezier if we want smooth:
                    val prevX = (index - 1) * stepX
                    val prevIncomeY = height - (dailyCashFlow[index - 1].income.toFloat() / maxAmount) * height * 0.8f
                    val prevExpenseY = height - (dailyCashFlow[index - 1].expense.toFloat() / maxAmount) * height * 0.8f

                    // Control points for smooth curve
                    val cp1x = prevX + (x - prevX) / 2
                    
                    incomePath.cubicTo(cp1x, prevIncomeY, cp1x, incomeY, x, incomeY)
                    expensePath.cubicTo(cp1x, prevExpenseY, cp1x, expenseY, x, expenseY)
                    
                    incomeAreaPath.cubicTo(cp1x, prevIncomeY, cp1x, incomeY, x, incomeY)
                    expenseAreaPath.cubicTo(cp1x, prevExpenseY, cp1x, expenseY, x, expenseY)
                }
            }

            incomeAreaPath.lineTo(width, height)
            incomeAreaPath.close()

            expenseAreaPath.lineTo(width, height)
            expenseAreaPath.close()

            // Draw Area (Gradients)
            drawPath(
                path = incomeAreaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(incomeColor.copy(alpha = 0.3f), Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )

            drawPath(
                path = expenseAreaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(expenseColor.copy(alpha = 0.3f), Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw Lines
            drawPath(
                path = incomePath,
                color = incomeColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            drawPath(
                path = expensePath,
                color = expenseColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
