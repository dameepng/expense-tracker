package com.example.expense_tracker.data

interface BillReminderRepository {
    fun insertReminder(reminder: BillReminder): Long
    fun updateReminder(reminder: BillReminder)
    fun deleteReminder(reminder: BillReminder)
    fun getAllReminders(): List<BillReminder>
    fun getActiveReminders(): List<BillReminder>
    fun getReminderById(id: Long): BillReminder?
}
