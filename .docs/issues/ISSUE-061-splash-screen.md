# ISSUE-061: Splash Screen

## Deskripsi
Menambahkan Splash Screen saat aplikasi pertama kali dibuka. Splash Screen akan menampilkan logo/asset branding aplikasi sebelum user masuk ke halaman utama (Home). Asset gambar yang digunakan sudah tersedia di `app/src/main/res/splash_screen.png`.

## Acceptance Criteria
- [x] Splash Screen muncul saat aplikasi pertama kali diluncurkan (cold start).
- [x] Menggunakan Android 12+ **SplashScreen API** (`core-splashscreen`) agar kompatibel ke versi Android lama sekaligus mengikuti standar modern.
- [x] Menampilkan asset `splash_screen.png` sebagai ikon/branding di tengah layar.
- [x] Background Splash Screen menggunakan warna yang sesuai dengan tema aplikasi.
- [x] Splash Screen otomatis menghilang setelah aplikasi siap (tidak ada delay buatan yang berlebihan).
- [x] Transisi dari Splash Screen ke Home Screen terasa mulus tanpa flicker/blank screen.
- [x] Build & CI GitHub Actions pipeline tetap hijau.

## Technical Details

### Data Layer
- Tidak ada perubahan pada data layer.

### UI Layer
- Implementasi menggunakan `androidx.core:core-splashscreen` library.
- Konfigurasi tema Splash Screen di `res/values/themes.xml` (atau `splash_theme.xml`):
  - `windowSplashScreenBackground`: Warna background.
  - `windowSplashScreenAnimatedIcon`: Referensi ke drawable `splash_screen.png`.
- Set tema Splash Screen pada `AndroidManifest.xml` di tag `<activity>` untuk `MainActivity`.
- Panggil `installSplashScreen()` di `MainActivity.onCreate()` sebelum `setContent {}`.
- Opsional: Tambahkan animasi exit menggunakan `splashScreen.setOnExitAnimationListener` untuk transisi yang lebih halus.

### Asset
- File: [splash_screen.png](file:///c:/dame-project/Android/expense_tracker/app/src/main/res/splash_screen.png)
- Perlu di-convert atau di-copy ke folder drawable yang sesuai (`res/drawable/` atau `res/drawable-xxxhdpi/`).

## Skills
- Clean Architecture: Pemisahan data/domain/ui layer
- Clean Code: Naming convention, single responsibility
- KISS: Cukup tambah 1 kolom, filter di query
- YAGNI: Tidak perlu icon/color per kategori sekarang
- CI GitHub Actions: Validasi build + test otomatis
