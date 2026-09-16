package com.example.expense_tracker.data

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AiMetadataMigrationTest {
    @Test
    fun `version 11 migrates with original rows intact and safe metadata defaults`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "ai-metadata-migration-test.db"
        context.deleteDatabase(databaseName)
        val oldDatabase = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(object : SupportSQLiteOpenHelper.Callback(11) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        createVersion11Schema(db)
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                })
                .build()
        )
        try {
            oldDatabase.writableDatabase.apply {
                execSQL("INSERT INTO categories (id, name, type) VALUES (2, 'Tagihan', 'EXPENSE')")
                execSQL("INSERT INTO wallets (id, name, balance, icon, color) VALUES (7, 'Cash', 500000, '', '')")
                execSQL(
                    "INSERT INTO expenses (id, amount, categoryId, description, timestamp, type, walletId) " +
                        "VALUES (42, 120000, 2, 'langganan lama', 1788886800000, 'EXPENSE', 7)"
                )
                execSQL(
                    "INSERT INTO bill_reminders " +
                        "(id, name, amount, dueDay, categoryId, walletId, isActive, createdAt, lastPaidMonth, isRepeat) " +
                        "VALUES (9, 'Internet', 250000, 10, 2, 7, 1, 1788886800000, '2026-08', 1)"
                )
            }
        } finally {
            oldDatabase.close()
        }

        val migrated = Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
            .addMigrations(AppDatabase.MIGRATION_11_12, AppDatabase.MIGRATION_12_13)
            .allowMainThreadQueries()
            .build()
        try {
            // Opening through Room also validates the complete migrated schema against version 13.
            assertEquals(
                Expense(
                    id = 42,
                    amount = 120_000,
                    categoryId = 2,
                    description = "langganan lama",
                    timestamp = 1788886800000,
                    type = "EXPENSE",
                    walletId = 7,
                    merchant = "",
                    isRecurring = false
                ),
                migrated.expenseDao().getExpenseById(42)
            )
            assertEquals(Category(id = 2, name = "Tagihan", type = "EXPENSE"), migrated.expenseDao().getCategoryById(2))
            assertEquals(Wallet(id = 7, name = "Cash", balance = 500_000), migrated.walletDao().getWalletById(7))
            assertEquals(
                BillReminder(
                    id = 9,
                    name = "Internet",
                    amount = 250_000,
                    dueDay = 10,
                    categoryId = 2,
                    walletId = 7,
                    isActive = true,
                    createdAt = 1788886800000,
                    lastPaidMonth = "2026-08",
                    isRepeat = true
                ),
                migrated.billReminderDao().getReminderById(9)
            )
            assertEquals(13, migrated.openHelper.writableDatabase.version)
        } finally {
            migrated.close()
            context.deleteDatabase(databaseName)
        }
    }

    private fun createVersion11Schema(db: SupportSQLiteDatabase) {
        val schemaJson = checkNotNull(
            javaClass.classLoader!!.getResourceAsStream("com.example.expense_tracker.data.AppDatabase/11.json")
        ) { "Room schema 11 must be available in test resources" }
            .bufferedReader().use { it.readText() }
        val schema = JSONObject(schemaJson).getJSONObject("database")
        val entities = schema.getJSONArray("entities")
        for (index in 0 until entities.length()) {
            val entity = entities.getJSONObject(index)
            val tableName = entity.getString("tableName")
            db.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", tableName))
            val indices = entity.optJSONArray("indices") ?: continue
            for (indexPosition in 0 until indices.length()) {
                db.execSQL(
                    indices.getJSONObject(indexPosition).getString("createSql")
                        .replace("\${TABLE_NAME}", tableName)
                )
            }
        }
        val setupQueries = schema.getJSONArray("setupQueries")
        for (index in 0 until setupQueries.length()) {
            db.execSQL(setupQueries.getString(index))
        }
    }
}
