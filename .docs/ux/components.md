# Component Spec — Expense Tracker MVP (Material Design 3)

## 1. Home Screen

### 1.1 StreakCounter (Menggunakan `Surface` / Badge)
| Prop | Type | Default | Notes |
|------|------|---------|-------|
| `streak` | Int | 0 | Jumlah hari berturut-turut |
| `modifier` | Modifier | — | Layout override |

**States:**
| State | Visual |
|-------|--------|
| `streak > 0` | 🔥 `N hari berturut-turut!` — Ikon api oranye, teks bold, `Surface` dengan `color = MaterialTheme.colorScheme.primaryContainer`, `shape = MaterialTheme.shapes.small` |
| `streak == 0` | 🔥 `Mulai streak kamu hari ini!` — Ikon api abu-abu (`onSurfaceVariant`), `Surface` dengan `color = MaterialTheme.colorScheme.surfaceVariant` |

**Layout:** `Surface` membungkus `Row`. Padding 12.dp horizontal, 8.dp vertical. Diposisikan di atas filter tabs.

### 1.2 FilterTabs (Menggunakan `SingleChoiceSegmentedButtonRow` M3)
| Prop | Type | Default | Notes |
|------|------|---------|-------|
| `selectedFilter` | FilterPeriod | `TODAY` | Enum: TODAY, WEEK, MONTH |
| `onFilterSelected` | (FilterPeriod) → Unit | — | Callback |

**Layout:** Memanfaatkan `SingleChoiceSegmentedButtonRow` dan `SegmentedButton` bawaan Material 3 Compose. Memberikan interaksi pemilihan periode yang lebih modern dan menyatu. 
Padding 16.dp horizontal.

### 1.3 TotalDisplay
| Prop | Type | Default | Notes |
|------|------|---------|-------|
| `amount` | Long | 0 | Total dalam rupiah |
| `periodLabel` | String | "Hari Ini" | Dinamis sesuai filter |
| `modifier` | Modifier | — | |

**States:**
| State | Visual |
|-------|--------|
| `amount > 0` | Angka besar (`style = MaterialTheme.typography.displaySmall`, `color = MaterialTheme.colorScheme.onBackground`), format: `Rp 150.000` |
| `amount == 0` | Angka `Rp 0`, tetap bold. |

**Layout:** Center-aligned, `Column` dengan jarak antar teks kecil.

### 1.4 ExpenseList
| Prop | Type | Default | Notes |
|------|------|---------|-------|
| `expenses` | List\<ExpenseWithCategory\> | emptyList() | Join expense + category name |
| `modifier` | Modifier | — | |

**States:**
| State | Visual |
|-------|--------|
| **Success** (ada data) | `LazyColumn` — tiap item adalah `ExpenseListItem` |
| **Empty** (tidak ada data) | Ilustrasi tengah + teks "Belum ada pengeluaran [periode]" (Warna `onSurfaceVariant`) |

**Layout:** `LazyColumn`, 16.dp horizontal padding. `Arrangement.spacedBy(8.dp)` antar item agar tidak perlu divider manual.

### 1.5 ExpenseListItem (Menggunakan `ElevatedCard` atau `ListItem` M3)
| Prop | Type | Default | Notes |
|------|------|---------|-------|
| `categoryName` | String | — | Nama kategori |
| `amount` | Long | — | Nominal |
| `timestamp` | Long | — | Epoch millis, format ke HH:mm |
| `modifier` | Modifier | — | |

**Layout:** Dianjurkan menggunakan `androidx.compose.material3.ListItem` yang sudah dioptimasi.
- `headlineContent`: Nama kategori (`titleMedium`)
- `supportingContent`: Jam (`bodyMedium`)
- `leadingContent`: Icon kategori dalam `Surface` bundar.
- `trailingContent`: Amount mentah (`bodyLarge`, bold)
Atau menggunakan `ElevatedCard` dengan `Row` kustom jika butuh visual lebih 'card-like'.

---

## 2. Input Screen (ModalBottomSheet atau Screen)

### 2.1 AmountInput (Menggunakan `OutlinedTextField` M3)
| Prop | Type | Default | Notes |
|------|------|---------|-------|
| `amount` | String | "" | Teks input mentah |
| `onAmountChange` | (String) → Unit | — | Filter numeric only |
| `modifier` | Modifier | — | |

**States:**
| State | Visual |
|-------|--------|
| **Empty** | Placeholder "0", `prefix = { Text("Rp") }` |
| **Filled** | Angka terformat. |
| **Error** | Jika submit invalid: `isError = true`, outline merah. |

**Layout:** `OutlinedTextField`, `keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)`. Width: `fillMaxWidth()`. Teks besar (`headlineMedium`).

### 2.2 CategoryGrid
| Prop | Type | Default | Notes |
|------|------|---------|-------|
| `categories` | List\<Category\> | — | 7 kategori |
| `selectedId` | Long? | null | ID kategori terpilih |
| `onCategorySelected` | (Long) → Unit | — | Callback |

**Layout:** `LazyVerticalGrid` (columns = 3), `verticalArrangement` dan `horizontalArrangement` 12.dp.

### 2.3 CategoryChip (Menggunakan `FilterChip` M3)
| Prop | Type | Default | Notes |
|------|------|---------|-------|
| `category` | Category | — | Data kategori |
| `isSelected` | Boolean | false | |
| `onClick` | () → Unit | — | |

**Layout:** Menggunakan `androidx.compose.material3.FilterChip`.
- `selected = isSelected`
- `onClick = onClick`
- `label = { Text(category.name) }`
- `leadingIcon`: Menampilkan icon kategori (dan checkmark bawaan M3 jika disetel).

### 2.4 SaveButton (Menggunakan `Button` M3)
| Prop | Type | Default | Notes |
|------|------|---------|-------|
| `enabled` | Boolean | false | True jika valid |
| `onClick` | () → Unit | — | |

**Layout:** `Button` (Filled Button), `modifier = Modifier.fillMaxWidth()`. Menggunakan warna primary secara otomatis.

---

## 3. Summary Screen

### 3.1 SummaryFilterTabs
Menggunakan `SingleChoiceSegmentedButtonRow` sama seperti di Home Screen.

### 3.2 BreakdownList
| Prop | Type | Default | Notes |
|------|------|---------|-------|
| `items` | List\<CategoryBreakdown\> | emptyList() | |
| `totalAmount` | Long | 0 | |
| `modifier` | Modifier | — | |

**Layout:** `LazyColumn` dengan `Arrangement.spacedBy(16.dp)`.

### 3.3 BreakdownItem
| Prop | Type | Default | Notes |
|------|------|---------|-------|
| `categoryName` | String | — | |
| `amount` | Long | — | |
| `percentage` | Float | 0f | 0.0 - 1.0 |
| `color` | Color | — | Warna kategori |

**Layout:** 
- Header Row: Nama kategori (kiri) & Amount (kanan).
- Bar Row: `LinearProgressIndicator` (M3) modifikasi dengan `trackColor = MaterialTheme.colorScheme.surfaceVariant`, `color = color`, tinggi 8.dp, rounded shape (`strokeCap = StrokeCap.Round`).
- Teks persentase di ujung.
