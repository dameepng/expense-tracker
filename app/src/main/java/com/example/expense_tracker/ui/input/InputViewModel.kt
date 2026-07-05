package com.example.expense_tracker.ui.input

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InputViewModel(
    private val repository: InputRepository,
    private val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(InputUiState())
    val uiState: StateFlow<InputUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val categories = withContext(ioDispatcher) {
                repository.getCategories()
            }
            _uiState.value = _uiState.value.copy(categories = categories)
        }
    }

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

        viewModelScope.launch {
            withContext(ioDispatcher) {
                repository.insertExpense(amount, categoryId, System.currentTimeMillis())
            }

            // Reset form for next input
            _uiState.value = InputUiState(
                categories = _uiState.value.categories,
                saved = true
            )
        }
    }
}
