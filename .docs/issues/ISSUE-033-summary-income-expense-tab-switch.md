# ISSUE-033: Income Summary Integration via Segmented Tab Switcher

## Deskripsi
Saat ini, halaman Summary (`SummaryScreen`) hanya menampilkan analisis & breakdown kategori untuk **Pengeluaran (Expense)**. Data **Pemasukan (Income)** belum terwakili di layar ringkasan.

Berdasarkan UI/UX Best Practice untuk aplikasi finansial modern, ringkasan pengeluaran dan pemasukan disatukan dalam **satu layar Summary** menggunakan **Segmented Control / Tab Switcher (`[ Pengeluaran | Pemasukan ]`)** tepat di bawah filter periode tanggal. Hal ini menjaga konsistensi konteks pengguna tanpa perlu berpindah-pindah layar.

## User Story
> Sebagai pengguna, saya ingin melihat ringkasan & breakdown kategori untuk Pemasukan di samping Pengeluaran pada Halaman Ringkasan, sehingga saya dapat dengan mudah menganalisis dari mana sumber pemasukan saya berasal pada periode tertentu.

## Acceptance Criteria
- [ ] Menambahkan Segmented Control `[ Pengeluaran | Pemasukan ]` pada Halaman Ringkasan (`SummaryScreen`)
- [ ] Berpindah ke Tab **Pemasukan** menampilkan:
  - Donut Chart pemasukan dengan skema warna hijau/emerald
  - Total Pemasukan pada bagian tengah Donut Chart
  - Grid/List breakdown kategori pemasukan (Gaji, Freelance, Bonus, Transfer Masuk, dll) beserta persentase & nominal
- [ ] Berpindah ke Tab **Pengeluaran** tetap menampilkan ringkasan pengeluaran yang sudah ada
- [ ] Filter tanggal (Hari ini, Minggu ini, Bulan ini, Tahun ini, Custom Date) berlaku secara dinamis untuk kedua tab
- [ ] Tampilan Empty State yang sesuai saat data pemasukan kosong ("Belum ada data pemasukan")

## Technical Details

### 1. Update State & ViewModel
**[MODIFY]** [SummaryUiState.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/summary/SummaryUiState.kt)
- Tambahkan `transactionType: TransactionType = TransactionType.EXPENSE` ke `SummaryUiState`.

**[MODIFY]** [SummaryViewModel.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/summary/SummaryViewModel.kt)
- Tambahkan fungsi `onTransactionTypeSelected(type: TransactionType)`.
- Update logika kalkulasi breakdown: jika `transactionType == TransactionType.INCOME`, ambil data transaksi kategori ber-type `INCOME` atau `BOTH` dan hitung total pemasukan.

### 2. Update UI & Donut Chart
**[MODIFY]** [SummaryScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/summary/SummaryScreen.kt)
- Tambahkan Segmented Control `[ Pengeluaran | Pemasukan ]` di bawah tab filter periode tanggal.
- Update `DonutChart` untuk mendukung palet warna pemasukan (Emerald/Teal/Green gradient colors) saat dalam mode Income.

## Skills
- UI/UX Financial Dashboard Pattern: Penggabungan Income & Expense dalam single-view tabbed architecture
- Reactive State Filtering: Pemfilteran ganda (TransactionType x DatePeriod) di ViewModel
- Dynamic Color Palette Theming: Adaptasi warna Donut Chart & Chips berdasarkan konteks transaksi
