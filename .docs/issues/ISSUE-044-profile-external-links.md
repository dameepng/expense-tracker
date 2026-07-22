# ISSUE-044: Profile - External Intents (Help, Rating, Privacy Policy)

## Deskripsi
Menghidupkan tombol-tombol pada grup "Bantuan & Informasi". Saat tombol-tombol ini ditekan, aplikasi akan memanggil sistem Android (Intent) untuk membuka browser eksternal atau Google Play Store.

## Acceptance Criteria
- [ ] Menyambungkan tombol "Pusat Bantuan / FAQ" agar membuka URL web bantuan (contoh: *Web View* atau browser bawaan `Intent.ACTION_VIEW`).
- [ ] Menyambungkan tombol "Beri Rating Aplikasi" menggunakan `Intent` menuju Google Play Store (`market://details?id=com.example.expense_tracker`).
- [ ] Menyambungkan tombol "Kebijakan Privasi" menuju URL web kebijakan privasi.
- [ ] Menerapkan *try-catch* jika aplikasi terkait (seperti Play Store) tidak ditemukan di perangkat pengguna, dan menampilkan notifikasi Toast.

## Skills
- Android Intent Management.
- Context & Uri parsing di Compose.
