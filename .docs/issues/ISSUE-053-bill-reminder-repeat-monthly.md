# ISSUE-053: Fitur Repeat Setiap Bulan pada Bill Reminder

## Deskripsi
Saat ini sistem *Bill Reminder* di aplikasi sudah berjalan dengan otomatis muncul kembali di bulan berikutnya setelah dibayar, tetapi tidak ada indikator visual atau opsi yang jelas bagi pengguna saat membuat reminder. Hal ini menyebabkan pengguna mengira mereka harus membuat *bill reminder* baru setiap bulannya.
Oleh karena itu, diperlukan sebuah sakelar (switch) atau opsi eksplisit "Repeat Every Month" (Ulangi Setiap Bulan) pada form pembuatan *Bill Reminder*. Jika opsi ini diaktifkan, tagihan akan berulang setiap bulan (seperti perilaku default saat ini). Jika opsi ini dimatikan, tagihan hanya bersifat sekali bayar (One-time) dan akan otomatis nonaktif setelah dibayar.

## User Story
> Sebagai pengguna, saya ingin ada opsi yang jelas untuk mengatur apakah sebuah tagihan itu rutin berulang setiap bulan atau hanya tagihan sekali bayar, sehingga saya tahu tagihan rutin akan tertagih otomatis bulan depan tanpa perlu membuatnya berulang kali.

## Acceptance Criteria
- [ ] Menambahkan *field* isRepeat: Boolean dengan default value true pada tabel bill_reminders di database Room.
- [ ] Menyediakan skrip migrasi database Room untuk menambahkan kolom isRepeat.
- [ ] Menambahkan komponen *Switch* dengan label "Repeat Every Month" (Ulangi Setiap Bulan) di ReminderFormScreen.
- [ ] Menampilkan indikator UI (misalnya teks "Berulang" atau "Sekali Bayar") pada *card* tagihan di ReminderListScreen agar pengguna dapat membedakan antara tagihan berulang dan sekali bayar.
- [ ] Mengubah logika saat pengguna menekan tombol "Bayar" di ReminderListViewModel:
  - Jika isRepeat == false (One-time): set isActive = false agar tagihan selesai dan tidak muncul lagi.
  - Jika isRepeat == true (Recurring): perbarui lastPaidMonth menjadi bulan saat ini seperti biasa.
