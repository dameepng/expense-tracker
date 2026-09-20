package com.example.expense_tracker.ui.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.R
import com.example.expense_tracker.ui.theme.spacing

/**
 * Card displaying localized error messages and retry action for AI errors.
 */
@Composable
fun AiErrorCard(
    error: AiUiError,
    onRetryLoad: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    val message = when (error) {
        AiUiError.CONFIGURATION -> R.string.ai_error_configuration
        AiUiError.NETWORK -> R.string.ai_error_network
        AiUiError.TIMEOUT -> R.string.ai_error_timeout
        AiUiError.RATE_LIMIT -> R.string.ai_error_rate_limit
        AiUiError.AUTHENTICATION -> R.string.ai_error_authentication
        AiUiError.SERVICE -> R.string.ai_error_service
        AiUiError.INVALID_RESPONSE -> R.string.ai_error_invalid_response
        AiUiError.INVALID_INPUT -> R.string.ai_error_invalid_input
        AiUiError.INITIALIZATION -> R.string.ai_error_initialization
        AiUiError.VALIDATION -> R.string.ai_validation_hint
        AiUiError.SAVE -> R.string.ai_error_save
    }
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
            Text(stringResource(message), style = MaterialTheme.typography.bodyMedium)
            if (error == AiUiError.INITIALIZATION) {
                TextButton(onClick = onRetryLoad) {
                    Text(stringResource(R.string.ai_retry_load))
                }
            }
        }
    }
}
