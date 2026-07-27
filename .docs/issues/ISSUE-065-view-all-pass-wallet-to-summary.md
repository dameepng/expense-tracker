# ISSUE-065: View All — Pass Active Wallet from Home to Summary

## Deskripsi
Saat ini, ketika user menekan tombol **"Lihat Semua"** pada halaman Home, navigasi ke halaman **Ringkasan (Summary)** selalu membuka dengan filter **"All Wallets"** — terlepas dari wallet mana yang sedang aktif di Home. Ini membuat pengalaman terasa tidak konsisten: user sedang melihat transaksi wallet BCA di Home, tapi ketika klik "Lihat Semua", Summary menampilkan transaksi dari semua wallet.

### Perilaku Saat Ini
1. User memilih wallet **"BCA"** di Home → transaksi yang tampil hanya dari BCA.
2. User klik **"Lihat Semua"** → navigasi ke Summary.
3. Summary terbuka dengan filter **"All Wallets"** (default) — bukan BCA.
4. User harus secara manual memilih wallet BCA lagi di halaman Summary.

### Perilaku yang Diharapkan
1. User memilih wallet **"BCA"** di Home → transaksi yang tampil hanya dari BCA.
2. User klik **"Lihat Semua"** → navigasi ke Summary.
3. Summary langsung terbuka dengan filter wallet **"BCA"** yang sudah terpilih.
4. User bisa mengubah filter wallet di Summary jika mau melihat wallet lain.

## Acceptance Criteria
- [x] Tombol "Lihat Semua" meneruskan `selectedWalletId` dari Home ke Summary.
- [x] Jika Home menampilkan "All Wallets" (`selectedWalletId = null`), Summary tetap terbuka dengan "All Wallets".
- [x] Jika Home menampilkan wallet spesifik (misal BCA), Summary langsung menampilkan data wallet tersebut.
- [x] Navigasi via bottom nav bar ke Summary **tidak terpengaruh** — tetap membuka dengan state terakhir Summary.
- [x] Build & CI GitHub Actions pipeline tetap hijau.

## Technical Details

### Pendekatan: Navigation Argument via `savedStateHandle`
Gunakan `savedStateHandle` untuk mengirim `walletId` dari Home ke Summary saat navigasi melalui tombol "Lihat Semua". Ini lebih bersih daripada mengubah route Summary menjadi parameterized route, karena navigasi via bottom nav bar tetap tanpa parameter.

### Navigation Layer (`NavRoutes.kt`)
Tidak perlu mengubah route. Gunakan `savedStateHandle` pada `navController`:

```kotlin
// Di MainActivity.kt - onNavigateToSummary callback
onNavigateToSummary = { walletId ->
    navController.navigate(NavRoutes.SUMMARY) {
        popUpTo(navController.graph.startDestinationId)
        launchSingleTop = true
    }
    // Pass walletId via savedStateHandle
    navController.currentBackStackEntry
        ?.savedStateHandle
        ?.set("summary_wallet_id", walletId)
}
```

### Home Layer
- **`HomeScreen.kt`**: Ubah `onNavigateToSummary` dari `() -> Unit` menjadi `(Long?) -> Unit` agar bisa mengirim `selectedWalletId`.
- **`HomeScreen.kt`** (Button click): Panggil `onNavigateToSummary(state.selectedWalletId)`.

### Summary Layer (`SummaryScreen.kt` / `SummaryViewModel.kt`)
- Di `composable(NavRoutes.SUMMARY)` pada `MainActivity.kt`, baca `savedStateHandle`:
  ```kotlin
  val walletId = it.savedStateHandle.get<Long?>("summary_wallet_id")
  if (walletId != null) {
      summaryViewModel.onWalletSelected(walletId)
  }
  ```
- `SummaryViewModel.onWalletSelected()` sudah ada dan berfungsi — tidak perlu diubah.

### File yang Terpengaruh
| File | Perubahan |
|---|---|
| `HomeScreen.kt` | Ubah signature callback `onNavigateToSummary: (Long?) -> Unit` |
| `MainActivity.kt` | Teruskan `walletId` dari callback, baca `savedStateHandle` di Summary composable |
| `NavRoutes.kt` | Tidak berubah |
| `SummaryViewModel.kt` | Tidak berubah |

### Unit Test
- Tidak ada unit test baru yang diperlukan karena ini murni perubahan wiring navigasi antar-screen.
- `SummaryViewModel.onWalletSelected()` sudah di-test sebelumnya.

## Skills
- Clean Architecture: Pemisahan data/domain/ui layer
- Clean Code: Naming convention, single responsibility
- KISS: Gunakan `savedStateHandle` yang sudah ada, tanpa ubah route
- YAGNI: Tidak menambah fitur baru yang belum diminta
- CI GitHub Actions: Validasi build + test otomatis
