package com.example.expense_tracker.data

class FakeBillReminderRepository : BillReminderRepository {
    private val reminders = mutableMapOf<Long, BillReminder>()
    private var nextId = 1L

    override fun insertReminder(reminder: BillReminder): Long {
        val id = if (reminder.id == 0L) nextId++ else reminder.id
        reminders[id] = reminder.copy(id = id)
        return id
    }

    override fun updateReminder(reminder: BillReminder) {
        reminders[reminder.id] = reminder
    }

    override fun deleteReminder(reminder: BillReminder) {
        reminders.remove(reminder.id)
    }

    override fun getAllReminders(): List<BillReminder> {
        return reminders.values.toList().sortedByDescending { it.createdAt }
    }

    override fun getActiveReminders(): List<BillReminder> {
        return reminders.values.filter { it.isActive }.sortedBy { it.dueDay }
    }

    override fun getReminderById(id: Long): BillReminder? {
        return reminders[id]
    }
}
