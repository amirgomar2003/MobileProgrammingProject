package com.example.noteproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.*
import com.example.noteproject.ui.theme.NoteProjectTheme
import com.example.noteproject.ui.components.Note
import com.example.noteproject.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            var showLogoutDialog by remember { mutableStateOf(false) }
            val authViewModel: AuthViewModel = viewModel()
            val authUiState by authViewModel.uiState.collectAsState()

            val notes = remember { mutableStateListOf<Note>() }
            var showDeleteDialog by remember { mutableStateOf(false) }
            var searchQuery by remember { mutableStateOf("") }

            NoteProjectTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                
                // Determine start destination based on login status
                val startDestination = if (authUiState.isLoggedIn) "home" else "onboarding"
                
                NavHost(navController, startDestination = startDestination) {
                    composable("onboarding") {
                        OnboardingScreen(
                            onGetStartedClick = { navController.navigate("login") }
                        )
                    }
                    composable("login") {
                        LoginScreen(
                            onLogin = { username, password ->
                                authViewModel.login(username, password) {
                                    navController.navigate("home") {
                                        popUpTo("onboarding") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            },
                            onRegisterClick = { navController.navigate("register") },
                            darkTheme = isDarkTheme,
                            onToggleTheme = { isDarkTheme = it },
                            authUiState = authUiState,
                            onClearError = { authViewModel.clearError() }
                        )
                    }
                    composable("register") {
                        RegisterScreen(
                            onRegister = { username, password, email, firstName, lastName ->
                                authViewModel.register(username, password, email, firstName, lastName) {
                                    navController.navigate("home") {
                                        popUpTo("onboarding") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            },
                            onLoginClick = { navController.navigate("login") },
                            darkTheme = isDarkTheme,
                            onToggleTheme = { isDarkTheme = it },
                            authUiState = authUiState,
                            onClearError = { authViewModel.clearError() }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            userName = authUiState.userInfo?.let { "${it.firstName} ${it.lastName}" } ?: "User",
                            userEmail = authUiState.userInfo?.email ?: "user@example.com",
                            userAvatar = painterResource(id = R.drawable.onboarding),
                            onBack = { navController.popBackStack() },
                            onChangePassword = { navController.navigate("change_password") },
                            onLogout = { showLogoutDialog = true },
                            showLogoutDialog = showLogoutDialog,
                            onDismissLogoutDialog = { showLogoutDialog = false },
                            onConfirmLogout = {
                                showLogoutDialog = false
                                authViewModel.logout()
                                navController.navigate("onboarding") {
                                    popUpTo("home") { inclusive = true }
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
                                authViewModel.changePassword(old, new) {
                                    // After successful password change, navigate back to settings
                                    navController.popBackStack()
                                }
                            },
                            currentPassword = currentPassword,
                            newPassword = newPassword,
                            retypePassword = retypePassword,
                            onCurrentPasswordChange = { currentPassword = it },
                            onNewPasswordChange = { newPassword = it },
                            onRetypePasswordChange = { retypePassword = it },
                            authUiState = authUiState,
                            onClearError = { authViewModel.clearError() },
                            onClearSuccess = { authViewModel.clearSuccess() }
                        )
                    }
                    composable("home") {
                        NotesMainPage(
                            notes = notes,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            onAddNote = {
                                val newId = (notes.maxOfOrNull { it.id } ?: 0) + 1
                                val newNote = Note(
                                    id = newId,
                                    header = "",
                                    body = "",
                                    lastEdited = System.currentTimeMillis()
                                )
                                notes.add(0, newNote)
                                navController.navigate("note_editor/$newId")
                            },
                            onNoteClick = { note ->
                                navController.navigate("note_editor/${note.id}")
                            },
                            onSettingsClick = {
                                navController.navigate("settings")
                            },
                            isDarkTheme = isDarkTheme
                        )
                    }
                    composable(
                        "note_editor/{noteId}",
                        arguments = listOf(navArgument("noteId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getInt("noteId") ?: return@composable
                        val note = notes.find { it.id == noteId }
                        if (note != null) {
                            NoteEditorPage(
                                note = note,
                                onHeaderChange = {
                                    note.header = it
                                    note.lastEdited = System.currentTimeMillis()
                                },
                                onBodyChange = {
                                    note.body = it
                                    note.lastEdited = System.currentTimeMillis()
                                },
                                onBack = { navController.popBackStack() },
                                onDelete = { showDeleteDialog = true },
                                showDeleteDialog = showDeleteDialog,
                                onDismissDelete = { showDeleteDialog = false },
                                onConfirmDelete = {
                                    notes.remove(note)
                                    showDeleteDialog = false
                                    navController.popBackStack("home", false)
                                },
                                onSave = { navController.popBackStack() }
                            )
                        }
                        // No else block! Don't navigate here if note is null.
                    }
                }
            }
        }
    }
}