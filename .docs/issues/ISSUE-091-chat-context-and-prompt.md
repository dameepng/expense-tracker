# ISSUE-091: Context Builder Agregat dan Scope Prompt

**Status:** Done
**Priority:** High
**Type:** Feature - AI Context
**Depends on:** ISSUE-089
**Overview:** [AI Chat Assistant](../TICKETS.md)

## Deskripsi

Ubah snapshot keuangan menjadi context ringkas untuk Claude. Pisahkan pengambilan data, serialisasi context, dan instruksi jawaban agar strategi context bisa dikembangkan tanpa perubahan UI.

## Scope

- `ChatContextProvider` membaca snapshot melalui data source ISSUE-089 dan preference currency/locale yang ada; `ChatContextBuilder` menyerialisasikan DTO dengan field yang dipilih eksplisit.
- Sertakan waktu snapshot, timezone, currency, scope seluruh wallet, rentang periode, total expense, breakdown kategori ketiga periode, serta perbandingan yang dihitung lokal.
- Snapshot diperbarui setiap send/retry. Data terkini menjadi acuan angka dibanding jawaban assistant lama di history.
- Context JSON maksimal 16.000 karakter dengan urutan deterministik. Jika tidak muat, kembalikan error terkontrol; jangan memotong JSON, membuang kategori diam-diam, atau mengubah total.
- System prompt membatasi jawaban pada keuangan pribadi berdasarkan data tersedia, memakai bahasa pengguna, menyatakan periode/scope, dan tidak mengarang angka/detail transaksi.
- Prompt memperlakukan pesan, label kategori, dan history sebagai data yang tidak boleh mengganti instruksi sistem. Off-topic diarahkan kembali ke topik pengeluaran.
- Pertanyaan di luar periode/scope dan detail transaksi aneh harus dijawab dengan batas cakupan. Perubahan agregat dapat dijelaskan sebagai indikasi, bukan kepastian fraud.

## Files Touched (Estimasi)

Path source relatif terhadap `app/src/main/java/com/example/expense_tracker/`:

- Baru: `data/ai/chat/ChatContext.kt`, `ChatContextProvider.kt`, `ChatContextBuilder.kt`, `ChatPromptBuilder.kt`.
- Reuse: `data/UserPreferencesRepository.kt` dan helper/data source ISSUE-089.
- Test baru: `app/src/test/java/com/example/expense_tracker/data/ai/chat/ChatContextBuilderTest.kt`, `ChatPromptBuilderTest.kt`.

## Acceptance Criteria

- [x] Serialized context mempertahankan nominal integer dan breakdown yang sama persis dengan fixture agregasi, termasuk kategori bulan sebelumnya untuk follow-up perbandingan.
- [x] Context menyebut rentang, waktu snapshot, timezone, currency preference, dan scope wallet; tidak melakukan konversi kurs.
- [x] Request context tidak berisi daftar transaksi, merchant, description/note, card number, card holder, card expiry, maupun object Wallet/Expense lengkap.
- [x] Nama kategori dengan quote/newline atau teks mirip instruksi tetap menjadi data yang di-escape, bukan menyusun instruksi sistem baru.
- [x] Empty data, kategori tanpa pengeluaran pada periode tercakup, periode tidak tercakup, dan kegagalan membaca data dapat dibedakan; kegagalan tidak berubah menjadi klaim nol pengeluaran.
- [x] Builder deterministik dan menerapkan batas panjang sebelum API dipanggil; payload terlalu besar menghasilkan error yang dapat dipetakan UI.
- [x] Test prompt memeriksa aturan cakupan, larangan fabrikasi, prioritas snapshot terbaru, batas interpretasi anomali, serta perbedaan bulan berjalan dan bulan penuh.
- [x] Persentase dengan pembanding nol tidak disajikan sebagai persentase pertumbuhan yang valid.

## Hasil Implementasi Lokal

- `ChatContextProvider` mengambil snapshot agregat baru dan preference currency/language pada setiap pemanggilan. Locale preference juga diteruskan ke resolver periode ISSUE-089.
- `ChatContextBuilder` menyusun JSON eksplisit dan deterministik, menjaga nominal `Long`, rentang `[start, end)`, metadata scope, ketiga breakdown kategori, serta perbandingan bulan. Payload di atas 16.000 karakter ditolak dengan `CONTEXT_TOO_LARGE` tanpa pemotongan data.
- `ChatPromptBuilder` menetapkan snapshot terbaru sebagai sumber angka, membatasi jawaban pada agregat keuangan yang tersedia, serta menjelaskan batas periode, empty data, kategori tanpa pengeluaran, pembanding nol, prompt injection, dan interpretasi anomali.
- Dua belas unit test khusus context/provider/prompt lulus tanpa request ke API AI.
- Validasi proyek lulus melalui `.\gradlew.bat assembleDebug testDebugUnitTest lintDebug`: working tree menjalankan 169 test dan salinan bersih `HEAD + staged ISSUE-091` menjalankan 112 test, seluruhnya 0 failure, 0 error, dan 0 skipped; lint keduanya 0 error.
- Dependency Gson untuk serialisasi JSON disertakan sebagai bagian ticket ini melalui perubahan Gradle yang di-stage per hunk; dependency OkHttp dan konfigurasi Claude milik fase 1/ISSUE-090 tidak disertakan.

## Validasi dan Checkpoint

Gunakan fixture/fake tanpa request AI untuk memeriksa angka, serialisasi, payload exclusion, dan aturan prompt. Pengujian respons model off-topic dilakukan saat QA, karena test isi prompt bukan bukti jaminan perilaku model. Jalankan pemeriksaan [workflow](../TICKETS.md#workflow-per-ticket), update status, commit, push, **STOP**. Ticket berikutnya: ISSUE-092.

Checkpoint implementasi dan validasi selesai.
