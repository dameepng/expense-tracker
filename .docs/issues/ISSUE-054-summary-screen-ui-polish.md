# ISSUE-054: Summary Screen UI Polish & Visual Redesign

**Priority:** Medium  
**Type:** UI/UX Enhancement  
**Screen:** Summary (Ringkasan)

## Problem

Halaman Summary saat ini fungsional tapi terasa **flat, boring, dan kurang premium**. Beberapa masalah visual:

1. **Donut Chart terlalu besar** — memakan hampir seluruh layar, terlalu dominan. Saat hanya 1 kategori, terlihat seperti lingkaran besar solid tanpa variasi visual.
2. **Breakdown Card sangat plain** — card abu-abu flat dengan dot kecil, persentase, dan nominal. Tidak ada warna kategori yang kuat, tidak ada progress bar, dan terasa generik.
3. **Filter section terlalu padat** — ada 3 layer filter (Tab + Chip + Segmented) berjajar vertikal tanpa visual breathing room.
4. **Tidak ada gradient atau depth** — semua komponen menggunakan surface color yang sama, tidak ada hierarki visual yang jelas.
5. **Empty state membosankan** — hanya emoji dan teks.

## Proposed Design Improvements

### 1. Donut Chart — Lebih Compact & Dengan Legend
- Perkecil ukuran donut (max 180dp, bukan full-width aspect ratio 1:1)
- Tambahkan **legend list** di samping kanan donut chart (layout horizontal: chart kiri, legend kanan)
- Legend menampilkan: colored dot + nama kategori + persentase
- Pada mobile yang sempit, legend bisa di bawah chart
- Tambahkan gap/spacing antar arc segment agar lebih jelas pemisahannya

### 2. Breakdown Cards — Lebih Colorful & Informative
- Tambahkan **color accent bar** di sisi kiri card (vertical strip warna kategori)
- Tambahkan **horizontal progress bar** yang menunjukkan proporsi terhadap total
- Layout card: icon + nama di kiri, nominal + persentase di kanan (single row per card, bukan grid 2 kolom)
- Gunakan **list layout** (bukan grid) agar lebih readable
- Background card sedikit tinted dengan warna kategori (very subtle alpha)

### 3. Header Area — Lebih Clean
- Gabungkan total amount ke area atas (bukan di dalam donut)
- Tampilkan total amount besar + label periode di bawah TopAppBar
- Beri subtle gradient background pada area header

### 4. Filter Section — Lebih Compact
- Wallet chips & period filter bisa dibuat lebih compact
- Tambahkan subtle divider atau spacing yang lebih baik antar section

### 5. Animasi & Polish
- Tambahkan animasi angka saat berganti filter (count-up animation)
- Smooth transition saat berganti data
- Skeleton loading state yang lebih menarik daripada spinner

## Visual Reference Layout

```
┌──────────────────────────────────┐
│  Ringkasan                    📅 │
├──────────────────────────────────┤
│  Pengeluaran  │  Pemasukan       │
├──────────────────────────────────┤
│ [Semua] [BCA] [Mandiri]         │
│  Today │ Week │ Month            │
├──────────────────────────────────┤
│                                  │
│  Total Pengeluaran               │
│  Rp 1.250.000                    │
│                                  │
│   ┌─────┐  • Makanan     40%     │
│   │ 🍩  │  • Transport   30%     │
│   │     │  • Belanja     20%     │
│   └─────┘  • Hiburan     10%     │
│                                  │
├──────────────────────────────────┤
│ ▌████████████ Makanan    Rp500k  │
│ ▌█████████   Transport   Rp375k  │
│ ▌██████     Belanja      Rp250k  │
│ ▌████       Hiburan      Rp125k  │
└──────────────────────────────────┘
```

## Acceptance Criteria
- [ ] Donut chart lebih compact dengan legend
- [ ] Breakdown menggunakan list layout dengan progress bar & color accent
- [ ] Total amount ditampilkan prominent di atas chart
- [ ] Filter area lebih compact
- [ ] Animasi smooth saat berganti data
- [ ] Tetap responsive dan tidak lag
