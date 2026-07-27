# ISSUE-068: Halaman In-App Kebijakan Privasi

## Deskripsi
Saat ini, menu **"Kebijakan Privasi"** di halaman Profil membuka link eksternal ke Google Search. Ini memberikan pengalaman yang kurang profesional. Halaman ini harus diganti menjadi halaman in-app yang menampilkan teks kebijakan privasi secara lengkap, mirip dengan tampilan *Privacy Policy* / *Terms and Conditions* pada aplikasi banking.

### Perilaku Saat Ini
1. Klik "Kebijakan Privasi" → membuka browser eksternal ke `google.com/search?q=kasflow+privacy+policy`.
2. Pengalaman pengguna terputus karena harus keluar dari aplikasi.

### Perilaku yang Diharapkan
1. Klik "Kebijakan Privasi" → navigasi ke halaman baru **di dalam aplikasi**.
2. Halaman menampilkan teks kebijakan privasi yang terstruktur dan mudah dibaca.
3. Konten disusun dengan heading dan paragraf (mirip dokumen legal pada app banking).
4. Tersedia AppBar dengan judul "Kebijakan Privasi" dan tombol Back.
5. Teks bersifat statis (hardcoded), mencakup:
   - Pendahuluan
   - Data yang Dikumpulkan (hanya lokal, tidak ada server)
   - Penggunaan Data
   - Keamanan Data
   - Hak Pengguna
   - Kontak

## Acceptance Criteria
- [ ] Dibuat rute navigasi baru `PRIVACY_POLICY` di `NavRoutes.kt`.
- [ ] Dibuat halaman `PrivacyPolicyScreen.kt` di package `ui/profile`.
- [ ] Halaman menampilkan teks kebijakan privasi yang terstruktur rapi (heading + paragraf).
- [ ] Halaman scrollable menggunakan `LazyColumn`.
- [ ] Halaman memiliki TopAppBar dengan tombol Back dan mengikuti M3 guidelines (windowInsets).
- [ ] Menu "Kebijakan Privasi" di `ProfileScreen.kt` diubah untuk navigasi ke halaman baru.
- [ ] Konten kebijakan privasi dalam Bahasa Indonesia dan Bahasa Inggris (menggunakan `strings.xml`).
- [ ] Build & CI GitHub Actions pipeline tetap hijau.

## Technical Details

### Navigasi
- Tambah rute `PRIVACY_POLICY = "privacy_policy"` di `NavRoutes.kt`.
- Daftarkan composable di `MainActivity.kt`.
- Update `ProfileScreen` untuk menerima callback `onNavigateToPrivacyPolicy`.

### UI Layer
- Gunakan `LazyColumn` dengan section heading dan paragraf.
- Gunakan `MaterialTheme.typography.titleMedium` untuk heading dan `bodyMedium` untuk paragraf.
- Ikuti M3 TopAppBar guidelines (`windowInsets = WindowInsets(0,0,0,0)`).
- Berikan nuansa formal/legal namun tetap modern dan readable.

### Data Layer
- Tidak perlu perubahan database. Konten bersifat statis.
- Buat list `PrivacySection(val title: String, val content: String)` sebagai data model.

## Skills
- Clean Architecture: Pemisahan UI layer, tidak perlu data layer karena konten statis.
- Clean Code: Naming convention, single responsibility.
- KISS: Konten hardcoded, tidak perlu backend / CMS.
- YAGNI: Tidak perlu fitur versioning kebijakan untuk saat ini.
- CI GitHub Actions: Validasi build + test otomatis.
