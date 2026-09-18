package com.example.expense_tracker.ui.nfc

import com.example.expense_tracker.data.nfc.NfcCardResult
import com.example.expense_tracker.data.nfc.NfcCardType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NfcQuickScanTest {

    @Test
    fun `createSimulatedCard returns expected card result for Mandiri e-Money`() {
        val fixedTime = 1700000000000L
        val card = createSimulatedCard(NfcCardType.MANDIRI_EMONEY, fixedTime)

        assertEquals("6032918239014812", card.cardNumber)
        assertEquals(74_500L, card.balance)
        assertEquals(NfcCardType.MANDIRI_EMONEY, card.cardType)
        assertEquals(4, card.transactions.size)
        assertEquals("Gerbang Tol Cilandak Utama", card.transactions[0].terminalId)
        assertEquals(15_000L, card.transactions[0].amount)
        assertEquals("EXPENSE", card.transactions[0].type)
    }

    @Test
    fun `createSimulatedCard returns expected card result for BNI TapCash`() {
        val fixedTime = 1700000000000L
        val card = createSimulatedCard(NfcCardType.BNI_TAPCASH, fixedTime)

        assertEquals("7546029381729401", card.cardNumber)
        assertEquals(125_000L, card.balance)
        assertEquals(NfcCardType.BNI_TAPCASH, card.cardType)
        assertEquals(3, card.transactions.size)
        assertEquals("KRL Manggarai - Bogor", card.transactions[0].terminalId)
        assertEquals(8_000L, card.transactions[0].amount)
        assertEquals("EXPENSE", card.transactions[0].type)
    }

    @Test
    fun `createSimulatedCard returns default result for unknown card type`() {
        val card = createSimulatedCard(NfcCardType.UNKNOWN)

        assertEquals("9988776655443322", card.cardNumber)
        assertEquals(50_000L, card.balance)
        assertEquals(NfcCardType.UNKNOWN, card.cardType)
        assertTrue(card.transactions.isEmpty())
    }

    @Test
    fun `formatNfcTransactionTime formats timestamp in expected Indonesian format`() {
        val timestamp = 1700000000000L
        val expected = SimpleDateFormat("dd MMM, HH:mm", Locale.forLanguageTag("id-ID")).format(Date(timestamp))
        val actual = formatNfcTransactionTime(timestamp)

        assertEquals(expected, actual)
    }

    @Test
    fun `NfcScanState handles all state variations`() {
        val scanningState: NfcScanState = NfcScanState.Scanning
        assertTrue(scanningState is NfcScanState.Scanning)

        val dummyCard = NfcCardResult("1234", 1000L, NfcCardType.UNKNOWN)
        val successState: NfcScanState = NfcScanState.Success(dummyCard)
        assertEquals(dummyCard, (successState as NfcScanState.Success).card)

        val errorState: NfcScanState = NfcScanState.Error("NFC Error")
        assertEquals("NFC Error", (errorState as NfcScanState.Error).message)
    }
}
