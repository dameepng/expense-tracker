package com.example.expense_tracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bill_reminders",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Wallet::class,
            parentColumns = ["id"],
            childColumns = ["walletId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId"), Index("walletId")]
)
data class BillReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Long,
    val dueDay: Int,
    val categoryId: Long,
    val walletId: Long,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastPaidMonth: String? = null
) {
    init {
        require(amount > 0) { "Amount must be greater than 0, but was $amount" }
        require(dueDay in 1..31) { "dueDay must be between 1 and 31, but was $dueDay" }
    }
}
