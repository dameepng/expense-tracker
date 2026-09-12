# ISSUE-092: Chat Model dan Repository Multi-turn

**Status:** Done
**Priority:** High
**Type:** Feature - AI Repository
**Depends on:** ISSUE-090, ISSUE-091
**Overview:** [AI Chat Assistant](../TICKETS.md)

## Deskripsi

Hubungkan pertanyaan dan history dengan context terbaru melalui shared client Claude. Repository menghasilkan jawaban teks serta error bertipe yang dapat ditangani ViewModel.

## Scope

- `ChatMessage` dengan ID stabil, role user/assistant, content, dan timestamp; kontrak request/result serta interface `ChatRepository`.
- `ClaudeChatRepository` menerima client/config dari provider ISSUE-090 dan context provider ISSUE-091 melalui injection.
- Pertanyaan baru maksimal 1.000 karakter setelah normalisasi whitespace tepi; kosong/terlalu panjang ditolak sebelum network call.
- Kirim maksimal 6 pasangan user/assistant selesai terbaru, total history maksimal 12.000 karakter, lalu satu pertanyaan saat ini. Pangkas pasangan tertua secara utuh dan pertahankan urutan role.
- Sertakan system prompt dan context saat ini satu kali; jangan mengulang semua context lama di history. Respons output maksimal 1.024 token sesuai batas MVP.
- Proses blok teks respons chat secara eksplisit; respons kosong, tipe tak didukung, atau terpotong menjadi error terkontrol sesuai kebijakan yang diuji.
- Gunakan mapping error bersama, tambah alasan khusus data/context bila diperlukan. Retry hanya terjadi melalui aksi user di lapisan UI.
- Repository chat bersifat read-only terhadap data keuangan; tidak memanggil parser/save transaksi dan tidak menyimpan history ke Room.

## Files Touched (Estimasi)

Path source relatif terhadap `app/src/main/java/com/example/expense_tracker/`:

- Baru: `data/ai/chat/ChatMessage.kt`, `ChatRepository.kt`, `ClaudeChatRepository.kt`, `ChatHistoryPolicy.kt`.
- Extend wiring bila diperlukan: `data/ai/AiDependencies.kt`.
- Test baru: `app/src/test/java/com/example/expense_tracker/data/ai/chat/ClaudeChatRepositoryTest.kt`, `ChatHistoryPolicyTest.kt`.

## Acceptance Criteria

- [x] Pertanyaan pertama mengirim satu user message; follow-up menyertakan pesan user/assistant sebelumnya secara urut dan pertanyaan baru tepat sekali.
- [x] History yang gagal/belum mendapat jawaban tidak membentuk pasangan palsu; pemangkasan mempertahankan urutan valid dan batas count/karakter.
- [x] Follow-up setelah perubahan transaksi membaca snapshot terbaru; context lama tidak ikut dikirim ulang sebagai sumber angka utama.
- [x] Key kosong, input invalid, data gagal dibaca, atau context terlalu besar tidak memanggil API; alasan error dapat dibedakan dari data kosong.
- [x] Client/config yang digunakan sama dengan NL Input; parser transaksi tidak dipakai untuk jawaban chat.
- [x] Respons teks valid diterima; empty/malformed/truncated response, offline, timeout, rate limit, authentication, dan service error dipetakan tanpa body mentah ke UI.
- [x] Cancellation diteruskan dan tidak melakukan retry otomatis; tidak ada penulisan transaksi/history ke database.
- [x] Fake transport membuktikan payload hanya memuat instruksi, context agregat, dan pesan percakapan yang diperlukan, tanpa metadata sensitif tambahan dari database.

## Hasil Implementasi Lokal

- `ChatMessage`, `ChatRequest`, `ChatResult`, `ChatRepository`, dan error chat bertipe menyediakan kontrak yang dapat dipakai ViewModel tanpa membocorkan detail transport.
- `ChatHistoryPolicy` hanya memilih pasangan user/assistant lengkap, mempertahankan urutan, mengambil maksimal enam pasangan terbaru, dan membuang pasangan tertua secara utuh sampai total history maksimal 12.000 karakter.
- `ClaudeChatRepository` memangkas whitespace tepi pertanyaan, menerapkan batas 1.000 karakter sebelum context/network, mengambil context baru setiap request, mengirim output maksimum 1.024 token, dan memproses text blocks secara eksplisit.
- Empty, malformed, unsupported, dan truncated response dibedakan. Mapping network, timeout, rate limit, authentication, service, serta context/data menggunakan alasan stabil tanpa body mentah.
- `AiDependencies` menyediakan repository chat dengan configuration dan instance `ClaudeApi` yang sama dengan NL Input. Jalur chat tidak mengimpor Room/DAO, parser transaksi, maupun operasi save/insert.
- Lima belas test baru lulus. Test terarah chat/shared AI berjumlah 34; seluruh unit test berjumlah 184 dengan 0 failure, 0 error, dan 0 skipped.
- `.\gradlew.bat assembleDebug testDebugUnitTest lintDebug` lulus pada 2026-09-12; lint menghasilkan 0 error.
- Implementasi repository dicatat bersama dependency AI yang sebelumnya belum berada pada baseline Git melalui catch-up commit sebelum QA integrasi.

## Validasi dan Checkpoint

Test dengan fake `ClaudeApi` dan context provider, termasuk request kedua, batas history, respons invalid, dan cancellation. Tidak memerlukan API berbayar. Jalankan pemeriksaan [workflow](../TICKETS.md#workflow-per-ticket), update status, commit, push, **STOP**. Ticket berikutnya: ISSUE-093.

Checkpoint implementasi dan validasi selesai.
