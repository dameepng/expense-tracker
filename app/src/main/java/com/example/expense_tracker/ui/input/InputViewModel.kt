package com.example.expense_tracker.ui.input

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InputViewModel(
    private val repository: InputRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        InputUiState(categories = repository.getCategories())
    )
    val uiState: StateFlow<InputUiState> = _uiState.asStateFlow()

    fun onAmountChange(text: String) {
        val isValid = text.toLongOrNull() != null && text.toLong() > 0
        _uiState.value = _uiState.value.copy(
            amountText = text,
            isSaveEnabled = isValid && _uiState.value.selectedCategoryId != null
        )
    }

    fun onCategorySelected(categoryId: Long) {
        val amountValid = _uiState.value.amountText.toLongOrNull()?.let { it > 0 } ?: false
        _uiState.value = _uiState.value.copy(
            selectedCategoryId = categoryId,
            isSaveEnabled = amountValid
        )
    }

    fun onSave() {
        val state = _uiState.value
        val amount = state.amountText.toLongOrNull() ?: return
        val categoryId = state.selectedCategoryId ?: return
        if (amount <= 0) return

        repository.insertExpense(amount, categoryId, System.currentTimeMillis())

        // Reset form for next input
        _uiState.value = InputUiState(
            categories = state.categories,
            saved = true
        )
    }
}
