# ISSUE-006: Update HomeScreen Layout & Cleanup

**Priority:** 🟡 Medium  
**Type:** UI Cleanup / Refactor  
**Sprint:** Sprint 1  
**Depends On:** ISSUE-001, ISSUE-002, ISSUE-003, ISSUE-004, ISSUE-005  
**Estimated Effort:** Small  

---

## Deskripsi

Issue terakhir sebagai **integrasi dan cleanup** setelah semua issue sebelumnya selesai. Memastikan semua komponen baru terintegrasi dengan baik di HomeScreen, menghapus kode yang tidak terpakai, dan memverifikasi seluruh flow.

## Perubahan yang Dibutuhkan

### Prinsip: Clean Architecture · Clean Code · KISS · YAGNI

### UI Layer

#### [MODIFY] [HomeScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeScreen.kt)

**Layout baru HomeScreen (top to bottom):**
1. `HomeHeader` — (tetap, minor adjustment)
2. `BalanceCard` — Debit card style (ISSUE-002)
3. `IncomeExpenseSummary` — Summary chips (ISSUE-004)
4. "Transactions" section header + "View all" button
5. `TransactionListItem` list — Mixed income & expense (ISSUE-005)

**Cleanup tasks:**
- Hapus import yang tidak terpakai (SegmentedButton, dll)
- Hapus composable `FilterTabs` jika masih ada
- Hapus `HomeViewModelFactory` parameter yang tidak diperlukan — YAGNI
- Update preview composables agar reflect UI baru
- Pastikan `Scaffold` di HomeScreen tidak conflict dengan `Scaffold` di MainActivity (karena NavigationBar sekarang ada di MainActivity)

#### [MODIFY] [HomeHeader.kt composable in HomeScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeScreen.kt#L77-L133)
- Remove `onNavigateToSummary` dari `HomeHeader` — navigasi ke Summary sekarang via NavigationBar
- Summary icon di header bisa dihapus atau diganti dengan notification icon — KISS
- Atau tetap ada sebagai shortcut — tidak wajib dihapus

#### [MODIFY] [HomeViewModel.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeViewModel.kt)
- Hapus `onFilterSelected()` jika SegmentedButton sudah dihapus
- Set default filter ke `MONTH` (menampilkan data bulan ini)
- Cleanup unused imports

#### [MODIFY] [HomeUiState.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeUiState.kt)
- Hapus `filter` field jika tidak lagi exposed di UI
- Atau simpan secara internal untuk data query — keputusan berdasarkan kebutuhan

#### [VERIFY] Build & Test
- `./gradlew assembleDebug` harus berhasil tanpa error
- Semua preview composable harus render tanpa crash
- Navigasi: Home ↔ Summary ↔ Input via NavigationBar lancar
- Data income & expense ditampilkan dengan benar

## Acceptance Criteria

- [ ] Tidak ada dead code atau unused imports
- [ ] Layout HomeScreen sesuai gambar referensi (card → summary → transactions)
- [ ] Build berhasil tanpa warning/error terkait perubahan ini
- [ ] Semua preview composable berfungsi
- [ ] NavigationBar di bottom konsisten di semua screen
- [ ] UX flow smooth: input income, input expense, lihat summary, lihat balance

## Catatan

> **Clean Code:** Semua rename harus konsisten (Expense → Transaction di naming yang relevan). Jangan rename database table — hanya naming di Kotlin code.

> **YAGNI:** Jangan tambahkan fitur baru di issue ini. Ini murni integrasi dan cleanup.
