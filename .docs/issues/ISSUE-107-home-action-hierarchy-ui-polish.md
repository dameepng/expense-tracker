# ISSUE-107: Polish Hierarki Aksi pada Dashboard/Home

**Status:** In Progress  
**Priority:** Medium  
**Type:** UI/UX Redesign  
**Depends on:** ISSUE-006, ISSUE-019, ISSUE-095, ISSUE-105  
**Estimated Effort:** Medium  

## Deskripsi

Rapikan dashboard/Home agar terlihat modern, simple, dan minimalist dengan memperjelas hirarki tiga aksi: pencatatan manual melalui `+`, pencatatan bahasa natural melalui **Catat dengan AI**, pemindaian melalui **Pindai struk**, serta pertanyaan read-only melalui **Tanya AI**.

Saat ini dua tombol full-width di bagian atas, extended FAB Chat AI, dan tombol `+` bersaing sebagai aksi utama. FAB Chat juga dapat menutupi empty state transaksi. Issue ini berfokus pada layout dan entry point tanpa mengubah perilaku bisnis atau kontrak AI/receipt.

## User Story

> Sebagai pengguna, saya ingin segera menemukan cara mencatat transaksi atau bertanya kepada AI tanpa merasa Home penuh oleh tombol yang sama-sama dominan.

## Scope

- Susun ulang Home dengan satu hirarki visual yang konsisten.
- Ubah shortcut pencatatan menjadi dua compact action/card yang setara: **Catat dengan AI** dan **Pindai struk**.
- Pertahankan tombol `+` pada NavigationBar sebagai entry point input manual.
- Ganti extended FAB **Chat AI** menjadi entry point inline berpenekanan lebih rendah, misalnya **Tanya AI**.
- Letakkan entry point Tanya AI di sekitar ringkasan atau section transaksi tanpa menutupi konten.
- Gunakan ikon yang membedakan fungsi: sparkle/AI untuk NL Input, camera/scan untuk receipt, chat untuk pertanyaan.
- Pertahankan aksesibilitas, state loading/error, navigasi, privacy copy, dan dukungan tema gelap/terang.

## Di Luar Scope

- Perubahan model data, repository, prompt, atau kemampuan Chat AI.
- Perubahan alur scan kamera/galeri dan review receipt.
- Penambahan fitur voice input atau transaksi melalui Chat AI.

## Files Touched (Estimasi)

- Ubah: `app/src/main/java/com/example/expense_tracker/ui/home/HomeScreen.kt`.
- Ubah bila diperlukan: `app/src/main/java/com/example/expense_tracker/ui/navigation/BottomNavBar.kt`, `MainActivity.kt`, dan resource string Indonesia/Inggris.
- Test/preview UI terkait Home dan navigasi bila tersedia.

## Acceptance Criteria

- [ ] Home tidak menampilkan dua CTA full-width dan extended FAB Chat AI yang sama-sama dominan.
- [ ] **Catat dengan AI** dan **Pindai struk** tetap dapat dibuka langsung dari Home dengan label dan ikon yang jelas.
- [ ] Tombol `+` tetap membuka input manual dan tidak kehilangan selected state pada NavigationBar.
- [ ] **Tanya AI** tetap mudah ditemukan, tidak menutupi empty state/list, dan tidak disalahartikan sebagai aksi membuat transaksi.
- [ ] Layout mempertahankan urutan informasi utama: header, saldo/wallet, ringkasan, aksi relevan, lalu transaksi.
- [ ] Semua target interaktif memiliki area tap minimum 48dp dan content description yang bermakna.
- [ ] Home tetap usable pada data kosong, banyak transaksi, keyboard/insets, ukuran layar berbeda, serta tema gelap/terang.
- [ ] Navigasi ke manual input, NL Input, receipt picker, Chat, wallet, summary, dan profile tidak berubah regresi.
- [ ] Preview/visual check menunjukkan spacing, radius, warna aksen, dan state enabled/disabled yang konsisten.

## Validasi dan Checkpoint

Jalankan preview/UI smoke test dan regression navigation yang relevan. Verifikasi manual pada empty state, populated state, dark/light theme, font scale, dan device width kecil. Jangan menandai issue `Done` sebelum hasil visual dan alur entry point dicatat.
