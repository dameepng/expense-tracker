# ISSUE-106: QA Integrasi Smart Receipt Scan dan Dokumentasi

**Status:** Planned
**Priority:** High
**Type:** QA - Integration
**Depends on:** ISSUE-097 sampai ISSUE-105
**Overview:** [Receipt Scan](../TICKETS.md)

## Deskripsi

Verifikasi alur end-to-end receipt scan, regresi AI Input/Chat/manual input, batas privasi, dan perilaku perangkat. Catat bukti aktual; model live atau kamera tidak boleh diklaim teruji bila tidak dijalankan.

## Scope

- Fixture fake image/response untuk receipt jelas, blur, non-receipt, total ambigu, kategori unknown, item banyak, dan tanggal missing.
- Test build, unit, lint, state race/cancel, transport payload, image size, Room save, navigation, dan resource Indonesia/Inggris.
- Manual/emulator: gallery, camera permission allow/deny/Settings, rotate, back, retry, offline, keyboard, light/dark, duplicate confirm.
- Dokumentasi setup, batas image/item/note, trade-off satu transaksi, data yang dikirim, fallback, dan checklist review.

## Files Touched (Estimasi)

- Baru: `.docs/qa/receipt-scan-test-report.md`, `RECEIPT_SCAN.md`.
- Extend: tests pada `data/ai/receipt`, `ui/receipt`, navigation/instrumentation bila diperlukan; update status issue/TICKETS.

## Acceptance Criteria

- [ ] `.\\gradlew.bat assembleDebug testDebugUnitTest lintDebug` lulus; hasil dan tanggal dicatat.
- [ ] Fixture success memverifikasi total, existing category ID, merchant/date, item summary, dan tepat satu Expense.
- [ ] Fallback/error/cancel/offline/timeout/rate-limit/image-too-large tidak crash, loading macet, atau menyimpan data parsial.
- [ ] Camera/gallery permission, rotate, back, retry, duplicate tap, dan language/theme diuji atau ditandai belum dijalankan.
- [ ] Regression NL Input expense/income, Chat, manual input, dan privacy payload lulus.
- [ ] Dokumentasi menyebut hasil live model/device hanya jika benar-benar tersedia dan dijalankan.

## Validasi dan Checkpoint

Setelah bukti lengkap, update status ticket 097-106 sesuai kenyataan, commit hanya scope QA/status, push, lalu STOP.

