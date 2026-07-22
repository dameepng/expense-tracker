package com.example.expense_tracker.data

import kotlinx.coroutines.flow.Flow

class RoomBillReminderRepository(
    private val dao: BillReminderDao
) : BillReminderRepository {
    override fun insertReminder(reminder: BillReminder): Long = dao.insertReminder(reminder)
    override fun updateReminder(reminder: BillReminder) = dao.updateReminder(reminder)
    override fun deleteReminder(reminder: BillReminder) = dao.deleteReminder(reminder)
    override fun getAllReminders(): List<BillReminder> = dao.getAllReminders()
    override fun getActiveReminders(): Flow<List<BillReminder>> = dao.getActiveReminders()
    override fun getReminderById(id: Long): BillReminder? = dao.getReminderById(id)
}
