package com.example.expense_tracker.data.nfc

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NfcCardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCard(card: NfcCardEntity)

    @Query("SELECT * FROM nfc_cards ORDER BY lastScannedAt DESC LIMIT 1")
    suspend fun getLatestCard(): NfcCardEntity?

    @Query("SELECT * FROM nfc_cards ORDER BY lastScannedAt DESC LIMIT 1")
    fun getLatestCardFlow(): Flow<NfcCardEntity?>

    @Query("SELECT * FROM nfc_cards ORDER BY lastScannedAt DESC")
    fun getAllCardsFlow(): Flow<List<NfcCardEntity>>

    @Query("DELETE FROM nfc_cards WHERE cardNumber = :cardNumber")
    suspend fun deleteCard(cardNumber: String)
}
