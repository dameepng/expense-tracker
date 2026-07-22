# ISSUE-038: Profile - Data Management, Security, and Danger Zone UI

## Deskripsi
Menambahkan section menu untuk pengelolaan data, keamanan, dan aksi kritikal (Keluar akun / Hapus Data) pada halaman Profile.

## Acceptance Criteria
- [ ] Menambahkan grup menu "Keamanan & Data" di bawah grup Preferensi.
- [ ] Menambahkan menu "Export Data Transaksi (CSV/PDF)".
- [ ] Menambahkan menu "Biometric Login / PIN" dengan *trailing component* berupa `Switch` (Toggle) UI.
- [ ] Menambahkan tombol "Keluar Akun (Logout)" yang warnanya menonjol sebagai *Danger Zone* (misal: warna teks merah / `colorScheme.error`).

## Skills
- Compose Switch/Toggle UI integration.
- Semantic Color utilization (Penggunaan warna destruktif untuk aksi kritikal).
