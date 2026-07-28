# ISSUE-081: Fix Non-ASCII Characters Lint Warning in Tests

## Description
Lint reported the following Internationalization warnings:
1. `ExpenseDaoAggregationTest.kt`: Non-ASCII characters
2. `StreakCalculatorTest.kt`: Non-ASCII characters

These warnings occur because the test files contain characters outside the standard ASCII range (like em-dashes `—`, arrows `→`, and box-drawing symbols `──`) inside their comments. While this is harmless for Kotlin compilation and test execution, Android Lint flags it to ensure internationalization consistency in production code. Since these are test files where such documentation symbols are acceptable and helpful, we can safely suppress the lint warning.

## Acceptance Criteria
- [x] Add `@file:Suppress("NonAsciiCharacters")` to the top of `ExpenseDaoAggregationTest.kt`.
- [x] Add `@file:Suppress("NonAsciiCharacters")` to the top of `StreakCalculatorTest.kt`.
- [x] Ensure project compiles and lint warnings are resolved.

## Technical Details
Adding the suppression at the file level is the cleanest way to prevent Lint from complaining about comments containing typography symbols or non-English text in test suites.
