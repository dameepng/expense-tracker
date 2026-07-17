# ISSUE-016: Notification Scheduler (WorkManager)

## Deskripsi
Implementasi background worker untuk mengirim notifikasi pengingat tagihan pada 4 tahap: awal bulan, H-7, H-3, dan hari-H.

## Acceptance Criteria
- [ ] `BillReminderWorker` dengan WorkManager
- [ ] Kalkulasi otomatis 4 jadwal notifikasi dari `dueDay`
- [ ] `NotificationHelper` dengan channel khusus "Bill Reminders"
- [ ] Notifikasi muncul di 4 titik waktu yang tepat
- [ ] CI GitHub Actions pipeline hijau

## Notifikasi Schedule
| Tahap | Kapan | Pesan |
|-------|-------|-------|
| Awal Bulan | Tgl 1 | "💡 Reminder: {nama} jatuh tempo tgl {dueDay}" |
| H-7 | dueDay - 7 | "⚠️ H-7: {nama} Rp{amount} jatuh tempo tgl {dueDay}" |
| H-3 | dueDay - 3 | "🔴 H-3: Segera bayar {nama} Rp{amount}!" |
| Hari-H | dueDay | "🚨 HARI INI: Batas pembayaran {nama}!" |

## Skills
- Clean Architecture, Clean Code, KISS, YAGNI, CI GitHub Actions
