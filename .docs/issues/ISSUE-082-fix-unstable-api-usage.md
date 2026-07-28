# ISSUE-082: Fix Unstable API Usage Warning in settings.gradle.kts

## Description
Lint reported the following JVM language warning:
`Unstable API Usage settings.gradle.kts : 'getRepositoriesMode()' is marked unstable with @Incubating...`

This occurs because centralized repository declaration (the `dependencyResolutionManagement` block) is marked as an incubating feature in the current Gradle API. IntelliJ / Android Studio flags any usage of `@Incubating` APIs with a warning. Since this is the standard and recommended way to declare repositories in modern Android projects, we can safely suppress this warning.

## Acceptance Criteria
- [x] Add `@Suppress("UnstableApiUsage")` above the `dependencyResolutionManagement` block in `settings.gradle.kts`.
- [x] Verify that the project syncs and compiles without errors.

## Technical Details
Adding the `@Suppress` annotation prevents the IDE from highlighting standard Gradle configuration blocks as warnings, keeping the codebase clean.
