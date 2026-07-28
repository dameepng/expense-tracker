# ISSUE-079: Fix Obsolete SDK_INT Version Checks

## Description
Lint reported performance/correctness issues regarding obsolete SDK version checks:
1. **mipmap-anydpi-v26**: This folder configuration (v26) is unnecessary since the app's `minSdkVersion` is 28. The resources inside this folder should be moved to `mipmap-anydpi` and the `v26` folder deleted.
2. **NotificationHelper.kt**: Contains an unnecessary `SDK_INT` check that is always true (since SDK_INT >= 28).

## Acceptance Criteria
- [x] Move all files from `app/src/main/res/mipmap-anydpi-v26` to `app/src/main/res/mipmap-anydpi`.
- [x] Delete the `app/src/main/res/mipmap-anydpi-v26` folder.
- [x] Remove the obsolete `Build.VERSION.SDK_INT >= Build.VERSION_CODES.O` (or similar) check in `NotificationHelper.kt`.
- [x] Ensure project compiles and lint errors are resolved.

## Technical Details
Since `minSdkVersion` is 28 (Android 9.0 Pie), any check for API <= 28 is guaranteed to be true, and any resource modifier like `-v26` is redundant if it's the only variant (or can be merged into the base folder).
