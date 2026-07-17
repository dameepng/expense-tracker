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
    private val walletRepository: com.example.expense_tracker.data.WalletRepository,
    private val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO,
    private val expenseId: Long? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(InputUiState())
    val uiState: StateFlow<InputUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val wallets = withContext(ioDispatcher) {
                walletRepository.getAllWallets()
            }
            var loadedAmount = ""
            var loadedDescription = ""
            var loadedCategoryId: Long? = null
            var loadedWalletId: Long? = if (wallets.size == 1) wallets.first().id else null
            var loadedTransactionType = com.example.expense_tracker.data.TransactionType.EXPENSE

            if (expenseId != null) {
                val expense = withContext(ioDispatcher) {
                    repository.getExpenseById(expenseId)
                }
                if (expense != null) {
                    loadedAmount = expense.amount.toString()
                    loadedDescription = expense.description
                    loadedCategoryId = expense.categoryId
                    loadedTransactionType = try {
                        com.example.expense_tracker.data.TransactionType.valueOf(expense.type)
                    } catch (e: IllegalArgumentException) {
                        com.example.expense_tracker.data.TransactionType.EXPENSE
                    }
                    loadedWalletId = expense.walletId
                }
            }

            val categories = withContext(ioDispatcher) {
                repository.getCategoriesByType(loadedTransactionType.name)
            }

            _uiState.value = _uiState.value.copy(
                categories = categories,
                wallets = wallets,
                amountText = loadedAmount,
                description = loadedDescription,
                selectedCategoryId = loadedCategoryId,
                selectedWalletId = loadedWalletId,
                transactionType = loadedTransactionType,
                isSaveEnabled = loadedAmount.isNotEmpty() && loadedCategoryId != null && loadedWalletId != null
            )
        }
    }

    fun onAmountChange(text: String) {
        val isValid = text.toLongOrNull() != null && text.toLong() > 0
        _uiState.value = _uiState.value.copy(
            amountText = text,
            isSaveEnabled = isValid && _uiState.value.selectedCategoryId != null && _uiState.value.selectedWalletId != null
        )
    }

    fun onDescriptionChange(text: String) {
        _uiState.value = _uiState.value.copy(
            description = text
        )
    }

    fun onCategorySelected(categoryId: Long) {
        val amountValid = _uiState.value.amountText.toLongOrNull()?.let { it > 0 } ?: false
        _uiState.value = _uiState.value.copy(
            selectedCategoryId = categoryId,
            isSaveEnabled = amountValid && _uiState.value.selectedWalletId != null
        )
    }

    fun onWalletSelected(walletId: Long) {
        val amountValid = _uiState.value.amountText.toLongOrNull()?.let { it > 0 } ?: false
        _uiState.value = _uiState.value.copy(
            selectedWalletId = walletId,
            isSaveEnabled = amountValid && _uiState.value.selectedCategoryId != null
        )
    }

    fun onTransactionTypeChange(type: com.example.expense_tracker.data.TransactionType) {
        _uiState.value = _uiState.value.copy(
            transactionType = type,
            selectedCategoryId = null,
            isSaveEnabled = false
        )
        viewModelScope.launch {
            val categories = withContext(ioDispatcher) {
                repository.getCategoriesByType(type.name)
            }
            _uiState.value = _uiState.value.copy(categories = categories)
        }
    }

    fun onSave() {
        val state = _uiState.value
        val amount = state.amountText.toLongOrNull() ?: return
        val categoryId = state.selectedCategoryId ?: return
        val walletId = state.selectedWalletId ?: return
        if (amount <= 0) return

        viewModelScope.launch {
            withContext(ioDispatcher) {
                val timestamp = if (expenseId != null) {
                    repository.getExpenseById(expenseId)?.timestamp ?: System.currentTimeMillis()
                } else {
                    System.currentTimeMillis()
                }
                repository.insertExpense(
                    amount = amount,
                    categoryId = categoryId,
                    description = state.description,
                    timestamp = timestamp,
                    type = state.transactionType.name,
                    walletId = walletId,
                    id = expenseId ?: 0L
                )
            }

            // Reset form for next input
            _uiState.value = InputUiState(
                categories = _uiState.value.categories,
                saved = true
            )
        }
    }
}
