# AI Chat Assistant

AI Chat membantu membaca ringkasan pengeluaran yang sudah tersimpan di Kasflow. Fitur ini bersifat read-only: chat tidak membuat, mengubah, atau menghapus transaksi.

## Setup

AI Chat memakai konfigurasi dan transport Claude yang sama dengan Catat dengan AI. Tambahkan konfigurasi berikut ke `local.properties`:

```properties
ANTHROPIC_API_KEY=isi_api_key_anthropic_sendiri
CLAUDE_MODEL=claude-sonnet-4-6
```

`CLAUDE_MODEL` bersifat opsional dan menggunakan `claude-sonnet-4-6` sebagai default. Build tanpa API key tetap berhasil; Chat menampilkan error konfigurasi yang terkontrol. Detail keamanan key untuk build pengembangan dijelaskan di [AI_INPUT.md](AI_INPUT.md).

## Cara menggunakan

1. Buka Home dan ketuk FAB **Chat AI**.
2. Baca kartu cakupan data sebelum mengirim pertanyaan.
3. Pilih salah satu contoh atau tulis pertanyaan maksimal 1.000 karakter.
4. Kirim pertanyaan dan tunggu jawaban. Request dapat dibatalkan, lalu pesan dapat dicoba kembali atau dibuang.
5. Gunakan Reset untuk menghapus percakapan aktif. Back membuang navigation entry; saat Chat dibuka kembali, sesi dimulai kosong.

Contoh pertanyaan:

- `Berapa habis untuk makan bulan ini?`
- `Kategori apa yang paling boros minggu ini?`
- `Bandingkan pengeluaran bulan ini dengan bulan lalu.`
- `Ada perubahan pengeluaran yang tidak biasa akhir-akhir ini?`

## Data dan periode

Setiap request membangun snapshot baru dari seluruh wallet. Data yang tersedia hanya:

- Total expense dan breakdown kategori minggu berjalan sampai waktu snapshot.
- Total expense dan breakdown kategori bulan berjalan sampai waktu snapshot.
- Total expense dan breakdown kategori satu bulan kalender sebelumnya.
- Selisih nominal dan persentase bulan berjalan terhadap bulan sebelumnya. Persentase tidak dibuat bila total bulan sebelumnya nol.
- Metadata waktu snapshot, zona waktu, locale, dan preferensi mata uang untuk membaca angka. Tidak ada konversi kurs.

Daftar transaksi mentah, merchant, catatan, ID wallet, dan metadata kartu tidak ditambahkan ke context. Teks pertanyaan pengguna tetap dikirim ke Claude bersama maksimal enam pasangan percakapan selesai terbaru dan context agregat terkini.

## Session dan batas request

- History hanya berada di memory selama navigation entry Chat masih hidup.
- Rotasi mempertahankan ViewModel dan percakapan tanpa mengirim ulang request.
- Back, pembuangan navigation entry, process death, atau Reset menghasilkan sesi kosong.
- Maksimal enam pasangan user/assistant selesai dan 12.000 karakter history dikirim; pasangan lama dibuang utuh.
- Pertanyaan maksimal 1.000 karakter, context JSON maksimal 16.000 karakter, dan respons dibatasi 1.024 token.
- Hanya satu request aktif per sesi. Aplikasi tidak melakukan retry otomatis.

## Keterbatasan

Chat hanya dapat menjawab berdasarkan tiga periode dan agregat di atas. Pertanyaan tentang periode lain, merchant, transaksi individual, saldo, atau detail kartu tidak memiliki data pendukung. Untuk pertanyaan transaksi tidak biasa, model hanya diarahkan membahas perubahan agregat dan harus menyatakan bahwa data tidak cukup untuk menunjuk transaksi atau dugaan fraud tertentu.

Scope dijaga melalui system prompt dan minimisasi payload. Output model tetap dapat keliru; guard prompt bukan jaminan mutlak. Catat dengan AI dan AI Chat membutuhkan internet, sedangkan pencatatan manual tetap tersedia secara offline.

## Verifikasi

Jalankan pemeriksaan otomatis tanpa API key atau request berbayar:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
```

Test integrasi memakai fixture tetap dan fake `ClaudeApi` untuk memeriksa angka, payload multi-turn, refresh snapshot, minimisasi data, dan aturan prompt. Checklist QA end-to-end untuk perangkat terdapat di [.docs/qa/ai-chat-assistant-test-report.md](.docs/qa/ai-chat-assistant-test-report.md).
