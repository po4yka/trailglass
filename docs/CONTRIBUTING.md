# Contributing to TrailGlass

Thank you for your interest in contributing to TrailGlass! This document provides guidelines and best practices for contributing.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Code Style Guidelines](#code-style-guidelines)
- [Commit Message Conventions](#commit-message-conventions)
- [Branching Strategy](#branching-strategy)
- [Pull Request Process](#pull-request-process)
- [Testing Requirements](#testing-requirements)
- [Documentation](#documentation)
- [Community](#community)

## Code of Conduct

This project adheres to a Code of Conduct that all contributors are expected to follow:

- **Be Respectful**: Treat everyone with respect and courtesy
- **Be Collaborative**: Work together towards common goals
- **Be Patient**: Help others learn and grow
- **Be Inclusive**: Welcome diverse perspectives and backgrounds
- **Be Professional**: Maintain professional conduct in all interactions

## Getting Started

1. **Fork the Repository**:
   ```bash
   # Click "Fork" on GitHub
   git clone https://github.com/YOUR_USERNAME/trailglass.git
   cd trailglass
   ```

2. **Add Upstream Remote**:
   ```bash
   git remote add upstream https://github.com/po4yka/trailglass.git
   git fetch upstream
   ```

3. **Set Up Development Environment**:
   - Follow [DEVELOPMENT.md](DEVELOPMENT.md) for detailed setup instructions

4. **Find an Issue**:
   - Browse [open issues](https://github.com/po4yka/trailglass/issues)
   - Look for `good first issue` or `help wanted` labels
   - Comment on issue to claim it

## Development Setup

See [DEVELOPMENT.md](DEVELOPMENT.md) for complete setup instructions.

### Quick Start

```bash
# Install dependencies
./gradlew build

# Run tests
./gradlew test

# Run Android app
./gradlew :composeApp:installDebug

# Build iOS framework
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```

## Code Style Guidelines

### Kotlin Code Style

We follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) with some additions.

#### Naming Conventions

```kotlin
// Classes and Objects: PascalCase
class PlaceVisitRepository
object DatabaseHelper

// Functions and Variables: camelCase
fun getPlaceVisits()
val userId: String

// Constants: SCREAMING_SNAKE_CASE
const val MAX_RETRIES = 3
const val DEFAULT_TIMEOUT_MS = 5000L

// Private properties: leading underscore for StateFlow
private val _state = MutableStateFlow(State())
val state: StateFlow<State> = _state.asStateFlow()
```

#### File Structure

```kotlin
// 1. Package declaration
package com.po4yka.trailglass.feature.timeline

// 2. Imports (organized alphabetically)
import com.po4yka.trailglass.domain.model.PlaceVisit
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

// 3. KDoc comment
/**
 * Controller for timeline screen.
 */

// 4. Class declaration
class TimelineController(
    private val useCase: GetTimelineForDayUseCase,
    private val scope: CoroutineScope,
    private val userId: String
) {
    // Class implementation
}
```

#### Formatting

```kotlin
// Line length: 120 characters max
// Indentation: 4 spaces (no tabs)

// Function parameters: one per line if long
fun myFunction(
    firstParameter: String,
    secondParameter: Int,
    thirdParameter: Boolean
): ReturnType {
    // Implementation
}

// Chaining: one call per line
val result = repository
    .getPlaceVisits(userId)
    .filter { it.isValid }
    .sortedBy { it.startTime }
    .map { it.toUI() }

// Control flow: always use braces
if (condition) {
    doSomething()
} else {
    doSomethingElse()
}

// When: use when over if-else chains
when (transportType) {
    TransportType.WALK -> handleWalk()
    TransportType.CAR -> handleCar()
    else -> handleUnknown()
}
```

#### Comments and Documentation

```kotlin
/**
 * Loads timeline items for a specific date.
 *
 * Fetches place visits and route segments for the given date,
 * then combines them into a chronologically ordered timeline.
 *
 * @param userId The user ID to load timeline for
 * @param date The date to load timeline items for
 * @return List of timeline items ordered by time
 * @throws RepositoryException if database query fails
 */
suspend fun getTimelineForDay(
    userId: String,
    date: LocalDate
): List<TimelineItemUI> {
    // Implementation
}

// Single-line comments for implementation details
// Use clear, concise explanations
val visits = repository.getVisits() // Fetch from database

// TODO comments for future work
// TODO: Add caching for frequently accessed visits
// TODO(username): Optimize query performance
```

### Swift Code Style

Follow the [Swift API Design Guidelines](https://swift.org/documentation/api-design-guidelines/).

```swift
// Classes and Structs: PascalCase
class TimelineViewModel
struct PlaceVisit

// Functions and Variables: camelCase
func loadTimeline()
var selectedDate: Date

// Constants: camelCase (not SCREAMING_SNAKE_CASE)
let maxRetries = 3
let defaultTimeout: TimeInterval = 5.0

// File structure
import SwiftUI
import Shared

/// Controller for timeline screen.
class TimelineViewModel: ObservableObject {
    @Published var items: [TimelineItem] = []

    func loadTimeline() {
        // Implementation
    }
}
```

### SQL Code Style (SQLDelight)

```sql
-- File: PlaceVisits.sq
-- Table names: snake_case
-- Query names: camelCase

-- Create table
CREATE TABLE place_visits (
    id TEXT PRIMARY KEY NOT NULL,
    user_id TEXT NOT NULL,
    start_time INTEGER NOT NULL,
    city TEXT,
    country TEXT
);

-- Query naming: descriptive and clear
getById:
SELECT * FROM place_visits
WHERE id = ? AND user_id = ?;

getForTimeRange:
SELECT * FROM place_visits
WHERE user_id = ?
  AND start_time >= ?
  AND end_time <= ?
ORDER BY start_time ASC;

-- Use consistent formatting
insert:
INSERT INTO place_visits (
    id,
    user_id,
    start_time,
    city,
    country
) VALUES (?, ?, ?, ?, ?);
```

### Compose Code Style

```kotlin
@Composable
fun TimelineScreen(
    controller: TimelineController,
    onNavigateToVisit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by controller.state.collectAsState()

    Column(modifier = modifier) {
        // Composable content
    }
}

// Preview
@Preview
@Composable
private fun TimelineScreenPreview() {
    TrailGlassTheme {
        TimelineScreen(
            controller = previewController(),
            onNavigateToVisit = {}
        )
    }
}
```

## Commit Message Conventions

We follow [Conventional Commits](https://www.conventionalcommits.org/).

### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- **feat**: New feature
- **fix**: Bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting, no logic change)
- **refactor**: Code refactoring
- **perf**: Performance improvements
- **test**: Adding or updating tests
- **build**: Build system or dependency changes
- **ci**: CI/CD configuration changes
- **chore**: Maintenance tasks

### Scopes

- **data**: Data layer (repositories, database)
- **domain**: Domain layer (models, use cases)
- **feature**: Feature modules (stats, timeline, map, photo)
- **location**: Location tracking
- **ui**: UI components
- **android**: Android-specific code
- **ios**: iOS-specific code
- **deps**: Dependencies

### Examples

```bash
# Feature
feat(timeline): add day marker components

Implement day start and day end markers for timeline view.
Markers show at the beginning and end of each day's events.

# Bug fix
fix(location): prevent crash on permission denial

Handle location permission denial gracefully instead of crashing.
Show user-friendly message when permissions are denied.

Fixes #123

# Documentation
docs: update architecture documentation

Add section on state management patterns and data flow.

# Refactoring
refactor(data): extract repository implementations

Move repository implementations to separate files for
better organization and maintainability.

# Breaking change
feat(api)!: change PlaceVisit structure

BREAKING CHANGE: PlaceVisit now requires tripId field.
This requires migration for existing databases.

Migration: Run database migration script v2.sqm
```

### Guidelines

1. **Subject Line**:
   - Use imperative mood: "add" not "added" or "adds"
   - Don't capitalize first letter
   - No period at the end
   - Maximum 72 characters

2. **Body** (optional):
   - Wrap at 72 characters
   - Explain what and why, not how
   - Separate from subject with blank line

3. **Footer** (optional):
   - Reference issues: `Fixes #123`, `Closes #456`
   - Breaking changes: `BREAKING CHANGE: description`

## Branching Strategy

We use **Git Flow** with simplified branch naming.

### Branch Types

```
main              # Production-ready code
develop           # Integration branch
feature/*         # New features
fix/*             # Bug fixes
docs/*            # Documentation
refactor/*        # Refactoring
release/*         # Release preparation
hotfix/*          # Production hotfixes
```

### Workflow

1. **Start New Feature**:
   ```bash
   git checkout develop
   git pull upstream develop
   git checkout -b feature/timeline-filters
   ```

2. **Work on Feature**:
   ```bash
   # Make changes
   git add .
   git commit -m "feat(timeline): add date filter"

   # Push regularly
   git push origin feature/timeline-filters
   ```

3. **Keep Updated**:
   ```bash
   git fetch upstream
   git rebase upstream/develop
   ```

4. **Complete Feature**:
   ```bash
   # Create PR to develop
   # After approval, merge and delete branch
   ```

### Branch Naming

```bash
# Good
feature/map-markers
fix/location-crash
docs/architecture-update
refactor/repository-split

# Bad
feature-map-markers    # Use slash, not dash
MapMarkers             # Use lowercase
my-changes             # Be descriptive
```

## Pull Request Process

### Before Creating PR

1. **Update from Develop**:
   ```bash
   git fetch upstream
   git rebase upstream/develop
   ```

2. **Run Tests**:
   ```bash
   ./scripts/run-tests.sh
   ```

3. **Check Coverage**:
   ```bash
   ./gradlew koverVerify
   # Must meet 75%+ coverage target
   ```

4. **Run Linter** (if configured):
   ```bash
   ./gradlew spotlessCheck
   ```

### Creating PR

1. **Push Branch**:
   ```bash
   git push origin feature/your-feature
   ```

2. **Open PR on GitHub**:
   - Base: `develop` (not `main`)
   - Title: Clear, descriptive (like commit message)
   - Description: Use template below

3. **PR Description Template**:
   ```markdown
   ## Description
   Brief description of changes

   ## Type of Change
   - [ ] Bug fix
   - [ ] New feature
   - [ ] Breaking change
   - [ ] Documentation update

   ## Testing
   - [ ] Unit tests added/updated
   - [ ] Integration tests added/updated
   - [ ] UI tests added/updated
   - [ ] Manual testing completed

   ## Checklist
   - [ ] Code follows style guidelines
   - [ ] Self-review completed
   - [ ] Comments added for complex code
   - [ ] Documentation updated
   - [ ] No new warnings
   - [ ] Tests pass locally
   - [ ] Coverage meets requirements (75%+)

   ## Related Issues
   Fixes #123
   Related to #456

   ## Screenshots (if applicable)
   [Add screenshots for UI changes]
   ```

### PR Review Process

1. **Automated Checks**: CI must pass
2. **Code Review**: At least 1 approval required
3. **Address Feedback**: Make requested changes
4. **Re-request Review**: After updates
5. **Merge**: Squash and merge to develop

### Review Guidelines

**For Authors**:
- Keep PRs small and focused
- Respond to feedback promptly
- Be open to suggestions
- Update PR description if scope changes

**For Reviewers**:
- Review within 2 business days
- Be constructive and specific
- Ask questions, don't make demands
- Approve when satisfied

## Testing Requirements

### Coverage Requirements

- **Overall**: 75%+ (enforced by Kover)
- **Domain Models**: 90%+
- **Repositories**: 85%+
- **Use Cases**: 80%+
- **Controllers**: 75%+
- **UI Components**: 70%+

### Test Checklist

- [ ] Unit tests for new functions
- [ ] Integration tests for repository changes
- [ ] UI tests for new screens/components
- [ ] Edge cases covered
- [ ] Error handling tested
- [ ] All tests pass locally

### Writing Tests

```kotlin
@Test
fun testGetPlaceVisitsForTimeRange_returnsCorrectVisits() = runTest {
    // Arrange
    val start = Instant.parse("2024-01-01T00:00:00Z")
    val end = Instant.parse("2024-01-31T23:59:59Z")

    repository.insertPlaceVisit(createTestVisit(
        id = "visit1",
        startTime = Instant.parse("2024-01-15T10:00:00Z")
    ))

    // Act
    val results = repository.getPlaceVisitsForTimeRange(userId, start, end).first()

    // Assert
    assertEquals(1, results.size)
    assertEquals("visit1", results[0].id)
}
```

See [TESTING.md](TESTING.md) for complete testing guide.

## Documentation

### When to Update Documentation

- **Always**: For new features or APIs
- **Architecture**: For structural changes
- **README**: For setup or usage changes
- **Comments**: For complex logic

### Documentation Checklist

- [ ] KDoc/DocC for public APIs
- [ ] README updated if needed
- [ ] Architecture docs updated if needed
- [ ] Examples added for new features
- [ ] Comments added for complex code

## Community

### Getting Help

- **Issues**: [GitHub Issues](https://github.com/po4yka/trailglass/issues)
- **Discussions**: [GitHub Discussions](https://github.com/po4yka/trailglass/discussions)
- **Documentation**: [docs/](docs/)

### Reporting Bugs

Use the bug report template:

```markdown
**Describe the bug**
Clear description of the bug

**To Reproduce**
1. Go to '...'
2. Click on '...'
3. See error

**Expected behavior**
What you expected to happen

**Screenshots**
If applicable

**Environment**
- OS: [e.g. Android 13, iOS 17]
- Device: [e.g. Pixel 5, iPhone 15]
- App version: [e.g. 1.0.0]

**Additional context**
Any other relevant information
```

### Suggesting Features

Use the feature request template:

```markdown
**Is your feature request related to a problem?**
Clear description of the problem

**Describe the solution you'd like**
Clear description of the desired solution

**Describe alternatives you've considered**
Any alternative solutions or features

**Additional context**
Mockups, examples, or references
```

## Release Process

(For maintainers)

1. **Create Release Branch**:
   ```bash
   git checkout -b release/v1.0.0 develop
   ```

2. **Update Version**:
   - `build.gradle.kts`: `versionName = "1.0.0"`
   - `build.gradle.kts`: `versionCode = 1`

3. **Update Changelog**:
   ```bash
   # Add to CHANGELOG.md
   ```

4. **Test Release**:
   ```bash
   ./gradlew build
   ./scripts/run-tests.sh
   ```

5. **Merge to Main**:
   ```bash
   git checkout main
   git merge --no-ff release/v1.0.0
   git tag -a v1.0.0 -m "Release v1.0.0"
   ```

6. **Merge Back to Develop**:
   ```bash
   git checkout develop
   git merge --no-ff release/v1.0.0
   ```

7. **Push**:
   ```bash
   git push upstream main develop --tags
   ```

## License

By contributing, you agree that your contributions will be licensed under the **Non-Commercial Open Software License (NC-OSL)**.

## Questions?

If you have questions not covered here:
1. Check [DEVELOPMENT.md](DEVELOPMENT.md)
2. Check [ARCHITECTURE.md](ARCHITECTURE.md)
3. Search existing issues
4. Create a new discussion

Thank you for contributing to TrailGlass! 🎉
