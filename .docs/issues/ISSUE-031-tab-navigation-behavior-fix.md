# ISSUE-031: Fix Summary Tab Navigation Flow from Home "View All" Button

## Deskripsi
Saat ini, ketika user menekan tombol "View All" (Lihat Semua) pada Halaman Home, aplikasi berpindah ke halaman Summary (`NavRoutes.SUMMARY`). Namun, perpindahan ini menggunakan `navController.navigate(NavRoutes.SUMMARY)` biasa tanpa konfigurasi Tab Navigation (`launchSingleTop = true`, `restoreState = true`, `popUpTo`).

Akibatnya, rute `SUMMARY` ditumpuk di atas rute `HOME` sebagai halaman anak (nested screen) menggunakan animasi slide. Saat pengguna menekan tab Home di navigasi bawah, halaman Summary masih muncul atau kembali tertumpuk sehingga membutuhkan tombol backspace/kembali untuk menampilkan Halaman Home yang sebenarnya.

Perbaikan ini akan memastikan tombol "View All" di Home memicu navigasi tab resmi (`navigateToTab(NavRoutes.SUMMARY)`), serta menambahkan transisi fade pada `NavRoutes.SUMMARY` agar konsisten dengan tab navigasi utama lainnya (`HOME`, `WALLET`, `PROFILE`).

## User Story
> Sebagai pengguna, saat saya menekan tombol "Lihat Semua" di Halaman Home, saya ingin aplikasi secara mulus berpindah ke Tab Summary, dan saat saya menekan Tab Home kembali, aplikasi langsung menampilkan Halaman Home tanpa ada layar tertumpuk atau membutuhkan tombol kembali.

## Acceptance Criteria
- [ ] Menekan tombol "View All" / "Lihat Semua" di Halaman Home mengaktifkan Tab Summary secara resmi
- [ ] Navigasi ke Tab Summary menggunakan transisi Fade yang konsisten dengan tab navigasi lainnya
- [ ] Menekan Tab Home setelah dari Summary langsung menampilkan Halaman Home secara bersih tanpa layar tertumpuk
- [ ] Konsistensi navigasi diterapkan di seluruh rute tab utama aplikasi

## Technical Details

### Root Cause
Di [MainActivity.kt](file:///c:/dame-project/Android/expense_tracker/MainActivity.kt), callback `onNavigateToSummary` di rute `NavRoutes.HOME` saat ini didefinisikan sebagai `{ navController.navigate(NavRoutes.SUMMARY) }` tanpa opsi `launchSingleTop`, `restoreState`, dan `popUpTo`. Selain itu, `composable(NavRoutes.SUMMARY)` belum diberikan animasi `fadeIn`/`fadeOut`.

### Fix
1. **[MODIFY]** [MainActivity.kt](file:///c:/dame-project/Android/expense_tracker/MainActivity.kt)
   - Buat helper lambda `navigateToTab: (String) -> Unit` yang mengelola navigasi tab dengan `popUpTo(startDestinationId) { saveState = true }`, `launchSingleTop = true`, dan `restoreState = true`.
   - Gunakan `navigateToTab(NavRoutes.SUMMARY)` pada `onNavigateToSummary` di `HomeScreen`.
   - Tambahkan `enterTransition`, `exitTransition`, `popEnterTransition`, `popExitTransition` berbasis `fadeIn`/`fadeOut` pada `composable(NavRoutes.SUMMARY)`.

## Skills
- Jetpack Navigation & Tab Backstack Management: Penggunaan `saveState`, `restoreState`, dan `launchSingleTop` untuk mencegah penumpukan halaman tab
- Consistent Material Design Transitions: Penggunaan animasi fade yang seragam untuk semua destinasi navigasi utama (top-level tabs)
- Single Responsibility & Clean Routing: Memastikan gesture dan tombol mengarah pada handler navigasi yang terpusat
