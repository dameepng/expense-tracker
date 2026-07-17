package com.example.expense_tracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Long,
    val categoryId: Long,
    val description: String = "",
    val timestamp: Long,
    val type: String = TransactionType.EXPENSE.name
) {
    init {
        require(amount > 0) { "Amount must be greater than 0, but was $amount" }
    }
}
