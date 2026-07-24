# ISSUE-057: Spending by Category — Card Redesign & Toggle

**Priority:** Medium  
**Type:** UI/UX Redesign  
**Screen:** Summary (Ringkasan) → Spending by Category Section

## Reference Design

![Reference - Spending by Category Card](file:///C:/Users/Adam/.gemini/antigravity-ide/brain/4ab7ebc6-077c-4859-a8c4-67a71f590b1d/media__1784859222547.png)

## Deskripsi

Redesign section "Spending by category" agar lebih compact dan rapi dengan membungkusnya dalam sebuah **Card**, serta mengganti TabRow (Pengeluaran / Pemasukan) menjadi **toggle kecil** di ujung kanan judul.

## Detail Implementasi

### 1. Wrap dalam Card
- Bungkus seluruh section "Spending by category" dalam sebuah `Card` dengan `RoundedCornerShape(16.dp)`
- Gunakan warna card yang sama dengan Cash Flow Insight card (putih di light mode, `surfaceContainerHigh` di dark mode)
- Elevation 2dp

### 2. Toggle Income / Outcome (Gantikan TabRow)
- **Hapus** `SummaryTypeTabs` (TabRow full-width) yang ada saat ini
- Tambahkan **toggle kecil / dropdown** di ujung kanan dari judul "Spending by category"
- Toggle menampilkan "Pengeluaran ▼" / "Pemasukan ▼" — saat diklik, switch antara keduanya
- Bisa menggunakan `TextButton` atau dropdown sederhana seperti `SummaryPeriodDropdown`

### 3. Layout dalam Card (sesuai referensi)
Semua elemen berikut berada di dalam Card:

```
┌─────────────────────────────────────────┐
│ Spending by category    [Pengeluaran ▼] │
│                                         │
│   ┌──────────┐   Housing        40%     │
│   │          │   Food & Dining  24%     │
│   │  DONUT   │   Transport      15%     │
│   │  CHART   │   Shopping       10%     │
│   │          │   Others         11%     │
│   └──────────┘                          │
│                                         │
│ Total spending          Rp14.020.000  > │
└─────────────────────────────────────────┘
```

- **Donut Chart** (kiri) + **Legend** (kanan) — layout yang sudah ada
- **Total spending** di bagian bawah card, dengan nominal di kanan dan ikon chevron `>`
- Hapus section "Total" + angka besar yang saat ini ada di atas donut chart (redundan)

### 4. Breakdown List di Bawah Card
- List detail per-kategori (`BreakdownCardItem`) tetap ditampilkan di bawah card sebagai daftar terpisah

## File yang Dimodifikasi

| File | Perubahan |
|---|---|
| `SummaryScreen.kt` | Wrap spending section dalam Card, ganti TabRow dengan toggle, tambah total spending footer |

## Acceptance Criteria

- [ ] Section "Spending by category" dibungkus dalam Card
- [ ] Toggle Income/Outcome di ujung kanan judul (bukan TabRow)
- [ ] Donut chart + legend tampil rapi di dalam card
- [ ] Total spending tampil di bagian bawah card
- [ ] Breakdown list tetap tampil di bawah card
- [ ] Tampilan konsisten di light & dark mode
