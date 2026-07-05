# Design Tokens — Expense Tracker MVP (Material Design 3)

## Color Tokens

Menggunakan Material 3 `ColorScheme` di Jetpack Compose. Mendukung Dynamic Color (jika tersedia di Android 12+) dan fallback ke static color scheme untuk **light** dan **dark** theme.

### Primary (Tindakan utama, FAB, elemen aktif)
| Token | Light (Fallback) | Dark (Fallback) |
|-------|-------|------|
| `primary` | `#1B6EF3` (Blue 600) | `#8DB8FF` (Blue 200) |
| `onPrimary` | `#FFFFFF` | `#00317A` |
| `primaryContainer` | `#D6E3FF` | `#0049B5` |
| `onPrimaryContainer` | `#001B3E` | `#D6E3FF` |

### Secondary (Elemen sekunder, chart colors)
| Token | Light (Fallback) | Dark (Fallback) |
|-------|-------|------|
| `secondary` | `#555F71` | `#BBC7DB` |
| `onSecondary` | `#FFFFFF` | `#283141` |
| `secondaryContainer` | `#D9E3F8` | `#3E475B` |
| `onSecondaryContainer` | `#121C2B` | `#D9E3F8` |

### Tertiary (Aksen tambahan)
| Token | Light (Fallback) | Dark (Fallback) |
|-------|-------|------|
| `tertiary` | `#705575` | `#D7BDDD` |
| `onTertiary` | `#FFFFFF` | `#3B2640` |
| `tertiaryContainer` | `#FAD8FF` | `#553B5B` |
| `onTertiaryContainer` | `#28132E` | `#FAD8FF` |

### Surface & Background (Latar belakang aplikasi dan kartu)
| Token | Light (Fallback) | Dark (Fallback) |
|-------|-------|------|
| `background` | `#FEFBFF` | `#1B1B1F` |
| `onBackground` | `#1B1B1F` | `#E4E2E6` |
| `surface` | `#FEFBFF` | `#1B1B1F` |
| `onSurface` | `#1B1B1F` | `#E4E2E6` |
| `surfaceVariant` | `#E1E2EC` | `#44474F` |
| `onSurfaceVariant` | `#44474F` | `#C4C6D0` |
| `outline` | `#74777F` | `#8E9099` |
| `outlineVariant` | `#C4C6D0` | `#44474F` |

### Error (Validasi, status negatif)
| Token | Light (Fallback) | Dark (Fallback) |
|-------|-------|------|
| `error` | `#BA1A1A` | `#FFB4AB` |
| `onError` | `#FFFFFF` | `#690005` |
| `errorContainer` | `#FFDAD6` | `#93000A` |
| `onErrorContainer` | `#410002` | `#FFDAD6` |

### Category Colors (Statis untuk 7 kategori)
Tetap menggunakan warna statis agar chart dan ikon konsisten, namun direpresentasikan sebagai `Color` dalam Compose.
- **Makanan**: `Color(0xFFFF8C00)` (Orange)
- **Transport**: `Color(0xFF1E90FF)` (Blue)
- **Belanja**: `Color(0xFF8A2BE2)` (Purple)
- **Hiburan**: `Color(0xFFFF1493)` (Pink)
- **Tagihan**: `Color(0xFFDC143C)` (Red)
- **Kesehatan**: `Color(0xFF2E8B57)` (Green)
- **Lainnya**: `Color(0xFF708090)` (Gray)

---

## Typography

Menggunakan `MaterialTheme.typography` di Compose.

| Style | M3 Token | Usage |
|-------|------|-------|
| `displaySmall` | `MaterialTheme.typography.displaySmall` | Total amount di Home |
| `headlineMedium`| `MaterialTheme.typography.headlineMedium` | Judul screen, Input nominal |
| `titleMedium` | `MaterialTheme.typography.titleMedium` | Nama kategori, header section |
| `bodyLarge` | `MaterialTheme.typography.bodyLarge` | Teks utama, nominal di list |
| `bodyMedium` | `MaterialTheme.typography.bodyMedium` | Teks pendukung, jam/timestamp |
| `labelLarge` | `MaterialTheme.typography.labelLarge` | Teks di tombol (`Button`), `AssistChip` |
| `labelSmall` | `MaterialTheme.typography.labelSmall` | Teks persentase kecil |

---

## Shape / Border Radius

Menggunakan `MaterialTheme.shapes` di Compose.

| Shape Token | M3 Default Value | Usage |
|-------|-------|-------|
| `ExtraSmall` | 4.0.dp | Badge kecil |
| `Small` | 8.0.dp | `AssistChip`, `FilterChip` |
| `Medium` | 12.0.dp | `ElevatedCard`, list item container |
| `Large` | 16.0.dp | Bottom sheet, dialog |
| `ExtraLarge` | 28.0.dp | `FloatingActionButton` (FAB) besar, Container utama |
| `Full` | Circle / 50% | Ikon avatar, badge bundar |

---

## Spacing & Layout

Di Jetpack Compose, layouting dilakukan dengan `Modifier.padding()` dan `Arrangement.spacedBy()`.

| Token Konsep | Value | Implementasi Compose |
|-------|-------|-------|
| `xs` | 4.dp | `Modifier.padding(4.dp)`, `Arrangement.spacedBy(4.dp)` |
| `sm` | 8.dp | `Modifier.padding(8.dp)`, `Arrangement.spacedBy(8.dp)` |
| `md` | 12.dp | `Modifier.padding(12.dp)`, `Arrangement.spacedBy(12.dp)` |
| `lg` | 16.dp | Horizontal margin screen utama |
| `xl` | 24.dp | Top/bottom padding antar section |
| `xxl` | 32.dp | Padding besar |

---

## Icons

Menggunakan `androidx.compose.material.icons:material-icons-extended`.
Diakses melalui `Icons.Filled.*`, `Icons.Outlined.*`, atau `Icons.Rounded.*`.

| Icon | Compose Reference | Usage |
|------|-------|-------|
| Tambah | `Icons.Filled.Add` | `FloatingActionButton` |
| Summary | `Icons.Filled.BarChart` / `PieChart` | `TopAppBar` action |
| Streak | `Icons.Filled.LocalFireDepartment` | Streak counter |
| Back | `Icons.AutoMirrored.Filled.ArrowBack` | `TopAppBar` navigation icon |
| Simpan | `Icons.Filled.Check` | Tombol simpan |
| Kategori | (Berbagai Icons.Filled) | Icon list/chip kategori |

---

## Elevation / Tonal Elevation

Di Material 3 Compose, shadow elevation jarang digunakan. Sebagai gantinya, digunakan **Tonal Elevation** yang otomatis mengubah warna surface berdasar `primary` color (dihandle oleh `Surface`, `ElevatedCard`, dll).

| Tonal Level | Tonal Elevation | Usage |
|-------|-------|-------|
| Level 0 | 0.dp | `Scaffold` background, `Card` biasa |
| Level 1 | 1.dp | `ElevatedCard` (item di list opsional) |
| Level 2 | 3.dp | `TopAppBar` (saat discroll) |
| Level 3 | 6.dp | `FloatingActionButton` |
| Level 4 | 8.dp | `ModalBottomSheet` |
