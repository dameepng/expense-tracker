package com.example.expense_tracker.data.ai

import android.content.Context
import com.example.expense_tracker.R
import com.example.expense_tracker.data.AppDatabase
import com.example.expense_tracker.ui.CurrencyFormatter
import com.example.expense_tracker.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

object NaturalLanguageQuickProcessor {

    suspend fun processAndSave(
        context: Context,
        text: String,
        walletId: Long? = null
    ): Result<ParsedTransaction> = withContext(Dispatchers.IO) {
        try {
            val cleanText = text.trim()
            if (cleanText.isBlank()) {
                val errorMsg = try {
                    context.getString(R.string.nl_quick_error_empty_text)
                } catch (_: Exception) {
                    "Teks transaksi tidak boleh kosong"
                }
                return@withContext Result.failure(IllegalArgumentException(errorMsg))
            }

            val database = AppDatabase.getInstance(context)
            val categories = database.expenseDao().getAllCategories().first()
            val wallets = database.walletDao().getAllWallets().first()

            val targetWallet = if (walletId != null) {
                wallets.find { it.id == walletId } ?: wallets.firstOrNull()
            } else {
                wallets.firstOrNull()
            }

            val targetWalletId = targetWallet?.id ?: 1L
            val targetWalletName = targetWallet?.name ?: try {
                context.getString(R.string.nl_quick_default_wallet_name)
            } catch (_: Exception) {
                "Dompet Utama"
            }

            val aiRepo = AiDependencies.shared.createNaturalLanguageRepository()
            val request = NaturalLanguageRequest(
                text = cleanText,
                categories = categories,
                referenceDate = LocalDate.now(),
                zoneId = ZoneId.systemDefault()
            )

            val parsed = aiRepo.parse(request)

            val draftRepo = RoomTransactionDraftRepository(database)
            draftRepo.save(parsed, targetWalletId)

            // Build friendly notification message
            val formattedAmount = CurrencyFormatter.format(parsed.amount)
            val categoryName = categories.find { it.id == parsed.categoryId }?.name ?: try {
                context.getString(R.string.nl_quick_default_category_name)
            } catch (_: Exception) {
                "Transaksi"
            }
            val merchantInfo = if (parsed.merchant.isNotBlank()) {
                try {
                    " " + context.getString(R.string.nl_quick_at_merchant, parsed.merchant)
                } catch (_: Exception) {
                    " di ${parsed.merchant}"
                }
            } else ""
            val noteInfo = if (parsed.note.isNotBlank()) "\n\"${parsed.note}\"" else ""

            val title = try {
                context.getString(R.string.nl_quick_success)
            } catch (_: Exception) {
                "Transaksi Berhasil Dicatat! ✨"
            }
            val message = "$formattedAmount • $categoryName$merchantInfo$noteInfo"
            val subText = targetWalletName

            // Fire real push notification to device
            NotificationHelper.showTransactionSuccessNotification(
                context = context,
                title = title,
                message = message,
                subText = subText
            )

            Result.success(parsed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
