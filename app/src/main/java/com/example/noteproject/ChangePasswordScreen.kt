package com.example.noteproject

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.noteproject.ui.components.LabeledTextField
import com.example.noteproject.ui.components.ButtonWithArrow
import com.example.noteproject.ui.components.ButtonWithArrowConfig
import com.example.noteproject.ui.viewmodel.AuthUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
    onSubmit: (String, String, String) -> Unit,
    currentPassword: String,
    newPassword: String,
    retypePassword: String,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onRetypePasswordChange: (String) -> Unit,
    authUiState: AuthUiState,
    onClearError: () -> Unit,
    onClearSuccess: () -> Unit
) {
    // Validation constraints.
    val currentPasswordError = currentPassword.isBlank()
    val newPasswordError = newPassword.isBlank() || !isStrongPassword(newPassword)
    val retypePasswordError = retypePassword.isBlank() || newPassword != retypePassword

    // The form is valid only if all fields are filled and valid.
    val isFormValid = !currentPasswordError && !newPasswordError && !retypePasswordError

    // Clear error/success when user starts typing
    LaunchedEffect(currentPassword, newPassword, retypePassword) {
        if (authUiState.errorMessage != null) {
            onClearError()
        }
        if (authUiState.successMessage != null) {
            onClearSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Password") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                "Please input your current password first",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            LabeledTextField(
                label = "Current Password",
                value = currentPassword,
                onValueChange = onCurrentPasswordChange,
                isPassword = true
            )
            if (currentPasswordError) {
                Text(
                    "Current password cannot be empty",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
            Text(
                "Now, create your new password",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            LabeledTextField(
                label = "New Password",
                value = newPassword,
                onValueChange = onNewPasswordChange,
                isPassword = true,
                placeholder = "Password should contain a-z, A-Z, 0-9"
            )
            if (newPassword.isNotBlank() && !isStrongPassword(newPassword)) {
                Text(
                    "Password must be at least 8 characters and include a-z, A-Z, and 0-9",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            } else if (newPassword.isBlank()) {
                Text(
                    "New password cannot be empty",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            LabeledTextField(
                label = "Retype New Password",
                value = retypePassword,
                onValueChange = onRetypePasswordChange,
                isPassword = true
            )
            if (retypePassword.isNotBlank() && newPassword != retypePassword) {
                Text(
                    "Passwords do not match",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            } else if (retypePassword.isBlank()) {
                Text(
                    "Please retype the new password",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            
            // Show success message if any
            authUiState.successMessage?.let { successMessage ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = successMessage,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
            
            // Show error message if any
            authUiState.errorMessage?.let { errorMessage ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (authUiState.isNetworkError) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (authUiState.isNetworkError) {
                                Icons.Default.Warning
                            } else {
                                Icons.Default.Info
                            },
                            contentDescription = null,
                            tint = if (authUiState.isNetworkError) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onErrorContainer
                            },
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = errorMessage,
                            color = if (authUiState.isNetworkError) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onErrorContainer
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            
            Spacer(Modifier.weight(1f))
            ButtonWithArrow(
                ButtonWithArrowConfig(
                    label = if (authUiState.isLoading) "Changing Password..." else "Submit New Password",
                    onClick = { onSubmit(currentPassword, newPassword, retypePassword) },
                    enabled = isFormValid && !authUiState.isLoading,
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    textColor = MaterialTheme.colorScheme.onPrimary
                ),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

fun isStrongPassword(password: String): Boolean {
    val lengthOk = password.length >= 8
    val hasLower = password.any { it.isLowerCase() }
    val hasUpper = password.any { it.isUpperCase() }
    val hasDigit = password.any { it.isDigit() }
    return lengthOk && hasLower && hasUpper && hasDigit
}
