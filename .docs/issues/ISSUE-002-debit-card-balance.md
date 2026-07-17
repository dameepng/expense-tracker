# ISSUE-002: Redesign BalanceCard menjadi Debit Card Style

**Priority:** 🔴 High  
**Type:** UI Redesign  
**Sprint:** Sprint 1  
**Estimated Effort:** Medium  

---

## Deskripsi

Mengubah tampilan `BalanceCard` dari gradient card sederhana menjadi **debit card style** seperti pada referensi gambar. Card harus terlihat seperti kartu debit fisik dengan elemen:
- Background dark/gradient yang premium
- Nominal balance besar di kiri atas
- Label "Balance" di bawah nominal
- Progress bar / indicator kecil (opsional, menunjukkan budget usage)
- Card number dots pattern (aesthetic, bukan data real)
- Card brand logo placeholder di kanan bawah
- Menu dots (⋮) di kanan atas

## Kondisi Saat Ini

- [BalanceCard](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeScreen.kt#L137-L219) menggunakan orange-pink gradient dengan centered layout
- Menampilkan: period label pill, amount, "Balance" text, dan add button (circular)
- Add button akan dipindah karena ada NavigationBar (ISSUE-001)

## Perubahan yang Dibutuhkan

### Prinsip: Clean Architecture · Clean Code · KISS · YAGNI

### Data Layer
- Tidak ada perubahan. YAGNI — card ini hanya UI visual, data `totalAmount` sudah tersedia.

### UI Layer

#### [MODIFY] [HomeScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeScreen.kt) — `BalanceCard` composable

**Signature baru (simplified):**
```kotlin
@Composable
fun BalanceCard(
    totalBalance: Long,        // Total balance (income - expense)
    modifier: Modifier = Modifier
)
```

**Visual specs (mengikuti referensi gambar):**
- **Shape:** `RoundedCornerShape(20.dp)` — M3 Large shape
- **Background:** Dark gradient (`Color(0xFF2D2D3A)` → `Color(0xFF1A1A2E)`) atau dark surface tone
- **Layout:** Left-aligned, bukan centered
- **Elemen:**
  1. **Amount** — `displayMedium`, bold, `Color.White`, left-aligned (misal: `Rp 5.480.000`)
  2. **"Balance"** label — `bodyMedium`, `Color.White.copy(alpha = 0.7f)`, di bawah amount
  3. **Card dots pattern** — `**** **** 402` decorative di bottom-left, `bodySmall`, white 60%
  4. **Menu icon** — `Icons.Filled.MoreHoriz` di top-right corner, white
  5. **Progress indicator** — Thin `LinearProgressIndicator` M3 di tengah card, menunjukkan spending ratio (opsional, bisa hardcode dulu)

**Hapus:**
- `periodLabel` pill badge → tidak relevan lagi di card style ini
- `onAddExpense` button → sudah ada di NavigationBar (ISSUE-001)

#### [MODIFY] [HomeScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeScreen.kt) — `HomeScreen` composable
- Update pemanggilan `BalanceCard` sesuai signature baru
- Hapus parameter yang sudah tidak dipakai

#### [MODIFY] [HomeScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeScreen.kt) — Preview
- Update `BalanceCardPreview` dan `HomeScreenPreview_withData` sesuai signature baru

## Acceptance Criteria

- [ ] BalanceCard terlihat seperti debit card fisik (dark, premium feel)
- [ ] Amount ditampilkan besar di kiri atas dengan format currency
- [ ] Card memiliki decorative card number pattern
- [ ] Tidak ada FAB/Add button di dalam card
- [ ] Card responsive — tidak overflow pada berbagai screen size
- [ ] Preview composable berjalan tanpa error

## Referensi Visual

Referensi gambar yang di-upload user:
- Dark rounded card
- Amount di kiri atas, besar & bold
- "Balance" label di bawah amount
- `**** **** 402` pattern di bottom
- Mastercard-style circle logo di bottom-right (gunakan decorative circles saja)
