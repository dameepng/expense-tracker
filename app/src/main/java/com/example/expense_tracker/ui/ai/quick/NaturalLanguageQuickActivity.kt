package com.example.expense_tracker.ui.ai.quick

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expense_tracker.R
import com.example.expense_tracker.data.AppDatabase
import com.example.expense_tracker.data.Wallet
import com.example.expense_tracker.data.ai.NaturalLanguageQuickProcessor
import com.example.expense_tracker.data.ai.ParsedTransaction
import com.example.expense_tracker.ui.CurrencyFormatter
import com.example.expense_tracker.ui.theme.Expense_trackerTheme
import com.example.expense_tracker.widget.NaturalLanguageWidgetProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class NaturalLanguageQuickActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val autoSpeech = intent.getBooleanExtra(NaturalLanguageWidgetProvider.EXTRA_AUTO_SPEECH, false)
        val initialWalletId = intent.getLongExtra(NaturalLanguageWidgetProvider.EXTRA_WALLET_ID, -1L)

        setContent {
            Expense_trackerTheme {
                // Dimmed translucent overlay background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { finishWithFade() }
                        ),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    NaturalLanguageQuickSheet(
                        autoSpeech = autoSpeech,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {} // Consume click
                            )
                            .navigationBarsPadding()
                            .imePadding(),
                        initialWalletId = initialWalletId,
                        onDismiss = { finishWithFade() },
                        onSuccessHaptic = { triggerHapticFeedback() }
                    )
                }
            }
        }
    }

    private fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(100)
            }
        } catch (_: Exception) {}
    }

    private fun finishWithFade() {
        finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }
}

@Composable
fun NaturalLanguageQuickSheet(
    autoSpeech: Boolean,
    modifier: Modifier = Modifier,
    initialWalletId: Long = -1L,
    onDismiss: () -> Unit,
    onSuccessHaptic: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    val micPrompt = stringResource(R.string.nl_quick_mic_prompt)
    val genericErrorMessage = stringResource(R.string.nl_quick_error_generic)

    var inputText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successResult by remember { mutableStateOf<ParsedTransaction?>(null) }

    // Load available wallets from Room
    val database = remember { AppDatabase.getInstance(context) }
    val walletsFlow = remember {
        try {
            database.walletDao().getAllWallets()
        } catch (_: Exception) {
            flowOf(emptyList<Wallet>())
        }
    }
    val wallets by walletsFlow.collectAsState(initial = emptyList())
    var selectedWalletId by remember { mutableStateOf<Long?>(null) }
    var showWalletDialog by remember { mutableStateOf(false) }

    // Select initial/saved wallet when loaded
    LaunchedEffect(wallets) {
        if (selectedWalletId == null && wallets.isNotEmpty()) {
            selectedWalletId = if (initialWalletId > 0 && wallets.any { it.id == initialWalletId }) {
                initialWalletId
            } else {
                val prefs = context.getSharedPreferences(NaturalLanguageWidgetProvider.PREFS_NAME, Context.MODE_PRIVATE)
                val savedId = prefs.getLong(NaturalLanguageWidgetProvider.KEY_SELECTED_WALLET_ID, -1L)
                wallets.find { it.id == savedId }?.id ?: wallets.first().id
            }
        }
    }

    // Speech-to-text launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                inputText = spokenText
            }
        }
    }

    val launchSpeech = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
            putExtra(RecognizerIntent.EXTRA_PROMPT, micPrompt)
        }
        try {
            speechLauncher.launch(intent)
        } catch (_: Exception) {}
    }

    // Auto-launch speech or auto-focus keyboard
    LaunchedEffect(Unit) {
        if (autoSpeech) {
            launchSpeech()
        } else {
            delay(150)
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    // Auto-dismiss after success celebration
    LaunchedEffect(successResult) {
        if (successResult != null) {
            onSuccessHaptic()
            delay(1400)
            onDismiss()
        }
    }

    val handleRecordTransaction = {
        val textToProcess = inputText.trim()
        if (textToProcess.isNotBlank() && !isProcessing) {
            isProcessing = true
            errorMessage = null
            scope.launch {
                val result = NaturalLanguageQuickProcessor.processAndSave(
                    context = context,
                    text = textToProcess,
                    walletId = selectedWalletId
                )
                result.fold(
                    onSuccess = { parsed ->
                        isProcessing = false
                        successResult = parsed
                    },
                    onFailure = { error ->
                        isProcessing = false
                        errorMessage = error.localizedMessage ?: genericErrorMessage
                    }
                )
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)),
        color = Color(0xFF151B26),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag Handle / Pill
            Box(
                modifier = Modifier
                    .size(width = 38.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF334155))
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF2563EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_widget_sparkle),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = stringResource(R.string.nl_quick_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = stringResource(R.string.nl_quick_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.nl_quick_close),
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SUCCESS STATE
            if (successResult != null) {
                val parsed = successResult!!
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF0F291E))
                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.nl_quick_success),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val amountFormatted = CurrencyFormatter.format(parsed.amount)
                    val merchantSuffix = if (parsed.merchant.isNotBlank()) {
                        " " + stringResource(R.string.nl_quick_at_merchant, parsed.merchant)
                    } else ""
                    Text(
                        text = "$amountFormatted$merchantSuffix",
                        color = Color(0xFFE2E8F0),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    if (parsed.note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "\"${parsed.note}\"",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.nl_quick_notification_sent),
                        color = Color(0xFF34D399),
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                // INPUT & PROCESSING STATE

                // Text Input Field
                OutlinedTextField(
                    value = inputText,
                    onValueChange = {
                        inputText = it
                        if (errorMessage != null) errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.nl_quick_hint),
                            color = Color(0xFF64748B),
                            fontSize = 14.sp
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { launchSpeech() },
                            enabled = !isProcessing
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = stringResource(R.string.nl_quick_mic_desc),
                                tint = if (inputText.isNotBlank()) Color(0xFF60A5FA) else Color(0xFF94A3B8)
                            )
                        }
                    },
                    maxLines = 3,
                    enabled = !isProcessing,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0D131D),
                        unfocusedContainerColor = Color(0xFF0D131D),
                        disabledContainerColor = Color(0xFF0D131D),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFF283244)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { handleRecordTransaction() })
                )

                // Error Message Box
                AnimatedVisibility(visible = errorMessage != null, enter = fadeIn(), exit = fadeOut()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2D1214))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFFCA5A5),
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Bar: Wallet Selector (Left) & Submit Button (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Wallet Chip & Selection Dialog
                    val currentWallet = wallets.find { it.id == selectedWalletId } ?: wallets.firstOrNull()
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF2E3A52), RoundedCornerShape(12.dp))
                            .clickable(enabled = !isProcessing) {
                                showWalletDialog = true
                            },
                        color = Color(0xFF1E2638),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentWallet?.name ?: stringResource(R.string.nav_wallet),
                                color = Color(0xFFCBD5E1),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = stringResource(R.string.input_choose_wallet),
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (showWalletDialog) {
                        AlertDialog(
                            onDismissRequest = { showWalletDialog = false },
                            containerColor = Color(0xFF151B26),
                            tonalElevation = 6.dp,
                            shape = RoundedCornerShape(20.dp),
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(28.dp)
                                )
                            },
                            title = {
                                Text(
                                    text = stringResource(R.string.input_choose_wallet),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            },
                            text = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    if (wallets.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.no_wallet),
                                            color = Color(0xFF94A3B8),
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(vertical = 12.dp)
                                        )
                                    } else {
                                        wallets.forEach { wallet ->
                                            val isSelected = wallet.id == selectedWalletId
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        selectedWalletId = wallet.id
                                                        showWalletDialog = false
                                                        context.getSharedPreferences(NaturalLanguageWidgetProvider.PREFS_NAME, Context.MODE_PRIVATE)
                                                            .edit {
                                                                putLong(NaturalLanguageWidgetProvider.KEY_SELECTED_WALLET_ID, wallet.id)
                                                            }
                                                    },
                                                color = if (isSelected) Color(0xFF1E2D4A) else Color(0xFF0F1724),
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (isSelected) Color(0xFF2563EB) else Color(0xFF233044)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.AccountBalanceWallet,
                                                            contentDescription = null,
                                                            tint = if (isSelected) Color(0xFF60A5FA) else Color(0xFF64748B),
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Text(
                                                            text = wallet.name,
                                                            color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            fontSize = 15.sp
                                                        )
                                                    }
                                                    if (isSelected) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = stringResource(R.string.nl_quick_wallet_selected_desc),
                                                            tint = Color(0xFF60A5FA),
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showWalletDialog = false }) {
                                    Text(text = stringResource(R.string.cancel), color = Color(0xFF60A5FA), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        )
                    }

                    // Submit / Catat Button
                    Button(
                        onClick = { handleRecordTransaction() },
                        enabled = inputText.isNotBlank() && !isProcessing,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            disabledContainerColor = Color(0xFF1E3A8A).copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.height(44.dp)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.nl_quick_processing),
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.nl_quick_btn_submit),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
