# ISSUE-008: Wallet List Screen & Navigation

**Priority:** 🔴 High  
**Type:** Feature (UI)  
**Sprint:** Sprint 2  
**Estimated Effort:** Medium  
**Depends On:** ISSUE-007

---

## Deskripsi

Membuat halaman **Wallet List** untuk menampilkan semua wallet milik user, serta menambahkan item "Wallet" pada NavigationBar agar user bisa mengakses halaman ini. User juga bisa menambah wallet baru dari halaman ini.

## Kondisi Saat Ini

- [BottomNavBar.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/navigation/BottomNavBar.kt) memiliki 4 item: Home, Summary, Add, Profile
- [NavRoutes.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/navigation/NavRoutes.kt) belum ada route untuk wallet
- [MainActivity.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/MainActivity.kt) belum ada composable destination untuk wallet
- Entity `Wallet` dan `WalletRepository` sudah tersedia dari ISSUE-007

## Perubahan yang Dibutuhkan

### Prinsip: Clean Architecture · Clean Code · KISS · YAGNI · CI GitHub Actions

### Navigation Layer

#### [MODIFY] [NavRoutes.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/navigation/NavRoutes.kt)
- Tambah `const val WALLET = "wallet"`

#### [MODIFY] [BottomNavBar.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/navigation/BottomNavBar.kt)
- Ganti item "Profile" (yang saat ini `TODO`) dengan item **Wallet**
- Icon: `Icons.Filled.AccountBalanceWallet` (dari `material-icons-extended`)
- Content description: "Wallet"
- Selected state berdasarkan `currentRoute == NavRoutes.WALLET`
- **KISS:** Hanya mengganti satu item, tidak mengubah struktur NavigationBar

#### [MODIFY] [MainActivity.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/MainActivity.kt)
- Tambah `composable(NavRoutes.WALLET)` destination
- Instantiate `WalletViewModel` via `WalletViewModelFactory`
- Render `WalletListScreen`

### UI Layer

#### [NEW] `ui/wallet/WalletUiState.kt`
```kotlin
data class WalletUiState(
    val wallets: List<Wallet> = emptyList(),
    val isLoading: Boolean = false
)
```
- **KISS:** Hanya state yang dibutuhkan untuk list. Tidak ada `selectedWallet`, `isEditing`, dll.

#### [NEW] `ui/wallet/WalletViewModel.kt`
```kotlin
class WalletViewModel(
    private val repository: WalletRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    // - Load all wallets on init
    // - fun addWallet(name: String)
    // - fun deleteWallet(wallet: Wallet)
    // - fun refresh()
}
```
- **Clean Architecture:** ViewModel hanya berkomunikasi via `WalletRepository` interface
- **YAGNI:** Tidak ada edit wallet. Tambah dan hapus saja dulu.

#### [NEW] `ui/wallet/WalletViewModelFactory.kt`
- Factory pattern konsisten dengan `HomeViewModelFactory`, `InputViewModelFactory`

#### [NEW] `ui/wallet/WalletListScreen.kt`
- **Header:** Judul "Wallet Saya" + tombol tambah wallet
- **List:** `LazyColumn` menampilkan setiap wallet sebagai M3 `Card`:
  - Nama wallet (bold)
  - Saldo wallet (formatted Rp)
  - Warna strip/accent sesuai `wallet.color` (atau default jika kosong)
- **Add Wallet Dialog:** `AlertDialog` sederhana dengan satu `OutlinedTextField` untuk nama wallet
  - **KISS:** Hanya input nama. Tidak perlu memilih icon/warna saat pertama kali.
- **Empty State:** Tampilkan pesan "Belum ada wallet" + tombol tambah
- **Swipe to Delete:** Konsisten dengan pattern `SwipeToDismissBox` yang sudah ada di `HomeScreen`

### Testing

#### [NEW] `test/ui/wallet/WalletViewModelTest.kt`
- Fake `WalletRepository`
- Test: initial state loads wallets
- Test: `addWallet` inserts dan refresh
- Test: `deleteWallet` removes dan refresh

### CI GitHub Actions
- `./gradlew assembleDebug` harus berhasil
- `./gradlew testDebugUnitTest` harus lolos termasuk `WalletViewModelTest`
- Tidak ada perubahan pada `ci.yml`

## Acceptance Criteria

- [ ] NavigationBar menampilkan icon Wallet menggantikan Profile
- [ ] Navigasi ke halaman Wallet List berfungsi
- [ ] Wallet List menampilkan semua wallet dari database
- [ ] User bisa menambah wallet baru via dialog
- [ ] User bisa menghapus wallet via swipe
- [ ] Empty state ditampilkan jika belum ada wallet
- [ ] Unit test `WalletViewModelTest` lolos
- [ ] CI pipeline (build + test + lint) ✅ hijau

## Catatan Teknis

> **KISS:** Tidak perlu bottom sheet atau full-screen form untuk menambah wallet. `AlertDialog` sederhana sudah cukup.

> **YAGNI:** Jangan tambahkan fitur edit wallet (rename, change icon/color) di issue ini. Cukup tambah dan hapus.

> **Clean Code:** Gunakan naming yang konsisten: `WalletListScreen`, `WalletViewModel`, `WalletUiState` — mengikuti pattern `HomeScreen`, `HomeViewModel`, `HomeUiState`.
