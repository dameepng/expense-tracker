package com.example.expense_tracker.data.analytics

import java.math.BigDecimal
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class TransactionAggregatorTest {
    @Test
    fun `aggregates only expenses and keeps equal category names separated by id`() = runBlocking {
        val entries = listOf(
            entry(30_000L, 1L, "Makanan", "2026-09-10T08:00:00Z"),
            entry(20_000L, 1L, "Makanan", "2026-09-10T09:00:00Z"),
            entry(7_000L, 2L, "Makanan", "2026-09-10T10:00:00Z"),
            entry(100_000L, 3L, "Gaji", "2026-09-10T11:00:00Z", "INCOME"),
            entry(80_000L, 4L, "Unknown", "2026-09-10T12:00:00Z", "TRANSFER")
        )

        val summary = aggregator(entries).createSnapshot()

        assertEquals(57_000L, summary.currentMonth.totalExpense)
        assertEquals(2, summary.currentMonth.categories.size)
        assertEquals(1L, summary.currentMonth.categories[0].categoryId)
        assertEquals(50_000L, summary.currentMonth.categories[0].totalAmount)
        assertEquals(2L, summary.currentMonth.categories[1].categoryId)
        assertEquals(7_000L, summary.currentMonth.categories[1].totalAmount)
        assertEquals(
            summary.currentMonth.totalExpense,
            summary.currentMonth.categories.sumOf { it.totalAmount }
        )
    }

    @Test
    fun `uses inclusive start and exclusive snapshot boundaries`() = runBlocking {
        val snapshot = Instant.parse("2026-09-12T10:00:00Z")
        val monthStart = "2026-09-01T00:00:00Z"
        val entries = listOf(
            entry(10L, 1L, "Food", monthStart),
            entry(20L, 1L, "Food", "2026-09-12T09:59:59.999Z"),
            entry(30L, 1L, "Food", snapshot.toString()),
            entry(40L, 1L, "Food", "2026-09-13T00:00:00Z")
        )

        val summary = aggregator(entries, snapshot).createSnapshot()

        assertEquals(30L, summary.currentMonth.totalExpense)
    }

    @Test
    fun `compares current month with full previous calendar month`() = runBlocking {
        val entries = listOf(
            entry(30_000L, 1L, "Food", "2026-09-05T08:00:00Z"),
            entry(20_000L, 1L, "Food", "2026-09-10T08:00:00Z"),
            entry(40_000L, 1L, "Food", "2026-08-20T08:00:00Z")
        )

        val summary = aggregator(entries).createSnapshot()

        assertEquals(50_000L, summary.currentMonth.totalExpense)
        assertEquals(40_000L, summary.previousMonth.totalExpense)
        assertEquals(10_000L, summary.monthComparison.amountDifference)
        assertEquals(0, BigDecimal("25").compareTo(summary.monthComparison.percentageChange))
        assertFalse(summary.currentMonth.period.isComplete)
        assertEquals(FinancialPeriodKind.CURRENT_MONTH, summary.currentMonth.period.kind)
        assertEquals(FinancialPeriodKind.PREVIOUS_MONTH, summary.previousMonth.period.kind)
        assertEquals(
            Instant.parse("2026-08-01T00:00:00Z").toEpochMilli(),
            summary.previousMonth.period.startInclusiveEpochMillis
        )
        assertEquals(
            Instant.parse("2026-09-01T00:00:00Z").toEpochMilli(),
            summary.previousMonth.period.endExclusiveEpochMillis
        )
    }

    @Test
    fun `returns empty summaries and no percentage when previous total is zero`() = runBlocking {
        val summary = aggregator(emptyList()).createSnapshot()

        assertEquals(0L, summary.currentWeek.totalExpense)
        assertEquals(0L, summary.currentMonth.totalExpense)
        assertEquals(0L, summary.previousMonth.totalExpense)
        assertEquals(emptyList<CategoryExpenseTotal>(), summary.currentMonth.categories)
        assertEquals(0L, summary.monthComparison.amountDifference)
        assertNull(summary.monthComparison.percentageChange)
    }

    @Test
    fun `reports overflow instead of wrapping totals`() {
        val entries = listOf(
            entry(Long.MAX_VALUE, 1L, "Food", "2026-09-10T08:00:00Z"),
            entry(1L, 1L, "Food", "2026-09-10T09:00:00Z")
        )

        assertThrows(FinancialAggregationException::class.java) {
            runBlocking { aggregator(entries).createSnapshot() }
        }
    }

    @Test
    fun `propagates source cancellation`() {
        val source = object : FinancialSummarySource {
            override suspend fun loadEntries(
                startInclusiveEpochMillis: Long,
                endExclusiveEpochMillis: Long
            ): List<FinancialTransactionEntry> = throw CancellationException("cancelled")
        }
        val aggregator = TransactionAggregator(
            source = source,
            periodResolver = resolver(DEFAULT_SNAPSHOT),
            calculationDispatcher = Dispatchers.Unconfined
        )

        assertThrows(CancellationException::class.java) {
            runBlocking { aggregator.createSnapshot() }
        }
    }

    @Test
    fun `week start follows the supplied locale`() {
        val snapshot = Instant.parse("2026-09-09T12:00:00Z")
        val clock = Clock.fixed(snapshot, ZoneId.of("UTC"))
        val usPeriods = FinancialPeriodResolver(
            clock = clock,
            zoneId = ZoneId.of("UTC"),
            locale = Locale.US
        ).resolve()
        val francePeriods = FinancialPeriodResolver(
            clock = clock,
            zoneId = ZoneId.of("UTC"),
            locale = Locale.FRANCE
        ).resolve()

        assertEquals(
            Instant.parse("2026-09-06T00:00:00Z").toEpochMilli(),
            usPeriods.currentWeek.startInclusiveEpochMillis
        )
        assertEquals(
            Instant.parse("2026-09-07T00:00:00Z").toEpochMilli(),
            francePeriods.currentWeek.startInclusiveEpochMillis
        )
    }

    @Test
    fun `resolves leap month year rollover and timezone calendar boundaries`() {
        val jakarta = ZoneId.of("Asia/Jakarta")
        val leapPeriods = resolver(
            snapshot = Instant.parse("2024-03-15T05:00:00Z"),
            zoneId = jakarta
        ).resolve()
        assertEquals(
            Instant.parse("2024-01-31T17:00:00Z").toEpochMilli(),
            leapPeriods.previousMonth.startInclusiveEpochMillis
        )
        assertEquals(
            Instant.parse("2024-02-29T17:00:00Z").toEpochMilli(),
            leapPeriods.previousMonth.endExclusiveEpochMillis
        )

        val yearRollover = resolver(
            snapshot = Instant.parse("2026-01-10T05:00:00Z"),
            zoneId = jakarta
        ).resolve()
        assertEquals(
            Instant.parse("2025-11-30T17:00:00Z").toEpochMilli(),
            yearRollover.previousMonth.startInclusiveEpochMillis
        )
        assertEquals(
            Instant.parse("2025-12-31T17:00:00Z").toEpochMilli(),
            yearRollover.previousMonth.endExclusiveEpochMillis
        )
    }

    @Test
    fun `uses real start of day across daylight saving offset changes`() {
        val zone = ZoneId.of("America/New_York")
        val periods = resolver(
            snapshot = Instant.parse("2024-03-10T16:00:00Z"),
            zoneId = zone,
            firstDayOfWeek = DayOfWeek.SUNDAY
        ).resolve()

        assertEquals(
            Instant.parse("2024-03-10T05:00:00Z").toEpochMilli(),
            periods.currentWeek.startInclusiveEpochMillis
        )
        assertEquals(11L * 60L * 60L * 1_000L, periods.snapshotEpochMillis - periods.currentWeek.startInclusiveEpochMillis)
    }

    private fun aggregator(
        entries: List<FinancialTransactionEntry>,
        snapshot: Instant = DEFAULT_SNAPSHOT
    ): TransactionAggregator = TransactionAggregator(
        source = FakeFinancialSummarySource(entries),
        periodResolver = resolver(snapshot),
        calculationDispatcher = Dispatchers.Unconfined
    )

    private fun resolver(
        snapshot: Instant,
        zoneId: ZoneId = ZoneId.of("UTC"),
        locale: Locale = Locale.US,
        firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
    ) = FinancialPeriodResolver(
        clock = Clock.fixed(snapshot, zoneId),
        zoneId = zoneId,
        locale = locale,
        firstDayOfWeek = firstDayOfWeek
    )

    private fun entry(
        amount: Long,
        categoryId: Long,
        categoryName: String,
        timestamp: String,
        type: String = "EXPENSE"
    ) = FinancialTransactionEntry(
        amount = amount,
        categoryId = categoryId,
        categoryName = categoryName,
        timestamp = Instant.parse(timestamp).toEpochMilli(),
        type = type
    )

    private class FakeFinancialSummarySource(
        private val entries: List<FinancialTransactionEntry>
    ) : FinancialSummarySource {
        override suspend fun loadEntries(
            startInclusiveEpochMillis: Long,
            endExclusiveEpochMillis: Long
        ): List<FinancialTransactionEntry> = entries
    }

    private companion object {
        val DEFAULT_SNAPSHOT: Instant = Instant.parse("2026-09-12T10:00:00Z")
    }
}
