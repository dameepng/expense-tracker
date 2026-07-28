# ISSUE-073: Fix KSP Block Placement in build.gradle.kts

## Description
During code inspection, a warning is raised in `app/build.gradle.kts` regarding the `ksp` configuration block for Room schema location:

```
Suspicious receiver type; this does not apply to the current receiver of type ApplicationDefaultConfig. This will apply to a receiver of type Project, found in one of the enclosing lambdas. Make sure it's declared in the right place in the file.
```

### Cause
The `ksp` block is currently placed inside the `android { defaultConfig { ... } }` block. In newer versions of the KSP plugin, the `ksp` extension is applied to the `Project` level, not to Android's `defaultConfig`. Placing it inside `defaultConfig` causes the Kotlin DSL to resolve the receiver to the enclosing `Project` instead of `ApplicationDefaultConfig`, which works but triggers a IDE warning because it's technically misplaced.

### Expected Solution
Move the `ksp { ... }` block to the top level of the `app/build.gradle.kts` file, entirely outside the `android { ... }` block.

## Acceptance Criteria
- [x] `ksp` block is moved to the top level of `app/build.gradle.kts`.
- [x] The "Suspicious receiver type" warning is resolved.
- [x] Project compiles and builds successfully.

## Technical Details
- File to modify: `app/build.gradle.kts`
- Move from:
  ```kotlin
  android {
      defaultConfig {
          // Room schema export directory for version migration checks
          ksp {
              arg("room.schemaLocation", "$projectDir/schemas")
          }
      }
  }
  ```
- Move to:
  ```kotlin
  // At the top level, outside android {}
  ksp {
      arg("room.schemaLocation", "$projectDir/schemas")
  }
  ```
