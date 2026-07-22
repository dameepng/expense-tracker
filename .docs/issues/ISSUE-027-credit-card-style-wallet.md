# ISSUE-027: Redesign Wallet Card to Credit/Debit Card Style

## Deskripsi
Saat ini, tampilan wallet di halaman Wallet List masih berupa card biasa (kotak abu-abu polos dengan nama wallet dan saldo). Desain ini terasa kurang menarik dan tidak memberikan kesan premium.

Perubahan ini akan mendesain ulang card wallet agar menyerupai **kartu debit/kredit asli** lengkap dengan:
- Nomor kartu (4 grup × 4 digit)
- Nama pemegang kartu
- Tanggal kadaluarsa (MM/YY)
- Chip icon & contactless icon
- **Pilihan warna gradasi** (5 opsi) yang bisa dipilih user saat membuat wallet baru

## User Story
> Sebagai user, saya ingin wallet saya terlihat seperti kartu debit/kredit yang sesungguhnya dengan warna gradasi yang premium, agar tampilan aplikasi terasa lebih menarik dan personal.

## Acceptance Criteria
- [ ] Card wallet ditampilkan dengan desain menyerupai kartu debit/kredit fisik (aspect ratio ~1.586:1)
- [ ] Card menampilkan: nama wallet, nomor kartu, nama pemegang, MM/YY, saldo, chip & contactless icon
- [ ] Form "Tambah Wallet Baru" memiliki field tambahan: Nomor Kartu, Nama Pemegang, MM/YY
- [ ] Form "Tambah Wallet Baru" memiliki **5 pilihan warna gradasi** yang bisa dipilih user
- [ ] Warna yang dipilih tersimpan di database dan ditampilkan pada card
- [ ] Card horizontal scrollable (jika jumlahnya banyak) atau stacked secara vertikal
- [ ] Masking nomor kartu: hanya 4 digit terakhir yang terlihat penuh (**** **** **** 5062)

## Technical Details

### Root Cause
Data model `Wallet` saat ini hanya memiliki field `name`, `balance`, `icon`, dan `color` — tidak ada field untuk nomor kartu, nama pemegang, dan tanggal kadaluarsa. UI card saat ini hanya menampilkan nama dan saldo dalam kotak polos.

### Fix

#### 1. Update Data Model (Room Migration)
**[MODIFY]** [Wallet.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/data/Wallet.kt)

Tambahkan field baru:
```kotlin
@Entity(tableName = "wallets")
data class Wallet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val balance: Long = 0L,
    val icon: String = "",
    val color: String = "",
    val cardNumber: String = "",       // [NEW] 16 digit nomor kartu
    val cardHolderName: String = "",   // [NEW] Nama pemegang kartu
    val cardExpiry: String = ""        // [NEW] Format MM/YY
)
```

#### 2. Database Migration (v8 → v9)
**[MODIFY]** [AppDatabase.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/data/AppDatabase.kt)

```kotlin
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE wallets ADD COLUMN cardNumber TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE wallets ADD COLUMN cardHolderName TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE wallets ADD COLUMN cardExpiry TEXT NOT NULL DEFAULT ''")
    }
}
```

#### 3. Definisi Warna Gradasi
**[NEW]** `ui/wallet/CardGradients.kt`

5 pilihan gradasi premium:
| Nama | Start Color | End Color | Nuansa |
|---|---|---|---|
| **Sunset Rose** | `#E96196` | `#C084C4` | Pink → Ungu (seperti referensi) |
| **Ocean Blue** | `#1A6DFF` | `#00D4FF` | Biru gelap → Cyan |
| **Midnight Purple** | `#4A148C` | `#7C4DFF` | Ungu tua → Violet |
| **Emerald Green** | `#00695C` | `#26A69A` | Hijau zamrud → Teal |
| **Obsidian Dark** | `#1A1A2E` | `#16213E` | Hitam → Navy gelap |

#### 4. Redesign Card Composable
**[MODIFY]** [WalletListScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/wallet/WalletListScreen.kt)

Ubah `WalletListItem` menjadi `CreditCardItem` yang menampilkan:
```
┌──────────────────────────────────────────┐
│  [Chip Icon]            [Contactless]    │
│                                          │
│  **** **** **** 5062                     │
│                                          │
│  Nama Pemegang              MM/YY        │
│  Nama Wallet         Rp8.000.000         │
└──────────────────────────────────────────┘
```

- Background: `Brush.linearGradient()` sesuai warna yang dipilih
- Aspect ratio: ~1.586:1 (standar kartu)
- Corner radius: 16.dp
- Font: Monospace untuk nomor kartu

#### 5. Update Form ModalBottomSheet
**[MODIFY]** [WalletListScreen.kt](file:///c:/dame-project/Android/expense_tracker/app/src/main/java/com/example/expense_tracker/ui/wallet/WalletListScreen.kt)

Tambahkan field input pada BottomSheet:
- **Nama Wallet** (sudah ada)
- **Nomor Kartu** (baru) — `OutlinedTextField` dengan `KeyboardType.Number`, max 16 digit, auto-format 4-4-4-4
- **Nama Pemegang** (baru) — `OutlinedTextField` biasa
- **MM/YY** (baru) — `OutlinedTextField` dengan `KeyboardType.Number`, max 5 karakter (auto-add `/`)
- **Pilih Warna** (baru) — Row berisi 5 lingkaran warna gradasi, tap untuk memilih (border highlight saat selected)

#### 6. Update ViewModel
**[MODIFY]** `WalletViewModel.kt`

Update fungsi `addWallet()` agar menerima parameter baru:
```kotlin
fun addWallet(name: String, cardNumber: String, cardHolderName: String, cardExpiry: String, color: String)
```

## Referensi Visual
Desain terinspirasi dari kartu debit/kredit fisik dengan gradasi warna premium dan layout informasi standar industri.

## Dependencies
- Room Migration v8 → v9
- Tidak ada dependency library baru
