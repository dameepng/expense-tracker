# ISSUE-050: Database Index untuk Query Performa

## Priority: 🟡 Medium
## Type: Performance / Database
## Status: Closed

## Deskripsi
Beberapa query yang sering dieksekusi melakukan filter pada kolom `timestamp`, `type`, dan `walletId`, namun hanya `categoryId` yang memiliki index di tabel `expenses`.

Tanpa index, Room (SQLite) harus melakukan **full table scan** setiap kali query dengan `WHERE timestamp >= ? AND timestamp < ?` dijalankan. Semakin banyak transaksi, semakin lambat.

## Query yang Terpengaruh
- `getTotalExpense(startTime, endTime)` — filter `timestamp + type`
- `getTotalIncome(startTime, endTime)` — filter `timestamp + type`
- `getAllTransactionsBetween(startTime, endTime)` — filter `timestamp`
- `getTotalExpenseByWallet(walletId, startTime, endTime)` — filter `walletId + timestamp + type`
- `getTransactionsByWallet(walletId, startTime, endTime)` — filter `walletId + timestamp`
- `getBreakdownByCategory(startTime, endTime)` — JOIN + filter `timestamp + type`

## Solusi
Tambahkan composite index ke entity `Expense`:

```kotlin
@Entity(
    tableName = "expenses",
    indices = [
        Index("categoryId"),
        Index("timestamp"),
        Index("walletId", "timestamp"),
        Index("type", "timestamp")
    ],
    ...
)
```

Ini memerlukan database migration baru (versi 9 → 10):

```kotlin
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_timestamp ON expenses(timestamp)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_walletId_timestamp ON expenses(walletId, timestamp)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_type_timestamp ON expenses(type, timestamp)")
    }
}
```

## File yang Terpengaruh
- `Expense.kt`: Tambahkan index di anotasi `@Entity`
- `AppDatabase.kt`: Naikkan versi database, tambahkan migration

## Dampak
- Query aggregate (SUM) dan filter range timestamp akan menggunakan index, mempercepat akses hingga 10-50x untuk dataset besar.
- Tidak ada perubahan API/UI — murni di layer database.
