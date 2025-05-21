package com.example.noteproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.example.noteproject.ui.theme.NoteProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteProjectTheme {
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
                            onRegisterClick = { navController.navigate("register") }
                        )
                    }
                    composable("register") {
                        RegisterScreen(
                            onRegister = { /* Handle registration */ },
                            onLoginClick = { navController.navigate("login") }
                        )
                    }
                }
            }
        }
    }
}
