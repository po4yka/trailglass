# Guest Mode (Offline Mode)

## Overview

Guest Mode allows users to use Trailglass without creating an account or logging in. All data is stored locally on the device, and no cloud synchronization occurs. This is perfect for users who want to:

- Try the app before committing to creating an account
- Use the app without sharing any data with servers
- Keep their location data completely private and offline
- Test the app's functionality

## Architecture

### User Session Management

Guest mode is managed through the `UserSession` interface, which supports three states:

1. **Authenticated** - User has a valid account and is logged in
2. **Guest** - User is using the app without an account (local-only)
3. **Unauthenticated** - User is not logged in and not in guest mode

```kotlin
interface UserSession {
    companion object {
        const val GUEST_USER_ID = "guest_user"
        const val DEFAULT_USER_ID = "default_user"
    }

    fun getCurrentUserId(): String?
    fun isAuthenticated(): Boolean
    fun isGuest(): Boolean
    fun setUserId(userId: String?)
}
```

When a user selects "Continue as Guest", the session is set with `GUEST_USER_ID`, which signals to the app that:
- No authentication is required
- All data is local-only
- Cloud sync should be skipped

### Authentication States

The `AuthController` manages authentication state with the following states:

```kotlin
sealed class AuthState {
    object Initializing : AuthState()
    object Unauthenticated : AuthState()
    object Guest : AuthState()                    // Guest mode
    data class Authenticated(...) : AuthState()
    data class Loading(...) : AuthState()
    data class Error(...) : AuthState()
}
```

### Guest Mode Flow

1. User launches app and sees login/register screen
2. User taps "Continue as Guest" button
3. `AuthController.continueAsGuest()` is called
4. Session is set to `GUEST_USER_ID`
5. Auth state changes to `AuthState.Guest`
6. Navigation system detects guest state and navigates to main app
7. All features work normally except cloud sync

## Implementation Details

### AuthController

Location: `shared/src/commonMain/kotlin/com/po4yka/trailglass/feature/auth/AuthController.kt`

Key methods:

```kotlin
fun continueAsGuest() {
    controllerScope.launch {
        try {
            userSession.setUserId(UserSession.GUEST_USER_ID)
            _state.value = AuthState.Guest
            logger.info { "Guest mode activated" }
        } catch (e: Exception) {
            _state.value = AuthState.Error(
                "Failed to enter guest mode: ${e.message}",
                AuthState.Unauthenticated
            )
        }
    }
}

fun isGuest(): Boolean {
    return _state.value is AuthState.Guest ||
            userSession.getCurrentUserId() == UserSession.GUEST_USER_ID
}

fun checkAuthStatus() {
    controllerScope.launch {
        try {
            // Check if user is in guest mode
            if (userSession.isGuest()) {
                logger.debug { "User is in guest mode" }
                _state.value = AuthState.Guest
                return@launch
            }
            // ... check for authenticated user
        } catch (e: Exception) {
            _state.value = AuthState.Unauthenticated
        }
    }
}
```

### UI Changes

#### LoginScreen

Location: `composeApp/src/main/kotlin/com/po4yka/trailglass/ui/screens/auth/LoginScreen.kt`

Added "Continue as Guest" button below the register link:

```kotlin
// Continue as Guest
HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

TextButton(
    onClick = { authController.continueAsGuest() },
    enabled = !isLoading,
    modifier = Modifier.fillMaxWidth()
) {
    Icon(imageVector = Icons.Default.Person, ...)
    Text("Continue as Guest")
}

Text(
    text = "No account needed. All data stored locally only.",
    style = MaterialTheme.typography.bodySmall,
    ...
)
```

#### RegisterScreen

Location: `composeApp/src/main/kotlin/com/po4yka/trailglass/ui/screens/auth/RegisterScreen.kt`

Same "Continue as Guest" button added below the login link.

#### AuthNavigation

Location: `composeApp/src/main/kotlin/com/po4yka/trailglass/ui/navigation/AuthNavigation.kt`

Updated navigation logic to handle guest mode:

```kotlin
LaunchedEffect(authState) {
    when (authState) {
        is AuthController.AuthState.Authenticated,
        is AuthController.AuthState.Guest -> {
            authRootComponent.onAuthenticated()
        }
        else -> {
            // Stay on auth screens
        }
    }
}
```

### Sync Behavior

Location: `shared/src/commonMain/kotlin/com/po4yka/trailglass/data/sync/SyncManager.kt`

Cloud sync is automatically skipped for guest users:

```kotlin
suspend fun performFullSync(): Result<SyncResultSummary> {
    // Skip sync for guest users (no account, local-only mode)
    if (userId == UserSession.GUEST_USER_ID) {
        logger.debug { "Skipping sync: user is in guest mode (local-only)" }
        _syncProgress.value = SyncProgress.Idle
        return Result.success(
            SyncResultSummary(
                uploaded = 0,
                downloaded = 0,
                conflicts = 0,
                conflictsResolved = 0,
                errors = 0
            )
        )
    }

    // ... normal sync logic
}
```

## User Experience

### Guest Mode Features

All core features work in guest mode:
- Location tracking
- Place visit detection
- Trip creation
- Photo attachments
- Journal entries
- Timeline view
- Map view
- Settings

### Limitations in Guest Mode

- No cloud backup
- No data sync across devices
- No account recovery
- Cannot share data with other users
- Data is lost if app is uninstalled

### Upgrading from Guest to Account

Currently not implemented. Future enhancement could allow:
1. User creates account while in guest mode
2. All local data is uploaded to new account
3. User gains all benefits of authenticated account

## Testing

### Manual Testing Steps

1. Launch app from fresh install
2. Verify login/register screen displays
3. Tap "Continue as Guest"
4. Verify navigation to main app
5. Create location samples, visits, trips
6. Verify data persists locally
7. Verify no sync attempts occur
8. Close and reopen app
9. Verify guest mode is restored
10. Verify data persists

### Automated Testing

Add tests to verify:
- Guest state transitions
- Sync skipping logic
- Navigation handling
- Session persistence

## Future Enhancements

1. **Guest to Account Migration**: Allow converting guest session to full account
2. **Export Data**: Allow exporting guest data before account creation
3. **Guest Mode Indicator**: Show persistent UI indicator when in guest mode
4. **Data Size Warning**: Warn users about local storage limits
5. **Account Benefits Promotion**: Periodically suggest account creation benefits

## Security Considerations

- Guest data is stored unencrypted on device
- No server-side security for guest data
- User should understand data is not backed up
- Clear data privacy implications in UI

## Privacy Benefits

Guest mode provides maximum privacy:
- No user registration or personal info collected
- No data transmitted to servers
- No analytics or tracking (if configured)
- Location data never leaves device
- Complete offline functionality
