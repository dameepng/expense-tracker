package com.example.expense_tracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallets")
data class Wallet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val balance: Long = 0L,
    val icon: String = "",
    val color: String = ""
)
