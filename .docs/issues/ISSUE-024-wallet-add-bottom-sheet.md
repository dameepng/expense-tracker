# ISSUE-024: Convert Add Wallet Form to ModalBottomSheet

## Deskripsi
Mengubah form "Add Wallet" yang saat ini menggunakan inline dialog/form menjadi `ModalBottomSheet` sesuai best practice Material Design 3. Bottom sheet menjaga user tetap dalam konteks halaman Wallet tanpa membuka layar baru.

## User Story
> Sebagai user, saya ingin menambah wallet baru melalui bottom sheet yang muncul dari bawah, agar pengalaman lebih cepat dan saya tetap melihat daftar wallet di belakangnya.

## Acceptance Criteria
- [ ] Klik FAB (+) di halaman Wallet membuka `ModalBottomSheet` dari bawah
- [ ] Form berisi field: Nama Wallet
- [ ] Tombol "Simpan" menyimpan wallet baru dan menutup bottom sheet
- [ ] Gesture drag-down menutup bottom sheet (default behavior)
- [ ] Validasi: nama tidak boleh kosong
- [ ] Setelah simpan, daftar wallet otomatis ter-refresh

## Technical Details

### Kondisi Saat Ini
Di [WalletListScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/wallet/WalletListScreen.kt), FAB sudah ada (line 68-71) dan `showAddDialog` state sudah disiapkan. Form add wallet kemungkinan menggunakan dialog biasa atau inline composable.

### Fix
1. Ganti implementasi dialog/form dengan `ModalBottomSheet` dari Material 3
2. Gunakan `rememberModalBottomSheetState()` untuk kontrol state
3. Form input tetap sederhana: satu `OutlinedTextField` + satu `Button`
4. Panggil `viewModel.addWallet()` saat simpan

#### [MODIFY] [WalletListScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/wallet/WalletListScreen.kt)
- Import `ModalBottomSheet`, `rememberModalBottomSheetState`
- Ganti blok `if (showAddDialog)` dengan `ModalBottomSheet`
- Desain form dengan padding, corner radius, dan spacing M3

### Dependencies
- Tidak ada dependency tambahan — `material3` sudah include `ModalBottomSheet`.

## Skills
- M3 Components: ModalBottomSheet guidelines
- Clean Code: Separation form logic ke composable terpisah
- KISS: Satu field + satu tombol, tidak over-engineer
