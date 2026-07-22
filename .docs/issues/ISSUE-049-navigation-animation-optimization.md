# ISSUE-049: Optimasi Animasi Navigasi Antar Halaman

## Priority: 🟡 Medium
## Type: Performance / UX
## Status: Closed

## Deskripsi
Saat ini, setiap perpindahan halaman menggunakan `slideInHorizontally + fadeIn` dengan durasi 300ms. Animasi ini diterapkan secara global di `NavHost`, termasuk untuk navigasi **antar tab** (bottom navigation).

Masalahnya:
1. **Slide animation di tab navigation** terasa tidak natural — standar Material Design untuk bottom tab adalah **crossfade**, bukan slide.
2. Animasi 300ms dikombinasikan dengan delay 300ms dari ViewModel (ISSUE-045), sehingga total waktu sebelum konten terlihat bisa mencapai **600ms+**.
3. `tween(300)` tanpa easing curve khusus terasa linear dan lambat.

## Solusi

### Bottom Tab: Gunakan Crossfade
Untuk navigasi bottom tab (Home, Summary, Wallet, Profile), gunakan animasi `fadeIn`/`fadeOut` dengan durasi lebih pendek (150ms):

```kotlin
composable(
    route = NavRoutes.HOME,
    enterTransition = { fadeIn(animationSpec = tween(150)) },
    exitTransition = { fadeOut(animationSpec = tween(150)) },
    popEnterTransition = { fadeIn(animationSpec = tween(150)) },
    popExitTransition = { fadeOut(animationSpec = tween(150)) }
)
```

### Push Navigation: Gunakan Slide yang Lebih Cepat
Untuk navigasi push (Input, Reminder Form), pertahankan slide tapi percepat:

```kotlin
enterTransition = { slideInHorizontally(tween(200, easing = FastOutSlowInEasing), initialOffsetX = { it / 3 }) + fadeIn(tween(200)) }
```

Menggunakan `it / 3` alih-alih `it` membuat slide hanya 1/3 layar, terasa jauh lebih cepat.

## File yang Terpengaruh
- `MainActivity.kt`: NavHost transition defaults dan per-composable overrides

## Dampak
- Perpindahan tab terasa instan (150ms crossfade vs 300ms slide)
- Navigasi push terasa snappy dan modern
