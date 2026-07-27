# ISSUE-071: Fix Launcher Icon Resource Resolution

## Deskripsi
Pada saat menjalankan **Inspect Code** di Android Studio, ditemukan error pada file `ic_launcher.xml` dan `ic_launcher_round.xml` di folder `mipmap-anydpi-v26`:

```
Cannot resolve symbol '@mipmap/ic_launcher_foreground'
```

### Penyebab
File `ic_launcher.xml` dan `ic_launcher_round.xml` mereferensikan `@mipmap/ic_launcher_foreground` untuk `foreground` dan `monochrome`, namun file foreground yang tersedia berupa `.webp` di density-specific folder (`mipmap-hdpi`, `mipmap-mdpi`, dll). Hal ini membuat Android Resources Validation tidak bisa melakukan resolve secara statis di folder `mipmap-anydpi-v26`.

Selain itu, terdapat juga file `@drawable/ic_launcher_foreground.xml` (versi vector) yang bisa dimanfaatkan sebagai gantinya, karena vector drawable lebih efisien dan tidak memerlukan file terpisah per density.

### Solusi yang Diharapkan
Ubah referensi pada `ic_launcher.xml` dan `ic_launcher_round.xml` dari `@mipmap/ic_launcher_foreground` menjadi `@drawable/ic_launcher_foreground` agar menggunakan versi vector drawable yang sudah ada, sehingga menghilangkan error validasi dan mengurangi duplikasi resource.

Setelah itu, file-file `.webp` di density-specific folder (`mipmap-hdpi/ic_launcher_foreground.webp`, dll) bisa dihapus karena sudah digantikan oleh vector drawable.

## Acceptance Criteria
- [ ] `ic_launcher.xml` mereferensikan `@drawable/ic_launcher_foreground` untuk foreground dan monochrome.
- [ ] `ic_launcher_round.xml` mereferensikan `@drawable/ic_launcher_foreground` untuk foreground dan monochrome.
- [ ] File `.webp` foreground di `mipmap-*dpi` dihapus (opsional, bisa di-issue terpisah jika ada concern backward compat).
- [ ] Error "Cannot resolve symbol" pada Inspect Code hilang.
- [ ] Build & lint tetap hijau.

## Technical Details
### File yang diubah
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`

### File yang bisa dihapus (opsional)
- `app/src/main/res/mipmap-hdpi/ic_launcher_foreground.webp`
- `app/src/main/res/mipmap-mdpi/ic_launcher_foreground.webp`
- `app/src/main/res/mipmap-xhdpi/ic_launcher_foreground.webp`
- `app/src/main/res/mipmap-xxhdpi/ic_launcher_foreground.webp`
- `app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.webp`

## Skills
- KISS: Perubahan minimal — hanya ubah referensi resource.
- DRY: Eliminasi duplikasi antara vector drawable dan bitmap webp.
- Clean Code: Gunakan satu sumber kebenaran (vector drawable) untuk ikon foreground.
