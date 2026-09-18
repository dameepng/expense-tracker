package com.example.expense_tracker.data.nfc

import com.example.expense_tracker.data.TransactionType

data class NfcCardTransaction(
    val id: Long = 0,
    val amount: Long,
    val type: String = TransactionType.EXPENSE.name, // EXPENSE or INCOME
    val timestamp: Long = System.currentTimeMillis(),
    val terminalId: String? = null
)

data class NfcCardResult(
    val cardNumber: String,
    val balance: Long,
    val cardType: NfcCardType,
    val scannedAt: Long = System.currentTimeMillis(),
    val transactions: List<NfcCardTransaction> = emptyList()
) {
    /**
     * Formats card number for display with masked mid-digits, e.g. "6032 •••• •••• 1294"
     */
    fun formattedCardNumber(): String {
        val clean = cardNumber.filter { !it.isWhitespace() }
        return when {
            clean.length >= 16 -> {
                val p1 = clean.substring(0, 4)
                val p4 = clean.substring(clean.length - 4)
                "$p1 •••• •••• $p4"
            }
            clean.length >= 8 -> {
                val p1 = clean.substring(0, 4)
                val p2 = clean.substring(clean.length - 4)
                "$p1 •••• $p2"
            }
            clean.isNotEmpty() -> clean
            else -> "•••• •••• •••• ••••"
        }
    }
}
