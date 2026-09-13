# ISSUE-102: ViewModel dan State Machine Receipt Scan

**Status:** Planned
**Priority:** High
**Type:** Feature - State Management
**Depends on:** ISSUE-101
**Overview:** [Receipt Scan](../TICKETS.md)

## Deskripsi

Orkestrasi picker, processor, repository, dan draft melalui state yang tahan rotasi dan aman terhadap cancel/race. State harus membedakan idle, selected, scanning, success, fallback, dan error.

## Scope

- `ReceiptScanUiState`, `ReceiptScanViewModel`, dan factory memakai dependency injection manual existing.
- Select/replace image, start scan, loading, cancel, retry, fallback ke manual/NL Input, dan reset.
- Ignore late result setelah cancel/reset; satu tap tidak boleh mengirim request ganda.
- Expose kategori/wallet untuk review dan validasi draft dasar.

## Files Touched (Estimasi)

- Baru: `ui/receipt/ReceiptScanUiState.kt`, `ReceiptScanViewModel.kt`, factory.
- Test: `app/src/test/.../ui/receipt/ReceiptScanViewModelTest.kt`.

## Acceptance Criteria

- [ ] Semua transisi state deterministic untuk success, fallback, offline, timeout, invalid response, dan image failure.
- [ ] Cancel membatalkan coroutine/request dan late response tidak mengubah state baru.
- [ ] Retry memakai image yang sama tanpa duplikasi state/message.
- [ ] Rotasi mempertahankan state melalui ViewModel; reset membersihkan URI/draft/error.
- [ ] State tidak menyimpan raw base64 atau response mentah.

## Validasi dan Checkpoint

Unit test memakai fake processor/repository dan coroutine test dispatcher.

