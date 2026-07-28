# ISSUE-080: Fix Image Density Folder for Splash Screen

## Description
Lint reported the following Usability warning:
`Image defined in density-independent drawable folder: splash_screen.png : Found bitmap drawable res/drawable/splash_screen.png in densityless folder`

This happens because bitmap images (`.png`, `.jpg`) should ideally be placed in density-specific folders (e.g., `drawable-mdpi`, `drawable-hdpi`, `drawable-xxhdpi`, or `drawable-nodpi`). Placing them in the base `drawable/` folder can cause inconsistent scaling across different devices or trigger lint warnings because `drawable/` is primarily meant for XML drawables (like shape drawables or vector graphics).

## Acceptance Criteria
- [x] Move `splash_screen.png` from `res/drawable/` to `res/drawable-xxhdpi/` (assuming it's a high-res asset to be scaled down gracefully on smaller screens).
- [x] Verify that the project compiles and lint errors are resolved.

## Technical Details
Move the file using file operations and ensure the directory `res/drawable-xxhdpi/` exists. No code changes are required as `@drawable/splash_screen` automatically resolves regardless of the specific density bucket.
