package com.example.expense_tracker.data

import android.content.ContentValues
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

@Database(
    entities = [Expense::class, Category::class, Wallet::class, BillReminder::class],
    version = 9,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun walletDao(): WalletDao
    abstract fun billReminderDao(): BillReminderDao

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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expenses ADD COLUMN description TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expenses ADD COLUMN type TEXT NOT NULL DEFAULT 'EXPENSE'")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS wallets (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        balance INTEGER NOT NULL DEFAULT 0,
                        icon TEXT NOT NULL DEFAULT '',
                        color TEXT NOT NULL DEFAULT ''
                    )
                """.trimIndent())
            }
        }
        
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expenses ADD COLUMN walletId INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add type column to categories
                db.execSQL("ALTER TABLE categories ADD COLUMN type TEXT NOT NULL DEFAULT 'EXPENSE'")
                // Update "Lainnya" to BOTH so it appears in both views
                db.execSQL("UPDATE categories SET type = 'BOTH' WHERE name = 'Lainnya'")
                // Insert income categories
                val incomeCategories = listOf("Gaji", "Freelance", "Bonus", "Transfer Masuk")
                for (name in incomeCategories) {
                    val values = ContentValues().apply {
                        put("name", name)
                        put("type", "INCOME")
                    }
                    db.insert("categories", 0, values)
                }
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS bill_reminders (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        amount INTEGER NOT NULL,
                        dueDay INTEGER NOT NULL,
                        categoryId INTEGER NOT NULL,
                        walletId INTEGER NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(categoryId) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(walletId) REFERENCES wallets(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_bill_reminders_categoryId ON bill_reminders(categoryId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_bill_reminders_walletId ON bill_reminders(walletId)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE bill_reminders ADD COLUMN lastPaidMonth TEXT")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE wallets ADD COLUMN cardNumber TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE wallets ADD COLUMN cardHolderName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE wallets ADD COLUMN cardExpiry TEXT NOT NULL DEFAULT ''")
            }
        }
    }

    class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val presetCategories = listOf(
                "Makanan" to "EXPENSE",
                "Transport" to "EXPENSE",
                "Belanja" to "EXPENSE",
                "Hiburan" to "EXPENSE",
                "Tagihan" to "EXPENSE",
                "Kesehatan" to "EXPENSE",
                "Lainnya" to "BOTH",
                "Gaji" to "INCOME",
                "Freelance" to "INCOME",
                "Bonus" to "INCOME",
                "Transfer Masuk" to "INCOME"
            )
            db.beginTransaction()
            try {
                for ((name, type) in presetCategories) {
                    val values = ContentValues().apply {
                        put("name", name)
                        put("type", type)
                    }
                    db.insert("categories", 0, values)
                }
                
                val walletValues = ContentValues().apply {
                    put("name", "Cash")
                    put("balance", 0)
                    put("icon", "")
                    put("color", "")
                    put("cardNumber", "")
                    put("cardHolderName", "")
                    put("cardExpiry", "")
                }
                db.insert("wallets", 0, walletValues)
                
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }
}
