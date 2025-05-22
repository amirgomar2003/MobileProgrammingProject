package com.example.noteproject

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.noteproject.ui.components.ThemeSwitch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userName: String,
    userEmail: String,
    userAvatar: Painter,
    onBack: () -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit,
    showLogoutDialog: Boolean,
    onDismissLogoutDialog: () -> Unit,
    onConfirmLogout: () -> Unit,
    darkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = userAvatar,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(56.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Divider()
            Text(
                "APP SETTINGS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )
            ListItem(
                leadingContent = {
                    Icon(Icons.Default.WbSunny, contentDescription = null)
                },
                headlineContent = {Text("Application Theme", fontWeight = FontWeight.Medium) },
                trailingContent = {
                    ThemeSwitch(darkTheme, onToggleTheme)
                }
            )
            ListItem(
                headlineContent = { Text("Change Password", fontWeight = FontWeight.Medium) },
                leadingContent = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                },
                modifier = Modifier
                    .clickable(onClick = onChangePassword)
                    .fillMaxWidth()
            )
            ListItem(
                headlineContent = {
                    Text("Log Out", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                },
                leadingContent = {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier
                    .clickable(onClick = onLogout)
                    .fillMaxWidth()
            )
            Spacer(Modifier.weight(1f))
            // App version at the bottom
            Text(
                "Taha Notes v1.1",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
            )
        }
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = onDismissLogoutDialog,
                title = { Text("Log Out", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to log out from the application?") },
                confirmButton = {
                    Button(onClick = onConfirmLogout) { Text("Yes") }
                },
                dismissButton = {
                    OutlinedButton(onClick = onDismissLogoutDialog) { Text("Cancel") }
                }
            )
        }
    }
}
