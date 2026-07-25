# ISSUE-062: Bill Reminder — Paid Status Visibility & Monthly Auto-Reset

## Deskripsi
Saat ini, ketika user membayar bill reminder yang bersifat **berulang bulanan** (`isRepeat = true`), field `lastPaidMonth` di-update ke bulan ini. Namun, UI halaman **Bill Reminders** tidak membedakan antara bill yang sudah dibayar bulan ini dengan yang belum. Akibatnya, bill yang sudah dibayar "seolah-olah hilang" karena query hanya menampilkan yang `isActive = 1`, padahal bill tersebut masih aktif — hanya sudah lunas bulan ini.

### Perilaku Saat Ini (Bug)
1. User tekan "Bayar" pada bill berulang → `lastPaidMonth` di-update.
2. Bill tetap tampil di list tapi **tidak ada indikasi visual** bahwa sudah dibayar.
3. Tombol "Bayar" masih aktif → user bisa double-pay.
4. Di **HomeScreen**, badge counter sudah benar (hanya menghitung yang `lastPaidMonth != currentMonth`), tapi di halaman Bill Reminders sendiri tidak ada pembedaan.

### Perilaku yang Diharapkan
1. Setelah user tekan "Bayar", bill **tetap tampil** di list.
2. Tampilkan **badge/chip "Sudah Dibayar"** berwarna hijau pada card bill tersebut.
3. **Tombol "Bayar" di-disable** (greyed out) untuk mencegah double payment.
4. Pada **awal bulan baru**, status otomatis reset → bill kembali bisa dibayar (tombol "Bayar" aktif kembali, badge "Sudah Dibayar" hilang).
5. Bill yang `isRepeat = false` dan sudah dibayar → tetap tampil dengan status "Lunas" dan tidak bisa dibayar lagi.

## Acceptance Criteria
- [x] Bill berulang yang sudah dibayar bulan ini tetap tampil di list dengan badge **"Sudah Dibayar"**.
- [x] Tombol "Bayar" di-disable ketika `lastPaidMonth == currentMonth`.
- [x] Pada awal bulan baru, bill berulang otomatis kembali ke status **"Belum Dibayar"** (tanpa perlu action dari user).
- [x] Bill sekali bayar (`isRepeat = false`) yang sudah dibayar tetap tampil dengan status **"Lunas"** dan tombol "Bayar" di-disable permanen.
- [x] Pesan **"No active reminders"** hanya muncul jika memang tidak ada bill reminder sama sekali (bukan karena semua sudah dibayar).
- [x] Unit test untuk logika `isPaidThisMonth` dan reset bulan baru.
- [x] Build & CI GitHub Actions pipeline tetap hijau.

## Technical Details

### Data Layer
- **Tidak perlu migrasi database**. Field `lastPaidMonth` dan `isActive` sudah ada.
- Ubah query di `BillReminderDao.getActiveReminders()`:
  - Saat ini: `WHERE isActive = 1` → hanya bill aktif.
  - Perubahan: Tampilkan **semua bill** (`isActive = 1` untuk berulang, **termasuk** yang sudah dibayar bulan ini). Bill `isRepeat = false` yang `isActive = false` tetap ditampilkan dengan status "Lunas".
- Alternatif: Tambahkan query baru `getAllVisibleReminders()` yang menampilkan bill aktif + bill sekali bayar yang baru saja dinonaktifkan.

### UI Layer (`ReminderListScreen.kt`)
- Tambahkan computed property `isPaidThisMonth`:
  ```kotlin
  val currentMonth = YearMonth.now().toString()
  val isPaid = reminder.lastPaidMonth == currentMonth
  ```
- **Card visual state**:
  - Belum bayar: Tampilan seperti sekarang (tombol "Bayar" aktif).
  - Sudah bayar: Badge hijau **"✓ Sudah Dibayar"**, tombol "Bayar" greyed out / disabled.
  - Lunas (sekali bayar): Badge abu-abu **"Lunas"**, tombol "Bayar" dihilangkan.
- Urutkan list: **Belum dibayar di atas**, sudah dibayar di bawah.

### ViewModel (`ReminderListViewModel.kt`)
- Update `markAsPaid()`: Untuk bill `isRepeat = false`, tetap set `isActive = false` tapi **jangan hapus dari UI**. Update `lastPaidMonth` juga agar ada timestamp pembayaran.
- Hapus kondisi yang menyembunyikan bill yang sudah dibayar dari list.

### Unit Test
- Test: `markAsPaid` pada bill berulang → `lastPaidMonth` terupdate, `isActive` tetap `true`.
- Test: `markAsPaid` pada bill sekali bayar → `isActive = false`, `lastPaidMonth` terupdate.
- Test: `isPaidThisMonth` mengembalikan `true` jika `lastPaidMonth == currentMonth`.
- Test: Di bulan baru, `isPaidThisMonth` otomatis `false` (natural reset tanpa perlu cron/worker).

## Skills
- Clean Architecture: Pemisahan data/domain/ui layer
- Clean Code: Naming convention, single responsibility
- KISS: Cukup tambah 1 kolom, filter di query
- YAGNI: Tidak perlu icon/color per kategori sekarang
- CI GitHub Actions: Validasi build + test otomatis
