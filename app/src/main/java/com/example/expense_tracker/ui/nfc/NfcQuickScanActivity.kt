package com.example.expense_tracker.ui.nfc

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.expense_tracker.BuildConfig
import com.example.expense_tracker.data.AppDatabase
import com.example.expense_tracker.data.nfc.EmoneyIsoDepParser
import com.example.expense_tracker.data.nfc.NfcCardEntity
import com.example.expense_tracker.data.nfc.NfcCardResult
import com.example.expense_tracker.data.nfc.NfcCardTransaction
import com.example.expense_tracker.data.nfc.NfcCardType
import com.example.expense_tracker.ui.theme.Expense_trackerTheme
import com.example.expense_tracker.widget.EmoneyWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
class NfcQuickScanActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var scanState by mutableStateOf<NfcScanState>(NfcScanState.Scanning)
    private var isFinishingActivity = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            scanState = NfcScanState.Error("Perangkat ini tidak memiliki sensor NFC hardware.")
        } else if (!nfcAdapter!!.isEnabled) {
            scanState = NfcScanState.Error("NFC belum diaktifkan. Silakan aktifkan NFC di Pengaturan HP Anda.")
        }

        // Handle initial intent if launched via NFC tech discovered
        intent?.let { handleNfcIntent(it) }

        setContent {
            Expense_trackerTheme {
                val coroutineScope = rememberCoroutineScope()
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                fun dismissSheet() {
                    coroutineScope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            finishWithFade()
                        }
                    }
                }

                ModalBottomSheet(
                    onDismissRequest = { finishWithFade() },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    dragHandle = { BottomSheetDefaults.DragHandle() }
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        NfcQuickScanSheetContent(
                            state = scanState,
                            onDismiss = { dismissSheet() },
                            onRetry = {
                                scanState = NfcScanState.Scanning
                            },
                            onSimulateScan = if (BuildConfig.DEBUG || nfcAdapter == null) {
                                { type ->
                                    coroutineScope.launch {
                                        simulateScan(type)
                                    }
                                }
                            } else null,
                            modifier = Modifier.widthIn(max = 560.dp)
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        enableNfcReaderMode()
    }

    override fun onPause() {
        super.onPause()
        disableNfcReaderMode()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNfcIntent(intent)
    }

    private fun enableNfcReaderMode() {
        val adapter = nfcAdapter ?: return
        if (!adapter.isEnabled) return

        val flags = NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

        val options = Bundle().apply {
            putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
        }

        adapter.enableReaderMode(
            this,
            { tag -> processTag(tag) },
            flags,
            options
        )
    }

    private fun disableNfcReaderMode() {
        nfcAdapter?.disableReaderMode(this)
    }

    private fun handleNfcIntent(intent: Intent) {
        val action = intent.action ?: return
        if (action == NfcAdapter.ACTION_TECH_DISCOVERED || action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }
            if (tag != null) {
                processTag(tag)
            }
        }
    }

    private fun processTag(tag: Tag) {
        val isoDep = IsoDep.get(tag)
        if (isoDep == null) {
            val techList = tag.techList.joinToString(", ") { it.substringAfterLast('.') }
            lifecycleScope.launch(Dispatchers.Main) {
                scanState = NfcScanState.Error("Kartu terdeteksi ($techList) tetapi tidak mendukung protokol ISO-DEP. Pastikan kartu TapCash/e-Money Anda menggunakan chip standar modern.")
            }
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val result = EmoneyIsoDepParser.readCard(isoDep, tag.id)
            withContext(Dispatchers.Main) {
                result.fold(
                    onSuccess = { cardResult ->
                        triggerHapticFeedback()
                        saveCardAndNotifyWidget(cardResult)
                        scanState = NfcScanState.Success(cardResult)
                    },
                    onFailure = { error ->
                        scanState = NfcScanState.Error(
                            error.message ?: "Gagal membaca kartu. Pastikan posisi kartu pas di belakang HP."
                        )
                    }
                )
            }
        }
    }

    private suspend fun simulateScan(type: NfcCardType) {
        val simulatedCard = when (type) {
            NfcCardType.MANDIRI_EMONEY -> NfcCardResult(
                cardNumber = "6032918239014812",
                balance = 74_500L,
                cardType = NfcCardType.MANDIRI_EMONEY,
                transactions = listOf(
                    NfcCardTransaction(1, 15_000, "EXPENSE", System.currentTimeMillis() - 7200_000L, "Gerbang Tol Cilandak Utama"),
                    NfcCardTransaction(2, 50_000, "INCOME", System.currentTimeMillis() - 86400_000L, "Top Up Livin' by Mandiri"),
                    NfcCardTransaction(3, 3_500, "EXPENSE", System.currentTimeMillis() - 86400_000L * 2, "TransJakarta Koridor 1"),
                    NfcCardTransaction(4, 12_000, "EXPENSE", System.currentTimeMillis() - 86400_000L * 3, "Parkir Mall Grand Indonesia")
                )
            )
            NfcCardType.BNI_TAPCASH -> NfcCardResult(
                cardNumber = "7546029381729401",
                balance = 125_000L,
                cardType = NfcCardType.BNI_TAPCASH,
                transactions = listOf(
                    NfcCardTransaction(1, 8_000, "EXPENSE", System.currentTimeMillis() - 3600_000L, "KRL Manggarai - Bogor"),
                    NfcCardTransaction(2, 100_000, "INCOME", System.currentTimeMillis() - 86400_000L, "Top Up ATM BNI"),
                    NfcCardTransaction(3, 5_000, "EXPENSE", System.currentTimeMillis() - 86400_000L * 2, "Parkir Stasiun Tebet")
                )
            )
            else -> NfcCardResult(
                cardNumber = "9988776655443322",
                balance = 50_000L,
                cardType = NfcCardType.UNKNOWN,
                transactions = emptyList()
            )
        }
        triggerHapticFeedback()
        saveCardAndNotifyWidget(simulatedCard)
        scanState = NfcScanState.Success(simulatedCard)
    }

    private fun saveCardAndNotifyWidget(card: NfcCardResult) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val entity = NfcCardEntity(
                    cardNumber = card.cardNumber,
                    cardType = card.cardType.name,
                    balance = card.balance,
                    lastScannedAt = card.scannedAt
                )
                db.nfcCardDao().upsertCard(entity)
                EmoneyWidgetProvider.updateAllWidgets(applicationContext)
            } catch (_: Exception) {}
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
        if (isFinishingActivity) return
        isFinishingActivity = true
        finishAndRemoveTask()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }

    override fun finish() {
        if (isFinishingActivity) return
        isFinishingActivity = true
        finishAndRemoveTask()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }
}
