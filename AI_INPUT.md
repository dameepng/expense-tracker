# Natural Language Input — fase 1

## Setup

Tambahkan ke `local.properties` di root project (file ini sudah diabaikan Git):

```properties
ANTHROPIC_API_KEY=isi_api_key_anthropic_sendiri
CLAUDE_MODEL=claude-sonnet-4-6
```

Pertahankan `sdk.dir` yang sudah ada. `CLAUDE_MODEL` opsional; default-nya `claude-sonnet-4-6`. Lakukan Gradle Sync lalu rebuild setelah mengganti key/model. Tidak ada API key yang disertakan. Build tanpa key tetap berhasil; UI menampilkan bahwa AI belum dikonfigurasi dan menyediakan input manual.

Key diteruskan melalui `BuildConfig`, sehingga dapat diekstrak dari APK. Setup ini untuk pengembangan/pengujian pribadi; sebelum mendistribusikan APK dengan kredensial berbayar, pindahkan panggilan Claude ke backend berautentikasi dan implementasikan `NaturalLanguageRepository` untuk backend tersebut.

Dependency baru: OkHttp `4.12.0` dan Gson `2.13.2` melalui version catalog. Manifest menambahkan izin `INTERNET` (tanpa dialog runtime). Versi Kotlin/Compose/Room dan DI manual existing dipertahankan.

## Pemakaian

1. Home → **Catat dengan AI**.
2. Ketik satu pengeluaran, misalnya `makan siang di warteg 25rb`, `beli bensin 50000 kemarin`, atau `langganan netflix 120rb tiap bulan`.
3. Ketuk **Proses dengan AI**. Kalimat, kategori pengeluaran, tanggal, dan zona waktu perangkat dikirim ke Claude; riwayat transaksi dan detail dompet tidak dikirim.
4. Periksa/edit nominal rupiah, kategori, merchant, tanggal `yyyy-MM-dd`, catatan, recurring, dan dompet. Jika ada beberapa dompet, pilih secara eksplisit.
5. Ketuk **Konfirmasi & simpan**. Parsing tidak menulis transaksi ke Room.

Jika AI gagal, proses ulang atau pilih **Isi manual**. UI membedakan masalah koneksi, timeout, rate limit, autentikasi, konfigurasi, respons invalid, pemuatan data, dan kegagalan simpan. Request dapat dibatalkan dan tidak diulang otomatis.

## Konvensi dan batas fase ini

- Kategori berasal dari tabel `categories` melalui DAO existing (`EXPENSE`/`BOTH`). Contoh output menggunakan `Makanan`, sesuai kategori app; tidak membuat kategori `Makanan & Minuman` baru.
- Field JSON `note` disimpan sebagai `Expense.description`; `merchant` dan `isRecurring` menjadi kolom baru. Migrasi Room **11 → 12** mempertahankan data lama dengan default merchant kosong dan recurring false. Metadata dipertahankan saat edit dan undo delete.
- Tanggal relatif menggunakan tanggal/zona perangkat saat submit; `kemarin` dan `tadi pagi` yang tidak ambigu diperiksa kembali secara lokal. Tanggal pilihan disimpan sebagai awal hari pada zona perangkat, karena output fase ini berupa tanggal tanpa jam.
- `isRecurring` adalah penanda transaksi. Fase ini tidak membuat transaksi berikutnya atau `BillReminder` otomatis. Pengingat bulanan existing tetap dapat dibuat lewat form pengingat.
- Mendukung satu pengeluaran per request. Pemasukan, transfer, beberapa transaksi sekaligus, atau nominal/tanggal ambigu diarahkan untuk diperjelas/diisi manual. Voice-to-text dan fase 2–4 belum diimplementasikan.

## Struktur

- `ui/ai/`: screen Compose, draft dan state, ViewModel, serta factory DI manual.
- `data/ai/`: interface dan model request/hasil, Claude Messages transport, prompt, parser strict, resolver tanggal, dan repository simpan draft.
- `res/values/ai_strings.xml` dan `res/values-en/ai_strings.xml`: teks UI Indonesia/Inggris.
- `data/Expense.kt`, `AppDatabase.kt`, schema Room versi 12: penyimpanan metadata.
- Navigasi Home/MainActivity, tampilan transaksi, dan kebijakan privasi disesuaikan untuk fitur AI opsional.

`NaturalLanguageRepository` bisa diganti fake dalam test atau provider lain. `TransactionDraftRepository` memisahkan pilihan kategori/dompet dan commit Room dari ViewModel. JSON mentah hanya berada di batas transport/parser; ViewModel menerima `ParsedTransaction` yang bertipe.

## Verifikasi

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

Test mencakup parser dengan respons AI mock, validasi tipe/nilai/kategori/tanggal, resolusi tanggal relatif, error HTTP dan pembatalan, preview/edit/konfirmasi, pencegahan simpan ganda, persistensi, migrasi, serta metadata pada edit/undo. Panggilan Claude sungguhan memerlukan key; unit test tidak mengirim request berbayar.

Referensi API resmi: [Messages API](https://platform.claude.com/docs/en/api/overview) dan [Claude Sonnet 4.6](https://platform.claude.com/docs/en/models/sonnet-4-6/overview).
