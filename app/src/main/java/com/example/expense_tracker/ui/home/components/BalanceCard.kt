package com.example.expense_tracker.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expense_tracker.R
import com.example.expense_tracker.data.Wallet
import com.example.expense_tracker.ui.CurrencyFormatter
import com.example.expense_tracker.ui.components.AutoResizeText
import com.example.expense_tracker.ui.theme.spacing
import com.example.expense_tracker.ui.wallet.CardGradients

private const val DEFAULT_MASKED_CARD_NUMBER = "•••• •••• •••• 4020"
private const val CARD_ASPECT_RATIO = 1.8f
private val CARD_DARK_START = Color(0xFF2D2D3A)
private val CARD_DARK_END = Color(0xFF1A1A2E)
private val MASTERCARD_RED = Color(0xFFEA001B)
private val MASTERCARD_ORANGE = Color(0xFFF79E1B)

/**
 * Credit card style balance presentation card with wallet selection menu, balance, and masked card details.
 */
@Composable
fun BalanceCard(
    totalBalance: Long,
    selectedWalletName: String,
    modifier: Modifier = Modifier,
    wallets: List<Wallet> = emptyList(),
    selectedWalletId: Long? = null,
    onWalletSelected: (Long?) -> Unit = {}
) {
    val selectedWallet = remember(wallets, selectedWalletId) {
        wallets.find { it.id == selectedWalletId }
    }

    val gradient = remember(selectedWallet?.color) {
        if (selectedWallet != null) {
            CardGradients.getGradient(selectedWallet.color).brush
        } else {
            Brush.linearGradient(listOf(CARD_DARK_START, CARD_DARK_END))
        }
    }

    val maskedCardNumber = remember(selectedWallet?.cardNumber) {
        if (selectedWallet != null && selectedWallet.cardNumber.isNotBlank()) {
            val digits = selectedWallet.cardNumber.filter { it.isDigit() }
            val masked = if (digits.length >= 4) {
                "•".repeat((digits.length - 4).coerceAtLeast(0)) + digits.takeLast(4)
            } else {
                digits
            }
            val padded = masked.padEnd(16, '•')
            padded.chunked(4).joinToString(" ")
        } else {
            DEFAULT_MASKED_CARD_NUMBER
        }
    }

    val spacing = MaterialTheme.spacing
    Surface(
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenMargin)
            .aspectRatio(CARD_ASPECT_RATIO)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(spacing.cardPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Wallet Name & More Options Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedWalletName.uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    WalletDropdownMenu(
                        wallets = wallets,
                        selectedWalletId = selectedWalletId,
                        onWalletSelected = onWalletSelected
                    )
                }

                // Middle & Bottom Row
                Column {
                    AutoResizeText(
                        text = CurrencyFormatter.format(totalBalance),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = stringResource(R.string.total_balance),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    // Bottom Row: Card number & Logo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = maskedCardNumber,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            letterSpacing = 2.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )

                        CardDecorativeCircles()
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletDropdownMenu(
    wallets: List<Wallet>,
    selectedWalletId: Long?,
    onWalletSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        var expanded by remember { mutableStateOf(false) }
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = Color.White.copy(alpha = 0.7f)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.all_wallets)) },
                onClick = {
                    expanded = false
                    if (selectedWalletId != null) {
                        onWalletSelected(null)
                    }
                },
                leadingIcon = {
                    if (selectedWalletId == null) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
            wallets.forEach { wallet ->
                DropdownMenuItem(
                    text = { Text(wallet.name) },
                    onClick = {
                        expanded = false
                        if (selectedWalletId != wallet.id) {
                            onWalletSelected(wallet.id)
                        }
                    },
                    leadingIcon = {
                        if (selectedWalletId == wallet.id) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CardDecorativeCircles(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(40.dp)
            .height(24.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterStart),
            shape = CircleShape,
            color = MASTERCARD_RED.copy(alpha = 0.8f)
        ) {}
        Surface(
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterEnd),
            shape = CircleShape,
            color = MASTERCARD_ORANGE.copy(alpha = 0.8f)
        ) {}
    }
}
