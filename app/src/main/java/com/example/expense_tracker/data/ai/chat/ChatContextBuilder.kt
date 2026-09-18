package com.example.expense_tracker.data.ai.chat

import com.example.expense_tracker.data.analytics.CategoryExpenseTotal
import com.example.expense_tracker.data.analytics.ExpenseComparison
import com.example.expense_tracker.data.analytics.FinancialPeriodKind
import com.example.expense_tracker.data.analytics.PeriodExpenseSummary
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneId

internal class ChatContextBuilder(
    private val maxContextCharacters: Int = MAX_CONTEXT_CHARACTERS,
    private val gson: Gson = Gson()
) {
    init {
        require(maxContextCharacters > 0) { "Context character limit must be positive" }
    }

    fun build(context: ChatContext): String {
        validate(context)
        val root = JsonObject().apply {
            addProperty("schema_version", SCHEMA_VERSION)
            addProperty("data_status", context.dataStatus())
            addProperty("snapshot_at", context.snapshotEpochMillis.toIsoString(context.zoneId))
            addProperty("timezone", context.zoneId)
            addProperty("preferred_locale", context.preferredLocale)
            addProperty("currency", context.currency)
            addProperty("amount_unit", "whole_currency_units")
            addProperty("fx_conversion_applied", false)
            addProperty("wallet_scope", context.walletScope.wireValue)
            add("available_periods", JsonArray().apply {
                add(FinancialPeriodKind.CURRENT_WEEK.wireValue)
                add(FinancialPeriodKind.CURRENT_MONTH.wireValue)
                add(FinancialPeriodKind.PREVIOUS_MONTH.wireValue)
            })
            addProperty(
                "category_breakdown_coverage",
                "all_non_zero_expense_categories; omitted categories have zero recorded expense in that period"
            )
            add("current_week", context.currentWeek.toJson(context.zoneId))
            add("current_month", context.currentMonth.toJson(context.zoneId))
            add("previous_month", context.previousMonth.toJson(context.zoneId))
            add("month_comparison", context.monthComparison.toJson())
        }

        val serialized = gson.toJson(root)
        if (serialized.length > maxContextCharacters) {
            throw ChatContextException(ChatContextError.CONTEXT_TOO_LARGE)
        }
        return serialized
    }

    private fun validate(context: ChatContext) {
        try {
            ZoneId.of(context.zoneId)
            require(CURRENCY_CODE.matches(context.currency))
            require(LOCALE_TAG.matches(context.preferredLocale))
            require(context.currentWeek.period.kind == FinancialPeriodKind.CURRENT_WEEK)
            require(context.currentMonth.period.kind == FinancialPeriodKind.CURRENT_MONTH)
            require(context.previousMonth.period.kind == FinancialPeriodKind.PREVIOUS_MONTH)
            require(!context.currentWeek.period.isComplete)
            require(!context.currentMonth.period.isComplete)
            require(context.previousMonth.period.isComplete)
            require(context.currentWeek.period.endExclusiveEpochMillis == context.snapshotEpochMillis)
            require(context.currentMonth.period.endExclusiveEpochMillis == context.snapshotEpochMillis)
            require(context.previousMonth.period.endExclusiveEpochMillis == context.currentMonth.period.startInclusiveEpochMillis)
            validateSummary(context.currentWeek)
            validateSummary(context.currentMonth)
            validateSummary(context.previousMonth)

            val expectedDifference = Math.subtractExact(
                context.currentMonth.totalExpense,
                context.previousMonth.totalExpense
            )
            require(context.monthComparison.amountDifference == expectedDifference)
            require(
                (context.previousMonth.totalExpense == 0L) ==
                    (context.monthComparison.percentageChange == null)
            )
            val expectedPercentage = context.previousMonth.totalExpense
                .takeUnless { it == 0L }
                ?.let { previousTotal ->
                    BigDecimal.valueOf(expectedDifference)
                        .multiply(ONE_HUNDRED)
                        .divide(BigDecimal.valueOf(previousTotal), PERCENTAGE_SCALE, RoundingMode.HALF_UP)
                        .stripTrailingZeros()
                }
            val actualPercentage = context.monthComparison.percentageChange
            require(
                (expectedPercentage == null && actualPercentage == null) ||
                    (expectedPercentage != null && actualPercentage != null &&
                        expectedPercentage.compareTo(actualPercentage) == 0)
            )
        } catch (failure: ChatContextException) {
            throw failure
        } catch (_: Exception) {
            throw ChatContextException(ChatContextError.INVALID_CONTEXT)
        }
    }

    private fun validateSummary(summary: PeriodExpenseSummary) {
        var categoryTotal = 0L
        val categoryIds = mutableSetOf<Long>()
        summary.categories.forEach { category ->
            require(categoryIds.add(category.categoryId))
            require(category.totalAmount > 0L)
            categoryTotal = Math.addExact(categoryTotal, category.totalAmount)
        }
        require(categoryTotal == summary.totalExpense)
    }

    private fun PeriodExpenseSummary.toJson(zoneId: String): JsonObject = JsonObject().apply {
        addProperty("period", period.kind.wireValue)
        addProperty("period_status", if (period.isComplete) "complete" else "to_snapshot")
        addProperty("data_status", if (totalExpense == 0L) "empty" else "available")
        addProperty("start_inclusive", period.startInclusiveEpochMillis.toIsoString(zoneId))
        addProperty("end_exclusive", period.endExclusiveEpochMillis.toIsoString(zoneId))
        addProperty("total_expense", totalExpense)
        add("categories", JsonArray().apply {
            categories
                .sortedWith(CATEGORY_ORDER)
                .forEach { category -> add(category.toJson()) }
        })
    }

    private fun CategoryExpenseTotal.toJson(): JsonObject = JsonObject().apply {
        addProperty("category_id", categoryId)
        addProperty("category_name", categoryName)
        addProperty("total_expense", totalAmount)
    }

    private fun ExpenseComparison.toJson(): JsonObject = JsonObject().apply {
        addProperty("amount_difference", amountDifference)
        val percentage = percentageChange
        if (percentage == null) {
            addProperty("percentage_status", "not_available_no_previous_expense")
        } else {
            addProperty("percentage_change", percentage)
            addProperty("percentage_status", "available")
        }
    }

    private fun ChatContext.dataStatus(): String =
        if (currentWeek.totalExpense == 0L &&
            currentMonth.totalExpense == 0L &&
            previousMonth.totalExpense == 0L
        ) {
            "empty"
        } else {
            "available"
        }

    private fun Long.toIsoString(zoneId: String): String =
        Instant.ofEpochMilli(this).atZone(ZoneId.of(zoneId)).toOffsetDateTime().toString()

    private val FinancialPeriodKind.wireValue: String
        get() = name.lowercase()

    companion object {
        const val MAX_CONTEXT_CHARACTERS = 16_000
        private const val SCHEMA_VERSION = 1
        private val CURRENCY_CODE = Regex("[A-Z]{3}")
        private val LOCALE_TAG = Regex("[a-z]{2}-[A-Z]{2}")
        private val CATEGORY_ORDER = compareByDescending<CategoryExpenseTotal> { it.totalAmount }
            .thenBy { it.categoryId }
        private val ONE_HUNDRED: BigDecimal = BigDecimal.valueOf(100L)
        private const val PERCENTAGE_SCALE = 2
    }
}
