# ISSUE-023: Custom Navigation Transitions (Slide In/Out)

## Deskripsi
Menambahkan animasi transisi navigasi `slideInHorizontally` / `slideOutHorizontally` pada `NavHost` di `MainActivity.kt`, agar perpindahan antar-layar terasa mulus dan natural sesuai best practice Material Design 3.

Saat ini perpindahan layar menggunakan transisi default Compose Navigation (fade/instant jump), yang terasa kaku dan kurang profesional.

## User Story
> Sebagai user, saya ingin perpindahan halaman terasa mulus dengan animasi geser, sehingga saya mengerti konteks navigasi (masuk/keluar halaman).

## Acceptance Criteria
- [ ] Navigasi **maju** (buka halaman baru): layar baru slide-in dari **kanan**, layar lama slide-out ke **kiri**
- [ ] Navigasi **mundur** (Back): layar sekarang slide-out ke **kanan**, layar sebelumnya slide-in dari **kiri**
- [ ] Tab bottom navigation (Home, Wallet, Profile) menggunakan **fade transition** (bukan slide) agar terasa seperti tab switch
- [ ] Durasi animasi mengikuti M3 motion guidelines (~300ms, `EaseInOut`)
- [ ] Tidak ada lag/jank pada perangkat mid-range

## Technical Details

### Root Cause
Di [MainActivity.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/MainActivity.kt#L100-L209), `NavHost` tidak memiliki parameter `enterTransition`, `exitTransition`, `popEnterTransition`, dan `popExitTransition`. Compose Navigation default ke `fadeIn`/`fadeOut` yang sangat cepat.

### Fix
1. Tambahkan **default transitions** pada `NavHost` level:
   - `enterTransition`: `slideInHorizontally(towards = END)` + `fadeIn`
   - `exitTransition`: `slideOutHorizontally(towards = START)` + `fadeOut`
   - `popEnterTransition`: `slideInHorizontally(towards = START)` + `fadeIn`
   - `popExitTransition`: `slideOutHorizontally(towards = END)` + `fadeOut`

2. **Override** untuk tab routes (HOME, WALLET, PROFILE): gunakan `fadeIn`/`fadeOut` saja agar perpindahan tab terasa instan, bukan geser.

#### [MODIFY] [MainActivity.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/MainActivity.kt)
- Tambahkan import `androidx.compose.animation.*`
- Atur default transition di `NavHost`
- Override transition pada `composable()` untuk tab routes

### Dependencies
- Tidak ada dependency tambahan — `compose-navigation` sudah include animation API.

## Skills
- M3 Motion Guidelines: Mengikuti durasi & easing standar Material
- Clean Code: Transitions didefinisikan sebagai reusable constants
- KISS: Gunakan API bawaan Compose Navigation, tanpa library tambahan
