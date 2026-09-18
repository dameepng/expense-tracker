package com.example.expense_tracker.ui.receipt

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expense_tracker.data.ai.AiError
import com.example.expense_tracker.data.ai.AiInputException
import com.example.expense_tracker.data.ai.ParsedTransaction
import com.example.expense_tracker.data.ai.TransactionDraftRepository
import com.example.expense_tracker.data.ai.receipt.ReceiptImageError
import com.example.expense_tracker.data.ai.receipt.ReceiptImageException
import com.example.expense_tracker.data.ai.receipt.ReceiptParseException
import com.example.expense_tracker.data.ai.receipt.ReceiptRepository
import com.example.expense_tracker.data.ai.receipt.ReceiptScanRequest
import com.example.expense_tracker.ui.ai.TransactionDraft
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Clock
import java.time.LocalDate


class ReceiptScanViewModel(
    private val repository: ReceiptRepository,
    private val draftRepository: TransactionDraftRepository,
    private val clock: Clock = Clock.systemDefaultZone()
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReceiptScanUiState())
    val uiState: StateFlow<ReceiptScanUiState> = _uiState.asStateFlow()
    private var scanJob: Job? = null
    private var generation = 0L

    init {
        viewModelScope.launch {
            combine(draftRepository.getCategories(), draftRepository.getWallets()) { c, w -> c to w }
                .collect { (c, w) ->
                    _uiState.update { state ->
                        state.copy(
                            categories = c,
                            wallets = w,
                            draft = state.draft?.let { draft ->
                                draft.copy(walletId = draft.walletId ?: w.firstOrNull()?.id)
                            }
                        )
                    }
                }
        }
    }

    fun selectImage(uri: Uri?) { cancel(); _uiState.update { it.copy(imageUri=uri, phase=if (uri == null) ReceiptScanPhase.IDLE else ReceiptScanPhase.SELECTED, draft=null, items=emptyList(), error=null) } }
    fun replaceImage(uri: Uri?) = selectImage(uri)
    fun startScan() {
        val state = _uiState.value; val uri = state.imageUri ?: return
        if (state.phase == ReceiptScanPhase.SCANNING) return
        cancel(); val token = ++generation
        _uiState.update { it.copy(phase=ReceiptScanPhase.SCANNING, error=null, draft=null, items=emptyList()) }
        scanJob = viewModelScope.launch {
            try {
                val categories = if (_uiState.value.categories.isNotEmpty()) {
                    _uiState.value.categories
                } else {
                    withTimeoutOrNull(3000) {
                        draftRepository.getCategories().first { it.isNotEmpty() }
                    } ?: _uiState.value.categories.ifEmpty {
                        draftRepository.getCategories().firstOrNull() ?: emptyList()
                    }
                }
                val wallets = if (_uiState.value.wallets.isNotEmpty()) {
                    _uiState.value.wallets
                } else {
                    withTimeoutOrNull(3000) {
                        draftRepository.getWallets().first { it.isNotEmpty() }
                    } ?: _uiState.value.wallets.ifEmpty {
                        draftRepository.getWallets().firstOrNull() ?: emptyList()
                    }
                }
                if (categories.isNotEmpty() && _uiState.value.categories.isEmpty()) {
                    _uiState.update { it.copy(categories = categories) }
                }
                if (wallets.isNotEmpty() && _uiState.value.wallets.isEmpty()) {
                    _uiState.update { it.copy(wallets = wallets) }
                }
                val result = repository.scan(ReceiptScanRequest(uri, LocalDate.now(clock), categories))
                if (token != generation) return@launch
                val defaultWalletId = _uiState.value.wallets.firstOrNull()?.id ?: wallets.firstOrNull()?.id
                val d = result.transaction.let {
                    TransactionDraft(
                        it.amount.toString(),
                        it.categoryId,
                        it.merchant,
                        it.date.toString(),
                        it.note,
                        it.isRecurring,
                        defaultWalletId,
                        it.type
                    )
                }
                _uiState.update { it.copy(phase=ReceiptScanPhase.SUCCESS, draft=d, items=result.items) }
            } catch (e: CancellationException) { throw e }
            catch (e: AiInputException) { if (token == generation) _uiState.update { it.copy(phase=ReceiptScanPhase.FALLBACK, error=e.reason.toScanError()) } }
            catch (e: ReceiptImageException) { if (token == generation) _uiState.update { it.copy(phase=ReceiptScanPhase.ERROR, error=when (e.reason) { ReceiptImageError.TOO_LARGE -> ReceiptScanError.IMAGE_TOO_LARGE; ReceiptImageError.DECODE_FAILED, ReceiptImageError.INVALID_URI -> ReceiptScanError.IMAGE_DECODE; else -> ReceiptScanError.IMAGE_FAILURE }) } }
            catch (e: ReceiptParseException) { if (token == generation) _uiState.update { it.copy(phase=ReceiptScanPhase.FALLBACK, error=ReceiptScanError.INVALID_RESPONSE) } }
            catch (_: Exception) { if (token == generation) _uiState.update { it.copy(phase=ReceiptScanPhase.ERROR, error=ReceiptScanError.UNKNOWN) } }
        }
    }
    fun cancel() { generation++; scanJob?.cancel(); scanJob=null; _uiState.update { if (it.imageUri == null) it.copy(phase=ReceiptScanPhase.IDLE) else it.copy(phase=ReceiptScanPhase.SELECTED) } }
    fun retry() { if (_uiState.value.imageUri != null) startScan() }
    fun updateDraft(transform: (TransactionDraft) -> TransactionDraft) { _uiState.update { s -> s.copy(draft = s.draft?.let(transform), error = null) } }
    fun save() {
        val s = _uiState.value
        val d = s.draft ?: return
        if (s.isSaving || s.saved) return
        val amount = d.amountText.toLongOrNull() ?: 0L
        val date = d.parsedDate()
        val categoryValid = s.categories.any { it.id == d.categoryId }
        val walletValid = s.wallets.any { it.id == d.walletId }
        if (amount <= 0 || date == null || d.categoryId == null || d.walletId == null || !categoryValid || !walletValid) {
            _uiState.update { it.copy(error = ReceiptScanError.VALIDATION) }
            return
        }
        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                draftRepository.saveReceipt(
                    ParsedTransaction(
                        amount = amount,
                        categoryId = d.categoryId,
                        merchant = d.merchant.trim(),
                        date = date,
                        note = d.note.trim(),
                        isRecurring = d.isRecurring,
                        type = d.type
                    ),
                    d.walletId,
                    _uiState.value.items
                )
                _uiState.update { it.copy(isSaving = false, saved = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update { it.copy(isSaving = false, error = ReceiptScanError.SAVE) }
            }
        }
    }
    fun fallbackToManual() { cancel(); _uiState.update { it.copy(phase=ReceiptScanPhase.FALLBACK) } }
    fun reset() { cancel(); _uiState.value = ReceiptScanUiState(categories=_uiState.value.categories, wallets=_uiState.value.wallets) }
}

private fun AiError.toScanError() = when (this) { AiError.MISSING_API_KEY -> ReceiptScanError.CONFIGURATION; AiError.NETWORK -> ReceiptScanError.NETWORK; AiError.TIMEOUT -> ReceiptScanError.TIMEOUT; AiError.INVALID_RESPONSE -> ReceiptScanError.INVALID_RESPONSE; else -> ReceiptScanError.UNKNOWN }
