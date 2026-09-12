# Laporan QA AI Chat Assistant

## Metadata

| Bagian | Nilai |
|---|---|
| Tanggal otomatis | 2026-09-13 |
| Zona kerja | Asia/Jakarta |
| Host | Windows, JDK 21 |
| Konfigurasi test | Fake `ClaudeApi`, API key dummy, model `test-model` |
| Request live | Tidak dijalankan; kualitas output model belum diverifikasi |
| QA perangkat | Diserahkan kepada user; hasil belum diisi |

## Fixture deterministik

Snapshot otomatis ditetapkan pada `2026-09-10T12:00:00+07:00`, zona `Asia/Jakarta`, locale Indonesia, dan currency `IDR`.

| Periode | Wallet | Tipe | Kategori | Nominal |
|---|---:|---|---|---:|
| 2026-09-07 | 1 | Expense | Makanan | 25.000 |
| 2026-09-08 | 2 | Expense | Makanan | 15.000 |
| 2026-09-09 | 2 | Expense | Transport | 10.000 |
| 2026-09-09 | 1 | Income | Gaji | 1.000.000 |
| 2026-08-10 | 1 | Expense | Makanan | 30.000 |
| 2026-08-20 | 2 | Expense | Transport | 10.000 |

Ekspektasi fixture:

- Minggu dan bulan berjalan: expense 50.000; Makanan 40.000; Transport 10.000.
- Kategori terbesar minggu berjalan: Makanan.
- Bulan sebelumnya: expense 40.000; Makanan 30.000; Transport 10.000.
- Perbandingan bulan: +10.000 dan +25%. Bulan berjalan berstatus parsial, bulan sebelumnya berstatus lengkap.
- Income 1.000.000 tidak masuk seluruh agregat expense.
- Dua wallet masuk agregat, tetapi ID wallet, merchant, note, dan metadata kartu tidak masuk payload.

## Hasil otomatis

`AiChatIntegrationTest` memeriksa empat contoh pertanyaan produk, angka fixture, rentang periode, scope seluruh wallet, dan payload minimal. Test follow-up memastikan urutan user/assistant dipertahankan, pertanyaan baru hanya muncul sekali, breakdown dua bulan tersedia, dan transaksi baru terlihat pada snapshot request berikutnya. Test prompt injection memastikan teks berbahaya tetap berada sebagai user message dan tidak mengganti system prompt.

Coverage regresi existing mencakup:

- Dataset kosong, pembanding nol, batas periode, timezone/locale, batas timestamp, perubahan data, overflow, dan context terlalu besar.
- History enam pasangan/12.000 karakter, input 1.000 karakter, pemangkasan pasangan, respons kosong/malformed/truncated, dan tidak ada retry otomatis.
- Offline, timeout, rate limit, authentication, service error, konfigurasi kosong, retry, cancel, reset, serta late response.
- NL Input expense/income, parser strict, edit/konfirmasi, penyimpanan Room, migrasi metadata, dan input manual.

Hasil pemeriksaan otomatis:

| Perintah | Hasil |
|---|---|
| `AiChatIntegrationTest` | Lulus: 3 test, 0 failure |
| `.\gradlew.bat assembleDebug testDebugUnitTest lintDebug` | Lulus: build sukses; 199 test, 0 failure/error/skipped; lint 0 issue |
| `.\gradlew.bat connectedDebugAndroidTest` | Tidak dijalankan; QA end-to-end dilakukan user |

## Checklist QA end-to-end untuk user

Gunakan perangkat/emulator dan catat model perangkat, versi Android, bahasa, tema, serta hasil aktual pada kolom Catatan.

| Status | Skenario | Ekspektasi | Catatan |
|---|---|---|---|
| [ ] | Home → Chat AI, tap FAB berulang | Hanya satu ChatScreen terbuka; bottom bar tersembunyi | |
| [ ] | Empat contoh pertanyaan fixture | Angka dan periode sesuai fixture; seluruh wallet disebut | |
| [ ] | Follow-up `gimana kalau dibanding bulan lalu?` | Topik sebelumnya dipertahankan dan bulan berjalan disebut parsial | |
| [ ] | Rotasi saat ada draft/percakapan/request | Draft dan pesan tetap ada; request tidak terkirim ulang | |
| [ ] | Back saat request, lalu buka Chat lagi | Request lama batal dan sesi baru kosong | |
| [ ] | Keyboard dan layar kecil | Input serta pesan terakhir tetap terjangkau | |
| [ ] | Mode terang/gelap dan Indonesia/Inggris | Teks terbaca dan label menggunakan locale aktif | |
| [ ] | Offline, konfigurasi kosong, retry/cancel/reset | Tidak crash, tidak loading macet, dan tidak ada bubble ganda | |
| [ ] | Privacy Policy | Menjelaskan pertanyaan/history/agregat, kebutuhan internet, dan history in-memory | |
| [ ] | Catat dengan AI: expense dan income | Preview benar, dapat diedit, dan baru tersimpan setelah konfirmasi | |
| [ ] | Input manual saat offline | Transaksi dapat disimpan tanpa layanan AI | |
| [ ] | Pertanyaan di luar periode/detail transaksi | Jawaban menyatakan data tidak tersedia dan tidak mengarang detail | |

## Batas hasil

Test fake membuktikan pembentukan context/request dan state aplikasi secara deterministik, bukan kualitas jawaban Claude. Request live tidak dijalankan, sehingga relevansi bahasa, penolakan off-topic, prompt injection, dan kepatuhan model terhadap batas anomali belum diverifikasi. Status akhir ISSUE-096 menunggu hasil checklist end-to-end dari user.
