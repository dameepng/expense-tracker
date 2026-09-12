package com.example.expense_tracker.data.ai.chat

import com.example.expense_tracker.data.UserPreferencesRepository
import com.example.expense_tracker.data.analytics.FinancialPeriodResolver
import com.example.expense_tracker.data.analytics.FinancialSummarySource
import com.example.expense_tracker.data.analytics.TransactionAggregator
import java.time.Clock
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

internal fun interface ChatContextSource {
    suspend fun loadContext(): ChatContext
}

internal class ChatContextProvider(
    private val financialSummarySource: FinancialSummarySource,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clockProvider: () -> Clock = { Clock.systemDefaultZone() },
    private val calculationDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ChatContextSource {
    /** Loads a new database snapshot and current preferences for every invocation. */
    override suspend fun loadContext(): ChatContext {
        try {
            val preferences = combine(
                userPreferencesRepository.currencyFlow,
                userPreferencesRepository.languageFlow
            ) { currency, language ->
                ChatPreferences(
                    currency = normalizeCurrency(currency),
                    locale = locale(language)
                )
            }.first()
            val clock = clockProvider()
            val summary = TransactionAggregator(
                source = financialSummarySource,
                periodResolver = FinancialPeriodResolver(
                    clock = clock,
                    zoneId = clock.zone,
                    locale = preferences.locale
                ),
                calculationDispatcher = calculationDispatcher
            ).createSnapshot()

            return ChatContext(
                snapshotEpochMillis = summary.snapshotEpochMillis,
                zoneId = summary.zoneId,
                currency = preferences.currency,
                preferredLocale = preferences.locale.toLanguageTag(),
                walletScope = WalletScope.ALL_WALLETS,
                currentWeek = summary.currentWeek,
                currentMonth = summary.currentMonth,
                previousMonth = summary.previousMonth,
                monthComparison = summary.monthComparison
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: ChatContextException) {
            throw failure
        } catch (_: Exception) {
            throw ChatContextException(ChatContextError.DATA_UNAVAILABLE)
        }
    }

    private fun normalizeCurrency(value: String): String {
        val currency = value.trim().uppercase(Locale.ROOT)
        if (!CURRENCY_CODE.matches(currency)) {
            throw ChatContextException(ChatContextError.INVALID_PREFERENCES)
        }
        return currency
    }

    private fun locale(language: String): Locale = when (language.trim().lowercase(Locale.ROOT)) {
        "indonesia" -> Locale.forLanguageTag("id-ID")
        "english" -> Locale.forLanguageTag("en-US")
        else -> throw ChatContextException(ChatContextError.INVALID_PREFERENCES)
    }

    private data class ChatPreferences(
        val currency: String,
        val locale: Locale
    )

    private companion object {
        val CURRENCY_CODE = Regex("[A-Z]{3}")
    }
}
