# ISSUE-020: Fix Notification Button & Responsive Income/Expense Card di Home

## Deskripsi
Dua bug pada `HomeScreen` yang mengganggu pengalaman pengguna:
1. **Tombol notifikasi tidak berfungsi** — menekan ikon notifikasi (bell) di header tidak membuka halaman Bill Reminder karena parameter `onNavigateToReminder` tidak di-pass dari `MainActivity`.
2. **Card Income/Expense tidak responsive** — ketika nominal semakin besar (contoh: Rp10.600.000), teks meluap (overflow/wrap) sehingga layout pecah dan sulit dibaca.

## User Story
> Sebagai user, saya ingin menekan tombol notifikasi di halaman Home agar langsung masuk ke daftar Bill Reminder, dan saya ingin card Income/Expense tetap rapi dan mudah dibaca meskipun nominalnya sangat besar.

## Acceptance Criteria

### Bug 1: Tombol Notifikasi
- [ ] Menekan ikon bell di `HeaderSection` menavigasi ke `NavRoutes.REMINDER_LIST`
- [ ] Parameter `onNavigateToReminder` di-pass dari `ExpenseTrackerApp()` pada `MainActivity.kt` ke `HomeScreen`
- [ ] Badge jumlah reminder aktif tetap tampil dengan benar

### Bug 2: Card Income/Expense Responsive
- [ ] Nominal pada card Income dan Expense tidak pernah overflow atau wrap ke baris baru secara tidak rapi
- [ ] Gunakan `AutoSizeText` atau kombinasi `maxLines = 1` + font size adaptif agar teks menyesuaikan lebar card
- [ ] Pada nominal sangat besar (contoh: Rp999.999.999), card tetap proporsional dan teks tetap terbaca
- [ ] Icon lingkaran di sisi kiri card tidak tergeser atau terjepit oleh teks panjang
- [ ] Layout card tetap seimbang antara Income dan Expense (menggunakan `Modifier.weight(1f)`)

### Testing & CI
- [ ] Unit test: navigasi `onNavigateToReminder` terpanggil saat tombol diklik
- [ ] UI manual: verifikasi card responsive dengan berbagai nominal (Rp0, Rp1.000, Rp10.600.000, Rp999.999.999)
- [ ] CI GitHub Actions pipeline hijau

## Technical Details

### Bug 1: Root Cause & Fix

**Root Cause**: Di `MainActivity.kt` line 112–118, `HomeScreen` dipanggil tanpa parameter `onNavigateToReminder`:
```kotlin
// SEBELUM (bug) — onNavigateToReminder tidak di-pass
HomeScreen(
    viewModel = homeViewModel,
    streakViewModel = streakViewModel,
    onNavigateToInput = { id -> navController.navigate(NavRoutes.inputRoute(id)) },
    onNavigateToSummary = { navController.navigate(NavRoutes.SUMMARY) },
    onNavigateToWalletDetail = { id -> navController.navigate(NavRoutes.walletDetailRoute(id)) }
)
```

Parameter `onNavigateToReminder` pada `HomeScreen` memiliki default value `{}` (no-op), sehingga klik tombol bell tidak menghasilkan aksi apapun.

**Fix**: Tambahkan parameter `onNavigateToReminder` yang menavigasi ke `NavRoutes.REMINDER_LIST`:
```kotlin
// SESUDAH (fix)
HomeScreen(
    viewModel = homeViewModel,
    streakViewModel = streakViewModel,
    onNavigateToInput = { id -> navController.navigate(NavRoutes.inputRoute(id)) },
    onNavigateToSummary = { navController.navigate(NavRoutes.SUMMARY) },
    onNavigateToWalletDetail = { id -> navController.navigate(NavRoutes.walletDetailRoute(id)) },
    onNavigateToReminder = { navController.navigate(NavRoutes.REMINDER_LIST) }
)
```

### Bug 2: Root Cause & Fix

**Root Cause**: Di `IncomeExpenseSummary` composable (HomeScreen.kt line 306–397), teks nominal menggunakan fixed `titleMedium` style tanpa constraint:
```kotlin
// SEBELUM (bug)
Text(
    text = CurrencyFormatter.format(totalIncome),
    style = MaterialTheme.typography.titleMedium,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.onSurface
)
```

Ketika nominal besar (misal Rp10.600.000), teks melebihi lebar card dan wrap/overflow.

**Fix**: Terapkan auto-sizing text yang menyusutkan font size secara otomatis agar muat dalam satu baris:
```kotlin
// SESUDAH (fix) — menggunakan autoSizeText atau manual approach
Text(
    text = CurrencyFormatter.format(totalIncome),
    style = MaterialTheme.typography.titleMedium,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.onSurface,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    modifier = Modifier.fillMaxWidth()
)
```

**Pendekatan lebih baik — Auto Size Text**:
Gunakan `autoSize` parameter (Material3 1.4+) atau custom `AutoResizeText` composable:
```kotlin
@Composable
fun AutoResizeText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified
) {
    var textStyle by remember { mutableStateOf(style) }
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text = text,
        style = textStyle,
        fontWeight = fontWeight,
        color = color,
        maxLines = maxLines,
        overflow = TextOverflow.Clip,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        onTextLayout = { result ->
            if (result.didOverflowWidth || result.didOverflowHeight) {
                textStyle = textStyle.copy(fontSize = textStyle.fontSize * 0.9)
            } else {
                readyToDraw = true
            }
        }
    )
}
```

### Struktur Composable (After Fix)
```
IncomeExpenseSummary (Row, weight 1:1)
├── Income Card (Surface, weight=1f)
│   └── Row (padding 16dp)
│       ├── Icon Circle (fixed 40dp) 
│       └── Column (Modifier.weight(1f))  ← tambah weight agar menghormati sisa ruang
│           ├── Label "Income"
│           └── AutoResizeText(nominal, maxLines=1)  ← auto shrink
│
└── Expense Card (Surface, weight=1f)
    └── Row (padding 16dp)
        ├── Icon Circle (fixed 40dp)
        └── Column (Modifier.weight(1f))  ← tambah weight agar menghormati sisa ruang
            ├── Label "Expense"
            └── AutoResizeText(nominal, maxLines=1)  ← auto shrink
```

### File yang Dimodifikasi

#### [MODIFY] [MainActivity.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/MainActivity.kt)
- Tambahkan `onNavigateToReminder = { navController.navigate(NavRoutes.REMINDER_LIST) }` pada pemanggilan `HomeScreen`

#### [MODIFY] [HomeScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/home/HomeScreen.kt)
- Pada `IncomeExpenseSummary`: tambahkan `Modifier.weight(1f)` pada `Column` di dalam `Row` card, agar teks nominal menghormati batas lebar
- Ganti `Text` nominal menjadi `AutoResizeText` atau terapkan `maxLines = 1` + `overflow = TextOverflow.Ellipsis` + font size adaptif
- (Opsional) Buat `AutoResizeText` composable sebagai helper reusable

### Dependency
- Tidak bergantung pada issue lain — dapat dikerjakan secara independen

## Skills
- Clean Architecture: Perbaikan navigasi di layer UI tanpa mengubah ViewModel atau Data layer
- Clean Code: Composable helper `AutoResizeText` reusable untuk kebutuhan teks responsif di seluruh app
- KISS: Fix navigasi cukup 1 baris tambahan parameter; fix responsive cukup constraint teks
- YAGNI: Tidak over-engineer — cukup auto-resize text, tidak perlu custom layout engine
- CI GitHub Actions: Validasi build + unit test otomatis
