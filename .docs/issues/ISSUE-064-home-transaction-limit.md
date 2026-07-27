# ISSUE-064: Home Screen — Limit Transaction List to Recent 5

## Deskripsi
Saat ini, halaman **Home** memuat dan menampilkan **seluruh transaksi** dalam periode yang dipilih (misalnya sebulan penuh). Jika user memiliki banyak transaksi, seluruh list di-load dari database dan di-render oleh `LazyColumn`, meskipun sebagian besar tidak terlihat di viewport awal. Ini memperlambat rendering Home dan bertentangan dengan best practice UI/UX untuk home/dashboard screen.

### Perspektif UI/UX
Berdasarkan prinsip **Miller's Law** (working memory manusia optimal pada 7±2 item) dan **Progressive Disclosure** (tampilkan yang esensial dulu, berikan akses ke detail lengkap via CTA), halaman Home idealnya hanya menampilkan **5 transaksi terakhir**:
- **GoPay, OVO, DANA**: Menampilkan 5 transaksi terakhir.
- **Mint, Wallet by BudgetBakers**: Menampilkan 5–7 transaksi terakhir.
- Home screen bukan tempat untuk browsing semua transaksi — itu tugas halaman **Ringkasan/Summary**.

### Perilaku Saat Ini
1. Query `getAllTransactionsBetween` / `getTransactionsByWallet` memuat **seluruh transaksi** dalam range periode (bisa ratusan item per bulan).
2. `HomeViewModel` memetakan semua transaksi ke `ExpenseWithCategory` dan meneruskan ke UI.
3. `LazyColumn` di `HomeScreen` me-render semua item — meskipun user hanya melihat 3–5 item pertama.
4. Tombol **"Lihat Semua"** sudah ada, tapi tidak ada limitasi di data layer.

### Perilaku yang Diharapkan
1. Home screen hanya menampilkan **maksimal 5 transaksi terakhir**.
2. Pembatasan dilakukan di **ViewModel layer** (bukan DAO) agar query tetap reusable.
3. Tombol **"Lihat Semua"** tetap ada dan mengarah ke halaman Ringkasan untuk melihat semua transaksi.
4. Performa Home screen meningkat karena UI hanya me-render 5 item.

## Acceptance Criteria
- [x] Halaman Home menampilkan **maksimal 5 transaksi terakhir** (sorted by timestamp DESC).
- [x] Limitasi dilakukan di `HomeViewModel` via `.take(5)` pada list transaksi sebelum mapping ke UI state.
- [x] Tombol **"Lihat Semua"** tetap berfungsi dan mengarahkan ke halaman Ringkasan.
- [x] Jika transaksi kurang dari 5, semua ditampilkan tanpa masalah.
- [x] Empty state tetap muncul jika tidak ada transaksi sama sekali.
- [x] Unit test untuk memverifikasi bahwa `transactions` di `HomeUiState` dibatasi maksimal 5 item.
- [x] Build & CI GitHub Actions pipeline tetap hijau.

## Technical Details

### ViewModel Layer (`HomeViewModel.kt`)
Perubahan minimal — tambahkan `.take(5)` setelah mapping transaksi:

```kotlin
// Sebelum:
transactions = withCategory,

// Sesudah:
transactions = withCategory.take(5),
```

Karena query DAO sudah `ORDER BY timestamp DESC`, `.take(5)` secara otomatis mengambil 5 transaksi terbaru.

### Tidak Ada Perubahan pada:
- **DAO / Repository** — Query tetap sama, tidak perlu `LIMIT` di SQL karena query ini juga digunakan oleh halaman lain (Summary).
- **HomeScreen.kt** — UI composable tidak berubah. `LazyColumn` tetap sama, hanya menerima list yang lebih pendek.
- **HomeUiState.kt** — Tidak ada field baru.

### Unit Test (`HomeViewModelTest.kt`)
- Tambahkan test case: Jika repository mengembalikan 10 transaksi, `HomeUiState.transactions` hanya berisi 5.
- Tambahkan test case: Jika repository mengembalikan 3 transaksi, `HomeUiState.transactions` berisi 3 (tidak error).

## Skills
- Clean Architecture: Pemisahan data/domain/ui layer
- Clean Code: Naming convention, single responsibility
- KISS: Perubahan minimal — 1 baris `.take(5)` di ViewModel
- YAGNI: Tidak menambah pagination atau DAO baru yang belum diperlukan
- CI GitHub Actions: Validasi build + test otomatis
