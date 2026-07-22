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
    val userStatusFlow: Flow<String>
    val userPhotoUriFlow: Flow<String?>
    
    suspend fun saveSelectedWalletId(walletId: Long?)
    suspend fun saveThemeMode(mode: String)
    suspend fun saveCurrency(currency: String)
    suspend fun saveLanguage(language: String)
    suspend fun saveBiometricsEnabled(enabled: Boolean)
    suspend fun saveUserProfile(name: String, status: String, photoUri: String?)
    suspend fun clearAllPreferences()
}

class UserPreferencesRepositoryImpl(private val dataStore: DataStore<Preferences>) : UserPreferencesRepository {
    private val SELECTED_WALLET_ID = longPreferencesKey("selected_wallet_id")
    private val THEME_MODE = stringPreferencesKey("theme_mode")
    private val CURRENCY = stringPreferencesKey("currency")
    private val LANGUAGE = stringPreferencesKey("language")
    private val IS_BIOMETRICS_ENABLED = booleanPreferencesKey("is_biometrics_enabled")
    
    private val USER_NAME = stringPreferencesKey("user_name")
    private val USER_STATUS = stringPreferencesKey("user_status")
    private val USER_PHOTO_URI = stringPreferencesKey("user_photo_uri")

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

    override val isBiometricsEnabledFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[IS_BIOMETRICS_ENABLED] ?: false }

    override val userNameFlow: Flow<String> = dataStore.data
        .map { preferences -> preferences[USER_NAME] ?: "Adam" }

    override val userStatusFlow: Flow<String> = dataStore.data
        .map { preferences -> preferences[USER_STATUS] ?: "Premium Member" }

    override val userPhotoUriFlow: Flow<String?> = dataStore.data
        .map { preferences -> preferences[USER_PHOTO_URI] }

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

    override suspend fun saveBiometricsEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[IS_BIOMETRICS_ENABLED] = enabled }
    }

    override suspend fun saveUserProfile(name: String, status: String, photoUri: String?) {
        dataStore.edit { preferences ->
            preferences[USER_NAME] = name
            preferences[USER_STATUS] = status
            if (photoUri == null) {
                preferences.remove(USER_PHOTO_URI)
            } else {
                preferences[USER_PHOTO_URI] = photoUri
            }
        }
    }
    
    override suspend fun clearAllPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
