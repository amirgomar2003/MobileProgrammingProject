package com.example.noteproject

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteproject.ui.GrayText
import com.example.noteproject.ui.PurplePrimary
import com.example.noteproject.ui.components.LabeledTextField
import com.example.noteproject.ui.components.FormField
import com.example.noteproject.ui.components.OrDevider
import com.example.noteproject.ui.components.ButtonWithArrow
import com.example.noteproject.ui.components.ButtonWithArrowConfig
import com.example.noteproject.ui.components.ThemeSwitch


@Composable
fun LoginScreen(
    onLogin: () -> Unit,
    onRegisterClick: () -> Unit,
    darkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    val fields = remember {
        mutableStateListOf(
            FormField("Email Address", "Example: hamifar.taha@gmail.com"),
            FormField("Password", "********", isPassword = true),
        )
    }
    val loginButtonConfig = ButtonWithArrowConfig(
        label = "Login",
        backgroundColor = PurplePrimary,
        textColor = Color.White,
        onClick = onLogin
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
                    .padding(16.dp)
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
