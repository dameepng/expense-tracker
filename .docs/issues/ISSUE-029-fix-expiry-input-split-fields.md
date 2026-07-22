# ISSUE-029: Fix Expiry Date Input — Split MM and YY into Two Separate Fields

## Deskripsi
Input field Kadaluarsa (MM/YY) di `ModalBottomSheet` tambah wallet saat ini menggunakan satu `OutlinedTextField` dengan logika penyisipan karakter `/` secara otomatis. 

Masalah terjadi ketika user mengetik misal `0726`: angka `2` masuk setelah karakter `/`, sehingga teks yang terisi di field menjadi `07/62` bukannya `07/26`. Hal ini membingungkan dan merusak data kadaluarsa kartu.

## User Story
> Sebagai user, saya ingin memasukkan bulan (MM) dan tahun (YY) kadaluarsa kartu dengan mudah dan tanpa bug pengetikan, sehingga data kadaluarsa terisi dengan benar.

## Acceptance Criteria
- [ ] Field Kadaluarsa diubah dari 1 single field menjadi **2 input field berdampingan** (`MM` dan `YY`) dipisahkan oleh tanda `/`
- [ ] Field `MM`: max 2 digit angka (01-12), `KeyboardType.Number`
- [ ] Field `YY`: max 2 digit angka (contoh: 26, 28), `KeyboardType.Number`
- [ ] Otomatis berpindah fokus dari field `MM` ke field `YY` setelah 2 digit terisi di field `MM`
- [ ] Data yang disimpan ke database tetap berformat string `MM/YY` (contoh: `07/26`)

## Technical Details

### Root Cause
Di [WalletListScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/wallet/WalletListScreen.kt), penyisipan karakter `/` pada `onValueChange` di single field menyebabkan recomposition cursor yang mengacaukan urutan digit saat mengetik digit ke-3 dan ke-4.

### Fix
**[MODIFY]** [WalletListScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/wallet/WalletListScreen.kt)
- Ganti `OutlinedTextField` single expiry date menjadi Layout `Row`:
  ```
  [ MM Field ]  /  [ YY Field ]
  ```
- Gunakan `FocusRequester` untuk berpindah fokus secara otomatis dari field MM ke YY saat MM mencapai 2 digit.
- Saat menekan tombol Simpan, gabungkan nilai `"${expiryMonth}/${expiryYear}"`.

## Skills
- Jetpack Compose Focus Management: Penggunaan `FocusRequester` & `FocusManager` untuk auto-focus UX
- Form State & Input Validation: Pemisahan state input & formatting saat submit
- Material 3 Form Design: Mendesain layout form input berdampingan (inline fields) yang rapi
