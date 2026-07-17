# ISSUE-001: Ganti SegmentedButton dengan M3 NavigationBar

**Priority:** 🔴 High  
**Type:** UI Refactor  
**Sprint:** Sprint 1  
**Estimated Effort:** Medium  

---

## Deskripsi

Saat ini aplikasi menggunakan `SingleChoiceSegmentedButtonRow` sebagai filter period (Hari Ini / Minggu Ini / Bulan Ini) di dalam body HomeScreen. Komponen ini akan **dihapus** dan digantikan oleh **Material Design 3 `NavigationBar`** yang ditempatkan di bottom screen, sesuai [M3 Navigation Bar Guidelines](https://m3.material.io/components/navigation-bar/guidelines).

NavigationBar akan menjadi navigasi utama aplikasi (bukan hanya filter), dengan destination:
- **Home** — Halaman utama (daftar transaksi)
- **Summary** — Halaman summary/breakdown
- **Add (+)** — FAB-style center button untuk tambah transaksi
- **Profile** — Placeholder untuk future feature

## Kondisi Saat Ini

- [FilterTabs](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeScreen.kt#L222-L250) menggunakan `SingleChoiceSegmentedButtonRow` + `SegmentedButton`
- Navigasi antar screen dihandle via callback (`onNavigateToInput`, `onNavigateToSummary`) tanpa bottom bar
- [MainActivity](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/MainActivity.kt) menggunakan `Scaffold` kosong (tanpa `bottomBar`)

## Perubahan yang Dibutuhkan

### Prinsip: Clean Architecture · Clean Code · KISS · YAGNI

### Data Layer
- Tidak ada perubahan. YAGNI — navigasi bar tidak membutuhkan perubahan di data layer.

### UI Layer

#### [MODIFY] [NavRoutes.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/navigation/NavRoutes.kt)
- Pastikan semua route destinations terdefinisi dengan jelas (HOME, SUMMARY, INPUT)
- Tidak perlu menambah route baru kecuali ada screen baru

#### [NEW] `ui/navigation/BottomNavBar.kt`
- Buat composable `BottomNavBar` menggunakan `NavigationBar` + `NavigationBarItem` dari M3
- Items: Home (Icons.Filled.Home), Summary (Icons.Filled.PieChart), Add (Icons.Filled.Add), Profile (Icons.Filled.Person)
- Item "Add" dibuat visually distinct (elevated/tinted) sesuai gambar referensi
- Highlight item aktif menggunakan M3 default indicator
- Terima `currentRoute: String?` dan `onNavigate: (String) -> Unit` sebagai parameter

#### [MODIFY] [MainActivity.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/MainActivity.kt)
- Tambahkan `BottomNavBar` ke `Scaffold.bottomBar`
- Integrasikan dengan `NavController` untuk navigasi antar screen
- Handle item "Add" untuk navigate ke InputScreen

#### [MODIFY] [HomeScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeScreen.kt)
- **Hapus** composable `FilterTabs` (line 222-250) beserta pemanggilan di `HomeScreen`
- **Hapus** import `SegmentedButton`, `SegmentedButtonDefaults`, `SingleChoiceSegmentedButtonRow`
- Filter period tidak lagi diperlukan dari segmented button — KISS, default ke `MONTH` atau exposed via small dropdown di header kalau masih needed

## Acceptance Criteria

- [ ] `NavigationBar` M3 tampil di bottom semua screen (Home & Summary)
- [ ] Navigasi antar Home, Summary, dan Input berjalan smooth
- [ ] `SegmentedButton` / `FilterTabs` sudah dihapus dari HomeScreen
- [ ] Tidak ada breaking changes pada fitur yang sudah ada
- [ ] Mengikuti M3 NavigationBar spec: max 5 items, icon + label, active indicator

## Referensi

- [M3 Navigation Bar Guidelines](https://m3.material.io/components/navigation-bar/guidelines)
- [Jetpack Compose NavigationBar](https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#NavigationBar)
