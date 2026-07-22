# ISSUE-052: HomeScreen Double Refresh & LaunchedEffect Overhead

## Priority: 🟠 High
## Type: Performance / Bug
## Status: Closed

## Deskripsi
`HomeScreen` saat ini memicu **dua kali refresh** setiap kali ditampilkan:

1. **`init {}` di HomeViewModel** (line 27-45): Saat ViewModel dibuat, `selectedWalletIdFlow.collect` langsung memanggil `refresh()`.
2. **`LaunchedEffect(Unit)` di HomeScreen** (line 628-630): Setiap kali HomeScreen masuk komposisi (termasuk kembali dari halaman lain), `viewModel.refresh()` dipanggil lagi.

```kotlin
// HomeScreen.kt
LaunchedEffect(Unit) {
    viewModel.refresh() // Refresh ke-2 (ke-1 sudah dari init{})
}
```

Ini menyebabkan:
- Double query ke database setiap navigasi ke Home
- Loading indicator muncul dua kali (flicker)
- Waktu tunggu x2 (600ms+ dengan artificial delay)

## Solusi
- **Hapus `LaunchedEffect(Unit) { viewModel.refresh() }`** dari HomeScreen. Flow dari `selectedWalletIdFlow` di `init {}` sudah cukup sebagai trigger refresh awal.
- Untuk refresh saat kembali dari InputScreen (setelah menambah transaksi), gunakan pendekatan yang lebih targeted:
  - Gunakan `SavedStateHandle` + `navController.previousBackStackEntry?.savedStateHandle` untuk mengirim signal "data changed" dari InputScreen.
  - Atau, setelah ISSUE-047 (Flow reaktif) diimplementasikan, refresh otomatis tidak diperlukan lagi.

## File yang Terpengaruh
- `HomeScreen.kt` (line 628-630): Hapus `LaunchedEffect(Unit)`
- `HomeViewModel.kt`: Pastikan `init {}` sudah cukup sebagai trigger pertama

## Dampak
- Menghilangkan 50% query yang redundan
- Home screen menampilkan data lebih cepat
- Menghilangkan flicker saat navigasi
