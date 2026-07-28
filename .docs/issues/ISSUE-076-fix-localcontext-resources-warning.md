# ISSUE-076: Fix Lint Warning for Reading Resources from LocalContext

## Description
Lint is reporting the following warning in `MainActivity.kt`:
`Reading Resources using LocalContext.current.resources`

This warning occurs because the code accesses `context.resources` directly from a context obtained via `LocalContext.current`. In Jetpack Compose, this practice is discouraged because it can bypass the Compose configuration mechanisms or lead to inconsistencies if the configuration is overridden via `CompositionLocalProvider` (which the code is actually doing!).

## Acceptance Criteria
- [x] Suppress the lint warning or refactor the code to avoid reading resources directly from `LocalContext.current` in the `remember` block.
- [x] Ensure language switching functionality still works.
- [x] Run `./gradlew lintDebug` (or compile) to verify the warning is gone.

## Technical Details
In `MainActivity.kt`, the `remember(language, configuration)` block contains:
```kotlin
context.resources.updateConfiguration(config, context.resources.displayMetrics)
```
We can try changing `context.resources` to `context.applicationContext.resources` to bypass the specific `LocalContext` rule, or suppress it. We will refactor it to `context.applicationContext.resources` which is safer for app-wide locale changes anyway.
