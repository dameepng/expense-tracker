package com.example.expense_tracker.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeBillReminderRepository : BillReminderRepository {
    private val _remindersFlow = MutableStateFlow<Map<Long, BillReminder>>(emptyMap())
    private var nextId = 1L

    override fun insertReminder(reminder: BillReminder): Long {
        val id = if (reminder.id == 0L) nextId++ else reminder.id
        _remindersFlow.update { it + (id to reminder.copy(id = id)) }
        return id
    }

    override fun updateReminder(reminder: BillReminder) {
        _remindersFlow.update { it + (reminder.id to reminder) }
    }

    override fun deleteReminder(reminder: BillReminder) {
        _remindersFlow.update { it - reminder.id }
    }

    override fun getAllReminders(): List<BillReminder> {
        return _remindersFlow.value.values.toList().sortedByDescending { it.createdAt }
    }

    override fun getActiveReminders(): Flow<List<BillReminder>> {
        return _remindersFlow.map { map ->
            map.values.filter { it.isActive || (!it.isRepeat && it.lastPaidMonth != null) }.sortedBy { it.dueDay }
        }
    }

    override fun getReminderById(id: Long): BillReminder? {
        return _remindersFlow.value[id]
    }
}
