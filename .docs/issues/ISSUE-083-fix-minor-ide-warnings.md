# ISSUE-083: Fix Remaining Minor IDE Warnings

## Description
The IDE reported two minor warnings:
1. **Declaration redundancy**: `HomeScreen.kt` - `Value of parameter 'maxLines' is always '1', and Value of parameter 'fontWeight' is always 'FontWeight.Bold'` for the `AutoResizeText` composable.
2. **Unnecessary module dependency**: `expense_tracker.app.androidTest` module sources supposedly do not depend on `expense_tracker.app.main`.

## Acceptance Criteria
- [x] Add `@Suppress("SameParameterValue")` to the `AutoResizeText` function in `HomeScreen.kt`. (It's a reusable UI component, so keeping the parameters makes sense for future-proofing, instead of hardcoding them).
- [x] Create a basic `ExampleInstrumentedTest.kt` in the `androidTest` folder that references a class from the `main` module (like `MainActivity`) to satisfy the IntelliJ dependency graph analyzer.

## Technical Details
The `androidTest` warning is a common false positive in IntelliJ/Android Studio when the test directory is empty or the tests don't explicitly reference application classes. Creating a baseline instrumentation test resolves this.
