# ISSUE-009: Transaction-Wallet Relationship

**Priority:** 🔴 High  
**Type:** Feature (Data + UI)  
**Sprint:** Sprint 2  
**Estimated Effort:** Large  
**Depends On:** ISSUE-007

---

## Deskripsi

Menghubungkan setiap transaksi (income/expense) ke wallet tertentu. Saat user membuat transaksi baru, mereka harus memilih wallet mana yang digunakan. Ini melibatkan perubahan di data layer (migration, DAO query) dan UI layer (InputScreen).

## Kondisi Saat Ini

- Entity [Expense.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/data/Expense.kt) tidak memiliki referensi ke wallet
- [InputScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/input/InputScreen.kt) tidak menampilkan pilihan wallet
- [ExpenseDao.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/data/ExpenseDao.kt) tidak bisa query transaksi per-wallet
- Tabel `wallets` sudah ada dari ISSUE-007

## Perubahan yang Dibutuhkan

### Prinsip: Clean Architecture · Clean Code · KISS · YAGNI · CI GitHub Actions

### Data Layer

#### [MODIFY] [Expense.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/data/Expense.kt)
- Tambah field `walletId: Long = 1L` (default ke wallet pertama / "Cash")
- **KISS:** Default `1L` agar data lama tetap kompatibel tanpa logic tambahan
- **Tidak perlu** foreign key constraint ke `wallets` — cukup Long ID saja (KISS)

#### [MODIFY] [AppDatabase.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/data/AppDatabase.kt)
- Bump version ke **5** (setelah v4 dari ISSUE-007)
- Tambahkan `MIGRATION_4_5`:
  ```sql
  ALTER TABLE expenses ADD COLUMN walletId INTEGER NOT NULL DEFAULT 1
  ```
- Semua transaksi lama akan otomatis terhubung ke wallet default (id = 1)

#### [MODIFY] [ExpenseDao.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/data/ExpenseDao.kt)
Tambah query baru untuk filter per-wallet:
```kotlin
@Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE walletId = :walletId AND timestamp >= :startTime AND timestamp < :endTime AND type = 'EXPENSE'")
fun getTotalExpenseByWallet(walletId: Long, startTime: Long, endTime: Long): Long

@Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE walletId = :walletId AND timestamp >= :startTime AND timestamp < :endTime AND type = 'INCOME'")
fun getTotalIncomeByWallet(walletId: Long, startTime: Long, endTime: Long): Long

@Query("SELECT * FROM expenses WHERE walletId = :walletId AND timestamp >= :startTime AND timestamp < :endTime ORDER BY timestamp DESC")
fun getTransactionsByWallet(walletId: Long, startTime: Long, endTime: Long): List<Expense>
```
- **Clean Code:** Naming konsisten — `ByWallet` suffix menandakan filter tambahan

#### [MODIFY] [ExpenseRepository.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/data/ExpenseRepository.kt)
- Tambah method baru yang delegate ke DAO query per-wallet

#### [MODIFY] [RoomExpenseRepository.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/data/RoomExpenseRepository.kt)
- Implementasi method baru

#### [MODIFY] [ExpenseWithCategory.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/data/ExpenseWithCategory.kt)
- Tambah field `walletId: Long = 1L`

### UI Layer — Input Screen

#### [MODIFY] [InputUiState.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/input/InputUiState.kt)
- Tambah field `wallets: List<Wallet> = emptyList()`
- Tambah field `selectedWalletId: Long? = null`

#### [MODIFY] [InputViewModel.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/input/InputViewModel.kt)
- Load wallets dari `WalletRepository` saat init
- Tambah `fun onWalletSelected(walletId: Long)`
- Saat save, sertakan `walletId` ke entity `Expense`
- Auto-select wallet pertama jika hanya ada satu wallet

#### [MODIFY] [InputRepository.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/input/InputRepository.kt)
- Update `insertExpense()` signature untuk terima parameter `walletId`

#### [MODIFY] [RoomInputRepository.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/data/RoomInputRepository.kt)
- Pass `walletId` saat membuat `Expense` entity

#### [MODIFY] [InputScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/input/InputScreen.kt)
- Tambahkan **Wallet Picker** di atas category picker
- Gunakan horizontal `LazyRow` dengan M3 `FilterChip` per wallet
- Jika hanya 1 wallet → auto-select, tidak perlu picker (KISS)
- Label chip: nama wallet (e.g. "BCA", "Cash")

### Testing

#### [UPDATE] `test/data/ExpenseDaoTest.kt`
- Tambah test: insert expense with `walletId`, query `getTransactionsByWallet` returns filtered results

#### [UPDATE] `test/ui/home/HomeViewModelTest.kt`
- Update `FakeExpenseRepository` untuk support method baru

#### [UPDATE] `test/ui/input/InputViewModelTest.kt`
- Update `FakeInputRepository` untuk support `walletId`
- Test: `onWalletSelected` sets correct wallet

### CI GitHub Actions
- `./gradlew assembleDebug` harus berhasil setelah migration
- `./gradlew testDebugUnitTest` harus lolos semua test yang diupdate
- Tidak ada perubahan pada `ci.yml`

## Acceptance Criteria

- [ ] Entity `Expense` memiliki field `walletId`
- [ ] Migration v4 → v5 berjalan tanpa crash
- [ ] Data lama otomatis mendapat `walletId = 1` (default wallet)
- [ ] InputScreen menampilkan wallet picker (jika > 1 wallet)
- [ ] Transaksi tersimpan dengan `walletId` yang dipilih
- [ ] DAO query per-wallet berfungsi
- [ ] Semua existing + new test lolos
- [ ] CI pipeline (build + test + lint) ✅ hijau

## Catatan Teknis

> **KISS:** Default `walletId = 1L` di entity menghindari null-safety complexity. Semua transaksi lama otomatis ke wallet default.

> **YAGNI:** Jangan tambahkan ForeignKey constraint. Cukup Long ID reference. ForeignKey menambah complexity migration tanpa benefit signifikan di tahap ini.

> **Clean Architecture:** Data layer migration dan DAO query selesai duluan, baru UI (InputScreen) dimodifikasi.
