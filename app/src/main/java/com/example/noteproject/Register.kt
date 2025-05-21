package com.example.noteproject

import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.padding
import com.example.noteproject.ui.GrayText
import com.example.noteproject.ui.PurplePrimary
import com.example.noteproject.ui.components.LabeledTextField
import com.example.noteproject.ui.components.FormField
import com.example.noteproject.ui.components.OrDevider
import com.example.noteproject.ui.components.ButtonWithArrow
import com.example.noteproject.ui.components.ButtonWithArrowConfig
import com.example.noteproject.ui.components.ThemeSwitch


@Composable
fun RegisterScreen(
    onRegister: () -> Unit,
    onLoginClick: () -> Unit,
    darkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
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

    val registerButtonConfig = ButtonWithArrowConfig(
        label = "Register",
        backgroundColor = PurplePrimary,
        textColor = androidx.compose.ui.graphics.Color.White,
        onClick = onRegister
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
                            Icons.Default.ArrowBack,
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
