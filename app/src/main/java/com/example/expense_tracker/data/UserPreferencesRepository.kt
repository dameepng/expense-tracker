package com.example.expense_tracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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
    val isBiometricsEnabledFlow: Flow<Boolean>
    
    val userNameFlow: Flow<String>
    val userPhotoUriFlow: Flow<String?>
    
    suspend fun saveSelectedWalletId(walletId: Long?)
    suspend fun saveThemeMode(mode: String)
    suspend fun saveCurrency(currency: String)
    suspend fun saveLanguage(language: String)
    suspend fun saveBiometricsEnabled(enabled: Boolean)
    suspend fun saveUserProfile(name: String, photoUri: String?)
    suspend fun clearAllPreferences()
}

class UserPreferencesRepositoryImpl(private val dataStore: DataStore<Preferences>) : UserPreferencesRepository {
    private val selectedWalletIdKey = longPreferencesKey("selected_wallet_id")
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val currencyKey = stringPreferencesKey("currency")
    private val languageKey = stringPreferencesKey("language")
    private val isBiometricsEnabledKey = booleanPreferencesKey("is_biometrics_enabled")
    
    private val userNameKey = stringPreferencesKey("user_name")
    private val userPhotoUriKey = stringPreferencesKey("user_photo_uri")

    override val selectedWalletIdFlow: Flow<Long?> = dataStore.data
        .map { preferences ->
            val id = preferences[selectedWalletIdKey] ?: -1L
            if (id == -1L) null else id
        }

    override val themeModeFlow: Flow<String> = dataStore.data
        .map { preferences -> preferences[themeModeKey] ?: "System Default" }

    override val currencyFlow: Flow<String> = dataStore.data
        .map { preferences -> preferences[currencyKey] ?: "IDR" }

    override val languageFlow: Flow<String> = dataStore.data
        .map { preferences -> preferences[languageKey] ?: "Indonesia" }

    override val isBiometricsEnabledFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[isBiometricsEnabledKey] ?: false }

    override val userNameFlow: Flow<String> = dataStore.data
        .map { preferences -> preferences[userNameKey] ?: "Adam" }

    override val userPhotoUriFlow: Flow<String?> = dataStore.data
        .map { preferences -> preferences[userPhotoUriKey] }

    override suspend fun saveSelectedWalletId(walletId: Long?) {
        dataStore.edit { preferences ->
            if (walletId == null) {
                preferences[selectedWalletIdKey] = -1L
            } else {
                preferences[selectedWalletIdKey] = walletId
            }
        }
    }

    override suspend fun saveThemeMode(mode: String) {
        dataStore.edit { preferences -> preferences[themeModeKey] = mode }
    }

    override suspend fun saveCurrency(currency: String) {
        dataStore.edit { preferences -> preferences[currencyKey] = currency }
    }

    override suspend fun saveLanguage(language: String) {
        dataStore.edit { preferences -> preferences[languageKey] = language }
    }

    override suspend fun saveBiometricsEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[isBiometricsEnabledKey] = enabled }
    }

    override suspend fun saveUserProfile(name: String, photoUri: String?) {
        dataStore.edit { preferences ->
            preferences[userNameKey] = name
            if (photoUri != null) {
                preferences[userPhotoUriKey] = photoUri
            } else {
                preferences.remove(userPhotoUriKey)
            }
        }
    }
    
    override suspend fun clearAllPreferences() {
        dataStore.edit { it.clear() }
    }
}
