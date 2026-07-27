# ISSUE-066: Category Detail - Clickable Spending by Category

## Deskripsi
Saat ini, pada halaman **Ringkasan (Summary)**, bagian **"Spending by Category"** (Pengeluaran Berdasarkan Kategori) hanya berupa list statis. Jika user melihat kategori "Belanja" memiliki pengeluaran Rp 10.000.000, user tidak bisa melihat rincian transaksi apa saja yang membentuk total tersebut.

Untuk meningkatkan *insight-driven UX*, card kategori tersebut harus bisa diklik (tappable) dan akan membuka halaman detail baru yang menampilkan daftar transaksi khusus untuk kategori tersebut dalam periode dan wallet yang sedang aktif di Summary.

### Perilaku Saat Ini
1. Card kategori di halaman Summary bersifat statis (tidak bisa diklik).
2. Tidak ada cara bagi user untuk men-drill-down pengeluaran kategori.

### Perilaku yang Diharapkan
1. Card kategori di "Spending by Category" menjadi clickable.
2. Ketika diklik, aplikasi bernavigasi ke halaman **Detail Kategori** (Category Detail Screen).
3. Halaman ini menampilkan **List Transaksi** dari kategori yang diklik, menggunakan UI list transaksi yang **konsisten dengan halaman Home** (berupa `LazyColumn` yang menampilkan ikon kategori, deskripsi, tanggal, dan nominal, mendukung swipe to dismiss / edit).
4. Data transaksi yang ditampilkan sesuai dengan filter yang sedang aktif di Summary (wallet tertentu atau all wallets, periode bulan tertentu, dll).
5. Tersedia AppBar dengan judul nama kategori (misal: "Detail Belanja") dan tombol "Back" ke Summary.

## Acceptance Criteria
- [ ] Card kategori di SummaryScreen bisa diklik (memberikan efek ripple).
- [ ] Dibuat rute baru di `NavRoutes.kt` untuk `CATEGORY_DETAIL` (menerima argumen `categoryId`, dan mungkin argumen filter tambahan atau mengambil filter dari repository/shared state).
- [ ] Dibuat halaman `CategoryDetailScreen` yang menampilkan daftar transaksi.
- [ ] UI List transaksi meminjam atau menggunakan komponen yang sama/konsisten dengan di `HomeScreen` (misalnya memisahkan UI list transaksi menjadi *shared composable* agar tidak terjadi duplikasi kode, jika memungkinkan, atau menyalin gaya UI-nya).
- [ ] `CategoryDetailViewModel` dibuat (atau `SummaryViewModel` diubah jika dirasa cukup) untuk mem-fetch transaksi berdasarkan `categoryId`, periode, dan wallet yang diteruskan.
- [ ] Unit Test dibuat untuk memverifikasi behavior di ViewModel yang baru.
- [ ] Build & CI GitHub Actions pipeline tetap hijau.

## Technical Details

### Navigasi
Kita perlu rute navigasi baru. Karena detail transaksi bergantung pada rentang waktu (`startTime`, `endTime`) dan `walletId` yang sedang aktif di Summary, kita bisa:
- **Opsi A**: Mengirim parameter `categoryId`, `walletId`, `startTime`, dan `endTime` via argumen rute.
- **Opsi B**: Membuat shared ViewModel atau State holder untuk Summary Filters, tapi ini lebih kompleks.
Opsi A (mengirim argumen via savedStateHandle atau route parameters) lebih direkomendasikan karena *stateless* dan sederhana. 

Contoh route (jika string argument): `category_detail/{categoryId}?walletId={walletId}&start={start}&end={end}`

### Data Layer
- Query ke database yang diperlukan: `getTransactionsByCategoryAndWallet(categoryId, walletId, startTime, endTime)` di DAO. Perlu ditambahkan ke `ExpenseDao.kt` jika belum ada.

### UI Layer
- Ekstrak UI *Transaction List Item* dari `HomeScreen.kt` menjadi komponen tersendiri (misal: `TransactionListItem.kt`) jika belum terpisah, sehingga dapat di-reuse di `HomeScreen`, `SummaryScreen` (jika ada), dan `CategoryDetailScreen`.

## Skills
- Clean Architecture: Pemisahan data/domain/ui layer
- Clean Code: Naming convention, single responsibility, DRY (Don't Repeat Yourself) melalui reusable UI components.
- KISS: Buat UI list konsisten dengan mendaur ulang Composable yang sudah ada, jangan membuat list styling baru dari awal.
- YAGNI: Hanya buat parameter navigasi yang dibutuhkan.
- CI GitHub Actions: Validasi build + test otomatis.
