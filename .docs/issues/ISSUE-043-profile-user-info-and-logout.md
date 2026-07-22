# ISSUE-043: Profile - User Profile Management & Logout Flow

## Deskripsi
Menghidupkan fitur personalisasi di Header Profil (ubah Nama/Foto) dan aksi "Keluar Akun" (Logout) di Danger Zone. Fungsionalitas ini akan mengatur *session* pengguna.

## Acceptance Criteria
- [ ] Menyimpan data profil (Nama, Foto URI, Status) di DataStore atau tabel Room khusus *User*.
- [ ] Membuat *Dialog* atau layar baru ketika ikon `Edit` di Header ditekan, yang memungkinkan input perubahan nama.
- [ ] Menghubungkan tombol "Keluar Akun" dengan fungsi `clearAllData()` atau menghapus *session* aktif.
- [ ] Memastikan navigasi diarahkan kembali ke layar *Onboarding / Login* (membersihkan *backstack*) setelah Logout berhasil.

## Skills
- Compose Dialog / Navigation routing.
- DataStore / Room Session Management.
