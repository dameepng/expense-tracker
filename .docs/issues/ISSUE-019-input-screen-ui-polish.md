# ISSUE-019: Input Screen UI Polish

## Deskripsi
Dua perbaikan UI pada `InputScreen` yang meningkatkan keterbacaan dan kemudahan interaksi:
1. **Satu segmented button 3-pilihan** menggantikan dua segmented button terpisah — menyatukan tipe transaksi dan mode input dalam satu komponen yang lebih ringkas.
2. **Seluruh form dapat di-scroll** sehingga kategori tidak terpotong dan semua field bisa diakses tanpa terhalangi tombol Simpan.

## User Story
> Sebagai user, saya ingin memilih tipe transaksi dan mode dengan satu segmented button, serta bisa scroll ke bawah untuk melihat semua kategori tanpa ada yang terpotong.

## Acceptance Criteria

### Segmented Button Tunggal 3-Pilihan
- [ ] `TransactionTypeToggle` dan `InputModeToggle` dihapus/digabung menjadi satu `InputTypeSegmentedButton`
- [ ] Tiga pilihan dalam satu `SingleChoiceSegmentedButtonRow`: **Pemasukan**, **Pengeluaran**, **Bill Reminder**
- [ ] Memilih **Pemasukan** → mode `TRANSACTION` + type `INCOME`, form standar (amount, description, wallet, kategori)
- [ ] Memilih **Pengeluaran** → mode `TRANSACTION` + type `EXPENSE`, form standar
- [ ] Memilih **Bill Reminder** → mode `BILL_REMINDER` + type `EXPENSE`, form tagihan (nama, nominal, dueDay, wallet, kategori)
- [ ] Warna aktif masing-masing pilihan berbeda: Pemasukan (hijau), Pengeluaran (merah/error), Bill Reminder (secondary)
- [ ] Header `InputHeader` menyesuaikan title berdasarkan pilihan aktif (bukan hanya berdasarkan `transactionType`)

### Form Scrollable
- [ ] Seluruh konten `InputScreen` (kecuali tombol Simpan) dibungkus `verticalScroll`
- [ ] Tombol Simpan **tetap fixed di bagian bawah** (tidak ikut scroll)
- [ ] `CategoryGrid` tidak lagi menggunakan `Modifier.weight(1f)` — diganti dengan `wrapContentHeight` agar semua item kategori tampil utuh
- [ ] `LazyVerticalGrid` pada `CategoryGrid` diganti dengan `FlowRow` atau grid non-lazy karena item-nya tidak pernah banyak dan sudah ada parent scrollable
- [ ] Tidak ada double-scroll conflict (nested scroll)

### Testing & CI
- [ ] Unit test: `InputViewModel` state saat memilih setiap 3 opsi segmented button
- [ ] CI GitHub Actions pipeline hijau

## Technical Details

### InputMode Enum — Tidak Berubah
```kotlin
enum class InputMode { TRANSACTION, BILL_REMINDER }
```

### ViewModel Changes
- `onTransactionTypeChange()` dan `onInputModeChange()` digabung menjadi satu fungsi:
  ```kotlin
  fun onInputTypeSelected(option: InputTypeOption) { ... }
  ```
- `InputTypeOption` enum baru:
  ```kotlin
  enum class InputTypeOption { INCOME, EXPENSE, BILL_REMINDER }
  ```
- Internally, `INCOME` → `InputMode.TRANSACTION + TransactionType.INCOME`
- Internally, `EXPENSE` → `InputMode.TRANSACTION + TransactionType.EXPENSE`  
- Internally, `BILL_REMINDER` → `InputMode.BILL_REMINDER + TransactionType.EXPENSE`

### Composable Structure (After)
```
InputScreen (Column)
├── InputHeader (title adaptif)
├── InputTypeSegmentedButton  ← Pemasukan / Pengeluaran / Bill Reminder (BARU, mengganti 2 toggle)
└── Scrollable Column (verticalScroll)
    ├── AmountInput
    ├── [Kondisional] TransactionFormContent atau BillReminderFormContent
    ├── WalletPicker
    └── CategoryGrid (non-lazy, wrapContent)
    
SaveButton (fixed bottom, di luar scroll)
```

### CategoryGrid — Non-Lazy
- Ganti `LazyVerticalGrid` → `FlowRow` (dari `androidx.compose.foundation.layout`) dengan `maxItemsInEachRow = 3`
- Ini aman karena jumlah kategori maksimal ~11 item dan tidak akan mengalami masalah performa

### Dependency
- Bergantung pada **ISSUE-018** (InputMode sudah tersedia)

## Skills
- Clean Architecture: ViewModel tetap sebagai single source of truth, UI hanya reaktif
- Clean Code: `InputTypeOption` enum yang ekspresif menggantikan dua boolean/enum terpisah
- KISS: Satu segmented button, satu fungsi pilih tipe — lebih simpel dari sebelumnya
- YAGNI: FlowRow cukup tanpa perlu LazyVerticalGrid untuk data kecil
- CI GitHub Actions: Validasi build + unit test otomatis
