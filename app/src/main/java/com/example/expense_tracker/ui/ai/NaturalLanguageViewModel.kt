package com.example.expense_tracker.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expense_tracker.data.ai.AiError
import com.example.expense_tracker.data.ai.AiInputException
import com.example.expense_tracker.data.ai.NaturalLanguageRepository
import com.example.expense_tracker.data.ai.NaturalLanguageRequest
import com.example.expense_tracker.data.ai.ParsedTransaction
import com.example.expense_tracker.data.ai.TransactionDraftRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

class NaturalLanguageViewModel(
    private val aiRepository: NaturalLanguageRepository,
    private val draftRepository: TransactionDraftRepository,
    private val clockProvider: () -> Clock = { Clock.systemDefaultZone() }
) : ViewModel() {
    private val _uiState = MutableStateFlow(NaturalLanguageUiState())
    val uiState: StateFlow<NaturalLanguageUiState> = _uiState.asStateFlow()
    private var parseJob: Job? = null
    private var loadJob: Job? = null

    init { retryLoad() }

    fun retryLoad() {
        if (_uiState.value.isSaving) return
        loadJob?.cancel()
        _uiState.update { it.copy(isInitializing = true, error = null) }
        loadJob = viewModelScope.launch {
            try {
                combine(draftRepository.getCategories(), draftRepository.getWallets()) { categories, wallets ->
                    categories to wallets
                }.collect { (categories, wallets) ->
                    _uiState.update { state ->
                        state.copy(
                            categories = categories,
                            wallets = wallets,
                            isInitializing = false,
                            drafts = state.drafts.map { draft ->
                                draft.copy(walletId = draft.walletId ?: wallets.singleOrNull()?.id)
                            }
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isInitializing = false, categories = emptyList(), wallets = emptyList(), error = AiUiError.INITIALIZATION)
                }
            }
        }
    }

    fun onInputChange(text: String) {
        if (_uiState.value.isSaving || _uiState.value.saved) return
        cancelParsing()
        _uiState.update { it.copy(inputText = text.take(1000), drafts = emptyList(), error = null) }
    }

    fun parse() {
        val state = _uiState.value
        if (!state.canParse) return
        val clock = clockProvider()
        val request = NaturalLanguageRequest(state.inputText.trim(), state.categories, LocalDate.now(clock), clock.zone)
        _uiState.update { it.copy(isParsing = true, error = null, drafts = emptyList()) }
        parseJob = viewModelScope.launch {
            try {
                val results = aiRepository.parse(request)
                coroutineContext.ensureActive()
                _uiState.update { current ->
                    current.copy(
                        isParsing = false,
                        drafts = results.map { result ->
                            TransactionDraft(
                                amountText = result.amount.toString(),
                                categoryId = result.categoryId,
                                merchant = result.merchant,
                                dateText = result.date.toString(),
                                note = result.note,
                                isRecurring = result.isRecurring,
                                walletId = current.wallets.singleOrNull()?.id,
                                type = result.type
                            )
                        }
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: AiInputException) {
                coroutineContext.ensureActive()
                _uiState.update { it.copy(isParsing = false, error = failure.reason.toUiError()) }
            } catch (_: Exception) {
                coroutineContext.ensureActive()
                _uiState.update { it.copy(isParsing = false, error = AiUiError.SERVICE) }
            }
        }
    }

    fun cancelParsing() {
        parseJob?.cancel()
        parseJob = null
        _uiState.update { it.copy(isParsing = false) }
    }

    fun updateDraft(index: Int = 0, transform: (TransactionDraft) -> TransactionDraft) {
        val state = _uiState.value
        if (state.isSaving || state.isParsing || state.saved) return
        if (index !in state.drafts.indices) return
        _uiState.update { current ->
            val updatedDrafts = current.drafts.toMutableList()
            val initial = updatedDrafts[index]
            val updated = transform(initial)
            val finalDraft = if (updated.type != initial.type) {
                val isCategoryCompatible = current.categories.any {
                    it.id == updated.categoryId && (it.type == updated.type || it.type == "BOTH")
                }
                if (!isCategoryCompatible) updated.copy(categoryId = null) else updated
            } else updated
            updatedDrafts[index] = finalDraft
            current.copy(drafts = updatedDrafts, error = null)
        }
    }

    fun removeDraft(index: Int) {
        val state = _uiState.value
        if (state.isSaving || state.isParsing || state.saved) return
        if (index !in state.drafts.indices) return
        _uiState.update { current ->
            val updatedDrafts = current.drafts.toMutableList()
            updatedDrafts.removeAt(index)
            current.copy(drafts = updatedDrafts, error = null)
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.isSaving || state.saved) return
        if (!state.canSave) {
            _uiState.update { it.copy(error = AiUiError.VALIDATION) }
            return
        }
        val pairs = state.drafts.map { draft ->
            val transaction = ParsedTransaction(
                amount = draft.amountText.toLong(),
                categoryId = requireNotNull(draft.categoryId),
                merchant = draft.merchant.trim(),
                date = requireNotNull(draft.parsedDate()),
                note = draft.note.trim(),
                isRecurring = draft.isRecurring,
                type = draft.type
            )
            transaction to requireNotNull(draft.walletId)
        }
        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                draftRepository.saveAllWithWallets(pairs)
                _uiState.update { it.copy(isSaving = false, saved = true) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _uiState.update { it.copy(isSaving = false, error = AiUiError.SAVE) }
            }
        }
    }
}

private fun AiError.toUiError(): AiUiError = when (this) {
    AiError.MISSING_API_KEY -> AiUiError.CONFIGURATION
    AiError.NETWORK -> AiUiError.NETWORK
    AiError.TIMEOUT -> AiUiError.TIMEOUT
    AiError.RATE_LIMIT -> AiUiError.RATE_LIMIT
    AiError.AUTHENTICATION -> AiUiError.AUTHENTICATION
    AiError.SERVER -> AiUiError.SERVICE
    AiError.INVALID_RESPONSE -> AiUiError.INVALID_RESPONSE
    AiError.INVALID_INPUT -> AiUiError.INVALID_INPUT
}
