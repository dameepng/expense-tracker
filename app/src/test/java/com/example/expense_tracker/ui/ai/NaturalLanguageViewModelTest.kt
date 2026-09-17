package com.example.expense_tracker.ui.ai

import com.example.expense_tracker.data.Category
import com.example.expense_tracker.data.Wallet
import com.example.expense_tracker.data.ai.*
import kotlinx.coroutines.CompletableDeferred
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
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class NaturalLanguageViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    // UTC is still September 9, but it is September 10 on the device.
    private val clock = Clock.fixed(Instant.parse("2026-09-09T18:30:00Z"), ZoneId.of("Asia/Jakarta"))
    private val parsed = ParsedTransaction(25000, 1, "Warteg", LocalDate.of(2026, 9, 10), "makan siang", false)

    private class FakeDraftRepository : TransactionDraftRepository {
        val categoryFlow = MutableStateFlow(listOf(Category(1, "Makanan"), Category(2, "Transport")))
        val walletFlow = MutableStateFlow(listOf(Wallet(1, "Cash", 0)))
        val saved = mutableListOf<Pair<ParsedTransaction, Long>>()
        var failSave = false
        var saveGate: CompletableDeferred<Unit>? = null
        override fun getCategories() = categoryFlow
        override fun getWallets() = walletFlow
        override suspend fun save(transaction: ParsedTransaction, walletId: Long) {
            saveGate?.await()
            if (failSave) error("disk error")
            saved += transaction to walletId
        }
    }

    private fun viewModel(
        repository: FakeDraftRepository,
        parse: suspend (NaturalLanguageRequest) -> ParsedTransaction = { parsed }
    ): NaturalLanguageViewModel = NaturalLanguageViewModel(
        object : NaturalLanguageRepository {
            override suspend fun parse(request: NaturalLanguageRequest) = parse.invoke(request)
        }, repository, { clock }
    ).also { dispatcher.scheduler.runCurrent() }

    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { Dispatchers.resetMain() }

    @Test fun `parse uses current device date zone and real categories without saving`() {
        val repo = FakeDraftRepository()
        var request: NaturalLanguageRequest? = null
        val vm = viewModel(repo) { request = it; parsed }
        vm.onInputChange("makan siang di warteg 25rb")
        vm.parse()
        assertTrue(vm.uiState.value.isParsing)
        dispatcher.scheduler.runCurrent()
        val req = request!!
        assertEquals(LocalDate.of(2026, 9, 10), req.referenceDate)
        assertEquals(clock.zone, req.zoneId)
        assertEquals(repo.categoryFlow.value, req.categories)
        assertEquals("25000", vm.uiState.value.draft!!.amountText)
        assertTrue(vm.uiState.value.canSave)
        assertTrue(repo.saved.isEmpty())
    }

    @Test fun `edited preview is saved once only after confirmation`() {
        val repo = FakeDraftRepository().apply { saveGate = CompletableDeferred() }
        val vm = viewModel(repo)
        vm.onInputChange("makan 25rb")
        vm.parse()
        dispatcher.scheduler.runCurrent()
        vm.updateDraft { it.copy(amountText = "50000", categoryId = 2, merchant = "SPBU", dateText = "2026-09-09", note = "bensin", isRecurring = true) }
        vm.save()
        vm.save()
        dispatcher.scheduler.runCurrent()
        assertTrue(vm.uiState.value.isSaving)
        repo.saveGate!!.complete(Unit)
        dispatcher.scheduler.runCurrent()
        assertEquals(1, repo.saved.size)
        assertEquals(ParsedTransaction(50000, 2, "SPBU", LocalDate.of(2026, 9, 9), "bensin", true), repo.saved.single().first)
        assertEquals(1L, repo.saved.single().second)
        assertTrue(vm.uiState.value.saved)
        vm.save()
        dispatcher.scheduler.runCurrent()
        assertEquals(1, repo.saved.size)
    }

    @Test fun `api failure exposes error`() {
        val repo = FakeDraftRepository()
        val vm = viewModel(repo) { throw AiInputException(AiError.NETWORK) }
        vm.onInputChange("makan 25rb")
        vm.parse()
        dispatcher.scheduler.runCurrent()
        assertEquals(AiUiError.NETWORK, vm.uiState.value.error)
        assertFalse(vm.uiState.value.isParsing)
        assertNull(vm.uiState.value.draft)
    }

    @Test fun `cancel cannot be overwritten by late response`() {
        val response = CompletableDeferred<ParsedTransaction>()
        val vm = viewModel(FakeDraftRepository()) { response.await() }
        vm.onInputChange("makan 25rb")
        vm.parse()
        dispatcher.scheduler.runCurrent()
        vm.cancelParsing()
        response.complete(parsed)
        dispatcher.scheduler.runCurrent()
        assertNull(vm.uiState.value.draft)
        assertFalse(vm.uiState.value.isParsing)
        assertNull(vm.uiState.value.error)
    }

    @Test fun `invalid date amount and stale selections cannot save`() {
        val repo = FakeDraftRepository()
        val vm = viewModel(repo)
        vm.onInputChange("makan siang di warteg 25rb")
        vm.parse()
        dispatcher.scheduler.runCurrent()
        vm.updateDraft { it.copy(amountText = "25000", categoryId = 1, dateText = "2026-02-30") }
        assertFalse(vm.uiState.value.canSave)
        vm.updateDraft { it.copy(dateText = "2026-02-28", amountText = "9223372036854775808") }
        assertFalse(vm.uiState.value.canSave)
        vm.updateDraft { it.copy(amountText = "0") }
        assertFalse(vm.uiState.value.canSave)
        vm.updateDraft { it.copy(amountText = "25000") }
        assertTrue(vm.uiState.value.canSave)
        repo.walletFlow.value = emptyList()
        dispatcher.scheduler.runCurrent()
        vm.save()
        assertEquals(AiUiError.VALIDATION, vm.uiState.value.error)
        assertTrue(repo.saved.isEmpty())
    }

    @Test fun `save failure retains edited draft for retry`() {
        val repo = FakeDraftRepository().apply { failSave = true }
        val vm = viewModel(repo)
        vm.onInputChange("makan siang di warteg 25rb")
        vm.parse()
        dispatcher.scheduler.runCurrent()
        vm.updateDraft { it.copy(amountText = "25000", categoryId = 1, note = "keep me") }
        vm.save()
        dispatcher.scheduler.runCurrent()
        assertEquals(AiUiError.SAVE, vm.uiState.value.error)
        assertEquals("keep me", vm.uiState.value.draft!!.note)
        assertTrue(vm.uiState.value.canSave)
        assertFalse(vm.uiState.value.saved)
        repo.failSave = false
        vm.save()
        dispatcher.scheduler.runCurrent()
        assertTrue(vm.uiState.value.saved)
    }

    @Test fun `multiple wallets require explicit choice`() {
        val repo = FakeDraftRepository().apply { walletFlow.value = listOf(Wallet(1, "Cash", 0), Wallet(2, "Bank", 0)) }
        val vm = viewModel(repo)
        vm.onInputChange("makan 25rb")
        vm.parse()
        dispatcher.scheduler.runCurrent()
        assertNull(vm.uiState.value.draft!!.walletId)
        assertFalse(vm.uiState.value.canSave)
        vm.updateDraft { it.copy(walletId = 2) }
        assertTrue(vm.uiState.value.canSave)
    }

    @Test fun `income transaction parses and saves with INCOME type`() {
        val incomeParsed = ParsedTransaction(5729860, 3, "Kantor", LocalDate.of(2026, 9, 10), "gaji", true, type = "INCOME")
        val repo = FakeDraftRepository().apply {
            categoryFlow.value = listOf(Category(1, "Makanan"), Category(3, "Gaji", "INCOME"))
        }
        val vm = viewModel(repo) { incomeParsed }
        vm.onInputChange("gajihan bulan ini 5.729.860")
        vm.parse()
        dispatcher.scheduler.runCurrent()
        assertEquals("INCOME", vm.uiState.value.draft!!.type)
        assertEquals(5729860L, vm.uiState.value.draft!!.amountText.toLong())
        assertTrue(vm.uiState.value.canSave)
        vm.save()
        dispatcher.scheduler.runCurrent()
        assertEquals("INCOME", repo.saved.single().first.type)
        assertEquals(5729860L, repo.saved.single().first.amount)
    }
}
