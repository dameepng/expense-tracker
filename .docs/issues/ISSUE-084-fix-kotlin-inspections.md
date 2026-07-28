# ISSUE-084: Fix Kotlin Inspections and Style Issues

## Description
Lint / IDE reported several minor Kotlin code style and correctness issues across the project:
1. **UserPreferencesRepositoryImpl**: Private property naming convention issues (using UPPER_SNAKE_CASE for non-const private val).
2. **InputViewModel**: Condition `loadedWalletId != null` is always true.
3. **BillReminderWorker**: Unused equals expression.
4. **MainActivity**: Boolean expression can be simplified, and explicit 'set' call can be replaced with property assignment.
5. **HeroBalanceCard**: Java `Math.abs` should be replaced with Kotlin `kotlin.math.abs`.
6. **HomeScreen & ProfileScreen**: Replace `it != null && it.isNotEmpty()` with `!it.isNullOrEmpty()`.

## Acceptance Criteria
- [x] Rename preferences keys to camelCase in `UserPreferencesRepositoryImpl` (or move to `companion object` as `const val`).
- [x] Simplify conditions in `InputViewModel` and `MainActivity`.
- [x] Fix unused equals in `BillReminderWorker`.
- [x] Replace `set(...)` with `=` in `MainActivity`.
- [x] Use `kotlin.math.abs` in `HeroBalanceCard.kt`.
- [x] Use `!isNullOrEmpty()` in `HomeScreen.kt` and `ProfileScreen.kt`.
- [x] Code compiles without warnings.
