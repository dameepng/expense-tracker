package com.example.expense_tracker.ui.input

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InputViewModel(
    private val repository: InputRepository,
    private val walletRepository: com.example.expense_tracker.data.WalletRepository,
    private val billReminderRepository: com.example.expense_tracker.data.BillReminderRepository,
    private val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO,
    private val expenseId: Long? = null,
    private val initialDescription: String? = null,
    private val initialAmount: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(InputUiState())
    val uiState: StateFlow<InputUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                coroutineScope {
                    val walletsDeferred = async(ioDispatcher) {
                        walletRepository.getAllWallets().first()
                    }
                    val expenseDeferred = if (expenseId != null) {
                        async(ioDispatcher) { repository.getExpenseById(expenseId) }
                    } else null

                    val wallets = walletsDeferred.await()
                    val expense = expenseDeferred?.await()

                    var loadedAmount = initialAmount ?: ""
                    var loadedDescription = initialDescription ?: ""
                    var loadedCategoryId: Long? = null
                    var loadedWalletId: Long? = if (wallets.size == 1) {
                        wallets.first().id
                    } else if (initialDescription != null) {
                        wallets.firstOrNull { wallet ->
                            initialDescription.contains(wallet.name, ignoreCase = true) ||
                            (wallet.name.contains("TapCash", ignoreCase = true) && initialDescription.contains("TapCash", ignoreCase = true)) ||
                            (wallet.name.contains("e-Money", ignoreCase = true) && initialDescription.contains("e-Money", ignoreCase = true))
                        }?.id ?: wallets.firstOrNull()?.id
                    } else null
                    var loadedTransactionType = com.example.expense_tracker.data.TransactionType.EXPENSE

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

                    val categories = withContext(ioDispatcher) {
                        repository.getCategoriesByType(loadedTransactionType.name).first()
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        categories = categories,
                        wallets = wallets,
                        amountText = loadedAmount,
                        description = loadedDescription,
                        selectedCategoryId = loadedCategoryId,
                        selectedWalletId = loadedWalletId,
                        transactionType = loadedTransactionType,
                        isSaveEnabled = loadedAmount.isNotEmpty() && loadedCategoryId != null
                    )
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun updateSaveEnabled() {
        val state = _uiState.value
        val amountValid = state.amountText.toLongOrNull()?.let { it > 0 } ?: false
        val walletSelected = state.selectedWalletId != null
        val categorySelected = state.selectedCategoryId != null
        
        val isEnabled = if (state.inputMode == InputMode.TRANSACTION) {
            amountValid && walletSelected && categorySelected
        } else {
            val dueDay = state.billReminderDueDay.toIntOrNull()
            val dueDayValid = dueDay != null && dueDay in 1..31
            val nameValid = state.billReminderName.isNotBlank()
            amountValid && walletSelected && dueDayValid && nameValid && categorySelected
        }
        _uiState.value = state.copy(isSaveEnabled = isEnabled)
    }

    fun onInputTypeSelected(option: InputTypeOption) {
        val newTransactionType = if (option == InputTypeOption.INCOME) com.example.expense_tracker.data.TransactionType.INCOME else com.example.expense_tracker.data.TransactionType.EXPENSE
        val newInputMode = if (option == InputTypeOption.BILL_REMINDER) InputMode.BILL_REMINDER else InputMode.TRANSACTION

        _uiState.value = _uiState.value.copy(
            inputTypeOption = option,
            transactionType = newTransactionType,
            inputMode = newInputMode,
            selectedCategoryId = null,
            amountText = "",
            billReminderName = "",
            billReminderDueDay = "",
            isRepeat = true,
            description = "",
            isSaveEnabled = false
        )
        
        viewModelScope.launch {
            val categories = withContext(ioDispatcher) {
                repository.getCategoriesByType(newTransactionType.name).first()
            }
            _uiState.value = _uiState.value.copy(categories = categories)
        }
    }

    fun onBillReminderNameChange(text: String) {
        _uiState.value = _uiState.value.copy(billReminderName = text)
        updateSaveEnabled()
    }

    fun onBillReminderDueDayChange(text: String) {
        // Only allow numbers up to 31
        if (text.isEmpty() || (text.toIntOrNull() != null && text.toInt() <= 31)) {
            _uiState.value = _uiState.value.copy(billReminderDueDay = text)
            updateSaveEnabled()
        }
    }

    fun onRepeatChange(isRepeat: Boolean) {
        _uiState.value = _uiState.value.copy(isRepeat = isRepeat)
    }

    fun onAmountChange(text: String) {
        _uiState.value = _uiState.value.copy(amountText = text)
        updateSaveEnabled()
    }

    fun onDescriptionChange(text: String) {
        _uiState.value = _uiState.value.copy(
            description = text
        )
    }

    fun onCategorySelected(categoryId: Long) {
        _uiState.value = _uiState.value.copy(
            selectedCategoryId = categoryId
        )
        updateSaveEnabled()
    }

    fun onWalletSelected(walletId: Long) {
        _uiState.value = _uiState.value.copy(
            selectedWalletId = walletId
        )
        updateSaveEnabled()
    }



    fun onSave() {
        val state = _uiState.value
        val amount = state.amountText.toLongOrNull() ?: return
        val categoryId = state.selectedCategoryId ?: return
        val walletId = state.selectedWalletId ?: return
        if (amount <= 0) return

        viewModelScope.launch {
            withContext(ioDispatcher) {
                if (state.inputMode == InputMode.TRANSACTION) {
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
                } else {
                    val dueDay = state.billReminderDueDay.toIntOrNull() ?: return@withContext
                    val reminder = com.example.expense_tracker.data.BillReminder(
                        name = state.billReminderName,
                        amount = amount,
                        dueDay = dueDay,
                        categoryId = categoryId,
                        walletId = walletId,
                        isActive = true,
                        isRepeat = state.isRepeat,
                        createdAt = System.currentTimeMillis()
                    )
                    billReminderRepository.insertReminder(reminder)
                }
            }

            // Reset form for next input
            _uiState.value = InputUiState(
                isLoading = false,
                categories = _uiState.value.categories,
                wallets = state.wallets,
                transactionType = state.transactionType,
                saved = true
            )
        }
    }
}
