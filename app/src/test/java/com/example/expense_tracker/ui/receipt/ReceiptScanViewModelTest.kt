package com.example.expense_tracker.ui.receipt

import android.net.Uri
import com.example.expense_tracker.data.Category
import com.example.expense_tracker.data.Wallet
import com.example.expense_tracker.data.ai.ParsedTransaction
import com.example.expense_tracker.data.ai.TransactionDraftRepository
import com.example.expense_tracker.data.ai.receipt.ReceiptRepository
import com.example.expense_tracker.data.ai.receipt.ReceiptScanRequest
import com.example.expense_tracker.data.ai.receipt.ReceiptTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ReceiptScanViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val clock = Clock.fixed(Instant.parse("2026-09-14T10:00:00Z"), ZoneId.of("Asia/Jakarta"))
    private val mockUri = Uri.parse("content://media/receipt.jpg")
    private val sampleCategories = listOf(Category(1, "Makanan"), Category(2, "Belanja"))
    private val sampleWallets = listOf(Wallet(1, "Cash", 100_000))

    private class FakeReceiptRepository : ReceiptRepository {
        var lastRequest: ReceiptScanRequest? = null
        var scanResult: ReceiptTransaction = ReceiptTransaction(
            transaction = ParsedTransaction(84700, 1, "Pawoon Resto", LocalDate.of(2017, 7, 23), "makan", false),
            items = listOf("Martabak Original x2", "Es Teh Manis x1")
        )
        var throwException: Exception? = null

        override suspend fun scan(request: ReceiptScanRequest): ReceiptTransaction {
            lastRequest = request
            throwException?.let { throw it }
            return scanResult
        }
    }

    private class FakeDraftRepository : TransactionDraftRepository {
        val categoryFlow = MutableStateFlow<List<Category>>(emptyList())
        val walletFlow = MutableStateFlow<List<Wallet>>(emptyList())
        val saved = mutableListOf<Pair<ParsedTransaction, Long>>()

        override fun getCategories() = categoryFlow
        override fun getWallets() = walletFlow
        override suspend fun save(transaction: ParsedTransaction, walletId: Long) {
            saved += transaction to walletId
        }
    }

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startScan transitions to SCANNING and then SUCCESS with draft and items`() {
        val draftRepo = FakeDraftRepository().apply {
            categoryFlow.value = sampleCategories
            walletFlow.value = sampleWallets
        }
        val receiptRepo = FakeReceiptRepository()
        val vm = ReceiptScanViewModel(receiptRepo, draftRepo, clock)
        dispatcher.scheduler.runCurrent()

        vm.selectImage(mockUri)
        assertEquals(ReceiptScanPhase.SELECTED, vm.uiState.value.phase)

        vm.startScan()
        assertEquals(ReceiptScanPhase.SCANNING, vm.uiState.value.phase)

        dispatcher.scheduler.runCurrent()
        assertEquals(ReceiptScanPhase.SUCCESS, vm.uiState.value.phase)
        assertNotNull(vm.uiState.value.draft)
        assertEquals("84700", vm.uiState.value.draft?.amountText)
        assertEquals("Pawoon Resto", vm.uiState.value.draft?.merchant)
        assertEquals("2017-07-23", vm.uiState.value.draft?.dateText)
        assertEquals(listOf("Martabak Original x2", "Es Teh Manis x1"), vm.uiState.value.items)
        assertEquals(1L, vm.uiState.value.draft?.walletId)
    }

    @Test
    fun `startScan awaits categories if not yet loaded when scan starts`() {
        val draftRepo = FakeDraftRepository()
        val receiptRepo = FakeReceiptRepository()
        val vm = ReceiptScanViewModel(receiptRepo, draftRepo, clock)
        dispatcher.scheduler.runCurrent()

        vm.selectImage(mockUri)
        vm.startScan()
        assertEquals(ReceiptScanPhase.SCANNING, vm.uiState.value.phase)

        draftRepo.categoryFlow.value = sampleCategories
        draftRepo.walletFlow.value = sampleWallets
        dispatcher.scheduler.runCurrent()

        assertEquals(ReceiptScanPhase.SUCCESS, vm.uiState.value.phase)
        assertEquals(sampleCategories, receiptRepo.lastRequest?.categories)
    }

    @Test
    fun `save saves receipt draft with items and marks state as saved`() {
        val draftRepo = FakeDraftRepository().apply {
            categoryFlow.value = sampleCategories
            walletFlow.value = sampleWallets
        }
        val receiptRepo = FakeReceiptRepository()
        val vm = ReceiptScanViewModel(receiptRepo, draftRepo, clock)
        dispatcher.scheduler.runCurrent()

        vm.selectImage(mockUri)
        vm.startScan()
        dispatcher.scheduler.runCurrent()

        vm.save()
        dispatcher.scheduler.runCurrent()

        assertTrue(vm.uiState.value.saved)
        assertEquals(1, draftRepo.saved.size)
        val (tx, walletId) = draftRepo.saved.first()
        assertEquals(84700L, tx.amount)
        assertEquals(1L, walletId)
        assertTrue(tx.note.contains("Item: Martabak Original x2, Es Teh Manis x1"))
    }

    @Test
    fun `reset clears image and draft but preserves categories and wallets`() {
        val draftRepo = FakeDraftRepository().apply {
            categoryFlow.value = sampleCategories
            walletFlow.value = sampleWallets
        }
        val vm = ReceiptScanViewModel(FakeReceiptRepository(), draftRepo, clock)
        dispatcher.scheduler.runCurrent()

        vm.selectImage(mockUri)
        vm.reset()

        assertEquals(ReceiptScanPhase.IDLE, vm.uiState.value.phase)
        assertNull(vm.uiState.value.imageUri)
        assertNull(vm.uiState.value.draft)
        assertEquals(sampleCategories, vm.uiState.value.categories)
        assertEquals(sampleWallets, vm.uiState.value.wallets)
    }

    @Test
    fun `save with invalid draft sets error to VALIDATION and does not save`() {
        val draftRepo = FakeDraftRepository().apply {
            categoryFlow.value = sampleCategories
            walletFlow.value = sampleWallets
        }
        val receiptRepo = FakeReceiptRepository()
        val vm = ReceiptScanViewModel(receiptRepo, draftRepo, clock)
        dispatcher.scheduler.runCurrent()

        vm.selectImage(mockUri)
        vm.startScan()
        dispatcher.scheduler.runCurrent()

        // Nullify walletId to trigger validation failure
        vm.updateDraft { it.copy(walletId = null) }
        vm.save()
        dispatcher.scheduler.runCurrent()

        assertEquals(ReceiptScanError.VALIDATION, vm.uiState.value.error)
        assertFalse(vm.uiState.value.saved)
        assertTrue(draftRepo.saved.isEmpty())
    }

    @Test
    fun `wallets emission backfills draft walletId if previously null`() {
        val draftRepo = FakeDraftRepository().apply {
            categoryFlow.value = sampleCategories
            // Start with empty wallets
            walletFlow.value = emptyList()
        }
        val receiptRepo = FakeReceiptRepository()
        val vm = ReceiptScanViewModel(receiptRepo, draftRepo, clock)
        dispatcher.scheduler.runCurrent()

        vm.selectImage(mockUri)
        vm.startScan()
        dispatcher.scheduler.runCurrent()

        // Initially walletId is null because wallets was empty
        assertNull(vm.uiState.value.draft?.walletId)

        // Now emit wallets
        draftRepo.walletFlow.value = sampleWallets
        dispatcher.scheduler.runCurrent()

        // draft.walletId should now be automatically backfilled with first wallet id
        assertEquals(1L, vm.uiState.value.draft?.walletId)
        assertTrue(vm.uiState.value.canSave)
    }
}
