package com.example.expense_tracker.data.nfc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EmoneyIsoDepParserTest {

    @Test
    fun testHexConversion() {
        val hex = "00A4040008A00000000386980701"
        val bytes = EmoneyIsoDepParser.hexToBytes(hex)
        val roundtrip = EmoneyIsoDepParser.bytesToHex(bytes)
        assertEquals(hex, roundtrip)
    }

    @Test
    fun testStatusWordValidation() {
        val successResponseIso = EmoneyIsoDepParser.hexToBytes("000123049000")
        val successResponseDesfire = EmoneyIsoDepParser.hexToBytes("000001E84800000075460200026622569100")
        val successResponseDesfireMoreData = EmoneyIsoDepParser.hexToBytes("000001E848000000754602000266225691AF")
        val errorResponse = EmoneyIsoDepParser.hexToBytes("6A82")
        val tooShort = byteArrayOf(0x90.toByte())

        assertTrue(EmoneyIsoDepParser.isSuccess(successResponseIso))
        assertTrue(EmoneyIsoDepParser.isSuccess(successResponseDesfire))
        assertTrue(EmoneyIsoDepParser.isSuccess(successResponseDesfireMoreData))
        assertFalse(EmoneyIsoDepParser.isSuccess(errorResponse))
        assertFalse(EmoneyIsoDepParser.isSuccess(tooShort))
        assertFalse(EmoneyIsoDepParser.isSuccess(null))
    }

    @Test
    fun testMandiriBalanceParsing() {
        // Balance = Rp 74.500 = 0x00012304
        // Response format: 4 bytes balance + 0x90 0x00
        val response1 = EmoneyIsoDepParser.hexToBytes("000123049000")
        val balance1 = EmoneyIsoDepParser.parseMandiriBalance(response1)
        assertEquals(74_500L, balance1)

        // Balance = Rp 150.000 = 0x000249F0
        val response2 = EmoneyIsoDepParser.hexToBytes("000249F09000")
        val balance2 = EmoneyIsoDepParser.parseMandiriBalance(response2)
        assertEquals(150_000L, balance2)

        // Balance at secondary offset (bytes 4..7)
        val responseSecondary = EmoneyIsoDepParser.hexToBytes("FFFFFFFF000123049000")
        val balanceSecondary = EmoneyIsoDepParser.parseMandiriBalance(responseSecondary)
        assertEquals(74_500L, balanceSecondary)
    }

    @Test
    fun testTapCashDesfireBalanceAndCardNumberParsing() {
        // DESFire payload from TapCash APDU 90 32 03 00 00:
        // Byte 0..1: 00 00
        // Byte 2..4: Balance (24-bit big endian: 01 E8 48 = 125.000)
        // Byte 5..7: 00 00 00
        // Byte 8..15: Card number (8 bytes: 75 46 02 00 02 66 22 56)
        // Byte 16..17: SW 91 00
        val desfirePayload = EmoneyIsoDepParser.hexToBytes("000001E84800000075460200026622569100")
        val balance = EmoneyIsoDepParser.parseTapCashBalanceDesfire(desfirePayload)
        assertEquals(125_000L, balance)

        val cardNumber = EmoneyIsoDepParser.parseTapCashCardNumber(desfirePayload)
        assertEquals("7546020002662256", cardNumber)
    }

    @Test
    fun testTapCashFallbackBalanceParsing() {
        // Balance = Rp 125.000 = 0x0001E848
        val response = EmoneyIsoDepParser.hexToBytes("0001E8489000")
        val balance = EmoneyIsoDepParser.parseTapCashBalance(response)
        assertEquals(125_000L, balance)
    }

    @Test
    fun testCardNumberFormatting() {
        val result16 = NfcCardResult(
            cardNumber = "6032918239014812",
            balance = 50_000L,
            cardType = NfcCardType.MANDIRI_EMONEY
        )
        assertEquals("6032 •••• •••• 4812", result16.formattedCardNumber())

        val resultTapCash = NfcCardResult(
            cardNumber = "7546020002662256",
            balance = 125_000L,
            cardType = NfcCardType.BNI_TAPCASH
        )
        assertEquals("7546 •••• •••• 2256", resultTapCash.formattedCardNumber())
    }

    @Test
    fun testFormatUidAsCardNumber() {
        val uid = "A1B2C3D4"
        val formatted = EmoneyIsoDepParser.formatUidAsCardNumber(uid, "7546")
        assertEquals(16, formatted.length)
        assertTrue(formatted.startsWith("7546A1B2C3D4"))
    }
}
