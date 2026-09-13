# ISSUE-100: Pilih/Kamera Receipt, Permission, dan Preview Awal

**Status:** Done
**Priority:** High
**Type:** Feature - Compose UI
**Depends on:** ISSUE-099
**Overview:** [Receipt Scan](../TICKETS.md)

## Deskripsi

Sediakan entry flow yang memungkinkan user memilih foto dari galeri atau mengambil foto baru, kemudian meninjau gambar sebelum menekan Scan. Gunakan Photo Picker/intent dan FileProvider yang sudah ada bila mencukupi; jangan menambah CameraX tanpa gap yang terbukti.

## Scope

- Komponen pilihan `Foto struk` dengan aksi Kamera dan Galeri.
- `ActivityResultContracts.PickVisualMedia` untuk galeri dan capture ke URI output aman untuk kamera.
- Runtime `CAMERA` permission, rationale, denial permanen, dan deep link Settings; galeri tetap dapat dipakai tanpa camera permission.
- Preview image, ganti foto, hapus, dan tombol Scan disabled jika belum ada gambar.
- Tambah manifest permission/provider/path hanya jika wiring existing belum cukup.

## Files Touched (Estimasi)

- Baru/ubah: `ui/receipt/ReceiptPickerScreen.kt`, component/state picker, `MainActivity.kt` atau navigation wiring, `AndroidManifest.xml`, resources.
- Test: Compose/UI contract test untuk source picker, denial, dan button state.

## Acceptance Criteria

- [x] Galeri dan kamera menghasilkan URI yang dapat dibaca processor tanpa crash.
- [x] Permission diminta hanya ketika kamera dipilih; denial memberi pesan jelas dan tombol Settings.
- [x] Preview mempertahankan aspect ratio, menangani cancel/result null, dan memungkinkan ganti gambar.
- [x] Scan belum memanggil AI; callback hanya menyerahkan URI setelah user menekan Scan.
- [x] Tidak ada akses storage permission legacy atau URI camera yang bocor tanpa grant.

## Validasi dan Checkpoint

Build, unit test, dan lint lulus; manual device test permission/camera disiapkan untuk ISSUE-106.
