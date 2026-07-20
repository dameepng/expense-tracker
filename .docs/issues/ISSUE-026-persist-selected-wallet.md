# ISSUE-026: Persist Selected Wallet Across App Restarts

## Deskripsi
Saat ini, ketika user memilih wallet tertentu (misal: BCA) di halaman Home dan kemudian menutup/membersihkan aplikasi, saat membuka kembali aplikasi selalu kembali ke "All Wallets". User harus memilih ulang wallet setiap kali membuka aplikasi.

Perbaikan ini akan menyimpan pilihan wallet terakhir menggunakan **Jetpack DataStore (Preferences)** sehingga pilihan user tetap tersimpan walaupun aplikasi ditutup atau dibersihkan dari recent apps.

## User Story
> Sebagai user, saya ingin aplikasi mengingat wallet terakhir yang saya pilih, sehingga saat membuka kembali aplikasi, saya langsung melihat data wallet tersebut tanpa harus memilih ulang.

## Acceptance Criteria
- [ ] Setelah user memilih wallet (misal: BCA), tutup app, buka lagi → tetap di wallet BCA
- [ ] Setelah user kembali ke "All Wallets", tutup app, buka lagi → tetap di "All Wallets"
- [ ] Jika wallet yang tersimpan sudah dihapus, fallback ke "All Wallets"
- [ ] Tidak ada lag/delay saat membaca preferensi di awal
- [ ] Data preferensi disimpan menggunakan Jetpack DataStore (bukan SharedPreferences deprecated)

## Technical Details

### Root Cause
Di [HomeViewModel.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeViewModel.kt#L23), state `selectedWalletId` hanya disimpan di `MutableStateFlow` yang bersifat in-memory. Saat `ViewModel` dihancurkan (app dibersihkan), data ini hilang.

App saat ini belum menggunakan DataStore maupun SharedPreferences untuk menyimpan preferensi apapun.

### Fix

#### 1. Tambahkan DataStore Preferences dependency
File: `build.gradle.kts` (app level)
```kotlin
implementation("androidx.datastore:datastore-preferences:1.1.7")
```

#### 2. Buat UserPreferencesRepository
**[NEW]** `data/UserPreferencesRepository.kt`
- Buat DataStore instance menggunakan `preferencesDataStore` delegate
- Key: `SELECTED_WALLET_ID` (Long, nullable → gunakan `-1L` sebagai sentinel untuk "All Wallets")
- Expose `selectedWalletIdFlow: Flow<Long?>` untuk dibaca secara reaktif
- Fungsi `saveSelectedWalletId(walletId: Long?)` untuk menyimpan

#### 3. Update HomeViewModel
**[MODIFY]** [HomeViewModel.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeViewModel.kt)
- Inject `UserPreferencesRepository` sebagai parameter constructor
- Di `init {}`, baca `selectedWalletIdFlow` dan set ke state sebelum `refresh()`
- Di `selectWallet()`, panggil `userPreferencesRepository.saveSelectedWalletId(walletId)` setelah update state
- Tambahkan validasi: jika wallet ID yang tersimpan tidak ditemukan di daftar wallet, fallback ke null ("All Wallets")

#### 4. Update Factory
**[MODIFY]** [HomeViewModelFactory.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeViewModelFactory.kt)
- Buat instance `UserPreferencesRepository` menggunakan `context.dataStore`
- Pass ke `HomeViewModel` constructor

### Dependencies
- `androidx.datastore:datastore-preferences:1.1.7` (tambah di `build.gradle.kts`)

## Skills
- Jetpack DataStore: Pengganti modern SharedPreferences
- Offline-First: Data preference dibaca lokal, zero-latency
- Clean Architecture: Repository pattern untuk akses preferences
- KISS: Hanya satu key yang disimpan, tidak over-engineer
