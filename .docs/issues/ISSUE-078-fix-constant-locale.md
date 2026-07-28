# ISSUE-078: Fix Constant Locale Lint Warning in TimeFormatter

## Description
Lint reported the following warning:
`Constant Locale: TimeFormatter.kt : Assigning Locale.getDefault() to a final static field is suspicious; this code will not work correctly if the user changes locale while the app is running`

This warning occurs because `Locale.getDefault()` is used to initialize a `val` inside an `object` singleton. The locale is evaluated only once when the class is loaded into memory. If the user changes the language of the application while it's running, the `TimeFormatter` will continue using the old locale. Furthermore, `SimpleDateFormat` is not thread-safe, making it dangerous to share a single instance across the app.

## Acceptance Criteria
- [x] Refactor `TimeFormatter.kt` to instantiate `SimpleDateFormat` within the `formatTime` function, ensuring it picks up the latest locale and is thread-safe.
- [x] Run `./gradlew lintDebug` (or compile) to verify the warning is gone.

## Technical Details
In `TimeFormatter.kt`:
Move the `timeFormat` instance inside the `formatTime` function, or use a custom getter `get()`. Instantiating it inside the function is better because `SimpleDateFormat` is not thread-safe.
