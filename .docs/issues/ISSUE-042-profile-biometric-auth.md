# ISSUE-042: Profile - Implement Biometric App Lock

## Deskripsi
Menghidupkan fungsionalitas "Kunci Layar (Biometric / PIN)". Ketika toggle ini diaktifkan, aplikasi akan meminta autentikasi sidik jari atau FaceID (tergantung hardware HP) setiap kali pengguna membuka aplikasi.

## Acceptance Criteria
- [ ] Menambahkan *dependency* `androidx.biometric:biometric` di `build.gradle`.
- [ ] Menyimpan state `isBiometricEnabled` ke dalam DataStore.
- [ ] Membuat fungsi *helper* `BiometricPromptManager` untuk memanggil sistem autentikasi bawaan Android.
- [ ] Menyambungkan logika *toggle on/off* di UI dengan prompt konfirmasi sidik jari (harus scan jari dulu untuk mengaktifkan).
- [ ] Menerapkan *auth check* di *entry point* aplikasi (misal: di `MainActivity` atau rute awal Navigasi) jika opsi ini diaktifkan.

## Skills
- AndroidX Biometric API.
- Android Lifecycle handling (Resume/Start app lock).
