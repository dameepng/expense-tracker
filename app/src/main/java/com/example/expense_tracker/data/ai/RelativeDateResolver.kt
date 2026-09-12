package com.example.expense_tracker.data.ai

import java.time.LocalDate

internal object RelativeDateResolver {
    private val compoundRelativeDate = Regex(
        "\\b(?:(?:minggu|pekan|bulan|tahun|senin|selasa|rabu|kamis|jumat|sabtu|ahad|sunday|monday|tuesday|wednesday|thursday|friday|saturday)\\s+(?:kemarin|tadi|besok|yesterday|tomorrow)" +
            "|(?:\\d+|satu|dua|tiga|empat|lima|enam|tujuh|delapan|sembilan|sepuluh)\\s+(?:hari|minggu|pekan|bulan|tahun)\\s+(?:kemarin|tadi|besok))\\b",
        RegexOption.IGNORE_CASE
    )
    private val absoluteDate = Regex(
        "\\b(?:\\d{4}-\\d{1,2}-\\d{1,2}|\\d{1,2}[/-]\\d{1,2}(?:[/-]\\d{2,4})?" +
            "|\\d{1,2}\\s+(?:januari|februari|maret|april|mei|juni|juli|agustus|september|oktober|november|desember))\\b",
        RegexOption.IGNORE_CASE
    )
    private val expressions = listOf(
        Regex("\\b(?:kemarin|yesterday)\\b", RegexOption.IGNORE_CASE) to -1L,
        Regex("\\b(?:hari ini|tadi(?: pagi| siang| sore| malam)?|today)\\b", RegexOption.IGNORE_CASE) to 0L,
        Regex("\\b(?:besok|tomorrow)\\b", RegexOption.IGNORE_CASE) to 1L,
        Regex("\\blusa\\b", RegexOption.IGNORE_CASE) to 2L
    )

    /** Only override the model when there is one unambiguous relative day. */
    fun resolve(text: String, referenceDate: LocalDate): LocalDate? {
        if (absoluteDate.containsMatchIn(text) || compoundRelativeDate.containsMatchIn(text)) return null
        val offsets = expressions.filter { (expression, _) -> expression.containsMatchIn(text) }
            .map { it.second }.distinct()
        return offsets.singleOrNull()?.let(referenceDate::plusDays)
    }
}
