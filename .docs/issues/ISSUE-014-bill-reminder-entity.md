# ISSUE-014: Bill Reminder Entity & Data Layer

## Deskripsi
Membuat entity, DAO, dan repository untuk fitur Bill Reminder. Menjadi fondasi data layer untuk sistem pengingat tagihan bulanan.

## Acceptance Criteria
- [ ] Entity `BillReminder` dengan kolom: name, amount, dueDay, categoryId, walletId, isActive
- [ ] `BillReminderDao` dengan CRUD operations
- [ ] `BillReminderRepository` sebagai abstraksi akses data
- [ ] Database migration v6→v7
- [ ] Unit test: DAO CRUD operations
- [ ] CI GitHub Actions pipeline hijau

## Technical Details
### Entity
```kotlin
BillReminder(id, name, amount, dueDay: Int, categoryId, walletId, isActive, createdAt)
```

### DAO Operations
- `insertReminder()`, `updateReminder()`, `deleteReminder()`
- `getAllReminders()`, `getActiveReminders()`

## Skills
- Clean Architecture, Clean Code, KISS, YAGNI, CI GitHub Actions
