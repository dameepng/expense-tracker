# ISSUE-005: Update Transaction List Item untuk Support Income & Expense

**Priority:** 🟡 Medium  
**Type:** UI Enhancement  
**Sprint:** Sprint 1  
**Depends On:** ISSUE-003 (Income Feature)  
**Estimated Effort:** Small  

---

## Deskripsi

Update tampilan `ExpenseListItem` agar bisa membedakan antara transaksi **income** dan **expense** secara visual. Saat ini semua item ditampilkan sebagai expense (merah, dengan prefix "-"). Setelah perubahan, income ditampilkan dengan warna hijau dan prefix "+".

## Kondisi Saat Ini

- [ExpenseListItem](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeScreen.kt#L254-L316) hanya menampilkan expense style
- Trailing content selalu menampilkan `"-" + CurrencyFormatter.format(amount)` dengan `colorScheme.error`
- Tidak ada pembeda visual antara income dan expense

## Perubahan yang Dibutuhkan

### Prinsip: Clean Architecture · Clean Code · KISS · YAGNI

### Data Layer
- Tidak ada perubahan tambahan (sudah dihandle di ISSUE-003)

### UI Layer

#### [MODIFY] [HomeScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeScreen.kt) — `ExpenseListItem`

**Rename** menjadi `TransactionListItem` — Clean Code, nama harus mencerminkan fungsi.

**Parameter baru:**
```kotlin
@Composable
fun TransactionListItem(
    transaction: ExpenseWithCategory,  // Sudah punya field `type`
    modifier: Modifier = Modifier
)
```

**Perubahan visual berdasarkan type:**

| Aspek | Income | Expense |
|-------|--------|---------|
| **Amount prefix** | `+` | `-` |
| **Amount color** | `Color(0xFF2E8B57)` (green) | `MaterialTheme.colorScheme.error` (red) |
| **Leading icon color** | Green tint circle | Category color (existing) |
| **Icon** | `Icons.Filled.TrendingUp` atau sesuai kategori | Category dot (existing) |

**KISS:** Perubahan minimal — hanya conditional di `trailingContent` dan `leadingContent` berdasarkan `transaction.type`.

#### [MODIFY] [HomeScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeScreen.kt) — `HomeScreen`
- Ganti semua pemanggilan `ExpenseListItem` → `TransactionListItem`

#### [MODIFY] [HomeScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeScreen.kt) — SwipeToDismiss
- Update SwipeToDismiss handlers — `deleteExpense` dan `undoDeleteExpense` harus work untuk kedua type
- Snackbar message: "Transaksi dihapus" (generic, tidak perlu dibedakan — KISS)

#### [MODIFY] Preview
- Update `ExpenseListItemPreview` menjadi `TransactionListItemPreview`
- Tambah preview untuk income item

## Acceptance Criteria

- [ ] Income items tampil dengan `+` prefix dan warna hijau
- [ ] Expense items tetap tampil dengan `-` prefix dan warna merah
- [ ] Swipe-to-dismiss tetap berfungsi untuk kedua type
- [ ] Naming konsisten: `TransactionListItem` di seluruh codebase
- [ ] Preview menampilkan kedua jenis (income & expense)

## Referensi Visual

Dari gambar referensi, transaction items tampil seperti:
```
┌──────────────────────────────────┐
│ (icon)  Dribbble Pro    -$145.00 │  ← Expense (merah)
│         18 Sep, 2021             │
├──────────────────────────────────┤
│ (icon)  Salary          +$5000   │  ← Income (hijau)
│         01 Sep, 2021             │
└──────────────────────────────────┘
```
