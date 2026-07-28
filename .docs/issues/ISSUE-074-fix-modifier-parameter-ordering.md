# ISSUE-074: Fix Compose Modifier Parameter Ordering

## Description
Lint is reporting 4 warnings across `DonutChart.kt`, `HomeScreen.kt`, and `SummaryScreen.kt`:
`Modifier parameter should be the first optional parameter`

According to Jetpack Compose guidelines, the `modifier` parameter should be the first optional parameter in a Composable function signature (i.e., immediately following the required parameters).

### Files affected
- `app/src/main/java/com/example/expense_tracker/ui/summary/DonutChart.kt` (1 warning)
- `app/src/main/java/com/example/expense_tracker/ui/home/HomeScreen.kt` (1 warning)
- `app/src/main/java/com/example/expense_tracker/ui/summary/SummaryScreen.kt` (2 warnings)

## Acceptance Criteria
- [x] Reorder parameters in `DonutChart.kt` so `modifier` is the first optional parameter.
- [x] Reorder parameters in `HomeScreen.kt` so `modifier` is the first optional parameter.
- [x] Reorder parameters in `SummaryScreen.kt` so `modifier` is the first optional parameter.
- [x] Fix any call sites if named arguments were not used for the reordered parameters.
- [x] Project compiles successfully and lint warnings are resolved.

## Technical Details
Find the composable declarations and move the `modifier: Modifier = Modifier` to immediately after the required parameters. Since the parameters are reordered, check usages in the codebase to ensure calls still compile (in most cases, since `modifier` is optional, if the callers used named arguments or didn't pass optional arguments, it's fine).
