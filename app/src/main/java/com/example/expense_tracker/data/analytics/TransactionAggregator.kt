package com.example.expense_tracker.data.analytics

import com.example.expense_tracker.data.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

class TransactionAggregator(
    private val source: FinancialSummarySource,
    private val periodResolver: FinancialPeriodResolver = FinancialPeriodResolver(),
    private val calculationDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    suspend fun createSnapshot(): FinancialSummary = withContext(calculationDispatcher) {
        val periods = periodResolver.resolve()
        val queryStart = minOf(
            periods.currentWeek.startInclusiveEpochMillis,
            periods.currentMonth.startInclusiveEpochMillis,
            periods.previousMonth.startInclusiveEpochMillis
        )
        val entries = source.loadEntries(queryStart, periods.snapshotEpochMillis)
        coroutineContext.ensureActive()

        try {
            val currentWeek = aggregate(entries, periods.currentWeek)
            val currentMonth = aggregate(entries, periods.currentMonth)
            val previousMonth = aggregate(entries, periods.previousMonth)

            FinancialSummary(
                snapshotEpochMillis = periods.snapshotEpochMillis,
                zoneId = periods.zoneId.id,
                currentWeek = currentWeek,
                currentMonth = currentMonth,
                previousMonth = previousMonth,
                monthComparison = compare(currentMonth, previousMonth)
            )
        } catch (exception: ArithmeticException) {
            throw FinancialAggregationException(
                message = "Financial amount exceeds the supported range",
                cause = exception
            )
        }
    }

    private fun aggregate(
        entries: List<FinancialTransactionEntry>,
        period: FinancialPeriod
    ): PeriodExpenseSummary {
        var totalExpense = 0L
        val totalsByCategory = linkedMapOf<Long, MutableCategoryTotal>()

        entries.forEach { entry ->
            if (shouldIncludeEntry(entry, period)) {
                require(entry.amount > 0L) { "Expense entries must have a positive amount" }
                totalExpense = Math.addExact(totalExpense, entry.amount)
                val category = totalsByCategory.getOrPut(entry.categoryId) {
                    MutableCategoryTotal(entry.categoryId, entry.categoryName)
                }
                category.totalAmount = Math.addExact(category.totalAmount, entry.amount)
            }
        }

        val categories = totalsByCategory.values
            .map { it.toImmutable() }
            .sortedWith(
                compareByDescending<CategoryExpenseTotal> { it.totalAmount }
                    .thenBy { it.categoryId }
            )

        return PeriodExpenseSummary(
            period = period,
            totalExpense = totalExpense,
            categories = categories
        )
    }

    private fun shouldIncludeEntry(
        entry: FinancialTransactionEntry,
        period: FinancialPeriod
    ): Boolean =
        entry.type == TransactionType.EXPENSE.name &&
        entry.timestamp >= period.startInclusiveEpochMillis &&
        entry.timestamp < period.endExclusiveEpochMillis

    private fun compare(
        currentMonth: PeriodExpenseSummary,
        previousMonth: PeriodExpenseSummary
    ): ExpenseComparison {
        val difference = Math.subtractExact(
            currentMonth.totalExpense,
            previousMonth.totalExpense
        )
        val percentage = previousMonth.totalExpense
            .takeUnless { it == 0L }
            ?.let { previousTotal ->
                BigDecimal.valueOf(difference)
                    .multiply(ONE_HUNDRED)
                    .divide(BigDecimal.valueOf(previousTotal), PERCENTAGE_SCALE, RoundingMode.HALF_UP)
                    .stripTrailingZeros()
            }

        return ExpenseComparison(
            amountDifference = difference,
            percentageChange = percentage
        )
    }

    private data class MutableCategoryTotal(
        val categoryId: Long,
        val categoryName: String,
        var totalAmount: Long = 0L
    ) {
        fun toImmutable() = CategoryExpenseTotal(
            categoryId = categoryId,
            categoryName = categoryName,
            totalAmount = totalAmount
        )
    }

    private companion object {
        val ONE_HUNDRED: BigDecimal = BigDecimal.valueOf(100L)
        const val PERCENTAGE_SCALE = 2
    }
}
