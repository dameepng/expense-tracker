# ISSUE-069: Cleanup Profile Screen - Hapus Item Tidak Relevan & Perbaiki Whitespace

## Deskripsi
Halaman Profil saat ini memiliki beberapa item menu yang tidak relevan dan whitespace berlebih yang membuat UI terasa kurang polished:

1. **"Beri Rating Aplikasi"** — aplikasi belum dipublish ke Play Store, sehingga fitur ini tidak berguna dan harus dihapus.
2. **"Keluar Akun" (Danger Zone)** — aplikasi ini **tidak memiliki halaman login/register**. Fitur "Keluar Akun" saat ini melakukan `clearAllPreferences()` dan `deleteDatabase()` lalu mengarahkan ke onboarding. Ini lebih tepat disebut **"Reset Data"** daripada "Keluar Akun". Seluruh section "Danger Zone" harus dihapus karena menyesatkan user.
3. **Whitespace berlebih** — terdapat `Spacer(height = 16.dp)` dan `Spacer(height = 24.dp)` yang berlebihan di beberapa tempat, menyebabkan layout tidak compact.

### Perilaku Saat Ini
1. Ada menu "Beri Rating Aplikasi" yang membuka Play Store (app belum dipublish).
2. Ada section "Danger Zone" dengan "Keluar Akun" padahal tidak ada fitur login.
3. Whitespace antar section terlalu besar, UI terlihat tidak padat.

### Perilaku yang Diharapkan
1. Menu "Beri Rating Aplikasi" **dihapus** dari section "Bantuan & Informasi".
2. Section "Danger Zone" beserta "Keluar Akun" **dihapus** seluruhnya.
3. Whitespace antar section dikurangi agar UI lebih clean dan compact.
4. Footer "Kasflow v1.0.0" tetap dipertahankan di bagian bawah.
5. Import yang tidak terpakai dibersihkan (misalnya `Icons.Default.Star`, `Icons.AutoMirrored.Filled.Logout`).
6. Callback `onLogoutSuccess` dihapus dari parameter `ProfileScreen` dan `MainActivity.kt`.

## Acceptance Criteria
- [ ] Menu "Beri Rating Aplikasi" dihapus dari `ProfileScreen.kt`.
- [ ] Section "Danger Zone" dan "Keluar Akun" dihapus dari `ProfileScreen.kt`.
- [ ] Fungsi `logout()` di `ProfileViewModel.kt` dihapus (opsional, bisa dipertahankan untuk masa depan).
- [ ] Parameter `onLogoutSuccess` dihapus dari `ProfileScreen` composable.
- [ ] Whitespace (Spacer) antar section dioptimalkan menjadi lebih compact (`8.dp` antar section).
- [ ] Import yang tidak terpakai dibersihkan.
- [ ] Build & CI GitHub Actions pipeline tetap hijau.

## Technical Details

### File yang Dimodifikasi
- **`ProfileScreen.kt`**: Hapus item menu, section, parameter, dan spacer berlebih.
- **`ProfileViewModel.kt`**: Hapus fungsi `logout()` (opsional).
- **`MainActivity.kt`**: Hapus referensi `onLogoutSuccess` pada composable `ProfileScreen`.

### Perubahan di ProfileScreen.kt
1. Hapus blok `SettingsItem` untuk "Beri Rating Aplikasi" (baris 244-260).
2. Hapus seluruh item blok section "Danger Zone" (baris 277-304).
3. Ubah `Spacer(modifier = Modifier.height(16.dp))` menjadi `Spacer(modifier = Modifier.height(8.dp))` antar section.
4. Hapus `Spacer(modifier = Modifier.height(32.dp))` sebelum footer, ganti dengan `Spacer(modifier = Modifier.height(16.dp))`.
5. Hapus import `Icons.Default.Star` dan `Icons.AutoMirrored.Filled.Logout`.

## Skills
- Clean Code: Menghapus dead code dan fitur yang tidak relevan.
- KISS: Jangan tampilkan fitur yang belum ada backend-nya (login/register).
- YAGNI: Hapus "Keluar Akun" karena tidak ada login flow.
- CI GitHub Actions: Validasi build + test otomatis.
