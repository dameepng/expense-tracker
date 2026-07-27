# ISSUE-070: Delete Wallet dengan Dialog Konfirmasi Ketik "hapus dompet"

## Deskripsi
Saat ini, tidak ada cara bagi pengguna untuk menghapus dompet (wallet) yang sudah dibuat. Fungsi `deleteWallet()` sudah tersedia di `WalletViewModel` dan `WalletDao`, namun belum dihubungkan ke UI mana pun.

Fitur ini akan menambahkan kemampuan menghapus wallet dari halaman **Daftar Dompet (WalletListScreen)** dengan mekanisme keamanan berlapis berupa **dialog konfirmasi yang mengharuskan pengguna mengetik teks tertentu** sebelum tombol hapus diaktifkan — mirip dengan cara GitHub meminta konfirmasi saat menghapus repository.

### Perilaku Saat Ini
1. Fungsi `deleteWallet(wallet)` sudah tersedia di `WalletViewModel`, `WalletRepository`, dan `WalletDao`.
2. Tidak ada elemen UI di `WalletListScreen` yang memicu penghapusan wallet.
3. Tidak ada dialog konfirmasi penghapusan.

### Perilaku yang Diharapkan
1. Setiap card wallet di `WalletListScreen` memiliki aksi hapus (contoh: ikon delete di pojok kanan atas card, atau opsi muncul via long-press/menu).
2. Saat aksi hapus ditekan, muncul **AlertDialog / ModalBottomSheet** konfirmasi dengan:
   - Judul: "Hapus Dompet?"
   - Pesan peringatan yang menjelaskan bahwa **semua transaksi di dompet ini akan ikut terhapus** dan aksi ini tidak bisa dibatalkan.
   - **TextField** yang harus diisi persis dengan teks `hapus dompet` (case-insensitive) sebelum tombol hapus menjadi aktif.
   - Tombol "Hapus" yang **disabled** secara default dan hanya menjadi **enabled** setelah teks yang benar diketik.
   - Tombol "Batal" untuk menutup dialog tanpa melakukan apa-apa.
3. Saat tombol "Hapus" ditekan (setelah konfirmasi valid), wallet beserta semua transaksi terkait dihapus secara permanen dari database.
4. Jika wallet yang dihapus adalah wallet yang sedang aktif (selected), aplikasi harus otomatis memilih wallet lain atau kembali ke state "All Wallets".
5. **Pencegahan**: Jika hanya tersisa 1 wallet, tampilkan pesan (Toast/Snackbar) bahwa wallet terakhir tidak bisa dihapus.

## Acceptance Criteria
- [ ] Setiap card wallet di `WalletListScreen` memiliki ikon/tombol hapus (delete).
- [ ] Dialog konfirmasi muncul saat tombol hapus ditekan.
- [ ] Dialog memiliki `OutlinedTextField` yang harus diisi dengan teks "hapus dompet".
- [ ] Tombol "Hapus" di dialog hanya aktif (enabled) jika teks yang diketik cocok (case-insensitive).
- [ ] Wallet beserta transaksi terkait berhasil dihapus dari database saat konfirmasi valid.
- [ ] Jika wallet yang dihapus sedang aktif, aplikasi fallback ke wallet lain atau "All Wallets".
- [ ] Wallet terakhir (satu-satunya) tidak bisa dihapus, ditampilkan pesan peringatan.
- [ ] Build & CI GitHub Actions pipeline tetap hijau.

## Technical Details

### Data Layer
- Pastikan `WalletDao.deleteWallet(wallet)` sudah memiliki `CASCADE` delete untuk transaksi terkait, atau tambahkan query manual `deleteTransactionsByWalletId(walletId)` sebelum menghapus wallet.
- Periksa `ForeignKey` constraint di entity `Expense` terhadap `Wallet`.

### UI Layer
- Tambahkan ikon `Icons.Default.Delete` atau `Icons.Default.DeleteForever` pada card wallet.
- Buat composable `DeleteWalletConfirmDialog` (atau bisa inline di `WalletListScreen`) yang berisi:
  - `AlertDialog` dengan `OutlinedTextField`.
  - State `confirmText` yang di-compare dengan `"hapus dompet"` (case-insensitive via `.equals(ignoreCase = true)`).
  - Tombol "Hapus" menggunakan warna `MaterialTheme.colorScheme.error`.
- Gunakan `AnimatedVisibility` atau efek visual pada tombol hapus saat teks konfirmasi sudah benar (misalnya transisi warna dari disabled ke merah).

### ViewModel
- Fungsi `deleteWallet(wallet)` sudah ada di `WalletViewModel`. Mungkin perlu ditambahkan logika:
  - Cek apakah wallet yang dihapus adalah wallet yang sedang aktif.
  - Cek apakah ini wallet terakhir (jangan izinkan hapus).

### Strings
- Tambahkan string resource baru untuk dialog konfirmasi (Bahasa Indonesia & English).
  - `delete_wallet_title`: "Hapus Dompet?" / "Delete Wallet?"
  - `delete_wallet_warning`: Pesan peringatan tentang konsekuensi penghapusan.
  - `delete_wallet_confirm_hint`: "Ketik \"hapus dompet\" untuk mengonfirmasi" / "Type \"hapus dompet\" to confirm"
  - `delete_wallet_button`: "Hapus" / "Delete"
  - `delete_wallet_last_warning`: "Tidak bisa menghapus dompet terakhir" / "Cannot delete the last wallet"

## Skills
- Clean Architecture: Pemisahan data/domain/ui layer.
- Clean Code: Single Responsibility — dialog konfirmasi sebagai composable terpisah.
- KISS: Gunakan `AlertDialog` M3 standard, tidak perlu buat custom modal yang rumit.
- YAGNI: Hanya implementasikan fitur hapus wallet, tidak perlu fitur arsip/soft-delete.
- DRY: Reuse fungsi `deleteWallet()` yang sudah ada di ViewModel.
- CI GitHub Actions: Validasi build + lint otomatis.
