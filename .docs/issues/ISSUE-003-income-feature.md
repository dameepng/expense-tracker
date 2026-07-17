# ISSUE-003: Tambahkan Fitur Income (Pemasukan)

**Priority:** 🔴 High  
**Type:** Feature  
**Sprint:** Sprint 1  
**Estimated Effort:** Large  

---

## Deskripsi

Saat ini aplikasi hanya mencatat **expense (pengeluaran)**. Issue ini menambahkan kemampuan mencatat **income (pemasukan)** sehingga aplikasi menjadi tracker keuangan yang lebih lengkap. Perubahan mencakup:
- Menambahkan field `type` pada entity `Expense` → di-rename menjadi `Transaction`
- Menambahkan UI untuk input income
- Menampilkan summary income vs expense di HomeScreen
- Update DAO queries untuk mendukung income

## Kondisi Saat Ini

- Entity [Expense](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/data/Expense.kt) hanya menyimpan `amount`, `categoryId`, `description`, `timestamp`
- [ExpenseDao](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/data/ExpenseDao.kt) hanya punya query untuk expense
- [InputScreen](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/input/InputScreen.kt) hanya untuk input pengeluaran
- [HomeScreen](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeScreen.kt) hanya menampilkan expense list

## Perubahan yang Dibutuhkan

### Prinsip: Clean Architecture · Clean Code · KISS · YAGNI

### Data Layer

#### [NEW] `data/TransactionType.kt`
```kotlin
enum class TransactionType {
    INCOME,
    EXPENSE
}
```
Enum sederhana — KISS, hanya 2 value yang dibutuhkan.

#### [MODIFY] [Expense.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/data/Expense.kt)
- Tambahkan field `type: String = "EXPENSE"` pada entity
- Gunakan `String` untuk Room compatibility (bukan enum langsung) — KISS
- **Tidak perlu rename tabel** untuk menghindari migration complexity — YAGNI
- Atau bisa ditambahkan via Room migration

#### [MODIFY] [ExpenseDao.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/data/ExpenseDao.kt)
- Tambah query `getTotalIncome(startTime, endTime)` — SUM amount WHERE type = 'INCOME'
- Tambah query `getIncomesBetween(startTime, endTime)` — SELECT WHERE type = 'INCOME'
- Modify existing queries: `getTotalExpense` filter WHERE type = 'EXPENSE'
- Modify `getExpensesBetween` filter WHERE type = 'EXPENSE'
- Tambah query `getAllTransactionsBetween(startTime, endTime)` — SELECT semua tanpa filter type

#### [MODIFY] [AppDatabase.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/data/AppDatabase.kt)
- Bump version ke 3
- Tambahkan `MIGRATION_2_3`: `ALTER TABLE expenses ADD COLUMN type TEXT NOT NULL DEFAULT 'EXPENSE'`

#### [MODIFY] [ExpenseRepository.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/data/ExpenseRepository.kt)
- Tambah method: `getTotalIncome(startTime, endTime): Long`
- Tambah method: `getAllTransactionsBetween(startTime, endTime): List<Expense>`

#### [MODIFY] [RoomExpenseRepository.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/data/RoomExpenseRepository.kt)
- Implementasi method baru yang delegate ke DAO

#### [MODIFY] [ExpenseWithCategory.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/data/ExpenseWithCategory.kt)
- Tambah field `type: String = "EXPENSE"`

### UI Layer

#### [MODIFY] [InputScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/input/InputScreen.kt)
- Tambahkan toggle Income/Expense di atas amount input
- Gunakan M3 `SegmentedButton` (2 pilihan: Income / Expense) — cocok untuk binary choice
- Ubah header text dinamis: "Tambah Pemasukan" / "Tambah Pengeluaran"
- Warna amount berubah: hijau untuk income, merah/default untuk expense

#### [MODIFY] [InputUiState.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/input/InputUiState.kt)
- Tambah field `transactionType: TransactionType = TransactionType.EXPENSE`

#### [MODIFY] [InputViewModel.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/input/InputViewModel.kt)
- Tambah fungsi `onTransactionTypeChange(type: TransactionType)`
- Saat save, kirim `type` ke repository

#### [MODIFY] [InputRepository.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/input/InputRepository.kt)
- Update `insertExpense()` signature untuk terima parameter `type`

#### [MODIFY] [RoomInputRepository.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/data/RoomInputRepository.kt)
- Pass `type` saat membuat `Expense` entity

#### [MODIFY] [HomeUiState.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeUiState.kt)
- Tambah field: `totalIncome: Long = 0L`
- Rename `expenses` → `transactions` (atau tambah `transactions` list baru)

#### [MODIFY] [HomeViewModel.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeViewModel.kt)
- Fetch `totalIncome` bersamaan dengan `totalExpense` di `refresh()`
- Gabungkan income + expense ke satu list transactions, sorted by timestamp DESC

#### [MODIFY] [HomeScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeScreen.kt)
- Tambahkan **Income/Expense summary chips** di bawah BalanceCard (seperti gambar referensi)
  - Chip Income: icon hijau ↑, "+Rp X.XXX" atau persentase
  - Chip Expense: icon kuning/oranye ↓, "-Rp X.XXX" atau persentase
  - Gunakan M3 `Surface` + `Row` atau `AssistChip`
- Update `ExpenseListItem` untuk menampilkan warna berbeda:
  - Income → hijau (`+Rp`)
  - Expense → merah (`-Rp`)

## Acceptance Criteria

- [ ] User bisa memilih Income atau Expense saat input transaksi baru
- [ ] Data income tersimpan dengan benar di database (type = 'INCOME')
- [ ] HomeScreen menampilkan summary income & expense (chips/cards)
- [ ] BalanceCard menampilkan **balance** (income - expense), bukan hanya total expense
- [ ] Transaction list menampilkan income (hijau, +) dan expense (merah, -) dengan visual berbeda
- [ ] Database migration berjalan lancar (v2 → v3)
- [ ] Existing expense data tidak rusak setelah migration

## Catatan Teknis

> **KISS:** Gunakan `String` untuk field `type` di Room entity agar tidak perlu TypeConverter. Cukup "INCOME" dan "EXPENSE".

> **YAGNI:** Jangan tambahkan kategori khusus income dulu. Gunakan kategori yang sudah ada. Jika nanti dibutuhkan, bisa ditambah di sprint berikutnya.

> **Clean Architecture:** Perubahan data layer (Entity, DAO, Repository) harus selesai dan teruji sebelum menyentuh UI layer.
