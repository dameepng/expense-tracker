# ISSUE-046: ViewModel Dibuat Ulang Setiap Navigasi (Tidak Di-cache)

## Priority: 🔴 Critical
## Type: Performance / Architecture
## Status: Closed

## Deskripsi
Setiap kali pengguna berpindah ke sebuah tab (Home, Summary, Wallet, Profile), ViewModel baru dibuat dari nol melalui `viewModel(factory = ...)` di dalam `composable { }`. Karena Navigation Compose me-*destroy* composable saat keluar dari backstack, ViewModel juga ikut hancur, lalu **dibuat ulang + query database ulang** saat kembali.

Ini adalah penyebab utama "lambat saat pindah halaman" karena:
1. Setiap navigasi = instantiate ViewModel baru
2. Setiap ViewModel baru = query Room Database dari awal
3. Ditambah artificial delay 300ms (ISSUE-045)

## File yang Terpengaruh
- `MainActivity.kt` (line 248-354): Semua `composable { }` block membuat ViewModel baru setiap kali.

## Solusi
Gunakan `remember` + `ViewModelStoreOwner` yang ter-*scope* ke `NavBackStackEntry` (ini sudah default di Navigation Compose), **tetapi** pastikan kita menggunakan strategi `saveState = true` dan `restoreState = true` secara konsisten pada navigasi bottom tab agar backstack entry tetap hidup.

Alternatif yang lebih agresif:
- Gunakan `hiltViewModel()` atau buat scoped ViewModel store manual di level `ExpenseTrackerApp` untuk tab-tab utama, sehingga ViewModel tidak perlu di-recreate.

## Verifikasi
- Berpindah antar tab 5x berturut-turut dan pastikan tidak ada loading indicator yang muncul di tab yang sudah pernah dikunjungi.
