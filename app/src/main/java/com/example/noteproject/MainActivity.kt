// MainActivity.kt
package com.example.noteproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.example.noteproject.ui.theme.NoteProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }

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
                            onLogin = { /* Handle login */ },
                            onRegisterClick = { navController.navigate("register") },
                            darkTheme = isDarkTheme,
                            onToggleTheme = { isDarkTheme = it }
                        )
                    }
                    composable("register") {
                        RegisterScreen(
                            onRegister = { /* Handle registration */ },
                            onLoginClick = { navController.navigate("login") },
                            darkTheme = isDarkTheme,
                            onToggleTheme = { isDarkTheme = it }
                        )
                    }
                }
            }
        }
    }
}
