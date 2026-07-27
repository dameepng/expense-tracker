# ISSUE-063: Cash Flow Insight — Hero Net Cash Flow Layout Redesign

## Deskripsi
Saat ini, card **"Cash Flow Insight"** pada halaman **Ringkasan** menampilkan tiga angka (Pemasukan, Pengeluaran, Net Cash Flow) secara sejajar dalam satu `Row`. Layout ini memperlakukan ketiga angka tersebut setara, padahal **Net Cash Flow adalah kesimpulan utama** dari card ini — insight yang paling penting bagi user.

### Perilaku Saat Ini
1. Tiga angka (Pemasukan, Pengeluaran, Net Cash Flow) ditampilkan **sejajar horizontal** dalam satu baris.
2. Tidak ada hierarki visual yang membedakan mana yang paling penting.
3. Card terasa seperti "3 angka sejajar" — kurang *insight-driven*.

### Perilaku yang Diharapkan
1. **Net Cash Flow** dijadikan **hero** di bagian atas card — teks lebih besar, background highlight (rounded container berwarna hijau muda/merah muda tergantung positif/negatif).
2. **Pemasukan & Pengeluaran** menjadi **breakdown 2 kolom** kecil di bawah hero section.
3. Hierarki visual jelas: user langsung melihat kesimpulan utama (net), lalu detail pendukung (income vs expense).
4. Card terasa lebih *insight-driven* dan premium.

## Acceptance Criteria
- [x] **Net Cash Flow** ditampilkan sebagai hero section di bagian atas card, dengan:
  - Label "Net cash flow" di atas angka.
  - Angka dengan ukuran font lebih besar (`titleLarge` atau `headlineSmall`).
  - Background rounded container: hijau muda (`Color(0xFFECFDF5)`) jika positif, merah muda (`Color(0xFFFEF2F2)`) jika negatif.
  - Warna teks: hijau (`#10B981`) jika positif, merah (`#EF4444`) jika negatif.
- [x] **Pemasukan & Pengeluaran** ditampilkan sebagai 2 kolom breakdown di bawah hero, masing-masing dengan:
  - Dot indikator berwarna (hijau untuk pemasukan, merah untuk pengeluaran).
  - Label di atas angka.
  - Font size lebih kecil dari hero (`bodyMedium` / `titleSmall`).
- [x] Urutan vertikal di dalam card: Title Row → Hero Net Cash Flow → Breakdown Row → Cash Flow Chart.
- [x] Dark mode tetap konsisten — background highlight menyesuaikan (gelap, bukan putih pucat).
- [x] Tidak ada perubahan pada data layer, ViewModel, atau unit test — ini murni perubahan UI composable.
- [x] Build & CI GitHub Actions pipeline tetap hijau.

## Technical Details

### UI Layer (`SummaryScreen.kt`)

#### Perubahan pada Cash Flow Insight Card (lines ~483–500)
Ganti `Row` yang menampilkan 3 kolom sejajar menjadi layout vertikal:

```
Column {
    // 1. Hero: Net Cash Flow (full width, background highlight)
    Box(background = greenLight/redLight, rounded, fullWidth, padding) {
        Column(center) {
            Text("Net cash flow", labelSmall)
            Text(CurrencyFormatter.format(netCashFlow), headlineSmall, bold, green/red)
        }
    }

    Spacer(12.dp)

    // 2. Breakdown: Income & Expense (2 kolom)
    Row(fillMaxWidth, SpaceBetween) {
        Row {
            Dot(green) 
            Column { Text("Pemasukan", label); Text(amount, bodyMedium, green) }
        }
        Row {
            Dot(red)
            Column { Text("Pengeluaran", label); Text(amount, bodyMedium, red) }
        }
    }
}
```

#### Tidak Ada File Lain yang Terpengaruh
- **SummaryViewModel.kt** — Tidak berubah. Data `netCashFlow`, `totalIncome`, `totalExpense` sudah tersedia di `SummaryUiState`.
- **SummaryUiState.kt** — Tidak berubah. Tidak ada field baru.
- **Unit Test** — Tidak ada test baru yang diperlukan. Ini murni perubahan visual/layout.

## Skills
- Clean Architecture: Pemisahan data/domain/ui layer
- Clean Code: Naming convention, single responsibility
- KISS: Perubahan minimal, hanya restrukturisasi layout composable
- YAGNI: Tidak menambah data/field baru yang belum diperlukan
- CI GitHub Actions: Validasi build + test otomatis
