package com.example.expense_tracker.data.nfc

enum class NfcCardType(
    val displayName: String,
    val bankName: String,
    val gradientColors: List<Long>,
    val defaultIconName: String
) {
    MANDIRI_EMONEY(
        displayName = "Mandiri e-Money",
        bankName = "Bank Mandiri",
        gradientColors = listOf(0xFF0A2540, 0xFF194880, 0xFFF59E0B),
        defaultIconName = "AccountBalance"
    ),
    BNI_TAPCASH(
        displayName = "BNI TapCash",
        bankName = "Bank Negara Indonesia",
        gradientColors = listOf(0xFFE05206, 0xFFF97316, 0xFF0D9488),
        defaultIconName = "CreditCard"
    ),
    UNKNOWN(
        displayName = "Kartu E-Money",
        bankName = "Uang Elektronik",
        gradientColors = listOf(0xFF334155, 0xFF1E293B),
        defaultIconName = "Contactless"
    )
}
