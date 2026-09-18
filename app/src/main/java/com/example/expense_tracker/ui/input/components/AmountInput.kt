package com.example.expense_tracker.ui.input.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Formats digits with thousand-separator dots without allocating intermediate reverse/chunk lists.
 */
internal fun formatWithDots(value: String): String {
    val len = value.length
    if (len <= 3) return value
    val sb = StringBuilder(len + (len - 1) / 3)
    for (i in 0 until len) {
        if (i > 0 && (len - i) % 3 == 0) {
            sb.append('.')
        }
        sb.append(value[i])
    }
    return sb.toString()
}

private const val CURRENCY_PREFIX = "Rp "
private const val ZERO_AMOUNT_TEXT = "Rp 0"

@Composable
fun AmountInput(
    amountText: String,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayText = if (amountText.isEmpty()) {
        ZERO_AMOUNT_TEXT
    } else {
        CURRENCY_PREFIX + formatWithDots(amountText)
    }

    val textColor = if (amountText.isEmpty()) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.onBackground
    }

    val dynamicFontSize = when {
        displayText.length > 20 -> 20.sp
        displayText.length > 16 -> 24.sp
        displayText.length > 13 -> 28.sp
        displayText.length > 10 -> 34.sp
        else -> 44.sp
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasicTextField(
            value = amountText,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) onAmountChange(newValue)
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.displayLarge.copy(
                color = Color.Transparent,
                fontSize = dynamicFontSize
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            cursorBrush = SolidColor(Color.Transparent),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = dynamicFontSize
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    innerTextField()
                }
            }
        )
    }
}
