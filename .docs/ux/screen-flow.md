# Screen Flow & Architecture — Expense Tracker MVP (Material Design 3)

## Navigation Map (Compose `Scaffold` Structure)

```
┌─────────────────────────────────────────────────┐
│                  HOME SCREEN                     │
│  (Menggunakan M3 Scaffold)                      │
│                                                 │
│  [TopAppBar]                                    │
│  Expense Tracker                           [📊] │
│ ──────────────────────────────────────────────── │
│                                                 │
│  🔥 Streak Counter (Surface/Badge)               │
│  ┌──────┬──────┬──────┐                 │       │
│  │ Hari │Minggu│ Bulan│  ← SegmentedButton       │
│  │ Ini  │ Ini  │ Ini  │                 │       │
│  └──────┴──────┴──────┘                 │       │
│                                                 │
│        Rp 150.000 (displaySmall)                │
│      Total Hari Ini                             │
│                                                 │
│  ┌──────────────────────────────────┐           │
│  │ 🍔 Makanan         Rp 50.000    │           │
│  │ 🚗 Transport       Rp 35.000    │           │
│  │ 🛍️ Belanja         Rp 65.000    │           │
│  └──────────────────────────────────┘           │
│          (Empty state jika 0)                   │
│                                                 │
│                                    [+] (FAB)    │
└─────────────────────────────────────────────────┘
        │ tap FAB [+]         │ tap icon [📊]
        ▼                     ▼
┌───────────────┐     ┌───────────────────────────┐
│ INPUT SCREEN  │     │     SUMMARY SCREEN        │
│(BottomSheet   │     │   (M3 Scaffold)           │
│ atau lay.baru)│     │                           │
│               │     │ [TopAppBar] ← Back [📊]   │
│ Nominal:      │     │                           │
│ [ Rp 0       ]│     │ Filter: Hari/Minggu/Bulan │
│(OutlinedField)│     │ (SegmentedButton)         │
│               │     │                           │
│ Kategori:     │     │ 🍔 Makanan   50.000 33%   │
│ ┌───┬───┬───┐ │     │ ████████████              │
│ │🍔 │🚗 │🛍️ │ │     │ 🚗 Transport 35.000 23%   │
│ ├───┼───┼───┤ │     │ █████████                 │
│ │🎬 │📋 │🏥 │ │     │ 🛍️ Belanja   65.000 43%   │
│ ├───┼───┼───┤ │     │ ███████████████           │
│ │...│       │ │     │                           │
│ └───┴───┴───┘ │     │ Total: Rp 150.000         │
│ (FilterChip)  │     │                           │
│               │     │ (Empty state jika 0)      │
│ [✓ Simpan]    │     └───────────────────────────┘
│ (Button M3)   │              │
└───────┬───────┘              │
        │ Simpan sukses        │ ◀ back (TopAppBar navigation icon)
        ▼                      ▼
   HOME SCREEN           HOME SCREEN
   (refresh data)        (tetap)
```

## Transitions

| From | Trigger | To | Notes |
|------|---------|----|-------|
| Home | Tap FAB `[+]` | Input | `ModalBottomSheet` (rekomendasi M3) atau pindah screen `NavHost` |
| Home | Tap icon `[📊]` | Summary | Via `TopAppBar` action |
| Input | Tap `[✓ Simpan]` (valid) | Home | Insert expense → dismiss sheet/pop back |
| Input | Swipe down / Back | Home | Dismiss sheet (discard input) |
| Summary | Back icon di TopAppBar | Home | `navController.popBackStack()` |
| Home | Tap filter `SegmentedButton` | Home | In-place state refresh |

## Navigation Type (Jetpack Compose)

- **Single Activity Architecture**.
- **`NavHost`**:
  - `home`
  - `summary`
  - (Opsional) `input` jika dirender sebagai screen terpisah, atau cukup sebagai `ModalBottomSheet` di atas `home` screen.
- Menggunakan `Scaffold` Material 3 untuk pengaturan layout konsisten (`topBar`, `floatingActionButton`, `content`).
