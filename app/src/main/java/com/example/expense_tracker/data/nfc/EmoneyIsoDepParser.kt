package com.example.expense_tracker.data.nfc

import android.nfc.tech.IsoDep
import com.example.expense_tracker.data.TransactionType
import java.io.IOException

object EmoneyIsoDepParser {

    private const val MANDIRI_PAN_PREFIX = "6032"
    private const val TAPCASH_PAN_PREFIX = "7546"
    private const val MAX_CARD_BALANCE = 20_000_000L
    private const val MAX_TRANSACTION_AMOUNT = 10_000_000L
    private const val ISO_DEP_TIMEOUT_MS = 5000
    private const val ONE_HOUR_MS = 3600_000L

    // APDU Commands - Mandiri e-Money Gen 2
    private val APDU_SELECT_MANDIRI_AID_1 = hexToBytes("00A4040008A00000000386980701")
    private val APDU_SELECT_MANDIRI_AID_2 = hexToBytes("00A4040007A000000003869807")
    private val APDU_READ_MANDIRI_CARD_NUM = hexToBytes("00B2010400")
    private val APDU_READ_MANDIRI_BALANCE = hexToBytes("00B2010C00")

    // APDU Commands - BNI TapCash (MIFARE DESFire EV1 / Java Card)
    // AID 1 (Master DF): A0 00 42 4E 49 10 00 01 (42 4E 49 is ASCII "BNI")
    private val APDU_SELECT_TAPCASH_AID_1 = hexToBytes("00A4040008A000424E49100001")
    // AID 2 (Purse Sub-applet): A0 00 42 4E 49 99 99 99
    private val APDU_SELECT_TAPCASH_AID_2 = hexToBytes("00A4040008A000424E49999999")
    // TapCash DESFire Read Purse Data (balance & card number in one payload)
    private val APDU_READ_TAPCASH_DATA = hexToBytes("9032030000")
    // TapCash DESFire Read Cyclic Records File 0x04 (Transaction Log)
    private val APDU_READ_TAPCASH_RECORDS = hexToBytes("90BB040000000000")
    // Fallback ISO 7816 Read Record
    private val APDU_READ_TAPCASH_BALANCE_FALLBACK = hexToBytes("00B2010C00")

    /**
     * Reads and parses an ISO-DEP contactless card (Mandiri e-Money or BNI TapCash).
     */
    fun readCard(isoDep: IsoDep, uid: ByteArray): Result<NfcCardResult> {
        return try {
            if (!ensureConnected(isoDep)) {
                return Result.failure(IOException("Failed to connect to NFC card"))
            }
            isoDep.timeout = ISO_DEP_TIMEOUT_MS

            val uidHex = bytesToHex(uid)

            // 1. Try BNI TapCash
            val tapCashResult = tryReadTapCash(isoDep, uidHex)
            if (tapCashResult != null) {
                return Result.success(tapCashResult)
            }

            // 2. Try Mandiri e-Money
            val mandiriResult = tryReadMandiri(isoDep, uidHex)
            if (mandiriResult != null) {
                return Result.success(mandiriResult)
            }

            Result.failure(IllegalArgumentException("Kartu tidak didukung atau bukan e-Money/TapCash yang didukung"))
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            try {
                if (isoDep.isConnected) isoDep.close()
            } catch (_: IOException) {}
        }
    }

    private fun ensureConnected(isoDep: IsoDep): Boolean {
        if (!isoDep.isConnected) {
            try {
                isoDep.connect()
            } catch (_: Exception) {
                return false
            }
        }
        return true
    }

    private fun tryReadMandiri(isoDep: IsoDep, uidHex: String): NfcCardResult? {
        return try {
            if (!ensureConnected(isoDep)) return null
            if (!selectMandiriApplication(isoDep)) return null

            val cardNumber = readMandiriCardNumber(isoDep, uidHex)

            val balanceResp = try { isoDep.transceive(APDU_READ_MANDIRI_BALANCE) } catch (_: Exception) { null }
            if (!isSuccess(balanceResp) || balanceResp == null) return null

            val balance = parseMandiriBalance(balanceResp)
            val transactions = readMandiriTransactions(isoDep)

            NfcCardResult(
                cardNumber = cardNumber,
                balance = balance,
                cardType = NfcCardType.MANDIRI_EMONEY,
                transactions = transactions
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun selectMandiriApplication(isoDep: IsoDep): Boolean {
        val resp1 = try { isoDep.transceive(APDU_SELECT_MANDIRI_AID_1) } catch (_: Exception) { null }
        val resp = if (isSuccess(resp1)) {
            resp1
        } else {
            try { isoDep.transceive(APDU_SELECT_MANDIRI_AID_2) } catch (_: Exception) { null }
        }
        return isSuccess(resp)
    }

    private fun readMandiriCardNumber(isoDep: IsoDep, uidHex: String): String {
        var cardNumber = formatUidAsCardNumber(uidHex, MANDIRI_PAN_PREFIX)
        try {
            val cardNumResp = isoDep.transceive(APDU_READ_MANDIRI_CARD_NUM)
            if (isSuccess(cardNumResp) && cardNumResp.size >= 10) {
                val parsedNum = parseCardNumberBytes(cardNumResp)
                if (parsedNum != null && parsedNum.length >= 10) {
                    cardNumber = parsedNum
                }
            }
        } catch (_: Exception) {}
        return cardNumber
    }

    private fun readMandiriTransactions(isoDep: IsoDep): List<NfcCardTransaction> {
        val transactions = mutableListOf<NfcCardTransaction>()
        for (recordIndex in 1..5) {
            try {
                val cmd = byteArrayOf(0x00.toByte(), 0xB2.toByte(), recordIndex.toByte(), 0x1C.toByte(), 0x00.toByte())
                val txResp = isoDep.transceive(cmd)
                if (isSuccess(txResp) && txResp.size >= 12) {
                    val tx = parseTransactionRecord(txResp, recordIndex.toLong())
                    if (tx != null && tx.amount > 0) {
                        transactions.add(tx)
                    }
                } else {
                    break
                }
            } catch (_: Exception) {
                break
            }
        }
        return transactions
    }

    private fun tryReadTapCash(isoDep: IsoDep, uidHex: String): NfcCardResult? {
        return try {
            if (!ensureConnected(isoDep)) return null
            if (!selectTapCashApplication(isoDep)) return null

            val dataResp = try { isoDep.transceive(APDU_READ_TAPCASH_DATA) } catch (_: Exception) { null }
            if (dataResp != null && dataResp.size >= 16) {
                val balance = parseTapCashBalanceDesfire(dataResp)
                val cardNumber = parseTapCashCardNumber(dataResp) ?: formatUidAsCardNumber(uidHex, TAPCASH_PAN_PREFIX)
                val transactions = readTapCashTransactions(isoDep, dataResp)

                return NfcCardResult(
                    cardNumber = cardNumber,
                    balance = balance,
                    cardType = NfcCardType.BNI_TAPCASH,
                    transactions = transactions
                )
            }

            val fallbackResp = try { isoDep.transceive(APDU_READ_TAPCASH_BALANCE_FALLBACK) } catch (_: Exception) { null }
            if (fallbackResp != null && isSuccess(fallbackResp)) {
                val balance = parseTapCashBalance(fallbackResp)
                val cardNumber = formatUidAsCardNumber(uidHex, TAPCASH_PAN_PREFIX)
                return NfcCardResult(
                    cardNumber = cardNumber,
                    balance = balance,
                    cardType = NfcCardType.BNI_TAPCASH
                )
            }

            null
        } catch (_: Exception) {
            null
        }
    }

    private fun selectTapCashApplication(isoDep: IsoDep): Boolean {
        val resp1 = try { isoDep.transceive(APDU_SELECT_TAPCASH_AID_1) } catch (_: Exception) { null }
        if (isSuccess(resp1)) {
            val resp2 = try { isoDep.transceive(APDU_SELECT_TAPCASH_AID_2) } catch (_: Exception) { null }
            if (!isSuccess(resp2)) {
                try { isoDep.transceive(APDU_SELECT_TAPCASH_AID_1) } catch (_: Exception) {}
            }
            return true
        }
        val resp2 = try { isoDep.transceive(APDU_SELECT_TAPCASH_AID_2) } catch (_: Exception) { null }
        return isSuccess(resp2)
    }

    private fun readTapCashTransactions(isoDep: IsoDep, dataResp: ByteArray): List<NfcCardTransaction> {
        val transactions = mutableListOf<NfcCardTransaction>()
        try {
            val recordResp = isoDep.transceive(APDU_READ_TAPCASH_RECORDS)
            if (isSuccess(recordResp) && recordResp.size >= 18) {
                val parsed = parseTapCashRecords(recordResp)
                if (parsed.isNotEmpty()) {
                    transactions.addAll(parsed)
                }
            }
        } catch (_: Exception) {}

        if (transactions.isEmpty() && dataResp.size >= 46) {
            val txAmount = readUint32BigEndian(dataResp, 42)
            if (txAmount in 1..MAX_TRANSACTION_AMOUNT) {
                transactions.add(
                    NfcCardTransaction(
                        id = 1L,
                        amount = txAmount,
                        type = TransactionType.EXPENSE.name,
                        timestamp = System.currentTimeMillis() - ONE_HOUR_MS,
                        terminalId = "Mutasi Terakhir"
                    )
                )
            }
        }
        return transactions
    }

    /**
     * Checks if APDU response ends with standard ISO success (0x90 0x00)
     * or DESFire operation success / more data (0x91 0x00 / 0x91 0xAF).
     */
    fun isSuccess(response: ByteArray?): Boolean {
        if (response == null || response.size < 2) return false
        val sw1 = response[response.size - 2].toInt() and 0xFF
        val sw2 = response[response.size - 1].toInt() and 0xFF
        return (sw1 == 0x90 && sw2 == 0x00) || (sw1 == 0x91 && (sw2 == 0x00 || sw2 == 0xAF))
    }

    /**
     * Parses BNI TapCash balance from DESFire 90 32 03 00 00 response.
     * Stored as 24-bit unsigned Big-Endian integer in bytes 2..4.
     */
    fun parseTapCashBalanceDesfire(response: ByteArray): Long {
        if (response.size < 5) return 0L
        val b2 = response[2].toLong() and 0xFF
        val b3 = response[3].toLong() and 0xFF
        val b4 = response[4].toLong() and 0xFF
        var balance = (b2 shl 16) or (b3 shl 8) or b4
        // If sign bit is set, clamp to 0
        if ((response[2].toInt() and 0x80) != 0) {
            balance = 0L
        }
        return if (balance in 0..20_000_000L) balance else 0L
    }

    /**
     * Parses BNI TapCash 16-digit card number (CAN) from DESFire response.
     * Stored as 8 bytes in bytes 8..15 (converted to 16 hex digits).
     */
    fun parseTapCashCardNumber(response: ByteArray): String? {
        if (response.size < 16) return null
        val cardBytes = response.copyOfRange(8, 16)
        if (cardBytes.all { it == 0.toByte() }) return null
        val hex = bytesToHex(cardBytes)
        return if (hex.length == 16) hex else null
    }

    /**
     * Parses Mandiri e-Money balance from record response.
     * Balance is typically stored as a 4-byte unsigned int in bytes 0..3 or 4..7 (Big-Endian).
     */
    fun parseMandiriBalance(response: ByteArray): Long {
        val dataLen = response.size - 2 // strip SW1 SW2
        if (dataLen < 4) return 0L

        // Primary offset: bytes 0..3
        val balance1 = readUint32BigEndian(response, 0)
        // Check sanity (balance between 0 and Rp 20.000.000)
        if (balance1 in 0..MAX_CARD_BALANCE) {
            return balance1
        }

        // Secondary offset: bytes 4..7
        if (dataLen >= 8) {
            val balance2 = readUint32BigEndian(response, 4)
            if (balance2 in 0..MAX_CARD_BALANCE) {
                return balance2
            }
        }

        // Little endian fallback check
        val balanceLE = readUint32LittleEndian(response, 0)
        if (balanceLE in 0..MAX_CARD_BALANCE) {
            return balanceLE
        }

        return 0L
    }

    /**
     * Parses BNI TapCash balance from record response.
     */
    fun parseTapCashBalance(response: ByteArray): Long {
        val dataLen = response.size - 2
        if (dataLen < 4) return 0L

        val balance = readUint32BigEndian(response, 0)
        if (balance in 0..MAX_CARD_BALANCE) {
            return balance
        }

        if (dataLen >= 8) {
            val balance2 = readUint32BigEndian(response, 4)
            if (balance2 in 0..MAX_CARD_BALANCE) {
                return balance2
            }
        }
        return 0L
    }

    /**
     * Parses 16-digit card number from BCD or ASCII bytes.
     */
    fun parseCardNumberBytes(response: ByteArray): String? {
        val data = response.copyOfRange(0, response.size - 2)
        val hex = bytesToHex(data)
        // Match 16 consecutive digits if present
        val digits = hex.filter { it.isDigit() }
        return if (digits.length >= 16) {
            digits.substring(0, 16)
        } else {
            null
        }
    }

    /**
     * Parses single transaction record.
     */
    private fun parseTransactionRecord(response: ByteArray, id: Long): NfcCardTransaction? {
        val dataLen = response.size - 2
        if (dataLen < 8) return null

        val amount = readUint32BigEndian(response, 0)
        if (amount <= 0 || amount > MAX_TRANSACTION_AMOUNT) return null

        val typeByte = if (dataLen >= 9) response[8].toInt() and 0xFF else 0x01
        val txType = if (typeByte in listOf(0x03, 0x04, 0x07)) TransactionType.INCOME.name else TransactionType.EXPENSE.name
        val terminalHex = if (dataLen >= 12) bytesToHex(response.copyOfRange(4, 8)) else null
        val terminalName = when {
            terminalHex == null -> if (txType == TransactionType.INCOME.name) "Top Up Saldo" else "Pembayaran E-Money"
            txType == TransactionType.INCOME.name -> "Top Up ($terminalHex)"
            else -> "Pembayaran ($terminalHex)"
        }

        return NfcCardTransaction(
            id = id,
            amount = amount,
            type = txType,
            timestamp = System.currentTimeMillis() - (id * ONE_HOUR_MS),
            terminalId = terminalName
        )
    }

    /**
     * Parses BNI TapCash cyclic records if file 0x04 is accessible.
     */
    fun parseTapCashRecords(response: ByteArray): List<NfcCardTransaction> {
        val records = mutableListOf<NfcCardTransaction>()
        if (response.size < 18) return records
        val data = response.copyOfRange(0, response.size - 2)
        val recordSize = if (data.size % 32 == 0) 32 else if (data.size % 16 == 0) 16 else 0
        if (recordSize > 0) {
            val numRecords = (data.size / recordSize).coerceAtMost(10)
            for (i in 0 until numRecords) {
                val offset = i * recordSize
                val amount = readUint32LittleEndian(data, offset)
                if (amount in 1..MAX_TRANSACTION_AMOUNT) {
                    records.add(
                        NfcCardTransaction(
                            id = (i + 1).toLong(),
                            amount = amount,
                            type = TransactionType.EXPENSE.name,
                            timestamp = System.currentTimeMillis() - (i * ONE_HOUR_MS),
                            terminalId = "Mutasi TapCash #${i + 1}"
                        )
                    )
                }
            }
        }
        return records
    }

    private fun readUint32BigEndian(data: ByteArray, offset: Int): Long {
        if (offset + 4 > data.size) return 0L
        val b0 = data[offset].toLong() and 0xFF
        val b1 = data[offset + 1].toLong() and 0xFF
        val b2 = data[offset + 2].toLong() and 0xFF
        val b3 = data[offset + 3].toLong() and 0xFF
        return (b0 shl 24) or (b1 shl 16) or (b2 shl 8) or b3
    }

    private fun readUint32LittleEndian(data: ByteArray, offset: Int): Long {
        if (offset + 4 > data.size) return 0L
        val b0 = data[offset].toLong() and 0xFF
        val b1 = data[offset + 1].toLong() and 0xFF
        val b2 = data[offset + 2].toLong() and 0xFF
        val b3 = data[offset + 3].toLong() and 0xFF
        return (b3 shl 24) or (b2 shl 16) or (b1 shl 8) or b0
    }

    fun formatUidAsCardNumber(uidHex: String, prefix: String): String {
        val clean = uidHex.uppercase().filter { it.isLetterOrDigit() }
        val padded = clean.padEnd(12, '0').take(12)
        return "$prefix$padded"
    }

    fun hexToBytes(hex: String): ByteArray {
        val cleanHex = hex.filter { !it.isWhitespace() }
        val len = cleanHex.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(cleanHex[i], 16) shl 4) +
                    Character.digit(cleanHex[i + 1], 16)).toByte()
        }
        return data
    }

    fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02X", b))
        }
        return sb.toString()
    }
}
