# ISSUE-103: Review dan Edit Draft Receipt

**Status:** Planned
**Priority:** High
**Type:** Feature - Compose UI
**Depends on:** ISSUE-102; reusable UI AI Input tersedia
**Overview:** [Receipt Scan](../TICKETS.md)

## Deskripsi

Tampilkan hasil scan pada form konfirmasi yang konsisten dengan Natural Language Input. User harus bisa memeriksa dan memperbaiki nominal, kategori, merchant, tanggal, note, wallet, recurring, serta item sebelum menyimpan.

## Scope

- Ekstrak/parametrisasi bagian `TransactionPreview` AI Input bila perlu; hindari dua form dengan perilaku berbeda.
- Receipt image thumbnail dan daftar item read/edit dengan batas yang sudah ditetapkan.
- Banner confidence/fallback dan aksi Scan ulang atau Input manual/NL Input.
- Validasi field, keyboard, loading save, back/cancel, dan accessibility semantics.

## Files Touched (Estimasi)

- Ubah: `ui/ai/NaturalLanguageScreen.kt` bila ekstraksi dibutuhkan.
- Baru/ubah: `ui/receipt/ReceiptReviewScreen.kt`, components/state/resources.
- Test: Compose/state tests untuk edit, invalid field, fallback, dan item truncation.

## Acceptance Criteria

- [ ] Semua field yang akan disimpan terlihat dan dapat dikoreksi sebelum confirm.
- [ ] Kategori/wallet hanya memilih data yang tersedia dan valid untuk transaksi.
- [ ] Item tidak membuat transaksi tambahan; ringkasannya terlihat dan batas panjang ditandai.
- [ ] Fallback unreadable memberi jalur manual/NL Input yang berfungsi tanpa crash.
- [ ] Scan ulang mengganti draft lama dan tidak mempertahankan hasil stale.

## Validasi dan Checkpoint

Jalankan unit/Compose test; save ke Room dikerjakan ISSUE-104.

