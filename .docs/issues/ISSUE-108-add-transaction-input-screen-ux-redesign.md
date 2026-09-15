# ISSUE-108: Redesign UX Screen Tambah Transaksi (+)

**Status:** Planned  
**Priority:** Medium  
**Type:** UI/UX Redesign  
**Depends on:** ISSUE-018, ISSUE-019, ISSUE-032, ISSUE-105, ISSUE-107  
**Estimated Effort:** Medium  

## Deskripsi

Redesign screen yang dibuka dari tombol `+` agar pengguna dapat mengisi transaksi dengan fokus, cepat, dan nyaman. Nominal menjadi fokus utama, pilihan transaksi disederhanakan, field sekunder diringkas, dan CTA simpan tetap mudah dijangkau saat keyboard terbuka.

## User Story

> Sebagai pengguna, saya ingin menambahkan transaksi tanpa menavigasi form yang terasa padat, sambil tetap memahami tipe transaksi, kategori, dan wallet yang dipilih.

## Scope

- Ganti judul menjadi **Tambah transaksi** atau judul sentence case yang konsisten.
- Pisahkan transaksi biasa menjadi dua pilihan utama: **Pengeluaran** dan **Pemasukan**.
- Pindahkan **Bill Reminder** ke entry point/form khusus agar tidak bercampur dengan transaksi satu kali.
- Jadikan nominal sebagai hero input dengan label **Jumlah**, keyboard numerik, format rupiah live, kontras placeholder/fokus yang jelas, dan cursor yang terlihat.
- Ringkas field menjadi urutan `Jumlah → Catatan (opsional) → Kategori → Dompet`.
- Tampilkan kategori dalam grid/chip yang rapi, dengan state terpilih yang jelas dan **Lihat semua** bila daftar panjang.
- Tampilkan wallet secara eksplisit; bila ada beberapa wallet, gunakan pilihan yang jelas dan default wallet terakhir/yang valid.
- Gunakan sticky CTA **Simpan pengeluaran** atau **Simpan pemasukan** di atas system navigation/keyboard.
- Sembunyikan bottom NavigationBar utama pada screen input; back header/gesture tetap tersedia.
- Tambahkan validasi inline dan dukungan IME/insets/scroll agar field aktif serta CTA tidak tertutup keyboard.

## Di Luar Scope

- Perubahan aturan validasi, skema Room, repository, atau format penyimpanan transaksi.
- Pengubahan alur AI Input, receipt scan, atau Chat AI.
- Penghapusan kemampuan Bill Reminder; hanya entry point dan presentasi form yang dipisahkan.

## Files Touched (Estimasi)

- Ubah: `app/src/main/java/com/example/expense_tracker/ui/input/InputScreen.kt`.
- Ubah bila diperlukan: `InputViewModel.kt`, `InputUiState.kt`, `MainActivity.kt`, `NavRoutes.kt`, `BottomNavBar.kt`, dan resource string/theme.
- Tambah/ubah unit atau UI test untuk state pilihan tipe, validasi, keyboard/insets, dan navigasi.

## Acceptance Criteria

- [ ] Screen input memiliki header ringkas dan judul sentence case yang konsisten.
- [ ] Pengeluaran dan Pemasukan dapat dipilih tanpa kehilangan data field yang masih relevan.
- [ ] Bill Reminder tidak tampil sebagai pilihan yang membingungkan di form transaksi biasa dan tetap dapat diakses melalui flow khusus.
- [ ] Nominal adalah fokus visual utama; input angka memakai keyboard numerik dan format rupiah tidak menghilangkan cursor/fokus.
- [ ] Catatan diberi label **Catatan (opsional)** dan tidak menghalangi pengisian nominal, kategori, atau wallet.
- [ ] Kategori memiliki layout konsisten, selected state kontras, area tap minimum 48dp, dan tidak menyisakan baris yang janggal.
- [ ] Wallet terlihat saat pemilihan diperlukan; transaksi tidak tersimpan ke wallet yang tidak dipahami pengguna.
- [ ] CTA sticky berubah menjadi **Simpan pengeluaran/pemasukan**, aktif hanya saat syarat valid terpenuhi, dan tetap terbaca saat disabled.
- [ ] Validasi jumlah, kategori, wallet, dan field Bill Reminder tampil inline dekat sumber masalah.
- [ ] Bottom NavigationBar tidak tampil pada screen input dan CTA tidak tertutup keyboard atau system navigation.
- [ ] Back, rotasi, font scale, dark/light theme, device width kecil, dan aksesibilitas tidak menyebabkan layout rusak atau data hilang.
- [ ] Save tetap menghasilkan tepat satu transaksi/pengingat sesuai perilaku existing; tidak ada perubahan pada persistence contract.

## Validasi dan Checkpoint

Jalankan unit/UI test yang relevan lalu lakukan smoke test manual untuk transaksi pengeluaran, pemasukan, multi-wallet, Bill Reminder, nominal panjang, keyboard terbuka, invalid state, rotasi, dan dark/light theme. Catat screenshot atau hasil visual review sebelum menandai issue `Done`.

