package com.example.expense_tracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExpense(expense: Expense)

    @androidx.room.Delete
    fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE id = :id")
    fun getExpenseById(id: Long): Expense?

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): List<Expense>

    @Query("SELECT * FROM categories ORDER BY id ASC")
    fun getAllCategories(): List<Category>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun getCategoryById(id: Long): Category?

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE timestamp >= :startTime AND timestamp < :endTime")
    fun getTotalExpense(startTime: Long, endTime: Long): Long

    @Query("SELECT * FROM expenses WHERE timestamp >= :startTime AND timestamp < :endTime ORDER BY timestamp DESC")
    fun getExpensesBetween(startTime: Long, endTime: Long): List<Expense>

    @Query("""
        SELECT c.id AS categoryId, c.name AS categoryName, COALESCE(SUM(e.amount), 0) AS totalAmount
        FROM categories c
        LEFT JOIN expenses e ON c.id = e.categoryId AND e.timestamp >= :startTime AND e.timestamp < :endTime
        GROUP BY c.id, c.name
        HAVING totalAmount > 0
        ORDER BY totalAmount DESC
    """)
    fun getBreakdownByCategory(startTime: Long, endTime: Long): List<CategoryBreakdown>

    @Query("SELECT DISTINCT timestamp FROM expenses ORDER BY timestamp DESC")
    fun getDistinctDatesWithExpense(): List<Long>
}
