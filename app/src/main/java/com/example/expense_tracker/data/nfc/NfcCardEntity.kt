package com.example.expense_tracker.data.nfc

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nfc_cards")
data class NfcCardEntity(
    @PrimaryKey
    val cardNumber: String,
    val cardType: String, // MANDIRI_EMONEY, BNI_TAPCASH, UNKNOWN
    val balance: Long,
    val lastScannedAt: Long = System.currentTimeMillis(),
    val linkedWalletId: Long? = null,
    val cardLabel: String = ""
)
