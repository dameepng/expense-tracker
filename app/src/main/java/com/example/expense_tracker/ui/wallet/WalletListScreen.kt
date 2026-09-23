package com.example.expense_tracker.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.expense_tracker.R
import com.example.expense_tracker.data.Wallet
import com.example.expense_tracker.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletListScreen(
    viewModel: WalletViewModel,
    onSelectWallet: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var walletToDelete by remember { mutableStateOf<Wallet?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_wallet), fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_wallet))
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        val spacing = MaterialTheme.spacing
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            contentAlignment = Alignment.TopCenter
        ) {
            if (uiState.wallets.isEmpty() && !uiState.isLoading) {
                Text(
                    text = stringResource(R.string.no_wallet),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = MAX_WALLET_LIST_WIDTH),
                    contentPadding = PaddingValues(
                        start = spacing.screenMargin,
                        end = spacing.screenMargin,
                        top = spacing.sectionGap,
                        bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(spacing.sectionGap)
                ) {
                    items(
                        items = uiState.wallets,
                        key = { it.id },
                        contentType = { "wallet_card" }
                    ) { wallet ->
                        CreditCardItem(
                            wallet = wallet,
                            onClick = { onSelectWallet(wallet.id) },
                            onDeleteClick = {
                                walletToDelete = wallet
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        val spacing = MaterialTheme.spacing
        val coroutineScope = rememberCoroutineScope()
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var newWalletName by remember { mutableStateOf("") }
        var newCardNumber by remember { mutableStateOf("") }
        var newCardHolderName by remember { mutableStateOf("") }
        var expiryMonth by remember { mutableStateOf("") }
        var expiryYear by remember { mutableStateOf("") }
        var selectedColorId by remember { mutableStateOf(CardGradients.SunsetRose.id) }

        val yearFocusRequester = remember { FocusRequester() }

        fun dismissAddWallet() {
            coroutineScope.launch {
                sheetState.hide()
            }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                    showAddDialog = false
                }
            }
        }

        ModalBottomSheet(
            onDismissRequest = { showAddDialog = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = MAX_WALLET_SHEET_WIDTH)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = spacing.screenMargin)
                        .padding(bottom = spacing.space400)
                ) {
                Text(
                    text = stringResource(R.string.add_new_wallet),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = spacing.space200)
                )

                OutlinedTextField(
                    value = newWalletName,
                    onValueChange = { newWalletName = it },
                    label = { Text(stringResource(R.string.wallet_name_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(spacing.space150))

                OutlinedTextField(
                    value = newCardNumber,
                    onValueChange = { if (it.length <= 16 && it.all { char -> char.isDigit() }) newCardNumber = it },
                    label = { Text(stringResource(R.string.card_number)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newCardHolderName,
                    onValueChange = { newCardHolderName = it.uppercase() },
                    label = { Text(stringResource(R.string.card_holder_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.expiry_date),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = expiryMonth,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }.take(2)
                            expiryMonth = digits
                            if (digits.length == 2) {
                                yearFocusRequester.requestFocus()
                            }
                        },
                        placeholder = { Text("MM") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(80.dp),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Text(
                        text = "/",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = expiryYear,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }.take(2)
                            expiryYear = digits
                        },
                        placeholder = { Text("YY") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .width(80.dp)
                            .focusRequester(yearFocusRequester),
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.choose_card_color),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CardGradients.options.forEach { gradient ->
                        val isSelected = selectedColorId == gradient.id
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(gradient.brush)
                                .clickable { selectedColorId = gradient.id }
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { dismissAddWallet() },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(
                        onClick = {
                            val formattedExpiry = if (expiryMonth.isNotBlank() || expiryYear.isNotBlank()) {
                                "${expiryMonth.padStart(2, '0')}/${expiryYear.padStart(2, '0')}"
                            } else ""

                            viewModel.addWallet(
                                name = newWalletName,
                                cardNumber = newCardNumber,
                                cardHolderName = newCardHolderName,
                                cardExpiry = formattedExpiry,
                                color = selectedColorId
                            )
                            dismissAddWallet()
                        },
                        enabled = newWalletName.isNotBlank(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
    }

    walletToDelete?.let { wallet ->
        var confirmText by remember { mutableStateOf("") }
        val isConfirmed = confirmText.equals("hapus dompet", ignoreCase = true)
        
        AlertDialog(
            onDismissRequest = { walletToDelete = null },
            title = {
                Text(
                    text = stringResource(R.string.delete_wallet_title),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.delete_wallet_warning),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = confirmText,
                        onValueChange = { confirmText = it },
                        label = {
                            val hintText = stringResource(R.string.delete_wallet_hint)
                            val target = CONFIRM_DELETE_TARGET
                            val start = hintText.indexOf(target, ignoreCase = true)
                            if (start != -1) {
                                Text(
                                    buildAnnotatedString {
                                        append(hintText.substring(0, start))
                                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)) {
                                            append(hintText.substring(start, start + target.length))
                                        }
                                        append(hintText.substring(start + target.length))
                                    }
                                )
                            } else {
                                Text(hintText)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteWallet(wallet)
                        walletToDelete = null
                    },
                    enabled = isConfirmed,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                    )
                ) {
                    Text(stringResource(R.string.delete_wallet_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { walletToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun CreditCardItem(
    wallet: Wallet,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit = {}
) {
    val gradient = CardGradients.getGradient(wallet.color)

    val spacing = MaterialTheme.spacing
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.8f),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient.brush)
                .padding(spacing.cardPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Chip, Contactless icon & Wallet Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CardChip()
                        Icon(
                            imageVector = Icons.Default.Nfc,
                            contentDescription = "Contactless",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = wallet.name.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(32.dp).padding(start = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Middle: Card Number (Masked)
                Text(
                    text = formatMaskedCardNumber(wallet.cardNumber),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Bottom Row: Cardholder Name, Expiry & Balance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.card_holder),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = if (wallet.cardHolderName.isNotBlank()) wallet.cardHolderName.uppercase() else wallet.name.uppercase(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.expires),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = wallet.cardExpiry.ifBlank { DEFAULT_CARD_EXPIRY },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CardChip(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(36.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color(0xFFE6C875))
            .border(1.dp, Color(0xFFD4AF37), RoundedCornerShape(5.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .border(0.5.dp, Color(0xFF997A15), RoundedCornerShape(3.dp))
        )
    }
}

internal fun formatMaskedCardNumber(cardNumber: String): String {
    val digits = cardNumber.filter { it.isDigit() }
    if (digits.isEmpty()) return DEFAULT_CARD_MASK
    val masked = if (digits.length >= 4) {
        "•".repeat((digits.length - 4).coerceAtLeast(0)) + digits.takeLast(4)
    } else {
        digits
    }
    val padded = masked.padEnd(16, '•')
    return padded.chunked(4).joinToString(" ")
}

private val MAX_WALLET_LIST_WIDTH = 840.dp
private val MAX_WALLET_SHEET_WIDTH = 560.dp
private const val CONFIRM_DELETE_TARGET = "\"hapus dompet\""
private const val DEFAULT_CARD_EXPIRY = "12/28"
private const val DEFAULT_CARD_MASK = "•••• •••• •••• 5052"


