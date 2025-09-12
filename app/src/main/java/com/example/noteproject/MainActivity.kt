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
import com.example.noteproject.ui.viewmodel.NotesViewModel
import com.example.noteproject.ui.viewmodel.toUiNote

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            var showLogoutDialog by remember { mutableStateOf(false) }
            val authViewModel: AuthViewModel = viewModel()
            val notesViewModel: NotesViewModel = viewModel()
            val authUiState by authViewModel.uiState.collectAsState()
            val notesUiState by notesViewModel.uiState.collectAsState()

            // Convert backend notes to UI notes for backward compatibility
            val notes = remember(notesUiState.notes) {
                mutableStateListOf<Note>().apply {
                    clear()
                    addAll(notesUiState.notes.map { it.toUiNote() })
                }
            }
            
            var showDeleteDialog by remember { mutableStateOf(false) }
            var searchQuery by remember { mutableStateOf("") }
            
            // Track if user has any notes (for search bar visibility)
            var userHasNotes by remember { mutableStateOf(false) }
            
            // Update userHasNotes when loading all notes (not search results)
            LaunchedEffect(notesUiState.notes, searchQuery) {
                // Only update when not searching (to get the true total)
                if (searchQuery.isBlank() && notesUiState.notes.isNotEmpty()) {
                    userHasNotes = true
                }
            }
            
            // Debounce search to avoid excessive API calls
            LaunchedEffect(searchQuery) {
                kotlinx.coroutines.delay(150) // Optimized delay for better responsiveness
                if (searchQuery.isNotBlank()) {
                    notesViewModel.searchNotes(searchQuery)
                } else {
                    notesViewModel.loadNotes(refresh = true) // Reset to all notes when search is cleared
                }
            }
            
            // Load notes when user logs in
            LaunchedEffect(authUiState.isLoggedIn) {
                if (authUiState.isLoggedIn) {
                    notesViewModel.loadNotes(refresh = true)
                }
            }

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
                            onSearchQueryChange = { 
                                searchQuery = it
                            },
                            onAddNote = {
                                // Clear any previous errors
                                notesViewModel.clearError()
                                
                                // Navigate to editor with special new note ID
                                navController.navigate("note_editor/-1")
                            },
                            onNoteClick = { note ->
                                navController.navigate("note_editor/${note.id}")
                            },
                            onSettingsClick = {
                                navController.navigate("settings")
                            },
                            isDarkTheme = isDarkTheme,
                            isLoading = notesUiState.isLoading,
                            errorMessage = notesUiState.errorMessage,
                            onRetry = { notesViewModel.refresh() },
                            onLoadMore = { notesViewModel.loadNextPage() },
                            hasNextPage = notesUiState.hasNextPage,
                            onRefresh = {
                                searchQuery = ""
                                notesViewModel.loadNotes(refresh = true)
                            },
                            hasNotes = userHasNotes,
                            isOfflineMode = notesUiState.isOfflineMode,
                            isSyncing = notesUiState.isSyncing,
                            onSyncClick = { notesViewModel.syncNow() }
                        )
                    }
                    composable(
                        "note_editor/{noteId}",
                        arguments = listOf(navArgument("noteId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getInt("noteId") ?: return@composable
                        
                        // Handle new note creation (ID = -1)
                        val note = if (noteId == -1) {
                            // Create a temporary note for new note creation
                            Note(
                                id = -1,
                                header = "",
                                body = "",
                                lastEdited = System.currentTimeMillis()
                            )
                        } else {
                            notes.find { it.id == noteId }
                        }
                        
                        if (note != null) {
                            // For new notes, we'll track state via the editor's internal state
                            // and get the values through the save callback
                            
                            NoteEditorPage(
                                note = note,
                                onHeaderChange = { newHeader ->
                                    if (noteId != -1) {
                                        note.header = newHeader
                                        note.lastEdited = System.currentTimeMillis()
                                    }
                                    // For new notes (ID = -1), we don't need to update the note object
                                    // since the editor manages its own state
                                },
                                onBodyChange = { newBody ->
                                    if (noteId != -1) {
                                        note.body = newBody
                                        note.lastEdited = System.currentTimeMillis()
                                    }
                                    // For new notes (ID = -1), we don't need to update the note object
                                },
                                onBack = { 
                                    // For new notes (ID = -1), just go back without saving
                                    // For existing notes, only remove if empty and temporary
                                    if (note.id < 0 && note.id != -1 && note.header.isBlank() && note.body.isBlank()) {
                                        notes.remove(note)
                                    }
                                    navController.popBackStack() 
                                },
                                onDelete = { showDeleteDialog = true },
                                showDeleteDialog = showDeleteDialog,
                                onDismissDelete = { showDeleteDialog = false },
                                onConfirmDelete = {
                                    if (note.id == -1) {
                                        // It's a new note, just go back
                                        showDeleteDialog = false
                                        navController.popBackStack("home", false)
                                    } else if (note.id < 0) {
                                        // It's a temporary note, remove locally
                                        notes.remove(note)
                                        showDeleteDialog = false
                                        navController.popBackStack("home", false)
                                    } else {
                                        // It's a real note, delete from backend
                                        notesViewModel.deleteNote(note.id) {
                                            showDeleteDialog = false
                                            navController.popBackStack("home", false)
                                        }
                                    }
                                },
                                onSave = { currentTitle, currentBody ->
                                    // Exception blank field.
                                    if (currentTitle.isBlank() || currentBody.isBlank()) {
                                        // Don't save notes with any blank fields, just go back
                                        navController.popBackStack()
                                        return@NoteEditorPage
                                    }
                                    // New note
                                    if (noteId == -1) {
                                        // Both title and description are guaranteed to be non-blank due to validation above
                                        notesViewModel.createNote(
                                            title = currentTitle,
                                            description = currentBody
                                        ) { createdNote ->
                                            // Add the new note to the front of the list
                                            notes.add(0, createdNote.toUiNote())
                                            navController.popBackStack()
                                        }
                                    // Temporarly note
                                    } else if (note.id < 0) {
                                        // It's a temporary note that was created in the old flow, create it on backend
                                        // Both title and description are guaranteed to be non-blank due to validation above
                                        notesViewModel.createNote(
                                            title = currentTitle,
                                            description = currentBody
                                        ) { createdNote ->
                                            // Replace the temporary note with the real one
                                            val index = notes.indexOf(note)
                                            if (index >= 0) {
                                                notes[index] = createdNote.toUiNote()
                                            }
                                            navController.popBackStack()
                                        }
                                    // Update note
                                    } else {
                                        // It's an existing note, update it
                                        notesViewModel.updateNote(
                                            id = note.id,
                                            title = currentTitle.ifBlank { "" },
                                            description = currentBody.ifBlank { "" }
                                        ) { updatedNote ->
                                            // Update the local note with backend data
                                            val index = notes.indexOf(note)
                                            if (index >= 0) {
                                                notes[index] = updatedNote.toUiNote()
                                            }
                                            navController.popBackStack()
                                        }
                                    }
                                },
                                isLoading = notesUiState.isLoading,
                                errorMessage = notesUiState.errorMessage
                            )
                        }
                        // No else block! Don't navigate here if note is null.
                    }
                }
            }
        }
    }
}