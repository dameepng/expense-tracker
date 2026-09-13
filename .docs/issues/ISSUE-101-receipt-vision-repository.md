# ISSUE-101: Repository Vision Receipt dan Prompt Strict JSON

**Status:** Done
**Priority:** High
**Type:** Feature - AI Repository
**Depends on:** ISSUE-097, ISSUE-098, ISSUE-099
**Overview:** [Receipt Scan](../TICKETS.md)

## Deskripsi

Tambahkan jalur Repository mengikuti pola AI existing: proses gambar, kirim image + prompt ke client bersama, parse kontrak receipt, dan kembalikan draft atau fallback yang dapat dijelaskan. Prompt harus memuat katalog kategori aktual dan tidak mengizinkan tebakan field wajib.

## Scope

- `ReceiptScanRequest`, `ReceiptRepository`, dan implementasi Claude yang menerima URI/processed image, reference date, dan kategori.
- Prompt JSON-only dengan aturan satu total struk, daftar item bounded, ISO date, kategori existing, dan error saat blur/crop/non-receipt/ambiguity.
- Reuse `AiDependencies.shared` dan `AiErrorMapper`; tidak ada repository transport paralel.
- Logging hanya metadata non-sensitif; image/base64, key, raw response, dan data pribadi tidak dilog.

## Files Touched (Estimasi)

- Baru: `data/ai/receipt/ReceiptRepository.kt`, `ClaudeReceiptRepository.kt`, `ReceiptPromptBuilder.kt`.
- Test: repository/prompt tests dengan fake client dan fixture response.

## Acceptance Criteria

- [x] Request mengandung image processed sekali, kategori aktual, reference date, dan instruksi JSON-only.
- [x] Success menghasilkan draft receipt dengan item dan satu total; error vision menghasilkan fallback tanpa draft palsu.
- [x] Invalid JSON/parser, offline, timeout, rate limit, auth, service, dan image error diteruskan ke error stabil.
- [x] Test memastikan kategori non-existing tidak dikirim sebagai pilihan dan prompt injection dalam teks struk diperlakukan sebagai data.
- [x] Tidak ada request live dalam unit test.

## Validasi dan Checkpoint

CI (`assembleDebug`, `testDebugUnitTest`, `lintDebug`) dan test prompt/parser/transport lulus; UI belum menampilkan hasil pada ticket ini.
