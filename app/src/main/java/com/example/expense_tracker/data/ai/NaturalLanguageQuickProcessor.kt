package com.example.expense_tracker.data.ai

import android.content.Context
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
                return@withContext Result.failure(IllegalArgumentException("Teks transaksi tidak boleh kosong"))
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
            val targetWalletName = targetWallet?.name ?: "Dompet Utama"

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
            val categoryName = categories.find { it.id == parsed.categoryId }?.name ?: "Transaksi"
            val merchantInfo = if (parsed.merchant.isNotBlank()) " di ${parsed.merchant}" else ""
            val noteInfo = if (parsed.note.isNotBlank()) "\n\"${parsed.note}\"" else ""

            val title = "Transaksi Berhasil Dicatat! ✨"
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
