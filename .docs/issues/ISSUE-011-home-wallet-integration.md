# ISSUE-011: Home Screen Wallet Integration

**Priority:** 🟡 Medium  
**Type:** Integration  
**Sprint:** Sprint 2  
**Estimated Effort:** Medium  
**Depends On:** ISSUE-009, ISSUE-010

---

## Deskripsi

Mengintegrasikan fitur wallet ke HomeScreen. BalanceCard di Home menampilkan **total dari semua wallet** (gabungan), dan user bisa melihat daftar wallet mereka secara ringkas. Ini memberikan gambaran umum keuangan user di satu tempat.

## Kondisi Saat Ini

- [HomeScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeScreen.kt) menampilkan saldo global (tanpa konteks wallet)
- [HomeViewModel.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeViewModel.kt) hanya query dari `ExpenseRepository` tanpa wallet context
- Transaksi sudah memiliki `walletId` dari ISSUE-009
- Wallet list dan detail sudah tersedia dari ISSUE-008, ISSUE-010

## Perubahan yang Dibutuhkan

### Prinsip: Clean Architecture · Clean Code · KISS · YAGNI · CI GitHub Actions

### UI Layer

#### [MODIFY] [HomeUiState.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeUiState.kt)
- Tambah field `wallets: List<Wallet> = emptyList()`
- **KISS:** Cukup tambah list wallet saja, tidak perlu `selectedWallet` atau `walletBalances` map

#### [MODIFY] [HomeViewModel.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeViewModel.kt)
- Inject `WalletRepository` sebagai dependency tambahan
- Di `refresh()`: load wallets dan sertakan di state
- **Logika saldo tetap global** (total semua wallet) — KISS
- Update factory untuk inject `WalletRepository`

#### [MODIFY] [HomeViewModelFactory.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeViewModelFactory.kt)
- Tambah `WalletRepository` parameter

#### [MODIFY] [HomeScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeScreen.kt)
Tambahkan **Wallet Overview Section** di bawah IncomeExpenseSummary:
- Horizontal scrollable row (`LazyRow`) menampilkan wallet mini-cards
- Setiap mini-card menampilkan:
  - Nama wallet (e.g. "BCA")
  - Mini balance (e.g. "Rp 500.000")
- Tap mini-card → navigasi ke Wallet Detail
- **KISS:** Tidak ada interaksi kompleks (drag, reorder, etc.)
- **YAGNI:** Jangan tampilkan chart atau grafik per wallet di home

#### [MODIFY] [MainActivity.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/MainActivity.kt)
- Pass `onNavigateToWalletDetail` callback ke HomeScreen (jika diperlukan)

### Testing

#### [UPDATE] `test/ui/home/HomeViewModelTest.kt`
- Update `FakeExpenseRepository` dan tambah `FakeWalletRepository`
- Test: wallets loaded di state
- Test: saldo tetap global (bukan per-wallet)

### CI GitHub Actions
- `./gradlew assembleDebug` harus berhasil
- `./gradlew testDebugUnitTest` harus lolos
- Tidak ada perubahan pada `ci.yml`

## Acceptance Criteria

- [ ] HomeScreen menampilkan wallet mini-cards di bawah summary chips
- [ ] Mini-cards scrollable horizontal
- [ ] Tap mini-card navigasi ke Wallet Detail
- [ ] BalanceCard tetap menampilkan total gabungan semua wallet
- [ ] HomeViewModel berhasil load wallets
- [ ] Unit test updated dan lolos
- [ ] CI pipeline (build + test + lint) ✅ hijau

## Catatan Teknis

> **KISS:** Home tetap menampilkan data global. Wallet mini-cards hanya shortcut navigasi, bukan dashboard lengkap.

> **YAGNI:** Jangan tambahkan filter "per wallet" di home. User bisa tap ke Wallet Detail untuk melihat data per wallet.

> **Clean Architecture:** HomeViewModel inject WalletRepository via constructor. Tidak boleh akses DAO langsung.
