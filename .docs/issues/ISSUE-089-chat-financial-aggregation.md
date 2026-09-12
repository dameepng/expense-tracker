# ISSUE-089: Agregasi Keuangan dan Periode untuk AI Chat

**Status:** Done
**Priority:** High
**Type:** Feature - Data
**Depends on:** Baseline fase 1 tersedia pada branch implementasi
**Overview:** [AI Chat Assistant](../TICKETS.md)

## Deskripsi

Chat memerlukan angka pengeluaran nyata untuk menjawab total makanan, kategori terbesar minggu ini, dan perbandingan bulan. Siapkan agregasi reusable yang terpisah dari Claude dan UI, memakai query Room yang sudah ada bila sesuai.

## Scope

- Model snapshot keuangan, periode, total expense, dan breakdown kategori berdasarkan ID; nama kategori menjadi label.
- `TransactionAggregator`/period resolver dengan clock, timezone, dan awal minggu yang dapat dikontrol pada test.
- Data source Room untuk seluruh wallet, dengan snapshot konsisten bagi minggu berjalan, bulan berjalan, dan bulan kalender sebelumnya. Periode berjalan berakhir pada waktu snapshot; batas start inklusif dan end eksklusif.
- Filter transaksi `EXPENSE` secara eksplisit. Income maupun type tak dikenal tidak ikut expense. Kategori `BOTH` tidak menyebabkan hitung ganda.
- Hitung selisih nominal dan persentase perbandingan dari total expense nyata. Pembanding nol menghasilkan persentase absent/null.
- Pertahankan presisi `Long`; penjumlahan melewati kapasitas tidak boleh wrap menjadi angka salah. Baca dan hitung di luar main thread, dengan cancellation diteruskan.
- Tidak menambahkan entity chat, migrasi Room, fitur transfer, atau mengubah logika Summary di ticket ini.

## Files Touched (Estimasi)

Path source relatif terhadap `app/src/main/java/com/example/expense_tracker/`:

- Baru: `data/analytics/FinancialSummary.kt`, `FinancialSummarySource.kt`, `TransactionAggregator.kt`, `RoomFinancialSummarySource.kt`.
- Extend bila dibutuhkan untuk snapshot agregat yang konsisten: `data/ExpenseDao.kt`; reuse query dan pola `data/RoomSummaryRepository.kt` tanpa mengikat helper data ke UI.
- Test baru: `app/src/test/java/com/example/expense_tracker/data/analytics/TransactionAggregatorTest.kt`, `RoomFinancialSummarySourceTest.kt`.
- Referensi test yang ada: `app/src/test/java/com/example/expense_tracker/data/ExpenseDaoAggregationTest.kt`.

## Acceptance Criteria

- [x] Fixture expense makanan 30.000 + 20.000 dan income 100.000 menghasilkan expense makanan 50.000; income tidak masuk total/ranking.
- [x] Total dua wallet terakumulasi sekali; jumlah breakdown kategori sama dengan total pada periode/scope yang sama. ID kategori membedakan kategori bernama sama.
- [x] Start timestamp termasuk dan end timestamp tidak termasuk; future-dated transaction di luar snapshot tidak ikut.
- [x] Minggu mengikuti awal minggu locale yang diberikan; timezone device, pergantian bulan/tahun, Februari tahun kabisat, dan batas tanggal dengan perubahan offset lolos test deterministik.
- [x] Bulan berjalan 50.000 dan bulan sebelumnya 40.000 menghasilkan selisih +10.000 dan +25%; label/rentang membedakan periode berjalan dari bulan penuh.
- [x] Dataset kosong menghasilkan total nol dan breakdown kosong; pembanding nol tidak menghasilkan Infinity/NaN; overflow ditangani sebagai kegagalan terkontrol.
- [x] Semua agregat untuk satu request berasal dari snapshot data yang konsisten; perubahan transaksi terlihat pada pembacaan berikutnya.
- [x] Helper tidak membutuhkan Claude, network, Composable, raw merchant/description, atau informasi kartu wallet.

## Hasil Implementasi

- Model immutable untuk snapshot, periode, breakdown kategori, dan perbandingan bulan berada di `data/analytics/FinancialSummary.kt`.
- `FinancialPeriodResolver` memakai calendar boundary dari `java.time`, dengan `Clock`, `ZoneId`, locale, dan first-day-of-week yang dapat di-inject.
- `TransactionAggregator` membaca satu snapshot, memfilter `EXPENSE` secara eksplisit, menghitung total memakai `Math.addExact`, dan memakai `BigDecimal` untuk persentase.
- `RoomFinancialSummarySource` menjalankan satu query projection minimal pada dispatcher IO. Projection tidak memuat merchant, description, atau detail wallet.
- Test khusus ISSUE-089: 12 test, 0 failure. Seluruh unit test: 150 test, 0 failure/error/skipped.
- Validasi CI lokal: `.\gradlew.bat assembleDebug testDebugUnitTest lintDebug` berhasil pada 2026-09-12; lint menghasilkan 0 error.

## Validasi dan Checkpoint

Unit test agregasi dan Room serta pemeriksaan build/unit test/lint sesuai [workflow](../TICKETS.md#workflow-per-ticket) telah lulus. Setelah commit dan push, **STOP**. Ticket berikutnya: ISSUE-090.
