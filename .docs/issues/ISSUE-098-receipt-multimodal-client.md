# ISSUE-098: Ekstensi Claude Client untuk Request Multimodal

**Status:** Planned
**Priority:** High
**Type:** Feature - AI Transport
**Depends on:** ISSUE-097 dan shared client ISSUE-090
**Overview:** [Receipt Scan](../TICKETS.md)

## Deskripsi

Perluas transport Claude bersama agar satu request dapat membawa blok teks dan image base64, sambil mempertahankan kontrak text-only NL Input dan Chat. API vision menerima struktur image source `base64`; batas payload aplikasi harus ditegakkan sebelum network call.

## Scope

- Model content block type-safe untuk text dan image (`media_type`, `data`) yang diserialisasi sesuai Messages API.
- Method API/repository seam untuk request receipt tanpa membuat `OkHttpClaudeApi` atau konfigurasi kedua.
- Batas ukuran encoded payload, timeout, cancellation, error mapping, dan redaksi log mengikuti client existing.
- Regression test memastikan request NL Input/Chat tetap menghasilkan JSON lama.

## Files Touched (Estimasi)

- Ubah: `data/ai/ClaudeApi.kt`, `data/ai/AiDependencies.kt`, `data/ai/AiErrorMapper.kt` bila diperlukan.
- Baru/ubah: DTO content multimodal dan test transport/fake di `app/src/test/.../data/ai/`.

## Acceptance Criteria

- [ ] Payload receipt berisi image block base64 dengan media type yang benar dan prompt text strict JSON.
- [ ] Payload text-only existing tidak berubah secara semantik dan seluruh test AI existing lulus.
- [ ] Key kosong, network, timeout, 429, auth, server, invalid response, dan cancellation dipetakan konsisten tanpa membocorkan body/key.
- [ ] Payload yang melewati batas ditolak sebelum request dikirim.
- [ ] Tidak ada instance transport/configuration baru pada jalur aplikasi.

## Validasi dan Checkpoint

Gunakan fake `ClaudeApi` untuk test; tidak ada API key atau request live yang diperlukan.

