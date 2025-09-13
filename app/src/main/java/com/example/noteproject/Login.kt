package com.example.noteproject

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import com.example.noteproject.ui.GrayText
import com.example.noteproject.ui.PurplePrimary
import com.example.noteproject.ui.components.LabeledTextField
import com.example.noteproject.ui.components.FormField
import com.example.noteproject.ui.components.OrDevider
import com.example.noteproject.ui.components.ButtonWithArrow
import com.example.noteproject.ui.components.ButtonWithArrowConfig
import com.example.noteproject.ui.components.ThemeSwitch
import com.example.noteproject.ui.viewmodel.AuthUiState


@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onRegisterClick: () -> Unit,
    darkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    authUiState: AuthUiState,
    onClearError: () -> Unit
) {
    val fields = remember {
        mutableStateListOf(
            FormField("Username", "Example: hamifar.taha"),
            FormField("Password", "********", isPassword = true),
        )
    }
    
    // Clear error when user starts typing
    LaunchedEffect(fields[0].value, fields[1].value) {
        if (authUiState.errorMessage != null) {
            onClearError()
        }
    }
    
    val loginButtonConfig = ButtonWithArrowConfig(
        label = if (authUiState.isLoading) "Logging in..." else "Login",
        backgroundColor = PurplePrimary,
        textColor = Color.White,
        onClick = { 
            val username = fields[0].value.trim()
            val password = fields[1].value
            if (username.isNotEmpty() && password.isNotEmpty()) {
                onLogin(username, password)
            }
        },
        enabled = !authUiState.isLoading && fields[0].value.trim().isNotEmpty() && fields[1].value.isNotEmpty()
    )

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ThemeSwitch(
                isDark = darkTheme,
                onToggle = onToggleTheme,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .align(Alignment.Center)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(Modifier.height(32.dp))
                Text(
                    "Let's Login",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "And notes your idea",
                    color = GrayText,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(32.dp))
                fields.forEachIndexed { index, field ->
                    LabeledTextField(
                        label = field.label,
                        value = field.value,
                        onValueChange = { newValue -> fields[index] = field.copy(value = newValue) },
                        placeholder = field.placeholder,
                        isPassword = field.isPassword
                    )
                }
                Spacer(Modifier.height(24.dp))
                
                // Show success message if any
                authUiState.successMessage?.let { successMessage ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = successMessage,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
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
                
                ButtonWithArrow(loginButtonConfig)
                Spacer(Modifier.height(24.dp))
                OrDevider()
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = onRegisterClick) {
                        Text("Don’t have any account? Register here", color = PurplePrimary)
                    }
                }
            }
        }
    }
}
