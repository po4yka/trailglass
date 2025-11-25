package com.po4yka.trailglass.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.feature.auth.AuthController

/** Login screen for user authentication. */
@Composable
fun LoginScreen(
    authController: AuthController,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState by authController.state.collectAsState()
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isLoading = authState is AuthController.AuthState.Loading
    val errorMessage = (authState as? AuthController.AuthState.Error)?.message

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App logo/title
        Icon(
            imageVector = Icons.Default.Map,
            contentDescription = "TrailGlass",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "TrailGlass",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Your journeys, captured as they unfold",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Error message
        if (errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                authController.clearError()
            },
            label = { Text("Email") },
            placeholder = { Text("you@example.com") },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = "Email")
            },
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
            keyboardActions =
                KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                authController.clearError()
            },
            label = { Text("Password") },
            placeholder = { Text("Enter your password") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "Password")
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector =
                            if (passwordVisible) {
                                Icons.Default.Visibility
                            } else {
                                Icons.Default.VisibilityOff
                            },
                        contentDescription =
                            if (passwordVisible) {
                                "Hide password"
                            } else {
                                "Show password"
                            }
                    )
                }
            },
            visualTransformation =
                if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
            keyboardActions =
                KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (email.isNotBlank() && password.isNotBlank()) {
                            authController.login(email, password)
                        }
                    }
                ),
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Forgot password
        TextButton(
            onClick = onNavigateToForgotPassword,
            enabled = !isLoading
        ) {
            Text("Forgot password?")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login button
        Button(
            onClick = {
                focusManager.clearFocus()
                authController.login(email, password)
            },
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Log In")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Register link
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = onNavigateToRegister,
                enabled = !isLoading
            ) {
                Text("Sign up")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Continue as Guest
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        TextButton(
            onClick = { authController.continueAsGuest() },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Continue as Guest")
        }

        Text(
            text = "No account needed. All data stored locally only.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
