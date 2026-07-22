package com.example.expense_tracker.data

import kotlinx.coroutines.flow.Flow

interface BillReminderRepository {
    fun insertReminder(reminder: BillReminder): Long
    fun updateReminder(reminder: BillReminder)
    fun deleteReminder(reminder: BillReminder)
    fun getAllReminders(): List<BillReminder>
    fun getActiveReminders(): Flow<List<BillReminder>>
    fun getReminderById(id: Long): BillReminder?
}
