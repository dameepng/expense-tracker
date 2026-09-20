package com.example.expense_tracker.ui.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.R
import com.example.expense_tracker.ui.chat.ChatUiError
import com.example.expense_tracker.ui.theme.spacing

/**
 * Card displaying localized error information with optional retry and discard actions.
 */
@Composable
fun ChatErrorCard(
    error: ChatUiError,
    canRetry: Boolean,
    onRetry: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    val message = stringResource(error.messageResource())
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Column(
            modifier = Modifier.padding(spacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(spacing.space100)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.space100),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.chat_error_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = message, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (canRetry) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDiscard) {
                        Text(stringResource(R.string.chat_discard_failed))
                    }
                    Button(
                        onClick = onRetry,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(stringResource(R.string.chat_retry), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun ChatUiError.messageResource(): Int = when (this) {
    ChatUiError.CONFIGURATION -> R.string.chat_error_configuration
    ChatUiError.NETWORK -> R.string.chat_error_network
    ChatUiError.TIMEOUT -> R.string.chat_error_timeout
    ChatUiError.RATE_LIMIT -> R.string.chat_error_rate_limit
    ChatUiError.AUTHENTICATION -> R.string.chat_error_authentication
    ChatUiError.SERVICE -> R.string.chat_error_service
    ChatUiError.INVALID_RESPONSE -> R.string.chat_error_invalid_response
    ChatUiError.DATA_LOADING -> R.string.chat_error_data_loading
    ChatUiError.CONTEXT_LIMIT -> R.string.chat_error_context_limit
    ChatUiError.INVALID_INPUT -> R.string.chat_error_invalid_input
    ChatUiError.INPUT_LIMIT -> R.string.chat_error_input_limit
}
