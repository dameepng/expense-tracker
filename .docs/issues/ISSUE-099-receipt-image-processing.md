# ISSUE-099: Kompresi, Resize, dan Normalisasi Gambar Receipt

**Status:** Done
**Priority:** High
**Type:** Feature - Image Pipeline
**Depends on:** Baseline dependency image existing
**Overview:** [Receipt Scan](../TICKETS.md)

## Deskripsi

Bangun pipeline deterministik dari `Uri` kamera/galeri menjadi JPEG yang terbaca, berorientasi benar, dan cukup kecil untuk vision request. Pipeline tidak boleh memuat bitmap tak terbatas atau meninggalkan file temporary.

## Scope

- Decode bounds, downscale target, orientasi EXIF, kompresi JPEG, dan encode base64 melalui `ContentResolver`.
- Batas dimensi, ukuran encoded, format MIME yang diterima, dan kegagalan decode/kompresi sebagai error domain.
- Resource closing dan cleanup file/stream; operasi berjalan di dispatcher IO.
- Test dengan fixture gambar kecil, MIME tidak didukung, URI invalid, dan gambar terlalu besar.

## Files Touched (Estimasi)

- Baru: `data/ai/receipt/ReceiptImageProcessor.kt`, model hasil/error.
- Test: `app/src/test/.../data/ai/receipt/ReceiptImageProcessorTest.kt` dan resource fixture bila perlu.

## Acceptance Criteria

- [x] Gambar valid menghasilkan JPEG base64 di bawah batas yang disepakati dan dimensi tetap terbaca.
- [x] Orientasi EXIF tidak memutar teks struk terbalik; resource selalu ditutup.
- [x] URI kosong/tidak dapat dibaca, MIME bukan image, decode gagal, dan hasil masih melewati batas menghasilkan error terkontrol.
- [x] Processing tidak berjalan di main thread dan tidak mengubah bitmap input asli.
- [x] Batas pipeline konsisten dengan batas transport ISSUE-098.

## Validasi dan Checkpoint

Unit test `ReceiptImageProcessorTest` lulus memakai fixture bitmap dan URI invalid; belum menambah screen atau permission.
