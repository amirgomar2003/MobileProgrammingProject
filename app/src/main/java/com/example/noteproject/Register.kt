package com.example.noteproject

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun RegisterScreen(
    onRegister: (String, String, String, String, String) -> Unit,
    onLoginClick: () -> Unit,
    darkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    authUiState: AuthUiState,
    onClearError: () -> Unit
) {
    val fields = remember {
        mutableStateListOf(
            FormField("First Name", "Example: Taha"),
            FormField("Last Name", "Example: Hamifar"),
            FormField("Username", "Example: @HamifarTaha"),
            FormField("Email Address", "Example: hamifar.taha@gmail.com"),
            FormField("Password", "********", isPassword = true),
            FormField("Retype Password", "********", isPassword = true)
        )
    }

    // Clear error when user starts typing
    LaunchedEffect(fields.map { it.value }) {
        if (authUiState.errorMessage != null) {
            onClearError()
        }
    }

    val registerButtonConfig = ButtonWithArrowConfig(
        label = if (authUiState.isLoading) "Registering..." else "Register",
        backgroundColor = PurplePrimary,
        textColor = androidx.compose.ui.graphics.Color.White,
        onClick = { 
            val firstName = fields[0].value.trim()
            val lastName = fields[1].value.trim()
            val username = fields[2].value.trim()
            val email = fields[3].value.trim()
            val password = fields[4].value
            val retypePassword = fields[5].value
            
            if (firstName.isNotEmpty() && lastName.isNotEmpty() && 
                username.isNotEmpty() && email.isNotEmpty() && 
                password.isNotEmpty() && password == retypePassword) {
                onRegister(username, password, email, firstName, lastName)
            }
        },
        enabled = !authUiState.isLoading && fields.all { it.value.trim().isNotEmpty() } && 
                 fields[4].value == fields[5].value
    )

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .align(Alignment.TopCenter)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 72.dp), // prevent overlap with switch
                verticalArrangement = Arrangement.Top
            ) {
                TextButton(
                    onClick = onLoginClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = PurplePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Back to Login",
                            color = PurplePrimary,
                            fontSize = 16.sp
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Register",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "And start taking notes",
                    color = GrayText,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(24.dp))
                fields.forEachIndexed { index, field ->
                    LabeledTextField(
                        label = field.label,
                        value = field.value,
                        onValueChange = { newValue ->
                            fields[index] = field.copy(value = newValue)
                        },
                        placeholder = field.placeholder,
                        isPassword = field.isPassword
                    )
                }
                Spacer(Modifier.height(24.dp))
                
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
                
                ButtonWithArrow(registerButtonConfig)
                Spacer(Modifier.height(24.dp))
                OrDevider()
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = onLoginClick) {
                        Text("Already have an account? Login", color = PurplePrimary)
                    }
                }
            }
            ThemeSwitch(
                isDark = darkTheme,
                onToggle = onToggleTheme,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
        }
    }
}
