# ISSUE-093: Chat ViewModel, Session, Loading, dan Error State

**Status:** Done
**Priority:** High
**Type:** Feature - Presentation Logic
**Depends on:** ISSUE-092
**Overview:** [AI Chat Assistant](../TICKETS.md)

## Deskripsi

Kelola percakapan in-memory memakai pola StateFlow, coroutine, dan factory manual yang sudah digunakan fase 1. Pastikan request gagal atau dibatalkan tidak menggandakan pesan maupun merusak urutan percakapan.

## Scope

- `ChatUiState`: messages, draft input, loading, error, pending/failed message, dan status history yang dipangkas bila relevan.
- `ChatViewModel`: edit input, send, retry pesan gagal, cancel request, dan reset session. Hanya satu request aktif per session.
- Pesan user yang gagal tetap terlihat. Retry memakai pesan tersebut tanpa menambahkan bubble kedua; pesan gagal dapat dibatalkan sebelum mengirim pertanyaan lain.
- Error terpisah untuk konfigurasi, network, timeout, rate limit, authentication, service, invalid response, data loading, dan batas context/input.
- Cancellation/generation guard agar hasil request lama tidak mengubah session setelah cancel/reset. Batasi data history yang disimpan di state agar session panjang tidak tumbuh tanpa batas; gunakan kebijakan pasangan pada ISSUE-092 dan pertahankan draft/pending message.
- Factory menerima repository/provider yang ada. Lifetime ViewModel mengikuti navigation entry; detail integrasi lifecycle diselesaikan ISSUE-095.

## Files Touched (Estimasi)

Path source relatif terhadap `app/src/main/java/com/example/expense_tracker/`:

- Baru: `ui/chat/ChatUiState.kt`, `ChatViewModel.kt`, `ChatViewModelFactory.kt`.
- Test baru: `app/src/test/java/com/example/expense_tracker/ui/chat/ChatViewModelTest.kt`.
- Referensi pola: `ui/ai/NaturalLanguageViewModel.kt` dan test ViewModel fase 1.

## Acceptance Criteria

- [x] Input kosong/terlalu panjang tidak terkirim; double tap send tidak membuat dua request atau dua bubble user.
- [x] Success menambahkan satu jawaban assistant dengan ID/timestamp dan mengakhiri loading; follow-up membawa percakapan sebelumnya.
- [x] Failure mempertahankan pesan user dan error yang sesuai; retry sukses menambahkan satu jawaban tanpa duplikasi pesan user.
- [x] Send baru saat request aktif atau failure belum diselesaikan tidak merusak history; user bisa retry atau membatalkan pesan gagal.
- [x] Success, failure, dan cancellation semuanya mengakhiri loading. Cancel/reset membatalkan job; completion yang datang terlambat diabaikan.
- [x] Reset menghapus history/error session sebelumnya; state lama tidak muncul kembali akibat callback request terdahulu.
- [x] History yang terpangkas mengikuti pasangan utuh, mempertahankan pending message, dan mempunyai state pemberitahuan bahwa konteks lama sudah dilepas.
- [x] Unit test deterministik mencakup multi-turn, send ganda, semua kelompok error, retry, reset saat loading, serta late response setelah cancel.

## Hasil Implementasi Lokal

- `ChatUiState` menyimpan messages, draft input, loading, error bertipe, pending/failed message, serta indikator history yang sudah dipangkas. Derived state membatasi send dan retry sesuai kondisi session.
- `ChatViewModel` menangani edit input, send, retry tanpa bubble duplikat, cancel request, membuang failed message, dan reset session. Hanya satu request dapat aktif pada satu waktu.
- User message dibuat sekali dengan ID dan timestamp stabil. Failure/cancel mempertahankan bubble yang sama untuk retry; draft yang diketik selama loading tetap dipertahankan.
- Request generation guard dan pembatalan `Job` mencegah completion lama mengubah session setelah cancel/reset, termasuk repository fake yang sengaja tidak kooperatif terhadap cancellation.
- State percakapan memakai `ChatHistoryPolicy` ISSUE-092 sehingga hanya enam pasangan/12.000 karakter yang dipertahankan. Pending/failed message tetap disimpan di luar pasangan selesai dan indikator truncation bersifat sticky sampai reset.
- Semua alasan repository dipetakan ke kelompok UI terpisah untuk konfigurasi, input/limit, context/data, network, timeout, rate limit, authentication, service, dan invalid response.
- `ChatViewModelFactory` menerima `ChatRepository`, dengan overload internal untuk shared `AiDependencies` dan `ChatContextSource`; wiring navigation/lifecycle tetap menjadi scope ISSUE-095.
- Sepuluh unit test baru lulus. Seluruh unit test berjumlah 194 dengan 0 failure, 0 error, dan 0 skipped.
- `.\gradlew.bat assembleDebug testDebugUnitTest lintDebug` lulus pada 2026-09-13; lint menghasilkan 0 error.
- Implementasi ViewModel dicatat bersama dependency chat yang sebelumnya belum berada pada baseline Git melalui catch-up commit sebelum QA integrasi.

## Validasi dan Checkpoint

Gunakan coroutine test dispatcher, fixed clock bila diperlukan, dan fake repository. Jalankan pemeriksaan [workflow](../TICKETS.md#workflow-per-ticket), update status, commit, push, **STOP**. Ticket berikutnya: ISSUE-094.

Checkpoint implementasi dan validasi selesai.
