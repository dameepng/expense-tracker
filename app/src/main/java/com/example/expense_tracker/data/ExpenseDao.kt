package com.example.expense_tracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.expense_tracker.data.analytics.FinancialTransactionEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExpense(expense: Expense)

    @androidx.room.Delete
    fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expenses WHERE walletId = :walletId")
    fun deleteExpensesByWalletId(walletId: Long)

    @Query("SELECT * FROM expenses WHERE id = :id")
    fun getExpenseById(id: Long): Expense?

    @Query("SELECT * FROM expenses WHERE type = 'EXPENSE' ORDER BY timestamp DESC")
    fun getAllExpenses(): List<Expense>

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllTransactions(): List<Expense>

    @Query("SELECT * FROM categories ORDER BY id ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun getCategoryById(id: Long): Category?

    @Query("SELECT * FROM categories WHERE type = :type OR type = 'BOTH' ORDER BY id ASC")
    fun getCategoriesByType(type: String): Flow<List<Category>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE timestamp >= :startTime AND timestamp < :endTime AND type = 'EXPENSE'")
    fun getTotalExpense(startTime: Long, endTime: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE timestamp >= :startTime AND timestamp < :endTime AND type = 'INCOME'")
    fun getTotalIncome(startTime: Long, endTime: Long): Flow<Long>

    @Query("SELECT * FROM expenses WHERE timestamp >= :startTime AND timestamp < :endTime AND type = 'EXPENSE' ORDER BY timestamp DESC")
    fun getExpensesBetween(startTime: Long, endTime: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE timestamp >= :startTime AND timestamp < :endTime ORDER BY timestamp DESC")
    fun getAllTransactionsBetween(startTime: Long, endTime: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE timestamp >= :startTime AND timestamp < :endTime ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactionsBetween(startTime: Long, endTime: Long, limit: Int): Flow<List<Expense>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE walletId = :walletId AND timestamp >= :startTime AND timestamp < :endTime AND type = 'EXPENSE'")
    fun getTotalExpenseByWallet(walletId: Long, startTime: Long, endTime: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE walletId = :walletId AND timestamp >= :startTime AND timestamp < :endTime AND type = 'INCOME'")
    fun getTotalIncomeByWallet(walletId: Long, startTime: Long, endTime: Long): Flow<Long>

    @Query("SELECT * FROM expenses WHERE walletId = :walletId AND timestamp >= :startTime AND timestamp < :endTime ORDER BY timestamp DESC")
    fun getTransactionsByWallet(walletId: Long, startTime: Long, endTime: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE walletId = :walletId AND timestamp >= :startTime AND timestamp < :endTime ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactionsByWallet(walletId: Long, startTime: Long, endTime: Long, limit: Int): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId AND timestamp >= :startTime AND timestamp < :endTime ORDER BY timestamp DESC")
    fun getTransactionsByCategory(categoryId: Long, startTime: Long, endTime: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId AND walletId = :walletId AND timestamp >= :startTime AND timestamp < :endTime ORDER BY timestamp DESC")
    fun getTransactionsByCategoryAndWallet(categoryId: Long, walletId: Long, startTime: Long, endTime: Long): Flow<List<Expense>>

    @Query("""
        SELECT c.id AS categoryId, c.name AS categoryName, COALESCE(SUM(e.amount), 0) AS totalAmount
        FROM categories c
        LEFT JOIN expenses e ON c.id = e.categoryId AND e.timestamp >= :startTime AND e.timestamp < :endTime AND e.type = 'EXPENSE'
        GROUP BY c.id, c.name
        HAVING totalAmount > 0
        ORDER BY totalAmount DESC
    """)
    fun getBreakdownByCategory(startTime: Long, endTime: Long): List<CategoryBreakdown>

    @Query("""
        SELECT c.id AS categoryId, c.name AS categoryName, COALESCE(SUM(e.amount), 0) AS totalAmount
        FROM categories c
        LEFT JOIN expenses e ON c.id = e.categoryId AND e.timestamp >= :startTime AND e.timestamp < :endTime AND e.type = :type
        GROUP BY c.id, c.name
        HAVING totalAmount > 0
        ORDER BY totalAmount DESC
    """)
    fun getBreakdownByCategoryAndType(startTime: Long, endTime: Long, type: String): Flow<List<CategoryBreakdown>>

    @Query("""
        SELECT c.id AS categoryId, c.name AS categoryName, COALESCE(SUM(e.amount), 0) AS totalAmount
        FROM categories c
        LEFT JOIN expenses e ON c.id = e.categoryId AND e.timestamp >= :startTime AND e.timestamp < :endTime AND e.type = :type AND e.walletId = :walletId
        GROUP BY c.id, c.name
        HAVING totalAmount > 0
        ORDER BY totalAmount DESC
    """)
    fun getBreakdownByCategoryAndTypeAndWallet(startTime: Long, endTime: Long, type: String, walletId: Long): Flow<List<CategoryBreakdown>>

    /**
     * One query provides a consistent, minimal snapshot for local financial aggregation.
     * Type filtering remains explicit in the aggregator so unsupported values cannot be
     * silently classified as expenses.
     */
    @Query("""
        SELECT e.amount AS amount,
               e.categoryId AS categoryId,
               c.name AS categoryName,
               e.timestamp AS timestamp,
               e.type AS type
        FROM expenses e
        INNER JOIN categories c ON c.id = e.categoryId
        WHERE e.timestamp >= :startTime AND e.timestamp < :endTime
        ORDER BY e.timestamp ASC, e.id ASC
    """)
    fun getFinancialSummaryEntries(
        startTime: Long,
        endTime: Long
    ): List<FinancialTransactionEntry>

    @Query("SELECT DISTINCT timestamp FROM expenses ORDER BY timestamp DESC")
    fun getDistinctDatesWithExpense(): List<Long>

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) - 
               COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) 
        FROM expenses
    """)
    fun getTotalBalance(): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) - 
               COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) 
        FROM expenses WHERE walletId = :walletId
    """)
    fun getTotalBalanceByWallet(walletId: Long): Flow<Long>
}
