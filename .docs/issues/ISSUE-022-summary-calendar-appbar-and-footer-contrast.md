# ISSUE-022: Summary Screen — Move Calendar Icon to App Bar & Fix Total Footer BG

## Deskripsi
Dua perbaikan UI pada halaman Summary (`SummaryScreen`):
1. **Pindahkan icon calendar ke app bar** — Icon calendar (custom date picker) saat ini berada di samping segmented buttons, membuat area filter terlihat crowded. Pindahkan icon ini ke pojok kanan atas pada `TopAppBar` sebagai `actions` agar segmented buttons tampil lebih rapi dan simetris.
2. **Perbaiki warna background Total footer** — Background Total footer saat ini menggunakan warna yang sama dengan background layar (misal: dark mode → bg hitam), sehingga tidak kontras dan terlihat menyatu. Seharusnya menggunakan warna yang berlawanan/kontras — dark mode pakai bg terang, light mode pakai bg gelap.

## User Story
> Sebagai user, saya ingin segmented buttons pada halaman Summary tampil rapi tanpa icon calendar yang mengganggu, dan saya ingin footer total memiliki kontras yang jelas terhadap background layar.

## Acceptance Criteria

### Perbaikan 1: Calendar Icon → App Bar
- [ ] Icon calendar dipindahkan dari `SummaryFilterTabs` ke `TopAppBar` sebagai `actions`
- [ ] Segmented buttons hanya menampilkan 3 filter (Hari Ini, Minggu Ini, Bulan Ini) tanpa icon calendar di sampingnya
- [ ] Icon calendar tetap membuka `DateRangePicker` dialog saat ditekan
- [ ] State `showDatePicker` tetap berfungsi normal
- [ ] Warna icon calendar berubah saat filter CUSTOM aktif (sama seperti sebelumnya)

### Perbaikan 2: Total Footer Background Kontras
- [ ] Dark mode: footer total menggunakan background terang (misal `surfaceVariant` atau `inverseSurface`)
- [ ] Light mode: footer total menggunakan background gelap (misal `inverseSurface`)
- [ ] Teks pada footer total menggunakan warna yang kontras dengan background-nya
- [ ] Transisi warna smooth tanpa jarring effect

### Testing & CI
- [ ] UI manual: verifikasi posisi calendar icon dan kontras footer
- [ ] CI GitHub Actions pipeline hijau

## Technical Details

### Perbaikan 1: Root Cause & Fix

**Root Cause**: Di [SummaryScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/summary/SummaryScreen.kt#L76-L168), icon calendar ada di dalam `SummaryFilterTabs` composable (line 108-117), sejajar dengan `SingleChoiceSegmentedButtonRow`. Ini membuat area filter terlalu padat.

**Fix**: 
1. Pindahkan icon calendar ke `TopAppBar` → parameter `actions`
2. State `showDatePicker` dan `DatePickerDialog` harus diangkat ke level `SummaryScreen` agar accessible di `TopAppBar`
3. Hapus `IconButton` calendar dari `SummaryFilterTabs`
4. `SummaryFilterTabs` cukup menampilkan `SingleChoiceSegmentedButtonRow` saja

#### [MODIFY] [SummaryScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/summary/SummaryScreen.kt)

**Perubahan pada `SummaryScreen` composable (line 303-375)**:
- Tambahkan `var showDatePicker` state di level `SummaryScreen`
- Tambahkan `actions` pada `TopAppBar` dengan `IconButton` calendar
- Pindahkan `DatePickerDialog` ke level `SummaryScreen`

**Perubahan pada `SummaryFilterTabs` composable (line 76-168)**:
- Hapus parameter/state `showDatePicker`
- Hapus `IconButton` calendar (line 108-117)
- Hapus `DatePickerDialog` (line 120-167) — dipindahkan ke `SummaryScreen`
- `SingleChoiceSegmentedButtonRow` menggunakan full width tanpa `Modifier.weight(1f)`

---

### Perbaikan 2: Root Cause & Fix

**Root Cause**: Di [SummaryTotalFooter](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/summary/SummaryScreen.kt#L275-L299), `Row` tidak memiliki background color, sehingga mewarisi warna surface dari parent yang sama dengan background layar — tidak kontras.

**Fix**: Tambahkan `Modifier.background()` dengan warna `MaterialTheme.colorScheme.inverseSurface` pada `Row` di `SummaryTotalFooter`, dan ubah warna teks menjadi `MaterialTheme.colorScheme.inverseOnSurface` agar kontras dengan background.

#### [MODIFY] [SummaryScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/summary/SummaryScreen.kt)
- `SummaryTotalFooter`: Tambahkan background `inverseSurface` dan teks `inverseOnSurface`

### Dependency
- Tidak bergantung pada issue lain

## Skills
- Clean Architecture: State management diangkat ke parent composable
- Clean Code: Separation of concerns — filter tabs hanya menampilkan filter, app bar menangani actions
- KISS: Gunakan `inverseSurface`/`inverseOnSurface` dari Material 3 color scheme
- YAGNI: Tidak perlu custom color logic — M3 sudah menyediakan inverse colors
- CI GitHub Actions: Validasi build otomatis
