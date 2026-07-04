# Component Spec — Expense Tracker MVP

## 1. Home Screen

### 1.1 StreakCounter
| Prop | Type | Default | Notes |
|------|------|---------|-------|
| `streak` | Int | 0 | Jumlah hari berturut-turut |
| `modifier` | Modifier | — | Layout override |

**States:**
| State | Visual |
|-------|--------|
| `streak > 0` | 🔥 `N hari berturut-turut!` — icon api oranye, teks bold, badge background primaryContainer |
| `streak == 0` | 🔥 `Mulai streak kamu hari ini!` — icon api abu-abu (muted), teks subtle |

**Layout:** Horizontal chip/badge, padding 12dp horizontal, 8dp vertical. Ditempatkan di atas filter tabs (slot yang disediakan F2a).

### 1.2 FilterTabs
| Prop | Type | Default | Notes |
|------|------|---------|-------|
| `selectedFilter` | FilterPeriod | `TODAY` | Enum: TODAY, WEEK, MONTH |
| `onFilterSelected` | (FilterPeriod) → Unit | — | Callback |

**States:** Single state — salah satu tab selalu selected (default `TODAY`).

**Layout:** 3 chip/tab horizontal dalam Row, equal weight atau wrap content. Selected tab: filled/primary. Unselected: outlined/surface. Gap 8dp antar tab. Padding 16dp horizontal.

### 1.3 TotalDisplay
| Prop | Type | Default | Notes |
|------|------|---------|-------|
| `amount` | Long | 0 | Total dalam rupiah |
| `periodLabel` | String | "Hari Ini" | Dinamis sesuai filter |
| `modifier` | Modifier | — | |

**States:**
| State | Visual |
|-------|--------|
| `amount > 0` | Angka besar (textStyle displaySmall/bold), format: `Rp 150.000` |
| `amount == 0` | Angka `Rp 0`, tetap bold (jangan hilang — empty state beda komponen) |

**Layout:** Center-aligned, 24dp top padding dari filter tabs. Format rupiah pakai `NumberFormat.getCurrencyInstance("id-ID")`.

### 1.4 ExpenseList
| Prop | Type | Default | Notes |
|------|------|---------|-------|
| `expenses` | List\<ExpenseWithCategory\> | emptyList() | Join expense + category name |
| `modifier` | Modifier | — | |

**States:**
| State | Visual |
|-------|--------|
| **Success** (ada data) | `LazyColumn` — tiap item: ikon kategori + nama kategori + amount + jam (HH:mm) |
| **Empty** (tidak ada data) | Ilustrasi tengah + teks "Belum ada pengeluaran [hari ini/minggu ini/bulan ini]" |

**Layout:** `LazyColumn`, 16dp horizontal padding. Item height ~56dp. Divider tipis antar item. Empty state: `Box(contentAlignment = Center)` dengan `Column` (icon + text).

### 1.5 ExpenseListItem
| Prop | Type | Default | Notes |
|------|------|---------|-------|
| `categoryName` | String | — | Nama kategori |
| `amount` | Long | — | Nominal |
| `timestamp` | Long | — | Epoch millis, format ke HH:mm |
| `modifier` | Modifier | — | |

**Layout:** `Row` — ikon kategori (20dp circle/dot) + `Column` (nama kategori + jam) + `Spacer(weight)` + amount (bold, right-aligned).

---

## 2. Input Screen

### 2.1 AmountInput
| Prop | Type | Default | Notes |
|------|------|---------|-------|
| `amount` | String | "" | Teks input mentah |
| `onAmountChange` | (String) → Unit | — | Filter numeric only |
| `modifier` | Modifier | — | |

**States:**
| State | Visual |
|-------|--------|
| **Empty** | Placeholder "0", cursor blinking |
| **Filled** | Angka terformat, prefix "Rp" di luar input atau sebagai prefix TextField |
| **Error** (opsional) | Kalau amount = 0 lalu user submit, field border merah + helper text "Minimal Rp 1" |

**Layout:** `OutlinedTextField`, keyboardType = Number, textStyle headlineMedium, center-aligned text. Width: fillMaxWidth dengan 32dp horizontal padding.

### 2.2 CategoryGrid
| Prop | Type | Default | Notes |
|------|------|---------|-------|
| `categories` | List\<Category\> | — | 7 kategori dari DB |
| `selectedId` | Long? | null | ID kategori terpilih |
| `onCategorySelected` | (Long) → Unit | — | Callback |

**States:**
| State | Visual |
|-------|--------|
| **Loading** | 7 skeleton placeholder chips |
| **Success** | Grid 3 kolom (7 item = 3+3+1 baris terakhir) |
| **None selected** | Semua chip unselected (outlined style) |

**Layout:** `LazyVerticalGrid` columns = 3, 12dp gap. Tiap item: `AssistChip` atau `FilterChip` dengan ikon + nama kategori. Selected: primary container color.

### 2.3 CategoryChip
| Prop | Type | Default | Notes |
|------|------|---------|-------|
| `category` | Category | — | Data kategori |
| `isSelected` | Boolean | false | |
| `onClick` | () → Unit | — | |

**Layout:** ~96dp width, 40dp height. Ikon di atas teks (atau ikon di kiri). Selected = filled tonal button style. Unselected = outlined.

### 2.4 SaveButton
| Prop | Type | Default | Notes |
|------|------|---------|-------|
| `enabled` | Boolean | false | True hanya jika amount > 0 DAN kategori terpilih |
| `onClick` | () → Unit | — | |

**States:**
| State | Visual |
|-------|--------|
| **Disabled** (amount kosong atau kategori null) | Warna muted, tidak bisa diklik |
| **Enabled** | Warna primary, clickable |

**Layout:** `Button` full width, 16dp horizontal padding, 24dp bottom padding. Teks "Simpan" dengan checkmark icon.

---

## 3. Summary Screen

### 3.1 SummaryFilterTabs
Mirip dengan `FilterTabs` di Home Screen. Bisa reuse komponen yang sama.

### 3.2 BreakdownList
| Prop | Type | Default | Notes |
|------|------|---------|-------|
| `items` | List\<CategoryBreakdown\> | emptyList() | Nama kategori + total + persentase |
| `totalAmount` | Long | 0 | Total keseluruhan (buat hitung %) |
| `modifier` | Modifier | — | |

**States:**
| State | Visual |
|-------|--------|
| **Success** (ada data) | `LazyColumn` — tiap item: nama + amount + horizontal bar (persentase) + angka % |
| **Empty** | Ilustrasi tengah + "Belum ada data pengeluaran" |

**Layout:** `LazyColumn`, 16dp padding. Tiap item:
- Row: nama kategori (kiri) + amount (kanan)
- LinearProgressIndicator atau custom bar dengan width sesuai persentase, warna per kategori
- Teks persentase kecil di kanan bar

### 3.3 BreakdownItem
| Prop | Type | Default | Notes |
|------|------|---------|-------|
| `categoryName` | String | — | |
| `amount` | Long | — | |
| `percentage` | Float | 0f | 0.0 - 1.0 |
| `color` | Color | — | Warna per kategori (bisa dari mapping statis 7 warna) |

**Layout:** `Column` — `Row` (nama + amount) + `LinearProgressIndicator(progress = percentage, color = color)` + teks `(percentage*100).toInt()%`.
