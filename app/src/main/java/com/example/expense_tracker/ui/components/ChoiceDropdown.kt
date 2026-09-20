package com.example.expense_tracker.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Reusable dropdown menu picker for single-choice selection from a list of id-to-name pairs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoiceDropdown(
    label: String,
    selectedId: Long?,
    choices: List<Pair<Long, String>>,
    enabled: Boolean,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectionEnabled = enabled && choices.isNotEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded && selectionEnabled,
        onExpandedChange = { expanded = it && selectionEnabled },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = choices.firstOrNull { it.first == selectedId }?.second.orEmpty(),
            onValueChange = {},
            readOnly = true,
            enabled = selectionEnabled,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && selectionEnabled)
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, selectionEnabled)
        )
        ExposedDropdownMenu(
            expanded = expanded && selectionEnabled,
            onDismissRequest = { expanded = false }
        ) {
            choices.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelect(id)
                        expanded = false
                    }
                )
            }
        }
    }
}
