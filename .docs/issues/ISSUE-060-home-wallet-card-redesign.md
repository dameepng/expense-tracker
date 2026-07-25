# ISSUE-060: Home Wallet Card Redesign (Dynamic Credit Card Style)

## Deskripsi
Mengubah desain Card Wallet pada halaman Home agar sesuai dengan desain Credit Card yang ada pada halaman Wallet. Warna dan desain (gradient) pada Home screen akan dinamis mengikuti wallet yang sedang dipilih (contoh: jika memilih BCA, maka card berwarna biru; jika BSI, berwarna hijau). 
Selain itu, layout card di Home screen akan disesuaikan di mana **Total Balance diletakkan tepat di atas nomor kartu**.

## Acceptance Criteria
- [ ] Wallet Card di `HomeScreen` menggunakan gradient/warna yang sama dengan desain di `WalletScreen` berdasarkan wallet yang sedang aktif.
- [ ] Layout Wallet Card di `HomeScreen` menampilkan:
  - Bank/Wallet Name (kiri atas)
  - Menu icon 3 titik (kanan atas)
  - Total Balance (tengah, tepat di atas nomor kartu)
  - Masked Card Number (kiri bawah)
  - Card Provider Logo (Mastercard/Visa/dll) atau Chip (kanan/kiri bawah sesuai kecocokan layout).
- [ ] Saat user mengganti wallet aktif, desain card di Home langsung merender warna/gradient wallet tersebut.

## Technical Details
- Modifikasi komponen `WalletCard` atau `SummaryCard` di `HomeScreen.kt`.
- Reusable komponen atau *extract* logic warna/gradient dari `WalletScreen` agar bisa dipakai di `HomeScreen` (KISS & DRY principle).
- Sesuaikan layout Compose (Column/Row/ConstraintLayout) agar Balance berada di atas nomor kartu.

## Skills
- Clean Architecture: Pemisahan data/domain/ui layer
- Clean Code: Naming convention, single responsibility
- KISS: Cukup tambah 1 kolom, filter di query
- YAGNI: Tidak perlu icon/color per kategori sekarang
- CI GitHub Actions: Validasi build + test otomatis
