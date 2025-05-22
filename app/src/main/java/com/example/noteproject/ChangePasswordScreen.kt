package com.example.noteproject

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.alpha
import com.example.noteproject.ui.components.LabeledTextField
import com.example.noteproject.ui.components.ButtonWithArrow
import com.example.noteproject.ui.components.ButtonWithArrowConfig

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
    onRetypePasswordChange: (String) -> Unit
) {
    // Validation constraints.
    val currentPasswordError = currentPassword.isBlank()
    val newPasswordError = newPassword.isBlank() || !isStrongPassword(newPassword)
    val retypePasswordError = retypePassword.isBlank() || newPassword != retypePassword

    // The form is valid only if all fields are filled and valid.
    val isFormValid = !currentPasswordError && !newPasswordError && !retypePasswordError

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Password") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            Divider()
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
            Spacer(Modifier.weight(1f))
            ButtonWithArrow(
                ButtonWithArrowConfig(
                    label = "Submit New Password",
                    onClick = { onSubmit(currentPassword, newPassword, retypePassword) },
                    enabled = isFormValid,
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
