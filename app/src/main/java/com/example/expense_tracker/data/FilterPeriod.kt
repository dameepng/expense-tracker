package com.example.expense_tracker.data

import androidx.annotation.StringRes
import com.example.expense_tracker.R

enum class FilterPeriod(@StringRes val labelResId: Int) {
    TODAY(R.string.filter_today),
    WEEK(R.string.filter_week),
    MONTH(R.string.filter_month),
    CUSTOM(R.string.filter_custom)
}
