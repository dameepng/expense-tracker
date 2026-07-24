# ISSUE-056: Implement Profile Settings: Currency & Language Switcher

## Description
- Menghubungkan fungsionalitas dropdown **Currency** (IDR, USD, EUR) di `ProfileScreen` dengan DataStore agar format mata uang aplikasi langsung berubah (terintegrasi dengan `CurrencyFormatter`).
- Menghubungkan fungsionalitas dropdown **Language** (Indonesia, English) di `ProfileScreen` dengan DataStore.
- **[Bug Fix / Refactor]** Implementasi *best practice* lokalisasi menggunakan *Activity Recreate* atau `AppCompatDelegate.setApplicationLocales()` agar perubahan bahasa dari dropdown langsung diterapkan ke layar saat itu juga tanpa perlu navigasi.

## Acceptance Criteria
- [ ] Dropdown currency di Profile mengubah nilai currency di `DataStore`.
- [ ] Mengubah currency langsung tercermin di seluruh aplikasi berkat `CurrencyFormatter`.
- [ ] Dropdown language di Profile mengubah nilai language di `DataStore`.
- [ ] Mengubah language di dropdown langsung me-restart / me-recompose UI sehingga string lokal langsung berubah (tidak perlu pindah halaman manual).
- [ ] Opsi yang dipilih di dropdown sesuai dengan state aktual dari `DataStore`.
