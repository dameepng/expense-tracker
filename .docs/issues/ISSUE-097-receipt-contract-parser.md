# ISSUE-097: Kontrak Data dan Strict Parser Receipt

**Status:** Done
**Priority:** High
**Type:** Feature - AI Receipt Foundation
**Depends on:** Baseline AI fase 1 tersedia
**Overview:** [Receipt Scan](../TICKETS.md)

## Deskripsi

Tetapkan kontrak type-safe untuk hasil vision receipt dan parser JSON strict yang aman terhadap field tambahan, nilai ambigu, dan respons yang meminta fallback manual. Kontrak harus memisahkan item struk dari `ParsedTransaction` tanpa mengubah schema Room pada ticket ini.

## Scope

- DTO response untuk `amount`, `category`, `merchant`, `date`, `note`, `items`, `is_recurring`, atau objek error terkontrol.
- Validasi positive integer, ISO date, panjang merchant/note/item, jumlah item, serta kategori yang berasal dari katalog request.
- Pemetaan kategori ke ID katalog existing dan konversi ke draft domain receipt.
- Error terstruktur untuk `unclear_receipt`, `not_a_receipt`, `missing_total`, dan respons JSON invalid.
- Tidak menyimpan, mengubah, atau memecah item menjadi transaksi pada ticket ini.

## Files Touched (Estimasi)

- Baru/ubah: `data/ai/receipt/ReceiptResponse.kt`, `ReceiptResponseParser.kt`, `ReceiptTransaction.kt`.
- Test: `app/src/test/.../data/ai/receipt/ReceiptResponseParserTest.kt`.

## Acceptance Criteria

- [x] JSON valid dengan field wajib dan item terbatas menghasilkan model domain dengan kategori existing yang tepat.
- [x] Field tambahan, duplicate key, tipe salah, angka non-positive, tanggal invalid, output terlalu panjang, dan item terlalu banyak ditolak sebagai `INVALID_RESPONSE`.
- [x] Error receipt yang didukung dipetakan ke alasan fallback yang dapat ditampilkan UI; tidak ada data parsial yang dipaksa menjadi transaksi.
- [x] Kategori yang tidak ada atau tidak sesuai type tidak lolos parsing.
- [x] Unit test mencakup JSON fenced, whitespace, error response, batas panjang, dan fixture PRD.

## Validasi dan Checkpoint

Test `ReceiptResponseParserTest` lulus. Belum mengubah UI, API transport, Room, atau manifest.
