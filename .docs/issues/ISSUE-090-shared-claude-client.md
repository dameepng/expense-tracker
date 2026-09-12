# ISSUE-090: Shared Client Claude, Konfigurasi, dan Error Mapping

**Status:** Done
**Priority:** High
**Type:** Refactor - AI Foundation
**Depends on:** Baseline fase 1 tersedia; dikerjakan setelah ISSUE-089 dalam urutan review
**Overview:** [AI Chat Assistant](../TICKETS.md)

## Deskripsi

Transport fase 1 sudah mendukung list messages, tetapi constructor repository membuat `OkHttpClaudeApi` baru. Siapkan provider manual DI yang dapat dipakai NL Input dan Chat dengan instance/config yang sama.

## Scope

- Satu provider bersama untuk transport `ClaudeApi`/`OkHttpClaudeApi` dan konfigurasi `BuildConfig` existing.
- Alihkan factory NL Input ke dependency bersama; sediakan injection untuk repository Chat pada ISSUE-092 tanpa membuat stub fitur chat.
- Ekstrak/reuse mapping error umum melalui `AiError` dan exception existing. Hindari salinan mapping pada setiap repository.
- Pertahankan strict JSON prompt/parser khusus NL Input di repository NL. Respons teks chat tidak perlu melewati parser transaksi.
- Pertahankan perilaku cancellation, timeout, pembatasan response body, dan retry transport existing. Tidak menambah SDK, endpoint, key, atau pilihan model terpisah.

## Files Touched (Estimasi)

Path source relatif terhadap `app/src/main/java/com/example/expense_tracker/`:

- Baru: `data/ai/AiDependencies.kt`, `data/ai/AiErrorMapper.kt` bila ekstraksi diperlukan.
- Ubah: `data/ai/ClaudeNaturalLanguageRepository.kt`, `ui/ai/NaturalLanguageViewModelFactory.kt`.
- Sesuaikan bila diperlukan: `data/ai/ClaudeApi.kt`, `data/ai/NaturalLanguageRepository.kt`.
- Test: `app/src/test/java/com/example/expense_tracker/data/ai/AiDependenciesTest.kt`, `AiErrorMapperTest.kt`, dan test AI existing yang terkena refactor.

## Acceptance Criteria

- [x] Consumer/factory berbeda memperoleh transport yang sama dari provider; model/key tetap berasal konfigurasi fase 1.
- [x] NL Input tetap dapat memproses expense dan income dengan kontrak parser existing; test parser, repository, transport, dan ViewModel fase 1 tetap lolos.
- [x] Mapping 401/403 ke authentication, 408/504 ke timeout, 429 ke rate limit, offline ke network, dan error layanan tetap konsisten.
- [x] Cancellation diteruskan sebagai cancellation dan membatalkan HTTP call; bukan ditampilkan sebagai kegagalan server.
- [x] Key kosong tidak memanggil API dan menghasilkan error konfigurasi yang dapat ditangani UI. Build/test tetap bisa tanpa key.
- [x] Key, body error mentah, dan data pengguna tidak ditambahkan ke log atau pesan error UI.
- [x] Tidak ada konstruksi transport kedua pada jalur factory aplikasi yang sudah dialihkan ke provider; seam injection fake tetap tersedia untuk test.

## Hasil Implementasi Lokal

- `AiDependencies` menyediakan satu konfigurasi BuildConfig dan satu transport Claude untuk seluruh proses aplikasi, dengan constructor internal untuk fake test.
- Factory NL Input memperoleh repository dari shared dependency container. Constructor repository yang membuat transport baru telah dihapus.
- `AiErrorMapper` memusatkan mapping kegagalan transport ke alasan stabil dan meneruskan cancellation tanpa perubahan.
- Prompt strict JSON, parser transaksi, timeout, retry policy, endpoint, dan batas response body fase 1 tidak berubah.
- Test AI: 55 test lulus. Seluruh unit test: 157 test, 0 failure/error/skipped.
- `.\gradlew.bat assembleDebug testDebugUnitTest lintDebug` berhasil pada 2026-09-12; lint menghasilkan 0 error.
- Baseline AI fase 1 dan refactor shared client dicatat bersama dalam catch-up commit sebelum QA integrasi.

## Validasi dan Checkpoint

Test AI fase 1, provider/error mapping, build, seluruh unit test, dan lint telah lulus. Implementasi selesai dan siap menjadi dependency QA integrasi.
