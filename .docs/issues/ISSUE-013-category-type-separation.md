# ISSUE-013: Kategori per Tipe Transaksi

## Deskripsi
Memisahkan kategori antara Income dan Expense agar user tidak melihat kategori yang tidak relevan saat menambahkan transaksi. Saat ini semua kategori (Makanan, Transport, dll.) muncul di kedua tipe transaksi.

## Acceptance Criteria
- [ ] Entity `Category` memiliki kolom `type` (INCOME/EXPENSE/BOTH)
- [ ] Database migration v5→v6 berjalan tanpa kehilangan data
- [ ] Seed kategori Income baru: Gaji, Freelance, Bonus, Transfer Masuk, Lainnya
- [ ] Seed kategori Expense default tetap ada + "Lainnya" jadi type BOTH
- [ ] `CategoryGrid` di `InputScreen` memfilter kategori berdasarkan `TransactionType` aktif
- [ ] Unit test: DAO filter by type
- [ ] CI GitHub Actions pipeline hijau

## Technical Details
### Data Layer
- Tambah kolom `type TEXT NOT NULL DEFAULT 'EXPENSE'` pada tabel `categories`
- Migration: ALTER TABLE + INSERT kategori income baru
- `ExpenseDao.getCategoriesByType(type: String)` query baru

### UI Layer
- `InputViewModel.onTransactionTypeChange()` → refresh filtered categories
- `CategoryGrid` menerima list yang sudah difilter

## Skills
- Clean Architecture: Pemisahan data/domain/ui layer
- Clean Code: Naming convention, single responsibility
- KISS: Cukup tambah 1 kolom, filter di query
- YAGNI: Tidak perlu icon/color per kategori sekarang
- CI GitHub Actions: Validasi build + test otomatis
