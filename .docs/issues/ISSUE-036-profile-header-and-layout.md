# ISSUE-036: Profile Layout & User Header UI

## Deskripsi
Pembuatan fondasi dasar halaman Profile (`ProfileScreen`). Ini mencakup kerangka halaman, TopAppBar, dan bagian Header yang menampilkan profil pengguna (Avatar, Nama, dan Email/Username).

## Acceptance Criteria
- [ ] Membuat file komponen/memperbarui `ProfileScreen` di struktur navigasi jika diperlukan.
- [ ] Menambahkan TopAppBar dengan judul "Profil" atau membiarkannya clean tanpa AppBar jika preferensi desain menghendaki.
- [ ] Menambahkan komponen `ProfileHeader` yang berisi gambar avatar bundar, teks nama pengguna ("Adam"), dan badge status ("Pro Member" / email).
- [ ] Menyiapkan container utama (`LazyColumn` atau `Column` + `verticalScroll`) untuk menampung daftar menu pengaturan di bawahnya.

## Skills
- Jetpack Compose Layouts (Column, Row, Box).
- Image & Clip Shape untuk Avatar Profile (CircleShape).
