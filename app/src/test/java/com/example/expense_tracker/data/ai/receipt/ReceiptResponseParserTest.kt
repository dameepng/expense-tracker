package com.example.expense_tracker.data.ai.receipt

import com.example.expense_tracker.data.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ReceiptResponseParserTest {
    private val categories = listOf(Category(7, "Makanan", "EXPENSE"), Category(8, "Gaji", "INCOME"))
    private val parser = ReceiptResponseParser()
    private fun valid(items: String = "[]") = """{"amount":1,"category":"Makanan","merchant":"X","date":"2026-01-01","note":"n","items":$items,"is_recurring":false}"""

    @Test fun parsesFixtureAndMapsCategoryId() {
        val result = parser.parse("""{"amount":47500,"category":"Makanan","merchant":"Indomaret","date":"2026-09-13","note":"belanja bulanan","items":["Indomie 2pcs","Teh Botol"],"is_recurring":false}""", categories)
        assertEquals(47500, result.transaction.amount); assertEquals(7, result.transaction.categoryId)
        assertEquals(listOf("Indomie 2pcs", "Teh Botol"), result.items)
    }

    @Test fun parsesFencedJsonAndWhitespace() = assertEquals(1, parser.parse("  ```json\n${valid()}\n``` ", categories).transaction.amount)

    @Test fun rejectsUnknownFieldDuplicateAndInvalidValues() {
        listOf(
            valid().dropLast(1) + ",\"extra\":1}",
            """{"amount":1,"amount":2,"category":"Makanan","merchant":"X","date":"2026-01-01","note":"n","items":[],"is_recurring":false}""",
            valid().replace("\"amount\":1", "\"amount\":0"), valid().replace("2026-01-01", "2026-02-30")
        ).forEach { assertThrows(ReceiptParseException::class.java) { parser.parse(it, categories) } }
    }

    @Test fun rejectsWrongCategoryTypeAndTooManyItems() {
        assertThrows(ReceiptParseException::class.java) { parser.parse(valid().replace("Makanan", "Gaji"), categories) }
        val items = (1..101).joinToString(",") { "\"x\"" }
        assertThrows(ReceiptParseException::class.java) { parser.parse(valid("[$items]"), categories) }
    }

    @Test fun mapsSupportedErrorsAndRejectsPartialError() {
        assertEquals(ReceiptParseError.UNCLEAR_RECEIPT, assertThrows(ReceiptParseException::class.java) { parser.parse("""{"error":"unclear_receipt"}""", categories) }.reason)
        assertThrows(ReceiptParseException::class.java) { parser.parse("""{"error":"not_a_receipt","amount":1}""", categories) }
    }
}
