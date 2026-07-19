# ISSUE-018: Segmented Button "Bill Reminder" di Add Transaction Screen

## Deskripsi
Menambahkan segmented button pada `InputScreen` agar user bisa beralih antara mode **Transaksi** dan mode **Bill Reminder** dalam satu halaman yang sama. Ini menghilangkan kebutuhan navigasi terpisah ke `ReminderFormScreen` untuk kasus penambahan reminder baru, sehingga flow terasa lebih natural dan cepat.

## User Story
> Sebagai user, saya ingin bisa langsung menandai sebuah pengeluaran sebagai tagihan rutin (bill reminder) dari halaman Add Transaction, tanpa harus berpindah ke menu lain.

## Acceptance Criteria
- [ ] `InputScreen` memiliki segmented button dengan dua opsi: **Transaksi** dan **Bill Reminder**
- [ ] Saat mode **Transaksi** aktif → form tetap seperti semula (amount, category, wallet, note, date)
- [ ] Saat mode **Bill Reminder** aktif → form berubah menampilkan field: nama tagihan, nominal, tanggal jatuh tempo (dueDay 1–31), pilih wallet
- [ ] Field kategori pada mode Bill Reminder tetap tersedia (opsional, untuk klasifikasi)
- [ ] Tombol simpan di mode Bill Reminder → menyimpan ke `BillReminder` entity (ISSUE-014), bukan ke tabel `transactions`
- [ ] Segmented button hanya muncul saat tipe transaksi **Expense** aktif (bill reminder tidak relevan untuk income)
- [ ] Animasi/transisi halus saat beralih antar mode (crossfade atau slide)
- [ ] Unit test: `InputViewModel` menangani dua mode state dengan benar
- [ ] CI GitHub Actions pipeline hijau

## Technical Details

### UI Layer
- Tambah `InputMode` enum: `TRANSACTION`, `BILL_REMINDER`
- `InputViewModel` memiliki `inputMode: StateFlow<InputMode>`
- `InputViewModel.onInputModeChange(mode: InputMode)` → reset form state yang tidak relevan
- Segmented button dirender sebagai `SingleChoiceSegmentedButtonRow` (Material 3)
- Segmented button hanya visible ketika `transactionType == EXPENSE`

### Composable Structure
```
InputScreen
├── TransactionTypeToggle (Income / Expense)               ← sudah ada
├── InputModeSegmentedButton (Transaksi / Bill Reminder)   ← BARU, visible jika Expense
├── TransactionFormContent   (amount, category, wallet, note, date)
└── BillReminderFormContent  (nama, nominal, dueDay, wallet, category)
```

### Data Flow
- Mode `BILL_REMINDER` → `onSave()` memanggil `BillReminderRepository.insertReminder()`
- Mode `TRANSACTION` → tetap memanggil `TransactionRepository.insertTransaction()`
- `InputViewModel` menjadi single source of truth untuk kedua mode

### Dependency
- Bergantung pada **ISSUE-014** (BillReminder entity & repository sudah tersedia)
- Bergantung pada **ISSUE-013** (category filter by type sudah berjalan)

## Skills
- Clean Architecture: ViewModel sebagai mediator dua mode form, tidak ada logika di Composable
- Clean Code: `InputMode` enum menggantikan boolean flag, naming yang ekspresif
- KISS: Satu screen, dua mode — tidak perlu screen baru atau bottom sheet tambahan
- YAGNI: Tidak menambah fitur edit reminder dari screen ini; cukup insert saja
- CI GitHub Actions: Validasi build + unit test otomatis
