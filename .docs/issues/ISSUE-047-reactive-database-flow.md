# ISSUE-047: Optimasi Query Database - Gunakan Flow Reaktif

## Priority: 🟠 High
## Type: Performance / Architecture
## Status: Open

## Deskripsi
Saat ini, seluruh query di `ExpenseDao` dan `ExpenseRepository` mengembalikan `List<T>` secara sinkron (one-shot). Artinya:
1. Setiap kali data berubah (tambah/hapus transaksi), kita harus memanggil `refresh()` secara manual.
2. `refresh()` melakukan **5-6 query database terpisah secara sequential** (`getTotalExpense`, `getTotalIncome`, `getAllTransactionsBetween`, `getCategories`, `getAllWallets`, `getActiveReminders`).
3. Ini menyebabkan blocking I/O yang kumulatif.

## Solusi
### Fase 1: Konversi Query ke `Flow<T>`
Ubah DAO agar mengembalikan `Flow<List<T>>` untuk query yang sering berubah:

```kotlin
// Sebelum
@Query("SELECT * FROM expenses WHERE ...")
fun getAllTransactionsBetween(...): List<Expense>

// Sesudah
@Query("SELECT * FROM expenses WHERE ...")
fun getAllTransactionsBetween(...): Flow<List<Expense>>
```

### Fase 2: Combine Flow di ViewModel
Gunakan `combine()` untuk menggabungkan flow-flow tersebut sehingga UI secara otomatis terupdate tanpa perlu `refresh()` manual:

```kotlin
val uiState = combine(
    repository.transactionsFlow,
    repository.totalExpenseFlow,
    repository.totalIncomeFlow
) { transactions, expense, income -> ... }
```

### Fase 3: Hapus Manual Refresh
Setelah flow berjalan, hapus semua pemanggilan `refresh()` manual dari `HomeScreen`, `deleteExpense()`, dan `undoDeleteExpense()`.

## File yang Terpengaruh
- `ExpenseDao.kt`
- `ExpenseRepository.kt` (interface)
- `RoomExpenseRepository.kt` (implementasi)
- `HomeViewModel.kt`
- `SummaryViewModel.kt`

## Dampak
- Menghilangkan round-trip query berulang.
- Data di UI selalu sinkron secara real-time.
- Mengurangi beban I/O di main thread secara drastis.

## Catatan
Ini adalah refactoring besar yang sebaiknya dikerjakan bertahap (Fase 1 → 2 → 3).
