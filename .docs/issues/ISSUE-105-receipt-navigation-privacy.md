# ISSUE-105: Entry Point, Navigasi, dan Informasi Privasi Receipt

**Status:** Planned
**Priority:** Medium
**Type:** Feature - Product Integration
**Depends on:** ISSUE-100 sampai ISSUE-104
**Overview:** [Receipt Scan](../TICKETS.md)

## Deskripsi

Integrasikan Receipt Scan ke entry point Home yang sudah memiliki Catat dengan AI/Chat, dengan route lifecycle dan informasi pengiriman gambar yang jelas.

## Scope

- Opsi `Ketik teks` / `Foto struk` dari entry point AI existing atau flow yang konsisten di Home.
- Route receipt picker → review → selesai/kembali, bottom bar policy, back cancellation, dan duplicate navigation guard.
- Informasi sebelum Scan: foto dan prompt dikirim ke Claude, kebutuhan internet, retensi in-memory, serta fallback manual.
- Update privacy policy Indonesia/Inggris tanpa mengklaim pemrosesan lokal atau akurasi sempurna.

## Files Touched (Estimasi)

- Ubah: `MainActivity.kt`, `ui/navigation/NavRoutes.kt`, `ui/home/HomeScreen.kt`, AI entry screen, privacy resources/screen.
- Baru/ubah: receipt destination wiring dan navigation tests.

## Acceptance Criteria

- [ ] User dapat membuka picker dari Home dan kembali tanpa merusak Catat dengan AI, Chat, atau input manual.
- [ ] Tap berulang tidak membuat destination ganda; Back membatalkan scan aktif dan membersihkan temporary state.
- [ ] Bottom bar/keyboard/insets tidak menutupi aksi Scan atau Confirm.
- [ ] Privacy copy menyebut image, prompt, kategori, kebutuhan network, dan fallback secara akurat.
- [ ] Tidak ada image/base64 atau history receipt yang disimpan ke Room.

## Validasi dan Checkpoint

Jalankan navigation/resource tests; smoke device lengkap dicatat ISSUE-106.

