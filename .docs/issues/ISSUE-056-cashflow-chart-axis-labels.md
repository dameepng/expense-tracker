# ISSUE-056: Cash Flow Chart — Tambah Label Axis X & Y

**Priority:** Medium  
**Type:** UI Enhancement  
**Screen:** Summary (Ringkasan) → Cash Flow Insight Card

## Reference Design

![Reference - Chart with Axis Labels](file:///C:/Users/Adam/.gemini/antigravity-ide/brain/4ab7ebc6-077c-4859-a8c4-67a71f590b1d/media__1784858730369.png)

## Deskripsi

Chart Cash Flow saat ini hanya menampilkan garis income dan expense tanpa label pada sumbu X maupun Y. Hal ini membuat pengguna sulit memahami skala dan rentang waktu yang ditampilkan.

Tambahkan label axis agar chart lebih informatif dan mudah dibaca, sesuai referensi.

## Detail Implementasi

### 1. Axis Y (Kiri) — Skala Nilai
- Tampilkan **3–4 label** di sisi kiri chart secara vertikal (misal: `0`, `3k`, `6k`)
- Format menggunakan singkatan:
  - < 1.000 → angka biasa (misal `500`)
  - ≥ 1.000 → `1k`, `3k`, `6k`
  - ≥ 1.000.000 → `1jt`, `3jt`
- Tambahkan **garis horizontal tipis** (gridline) berwarna abu-abu transparan pada tiap level Y untuk membantu pembacaan

### 2. Axis X (Bawah) — Label Tanggal
- Tampilkan **3–4 label tanggal** di bawah chart secara horizontal
- Format tanggal ringkas: `1 Mei`, `10 Mei`, `20 Mei`, `31 Mei` (sesuaikan dengan periode yang dipilih)
- Distribusikan label secara merata sepanjang sumbu X (awal, tengah, akhir)
- Gunakan locale Indonesia untuk nama bulan

### 3. Layout Adjustment
- Berikan **padding kiri** pada area chart untuk mengakomodasi label Y
- Berikan **padding bawah** pada area chart untuk mengakomodasi label X
- Pastikan garis chart tetap sejajar dengan gridline Y

## File yang Dimodifikasi

| File | Perubahan |
|---|---|
| `CashFlowChart.kt` | Tambahkan drawText untuk axis X & Y, drawLine untuk gridline horizontal |

## Acceptance Criteria

- [ ] Axis Y menampilkan 3–4 label skala di sisi kiri chart
- [ ] Gridline horizontal tipis tampil pada tiap level Y
- [ ] Axis X menampilkan 3–4 label tanggal di bawah chart
- [ ] Label tanggal sesuai dengan periode filter yang aktif
- [ ] Format angka menggunakan singkatan (k, jt)
- [ ] Chart tetap responsif dan tidak terpotong
