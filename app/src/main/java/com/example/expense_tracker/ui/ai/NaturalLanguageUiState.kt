package com.example.expense_tracker.ui.ai

import androidx.compose.runtime.Immutable
import com.example.expense_tracker.data.Category
import com.example.expense_tracker.data.TransactionType
import com.example.expense_tracker.data.Wallet
import java.time.LocalDate

enum class AiUiError {
    CONFIGURATION, NETWORK, TIMEOUT, RATE_LIMIT, AUTHENTICATION, SERVICE,
    INVALID_RESPONSE, INVALID_INPUT, INITIALIZATION, VALIDATION, SAVE
}

@Immutable
data class TransactionDraft(
    val amountText: String = "",
    val categoryId: Long? = null,
    val merchant: String = "",
    val dateText: String = "",
    val note: String = "",
    val isRecurring: Boolean = false,
    val walletId: Long? = null,
    val type: String = TransactionType.EXPENSE.name
) {
    fun parsedDate(): LocalDate? = if (dateText.matches(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}"))) {
        runCatching { LocalDate.parse(dateText) }.getOrNull()?.takeIf { it.year in 1..9999 }
    } else null

    fun isValid(categories: List<Category>, wallets: List<Wallet>): Boolean {
        val amount = amountText.toLongOrNull() ?: 0
        return amount > 0 && parsedDate() != null &&
            merchant.length <= 200 && note.length <= 1000 &&
            categories.any { category -> category.id == categoryId && (category.type == type || category.type == "BOTH") } &&
            wallets.any { wallet -> wallet.id == walletId }
    }
}

@Immutable
data class NaturalLanguageUiState(
    val inputText: String = "",
    val categories: List<Category> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val isInitializing: Boolean = true,
    val isParsing: Boolean = false,
    val isSaving: Boolean = false,
    val drafts: List<TransactionDraft> = emptyList(),
    val error: AiUiError? = null,
    val saved: Boolean = false
) {
    val draft: TransactionDraft? get() = drafts.firstOrNull()

    val canParse: Boolean
        get() = !isInitializing && !isParsing && !isSaving && !saved &&
            inputText.isNotBlank() && inputText.length <= 1000 && categories.isNotEmpty()

    val canSave: Boolean
        get() = !isInitializing && !isParsing && !isSaving && !saved && drafts.isNotEmpty() && drafts.all {
            it.isValid(categories, wallets)
        }
}
