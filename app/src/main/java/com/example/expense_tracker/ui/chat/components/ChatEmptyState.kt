package com.example.expense_tracker.ui.chat.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.R
import com.example.expense_tracker.ui.theme.spacing

data class PromptSuggestion(
    val title: String,
    val query: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBackground: Color
)

@Composable
fun ChatEmptyState(
    onExampleClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing

    val promptFoodTitle = stringResource(R.string.chat_prompt_food_title)
    val promptFoodQuery = stringResource(R.string.chat_example_food_month)
    val promptTopTitle = stringResource(R.string.chat_prompt_top_category_title)
    val promptTopQuery = stringResource(R.string.chat_example_top_category)
    val promptCompTitle = stringResource(R.string.chat_prompt_compare_title)
    val promptCompQuery = stringResource(R.string.chat_example_month_comparison)
    val promptUnusualTitle = stringResource(R.string.chat_prompt_unusual_title)
    val promptUnusualQuery = stringResource(R.string.chat_example_unusual)

    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val secondaryContainer = MaterialTheme.colorScheme.secondaryContainer
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer
    val surfaceContainerHighest = MaterialTheme.colorScheme.surfaceContainerHighest
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val suggestions = remember(
        promptFoodTitle, promptFoodQuery,
        promptTopTitle, promptTopQuery,
        promptCompTitle, promptCompQuery,
        promptUnusualTitle, promptUnusualQuery,
        primaryColor, primaryContainer,
        secondaryColor, secondaryContainer,
        tertiaryColor, tertiaryContainer,
        surfaceContainerHighest, onSurfaceVariant
    ) {
        listOf(
            PromptSuggestion(
                title = promptFoodTitle,
                query = promptFoodQuery,
                icon = Icons.Rounded.Restaurant,
                iconTint = primaryColor,
                iconBackground = primaryContainer.copy(alpha = 0.6f)
            ),
            PromptSuggestion(
                title = promptTopTitle,
                query = promptTopQuery,
                icon = Icons.AutoMirrored.Rounded.TrendingUp,
                iconTint = secondaryColor,
                iconBackground = secondaryContainer.copy(alpha = 0.6f)
            ),
            PromptSuggestion(
                title = promptCompTitle,
                query = promptCompQuery,
                icon = Icons.AutoMirrored.Rounded.CompareArrows,
                iconTint = tertiaryColor,
                iconBackground = tertiaryContainer.copy(alpha = 0.6f)
            ),
            PromptSuggestion(
                title = promptUnusualTitle,
                query = promptUnusualQuery,
                icon = Icons.Rounded.Lightbulb,
                iconTint = onSurfaceVariant,
                iconBackground = surfaceContainerHighest
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacing.space100),
        verticalArrangement = Arrangement.spacedBy(spacing.space150)
    ) {
        ChatWelcomeHero()

        Text(
            text = stringResource(R.string.chat_prompt_suggestions_title),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = spacing.space50)
        )

        suggestions.forEach { item ->
            PromptSuggestionCard(
                item = item,
                onClick = { onExampleClick(item.query) }
            )
        }
    }
}

@Composable
fun ChatWelcomeHero(
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = spacing.space50),
        verticalArrangement = Arrangement.spacedBy(spacing.space100)
    ) {
        Text(
            text = stringResource(R.string.chat_welcome),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = stringResource(R.string.chat_empty_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PromptSuggestionCard(
    item: PromptSuggestion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    val haptic = LocalHapticFeedback.current

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.space200, vertical = spacing.space150),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.space150)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacing.space25)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.query,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
