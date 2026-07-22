# ISSUE-040: Profile - Implement Preferences with DataStore

## Deskripsi
Menghidupkan fungsionalitas grup "Preferensi" di halaman Profile menggunakan Jetpack DataStore (Preferences DataStore) untuk menyimpan pengaturan pengguna secara persisten. Pengaturan ini akan diaplikasikan secara global di seluruh aplikasi.

## Acceptance Criteria
- [ ] Membuat atau memperbarui `UserPreferencesRepository` berbasis DataStore.
- [ ] Menyimpan dan membaca state **Tema Aplikasi** (Dark Mode, Light Mode, System Default) dan menerapkannya di `AppTheme`.
- [ ] Menyimpan dan membaca state **Mata Uang Utama** (misal: IDR / USD) dan menerapkannya pada fungsi formatter uang di seluruh aplikasi.
- [ ] Menyimpan dan membaca state **Bahasa** (Indonesia / English) dan mengaitkannya dengan Locale configuration.
- [ ] Menyambungkan state dari ViewModel ke UI `SettingsItem` (saat item diklik, muncul dialog/bottom sheet untuk memilih opsi).

## Skills
- Jetpack DataStore Preferences.
- Compose State Hoisting & Dynamic Theming.
- Kotlin Flow.
