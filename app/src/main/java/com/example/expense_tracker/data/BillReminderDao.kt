package com.example.expense_tracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BillReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReminder(reminder: BillReminder): Long

    @Update
    fun updateReminder(reminder: BillReminder)

    @Delete
    fun deleteReminder(reminder: BillReminder)

    @Query("SELECT * FROM bill_reminders ORDER BY createdAt DESC")
    fun getAllReminders(): List<BillReminder>

    @Query("SELECT * FROM bill_reminders WHERE isActive = 1 ORDER BY dueDay ASC")
    fun getActiveReminders(): Flow<List<BillReminder>>

    @Query("SELECT * FROM bill_reminders WHERE id = :id")
    fun getReminderById(id: Long): BillReminder?
}
