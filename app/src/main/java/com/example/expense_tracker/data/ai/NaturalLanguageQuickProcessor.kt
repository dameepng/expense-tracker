package com.example.expense_tracker.data.ai

import android.content.Context
import com.example.expense_tracker.R
import com.example.expense_tracker.data.AppDatabase
import com.example.expense_tracker.data.Category
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
    ): Result<List<ParsedTransaction>> = withContext(Dispatchers.IO) {
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

            val parsedList = aiRepo.parse(request)
            if (parsedList.isEmpty()) {
                return@withContext Result.failure(IllegalStateException("Tidak ada transaksi yang terdeteksi"))
            }

            val draftRepo = RoomTransactionDraftRepository(database)
            draftRepo.saveAll(parsedList, targetWalletId)

            val title = buildNotificationTitle(context, parsedList.size)
            val message = buildNotificationMessage(context, parsedList, categories)
            val subText = targetWalletName

            NotificationHelper.showTransactionSuccessNotification(
                context = context,
                title = title,
                message = message,
                subText = subText
            )

            Result.success(parsedList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildNotificationTitle(context: Context, count: Int): String {
        return if (count > 1) {
            "$count Transaksi Berhasil Dicatat! ✨"
        } else {
            try {
                context.getString(R.string.nl_quick_success)
            } catch (_: Exception) {
                "Transaksi Berhasil Dicatat! ✨"
            }
        }
    }

    private fun buildNotificationMessage(
        context: Context,
        parsedList: List<ParsedTransaction>,
        categories: List<Category>
    ): String {
        return if (parsedList.size == 1) {
            val parsed = parsedList.first()
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
            "$formattedAmount • $categoryName$merchantInfo$noteInfo"
        } else {
            val totalAmount = parsedList.sumOf { it.amount }
            val totalFormatted = CurrencyFormatter.format(totalAmount)
            val itemsSummary = parsedList.joinToString("\n") { p ->
                val merch = if (p.merchant.isNotBlank()) " di ${p.merchant}" else if (p.note.isNotBlank()) " (${p.note})" else ""
                "• ${CurrencyFormatter.format(p.amount)}$merch"
            }
            "Total: $totalFormatted\n$itemsSummary"
        }
    }
}
