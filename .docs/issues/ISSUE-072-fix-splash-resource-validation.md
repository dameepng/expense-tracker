# ISSUE-072: Fix Splash Screen Resource Validation Errors

## Deskripsi
Pada saat menjalankan **Inspect Code** di Android Studio, ditemukan error validasi resource terkait splash screen:

1. **AndroidManifest.xml**: `Cannot resolve symbol '@style/Theme.Expense_tracker.Splash'`
2. **themes.xml**: `Cannot resolve symbol '@color/splash_background'` dan `Cannot resolve symbol '@drawable/splash_icon'`
3. **splash_icon.xml**: `Cannot resolve symbol '@drawable/splash_screen'`

### Analisis
Setelah investigasi, **semua resource yang direferensikan sebenarnya sudah ada** di project:
- `@style/Theme.Expense_tracker.Splash` → ada di `res/values/themes.xml` (baris 6-10)
- `@color/splash_background` → ada di `res/values/colors.xml` (baris 10, `#FF0066AE`)
- `@drawable/splash_icon` → ada di `res/drawable/splash_icon.xml`
- `@drawable/splash_screen` → ada di `res/drawable/splash_screen.png`

Error ini terjadi karena **Android Resources Validation** di Android Studio tidak selalu bisa melakukan resolve resource cross-file yang melibatkan tema SplashScreen API dari library `androidx.core:core-splashscreen`. Tema parent `Theme.SplashScreen` berasal dari library eksternal, dan Android Studio terkadang gagal memvalidasi property-property kustomnya secara statis.

### Solusi yang Diharapkan
Karena resource-resource ini sebenarnya sudah **valid dan berfungsi di runtime**, solusinya adalah menambahkan suppress annotation `tools:ignore` pada elemen-elemen yang menghasilkan false positive agar menghilangkan *noise* pada Inspect Code tanpa mengubah perilaku runtime.

Alternatifnya: jika project ini sudah tidak menggunakan *core-splashscreen* library (misalnya sudah menggunakan Compose-level splash), resource-resource ini bisa dihapus atau dimigrasi ke pendekatan yang lebih modern.

## Acceptance Criteria
- [ ] Error validasi splash screen pada Inspect Code hilang atau di-suppress.
- [ ] Splash screen tetap berfungsi normal saat aplikasi diluncurkan.
- [ ] Build & lint tetap hijau.

## Technical Details
### Pendekatan 1: Suppress (Minimal Risk)
- Tambahkan `tools:ignore="ResourceValidation"` pada elemen-elemen yang terdeteksi false positive.

### Pendekatan 2: Validasi Ulang Library (Jika Diperlukan)
- Pastikan dependency `androidx.core:core-splashscreen:1.0.1` masih up-to-date.
- Jalankan `./gradlew dependencies` untuk memastikan library ter-resolve.

## Skills
- KISS: Prioritaskan suppress jika hanya false positive.
- Clean Code: Hapus resource yang benar-benar tidak terpakai jika ditemukan.
