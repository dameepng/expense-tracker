package com.example.expense_tracker.ui.receipt

import android.net.Uri
import com.example.expense_tracker.data.Category
import com.example.expense_tracker.data.Wallet
import com.example.expense_tracker.ui.ai.TransactionDraft

enum class ReceiptScanPhase { IDLE, SELECTED, SCANNING, SUCCESS, FALLBACK, ERROR }
enum class ReceiptScanError {
    CONFIGURATION, NETWORK, TIMEOUT, INVALID_RESPONSE, IMAGE_FAILURE,
    IMAGE_TOO_LARGE, IMAGE_DECODE, OFFLINE, UNKNOWN, VALIDATION, SAVE
}

data class ReceiptScanUiState(
    val phase: ReceiptScanPhase = ReceiptScanPhase.IDLE,
    val imageUri: Uri? = null,
    val categories: List<Category> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val draft: TransactionDraft? = null,
    val items: List<String> = emptyList(),
    val error: ReceiptScanError? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false
) {
    val canSave: Boolean
        get() = phase == ReceiptScanPhase.SUCCESS && !isSaving && !saved && draft?.let {
            (it.amountText.toLongOrNull() ?: 0) > 0 &&
                it.parsedDate() != null &&
                it.categoryId != null &&
                it.walletId != null &&
                categories.any { cat -> cat.id == it.categoryId } &&
                wallets.any { wallet -> wallet.id == it.walletId }
        } == true
}
