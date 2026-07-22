# ISSUE-051: Redundant DataStore Read di WalletListScreen

## Priority: 🟡 Medium
## Type: Performance / Code Quality
## Status: Open

## Deskripsi
Di `MainActivity.kt` (line 318-320), halaman Wallet membuat instance `UserPreferencesRepositoryImpl` baru di dalam `composable { }` block menggunakan `remember`:

```kotlin
val userPrefsRepo = remember(context) {
    com.example.expense_tracker.data.UserPreferencesRepositoryImpl(context.dataStore)
}
```

Masalahnya:
1. Ini membuat **instance repository duplikat** — satu sudah ada di level `MainActivity` untuk membaca theme & biometrics, dan satu lagi dibuat di sini.
2. `DataStore` secara internal menggunakan file tunggal, jadi dua instance tidak menyebabkan data corruption, tapi tetap menambah overhead (dua coroutine scope, dua file read).
3. Pola ini melanggar *Single Source of Truth* — seharusnya ada satu repository yang di-*share* ke seluruh ViewModel.

## Solusi
Pindahkan penyimpanan `selectedWalletId` ke `WalletViewModel`, yang sudah memiliki akses ke repository melalui dependency injection:

```kotlin
class WalletViewModel(
    private val repository: WalletRepository,
    private val userPreferencesRepository: UserPreferencesRepository, // Tambahkan ini
    ...
) {
    fun selectWallet(walletId: Long) {
        viewModelScope.launch {
            userPreferencesRepository.saveSelectedWalletId(walletId)
        }
    }
}
```

## File yang Terpengaruh
- `MainActivity.kt`: Hapus inline repository creation di Wallet composable
- `WalletViewModel.kt`: Tambahkan `UserPreferencesRepository` dependency
- `WalletViewModelFactory.kt`: Update factory untuk menyediakan repository

## Dampak
- Menghilangkan duplikasi repository instance
- Kode lebih bersih dan maintainable
