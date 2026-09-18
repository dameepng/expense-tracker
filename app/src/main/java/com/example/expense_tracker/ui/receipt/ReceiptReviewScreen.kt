package com.example.expense_tracker.ui.receipt

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.expense_tracker.R
import com.example.expense_tracker.ui.ai.TransactionDraft
import com.example.expense_tracker.ui.navigation.LocalNavAnimatedVisibilityScope
import com.example.expense_tracker.ui.navigation.LocalSharedTransitionScope
import com.example.expense_tracker.ui.theme.spacing
import java.time.LocalDate

private val MAX_RECEIPT_REVIEW_WIDTH = 640.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ReceiptReviewScreen(
    viewModel: ReceiptScanViewModel,
    onBack: () -> Unit,
    onManualInput: () -> Unit,
    onSaved: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }
    LaunchedEffect(state.imageUri, state.phase) {
        if (state.imageUri != null && state.phase == ReceiptScanPhase.SELECTED) {
            viewModel.startScan()
        }
    }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val handleBack = {
        keyboard?.hide()
        focusManager.clearFocus()
        viewModel.cancel()
        onBack()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.cancel()
        }
    }

    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
    val sharedImageModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(key = "receipt_image"),
                animatedVisibilityScope = animatedVisibilityScope
            )
        }
    } else {
        Modifier
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val spacing = MaterialTheme.spacing
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.receipt_review_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                windowInsets = WindowInsets(0, 0, 0, 0),
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = MAX_RECEIPT_REVIEW_WIDTH)
                    .padding(horizontal = spacing.screenMargin, vertical = spacing.itemGap)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(spacing.sectionGap)
            ) {
            state.imageUri?.let {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(sharedImageModifier)
                ) {
                    AsyncImage(
                        model = it,
                        contentDescription = stringResource(R.string.receipt_preview_cd),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }

            if (state.phase == ReceiptScanPhase.SCANNING) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.cardPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(spacing.space150)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = stringResource(R.string.receipt_scanning_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.receipt_scanning_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (state.phase == ReceiptScanPhase.FALLBACK) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(spacing.cardPadding), verticalArrangement = Arrangement.spacedBy(spacing.space100)) {
                        Text(
                            stringResource(R.string.receipt_fallback_title),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            stringResource(R.string.receipt_fallback_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        TextButton(onClick = onManualInput) {
                            Text(stringResource(R.string.receipt_fallback_action))
                        }
                    }
                }
            }

            state.error?.let { error ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(spacing.cardPadding),
                        horizontalArrangement = Arrangement.spacedBy(spacing.space150),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Text(
                            text = stringResource(
                                when (error) {
                                    ReceiptScanError.VALIDATION -> R.string.receipt_error_validation
                                    ReceiptScanError.SAVE -> R.string.receipt_error_save
                                    ReceiptScanError.CONFIGURATION -> R.string.receipt_error_config
                                    ReceiptScanError.NETWORK -> R.string.receipt_error_network
                                    ReceiptScanError.TIMEOUT -> R.string.receipt_error_timeout
                                    ReceiptScanError.IMAGE_TOO_LARGE -> R.string.receipt_error_image_large
                                    ReceiptScanError.IMAGE_DECODE -> R.string.receipt_error_image_decode
                                    ReceiptScanError.INVALID_RESPONSE -> R.string.receipt_error_invalid
                                    else -> R.string.receipt_error_unknown
                                }
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            state.draft?.let { draft ->
                DraftFields(state = state, draft = draft, vm = viewModel)

                if (state.items.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(modifier = Modifier.padding(spacing.cardPadding), verticalArrangement = Arrangement.spacedBy(spacing.space75)) {
                            Text(
                                stringResource(R.string.receipt_items_title, state.items.size),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            state.items.take(15).forEach { item ->
                                Text("• $item", style = MaterialTheme.typography.bodySmall)
                            }
                            if (state.items.size > 15) {
                                Text(
                                    stringResource(R.string.receipt_items_more, state.items.size - 15),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = { viewModel.save() },
                    enabled = state.canSave && !state.isSaving && !state.saved,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.receipt_saving), fontWeight = FontWeight.Bold)
                    } else {
                        Text(stringResource(R.string.receipt_save_button), fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (state.phase != ReceiptScanPhase.SCANNING && state.imageUri != null) {
                OutlinedButton(
                    onClick = viewModel::startScan,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(stringResource(R.string.receipt_rescan), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
}

@Composable
private fun DraftFields(
    state: ReceiptScanUiState,
    draft: TransactionDraft,
    vm: ReceiptScanViewModel
) {
    val today = remember { LocalDate.now() }
    val isDateInCurrentMonth = remember(draft.dateText) {
        draft.parsedDate()?.let { it.year == today.year && it.monthValue == today.monthValue } ?: true
    }

    // Nominal
    OutlinedTextField(
        value = draft.amountText,
        onValueChange = { value -> vm.updateDraft { it.copy(amountText = value.filter { c -> c.isDigit() }) } },
        label = { Text(stringResource(R.string.receipt_field_amount)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    )

    // Kategori Dropdown
    val categoryChoices = remember(state.categories, draft.type) {
        state.categories
            .filter { it.type == draft.type || it.type == "BOTH" }
            .map { it.id to it.name }
    }
    ReceiptChoiceDropdown(
        label = stringResource(R.string.receipt_field_category),
        selectedId = draft.categoryId,
        choices = categoryChoices,
        onSelect = { id -> vm.updateDraft { it.copy(categoryId = id) } }
    )

    // Dompet Dropdown
    val walletChoices = remember(state.wallets) {
        state.wallets.map { it.id to it.name }
    }
    ReceiptChoiceDropdown(
        label = stringResource(R.string.receipt_field_wallet),
        selectedId = draft.walletId,
        choices = walletChoices,
        onSelect = { id -> vm.updateDraft { it.copy(walletId = id) } }
    )

    // Merchant
    OutlinedTextField(
        value = draft.merchant,
        onValueChange = { value -> vm.updateDraft { it.copy(merchant = value) } },
        label = { Text(stringResource(R.string.receipt_field_merchant)) },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    )

    // Tanggal
    val spacing = MaterialTheme.spacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.space50)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.space100),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = draft.dateText,
                onValueChange = { value -> vm.updateDraft { it.copy(dateText = value) } },
                label = { Text(stringResource(R.string.receipt_field_date)) },
                supportingText = { Text(stringResource(R.string.receipt_field_date_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            )
            FilledTonalButton(
                onClick = { vm.updateDraft { it.copy(dateText = today.toString()) } },
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.receipt_field_date_today), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }
        }

        if (!isDateInCurrentMonth && draft.dateText.isNotBlank()) {
            Text(
                text = stringResource(R.string.receipt_date_outside_month, draft.dateText),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }

    // Catatan
    OutlinedTextField(
        value = draft.note,
        onValueChange = { value -> vm.updateDraft { it.copy(note = value) } },
        label = { Text(stringResource(R.string.receipt_field_note)) },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        minLines = 2,
        maxLines = 4,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceiptChoiceDropdown(
    label: String,
    selectedId: Long?,
    choices: List<Pair<Long, String>>,
    onSelect: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val hasChoices = choices.isNotEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded && hasChoices,
        onExpandedChange = { expanded = it && hasChoices }
    ) {
        OutlinedTextField(
            value = choices.firstOrNull { it.first == selectedId }?.second.orEmpty(),
            onValueChange = {},
            readOnly = true,
            enabled = hasChoices,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && hasChoices)
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, hasChoices)
        )
        ExposedDropdownMenu(
            expanded = expanded && hasChoices,
            onDismissRequest = { expanded = false }
        ) {
            choices.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelect(id)
                        expanded = false
                    }
                )
            }
        }
    }
}
