# ISSUE-010: Wallet Detail & Filtered Transactions

**Priority:** 🟡 Medium  
**Type:** Feature (UI)  
**Sprint:** Sprint 2  
**Estimated Effort:** Medium  
**Depends On:** ISSUE-008, ISSUE-009

---

## Deskripsi

Membuat halaman **Wallet Detail** yang menampilkan saldo dan riwayat transaksi untuk satu wallet tertentu. User bisa masuk ke halaman ini dari Wallet List (ISSUE-008) dengan tap pada wallet card.

## Kondisi Saat Ini

- Wallet List Screen sudah ada (ISSUE-008) tetapi belum bisa navigasi ke detail
- DAO query per-wallet sudah tersedia (ISSUE-009)
- `ExpenseRepository` sudah punya method filter per `walletId`

## Perubahan yang Dibutuhkan

### Prinsip: Clean Architecture · Clean Code · KISS · YAGNI · CI GitHub Actions

### Navigation Layer

#### [MODIFY] [NavRoutes.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/navigation/NavRoutes.kt)
- Tambah route:
  ```kotlin
  const val WALLET_DETAIL = "wallet/{walletId}"
  fun walletDetailRoute(walletId: Long): String = "wallet/$walletId"
  ```

#### [MODIFY] [MainActivity.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/MainActivity.kt)
- Tambah `composable(NavRoutes.WALLET_DETAIL)` destination dengan argument `walletId`
- Instantiate `WalletDetailViewModel` dan render `WalletDetailScreen`

### UI Layer

#### [NEW] `ui/wallet/WalletDetailUiState.kt`
```kotlin
data class WalletDetailUiState(
    val wallet: Wallet? = null,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val balance: Long = 0L,
    val transactions: List<ExpenseWithCategory> = emptyList(),
    val isLoading: Boolean = false
)
```
- **KISS:** Re-use `ExpenseWithCategory` yang sudah ada, tidak perlu model baru

#### [NEW] `ui/wallet/WalletDetailViewModel.kt`
```kotlin
class WalletDetailViewModel(
    private val walletId: Long,
    private val walletRepository: WalletRepository,
    private val expenseRepository: ExpenseRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    // - Load wallet info by ID
    // - Load transactions filtered by walletId
    // - Calculate totalIncome, totalExpense, balance for this wallet
    // - fun deleteTransaction(transaction: ExpenseWithCategory)
}
```
- **Clean Architecture:** Menggunakan kedua repository (wallet + expense) via interface

#### [NEW] `ui/wallet/WalletDetailViewModelFactory.kt`
- Menerima `walletId` sebagai parameter
- Factory pattern konsisten

#### [NEW] `ui/wallet/WalletDetailScreen.kt`
Layout:
1. **Top Bar:** Nama wallet + tombol back
2. **Balance Card:** Re-use `BalanceCard` composable dari HomeScreen (atau extract sebagai shared composable)
   - Tampilkan saldo wallet ini (bukan global)
3. **Income/Expense Summary:** Re-use `IncomeExpenseSummary` composable
4. **Transaction List:** Re-use `TransactionListItem` composable
   - Hanya menampilkan transaksi milik wallet ini
   - Swipe to delete tetap berfungsi

- **KISS:** Re-use composable yang sudah ada dari HomeScreen. Tidak membuat ulang.
- **Clean Code:** Jika composable perlu di-share, extract ke package `ui/components/`

#### [MODIFY] `ui/wallet/WalletListScreen.kt` (dari ISSUE-008)
- Tambah `onClick` handler pada wallet card → navigasi ke `WalletDetailScreen`
- Callback: `onNavigateToWalletDetail: (Long) -> Unit`

### Testing

#### [NEW] `test/ui/wallet/WalletDetailViewModelTest.kt`
- Fake repositories
- Test: loads wallet info correctly
- Test: transactions filtered by walletId
- Test: balance = income - expense for this wallet only

### CI GitHub Actions
- `./gradlew assembleDebug` harus berhasil
- `./gradlew testDebugUnitTest` harus lolos termasuk `WalletDetailViewModelTest`
- Tidak ada perubahan pada `ci.yml`

## Acceptance Criteria

- [ ] Tap wallet card di Wallet List navigasi ke Wallet Detail
- [ ] Wallet Detail menampilkan nama wallet di top bar
- [ ] Balance card menampilkan saldo khusus wallet ini (income - expense)
- [ ] Summary chips menampilkan income/expense khusus wallet ini
- [ ] Transaction list hanya menampilkan transaksi milik wallet ini
- [ ] Swipe to delete berfungsi pada transaksi di wallet detail
- [ ] Back button kembali ke Wallet List
- [ ] Unit test `WalletDetailViewModelTest` lolos
- [ ] CI pipeline (build + test + lint) ✅ hijau

## Catatan Teknis

> **KISS:** Re-use composable dari HomeScreen (`BalanceCard`, `IncomeExpenseSummary`, `TransactionListItem`). Jangan duplikasi kode.

> **YAGNI:** Jangan tambahkan fitur edit wallet (rename, change color) di halaman detail. Cukup tampilkan info + transaksi.

> **Clean Architecture:** ViewModel berkomunikasi via repository interface. Screen hanya terima state dan event callbacks.
