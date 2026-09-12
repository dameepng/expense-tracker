# ISSUE-096: QA Integrasi AI Chat dan Dokumentasi Penggunaan

**Status:** Todo
**Priority:** High
**Type:** QA - Integration
**Depends on:** ISSUE-089 sampai ISSUE-095
**Overview:** [AI Chat Assistant](../TICKETS.md)

## Deskripsi

Verifikasi alur Chat lengkap terhadap dataset yang diketahui dan pastikan perubahan shared client tidak merusak fase NL Input. Test unit penting dibuat bersama ticket pemiliknya; ticket ini memeriksa integrasi dan mencatat bukti hasil akhir.

## Scope

- Jalankan build, unit test, lint, dan skenario manual/emulator untuk integrasi navigasi, session, network error, privacy, serta empat contoh pertanyaan PRD.
- Siapkan fixture deterministik dengan waktu/currency yang dicatat: minggu/bulan berjalan berisi expense makanan 25.000 + 15.000 dan transport 10.000 pada dua wallet, income 1.000.000, serta bulan sebelumnya makanan 30.000 + transport 10.000. Semua transaksi berjalan harus berada sebelum waktu snapshot dan di minggu yang sedang diuji.
- Verifikasi wajib mencakup angka/payload melalui fake client dan perilaku aplikasi melalui test/manual pada perangkat atau emulator. Uji model live bersifat tambahan bila konfigurasi pengembangan tersedia, untuk menilai kualitas bahasa, relevansi follow-up, dan scope guard. Jangan menganggap output model selalu identik atau guard prompt sebagai jaminan mutlak.
- Catat hasil pada laporan QA dan dokumentasi penggunaan/setup Chat. Selaraskan bagian `AI_INPUT.md` yang masih menyebut income unsupported dengan perilaku kode existing.
- Perbaikan kecil dalam scope chat dapat disertakan bersama regression check; temuan besar dicatat sebagai follow-up issue dan tidak disembunyikan sebagai hasil lulus.

## Files Touched (Estimasi)

- Baru: `.docs/qa/ai-chat-assistant-test-report.md`, `AI_CHAT.md`.
- Ubah: `README.md`, `AI_INPUT.md`, serta status issue/overview yang benar-benar sudah selesai.
- Extend bila ada gap integrasi: test pada `app/src/test/java/com/example/expense_tracker/data/ai/chat/`, `ui/chat/`, dan `app/src/androidTest/java/com/example/expense_tracker/ui/chat/`.
- File implementasi hanya bila ada bug chat kecil yang ditemukan dan dijelaskan pada laporan; bukan refactor luas di luar scope.

## Acceptance Criteria

- [ ] `assembleDebug`, `testDebugUnitTest`, dan `lintDebug` lulus; test otomatis memakai fake dan tidak membutuhkan key API.
- [ ] Dengan fixture di atas, context/request untuk pertanyaan makanan bulan ini berisi total 40.000; kategori terbesar minggu ini makanan; total expense bulan berjalan 50.000, bulan sebelumnya 40.000, delta +10.000/+25% dengan periode jelas. Jika live dijalankan, angka jawaban juga dibandingkan dengan fixture ini.
- [ ] Request follow-up "gimana kalau dibanding bulan lalu?" mempertahankan history topik/kategori sebelumnya dan breakdown kedua bulan; income tidak masuk expense. Relevansi jawaban model hanya dilaporkan terverifikasi bila diuji live.
- [ ] Prompt/context untuk pertanyaan transaksi aneh membatasi interpretasi pada agregat tersedia dan mengharuskan pengakuan data belum cukup untuk detail transaksi. Jika live dijalankan, cek bahwa jawaban tidak menyebut merchant/transaksi yang tidak ada di context.
- [ ] Dataset kosong, pembanding nol, periode di luar cakupan, perubahan data di antara dua send, dan history/context mencapai batas memberi perilaku sesuai ticket pemiliknya.
- [ ] Offline, timeout, rate limit, authentication, konfigurasi kosong, respons invalid, retry, cancel, dan reset tidak menyebabkan crash, loading macet, atau bubble ganda.
- [ ] Navigasi, rotasi, exit/reopen, keyboard, light/dark, dan Indonesia/Inggris diverifikasi; NL Input expense/income serta pencatatan manual offline tetap bekerja.
- [ ] Payload fixture memverifikasi minimisasi data, aturan off-topic, dan perlakuan input mirip prompt injection sebagai data. Jika live tersedia, uji skenario tersebut pada model dan catat hasil aktual; tanpa live, tandai perilaku model belum terverifikasi.
- [ ] Laporan menyebut tanggal, perangkat/emulator, konfigurasi model bila live, perintah, hasil, serta skenario belum dijalankan dan alasannya. Jangan centang validasi manual/live yang belum dilakukan.
- [ ] `AI_CHAT.md` menjelaskan entry point, setup konfigurasi bersama, scope wallet/periode, history in-memory, batas request, kebutuhan internet, keterbatasan anomali, dan cara test/review.

## Validasi dan Checkpoint

Jalankan `.\gradlew.bat assembleDebug testDebugUnitTest lintDebug`. Jika ada test instrumentasi, jalankan `.\gradlew.bat connectedDebugAndroidTest` dengan perangkat yang tersedia. Validasi otomatis dan manual aplikasi yang wajib harus mempunyai bukti hasil. Uji live yang tidak dijalankan dicatat beserta alasan dan keterbatasan verifikasinya; tidak menghalangi `Done` setelah seluruh pemeriksaan wajib lulus dan tidak boleh dilaporkan sebagai validasi perilaku model.

Setelah seluruh acceptance criteria terpenuhi, update laporan/status, commit, push, **STOP** sesuai [workflow](../TICKETS.md#workflow-per-ticket). Jangan melanjutkan Smart Receipt Scan atau AI Wrapped otomatis.
