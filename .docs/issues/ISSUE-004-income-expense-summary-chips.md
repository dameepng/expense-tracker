# ISSUE-004: Tambahkan Income/Expense Summary Chips di HomeScreen

**Priority:** 🟡 Medium  
**Type:** UI Enhancement  
**Sprint:** Sprint 1  
**Depends On:** ISSUE-003 (Income Feature)  
**Estimated Effort:** Small  

---

## Deskripsi

Menambahkan dua summary chips/cards di bawah BalanceCard yang menampilkan ringkasan **Income** dan **Expense** secara visual. Mengacu pada gambar referensi dimana terdapat dua box kecil side-by-side:
- **Income** — icon hijau, persentase/nominal income
- **Expense** — icon oranye/kuning, persentase/nominal expense

## Kondisi Saat Ini

- Tidak ada ringkasan income/expense terpisah di HomeScreen
- Langsung dari BalanceCard ke transaction list

## Perubahan yang Dibutuhkan

### Prinsip: Clean Architecture · Clean Code · KISS · YAGNI

### Data Layer
- Tidak ada perubahan — data `totalIncome` dan `totalAmount` (expense) sudah tersedia dari ISSUE-003

### UI Layer

#### [MODIFY] [HomeScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeScreen.kt)

**Tambahkan composable baru:**
```kotlin
@Composable
fun IncomExpenseSummary(
    totalIncome: Long,
    totalExpense: Long,
    modifier: Modifier = Modifier
)
```

**Visual specs:**
- **Layout:** `Row` dengan 2 `Surface` (M3) yang sama lebar, `Arrangement.spacedBy(12.dp)`
- **Income Card:**
  - Leading icon: circle hijau dengan arrow up (↑) atau `Icons.Filled.TrendingUp`
  - Text: nominal atau persentase income
  - Label: "Income"
  - Container: `MaterialTheme.colorScheme.surface` dengan border/outline halus
- **Expense Card:**
  - Leading icon: circle oranye/kuning dengan arrow down (↓) atau `Icons.Filled.TrendingDown`
  - Text: nominal atau persentase expense
  - Label: "Expense"
  - Container: sama seperti Income Card
- **Shape:** `RoundedCornerShape(16.dp)` — M3 Large
- Ditempatkan antara BalanceCard dan "Transactions" header

#### [MODIFY] [HomeScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeScreen.kt) — `HomeScreen`
- Sisipkan `IncomeExpenseSummary` composable di antara `BalanceCard` dan section "Transactions"

## Acceptance Criteria

- [ ] Dua summary cards tampil side-by-side di bawah BalanceCard
- [ ] Income card menampilkan total income dengan warna hijau
- [ ] Expense card menampilkan total expense dengan warna oranye/merah
- [ ] Visual sesuai referensi gambar (rounded, icon + text + label)
- [ ] Responsive pada berbagai screen width

## Referensi Visual

Dari gambar referensi:
```
┌─────────────┐  ┌─────────────┐
│ 🟢  +24%    │  │ 🟡  -42%    │
│   Income    │  │   Expense   │
└─────────────┘  └─────────────┘
```
