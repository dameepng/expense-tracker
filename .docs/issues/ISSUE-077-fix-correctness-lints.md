# ISSUE-077: Fix Remaining Correctness Lint Warnings

## Description
During Android Lint inspection, three correctness warnings were reported:

1. **App Bundle handling of runtime locale changes (MainActivity.kt)**
   Found dynamic locale changes, but did not find corresponding Play Core library calls for downloading languages and splitting by language is not disabled in the bundle configuration.
   
2. **Implied default locale in case conversion (HeroBalanceCard.kt)**
   Implicitly using the default locale is a common source of bugs: Use `String.format(Locale, ...)` or `.uppercase(Locale)` instead.

3. **Redundant label on activity (AndroidManifest.xml)**
   Redundant label on activity can be removed since the application tag already defines it.

## Acceptance Criteria
- [x] Add `@SuppressLint("AppBundleLocaleChanges")` to the locale configuration update block in `MainActivity.kt`.
- [x] Explicitly pass `Locale.ROOT` to the string format/case conversion in `HeroBalanceCard.kt` to avoid Compose `NonObservableLocale` error.
- [x] Remove `android:label="@string/app_name"` from the `.MainActivity` tag in `AndroidManifest.xml`.
- [x] Project compiles successfully and lint warnings are resolved.

## Technical Details
- File: `MainActivity.kt` (Suppress the warning as we handle locales manually).
- File: `HeroBalanceCard.kt` (Fix the implied locale usage).
- File: `AndroidManifest.xml` (Remove the redundant label).
