# ISSUE-048: Optimasi Compose Recomposition - Stabilitas & Key

## Priority: 🟠 High
## Type: Performance
## Status: Closed

## Deskripsi
Beberapa composable di aplikasi ini berpotensi menyebabkan **unnecessary recomposition**, yaitu Compose menggambar ulang komponen yang sebenarnya tidak berubah. Ini membuat perpindahan dan scroll terasa janky.

## Masalah yang Teridentifikasi

### 1. `ExpenseWithCategory` Bukan Data Class Stabil
`ExpenseWithCategory` digunakan di seluruh list transaksi. Jika Compose tidak bisa membuktikan bahwa instance-nya stabil (immutable), maka **setiap item di LazyColumn akan di-recompose** setiap kali parent-nya berubah, meskipun datanya sama.

**Solusi**: Pastikan `ExpenseWithCategory` adalah `data class` murni dengan properti primitif/immutable. Tambahkan anotasi `@Immutable` atau `@Stable` jika perlu.

### 2. Lambda Allocation di `items { }` Block
Di `HomeScreen.kt`, lambda `onDelete`, `onEdit`, dan `confirmValueChange` dibuat baru setiap recomposition karena mengacu pada `viewModel` dan `coroutineScope` secara langsung.

**Solusi**: Gunakan `remember` untuk lambda yang berat, atau pindahkan callback ke level yang lebih tinggi.

### 3. Nested `collectAsState()` yang Berlebihan
`HomeScreen` menggunakan `collectAsState()` yang meng-observe seluruh `HomeUiState`. Jika satu properti berubah (misal `userName`), seluruh screen di-recompose.

**Solusi**: Pecah state menjadi beberapa `StateFlow` kecil, atau gunakan `derivedStateOf` / `snapshotFlow` untuk properti yang jarang berubah.

## File yang Terpengaruh
- `HomeScreen.kt`: LazyColumn items, SwipeToDismiss callbacks
- `ExpenseWithCategory.kt`: Stabilitas data class
- `SummaryScreen.kt`: List recomposition

## Verifikasi
- Aktifkan "Show Recomposition Counts" di Layout Inspector Android Studio.
- Scroll list transaksi dan pastikan hanya item yang terlihat yang di-recompose.
