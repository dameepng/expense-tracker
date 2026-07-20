# ISSUE-021: Fix App Bar Position & Wallet Balance Bug

## Deskripsi
Tiga perbaikan pada halaman Bill Reminders dan Wallet:
1. **App Bar terlalu atas (tidak sesuai M3 guide)** — `TopAppBar` pada halaman Bill Reminders dan Wallet tidak menghormati system insets (status bar), sehingga konten title terlihat terlalu menempel ke atas dan tidak sesuai dengan panduan Material 3 Jetpack Compose.
2. **Wallet balance menampilkan Rp0** — Card wallet pada `WalletListScreen` menampilkan `Rp0` untuk semua wallet meskipun sudah ada transaksi (income/expense) yang tercatat. Ini karena kolom `balance` pada entity `Wallet` tidak pernah di-update saat transaksi dibuat; balance seharusnya dihitung secara dinamis dari tabel `expenses`.

## User Story
> Sebagai user, saya ingin app bar pada halaman Bill Reminders dan Wallet tampil dengan posisi yang benar sesuai panduan M3, dan saya ingin melihat saldo wallet yang akurat berdasarkan transaksi yang sudah saya catat.

## Acceptance Criteria

### Bug 1: App Bar Position (Bill Reminders & Wallet)
- [ ] `TopAppBar` pada `ReminderListScreen` menghormati status bar insets (tidak overlap dengan system bar)
- [ ] `TopAppBar` pada `WalletListScreen` menghormati status bar insets (tidak overlap dengan system bar)
- [ ] Posisi app bar sesuai dengan panduan Material 3 — title dan navigation icon berada di bawah status bar
- [ ] Tidak ada perubahan visual lain pada konten app bar (title, icon tetap sama)

### Bug 2: Wallet Balance Menampilkan Rp0
- [ ] Card wallet pada `WalletListScreen` menampilkan saldo yang dihitung dari transaksi (income - expense)
- [ ] Saldo dihitung secara dinamis: `SUM(income) - SUM(expense)` dari tabel `expenses` berdasarkan `walletId`
- [ ] Wallet tanpa transaksi menampilkan Rp0 (benar)
- [ ] Wallet dengan transaksi menampilkan saldo yang akurat
- [ ] Saldo di-refresh otomatis saat kembali dari halaman lain

### Testing & CI
- [ ] Unit test: verifikasi balance dihitung dari transaksi
- [ ] UI manual: verifikasi app bar position dan wallet balance
- [ ] CI GitHub Actions pipeline hijau

## Technical Details

### Bug 1: Root Cause & Fix

**Root Cause**: Kedua screen menggunakan `Scaffold` + `TopAppBar` tetapi `Scaffold` dari parent `MainActivity` sudah mengonsumsi `innerPadding` yang hanya mencakup bottom bar. Halaman-halaman child ini memiliki `Scaffold` sendiri dengan `TopAppBar`, namun `TopAppBar` default colors tidak otomatis menangani status bar insets ketika parent sudah meng-consume padding.

**Fix**: Gunakan `TopAppBarDefaults` dengan `windowInsets` yang benar, atau pastikan `Scaffold` pada masing-masing screen menggunakan `contentWindowInsets` yang tepat sehingga `TopAppBar` memiliki padding status bar yang sesuai.

#### [MODIFY] [ReminderListScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/reminder/ReminderListScreen.kt)
- Tambahkan `windowInsets` pada `TopAppBar` agar menghormati status bar
- Atau gunakan `TopAppBarDefaults.topAppBarColors()` dengan `windowInsets = TopAppBarDefaults.windowInsets`

#### [MODIFY] [WalletListScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/wallet/WalletListScreen.kt)
- Sama seperti di atas — pastikan `TopAppBar` menghormati status bar insets

---

### Bug 2: Root Cause & Fix

**Root Cause**: Entity `Wallet` memiliki kolom `balance` dengan default `0L`:
```kotlin
data class Wallet(
    val id: Long = 0,
    val name: String,
    val balance: Long = 0L, // ← tidak pernah di-update
)
```

Saat transaksi dibuat (`RoomInputRepository.insertExpense()`), hanya menyimpan ke tabel `expenses` tanpa meng-update `wallet.balance`. Di `HomeViewModel`, total balance dihitung dinamis dari query (`getTotalIncome - getTotalExpense`), tapi `WalletListScreen` langsung membaca `wallet.balance` dari entity → selalu `Rp0`.

**Fix — Pendekatan Computed Balance**: Alih-alih meng-update kolom `balance` setiap kali transaksi dibuat/dihapus (rawan inkonsistensi), hitung balance secara dinamis di DAO layer:

```kotlin
// WalletDao.kt — query baru
@Query("""
    SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) - 
           COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) 
    FROM expenses WHERE walletId = :walletId
""")
fun getComputedBalance(walletId: Long): Long
```

Kemudian `WalletViewModel` menghitung balance untuk setiap wallet setelah mengambil daftar wallet.

Alternatif data class untuk menampung wallet + computed balance:
```kotlin
data class WalletWithBalance(
    val wallet: Wallet,
    val computedBalance: Long
)
```

#### [MODIFY] [WalletDao.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/data/WalletDao.kt)
- Tambahkan query `getComputedBalance(walletId: Long): Long` untuk menghitung saldo dari transaksi

#### [MODIFY] [WalletRepository.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/data/WalletRepository.kt)
- Tambahkan method `getComputedBalance(walletId: Long): Long`

#### [MODIFY] [RoomWalletRepository.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/data/RoomWalletRepository.kt)
- Implementasi `getComputedBalance()` yang memanggil `dao.getComputedBalance()`

#### [MODIFY] [WalletUiState.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/wallet/WalletUiState.kt)
- Tambahkan `balanceMap: Map<Long, Long>` untuk menyimpan computed balance per wallet

#### [MODIFY] [WalletViewModel.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/wallet/WalletViewModel.kt)
- Setelah fetch wallets, hitung `computedBalance` untuk setiap wallet via `repository.getComputedBalance(wallet.id)`
- Simpan hasilnya di `balanceMap` pada `WalletUiState`

#### [MODIFY] [WalletListScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/wallet/WalletListScreen.kt)
- Ganti `wallet.balance` dengan `uiState.balanceMap[wallet.id] ?: 0L` saat menampilkan saldo

### Dependency
- Tidak bergantung pada issue lain

## Skills
- Clean Architecture: Balance dihitung di Data layer (DAO query), ViewModel sebagai orchestrator, UI hanya menampilkan
- Clean Code: Query SQL yang ekspresif untuk computed balance, menghindari duplikasi logika
- KISS: Satu query baru untuk menghitung balance, tidak perlu migration schema atau kolom baru
- YAGNI: Tidak menambahkan trigger atau observer yang kompleks — cukup computed query saat refresh
- CI GitHub Actions: Validasi build + unit test otomatis
