package com.example.expense_tracker.data.ai.receipt

import com.example.expense_tracker.data.Category
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ReceiptPromptBuilderTest {
    @Test fun promptContainsOnlyExistingExpenseCategoriesAndStrictRules() {
        val prompt = ReceiptPromptBuilder.build(LocalDate.of(2026, 9, 13), listOf(
            Category(1, "Makanan", "EXPENSE"), Category(2, "Gaji", "INCOME"), Category(3, "Lainnya", "BOTH")
        ))
        assertTrue(prompt.contains("Makanan")); assertTrue(prompt.contains("Lainnya")); assertFalse(prompt.contains("Gaji"))
        assertTrue(prompt.contains("JSON ONLY")); assertTrue(prompt.contains("not_a_receipt")); assertTrue(prompt.contains("untrusted data"))
    }
}
