package com.example.expense_tracker.ui.summary.categorydetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.expense_tracker.data.Category
import com.example.expense_tracker.data.ExpenseRepository
import com.example.expense_tracker.data.ExpenseWithCategory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CategoryDetailUiState(
    val category: Category? = null,
    val transactions: List<ExpenseWithCategory> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class CategoryDetailViewModel(
    private val repository: ExpenseRepository,
    savedStateHandle: SavedStateHandle,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val categoryId: Long = checkNotNull(savedStateHandle.get<String>("categoryId")?.toLongOrNull())
    private val walletId: Long? = savedStateHandle.get<String>("walletId")?.toLongOrNull()
    private val startTime: Long = checkNotNull(savedStateHandle.get<String>("startTime")?.toLongOrNull())
    private val endTime: Long = checkNotNull(savedStateHandle.get<String>("endTime")?.toLongOrNull())

    private val _uiState = MutableStateFlow(CategoryDetailUiState())
    val uiState: StateFlow<CategoryDetailUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // We combine the categories flow and transactions flow
                // Actually category name is needed, but we can just use the categories flow to find it.
                val transactionsFlow = if (walletId != null) {
                    repository.getTransactionsByCategoryAndWallet(categoryId, walletId, startTime, endTime)
                } else {
                    repository.getTransactionsByCategory(categoryId, startTime, endTime)
                }

                combine(
                    repository.getCategories(),
                    transactionsFlow
                ) { categories, expenses ->
                    val category = categories.find { it.id == categoryId }
                    val expensesWithCategory = expenses.map { expense ->
                        ExpenseWithCategory(
                            id = expense.id,
                            amount = expense.amount,
                            description = expense.description,
                            timestamp = expense.timestamp,
                            type = expense.type,
                            categoryId = expense.categoryId,
                            walletId = expense.walletId,
                            categoryName = category?.name ?: "Unknown",
                            merchant = expense.merchant,
                            isRecurring = expense.isRecurring
                        )
                    }
                    CategoryDetailUiState(
                        category = category,
                        transactions = expensesWithCategory,
                        isLoading = false
                    )
                }.catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteExpense(expense: ExpenseWithCategory) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val expenseEntity = com.example.expense_tracker.data.Expense(
                    id = expense.id,
                    amount = expense.amount,
                    description = expense.description,
                    timestamp = expense.timestamp,
                    type = expense.type,
                    categoryId = expense.categoryId,
                    walletId = expense.walletId,
                    merchant = expense.merchant,
                    isRecurring = expense.isRecurring
                )
                repository.deleteExpense(expenseEntity)
            }
        }
    }

    fun undoDeleteExpense(expense: ExpenseWithCategory) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val expenseEntity = com.example.expense_tracker.data.Expense(
                    id = expense.id,
                    amount = expense.amount,
                    description = expense.description,
                    timestamp = expense.timestamp,
                    type = expense.type,
                    categoryId = expense.categoryId,
                    walletId = expense.walletId,
                    merchant = expense.merchant,
                    isRecurring = expense.isRecurring
                )
                repository.insertExpense(expenseEntity)
            }
        }
    }
}

class CategoryDetailViewModelFactory(
    private val app: android.app.Application
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T {
        val database = com.example.expense_tracker.data.AppDatabase.getInstance(app)
        val repository = com.example.expense_tracker.data.RoomExpenseRepository(database.expenseDao())
        val savedStateHandle = extras.createSavedStateHandle()
        if (modelClass.isAssignableFrom(CategoryDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryDetailViewModel(repository, savedStateHandle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
