package com.example.expense_tracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import com.example.expense_tracker.data.nfc.NfcCardDao
import com.example.expense_tracker.data.nfc.NfcCardEntity

@Database(
    entities = [Expense::class, Category::class, Wallet::class, BillReminder::class, NfcCardEntity::class],
    version = 13,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun walletDao(): WalletDao
    abstract fun billReminderDao(): BillReminderDao
    abstract fun nfcCardDao(): NfcCardDao

    class SeedCallback : DatabaseSeedCallback()

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker.db"
                )
                    .addCallback(SeedCallback())
                    .addMigrations(*DatabaseMigrations.ALL)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        val MIGRATION_1_2 = DatabaseMigrations.MIGRATION_1_2
        val MIGRATION_2_3 = DatabaseMigrations.MIGRATION_2_3
        val MIGRATION_3_4 = DatabaseMigrations.MIGRATION_3_4
        val MIGRATION_4_5 = DatabaseMigrations.MIGRATION_4_5
        val MIGRATION_5_6 = DatabaseMigrations.MIGRATION_5_6
        val MIGRATION_6_7 = DatabaseMigrations.MIGRATION_6_7
        val MIGRATION_7_8 = DatabaseMigrations.MIGRATION_7_8
        val MIGRATION_8_9 = DatabaseMigrations.MIGRATION_8_9
        val MIGRATION_9_10 = DatabaseMigrations.MIGRATION_9_10
        val MIGRATION_10_11 = DatabaseMigrations.MIGRATION_10_11
        val MIGRATION_11_12 = DatabaseMigrations.MIGRATION_11_12
        val MIGRATION_12_13 = DatabaseMigrations.MIGRATION_12_13
    }
}
