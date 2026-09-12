package com.example.expense_tracker.data.ai.chat

import com.example.expense_tracker.data.UserPreferencesRepository
import com.example.expense_tracker.data.analytics.FinancialSummarySource
import com.example.expense_tracker.data.analytics.FinancialTransactionEntry
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ChatContextProviderTest {
    @Test
    fun `loads fresh aggregate and current preferences on every request`() = runBlocking {
        val entries = mutableListOf(
            entry(10_000L, "2026-09-05T08:00:00Z")
        )
        val preferences = FakeUserPreferencesRepository()
        val provider = provider(
            source = sourceOf { _, _ -> entries.toList() },
            preferences = preferences
        )

        val first = provider.loadContext()
        entries += entry(15_000L, "2026-09-10T08:00:00Z")
        preferences.currency.value = "usd"
        preferences.language.value = "English"
        val second = provider.loadContext()

        assertEquals(10_000L, first.currentMonth.totalExpense)
        assertEquals("IDR", first.currency)
        assertEquals("id-ID", first.preferredLocale)
        assertEquals(
            Instant.parse("2026-09-06T00:00:00Z").toEpochMilli(),
            first.currentWeek.period.startInclusiveEpochMillis
        )
        assertEquals(25_000L, second.currentMonth.totalExpense)
        assertEquals("USD", second.currency)
        assertEquals("en-US", second.preferredLocale)
        assertEquals(
            Instant.parse("2026-09-06T00:00:00Z").toEpochMilli(),
            second.currentWeek.period.startInclusiveEpochMillis
        )
        assertEquals(WalletScope.ALL_WALLETS, second.walletScope)
    }

    @Test
    fun `keeps successful empty snapshot distinct from data failure`() = runBlocking {
        val empty = provider(sourceOf { _, _ -> emptyList() }).loadContext()
        assertEquals(0L, empty.currentMonth.totalExpense)

        val failure = assertThrows(ChatContextException::class.java) {
            runBlocking {
                provider(sourceOf { _, _ -> throw IOException("database details") })
                    .loadContext()
            }
        }
        assertEquals(ChatContextError.DATA_UNAVAILABLE, failure.reason)
        assertEquals(ChatContextError.DATA_UNAVAILABLE.name, failure.message)
    }

    @Test
    fun `reports invalid stored preferences without replacing them silently`() {
        val preferences = FakeUserPreferencesRepository().apply {
            currency.value = "not-a-currency"
        }

        val failure = assertThrows(ChatContextException::class.java) {
            runBlocking { provider(preferences = preferences).loadContext() }
        }

        assertEquals(ChatContextError.INVALID_PREFERENCES, failure.reason)
    }

    private fun provider(
        source: FinancialSummarySource = sourceOf { _, _ -> emptyList() },
        preferences: FakeUserPreferencesRepository = FakeUserPreferencesRepository()
    ): ChatContextProvider = ChatContextProvider(
        financialSummarySource = source,
        userPreferencesRepository = preferences,
        clockProvider = { Clock.fixed(SNAPSHOT, ZONE_ID) },
        calculationDispatcher = Dispatchers.Unconfined
    )

    private fun sourceOf(
        loader: suspend (startInclusiveEpochMillis: Long, endExclusiveEpochMillis: Long) ->
            List<FinancialTransactionEntry>
    ) = object : FinancialSummarySource {
        override suspend fun loadEntries(
            startInclusiveEpochMillis: Long,
            endExclusiveEpochMillis: Long
        ): List<FinancialTransactionEntry> = loader(
            startInclusiveEpochMillis,
            endExclusiveEpochMillis
        )
    }

    private fun entry(amount: Long, timestamp: String) = FinancialTransactionEntry(
        amount = amount,
        categoryId = 1L,
        categoryName = "Makanan",
        timestamp = Instant.parse(timestamp).toEpochMilli(),
        type = "EXPENSE"
    )

    private class FakeUserPreferencesRepository : UserPreferencesRepository {
        val currency = MutableStateFlow("IDR")
        val language = MutableStateFlow("Indonesia")

        override val selectedWalletIdFlow = MutableStateFlow<Long?>(null)
        override val themeModeFlow = MutableStateFlow("System Default")
        override val currencyFlow: Flow<String> = currency
        override val languageFlow: Flow<String> = language
        override val isBiometricsEnabledFlow = MutableStateFlow(false)
        override val userNameFlow = MutableStateFlow("User")
        override val userPhotoUriFlow = MutableStateFlow<String?>(null)

        override suspend fun saveSelectedWalletId(walletId: Long?) = Unit
        override suspend fun saveThemeMode(mode: String) = Unit
        override suspend fun saveCurrency(currency: String) = Unit
        override suspend fun saveLanguage(language: String) = Unit
        override suspend fun saveBiometricsEnabled(enabled: Boolean) = Unit
        override suspend fun saveUserProfile(name: String, photoUri: String?) = Unit
        override suspend fun clearAllPreferences() = Unit
    }

    private companion object {
        val SNAPSHOT: Instant = Instant.parse("2026-09-12T10:00:00Z")
        val ZONE_ID: ZoneId = ZoneId.of("UTC")
    }
}
