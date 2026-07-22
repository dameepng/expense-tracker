# ISSUE-034: Summary Type Switcher as M3 Tabs Component

## Deskripsi
Sesuai arahan UI/UX, Segmented Button untuk beralih antara Pengeluaran dan Pemasukan pada halaman Summary diganti dengan Material 3 Tabs component (`TabRow` & `Tab`). Posisinya dipindahkan menjadi paling atas, tepat di bawah Title "Ringkasan" (sebelum filter tanggal).

## User Story
> Sebagai pengguna, saya ingin dapat berpindah antara Pengeluaran dan Pemasukan menggunakan tampilan Tab yang lebih terintegrasi dengan struktur header halaman Summary, sehingga tampilan UI lebih elegan mengikuti panduan desain Material 3.

## Acceptance Criteria
- [x] Komponen `SegmentedButton` diganti dengan `TabRow` dan `Tab` (Material 3).
- [x] Posisi Tabs dipindahkan ke paling atas (di bawah TopAppBar).
- [x] Tab Pengeluaran menggunakan warna primary theme.
- [x] Tab Pemasukan menggunakan warna khusus (Emerald).
- [x] Kode lama (SegmentedButton imports & usage) dibersihkan.
- [x] CI Build dan Unit Tests tetap passing.

## Skills
- Material 3 Compose UI Pattern: Penerapan Primary Tabs architecture (TabRow & Tab).
- UI Element Hierarchy Reordering.
