# ISSUE-025: Swipe-to-Delete Confirmation via ModalBottomSheet

## Deskripsi
Menambahkan konfirmasi penghapusan transaksi menggunakan `ModalBottomSheet` sebagai pengganti langsung menghapus saat swipe. Saat ini di `WalletDetailScreen`, swipe-to-delete langsung menghapus transaksi tanpa konfirmasi (hanya mengandalkan undo via Snackbar). Menambahkan konfirmasi bottom sheet akan mencegah penghapusan yang tidak disengaja.

## User Story
> Sebagai user, saya ingin mendapat konfirmasi sebelum transaksi benar-benar dihapus, agar saya tidak kehilangan data secara tidak sengaja.

## Acceptance Criteria
- [ ] Swipe transaksi ke kiri → muncul `ModalBottomSheet` konfirmasi (bukan langsung hapus)
- [ ] Bottom sheet menampilkan: judul "Hapus Transaksi?", detail transaksi, tombol "Hapus" (warna error) dan "Batal"
- [ ] Klik "Hapus" → transaksi dihapus + snackbar undo tetap muncul
- [ ] Klik "Batal" atau drag-down → bottom sheet tertutup, transaksi tidak terhapus
- [ ] Swipe-to-edit (ke kanan) tetap berfungsi normal tanpa konfirmasi

## Technical Details

### Kondisi Saat Ini
Di [WalletDetailScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/wallet/WalletDetailScreen.kt#L153-L177), `SwipeToDismissBox` dengan `confirmValueChange` langsung menjalankan `viewModel.deleteTransaction()` tanpa konfirmasi.

### Fix
1. Saat swipe end-to-start terdeteksi, **jangan langsung hapus** → simpan transaksi ke state `transactionToDelete`
2. Tampilkan `ModalBottomSheet` konfirmasi
3. Di bottom sheet, tampilkan detail transaksi + dua tombol aksi
4. Jika user konfirmasi, jalankan delete + snackbar undo

#### [MODIFY] [WalletDetailScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/wallet/WalletDetailScreen.kt)
- Tambahkan state `var transactionToDelete` dan `showDeleteSheet`
- Ubah `confirmValueChange` untuk menyimpan ke state, bukan langsung delete
- Tambahkan `ModalBottomSheet` dengan layout konfirmasi

### Dependencies
- Tidak ada dependency tambahan

## Skills
- M3 Components: ModalBottomSheet untuk konfirmasi
- UX Best Practice: Konfirmasi destructive action
- Clean Code: State-driven UI pattern
