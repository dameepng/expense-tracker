package com.example.expense_tracker.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class BillReminderDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var reminderDao: BillReminderDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        reminderDao = database.billReminderDao()

        // Setup foreign keys dependencies
        val db = database.openHelper.writableDatabase
        db.execSQL("INSERT INTO categories (id, name, type) VALUES (1, 'Tagihan', 'EXPENSE')")
        db.execSQL("INSERT INTO wallets (id, name, balance, icon, color) VALUES (1, 'Cash', 0, '', '')")
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetReminder() {
        val reminder = BillReminder(
            name = "Listrik",
            amount = 150000,
            dueDay = 20,
            categoryId = 1,
            walletId = 1
        )
        val id = reminderDao.insertReminder(reminder)
        
        val retrieved = reminderDao.getReminderById(id)
        assertNotNull(retrieved)
        assertEquals("Listrik", retrieved?.name)
        assertEquals(150000L, retrieved?.amount)
        assertEquals(20, retrieved?.dueDay)
    }

    @Test
    fun updateReminder() {
        val reminder = BillReminder(
            name = "Listrik",
            amount = 150000,
            dueDay = 20,
            categoryId = 1,
            walletId = 1
        )
        val id = reminderDao.insertReminder(reminder)
        
        val retrieved = reminderDao.getReminderById(id)
        assertNotNull(retrieved)
        
        val updated = retrieved!!.copy(amount = 200000, dueDay = 21, isActive = false)
        reminderDao.updateReminder(updated)
        
        val newRetrieved = reminderDao.getReminderById(id)
        assertEquals(200000L, newRetrieved?.amount)
        assertEquals(21, newRetrieved?.dueDay)
        assertEquals(false, newRetrieved?.isActive)
    }

    @Test
    fun deleteReminder() {
        val reminder = BillReminder(
            name = "Listrik",
            amount = 150000,
            dueDay = 20,
            categoryId = 1,
            walletId = 1
        )
        val id = reminderDao.insertReminder(reminder)
        
        val retrieved = reminderDao.getReminderById(id)
        assertNotNull(retrieved)
        
        reminderDao.deleteReminder(retrieved!!)
        val newRetrieved = reminderDao.getReminderById(id)
        assertNull(newRetrieved)
    }

    @Test
    fun getAllReminders() {
        reminderDao.insertReminder(BillReminder(name = "A", amount = 100, dueDay = 1, categoryId = 1, walletId = 1))
        reminderDao.insertReminder(BillReminder(name = "B", amount = 200, dueDay = 2, categoryId = 1, walletId = 1))
        
        val all = reminderDao.getAllReminders()
        assertEquals(2, all.size)
    }

    @Test
    fun getActiveReminders() {
        reminderDao.insertReminder(BillReminder(name = "A", amount = 100, dueDay = 10, categoryId = 1, walletId = 1, isActive = true))
        reminderDao.insertReminder(BillReminder(name = "B", amount = 200, dueDay = 5, categoryId = 1, walletId = 1, isActive = false))
        reminderDao.insertReminder(BillReminder(name = "C", amount = 300, dueDay = 20, categoryId = 1, walletId = 1, isActive = true))
        
        val active = reminderDao.getActiveReminders()
        assertEquals(2, active.size)
        // verify order by dueDay ASC
        assertEquals("A", active[0].name)
        assertEquals("C", active[1].name)
    }
}
