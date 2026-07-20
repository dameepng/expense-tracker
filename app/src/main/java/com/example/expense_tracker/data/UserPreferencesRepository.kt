package com.example.expense_tracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

interface UserPreferencesRepository {
    val selectedWalletIdFlow: Flow<Long?>
    suspend fun saveSelectedWalletId(walletId: Long?)
}

class UserPreferencesRepositoryImpl(private val dataStore: DataStore<Preferences>) : UserPreferencesRepository {
    private val SELECTED_WALLET_ID = longPreferencesKey("selected_wallet_id")

    override val selectedWalletIdFlow: Flow<Long?> = dataStore.data
        .map { preferences ->
            val id = preferences[SELECTED_WALLET_ID] ?: -1L
            if (id == -1L) null else id
        }

    override suspend fun saveSelectedWalletId(walletId: Long?) {
        dataStore.edit { preferences ->
            if (walletId == null) {
                preferences[SELECTED_WALLET_ID] = -1L
            } else {
                preferences[SELECTED_WALLET_ID] = walletId
            }
        }
    }
}
