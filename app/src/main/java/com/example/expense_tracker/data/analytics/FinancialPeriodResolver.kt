package com.example.expense_tracker.data.analytics

import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * Encapsulates the resolved financial periods for analytics snapshot calculation.
 */
data class FinancialPeriods(
    val snapshotEpochMillis: Long,
    val zoneId: ZoneId,
    val currentWeek: FinancialPeriod,
    val currentMonth: FinancialPeriod,
    val previousMonth: FinancialPeriod
)

/**
 * Resolves running and previous financial periods based on device clock and timezone.
 * Extracted from TransactionAggregator to adhere to Single Responsibility.
 */
class FinancialPeriodResolver(
    private val clock: Clock = Clock.systemDefaultZone(),
    private val zoneId: ZoneId = clock.zone,
    locale: Locale = Locale.getDefault(),
    private val firstDayOfWeek: DayOfWeek = WeekFields.of(locale).firstDayOfWeek
) {
    fun resolve(): FinancialPeriods {
        val snapshot = clock.instant()
        val snapshotEpochMillis = snapshot.toEpochMilli()
        val currentDate = snapshot.atZone(zoneId).toLocalDate()
        val currentMonthStartDate = currentDate.withDayOfMonth(1)
        val previousMonthStartDate = currentMonthStartDate.minusMonths(1)
        val currentWeekStartDate = currentDate.with(
            TemporalAdjusters.previousOrSame(firstDayOfWeek)
        )

        return FinancialPeriods(
            snapshotEpochMillis = snapshotEpochMillis,
            zoneId = zoneId,
            currentWeek = runningPeriod(
                FinancialPeriodKind.CURRENT_WEEK,
                currentWeekStartDate,
                snapshotEpochMillis
            ),
            currentMonth = runningPeriod(
                FinancialPeriodKind.CURRENT_MONTH,
                currentMonthStartDate,
                snapshotEpochMillis
            ),
            previousMonth = FinancialPeriod(
                kind = FinancialPeriodKind.PREVIOUS_MONTH,
                startInclusiveEpochMillis = startOfDay(previousMonthStartDate),
                endExclusiveEpochMillis = startOfDay(currentMonthStartDate),
                isComplete = true
            )
        )
    }

    private fun runningPeriod(
        kind: FinancialPeriodKind,
        startDate: LocalDate,
        snapshotEpochMillis: Long
    ) = FinancialPeriod(
        kind = kind,
        startInclusiveEpochMillis = startOfDay(startDate),
        endExclusiveEpochMillis = snapshotEpochMillis,
        isComplete = false
    )

    private fun startOfDay(date: LocalDate): Long =
        date.atStartOfDay(zoneId).toInstant().toEpochMilli()
}
