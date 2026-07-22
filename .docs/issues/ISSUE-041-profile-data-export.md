# ISSUE-041: Profile - Implement Data Export to CSV

## Deskripsi
Menghidupkan fitur "Export Data Transaksi" pada grup Keamanan & Data. Fitur ini akan menarik seluruh data transaksi dari Room Database dan mengubahnya menjadi file berformat CSV (.csv) agar bisa dibuka pengguna melalui Excel atau disebarkan (share).

## Acceptance Criteria
- [ ] Membuat fungsi di *Repository/ViewModel* untuk mengambil seluruh daftar transaksi (`Expense` dan `Income`) secara lengkap.
- [ ] Membuat fungsi *helper* pencetak CSV yang memetakan kolom: Tanggal, Kategori, Wallet, Tipe (Pemasukan/Pengeluaran), Catatan, dan Nominal.
- [ ] Menyimpan file CSV ke penyimpanan perangkat (menggunakan `MediaStore` atau `Context.getExternalFilesDir`).
- [ ] Menampilkan notifikasi Toast/Snackbar saat export berhasil dan memunculkan *Intent Share* agar pengguna bisa langsung membagikan file tersebut (misal: via WhatsApp/Email).

## Skills
- File I/O di Android.
- Android Intent (ACTION_SEND).
- Room Database Querying.
