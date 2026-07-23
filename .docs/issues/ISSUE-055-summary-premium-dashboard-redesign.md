# ISSUE-055: Summary Screen Redesign — Premium Dashboard Style

**Priority:** High  
**Type:** UI/UX Redesign  
**Screen:** Summary (Ringkasan)

## Reference Design

Redesign halaman Summary mengikuti gaya **premium finance dashboard** sesuai referensi visual berikut:

![Reference - Balance Card & Cash Flow](file:///C:/Users/Adam/.gemini/antigravity-ide/brain/4ab7ebc6-077c-4859-a8c4-67a71f590b1d/media__1784793568512.jpg)

## Design Breakdown

### 1. Total Balance Card (Hero Card)
Bagian paling atas — sebuah card dengan **dark gradient background** (navy/teal tones):
- Label "Total balance" dengan ikon eye (show/hide balance)
- **Nominal balance besar** (headline, bold, putih)
- Indikator perubahan: `↑ 6.4% vs bulan lalu` (warna hijau untuk naik, merah untuk turun)
- Background: subtle sparkline/mini area chart yang menampilkan trend saldo
- Ikon dekoratif (lingkaran dengan simbol uang) di kanan atas

> **Mapping ke app kita:**  
> - Total balance = saldo total dari semua wallet (atau wallet yang dipilih via filter)
> - Persentase perubahan = perbandingan saldo bulan ini vs bulan lalu
> - Sparkline = trend perubahan saldo harian selama periode yang dipilih

### 2. Cash Flow Insight Section
Di bawah hero card, section **"Cash flow insight"** dengan period picker ("This month"):
- **3 angka utama** sejajar horizontal:
  - `Income` (hijau) — total pemasukan
  - `Expenses` (gelap/merah) — total pengeluaran  
  - `Net cash flow` (biru/teal) — selisih income - expenses
- **Area/Line chart** yang menampilkan trend income & expenses sepanjang periode
  - 2 garis: satu untuk income, satu untuk expenses
  - Area di bawah garis diberi fill gradient tipis
  - Sumbu X: tanggal (May 1, May 10, May 20, May 31)
  - Sumbu Y: nominal (3k, 6k)

> **Mapping ke app kita:**
> - Income & Expenses = sudah ada datanya di DAO
> - Line chart = bisa menggunakan Canvas custom atau library chart ringan
> - Period = sudah ada filter Hari Ini / Minggu Ini / Bulan Ini

### 3. Upcoming Bills (Opsional — sudah ada di nav terpisah)
Card kiri bawah menampilkan bill reminder terdekat. **Bisa dipertimbangkan** untuk ditampilkan di summary sebagai quick preview, tapi ini opsional karena kita sudah punya tab Bill Reminder.

### 4. Spending by Category (Donut Chart + Legend)
Card kanan bawah menampilkan breakdown pengeluaran per kategori:
- **Donut chart** compact dengan warna-warna kategori
- **Legend** di samping kanan: nama kategori + persentase
- Menampilkan top 5 kategori

> **Mapping ke app kita:**
> - Sudah ada implementasi DonutChart + BreakdownCardItem
> - Perlu di-wrap ke dalam card yang lebih compact
> - Legend sudah diimplementasikan di ISSUE-054

## Scope Perubahan

### Harus Diimplementasikan
- [ ] **Hero Balance Card** — dark gradient card dengan total balance, persentase perubahan, dan sparkline
- [ ] **Cash Flow Summary** — 3 angka (income, expenses, net) secara horizontal
- [ ] **Cash Flow Line Chart** — area/line chart menampilkan trend income & expense
- [ ] **Spending by Category** tetap ada, di-improve layout-nya ke dalam card

### Opsional / Phase 2  
- [ ] Upcoming Bills preview di halaman summary
- [ ] Animasi sparkline pada hero card
- [ ] Toggle show/hide balance (eye icon)

## Technical Notes
- Hero card menggunakan `Brush.linearGradient` untuk background
- Sparkline & Line chart menggunakan custom `Canvas` composable
- Perlu query baru di DAO untuk mendapatkan data harian (daily aggregation) untuk chart
- Persentase perubahan perlu query pembanding antara bulan ini dan bulan lalu

## Acceptance Criteria
- [ ] Hero card dengan gradient, total balance, dan persentase perubahan
- [ ] Cash flow insight dengan 3 angka dan line/area chart
- [ ] Spending by category tetap ada dan lebih compact
- [ ] Tampilan terasa premium, modern, dan tidak flat
- [ ] Filter periode (Hari/Minggu/Bulan) dan wallet filter tetap berfungsi
