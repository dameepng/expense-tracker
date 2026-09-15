# AI Chat Assistant - Ticket Implementasi Fase 2

**Status:** In Review; QA otomatis ISSUE-096 selesai, end-to-end menunggu hasil user.
**Tanggal:** 2026-09-12
**Sumber kebutuhan:** [PRD AI Chat Assistant](prd/prompt-ai-chat-assistant.md)
**Lokasi issue:** `.docs/issues/`, melanjutkan penomoran terakhir `ISSUE-088`.

Dokumen ini hanya merencanakan implementasi. Pembuatan backlog tidak berarti ticket implementasi sudah selesai. Nomor ISSUE di sini adalah nomor dokumen lokal, bukan nomor GitHub Issues.

## Urutan Pengerjaan

| Urutan | Ticket | Hasil yang bisa direview | Depends on | Status |
|---|---|---|---|---|
| 1 | [ISSUE-089: Agregasi keuangan dan periode](issues/ISSUE-089-chat-financial-aggregation.md) | Snapshot pengeluaran akurat beserta unit test | Baseline fase 1 tersedia | Done |
| 2 | [ISSUE-090: Shared client Claude](issues/ISSUE-090-shared-claude-client.md) | Client/config bersama; NL Input tetap berfungsi | Baseline fase 1 tersedia | Done |
| 3 | [ISSUE-091: Context builder dan scope prompt](issues/ISSUE-091-chat-context-and-prompt.md) | Context agregat terbatas dan aturan jawaban AI | 089 | Done |
| 4 | [ISSUE-092: Chat model dan repository](issues/ISSUE-092-chat-model-and-repository.md) | Request multi-turn melalui client yang sama | 090, 091 | Done |
| 5 | [ISSUE-093: Chat ViewModel dan state](issues/ISSUE-093-chat-viewmodel-and-state.md) | Session, loading, error, retry, dan cancellation | 092 | Done |
| 6 | [ISSUE-094: Chat screen Compose](issues/ISSUE-094-chat-screen-compose.md) | Bubble chat, input, loading, dan error UI | 093 | Done |
| 7 | [ISSUE-095: Navigasi dan informasi privasi](issues/ISSUE-095-chat-navigation-and-privacy.md) | Chat dapat dibuka dari Home dan scope data jelas | 090, 093, 094 | Done |
| 8 | [ISSUE-096: QA integrasi dan dokumentasi](issues/ISSUE-096-chat-integration-qa.md) | Bukti pengujian alur lengkap dan regresi fase 1 | 089-095 | In Review - E2E pending user |

ISSUE-089 sampai ISSUE-095 telah selesai. Baseline AI fase 1 dan ticket yang sebelumnya tertahan dicatat melalui catch-up commit terverifikasi sebelum pengerjaan ISSUE-096.

## Workflow Per Ticket

1. Tunggu user menyetujui breakdown atau meminta mulai ticket tertentu. Pada tahap penyusunan backlog ini, berhenti setelah dokumen selesai.
2. Saat mulai implementasi, ubah status satu ticket menjadi `In Progress` pada issue dan tabel ini. Kerjakan hanya scope ticket tersebut.
3. Jalankan validasi yang tercantum pada ticket. Sebelum commit penyelesaian, jalankan pemeriksaan yang dipakai CI: `.\gradlew.bat assembleDebug testDebugUnitTest lintDebug`. Jangan menyebut pemeriksaan berhasil jika tidak dijalankan atau gagal.
4. Setelah acceptance criteria dan validasi terpenuhi, centang kriterianya dan ubah status menjadi `Done`. Stage hanya file milik ticket dan pembaruan statusnya; jangan memakai `git add .` atau mengikutsertakan perubahan lain.
5. **Otomatis commit lalu push ke GitHub untuk ticket tersebut**, sesuai instruksi user. Contoh pesan commit: `feat(chat): add financial aggregation (ISSUE-089)`. Gunakan branch kerja dan tujuan push yang sudah diverifikasi; jangan force-push atau menggabungkan ticket berikutnya dalam commit yang sama.
6. Setelah push berhasil, berikan ringkasan perubahan, hasil validasi, cara review/test, commit SHA, dan branch/tautan commit GitHub. **STOP dan tunggu user bilang lanjut** sebelum mengerjakan ticket berikutnya.
7. Jika validasi, commit, atau push gagal, laporkan status sebenarnya dan penyebabnya. Jangan menandai seluruh workflow selesai atau lanjut ticket berikutnya. Jika commit sudah terbentuk tetapi push gagal, sebutkan bahwa commit masih lokal.

Otomatis commit/push di atas berlaku saat menyelesaikan **implementasi** setiap ticket. Pembuatan backlog awal sendiri tidak dihitung sebagai penyelesaian ticket implementasi.

## Temuan Codebase dan Prasyarat

- Fase 1 ada pada working tree, tetapi sejumlah file AI dan perubahan terkait masih modified/untracked saat breakdown dibuat. Sebelum implementasi chat, pastikan baseline fase 1 tersedia dalam riwayat branch yang akan dipakai agar commit chat dapat dibangun dari checkout bersih. Pencatatan prasyarat ini tidak mengizinkan memasukkan seluruh perubahan lama ke commit ticket chat.
- `data/ai/ClaudeApi.kt` sudah memiliki `ClaudeApi`, `OkHttpClaudeApi`, dan DTO request dengan list messages. Namun, constructor `ClaudeNaturalLanguageRepository` masih membuat transport baru; factory fase 1 belum memakai provider bersama. ISSUE-090 menangani gap ini.
- Konfigurasi sudah memakai `BuildConfig.ANTHROPIC_API_KEY` dan `BuildConfig.CLAUDE_MODEL`. Reuse konfigurasi ini; tidak perlu SDK, provider AI, atau konfigurasi model kedua.
- Pola aktual adalah Repository + ViewModel + factory manual. Belum ada AI UseCase terpisah. Ticket mengikuti pola aktual, tanpa mewajibkan layer tambahan yang hanya meneruskan pemanggilan.
- `ExpenseDao` sudah memiliki total dan breakdown kategori berbasis periode/type/wallet. `RoomSummaryRepository` dapat menjadi referensi; interface `SummaryRepository` saat ini berada di `ui/summary`, sehingga helper data baru tidak perlu bergantung pada package UI.
- `Expense.amount` berupa `Long`, timestamp berupa epoch millis, dan jenis transaksi saat ini hanya `EXPENSE`/`INCOME`. Tidak ada model transfer antar-wallet.
- `AI_INPUT.md` masih menyebut income tidak didukung, sedangkan implementasi fase 1 mendukung income. Penyelarasan dokumentasi dicakup ISSUE-096.

Path Kotlin di atas relatif terhadap `app/src/main/java/com/example/expense_tracker/`.

## Asumsi MVP untuk Direview

| Bagian | Usulan cakupan |
|---|---|
| Strategi AI | Opsi A: agregasi lokal. Context provider dipisahkan agar nanti bisa diganti strategi tool use tanpa merombak UI. |
| Scope wallet | Seluruh wallet; tampilkan scope ini pada chat. Pemilihan wallet khusus belum masuk MVP chat. |
| Data | Total expense dan breakdown kategori untuk minggu berjalan, bulan berjalan, dan satu bulan kalender sebelumnya. Tidak mengirim seluruh transaksi. |
| Periode | Timezone device, awal minggu mengikuti locale aplikasi yang aktif. Minggu/bulan berjalan sampai waktu snapshot; bulan sebelumnya memakai rentang kalender penuh. Rentang `[start, end)` ditulis eksplisit. |
| Perbandingan | Bandingkan angka nyata dari dua periode; sebutkan bulan berjalan belum lengkap. Jika pembanding nol, persentase tidak dihitung. |
| Currency | Nominal integer sesuai data yang tersimpan; sertakan preferensi currency aplikasi sebagai metadata tampilan. Tidak ada konversi kurs atau currency per transaksi. |
| Anomali | AI hanya boleh menjelaskan perubahan agregat yang tersedia. Detail transaksi mencurigakan tidak dapat dipastikan dari context ini; jawaban harus menyebut keterbatasan tersebut. Deteksi anomali per transaksi ditunda. |
| History | In-memory selama navigation entry chat masih hidup; rotasi mempertahankan ViewModel. Back yang membuang entry, reset session, atau process death memulai chat kosong. |
| Batas awal | Input maksimal 1.000 karakter; request menyertakan maksimal 6 pasangan percakapan selesai terbaru dengan total history 12.000 karakter, plus pertanyaan baru; context JSON maksimal 16.000 karakter; output maksimal 1.024 token. Ini batas aplikasi, bukan klaim batas layanan. |
| Pemangkasan | History dipangkas per pasangan utuh. Context tidak dipotong sebagai string/JSON mentah; jika tidak muat, tampilkan error terkontrol. Tidak menyamarkan data parsial menjadi lengkap. |
| Entry point | FAB berlabel Chat AI pada Home; route khusus chat dengan bottom bar disembunyikan. Akses Catat dengan AI tetap tersedia. |
| Jawaban | Teks biasa, satu respons lengkap per submit. Loading sampai selesai; streaming belum masuk MVP. |
| Privasi | Request berisi pertanyaan, history yang dibatasi, dan agregat. Metadata transaksi mentah maupun detail kartu wallet tidak ditambahkan oleh context builder. Teks yang diketik user tetap dikirim sebagai pesan. |

Batas panjang, entry point, scope wallet, dan perilaku session di atas adalah keputusan usulan untuk membuat ticket konkret; dapat dikoreksi saat review sebelum implementasi. Unit test menggunakan fake client sehingga tidak membutuhkan API key atau request berbayar. Scope guard berbasis prompt perlu diuji, tetapi tidak menjamin penolakan di luar topik selalu berhasil.

## Keterlacakan Requirements

| Kebutuhan PRD | Ticket |
|---|---|
| ChatMessage, ChatRepository, reuse AI client | 090, 092 |
| Agregasi akurat dan context yang bisa dikembangkan | 089, 091 |
| Multi-turn in-memory | 092, 093 |
| Pembatasan topik dan jawaban berdasarkan data nyata | 091, 092, 096 |
| Bubble UI, input, loading, entry point | 094, 095 |
| Offline, timeout, rate limit, dan error lain | 090, 092, 093, 094, 096 |
| Unit test context-building | 089, 091; verifikasi integrasi pada 096 |
| Satu ticket selesai, commit, push, lalu stop | Workflow berlaku untuk 089-096 |

Tool use/function calling, penyimpanan history di Room, Smart Receipt Scan, AI Wrapped, perubahan transaksi lewat chat, dan fitur perencanaan keuangan baru berada di luar scope fase ini.

## Fase: Receipt Scan

**Status fase:** Planned — backlog saja; belum ada implementasi.
**Sumber kebutuhan:** [PRD Smart Receipt Scan](prd/prompt-ai-receipt-scan.md)
**Lokasi issue:** `.docs/issues/`, melanjutkan penomoran dari `ISSUE-096`.

Keputusan MVP: satu gambar struk menghasilkan satu transaksi expense berdasarkan total struk. Item diekstrak dan ditampilkan pada layar review, lalu disimpan sebagai ringkasan terbatas pada note karena `Expense` belum memiliki kolom item; item tidak menjadi transaksi terpisah atau data yang dapat difilter. Kategori dikirim ke AI dari katalog Room dan hasilnya divalidasi terhadap ID/nama kategori yang tersedia. Struk buram, terpotong, bukan struk, atau field wajib ambigu menghasilkan fallback manual/NL Input.

| Urutan | Ticket | Hasil yang bisa direview | Depends on | Status |
|---|---|---|---|---|
| 1 | [ISSUE-097: Kontrak dan parser receipt](issues/ISSUE-097-receipt-contract-parser.md) | DTO type-safe dan parser strict untuk hasil vision | Baseline AI fase 1 | Done |
| 2 | [ISSUE-098: Claude multimodal transport](issues/ISSUE-098-receipt-multimodal-client.md) | Client bersama dapat mengirim content text + image | 097, shared client ISSUE-090 | Done |
| 3 | [ISSUE-099: Pemrosesan dan batas gambar](issues/ISSUE-099-receipt-image-processing.md) | URI menjadi payload base64 terkompresi dengan aman | Baseline Android image dependencies | Done |
| 4 | [ISSUE-100: Picker, kamera, permission, dan preview](issues/ISSUE-100-receipt-picker-permission-preview.md) | User memilih/mengambil foto dan melihat preview sebelum scan | 099 | Done |
| 5 | [ISSUE-101: Repository vision receipt](issues/ISSUE-101-receipt-vision-repository.md) | Prompt kategori + image menghasilkan draft atau fallback | 097-099 | Done |
| 6 | [ISSUE-102: ViewModel dan state scan](issues/ISSUE-102-receipt-scan-viewmodel.md) | State idle/loading/success/error, retry, cancel | 101 | Planned |
| 7 | [ISSUE-103: Review dan edit hasil scan](issues/ISSUE-103-receipt-review-screen.md) | Draft receipt dapat dikoreksi sebelum disimpan | 102; reusable UI dari AI Input | Planned |
| 8 | [ISSUE-104: Simpan transaksi dan ringkasan item](issues/ISSUE-104-receipt-save-transaction.md) | Confirm membuat satu Expense valid tanpa duplikasi | 103; Room repositories existing | Planned |
| 9 | [ISSUE-105: Entry point, navigasi, dan privacy](issues/ISSUE-105-receipt-navigation-privacy.md) | Receipt Scan dapat dibuka dari Home dan scope data dijelaskan | 100-104 | Planned |
| 10 | [ISSUE-106: QA integrasi receipt scan](issues/ISSUE-106-receipt-integration-qa.md) | Bukti test otomatis/manual, regresi, dan dokumentasi | 097-105 | Planned |

## Fase: UI/UX Polish Home dan Input

**Status fase:** Planned — backlog desain dan implementasi; belum ada perubahan kode dari ticket ini.

| Urutan | Ticket | Hasil yang bisa direview | Depends on | Status |
|---|---|---|---|---|
| 1 | [ISSUE-107: Hierarki aksi Dashboard/Home](issues/ISSUE-107-home-action-hierarchy-ui-polish.md) | Home memiliki entry point manual, AI input, receipt, dan Chat dengan hirarki visual yang jelas | 006, 019, 095, 105 | In Progress |
| 2 | [ISSUE-108: UX screen Tambah Transaksi (+)](issues/ISSUE-108-add-transaction-input-screen-ux-redesign.md) | Form input fokus, ringkas, keyboard-friendly, dan CTA sticky | 018, 019, 032, 105, 107 | Planned |

### Workflow Receipt Scan

1. Tunggu user menyetujui atau mengoreksi breakdown ini; belum boleh menulis kode implementasi.
2. Setelah disetujui, kerjakan tepat satu ticket per giliran dan tandai `In Progress` pada issue serta tabel ini.
3. Jalankan validasi ticket dan pemeriksaan CI `.\\gradlew.bat assembleDebug testDebugUnitTest lintDebug` sebelum menandai `Done`.
4. Stage hanya file ticket dan pembaruan statusnya. Jangan memasukkan perubahan working tree lain.
5. Setelah acceptance criteria terpenuhi, commit dengan pesan yang menyebut nomor issue lalu push ke GitHub.
6. Laporkan file, validasi, cara review, SHA, dan branch/tautan commit; lalu STOP sampai user meminta ticket berikutnya.
7. Bila validasi, commit, atau push gagal, laporkan keadaan sebenarnya dan jangan lanjut ke ticket lain.
