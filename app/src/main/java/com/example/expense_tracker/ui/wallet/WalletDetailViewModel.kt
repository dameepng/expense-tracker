package com.example.expense_tracker.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expense_tracker.data.ExpenseRepository
import com.example.expense_tracker.data.ExpenseWithCategory
import com.example.expense_tracker.data.WalletRepository
import com.example.expense_tracker.data.Expense
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalletDetailViewModel(
    private val walletId: Long,
    private val walletRepository: WalletRepository,
    private val expenseRepository: ExpenseRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletDetailUiState(isLoading = true))
    val uiState: StateFlow<WalletDetailUiState> = _uiState.asStateFlow()

    private var currentStartTime: Long = 0L
    private var currentEndTime: Long = Long.MAX_VALUE

    init {
        loadData()
    }

    fun refresh() {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            withContext(ioDispatcher) {
                val wallet = walletRepository.getWalletById(walletId)
                val income = expenseRepository.getTotalIncomeByWallet(walletId, currentStartTime, currentEndTime)
                val expense = expenseRepository.getTotalExpenseByWallet(walletId, currentStartTime, currentEndTime)
                val balance = income - expense // Based on transaction history of this wallet
                val transactions = expenseRepository.getTransactionsByWallet(walletId, currentStartTime, currentEndTime)
                val categories = expenseRepository.getCategories()

                val withCategory = transactions.map { exp ->
                    val category = categories.find { it.id == exp.categoryId }
                    ExpenseWithCategory(
                        id = exp.id,
                        amount = exp.amount,
                        categoryId = exp.categoryId,
                        categoryName = category?.name ?: "Lainnya",
                        description = exp.description,
                        timestamp = exp.timestamp,
                        type = exp.type,
                        walletId = exp.walletId
                    )
                }

                _uiState.value = _uiState.value.copy(
                    wallet = wallet,
                    totalIncome = income,
                    totalExpense = expense,
                    balance = (wallet?.balance ?: 0L) + balance,
                    transactions = withCategory,
                    isLoading = false
                )
            }
        }
    }

    fun deleteTransaction(transaction: ExpenseWithCategory) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                // Convert ExpenseWithCategory back to Expense for deletion
                val expense = Expense(
                    id = transaction.id,
                    amount = transaction.amount,
                    categoryId = transaction.categoryId,
                    description = transaction.description,
                    timestamp = transaction.timestamp,
                    type = transaction.type,
                    walletId = walletId
                )
                expenseRepository.deleteExpense(expense)
            }
            loadData() // refresh
        }
    }

    fun undoDeleteTransaction(transaction: ExpenseWithCategory) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val expense = Expense(
                    id = transaction.id,
                    amount = transaction.amount,
                    categoryId = transaction.categoryId,
                    description = transaction.description,
                    timestamp = transaction.timestamp,
                    type = transaction.type,
                    walletId = walletId
                )
                expenseRepository.insertExpense(expense)
            }
            loadData() // refresh
        }
    }
}
