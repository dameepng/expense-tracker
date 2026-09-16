# PRD — Fitur Baca Kartu NFC E-Money

**Produk:** Aplikasi Catatan Keuangan (Android Native, Jetpack Compose)
**Fitur:** Scan kartu e-money via NFC untuk melihat saldo dan riwayat transaksi kartu
**Status:** Draft v1

---

## 1. Latar Belakang

Aplikasi catatan keuangan saat ini mengandalkan input manual dari user untuk mencatat pengeluaran. Sebagian besar transaksi harian masyarakat Indonesia (parkir, tol, transportasi umum, minimarket) dilakukan lewat kartu uang elektronik (Flazz, Brizzi, TapCash, e-Money Mandiri, JakCard) yang jarang dicatat manual karena repot. Fitur ini memungkinkan user menempelkan kartu ke HP untuk langsung melihat saldo dan riwayat transaksi yang tersimpan di chip kartu, tanpa perlu aplikasi bank terpisah.

## 2. Tujuan (Goals)

- User bisa cek saldo kartu e-money langsung dari aplikasi, tanpa app bank terpisah.
- User bisa melihat riwayat transaksi yang tersimpan di chip kartu (biasanya beberapa transaksi terakhir).
- Mendukung 5 jenis kartu sekaligus: Flazz (BCA), Brizzi (BRI), TapCash (BNI), e-Money (Mandiri), JakCard (DKI).

## 3. Non-Goals (di luar scope MVP)

- **Tidak** mencatat otomatis hasil scan ke ledger keuangan utama — riwayat kartu ditampilkan terpisah, murni sebagai referensi.
- **Tidak** ada fitur top-up / tulis ke kartu.
- **Tidak** melakukan sinkronisasi ke server bank manapun — semua data dibaca langsung dari chip kartu (offline, lokal).
- **Tidak** menjamin akurasi 100% untuk semua varian/versi kartu, mengingat format data hasil reverse-engineering komunitas (lihat Risiko §9).

## 4. Target User

Pengguna aplikasi catatan keuangan yang rutin memakai kartu e-money untuk transaksi harian dan ingin punya gambaran pengeluaran dari kartu tersebut tanpa harus cek manual lewat mesin ATM/reader di minimarket.

## 5. User Stories

| # | Sebagai user, saya ingin... | supaya... |
|---|---|---|
| US-1 | menempelkan kartu e-money ke HP | bisa langsung lihat saldo kartu |
| US-2 | melihat daftar transaksi terakhir dari kartu | tahu ke mana saja saldo saya terpakai |
| US-3 | tahu jenis kartu apa yang sedang di-scan | yakin aplikasi membaca kartu yang benar |
| US-4 | diberi tahu kalau kartu tidak didukung / gagal dibaca | tidak bingung kenapa scan tidak berhasil |
| US-5 | melihat riwayat scan sebelumnya (histori per kartu) | tidak perlu scan ulang tiap kali buka fitur ini |

## 6. Functional Requirements

### 6.1 Deteksi & Kompatibilitas Kartu
- FR-1: Aplikasi mendeteksi tag NFC saat kartu ditempelkan (foreground dispatch), menampilkan indikator "sedang membaca" secara real-time.
- FR-2: Aplikasi mengenali jenis kartu (Flazz/Brizzi/TapCash/e-Money/JakCard) berdasarkan signature/struktur data di kartu, lalu menampilkan nama bank/jenis kartu ke user.
- FR-3: Jika jenis kartu tidak dikenali atau HP tidak mendukung NFC, tampilkan pesan error yang jelas (bukan crash atau silent fail).

### 6.2 Baca Saldo
- FR-4: Menampilkan saldo terkini kartu setelah scan berhasil.
- FR-5: Saldo ditampilkan dalam format Rupiah standar.

### 6.3 Riwayat Transaksi dari Kartu
- FR-6: Menampilkan daftar transaksi yang tersimpan di chip kartu (biasanya 5–10 transaksi terakhir, tergantung jenis kartu).
- FR-7: Setiap entri transaksi menampilkan minimal: tanggal/waktu, nominal, jenis transaksi (debit/kredit) — merchant/lokasi ditampilkan jika tersedia di data kartu (tidak semua kartu menyediakan ini).
- FR-8: Riwayat transaksi dari kartu ditampilkan di layar terpisah ("Riwayat Kartu"), **tidak** otomatis masuk ke ledger keuangan utama aplikasi.

### 6.4 Histori Scan Lokal
- FR-9: Setiap hasil scan (saldo + riwayat transaksi) disimpan ke database lokal (Room), dikaitkan dengan identitas kartu (UID/nomor kartu), supaya user bisa lihat histori scan sebelumnya tanpa perlu tempel kartu ulang.
- FR-10: Jika kartu yang sama di-scan ulang, data lama untuk kartu tersebut diperbarui (bukan duplikat), dan transaksi baru yang belum tercatat sebelumnya ditambahkan ke histori lokal.

## 7. Non-Functional Requirements

- NFR-1: Proses scan-ke-tampil-hasil harus terasa cepat (idealnya di bawah 2 detik untuk kartu yang didukung penuh), tanpa membekukan UI.
- NFR-2: Semua operasi baca NFC dan parsing berjalan di background thread (coroutine), tidak di main thread.
- NFR-3: Data kartu (saldo, riwayat transaksi) hanya disimpan lokal di device, tidak dikirim ke server manapun — mengingat ini data finansial sensitif milik user.
- NFR-4: Aplikasi tetap berfungsi (fitur lain tidak terganggu) di device tanpa NFC — fitur ini disembunyikan/dinonaktifkan secara graceful, bukan menyebabkan crash.
- NFR-5: Mendukung Android dengan NFC minimal API 19, tapi UX predictive back dan optimasi terbaik ditargetkan untuk API 26+ (disesuaikan dengan min SDK app secara keseluruhan — perlu dikonfirmasi).

## 8. Pendekatan Teknis & Rekomendasi

### 8.1 Rekomendasi pendekatan parsing (menjawab open question kamu)

**Rekomendasi: pakai referensi dari implementasi open-source yang sudah ada sebagai acuan struktur data, lalu reimplementasi native di Kotlin** — bukan murni reverse-engineering dari nol, dan bukan juga copy-paste library pihak ketiga secara langsung. Alasannya:

- Format data kartu e-money Indonesia **sudah pernah di-reverse-engineer oleh komunitas** (ada beberapa proyek Flutter/Java yang mendokumentasikan struktur block MIFARE untuk Flazz, Brizzi, TapCash, e-Money, JakCard) — reverse engineering dari nol untuk 5 jenis kartu sekaligus akan memakan waktu jauh lebih lama dan berisiko salah tanpa hasil yang jelas lebih baik.
- Tapi library pihak ketiga yang ada kebanyakan berbasis Flutter, belum tentu terawat, dan kualitas/akurasinya bervariasi — jadi lebih aman dipakai sebagai **referensi struktur data** (block index mana berisi saldo, block mana berisi log transaksi), sementara implementasi pembacaan NFC dan parsing-nya ditulis native Kotlin supaya kamu punya kontrol penuh dan bisa audit/debug sendiri.
- Pendekatan ini juga lebih mudah untuk maintenance jangka panjang — kalau salah satu jenis kartu berubah format, kamu bisa isolasi & perbaiki parser-nya per jenis kartu tanpa bongkar keseluruhan sistem NFC.

### 8.2 Arsitektur (selaras dengan stack Compose native kamu)

```
UI (Compose)
  └─ ScanCardScreen, CardHistoryScreen, TransactionListScreen
ViewModel
  └─ CardScanViewModel (state: idle/scanning/success/error)
Domain
  └─ CardParser (interface) → FlazzParser, BrizziParser, TapCashParser, EmoneyParser, JakCardParser
  └─ CardReaderRepository
Data
  └─ NfcCardReader (wrapper IsoDep/MifareClassic)
  └─ Room: CardEntity, CardTransactionEntity
```

- Satu interface `CardParser` dengan implementasi berbeda per jenis kartu → memudahkan tambah/ubah dukungan kartu tanpa mengubah core NFC handling.
- Deteksi jenis kartu dilakukan di awal (berdasarkan ATS/signature MIFARE), baru arahkan ke parser yang sesuai.

## 9. Risiko & Asumsi

| Risiko | Dampak | Mitigasi |
|---|---|---|
| Format data kartu tidak resmi/tidak terdokumentasi, bisa berubah sewaktu-waktu | Fitur salah baca / berhenti berfungsi untuk kartu tertentu | Isolasi parser per jenis kartu, tambahkan validasi/error handling per parser, siapkan mekanisme "kartu tidak dikenal" yang graceful |
| Tidak semua jenis kartu punya struktur riwayat transaksi yang sama lengkapnya | Beberapa kartu mungkin cuma bisa nampilin saldo, bukan riwayat detail | Definisikan per kartu: level dukungan (saldo saja vs saldo+riwayat), tampilkan di UI kartu mana yang didukung penuh |
| Variasi versi/varian kartu (kartu lama vs baru dari bank yang sama) | Parser bisa gagal di kartu versi tertentu | Uji dengan sampel kartu asli sebanyak mungkin sebelum rilis, beri fallback pesan error yang informatif |
| Data finansial sensitif | Concern privasi user | Pastikan data tidak pernah dikirim keluar device (NFR-3), state ini jadi bagian dari copy/UI supaya user paham datanya aman lokal |

**Asumsi:** Kartu yang di-scan adalah kartu asli milik user sendiri (bukan use case membaca kartu orang lain tanpa izin).

## 10. Alur UX (High-Level)

1. **Entry point** — tombol/menu "Scan Kartu E-Money" di aplikasi.
2. **ScanCardScreen** — instruksi tempel kartu, indikator loading saat NFC aktif mendeteksi tag.
3. Setelah scan berhasil → **CardResultScreen** menampilkan jenis kartu, saldo, dan tombol "Lihat Riwayat Transaksi".
4. **TransactionListScreen** — daftar transaksi dari kartu (terpisah dari ledger utama), dengan label jelas bahwa ini "riwayat dari kartu", bukan catatan keuangan resmi user.
5. **CardHistoryScreen** (opsional, akses dari menu) — daftar kartu yang pernah di-scan sebelumnya beserta saldo terakhir diketahui, tanpa perlu tempel ulang.

## 11. Data Model (Ringkas)

**CardEntity**
- `cardId` (UID kartu, primary key)
- `cardType` (Flazz/Brizzi/TapCash/Emoney/JakCard)
- `lastKnownBalance`
- `lastScannedAt`

**CardTransactionEntity**
- `id`
- `cardId` (foreign key)
- `timestamp`
- `amount`
- `type` (debit/kredit)
- `merchantInfo` (nullable — tidak semua kartu punya data ini)

## 12. Milestone yang Disarankan

| Fase | Cakupan |
|---|---|
| Fase 1 | Infrastruktur NFC dasar (deteksi tag, baca UID) + dukungan penuh 1 jenis kartu (disarankan mulai dari kartu yang paling terdokumentasi baik, misalnya Flazz atau e-Money) |
| Fase 2 | Tambah parser untuk 4 jenis kartu lainnya satu per satu, uji tiap kartu dengan sampel asli |
| Fase 3 | CardHistoryScreen + penyimpanan lokal riwayat scan (Room) |
| Fase 4 | Polish UX (error states, empty states, loading states) + performance pass |

## 13. Open Questions

- Kartu mana yang mau diprioritaskan di Fase 1? (menentukan urutan development)
- Apakah min SDK aplikasi secara keseluruhan sudah fix, supaya bisa dipastikan predictive back & foreground dispatch NFC berjalan optimal?
- Apakah nanti user perlu opsi hapus histori scan kartu tertentu (privasi/data lama)?
