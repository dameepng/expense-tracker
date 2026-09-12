# ISSUE-095: Navigasi Chat, Session Lifecycle, dan Informasi Privasi

**Status:** Done
**Priority:** High
**Type:** Feature - Integration
**Depends on:** ISSUE-090, ISSUE-093, ISSUE-094
**Overview:** [AI Chat Assistant](../TICKETS.md)

## Deskripsi

Hubungkan ChatScreen ke aplikasi melalui FAB berlabel Chat AI pada Home. Jelaskan data yang dikirim dan pastikan lifecycle session cocok dengan navigasi aplikasi.

## Scope

- Tambahkan route chat di `NavRoutes` dan composable destination di `MainActivity` memakai factory/shared provider yang sudah dibuat.
- Tambahkan FAB pada Home dengan callback navigasi. Cegah duplicate navigation entry saat tap berulang; hindari FAB menutupi transaksi atau bottom nav.
- Sembunyikan bottom bar selama chat seperti pola screen AI Input existing. Back kembali ke Home; entry yang di-pop melepaskan session/call aktif.
- Pertahankan session pada rotasi selama navigation entry tetap hidup. Setelah entry dibuang atau process death, session baru kosong tanpa persist chat ke Room/SavedState.
- Jelaskan pada chat sebelum send bahwa pertanyaan, history terbatas, dan ringkasan keuangan dikirim ke layanan AI; sampaikan scope seluruh wallet dan keterbatasan periode secara ringkas.
- Perbarui bagian AI pada privacy policy Indonesia/Inggris untuk mencakup Chat. Ini informasi dalam flow existing, tanpa alur consent tambahan yang tidak diminta.

## Files Touched (Estimasi)

Path source relatif terhadap `app/src/main/java/com/example/expense_tracker/`:

- Ubah: `MainActivity.kt`, `ui/navigation/NavRoutes.kt`, `ui/home/HomeScreen.kt`, `ui/chat/ChatScreen.kt`.
- Sesuaikan wiring bila diperlukan: `ui/chat/ChatViewModelFactory.kt`, `data/ai/AiDependencies.kt`.
- Update resource chat dan resource pemilik `privacy_ai_content` pada `app/src/main/res/values/` dan `values-en/`; `ui/profile/PrivacyPolicyScreen.kt` hanya bila struktur section perlu berubah.
- Test navigasi/lifecycle bila diperlukan: `app/src/androidTest/java/com/example/expense_tracker/ui/chat/ChatNavigationTest.kt`.

## Acceptance Criteria

- [x] Dari Home, tap FAB membuka ChatScreen satu kali; Back kembali ke Home dan navigasi tab lain tetap berfungsi.
- [x] Catat dengan AI dan tambah transaksi manual tetap dapat diakses serta berfungsi seperti sebelumnya.
- [x] Chat tidak tertutup bottom bar/keyboard; FAB Home tidak menutupi aksi penting pada layar kecil.
- [x] Rotasi selama percakapan/request mempertahankan pesan tanpa mengirim ulang request; Back yang membuang entry membatalkan request aktif.
- [x] Membuka kembali chat setelah exit yang membuang entry memulai session kosong. Hasil request session lama tidak masuk ke session baru.
- [x] Informasi pengiriman data terlihat sebelum user mengirim pesan; privacy policy menjelaskan agregat + pertanyaan/history, bukan mengklaim semua pemrosesan selalu lokal.
- [x] Metadata kartu dan transaksi mentah tetap tidak ditambahkan ke request; tidak ada penyimpanan history baru di database.
- [x] Test deterministik chat dengan konfigurasi kosong/offline menampilkan state terkontrol; fitur transaksi manual tetap terkompilasi dan tidak bergantung pada layanan AI.

## Hasil Implementasi Lokal

- Route `chat` terhubung dari FAB berlabel Chat AI di Home. Guard terhadap destination aktif dan `launchSingleTop` mencegah entry ganda akibat tap berulang.
- Bottom bar disembunyikan pada Chat dan AI Input melalui satu kebijakan route yang diuji. Chat tetap memakai safe-area, navigation-bar, dan keyboard inset dari `ChatScreen`.
- FAB berada pada slot `Scaffold`; daftar transaksi mendapat ruang bawah 96 dp agar baris terakhir dapat digulir melewati FAB dan tidak tertutup.
- `ChatViewModel` dibuat dengan `NavBackStackEntry` Chat sebagai owner. Rotasi mempertahankan instance yang sama, sedangkan Back memanggil cancellation lalu mem-pop entry; pembuangan entry juga membatalkan `viewModelScope`. Factory tidak memakai `SavedStateHandle` dan tidak ada penyimpanan history ke Room.
- Destination membuat `ChatContextProvider` dari `RoomFinancialSummarySource`, preferensi aplikasi yang sama, dan `AiDependencies.shared`. Repository tetap dibuat oleh factory khusus Chat dengan transport/configuration bersama.
- Scope card sebelum input menjelaskan bahwa pertanyaan, maksimal enam pasangan percakapan selesai terbaru, dan ringkasan agregat dikirim ke Claude. Seluruh wallet serta tiga periode yang tersedia tetap terlihat bersama batas detail transaksi.
- Privacy policy Indonesia/Inggris kini membedakan data Catat dengan AI dan AI Chat, kebutuhan internet, batas history, data agregat, metadata yang dikirim, data mentah/kartu yang tidak ditambahkan, lifecycle history in-memory, dan ketersediaan input manual.
- `NavRoutesTest` menambahkan dua test untuk memastikan bottom bar tersembunyi pada kedua destination AI dan tetap tampil pada destination utama.
- `.\gradlew.bat assembleDebug testDebugUnitTest lintDebug` lulus pada 2026-09-13: 196 test, 0 failure, 0 error, 0 skipped; lint 0 issue.
- Test deterministik existing memverifikasi konfigurasi API kosong, network error, cancellation, reset session, serta pengabaian respons terlambat. Build penuh juga mengompilasi kembali AI Input dan input manual.
- Smoke test interaktif Home → Chat → follow-up → rotasi → Back → buka ulang belum dijalankan karena tidak ada perangkat/emulator terhubung. Validasi perangkat dicakup oleh ISSUE-096; tidak ada klaim request live.
- Implementasi integrasi dicatat bersama dependency chat yang sebelumnya belum berada pada baseline Git melalui catch-up commit sebelum QA integrasi.

## Validasi dan Checkpoint

Lakukan smoke test Home -> Chat -> follow-up -> rotasi -> Back -> buka ulang, serta regresi AI Input/manual input. Gunakan fake untuk jalur deterministik dan konfigurasi pengembangan yang ada untuk smoke test live bila tersedia. Jalankan pemeriksaan [workflow](../TICKETS.md#workflow-per-ticket), update status, commit, push, **STOP**. Ticket berikutnya: ISSUE-096.

Checkpoint implementasi dan validasi otomatis selesai. Validasi manual/emulator dilanjutkan pada ISSUE-096.
