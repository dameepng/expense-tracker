package com.example.expense_tracker.ui.profile

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expense_tracker.data.UserPreferencesRepository
import com.example.expense_tracker.data.UserPreferencesRepositoryImpl
import com.example.expense_tracker.data.dataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.expense_tracker.data.AppDatabase
import com.example.expense_tracker.data.ExpenseRepository
import com.example.expense_tracker.data.RoomExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ProfileUiState(
    val themeMode: String = "System Default",
    val currency: String = "IDR",
    val language: String = "Indonesia",
    val isBiometricsEnabled: Boolean = false // to be implemented later
)

class ProfileViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        preferencesRepository.themeModeFlow,
        preferencesRepository.currencyFlow,
        preferencesRepository.languageFlow
    ) { theme, currency, language ->
        ProfileUiState(
            themeMode = theme,
            currency = currency,
            language = language
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState()
    )

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.saveThemeMode(mode)
        }
    }

    fun setCurrency(currency: String) {
        viewModelScope.launch {
            preferencesRepository.saveCurrency(currency)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            preferencesRepository.saveLanguage(language)
        }
    }

    fun exportData(context: Context) {
        viewModelScope.launch {
            try {
                val transactions = withContext(Dispatchers.IO) {
                    expenseRepository.getAllTransactions()
                }

                if (transactions.isEmpty()) {
                    Toast.makeText(context, "Tidak ada data untuk di-export", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val fileName = "Kasflow_Transactions_${System.currentTimeMillis()}.csv"
                val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

                withContext(Dispatchers.IO) {
                    val writer = FileWriter(file)
                    writer.append("Date,Type,Amount,Category,Note,WalletId\n")
                    
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    
                    transactions.forEach { expense ->
                        val dateStr = dateFormat.format(Date(expense.timestamp))
                        writer.append("${dateStr},${expense.type},${expense.amount},${expense.categoryId},\"${expense.description}\",${expense.walletId}\n")
                    }
                    writer.flush()
                    writer.close()
                }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Bagikan Data Transaksi"))

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Gagal mengekspor data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

object ProfileViewModelFactory {
    fun create(application: Application): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                    val userPrefsRepo = UserPreferencesRepositoryImpl(application.dataStore)
                    val db = AppDatabase.getInstance(application)
                    val expenseRepo = RoomExpenseRepository(db.expenseDao())
                    return ProfileViewModel(userPrefsRepo, expenseRepo) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
