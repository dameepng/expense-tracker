# ISSUE-035: Clean UI for Wallet Credit Card

## Deskripsi
Pengguna ingin menghilangkan teks informasi Total Balance pada UI kartu kredit di halaman Wallet List, sehingga desain kartu terlihat jauh lebih bersih (clean) layaknya fisik kartu aslinya. Selain itu, proporsi dimensi kartu sedikit dikecilkan (dibuat lebih lebar secara visual / aspect ratio diubah) agar menyerupai rasio asli kartu kredit/debit.

## User Story
> Sebagai pengguna, saya ingin UI kartu di daftar wallet saya terlihat clean tanpa ada nominal saldo di atas kartu itu sendiri, dan saya ingin proporsi tingginya sedikit dikurangi agar kartu terlihat lebih proporsional seperti kartu asli.

## Acceptance Criteria
- [x] Menghilangkan teks "Total Balance" dan nominal `computedBalance` dari fungsi rendering `CreditCardItem`.
- [x] Menghapus unused parameter `computedBalance` di fungsi `CreditCardItem`.
- [x] Mengubah `aspectRatio` `CreditCardItem` dari `1.586f` ke `1.8f` (sehingga kartu lebih pipih / tingginya dikurangi secara visual).
- [x] Membersihkan unused imports (`NumberFormat`, `Locale`) di `WalletListScreen.kt`.

## Skills
- Jetpack Compose UI Adjustments: Modifikasi `aspectRatio` modifier.
- Compose Code Refactoring: Penghapusan parameter dan pembersihan UI layer.
