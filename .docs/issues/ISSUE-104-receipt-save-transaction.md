# ISSUE-104: Persist Receipt sebagai Satu Expense

**Status:** Planned
**Priority:** High
**Type:** Feature - Persistence Integration
**Depends on:** ISSUE-103; Room repositories existing
**Overview:** [Receipt Scan](../TICKETS.md)

## Deskripsi

Hubungkan confirm receipt ke penyimpanan `Expense` existing. Total struk menjadi satu transaksi; item diserialisasi sebagai ringkasan bounded pada note bersama note user, karena schema tidak memiliki kolom item.

## Scope

- Extend interface/repository draft existing atau adapter receipt tanpa membuat jalur Room baru.
- Validasi ulang category/wallet dan amount di dalam transaction sebelum insert.
- Format ringkasan item deterministik, aman untuk note maksimal 1.000 karakter, dengan perilaku jelas saat item terpotong.
- Cegah double save, handle database failure, dan kembalikan saved state hanya setelah insert sukses.

## Files Touched (Estimasi)

- Ubah: `data/ai/RoomTransactionDraftRepository.kt`/interface terkait, model receipt-to-expense.
- Test: repository/database test untuk satu insert, item formatting, invalid wallet/category, retry, dan duplicate tap.

## Acceptance Criteria

- [ ] Confirm satu kali memasukkan tepat satu `Expense` dengan total, merchant, date, category, wallet, recurring, dan note yang benar.
- [ ] Item tidak menghasilkan beberapa transaksi; item summary dibatasi dan tidak menghapus note user secara diam-diam.
- [ ] Reconfirm/retry setelah sukses tidak membuat duplicate row.
- [ ] Category/wallet dihapus atau berubah menyebabkan save gagal terkontrol tanpa partial insert.
- [ ] Existing manual/NL Input save tests tetap lulus.

## Validasi dan Checkpoint

Jalankan Room/database dan AI regression tests; migration schema tidak boleh ditambah untuk MVP ini.

