# ISSUE-028: Remove Swipe-to-Delete on Wallet Card List

## Deskripsi
Fitur swipe-to-delete pada halaman daftar wallet (`WalletListScreen`) tidak cocok untuk tampilan kartu kredit/debit karena gesture swipe secara tidak sengaja dapat memicu penghapusan wallet saat pengguna menggeser daftar. Fitur swipe-to-delete perlu dihapus dari `WalletListScreen`.

## User Story
> Sebagai user, saya ingin dapat melihat dan memilih kartu wallet tanpa takut tidak sengaja terhapus karena gesture swipe.

## Acceptance Criteria
- [ ] Gesture swipe-to-delete pada card wallet di `WalletListScreen` dihapus
- [ ] Card tetap bisa di-tap untuk membuka halaman detail wallet
- [ ] Tampilan card kartu kredit terlihat bersih tanpa background merah swipe action saat di-swipe

## Technical Details

### Root Cause
Di [WalletListScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/wallet/WalletListScreen.kt), item wallet dibungkus oleh composable `SwipeToDismissBox`.

### Fix
**[MODIFY]** [WalletListScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/wallet/WalletListScreen.kt)
- Hapus wrapper `SwipeToDismissBox` dari `WalletListItem` (atau hapus `WalletListItem` jika hanya membungkus `CreditCardItem` dan langsung gunakan `CreditCardItem`).
- Hapus handler delete dari swipe gesture di `WalletListScreen`.

## Skills
- Jetpack Compose UI Refactoring: Simplifikasi composable hierarchy
- Material 3 Guidelines: Memastikan gesture interaksi intuitif dan mencegah tindakan destruktif tak sengaja
- Clean Code: Menghapus komponen & parameter berlebih yang tidak lagi digunakan
