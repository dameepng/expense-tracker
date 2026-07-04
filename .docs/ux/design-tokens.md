# Design Tokens — Expense Tracker MVP

## Color Tokens

Menggunakan Material 3 color scheme. Semua token support **light** dan **dark** theme.

### Primary (Actions, selected tabs, FAB)
| Token | Light | Dark |
|-------|-------|------|
| `primary` | `#1B6EF3` (Blue 600) | `#8DB8FF` (Blue 200) |
| `onPrimary` | `#FFFFFF` | `#00317A` |
| `primaryContainer` | `#D6E3FF` | `#0049B5` |
| `onPrimaryContainer` | `#001B3E` | `#D6E3FF` |

### Secondary (Summary/chart colors)
| Token | Light | Dark |
|-------|-------|------|
| `secondary` | `#555F71` | `#BBC7DB` |
| `onSecondary` | `#FFFFFF` | `#283141` |
| `secondaryContainer` | `#D9E3F8` | `#3E475B` |
| `onSecondaryContainer` | `#121C2B` | `#D9E3F8` |

### Tertiary (Accent)
| Token | Light | Dark |
|-------|-------|------|
| `tertiary` | `#705575` | `#D7BDDD` |
| `onTertiary` | `#FFFFFF` | `#3B2640` |
| `tertiaryContainer` | `#FAD8FF` | `#553B5B` |
| `onTertiaryContainer` | `#28132E` | `#FAD8FF` |

### Surface & Background
| Token | Light | Dark |
|-------|-------|------|
| `surface` | `#FEFBFF` | `#1B1B1F` |
| `onSurface` | `#1B1B1F` | `#E4E2E6` |
| `surfaceVariant` | `#E1E2EC` | `#44474F` |
| `onSurfaceVariant` | `#44474F` | `#C4C6D0` |
| `background` | `#FEFBFF` | `#1B1B1F` |
| `onBackground` | `#1B1B1F` | `#E4E2E6` |

### Error (Validation, negative states)
| Token | Light | Dark |
|-------|-------|------|
| `error` | `#BA1A1A` | `#FFB4AB` |
| `onError` | `#FFFFFF` | `#690005` |
| `errorContainer` | `#FFDAD6` | `#93000A` |
| `onErrorContainer` | `#410002` | `#FFDAD6` |

### Category Colors (fixed mapping untuk 7 kategori)
Digunakan untuk breakdown bar chart dan ikon kategori. Sama untuk light & dark.

| Kategori | Color | Hex |
|----------|-------|-----|
| Makanan | Orange | `#FF8C00` |
| Transport | Blue | `#1E90FF` |
| Belanja | Purple | `#8A2BE2` |
| Hiburan | Pink | `#FF1493` |
| Tagihan | Red | `#DC143C` |
| Kesehatan | Green | `#2E8B57` |
| Lainnya | Gray | `#708090` |

---

## Typography

Menggunakan Material 3 `Typography` defaults dengan font system.

| Style | Size | Weight | Usage |
|-------|------|--------|-------|
| `displaySmall` | 36sp | Bold | Total amount di home |
| `headlineSmall` | 24sp | Medium | Judul screen |
| `titleMedium` | 16sp | Medium | Label filter tab, nama kategori |
| `bodyLarge` | 16sp | Normal | Amount di list item |
| `bodyMedium` | 14sp | Normal | Jam/timestamp, teks pendukung |
| `labelLarge` | 14sp | Medium | Tombol, chip kategori |
| `labelSmall` | 11sp | Medium | Persentase di breakdown |

---

## Spacing

| Token | Value | Usage |
|-------|-------|-------|
| `xs` | 4dp | Gap ikon-teks dalam chip |
| `sm` | 8dp | Gap antar chip/filter tab, padding streak badge |
| `md` | 12dp | Gap antar item list, grid gap |
| `lg` | 16dp | Padding horizontal screen, margin section |
| `xl` | 24dp | Padding top/bottom section |
| `xxl` | 32dp | Padding horizontal untuk input field |

---

## Shape / Border Radius

| Token | Value | Usage |
|-------|-------|-------|
| `small` | 8dp | Chip, badge |
| `medium` | 12dp | Card, list item container |
| `large` | 16dp | Button, TextField |

---

## Icons

Menggunakan Material Icons default (filled).

| Icon | Usage |
|------|-------|
| `Add` | FAB tambah expense |
| `BarChart` / `PieChart` | Navigasi ke Summary |
| `LocalFireDepartment` | Streak counter 🔥 |
| `ArrowBack` / `Close` | Navigasi kembali |
| `Check` | Tombol simpan |
| `Restaurant` | Kategori Makanan |
| `DirectionsCar` | Kategori Transport |
| `ShoppingBag` | Kategori Belanja |
| `Movie` | Kategori Hiburan |
| `Receipt` | Kategori Tagihan |
| `LocalHospital` | Kategori Kesehatan |
| `MoreHoriz` | Kategori Lainnya |

---

## Elevation

| Level | Value | Usage |
|-------|-------|-------|
| 0 | 0dp | Background, list content |
| 1 | 1dp | Card/item dalam list (opsional) |
| 2 | 3dp | Top app bar |
| 3 | 6dp | FAB |
| 4 | 8dp | Bottom sheet (input screen jika pakai bottom sheet) |
