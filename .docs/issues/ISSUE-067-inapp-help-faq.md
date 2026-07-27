# ISSUE-067: Halaman In-App Pusat Bantuan / FAQ

## Deskripsi
Saat ini, menu **"Pusat Bantuan / FAQ"** di halaman Profil membuka link eksternal ke Google Search. Ini memberikan pengalaman yang kurang profesional dan tidak sesuai dengan standar aplikasi finance/banking modern.

Menu ini harus diganti menjadi halaman in-app yang menampilkan daftar FAQ (Frequently Asked Questions) dengan tampilan expandable/accordion, mirip halaman *Terms & Conditions* atau *FAQ* pada aplikasi banking.

### Perilaku Saat Ini
1. Klik "Pusat Bantuan / FAQ" → membuka browser eksternal ke `google.com/search?q=kasflow+help`.
2. Pengalaman pengguna terputus karena harus keluar dari aplikasi.

### Perilaku yang Diharapkan
1. Klik "Pusat Bantuan / FAQ" → navigasi ke halaman baru **di dalam aplikasi**.
2. Halaman menampilkan daftar pertanyaan umum (FAQ) dengan format **expandable card/accordion**.
3. User dapat tap pertanyaan untuk melihat/menyembunyikan jawaban.
4. Tersedia AppBar dengan judul "Pusat Bantuan / FAQ" dan tombol Back.
5. Konten FAQ bersifat statis (hardcoded) untuk saat ini, mencakup topik umum:
   - Cara menambah transaksi
   - Cara mengelola dompet
   - Cara melihat ringkasan pengeluaran
   - Cara mengubah mata uang
   - Cara mengekspor data
   - Cara mengaktifkan kunci layar
   - Apa itu Bill Reminder

## Acceptance Criteria
- [ ] Dibuat rute navigasi baru `HELP_FAQ` di `NavRoutes.kt`.
- [ ] Dibuat halaman `HelpFaqScreen.kt` di package `ui/profile`.
- [ ] FAQ ditampilkan dengan UI expandable (click to expand/collapse).
- [ ] Halaman memiliki TopAppBar dengan tombol Back dan mengikuti M3 guidelines (windowInsets).
- [ ] Menu "Pusat Bantuan / FAQ" di `ProfileScreen.kt` diubah untuk navigasi ke halaman baru.
- [ ] Konten FAQ dalam Bahasa Indonesia dan Bahasa Inggris (menggunakan `strings.xml`).
- [ ] Build & CI GitHub Actions pipeline tetap hijau.

## Technical Details

### Navigasi
- Tambah rute `HELP_FAQ = "help_faq"` di `NavRoutes.kt`.
- Daftarkan composable di `MainActivity.kt`.
- Update `ProfileScreen` untuk menerima callback `onNavigateToHelpFaq`.

### UI Layer
- Gunakan `LazyColumn` dengan item FAQ yang bisa di-expand/collapse.
- Setiap item FAQ terdiri dari pertanyaan (header) dan jawaban (body).
- Gunakan `AnimatedVisibility` atau `animateContentSize()` untuk animasi expand/collapse.
- Ikuti M3 TopAppBar guidelines (`windowInsets = WindowInsets(0,0,0,0)`).

### Data Layer
- Tidak perlu perubahan database. FAQ bersifat statis.
- Buat data class `FaqItem(val question: String, val answer: String)` dan list hardcoded.

## Skills
- Clean Architecture: Pemisahan UI layer, tidak perlu data layer karena konten statis.
- Clean Code: Naming convention, single responsibility.
- KISS: Konten FAQ hardcoded, tidak perlu backend.
- YAGNI: Tidak perlu fitur search/filter FAQ untuk saat ini.
- CI GitHub Actions: Validasi build + test otomatis.
