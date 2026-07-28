# ISSUE-087: Allow Deleting All Wallets & Show Message When No Wallet Exists

## Description
Saat ini, aplikasi melarang penghapusan wallet terakhir — jika hanya tersisa 1 wallet, tombol hapus tidak berfungsi atau tidak tersedia. Perilaku ini perlu diubah agar pengguna bisa menghapus **semua** wallet tanpa terkecuali.

Sebagai konsekuensinya, ketika pengguna mencoba menginput transaksi baru dan jumlah wallet adalah 0, aplikasi harus menampilkan pesan informatif yang meminta pengguna untuk menambahkan wallet terlebih dahulu sebelum bisa mencatat transaksi.

## Acceptance Criteria
- [x] Hapus batasan yang mencegah penghapusan wallet terakhir (sisa 1 wallet tetap bisa dihapus).
- [x] Saat pengguna membuka halaman input transaksi dan tidak ada wallet sama sekali (jumlah wallet = 0), tampilkan pesan/UI yang menginformasikan: "Tambahkan wallet terlebih dahulu untuk mencatat transaksi" (atau pesan serupa).
- [x] Sediakan tombol/aksi langsung dari pesan tersebut untuk navigasi ke halaman tambah wallet.
- [x] Pastikan tidak ada crash atau error saat aplikasi berjalan dengan 0 wallet.

## Technical Details
### Perubahan yang Diperlukan
1. **WalletViewModel / WalletScreen**: Cari logika yang mem-blokir penghapusan wallet terakhir dan hapus/modifikasi kondisi tersebut.
2. **InputScreen / InputViewModel**: Tambahkan pengecekan jumlah wallet. Jika 0, tampilkan state kosong (*empty state*) dengan pesan dan tombol navigasi ke halaman wallet.
3. **HomeScreen (opsional)**: Pertimbangkan apakah hero balance card dan daftar transaksi perlu menampilkan *empty state* khusus saat tidak ada wallet.

## Notes
- Pastikan `selectedWalletId` di DataStore di-handle dengan baik saat wallet terakhir dihapus (set ke `null` atau string kosong).
- Pertimbangkan juga *edge case* di Summary dan screen lain yang bergantung pada wallet.
