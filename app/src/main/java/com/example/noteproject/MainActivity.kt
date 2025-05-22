package com.example.noteproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.*
import com.example.noteproject.ui.theme.NoteProjectTheme
import com.example.noteproject.ui.components.Note

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            var showLogoutDialog by remember { mutableStateOf(false) }

            val notes = remember { mutableStateListOf<Note>() }
            var showDeleteDialog by remember { mutableStateOf(false) }
            var searchQuery by remember { mutableStateOf("") }

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
                            onLogin = { navController.navigate("home") },
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
                            userAvatar = painterResource(id = R.drawable.onboarding),
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
                                navController.popBackStack()
                            },
                            currentPassword = currentPassword,
                            newPassword = newPassword,
                            retypePassword = retypePassword,
                            onCurrentPasswordChange = { currentPassword = it },
                            onNewPasswordChange = { newPassword = it },
                            onRetypePasswordChange = { retypePassword = it }
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