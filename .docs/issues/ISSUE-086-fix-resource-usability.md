# ISSUE-086: Fix Resource Usability and Missing Folders

## Description
Lint reported two resource usability issues:
1. **Icon is specified both as .xml file and as a bitmap**: The launcher icon (`ic_launcher`) is defined as both XML (Adaptive Icon) and legacy bitmap images (`.webp`). Since the application's `minSdkVersion` is 28 (Android 9.0 Pie), legacy bitmap launcher icons are obsolete because Adaptive Icons (API 26+) are fully supported.
2. **Missing density folder**: The `res` directory is missing standard density folders (`drawable-hdpi`, `drawable-mdpi`, `drawable-xhdpi`). Android Lint expects these baseline folders to exist (even if empty) to ensure developers haven't accidentally omitted density variations.

## Acceptance Criteria
- [x] Delete all legacy bitmap launcher icons (`ic_launcher.webp` and `ic_launcher_round.webp`) from `mipmap-mdpi`, `mipmap-hdpi`, `mipmap-xhdpi`, `mipmap-xxhdpi`, and `mipmap-xxxhdpi`.
- [x] Create empty directories for `drawable-mdpi`, `drawable-hdpi`, and `drawable-xhdpi`.
- [x] Ensure project compiles and lint errors are resolved.

## Technical Details
Removing the legacy `.webp` icons will clear the "duplicate icon" warning and reduce APK size, as Android 9+ devices will solely use the crisp XML Adaptive Icons. Creating the empty `drawable` density folders satisfies the lint structural checks.
