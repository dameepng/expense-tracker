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

data class ProfileUiState(
    val themeMode: String = "System Default",
    val currency: String = "IDR",
    val language: String = "Indonesia",
    val isBiometricsEnabled: Boolean = false // to be implemented later
)

class ProfileViewModel(
    private val preferencesRepository: UserPreferencesRepository
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
}

object ProfileViewModelFactory {
    fun create(application: Application): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                    val repo = UserPreferencesRepositoryImpl(application.dataStore)
                    return ProfileViewModel(repo) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
