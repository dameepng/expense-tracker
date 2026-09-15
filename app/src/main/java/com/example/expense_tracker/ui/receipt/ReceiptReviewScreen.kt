package com.example.expense_tracker.ui.receipt

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.expense_tracker.ui.ai.TransactionDraft
import com.example.expense_tracker.ui.theme.spacing
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
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
    BackHandler { viewModel.cancel(); onBack() }
    val spacing = MaterialTheme.spacing
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Struk") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.cancel(); onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
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
                    .widthIn(max = 640.dp)
                    .padding(horizontal = spacing.screenMargin, vertical = spacing.itemGap)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(spacing.sectionGap)
            ) {
            state.imageUri?.let {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = it,
                        contentDescription = "Preview struk",
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
                            text = "Menganalisis struk dengan AI...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Membaca nominal, merchant, tanggal, dan item",
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
                            "Struk tidak terbaca atau format tidak sesuai.",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            "Silakan gunakan input manual atau Catat dengan AI.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        TextButton(onClick = onManualInput) {
                            Text("Input Manual / Catat dengan AI")
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
                            text = when (error) {
                                ReceiptScanError.VALIDATION -> "Mohon lengkapi data: pastikan nominal valid, kategori dan dompet dipilih, serta tanggal YYYY-MM-DD."
                                ReceiptScanError.SAVE -> "Gagal menyimpan transaksi ke database. Silakan periksa data dan coba lagi."
                                ReceiptScanError.CONFIGURATION -> "Kunci API belum dikonfigurasi."
                                ReceiptScanError.NETWORK -> "Koneksi internet bermasalah. Periksa jaringan Anda."
                                ReceiptScanError.TIMEOUT -> "Waktu permintaan habis. Silakan coba lagi."
                                ReceiptScanError.IMAGE_TOO_LARGE -> "Ukuran gambar terlalu besar."
                                ReceiptScanError.IMAGE_DECODE -> "Gagal memproses file gambar."
                                ReceiptScanError.INVALID_RESPONSE -> "Respons dari AI tidak valid. Coba scan ulang."
                                else -> "Terjadi kesalahan saat memproses struk."
                            },
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
                            Text("Daftar Item (${state.items.size}):", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            state.items.take(15).forEach { item ->
                                Text("• $item", style = MaterialTheme.typography.bodySmall)
                            }
                            if (state.items.size > 15) {
                                Text("+ ${state.items.size - 15} item lainnya…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("Menyimpan…", fontWeight = FontWeight.Bold)
                    } else {
                        Text("Simpan Transaksi", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (state.phase != ReceiptScanPhase.SCANNING && state.imageUri != null) {
                OutlinedButton(
                    onClick = viewModel::startScan,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Scan Ulang", fontWeight = FontWeight.SemiBold)
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
        label = { Text("Nominal (Rp)") },
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
        label = "Kategori",
        selectedId = draft.categoryId,
        choices = categoryChoices,
        onSelect = { id -> vm.updateDraft { it.copy(categoryId = id) } }
    )

    // Dompet Dropdown
    val walletChoices = remember(state.wallets) {
        state.wallets.map { it.id to it.name }
    }
    ReceiptChoiceDropdown(
        label = "Pilih Dompet",
        selectedId = draft.walletId,
        choices = walletChoices,
        onSelect = { id -> vm.updateDraft { it.copy(walletId = id) } }
    )

    // Merchant
    OutlinedTextField(
        value = draft.merchant,
        onValueChange = { value -> vm.updateDraft { it.copy(merchant = value) } },
        label = { Text("Merchant / Toko") },
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
                label = { Text("Tanggal") },
                supportingText = { Text("Format: YYYY-MM-DD") },
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
                Text("Hari ini", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }
        }

        if (!isDateInCurrentMonth && draft.dateText.isNotBlank()) {
            Text(
                text = "Catatan: Tanggal struk (${draft.dateText}) berada di luar bulan ini. Klik 'Hari ini' jika ingin dicatat pada bulan berjalan.",
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
        label = { Text("Catatan") },
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
