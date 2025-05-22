package com.example.noteproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.example.noteproject.ui.theme.NoteProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            var showLogoutDialog by remember { mutableStateOf(false) }

            NoteProjectTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "onboarding") {
                    composable("onboarding") {
                        OnboardingScreen(
                            onGetStartedClick = { navController.navigate("login") }
                        )
                    }
                    composable("login") {
                        LoginScreen(
                            onLogin = { navController.navigate("settings") },
                            onRegisterClick = { navController.navigate("register") },
                            darkTheme = isDarkTheme,
                            onToggleTheme = { isDarkTheme = it }
                        )
                    }
                    composable("register") {
                        RegisterScreen(
                            onRegister = { navController.navigate("settings") },
                            onLoginClick = { navController.navigate("login") },
                            darkTheme = isDarkTheme,
                            onToggleTheme = { isDarkTheme = it }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            userName = "Taha Hamifar",
                            userEmail = "hamifar.taha@gmail.com",
                            userAvatar = painterResource(id = R.drawable.onboarding), // Mock avatar
                            onBack = { navController.popBackStack() },
                            onChangePassword = { navController.navigate("change_password") },
                            onLogout = { showLogoutDialog = true },
                            showLogoutDialog = showLogoutDialog,
                            onDismissLogoutDialog = { showLogoutDialog = false },
                            onConfirmLogout = {
                                showLogoutDialog = false
                                navController.navigate("login") {
                                    popUpTo("onboarding") { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            darkTheme = isDarkTheme,
                            onToggleTheme = { isDarkTheme = it }
                        )
                    }
                    composable("change_password") {
                        var currentPassword by remember { mutableStateOf("") }
                        var newPassword by remember { mutableStateOf("") }
                        var retypePassword by remember { mutableStateOf("") }
                        ChangePasswordScreen(
                            onBack = { navController.popBackStack() },
                            onSubmit = { old, new, retype ->
                                // Handle change password logic
                                navController.popBackStack() // Go back to settings
                            },
                            currentPassword = currentPassword,
                            newPassword = newPassword,
                            retypePassword = retypePassword,
                            onCurrentPasswordChange = { currentPassword = it },
                            onNewPasswordChange = { newPassword = it },
                            onRetypePasswordChange = { retypePassword = it }
                        )
                    }
                }
            }
        }
    }
}
