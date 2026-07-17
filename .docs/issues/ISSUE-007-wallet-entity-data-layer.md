# ISSUE-007: Wallet Entity & Data Layer

**Priority:** 🔴 High  
**Type:** Feature (Data)  
**Sprint:** Sprint 2  
**Estimated Effort:** Large  
**Depends On:** —

---

## Deskripsi

Membuat fondasi data layer untuk fitur Multi Wallet. Issue ini menambahkan entity `Wallet`, DAO, Repository, dan database migration agar aplikasi mendukung banyak wallet (contoh: BCA, Mandiri, Cash, dll).

## Kondisi Saat Ini

- Tidak ada konsep wallet — semua transaksi bersifat global tanpa pembeda sumber dana
- Database versi 3 dengan tabel `expenses` dan `categories`

## Perubahan yang Dibutuhkan

### Prinsip: Clean Architecture · Clean Code · KISS · YAGNI · CI GitHub Actions

### Data Layer

#### [NEW] `data/Wallet.kt`
```kotlin
@Entity(tableName = "wallets")
data class Wallet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,           // e.g. "BCA", "Mandiri"
    val balance: Long = 0L,     // Saldo awal
    val icon: String = "",      // Icon identifier (optional, YAGNI: cukup String)
    val color: String = ""      // Hex color (optional)
)
```
- **KISS:** Hanya field yang benar-benar dibutuhkan. Tidak ada `currency`, `type`, atau `bank_code`.
- **YAGNI:** `icon` dan `color` cukup String kosong default, bisa diisi nanti.

#### [NEW] `data/WalletDao.kt`
```kotlin
@Dao
interface WalletDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWallet(wallet: Wallet)

    @Delete
    fun deleteWallet(wallet: Wallet)

    @Query("SELECT * FROM wallets ORDER BY id ASC")
    fun getAllWallets(): List<Wallet>

    @Query("SELECT * FROM wallets WHERE id = :id")
    fun getWalletById(id: Long): Wallet?
}
```
- **Clean Code:** Naming konsisten dengan pattern DAO yang sudah ada (`ExpenseDao`).
- **KISS:** Hanya 4 operasi CRUD dasar. Tidak ada query kompleks yang belum diperlukan.

#### [NEW] `data/WalletRepository.kt`
```kotlin
interface WalletRepository {
    fun getAllWallets(): List<Wallet>
    fun getWalletById(id: Long): Wallet?
    fun insertWallet(wallet: Wallet)
    fun deleteWallet(wallet: Wallet)
}
```
- **Clean Architecture:** Interface terpisah dari implementasi, memudahkan testing dengan fake.

#### [NEW] `data/RoomWalletRepository.kt`
```kotlin
class RoomWalletRepository(private val dao: WalletDao) : WalletRepository {
    // Delegate semua ke DAO
}
```

#### [MODIFY] [AppDatabase.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/data/AppDatabase.kt)
- Tambahkan `Wallet::class` ke `entities` array
- Expose `abstract fun walletDao(): WalletDao`
- Bump version ke **4**
- Tambahkan `MIGRATION_3_4`:
  ```sql
  CREATE TABLE IF NOT EXISTS wallets (
      id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
      name TEXT NOT NULL,
      balance INTEGER NOT NULL DEFAULT 0,
      icon TEXT NOT NULL DEFAULT '',
      color TEXT NOT NULL DEFAULT ''
  )
  ```
- Seed callback: insert default wallet "Cash" agar existing user punya minimal 1 wallet

### Testing

#### [NEW] `test/data/WalletDaoTest.kt`
- In-memory Room database (Robolectric)
- Test `insertWallet` → `getAllWallets` returns inserted wallet
- Test `getWalletById` returns correct wallet
- Test `deleteWallet` removes wallet

#### [UPDATE] Existing tests harus tetap lolos

### CI GitHub Actions
- Pastikan `./gradlew assembleDebug` berhasil setelah migration
- Pastikan `./gradlew testDebugUnitTest` lolos termasuk `WalletDaoTest`
- Tidak ada perubahan pada `ci.yml` di issue ini — pipeline yang ada sudah mencakup build + test

## Acceptance Criteria

- [ ] Entity `Wallet` terdaftar di `AppDatabase`
- [ ] Migration v3 → v4 berjalan tanpa crash
- [ ] Tabel `wallets` berhasil dibuat dengan kolom `id`, `name`, `balance`, `icon`, `color`
- [ ] Default wallet "Cash" di-seed saat database pertama kali dibuat
- [ ] `WalletDao` CRUD operations berfungsi (diverifikasi via unit test)
- [ ] `WalletRepository` interface + implementasi lengkap
- [ ] Semua existing test tetap lolos
- [ ] CI pipeline (build + test + lint) ✅ hijau

## Catatan Teknis

> **KISS:** Tidak perlu membuat `WalletType` enum (Debit/Credit/E-Wallet). Cukup `name` saja — user mendefinisikan sendiri.

> **YAGNI:** Jangan tambahkan fitur transfer antar wallet. Itu bisa jadi sprint berikutnya.

> **Clean Architecture:** Data layer selesai dan teruji dulu sebelum menyentuh UI di ISSUE-008.
