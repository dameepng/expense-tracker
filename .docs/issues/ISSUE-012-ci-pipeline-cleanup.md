# ISSUE-012: CI Pipeline Enhancement & Final Cleanup

**Priority:** 🟡 Medium  
**Type:** Infra + Cleanup  
**Sprint:** Sprint 2  
**Estimated Effort:** Small  
**Depends On:** ISSUE-007 — ISSUE-011

---

## Deskripsi

Memperkuat CI pipeline di GitHub Actions agar lebih robust untuk mendukung fitur wallet yang baru ditambahkan, serta melakukan final cleanup pada seluruh kode Sprint 2. Memastikan semua prinsip (Clean Architecture, Clean Code, KISS, YAGNI) diterapkan secara konsisten.

## Kondisi Saat Ini

- [ci.yml](file:///c:/dame-project/Android/expense_tracker/.github/workflows/ci.yml) sudah ada dengan steps: build, test, lint, upload reports
- Sprint 2 menambahkan banyak file baru (Wallet entity, screen, ViewModel)
- Kemungkinan ada dead code, unused imports, atau inkonsistensi setelah 5 issue berturut-turut

## Perubahan yang Dibutuhkan

### Prinsip: Clean Architecture · Clean Code · KISS · YAGNI · CI GitHub Actions

### CI Pipeline

#### [MODIFY] [.github/workflows/ci.yml](file:///c:/dame-project/Android/expense_tracker/.github/workflows/ci.yml)
Tambahkan steps baru:

```yaml
- name: Run Unit Tests with Coverage
  run: ./gradlew testDebugUnitTest

- name: Check Test Results
  if: failure()
  run: |
    echo "::error::Unit tests failed! Check test report for details."
    exit 1

- name: Upload APK Artifact
  if: success()
  uses: actions/upload-artifact@v4
  with:
    name: debug-apk
    path: app/build/outputs/apk/debug/app-debug.apk
```

Perubahan:
- **Upload APK:** Artifact APK tersedia untuk download setelah CI sukses
- **Explicit error message** saat test gagal
- **KISS:** Tidak menambahkan code coverage tool (JaCoCo), cukup pastikan test jalan
- **YAGNI:** Tidak menambahkan deploy step, CD, atau release automation

### Cleanup Tasks

#### Code Review Checklist

Lakukan review menyeluruh pada seluruh file yang diubah/ditambahkan di Sprint 2:

**Clean Architecture:**
- [ ] Semua ViewModel hanya berkomunikasi via Repository interface
- [ ] Tidak ada DAO yang diakses langsung dari UI layer
- [ ] Entity, DAO, Repository, ViewModel, Screen — setiap layer terpisah

**Clean Code:**
- [ ] Naming konsisten: `WalletDao`, `WalletRepository`, `WalletViewModel`, `WalletListScreen`
- [ ] Tidak ada dead code atau unused imports
- [ ] Semua fungsi memiliki single responsibility
- [ ] Comment hanya untuk "why", bukan "what"

**KISS:**
- [ ] Tidak ada abstraksi berlebihan (misal: tidak ada `BaseRepository`, `BaseViewModel`)
- [ ] Dialog untuk add wallet cukup `AlertDialog` sederhana
- [ ] Wallet picker di InputScreen cukup `FilterChip` sederhana

**YAGNI:**
- [ ] Tidak ada fitur transfer antar wallet
- [ ] Tidak ada fitur edit wallet (rename, change icon)
- [ ] Tidak ada fitur multi-currency
- [ ] Tidak ada fitur wallet grouping atau tagging

#### File Cleanup

- [ ] Hapus unused imports di semua file yang dimodifikasi
- [ ] Pastikan semua preview composable masih berjalan
- [ ] Periksa konsistensi formatting (indentation, spacing)
- [ ] Pastikan semua `TODO` comments sudah resolved atau tracked

### Testing

#### Verifikasi Semua Tests

```bash
./gradlew testDebugUnitTest
```

Tests yang harus lolos:
- `ExpenseDaoTest` — termasuk query per-wallet
- `HomeViewModelTest` — termasuk wallet integration
- `InputViewModelTest` — termasuk wallet selection
- `WalletDaoTest` — CRUD operations
- `WalletViewModelTest` — list + add + delete
- `WalletDetailViewModelTest` — filtered transactions

### CI GitHub Actions
- `./gradlew assembleDebug` harus berhasil
- `./gradlew testDebugUnitTest` harus lolos **semua** test
- `./gradlew lintDebug` harus lolos tanpa error (warning diperbolehkan)
- APK artifact tersedia untuk download
- Perubahan `ci.yml` sudah di-commit dan di-push

## Acceptance Criteria

- [ ] CI pipeline enhanced dengan APK upload
- [ ] Semua test lolos (6+ test classes)
- [ ] Build sukses tanpa error
- [ ] Lint lolos tanpa error
- [ ] Code review checklist: semua centang ✅
- [ ] Tidak ada dead code, unused imports, atau TODO yang belum resolved
- [ ] APK artifact bisa di-download dari GitHub Actions
- [ ] CI pipeline (build + test + lint + upload) ✅ hijau

## Catatan Teknis

> **KISS:** CI pipeline tetap sederhana — build, test, lint, upload. Tidak perlu JaCoCo, SonarQube, atau tools tambahan.

> **YAGNI:** Jangan tambahkan CD (Continuous Deployment), release workflow, atau Play Store upload automation. Itu bisa jadi sprint berikutnya.

> **Clean Code:** Issue ini adalah kesempatan terakhir untuk membersihkan technical debt sebelum sprint ditutup.
