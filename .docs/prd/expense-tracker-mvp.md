# PRD: Expense Tracker (MVP)

## Status
Final — 2026-07-04

## Problem Statement
Kebanyakan expense tracker app terlalu ribet — kategori kompleks, field wajib kebanyakan, butuh setup awal (budget, akun bank, dll) — sehingga user drop off setelah beberapa hari pakai. Inti masalahnya: mencatat pengeluaran harus SECEPAT MUNGKIN, kalau tidak orang malas dan berhenti pakai.

## Goals
- User bisa catat pengeluaran dalam hitungan detik, minim tap
- Kategori preset yang langsung kepake tanpa setup
- User bisa lihat ringkasan pengeluaran (hari ini/minggu ini/bulan ini) tanpa effort

## Non-Goals
- Bukan financial planning app lengkap (tidak ada budget goal, target nabung, investasi)
- Tidak ada multi-currency atau sync ke rekening bank
- Tidak ada fitur social/sharing
- Tidak ada cloud sync/backend di versi ini — local storage dulu

## Success Metrics
- **Speed**: rata-rata waktu dari buka app sampai expense tersimpan < 10 detik
- **Consistency**: user bisa nyatet berturut-turut tanpa gap lebih dari 1 hari (streak tracking)
- **Retention**: user masih aktif input expense setelah 7 hari pakai pertama

## User Stories

1. **As a user, I want to input expense hanya dengan nominal + kategori, so that saya tidak males nyatet.**
   - Acceptance Criteria:
     - [ ] Given user di home screen, when tap tombol "Tambah", then muncul input nominal + grid kategori preset
     - [ ] Given nominal & kategori sudah diisi, when user tap simpan, then expense tersimpan dan user kembali ke home screen dalam < 2 tap total
     - [ ] Given user belum isi nominal, when tap simpan, then tombol simpan disabled/tidak bisa ditekan

2. **As a user, I want memilih dari kategori preset yang sudah tersedia, so that saya tidak perlu setup kategori dulu.**
   - Acceptance Criteria:
     - [ ] Given app baru diinstall, when user pertama kali buka form tambah expense, then kategori preset (Makanan, Transport, Belanja, Hiburan, Tagihan, Kesehatan, Lainnya) sudah tersedia tanpa konfigurasi tambahan
     - [ ] Given user pilih satu kategori, when kategori lain di-tap, then hanya satu kategori yang ter-select (single select)

3. **As a user, I want melihat total pengeluaran hari ini/minggu ini/bulan ini, so that saya tahu kebiasaan belanja saya tanpa mikir keras.**
   - Acceptance Criteria:
     - [ ] Given ada expense tersimpan, when user buka home screen, then total pengeluaran hari ini ditampilkan di posisi paling menonjol
     - [ ] Given user switch tab/filter (hari ini/minggu ini/bulan ini), when filter dipilih, then total & list expense ter-update sesuai rentang waktu tsb
     - [ ] Given tidak ada expense di rentang waktu tsb, when filter dipilih, then tampilkan empty state yang jelas (bukan angka 0 doang)

4. **As a user, I want melihat breakdown pengeluaran per kategori, so that saya tahu kategori mana yang paling boros.**
   - Acceptance Criteria:
     - [ ] Given ada expense tersimpan di beberapa kategori, when user lihat summary, then tampil breakdown per kategori (list atau chart sederhana) untuk rentang waktu yang aktif

5. **As a user, I want melihat streak konsistensi nyatet, so that saya termotivasi tetap pakai app ini.**
   - Acceptance Criteria:
     - [ ] Given user input minimal 1 expense per hari, when hari berikutnya user buka app, then streak counter bertambah
     - [ ] Given user skip 1 hari tanpa input expense, when app dibuka, then streak reset ke 0

## Edge Cases & Constraints
- **Data storage**: Local only menggunakan Room DB — tidak ada backend/auth di versi ini
- **Offline-first**: Semua fitur harus berfungsi penuh tanpa koneksi internet (karena local-only)
- **Kategori preset bersifat fix** di MVP ini — tidak ada custom kategori dulu (deferred, lihat Out of Scope)
- **Timezone/tanggal**: Perhitungan "hari ini/minggu ini/bulan ini" mengikuti timezone device user
- **Data loss risk**: Karena local-only tanpa backup, perlu dipertimbangkan reminder/warning ke user soal risiko uninstall app = data hilang (bisa jadi item terpisah, dicatat di Out of Scope dulu)

## Out of Scope / Deferred
- Custom kategori buatan user
- Cloud sync / backup / multi-device
- Export data (CSV/PDF)
- Budget limit & notifikasi over-budget
- Input via foto struk / OCR
- Multi-currency
