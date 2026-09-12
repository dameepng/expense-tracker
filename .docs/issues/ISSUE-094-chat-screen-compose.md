# ISSUE-094: Chat Screen Compose, Bubble, dan Input Bar

**Status:** Done
**Priority:** High
**Type:** Feature - UI
**Depends on:** ISSUE-093
**Overview:** [AI Chat Assistant](../TICKETS.md)

## Deskripsi

Buat layar percakapan yang mengikuti tema Compose aplikasi, dengan pesan mudah dibaca dan input tetap terjangkau saat keyboard tampil. Ticket ini menyiapkan layar beserta preview; route aplikasi dihubungkan pada ISSUE-095.

## Scope

- `ChatScreen` dengan top app bar/back/reset session, daftar bubble user/assistant, serta input bar di bawah.
- Empty state dengan contoh pertanyaan dari PRD; tap contoh mengisi draft untuk dikirim user.
- Loading indicator, send disabled saat tidak valid/sedang memproses, cancel, dan error yang menyediakan aksi retry/batal sesuai state ViewModel.
- Nominal/jawaban tampil sebagai teks biasa. Gunakan string resource Indonesia/Inggris, tema terang/gelap, dan semantics aksesibilitas.
- Tangani keyboard inset, safe area, pesan panjang, serta scroll ke pesan baru dengan tetap memungkinkan membaca pesan lama.
- Tampilkan scope seluruh wallet/periode tersedia, batas data untuk detail transaksi, serta pemberitahuan saat history lama terpangkas.

## Files Touched (Estimasi)

Path source relatif terhadap `app/src/main/java/com/example/expense_tracker/`:

- Baru: `ui/chat/ChatScreen.kt`, `ui/chat/components/ChatMessageBubble.kt` bila pemisahan membantu keterbacaan.
- Baru: `app/src/main/res/values/chat_strings.xml`, `app/src/main/res/values-en/chat_strings.xml`.
- Preview menggunakan state/fake tanpa network; tambahkan test Compose perilaku bila diperlukan di `app/src/androidTest/java/com/example/expense_tracker/ui/chat/ChatScreenTest.kt`.

## Acceptance Criteria

- [x] Empty state, percakapan multi-turn, bubble berbeda role, dan pesan panjang tampil terbaca tanpa overflow pada layar kecil.
- [x] Input tetap terlihat saat keyboard terbuka; send/IME action mengikuti validasi 1.000 karakter dan loading state.
- [x] Loading terlihat saat menunggu; cancel berfungsi dan user dapat melanjutkan setelah request selesai/dibatalkan.
- [x] Network, timeout, rate limit, konfigurasi, service, respons invalid, dan masalah context/data mempunyai pesan yang jelas; retry tidak menggandakan bubble.
- [x] Pertanyaan contoh mengisi input; back/reset/aksi error terhubung ke callback atau ViewModel yang tepat.
- [x] Scroll memperlihatkan pesan terbaru saat user mengikuti akhir chat; user tetap dapat menelusuri history lama.
- [x] Teks statis tersedia dalam Indonesia/Inggris; ikon aksi memiliki label aksesibel; state error/loading dapat dikenali tanpa mengandalkan warna saja.
- [x] Preview/review visual mencakup light/dark, font besar, keyboard, empty/loading/error/success, dan pemberitahuan history terpangkas.

## Hasil Implementasi Lokal

- `ChatScreen` menghubungkan StateFlow ViewModel ke top app bar, back/reset, daftar pesan, loading/cancel, error retry/discard, serta input bar tetap di bawah.
- `ChatScreenContent` menerima state dan callback murni sehingga seluruh kondisi layar dapat direview lewat preview tanpa repository atau network.
- Empty state memuat empat contoh pertanyaan PRD yang mengisi draft. Scope card menjelaskan seluruh wallet, tiga periode yang tersedia, data agregat, serta batas detail transaksi.
- Bubble user/assistant memakai label role dan warna tema berbeda, membungkus teks panjang, mendukung text selection, serta menampilkan status mengirim/gagal sebagai teks dan ikon.
- Input memakai `imePadding` dan `navigationBarsPadding`; tombol serta IME send mengikuti `canSend` dan batas 1.000 karakter. Draft masih dapat diedit saat loading sementara request kedua tetap terkunci.
- Lazy list mengikuti pesan terbaru selama pengguna berada di ujung chat. Ketika pengguna menelusuri pesan lama, penambahan state tidak memaksa scroll kembali ke bawah.
- Semua kelompok error memiliki string Indonesia/Inggris dan live-region semantics. Back/reset/send mempunyai accessibility label; loading, error, dan truncation menyertakan teks sehingga tidak bergantung pada warna.
- Enam preview terkompilasi untuk empty light, conversation dark/success, long text dengan font besar pada layar kecil, loading dengan viewport pendek sebagai representasi keyboard, error, dan history truncated.
- Resource parity: 35 string default dan 35 string Inggris, tanpa key yang hilang. Lint tidak menemukan issue pada file chat.
- `.\gradlew.bat assembleDebug testDebugUnitTest lintDebug` lulus pada 2026-09-13: 194 test, 0 failure, 0 error, 0 skipped; lint 0 error.
- Tidak ada instrumented Compose test yang ditambahkan, sehingga tidak ada klaim pengujian emulator/perangkat pada ticket ini.
- Implementasi layar dicatat bersama dependency chat yang sebelumnya belum berada pada baseline Git melalui catch-up commit sebelum QA integrasi.

## Validasi dan Checkpoint

Review state layar melalui preview/test host dan test interaksi yang diperlukan. Jika test Compose dibuat, jalankan dengan emulator/perangkat dan laporkan hasil sebenarnya. Jalankan pemeriksaan [workflow](../TICKETS.md#workflow-per-ticket), update status, commit, push, **STOP**. Ticket berikutnya: ISSUE-095.

Checkpoint implementasi dan validasi selesai.
