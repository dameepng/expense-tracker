package com.example.expense_tracker.ui.summary.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expense_tracker.R
import com.example.expense_tracker.data.FilterPeriod

/**
 * Dropdown selector for filtering transactions by period (e.g. Daily, Weekly, Monthly, Custom).
 */
@Composable
fun SummaryPeriodDropdown(
    selected: FilterPeriod,
    onSelected: (FilterPeriod, Long?, Long?) -> Unit,
    onCustomClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val pillBg = if (isDark) Color(0xFF222630) else MaterialTheme.colorScheme.surfaceContainerHighest

    Box(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = pillBg,
            modifier = Modifier.clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(selected.labelResId),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.choose_period),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            val standardFilters = FilterPeriod.entries.filter { it != FilterPeriod.CUSTOM }
            standardFilters.forEach { filter ->
                DropdownMenuItem(
                    text = { Text(stringResource(filter.labelResId)) },
                    onClick = {
                        expanded = false
                        if (selected != filter) {
                            onSelected(filter, null, null)
                        }
                    },
                    trailingIcon = if (selected == filter) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(FilterPeriod.CUSTOM.labelResId)) },
                onClick = {
                    expanded = false
                    if (selected != FilterPeriod.CUSTOM) {
                        onCustomClick()
                    }
                },
                trailingIcon = if (selected == FilterPeriod.CUSTOM) {
                    { Icon(Icons.Default.Check, contentDescription = null) }
                } else null
            )
        }
    }
}
