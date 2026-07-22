# ISSUE-032: Amount Input UI Polish & Dynamic Font Resizing

## Deskripsi
Halaman Add Transaksi (`InputScreen`) saat ini menampilkan tanda `+` / `-` di depan prefix `Rp` dengan warna hijau/merah. Selain itu, ketika pengguna memasukkan angka yang besar (misal 10 juta, 100 juta, hingga 1 Miliar), ukuran teks angka tetap besar sehingga berisiko terpotong atau wrap ke baris kedua.

Perubahan ini akan merapikan UI input nominal:
- Menghapus tanda `+` / `-` sehingga hanya menampilkan `Rp`
- Mengubah warna teks nominal menjadi **putih bersih** (`Color.White`) untuk tampilan yang lebih clean & modern
- Menerapkan **Dynamic Font Resizing** berbasis panjang digit agar teks otomatis menyesuaikan ukuran (responsive) secara mulus dari angka ribuan hingga Miliaran tanpa terpotong

## User Story
> Sebagai pengguna, saya ingin tampilan input nominal transaksi terlihat bersih, elegan dengan warna putih, serta teks angka otomatis mengecil secara mulus saat nominal bertambah besar, agar layar input tetap rapi dan tidak terpotong.

## Acceptance Criteria
- [ ] Tanda `+` dan `-` di depan `Rp` dihapus (hanya menampilkan `Rp 10.000.000`)
- [ ] Warna teks nominal angka diubah menjadi warna putih bersih (`Color.White`)
- [ ] Teks nominal angka responsif (font size menyesuaikan otomatis) mulai dari nominal kecil hingga 1 Miliar+
- [ ] Single line input tanpa wrap atau terpotong di tepi layar

## Technical Details

### Fix
**[MODIFY]** [InputScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/input/InputScreen.kt)
- Update `AmountInput` composable:
  - Set `prefix = "Rp "` (menghapus `+` / `-`)
  - Set `textColor = Color.White` (jika terisi) atau warna placeholder saat kosong
  - Hitung `fontSize` secara dinamis berdasarkan `displayText.length` (misal 42.sp -> 34.sp -> 28.sp -> 22.sp) dengan `maxLines = 1`

## Skills
- Dynamic Typography & Responsive Layout: Penggunaan font scaling berbasis panjang string di Jetpack Compose
- Material 3 Visual Polish: Penerapan palet warna monokrom clean & high-contrast
