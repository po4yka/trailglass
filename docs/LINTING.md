# Code Quality & Linting

This document describes the linting and code quality tools configured for the Trailglass project.

## Overview

The project uses multiple linters and static analyzers to maintain code quality across different platforms:

| Tool | Language | Purpose | Auto-fix |
|------|----------|---------|----------|
| **ktlint** | Kotlin | Code formatting & style | ✅ Yes |
| **detekt** | Kotlin | Static analysis & code smells | ⚠️ Partial |
| **Android Lint** | Kotlin/XML | Android-specific issues | ❌ No |
| **SwiftLint** | Swift | Swift code style & quality | ⚠️ Partial |

## Quick Start

### Run All Linters

```bash
./scripts/lint-all.sh
```

### Auto-Format All Code

```bash
./scripts/format-all.sh
```

## Individual Tools

### ktlint (Kotlin Formatting)

ktlint enforces the official Kotlin coding conventions.

#### Commands

```bash
# Check all Kotlin code
./gradlew ktlintCheck

# Auto-format all Kotlin code
./gradlew ktlintFormat

# Check specific module
./gradlew :shared:ktlintCheck
./gradlew :composeApp:ktlintCheck
```

#### Configuration

- Version: 1.5.0
- Config: Built-in Kotlin coding conventions
- Editor config: `.editorconfig`
- Excludes: Generated code (SQLDelight, kotlin-inject)

#### Integration

ktlint is integrated into the build process but doesn't fail builds by default. Run checks before committing.

### detekt (Static Analysis)

detekt performs static code analysis to find code smells, complexity issues, and potential bugs.

#### Commands

```bash
# Run static analysis
./gradlew detekt

# Auto-fix issues (where possible)
./gradlew detekt --auto-correct

# Generate baseline (ignore existing issues)
./gradlew detektBaseline
```

#### Reports

After running detekt, view reports at:
- Shared module: `shared/build/reports/detekt/detekt.html`
- Android app: `composeApp/build/reports/detekt/detekt.html`

#### Configuration

- Config file: `config/detekt/detekt.yml`
- Baseline: `config/detekt/baseline.xml` (if generated)
- Key checks:
  - Complexity (max method length: 60, complexity: 15)
  - Naming conventions
  - Performance issues
  - Potential bugs
  - Code style

#### Creating a Baseline

If you have many existing issues and want to focus on new code:

```bash
./gradlew detektBaseline
```

This creates `config/detekt/baseline.xml` with all current issues. Future runs will only report new issues.

### Android Lint

Android Lint checks for Android-specific issues, including:
- Compose best practices
- Performance problems
- Accessibility issues
- Security vulnerabilities

#### Commands

```bash
# Run Android Lint
./gradlew :composeApp:lint

# Lint specific build variant
./gradlew :composeApp:lintDebug
./gradlew :composeApp:lintRelease
```

#### Reports

View reports at: `composeApp/build/reports/lint-results.html`

#### Configuration

- Config file: `config/android-lint.xml`
- Baseline: `composeApp/lint-baseline.xml` (if generated)
- Key checks:
  - `UnusedMaterial3ScaffoldPaddingParameter` (error)
  - `UnrememberedMutableState` (error)
  - Security issues (exported components, permissions)
  - Accessibility (content descriptions)

#### Creating a Baseline

To ignore existing issues:

```bash
./gradlew :composeApp:lint
# Android Lint will prompt to create a baseline if issues exist
```

Or manually set `baseline = file("lint-baseline.xml")` in `composeApp/build.gradle.kts`.

### SwiftLint (iOS)

SwiftLint enforces Swift style and conventions for the iOS app.

#### Installation

```bash
brew install swiftlint
```

#### Commands

```bash
# Lint Swift code
cd iosApp && swiftlint

# Auto-fix issues
cd iosApp && swiftlint --fix

# Strict mode (warnings as errors)
cd iosApp && swiftlint --strict
```

#### Configuration

- Config file: `iosApp/.swiftlint.yml`
- Key rules:
  - Line length: 120 (warning), 150 (error)
  - Function body length: 50 (warning), 100 (error)
  - File length: 500 (warning), 1000 (error)
  - Cyclomatic complexity: 15 (warning), 25 (error)
  - Custom rules: No `print()` or `NSLog()` statements

#### Xcode Integration

SwiftLint should be added as a build phase in Xcode:

1. Open `iosApp.xcodeproj` in Xcode
2. Select target → Build Phases
3. Add "Run Script Phase"
4. Add:
   ```bash
   if which swiftlint >/dev/null; then
     swiftlint
   else
     echo "warning: SwiftLint not installed"
   fi
   ```

## Editor Integration

### IntelliJ IDEA / Android Studio

1. **ktlint**: Install "ktlint" plugin from Marketplace
2. **detekt**: Install "Detekt" plugin from Marketplace
3. **EditorConfig**: Built-in support via `.editorconfig`

Settings:
- Enable "Format on Save" for automatic ktlint formatting
- Configure detekt to run on commit

### Xcode

1. **SwiftLint**: Automatically runs via build phase
2. **EditorConfig**: Install "EditorConfig" plugin

### VS Code

Install extensions:
- "Kotlin" by fwcd
- "SwiftLint" by shinnn
- "EditorConfig for VS Code"

## CI/CD Integration

### GitHub Actions Workflows

The project includes automated GitHub Actions workflows:

#### 1. Code Quality Workflow (lint.yml)

Runs on every push and pull request to `main` and `develop` branches.

**Jobs:**
- **ktlint**: Kotlin code formatting checks
- **detekt**: Static code analysis with SARIF upload to GitHub Security
- **android-lint**: Android-specific checks
- **swiftlint**: Swift code quality (runs on macOS)

**Artifacts:**
- ktlint reports (on failure)
- detekt HTML and SARIF reports
- Android Lint HTML and XML reports
- SwiftLint HTML report

#### 2. Main CI Workflow (ci.yml)

Comprehensive build and test workflow.

**Jobs:**
- **test-shared**: Run KMP tests with coverage verification
- **test-android**: Run Android unit tests
- **build-android**: Build debug APK
- **build-ios**: Build iOS framework and app

### Viewing CI Results

1. **On GitHub**: Go to "Actions" tab to see all workflow runs
2. **On PRs**: Check the "Checks" section to see status
3. **Reports**: Download artifacts from failed builds

### Running CI Checks Locally

```bash
# All linters (same as CI)
./scripts/lint-all.sh

# All tests
./scripts/run-tests.sh

# Build Android
./gradlew :composeApp:assembleDebug

# Build iOS framework
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

### Configuration Files

- `.github/workflows/lint.yml` - Linting workflow
- `.github/workflows/ci.yml` - Main CI workflow
- `.github/dependabot.yml` - Dependency updates
- `.github/pull_request_template.md` - PR checklist

## Pre-commit Hooks

### Option 1: Manual Git Hook

Create `.git/hooks/pre-commit`:

```bash
#!/bin/bash
echo "Running linters..."
./scripts/lint-all.sh
```

Make executable:
```bash
chmod +x .git/hooks/pre-commit
```

### Option 2: Pre-commit Framework

Install [pre-commit](https://pre-commit.com/):

```bash
brew install pre-commit
```

Create `.pre-commit-config.yaml`:

```yaml
repos:
  - repo: local
    hooks:
      - id: ktlint
        name: ktlint
        entry: ./gradlew ktlintCheck
        language: system
        pass_filenames: false
      - id: detekt
        name: detekt
        entry: ./gradlew detekt
        language: system
        pass_filenames: false
```

Install hooks:
```bash
pre-commit install
```

## Troubleshooting

### "detekt baseline not found"

If you see this error, either:
1. Generate a baseline: `./gradlew detektBaseline`
2. Remove the baseline reference from `build.gradle.kts`

### "ktlint formatting conflicts with detekt"

Both tools should be aligned. If conflicts occur:
1. Run `./gradlew ktlintFormat` first
2. Then run `./gradlew detekt`

### "SwiftLint not found"

Install SwiftLint:
```bash
brew install swiftlint
```

Or run linters without SwiftLint:
```bash
./gradlew ktlintCheck detekt
./gradlew :composeApp:lint
```

### Generated Code Issues

All linters are configured to exclude generated code:
- SQLDelight: `**/db/**`
- kotlin-inject: `**/*_Factory.kt`, `**/*Component*`

If you still see issues, add exclusions to the respective config files.

## Best Practices

1. **Run linters before committing**:
   ```bash
   ./scripts/lint-all.sh
   ```

2. **Auto-format regularly**:
   ```bash
   ./scripts/format-all.sh
   ```

3. **Fix issues incrementally**: Don't let issues accumulate

4. **Use baselines for legacy code**: Focus on keeping new code clean

5. **Review linter reports**: They often catch real bugs

6. **Configure your editor**: Auto-format on save saves time

## Gradle Tasks Reference

| Task | Description |
|------|-------------|
| `./gradlew ktlintCheck` | Check Kotlin formatting |
| `./gradlew ktlintFormat` | Auto-format Kotlin code |
| `./gradlew detekt` | Run static analysis |
| `./gradlew detekt --auto-correct` | Fix detekt issues |
| `./gradlew detektBaseline` | Generate baseline |
| `./gradlew :composeApp:lint` | Run Android Lint |
| `./gradlew :composeApp:lintDebug` | Lint debug variant |
| `./gradlew :shared:ktlintCheck` | Lint shared module only |

## Configuration Files

| File | Purpose |
|------|---------|
| `.editorconfig` | Editor/IDE settings |
| `config/detekt/detekt.yml` | Detekt rules |
| `config/detekt/baseline.xml` | Detekt baseline (optional) |
| `config/android-lint.xml` | Android Lint rules |
| `iosApp/.swiftlint.yml` | SwiftLint rules |
| `scripts/lint-all.sh` | Run all linters |
| `scripts/format-all.sh` | Auto-format all code |

## Further Reading

- [ktlint](https://pinterest.github.io/ktlint/)
- [detekt](https://detekt.dev/)
- [Android Lint](https://developer.android.com/studio/write/lint)
- [SwiftLint](https://github.com/realm/SwiftLint)
- [EditorConfig](https://editorconfig.org/)
