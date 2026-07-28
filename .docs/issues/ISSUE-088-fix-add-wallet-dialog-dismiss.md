# ISSUE-088: Fix Add Wallet Dialog Dismiss & Close Bugs

**Status:** Done
**Priority:** High (Bug)
**Related:** ISSUE-070 (Delete Wallet Confirmation)

## Description
Dialog "Add New Wallet" (ModalBottomSheet) memiliki tiga bug terkait penutupan dialog:

1. **Tombol Cancel tidak berfungsi**: `onClick` pada tombol Cancel berisi blok kosong `{ }`, sehingga menekan Cancel tidak menutup dialog.
2. **Dialog tidak otomatis tertutup setelah Save**: Setelah `viewModel.addWallet(...)` dipanggil, state `showAddDialog` tidak di-set ke `false`, sehingga dialog tetap terbuka meski wallet sudah berhasil ditambahkan.
3. **Aplikasi freeze saat dismiss manual**: `onDismissRequest` pada `ModalBottomSheet` berisi blok kosong `{ }`. Ketika user mencoba menutup bottom sheet dengan gesture swipe-down atau tap di luar area, Material3 menganggap dismiss berhasil namun state `showAddDialog` tetap `true`, menyebabkan UI menjadi tidak responsif (freeze).

## Acceptance Criteria
- [x] Tombol "Cancel" pada dialog Add Wallet menutup dialog dengan benar.
- [x] Setelah berhasil menambahkan wallet (tekan Save), dialog otomatis tertutup.
- [x] User dapat menutup dialog dengan gesture swipe-down atau tap di luar area tanpa menyebabkan freeze.

## Root Cause
Semua handler penutupan (`onDismissRequest`, Cancel `onClick`) dibiarkan kosong (`{ }`), dan setelah aksi Save tidak ada kode untuk menutup dialog. Ini menyebabkan state `showAddDialog` tetap `true` bahkan ketika ModalBottomSheet secara visual sudah tertutup, mengakibatkan inkonsistensi state yang membekukan UI.

## Changes Made
### `WalletListScreen.kt`
- `onDismissRequest`: Diubah dari `{ }` menjadi `{ showAddDialog = false }`.
- Cancel button `onClick`: Diubah dari `{ }` menjadi `{ showAddDialog = false }`.
- Save button `onClick`: Ditambahkan `showAddDialog = false` setelah `viewModel.addWallet(...)`.
