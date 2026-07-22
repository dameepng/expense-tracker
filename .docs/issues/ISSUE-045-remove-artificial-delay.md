# ISSUE-045: Hapus Artificial Delay di ViewModel

## Priority: 🔴 Critical
## Type: Performance / Bug Fix
## Status: Closed

## Deskripsi
Terdapat `kotlinx.coroutines.delay(300)` yang sengaja ditambahkan di beberapa ViewModel sebagai "artificial delay". Ini menyebabkan setiap kali data di-refresh (termasuk saat perpindahan halaman), ada jeda 300ms yang **tidak perlu** sebelum data ditampilkan.

## File yang Terpengaruh
- `HomeViewModel.kt` (line 57): `kotlinx.coroutines.delay(300)` di dalam `refresh()`
- `SummaryViewModel.kt` (line 45): `kotlinx.coroutines.delay(300)` di dalam `loadData()`

## Solusi
- Hapus seluruh `kotlinx.coroutines.delay(300)` dari kedua ViewModel.
- Jika ingin efek "smooth loading", gunakan animasi Compose (misalnya `AnimatedVisibility`) pada layer UI, bukan delay pada layer data.

## Dampak
- Menghemat 300ms di setiap refresh data.
- Perpindahan halaman terasa instan karena data langsung tersedia.
