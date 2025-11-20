---
name: kmp-code-reviewer
description: Reviews Kotlin Multiplatform code for architecture compliance, best practices, and platform-specific issues. Use when reviewing code, PRs, or analyzing code quality in the Trailglass project.
allowed-tools:
  - Read
  - Grep
  - Glob
---

# KMP Code Reviewer

Expert code review for Kotlin Multiplatform projects with focus on Trailglass architecture and best practices.

## Review Checklist

### Architecture Compliance

**Layer Separation:**
- [ ] No layer skipping (UI → Controller → UseCase → Repository → Database)
- [ ] No reverse dependencies (e.g., Repository depending on Controller)
- [ ] Domain models in `domain/`, implementations in `data/`
- [ ] Repository interfaces in `domain/repository/`, implementations in `data/repository/impl/`
- [ ] Controllers use StateFlow pattern correctly

**Dependency Injection:**
- [ ] All classes use constructor injection with `@Inject`
- [ ] No manual DI or factory methods
- [ ] Proper scoping (`@AppScope` for singletons)
- [ ] Platform dependencies in `PlatformModule`

**Platform Code:**
- [ ] `expect`/`actual` declarations are minimal and necessary
- [ ] Platform-specific code only in `androidMain`/`iosMain`
- [ ] No Android/iOS APIs in `commonMain`
- [ ] Proper error handling across platform boundaries

### Code Quality

**Kotlin Style:**
- [ ] Immutable data classes (`val` not `var`)
- [ ] Proper naming conventions (PascalCase for classes, camelCase for functions)
- [ ] No nullable types when avoidable
- [ ] Extension functions used appropriately
- [ ] Sealed classes for finite states

**State Management:**
- [ ] StateFlow never exposed as MutableStateFlow
- [ ] State updates use `.update { it.copy(...) }`
- [ ] No direct state mutation
- [ ] Loading/error/data states included
- [ ] Structured concurrency (no GlobalScope)

**Error Handling:**
- [ ] Uses sealed error class hierarchy
- [ ] Result<T> wrapper in use cases
- [ ] User-friendly messages separate from technical details
- [ ] Proper error propagation through layers

**Logging:**
- [ ] Uses kotlin-logging with lazy evaluation
- [ ] Appropriate log levels (TRACE/DEBUG/INFO/WARN/ERROR)
- [ ] No sensitive data at INFO level
- [ ] Clear, actionable log messages

### Database (SQLDelight)

**Schema:**
- [ ] Proper indices on queried columns
- [ ] Foreign key constraints defined
- [ ] Soft deletes (deleted_at) where appropriate
- [ ] Parameterized queries (no string concatenation)

**Queries:**
- [ ] Named queries with clear purpose
- [ ] SELECT specific columns (not *)
- [ ] Transactions for multi-operation updates
- [ ] Proper time-based filtering

**Repository:**
- [ ] Proper mapping between DB and domain models
- [ ] Flow-based reactive queries
- [ ] Error handling with try/catch
- [ ] Batch operations use transactions

### Testing

**Coverage:**
- [ ] Tests exist for new code
- [ ] Coverage meets threshold (75%+)
- [ ] Tests follow AAA pattern
- [ ] Descriptive test names

**Test Quality:**
- [ ] Tests are independent
- [ ] Uses TestDatabaseHelper for in-memory DB
- [ ] Edge cases tested
- [ ] Happy path and error conditions covered

### Performance

**Efficiency:**
- [ ] No unnecessary allocations in hot paths
- [ ] Proper use of lazy initialization
- [ ] Database queries optimized
- [ ] Flow collection properly scoped

**Memory:**
- [ ] No memory leaks (proper lifecycle handling)
- [ ] Resources properly closed
- [ ] Coroutines properly cancelled

### Security

**Data Protection:**
- [ ] No hardcoded secrets or API keys
- [ ] Sensitive data not logged
- [ ] Proper input validation
- [ ] Encrypted storage for tokens

### Platform-Specific Issues

**Android:**
- [ ] Proper permission handling
- [ ] Lifecycle-aware components
- [ ] Background work uses WorkManager
- [ ] Compose best practices followed

**iOS:**
- [ ] Proper memory management (no retain cycles)
- [ ] Background tasks properly configured
- [ ] iOS API usage correct

## Review Process

### 1. Understand Context
- Read the changed files using Read tool
- Understand the feature being implemented
- Check related files for context

### 2. Check Architecture
```kotlin
// Verify layer structure
// Controller should only depend on use cases
class MyController @Inject constructor(
    private val useCase: MyUseCase,  // ✅ Good
    // private val repository: Repository  // ❌ Bad - skip layer
)
```

### 3. Review State Management
```kotlin
// ✅ Good: Immutable state updates
_state.update { it.copy(isLoading = true) }

// ❌ Bad: Direct mutation
_state.value.isLoading = true
```

### 4. Check Error Handling
```kotlin
// ✅ Good: Sealed error classes
sealed class AppError {
    abstract val message: String
    abstract val technicalDetails: String?
}

// ❌ Bad: Generic exceptions
throw Exception("Something went wrong")
```

### 5. Verify Testing
```bash
# Check test coverage
./gradlew koverHtmlReport

# Look for test files
# All new code should have tests
```

### 6. Platform Parity
```kotlin
// Ensure functional parity between platforms
// Android and iOS should have same features
// Only UI differs, not functionality
```

## Common Issues

### Architecture Violations

**Issue:** Controller directly accessing Repository
```kotlin
// ❌ Bad
class MyController(private val repository: Repository) {
    fun loadData() {
        repository.getData()  // Skip UseCase layer
    }
}

// ✅ Good
class MyController(private val useCase: GetDataUseCase) {
    fun loadData() {
        useCase.execute()
    }
}
```

**Issue:** Exposing MutableStateFlow
```kotlin
// ❌ Bad
val state: MutableStateFlow<State> = MutableStateFlow(State())

// ✅ Good
private val _state = MutableStateFlow(State())
val state: StateFlow<State> = _state.asStateFlow()
```

**Issue:** Using expect/actual unnecessarily
```kotlin
// ❌ Bad: Could be common code
expect fun getCurrentTime(): String

// ✅ Good: Use kotlinx.datetime
Clock.System.now()
```

### Code Quality Issues

**Issue:** Mutable state
```kotlin
// ❌ Bad
data class State(var isLoading: Boolean)

// ✅ Good
data class State(val isLoading: Boolean)
```

**Issue:** Not using lazy logging
```kotlin
// ❌ Bad: String always created
logger.debug("Processing: $data")

// ✅ Good: Lazy evaluation
logger.debug { "Processing: $data" }
```

**Issue:** Missing error handling
```kotlin
// ❌ Bad
suspend fun getData(): Data {
    return repository.getData()  // What if it fails?
}

// ✅ Good
suspend fun getData(): Result<Data> {
    return try {
        Result.success(repository.getData())
    } catch (e: Exception) {
        Result.failure(DatabaseError.QueryFailed(e.message))
    }
}
```

### SQLDelight Issues

**Issue:** No indices on queried columns
```sql
-- ❌ Bad: No index
SELECT * FROM place_visits
WHERE user_id = ?
ORDER BY start_time DESC;

-- ✅ Good: Add index
CREATE INDEX idx_place_visits_user_time
ON place_visits(user_id, start_time DESC);
```

**Issue:** Not using soft deletes
```sql
-- ❌ Bad: Hard delete
DELETE FROM place_visits WHERE id = ?;

-- ✅ Good: Soft delete
UPDATE place_visits
SET deleted_at = ?
WHERE id = ?;
```

## Review Output Format

Provide feedback in this structure:

### Critical Issues (Must Fix)
- Issue description
- Location (file:line)
- Why it's critical
- Suggested fix

### Important Issues (Should Fix)
- Issue description
- Location
- Impact
- Suggested improvement

### Suggestions (Consider)
- Enhancement idea
- Potential benefit
- Trade-offs

### Positive Observations
- What's done well
- Good patterns to maintain

## Commands

After review, suggest:
```bash
# Fix issues, then verify
./gradlew :shared:test
./gradlew koverVerify
./gradlew :composeApp:assembleDebug
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```

## Related Documentation

- `docs/ARCHITECTURE.md` - Architecture principles
- `docs/TESTING.md` - Testing requirements
- `.cursor/rules/kotlin-multiplatform.mdc` - KMP rules
- `AGENTS.md` - Project standards

---

*Use this skill to ensure code quality, architecture compliance, and KMP best practices in Trailglass.*
