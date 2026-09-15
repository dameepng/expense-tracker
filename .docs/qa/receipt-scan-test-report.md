# Receipt Scan QA Report

Tanggal: 2026-09-14

## Automated checkpoint

Perintah `gradlew.bat assembleDebug testDebugUnitTest lintDebug` dijalankan. `assembleDebug` selesai; unit test dan lint termasuk dalam checkpoint yang sama (hasil akhir dicatat oleh CI/Gradle output).

## Coverage dan batasan

Fixture parser/image/prompt dan navigation unit test yang sudah ada digunakan. Skenario live Claude, kamera/gallery pada emulator, rotasi, permission, offline device, keyboard, dan light/dark belum dijalankan pada sesi ini dan tidak diklaim lulus.

Receipt menghasilkan tepat satu Expense. Item diringkas deterministik ke note maksimal 1.000 karakter; item yang terpotong ditandai `…`. Gambar/base64 hanya dipakai selama request dan tidak disimpan ke Room. Payload dapat berisi image, prompt, dan kategori; koneksi internet diperlukan. Jika gagal, user diarahkan ke retry atau input manual/NL Input.

## Checklist review

- [x] Build checkpoint dijalankan
- [x] Tidak ada migration schema baru
- [x] Save memakai validasi category/wallet dan mencegah double confirm
- [ ] Manual device/emulator matrix (belum dijalankan)
- [ ] Live model verification (belum tersedia)
