package com.example.expense_tracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

interface UserPreferencesRepository {
    val selectedWalletIdFlow: Flow<Long?>
    val themeModeFlow: Flow<String>
    val currencyFlow: Flow<String>
    val languageFlow: Flow<String>
    
    suspend fun saveSelectedWalletId(walletId: Long?)
    suspend fun saveThemeMode(mode: String)
    suspend fun saveCurrency(currency: String)
    suspend fun saveLanguage(language: String)
}

class UserPreferencesRepositoryImpl(private val dataStore: DataStore<Preferences>) : UserPreferencesRepository {
    private val SELECTED_WALLET_ID = longPreferencesKey("selected_wallet_id")
    private val THEME_MODE = stringPreferencesKey("theme_mode")
    private val CURRENCY = stringPreferencesKey("currency")
    private val LANGUAGE = stringPreferencesKey("language")

    override val selectedWalletIdFlow: Flow<Long?> = dataStore.data
        .map { preferences ->
            val id = preferences[SELECTED_WALLET_ID] ?: -1L
            if (id == -1L) null else id
        }

    override val themeModeFlow: Flow<String> = dataStore.data
        .map { preferences -> preferences[THEME_MODE] ?: "System Default" }

    override val currencyFlow: Flow<String> = dataStore.data
        .map { preferences -> preferences[CURRENCY] ?: "IDR" }

    override val languageFlow: Flow<String> = dataStore.data
        .map { preferences -> preferences[LANGUAGE] ?: "Indonesia" }

    override suspend fun saveSelectedWalletId(walletId: Long?) {
        dataStore.edit { preferences ->
            if (walletId == null) {
                preferences[SELECTED_WALLET_ID] = -1L
            } else {
                preferences[SELECTED_WALLET_ID] = walletId
            }
        }
    }

    override suspend fun saveThemeMode(mode: String) {
        dataStore.edit { preferences -> preferences[THEME_MODE] = mode }
    }

    override suspend fun saveCurrency(currency: String) {
        dataStore.edit { preferences -> preferences[CURRENCY] = currency }
    }

    override suspend fun saveLanguage(language: String) {
        dataStore.edit { preferences -> preferences[LANGUAGE] = language }
    }
}
