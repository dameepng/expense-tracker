# ISSUE-030: Direct Wallet Selection and Navigation to Home Screen

## Deskripsi
Saat ini, ketika user menekan kartu wallet (misal: BCA) pada halaman daftar wallet (`WalletListScreen`), aplikasi membuka halaman detail terpisah (`WalletDetailScreen` di bawah rute `wallet/{walletId}`). Halaman ini menampilkan ulang elemen yang hampir persis dengan Halaman Utama (Home), sehingga membingungkan flow navigasi pengguna.

Perubahan ini akan mengubah perilaku saat kartu wallet diklik: aplikasi akan langsung **menyimpan preferensi wallet yang dipilih ke DataStore** dan **mengarahkan pengguna ke Halaman Utama (`/home`)**, sehingga Halaman Utama otomatis menampilkan data wallet yang baru saja dipilih.

## User Story
> Sebagai pengguna, ketika saya menekan kartu wallet di daftar wallet, saya ingin langsung dialihkan ke Halaman Utama dengan wallet tersebut sudah otomatis terpilih, agar navigasi terasa langsung, cepat, dan tidak berbelit-belit.

## Acceptance Criteria
- [ ] Menekan kartu wallet pada `WalletListScreen` menyimpan `walletId` ke DataStore via `UserPreferencesRepository`
- [ ] Pengguna langsung dialihkan ke Halaman Utama (`NavRoutes.HOME` / Tab Home)
- [ ] Halaman Utama otomatis meng-update data saldo, summary, dan daftar transaksi sesuai wallet yang diklik
- [ ] Menghapus halaman & rute `WalletDetailScreen` yang redundan agar arsitektur aplikasi lebih bersih

## Technical Details

### Root Cause
Di [MainActivity.kt](file:///c:/dame-project/Android/expense_tracker/MainActivity.kt), callback `onNavigateToWalletDetail` di rute `NavRoutes.WALLET` saat ini memanggil `navController.navigate(NavRoutes.walletDetailRoute(id))`. Hal ini membuka `WalletDetailScreen` terpisah bukannya meng-update preferensi wallet dan kembali ke Home.

### Fix
1. **[MODIFY]** [WalletListScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/wallet/WalletListScreen.kt)
   - Tambahkan callback `onSelectWallet: (Long) -> Unit` pada `WalletListScreen`
   - Saat `CreditCardItem` diklik, panggil `onSelectWallet(wallet.id)`

2. **[MODIFY]** [MainActivity.kt](file:///c:/dame-project/Android/expense_tracker/MainActivity.kt)
   - Pada rute `NavRoutes.WALLET`, atur handler `onSelectWallet`: simpan preferensi wallet & navigasi ke `NavRoutes.HOME` (dengan `popUpTo(NavRoutes.HOME) { saveState = true }` dan `launchSingleTop = true`)
   - Hapus rute `NavRoutes.WALLET_DETAIL` dan berkas `WalletDetailScreen.kt` & `WalletDetailViewModel.kt`

3. **[MODIFY]** [NavRoutes.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/navigation/NavRoutes.kt)
   - Hapus rute `WALLET_DETAIL`

## Skills
- Single Source of Truth (SSOT) Architecture: Menggunakan DataStore untuk mengontrol wallet mana yang aktif di seluruh aplikasi
- Jetpack Navigation & Tab State Management: Navigasi mulus antar tab dengan state preservation (`launchSingleTop`, `popUpTo`)
- UX & Flow Optimization: Mengeliminasi screen redundan untuk alur pengguna yang lebih simpel dan cepat
