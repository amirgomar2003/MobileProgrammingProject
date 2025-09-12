package com.example.noteproject.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteproject.data.api.ApiClient
import com.example.noteproject.data.local.NetworkMonitor
import com.example.noteproject.data.local.NotesDatabase
import com.example.noteproject.data.local.TokenManager
import com.example.noteproject.data.model.*
import com.example.noteproject.data.repository.ApiResult
import com.example.noteproject.data.repository.OfflineFirstNotesRepository
import com.example.noteproject.data.sync.SyncWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class NotesUiState(
    val isLoading: Boolean = false,
    val notes: List<NoteResponse> = emptyList(),
    val currentNote: NoteResponse? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isNetworkError: Boolean = false,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false,
    val currentPage: Int = 1,
    val totalCount: Int = 0,
    val isOfflineMode: Boolean = false,
    val isSyncing: Boolean = false
)

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context = application
    private val tokenManager = TokenManager(application)
    private val apiClient = ApiClient(tokenManager)
    private val database = NotesDatabase.getDatabase(application)
    private val networkMonitor = NetworkMonitor(application)
    private val notesRepository = OfflineFirstNotesRepository(
        context = application,
        apiClient = apiClient,
        tokenManager = tokenManager,
        database = database,
        networkMonitor = networkMonitor
    )
    
    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()
    
    private var currentSearchQuery: String? = null
    
    init {
        // Monitor network connectivity
        viewModelScope.launch {
            networkMonitor.isConnected.collect { isConnected ->
                _uiState.value = _uiState.value.copy(
                    isOfflineMode = !isConnected
                )
                
                // Schedule sync when coming back online
                if (isConnected && tokenManager.isLoggedIn()) {
                    SyncWorker.scheduleImmediateSync(context)
                }
            }
        }
        
        // Start periodic sync and load initial data
        if (tokenManager.isLoggedIn()) {
            SyncWorker.schedulePeriodicSync(context)
            loadNotes()
            observeNotes()
        }
    }
    
    // Monitor authentication state
    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()
    
    // Check if user is still authenticated after an error
    private fun handleAuthenticationError(errorMessage: String): Boolean {
        return if (errorMessage.contains("Not authenticated", ignoreCase = true) ||
                   errorMessage.contains("Authentication failed", ignoreCase = true)) {
            // Check if tokens were cleared by the interceptor
            !tokenManager.isLoggedIn()
        } else {
            false
        }
    }
    
    fun loadNotes(page: Int = 1, pageSize: Int = 20, refresh: Boolean = false) {
        viewModelScope.launch {
            if (refresh || _uiState.value.notes.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null,
                    isNetworkError = false
                )
            }
            
            when (val result = notesRepository.getNotes(page, pageSize, refresh)) {
                is ApiResult.Success -> {
                    val notesData = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        notes = if (page == 1) notesData.results else _uiState.value.notes + notesData.results,
                        hasNextPage = notesData.next != null,
                        hasPreviousPage = notesData.previous != null,
                        currentPage = page,
                        totalCount = notesData.count,
                        errorMessage = null
                    )
                }
                is ApiResult.Error -> {
                    val isAuthError = handleAuthenticationError(result.message)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = if (isAuthError) "Session expired. Please log in again." else result.message,
                        isNetworkError = false
                    )
                    if (isAuthError) {
                        // Handle logout or token refresh logic if needed
                    }
                }
                is ApiResult.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        isNetworkError = true
                    )
                }
            }
        }
    }
    
    fun searchNotes(query: String, page: Int = 1, pageSize: Int = 20) {
        viewModelScope.launch {
            currentSearchQuery = query.takeIf { it.isNotBlank() }
            
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                isNetworkError = false
            )
            
            val result = notesRepository.searchNotes(
                title = currentSearchQuery,
                description = currentSearchQuery,
                page = page,
                pageSize = pageSize
            )
            
            when (result) {
                is ApiResult.Success -> {
                    val notesData = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        notes = if (page == 1) notesData.results else _uiState.value.notes + notesData.results,
                        hasNextPage = notesData.next != null,
                        hasPreviousPage = notesData.previous != null,
                        currentPage = page,
                        totalCount = notesData.count,
                        errorMessage = null
                    )
                }
                is ApiResult.Error -> {
                    val isAuthError = handleAuthenticationError(result.message)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = if (isAuthError) "Session expired. Please log in again." else result.message,
                        isNetworkError = false
                    )
                    if (isAuthError) {
                        // Handle logout or token refresh logic if needed
                    }
                }
                is ApiResult.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        isNetworkError = true
                    )
                }
            }
        }
    }
    
    fun loadNoteById(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                isNetworkError = false
            )
            
            when (val result = notesRepository.getNoteById(id)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentNote = result.data,
                        errorMessage = null
                    )
                }
                is ApiResult.Error -> {
                    val isAuthError = handleAuthenticationError(result.message)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = if (isAuthError) "Session expired. Please log in again." else result.message,
                        isNetworkError = false
                    )
                }
                is ApiResult.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        isNetworkError = true
                    )
                }
            }
        }
    }
    
    fun createNote(title: String, description: String, onSuccess: (NoteResponse) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null,
                isNetworkError = false
            )
            
            when (val result = notesRepository.createNote(title, description)) {
                is ApiResult.Success -> {
                    val newNote = result.data
                    
                    // Add the new note to the beginning of the list
                    val updatedNotes = listOf(newNote) + _uiState.value.notes
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        notes = updatedNotes,
                        totalCount = _uiState.value.totalCount + 1,
                        successMessage = "Note created successfully!",
                        errorMessage = null
                    )
                    onSuccess(newNote)
                }
                is ApiResult.Error -> {
                    val isAuthError = handleAuthenticationError(result.message)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = if (isAuthError) "Session expired. Please log in again." else result.message,
                        isNetworkError = false
                    )
                    if (isAuthError) {
                        // Handle logout or token refresh logic if needed
                    }
                }
                is ApiResult.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        isNetworkError = true
                    )
                }
            }
        }
    }
    
    fun updateNote(id: Int, title: String, description: String, onSuccess: (NoteResponse) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null,
                isNetworkError = false
            )
            
            // Pass the strings directly to allow empty values to be saved
            when (val result = notesRepository.updateNote(id, title, description)) {
                is ApiResult.Success -> {
                    val updatedNote = result.data
                    
                    // Update the note in the list
                    val updatedNotes = _uiState.value.notes.map { note ->
                        if (note.id == id) updatedNote else note
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        notes = updatedNotes,
                        currentNote = if (_uiState.value.currentNote?.id == id) updatedNote else _uiState.value.currentNote,
                        successMessage = "Note updated successfully!",
                        errorMessage = null
                    )
                    onSuccess(updatedNote)
                }
                is ApiResult.Error -> {
                    val isAuthError = handleAuthenticationError(result.message)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = if (isAuthError) "Session expired. Please log in again." else result.message,
                        isNetworkError = false
                    )
                    if (isAuthError) {
                        // Handle logout or token refresh logic if needed
                    }
                }
                is ApiResult.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        isNetworkError = true
                    )
                }
            }
        }
    }
    
    fun deleteNote(id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null,
                isNetworkError = false
            )
            
            when (val result = notesRepository.deleteNote(id)) {
                is ApiResult.Success -> {
                    // Remove the note from the list
                    val updatedNotes = _uiState.value.notes.filter { it.id != id }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        notes = updatedNotes,
                        currentNote = if (_uiState.value.currentNote?.id == id) null else _uiState.value.currentNote,
                        totalCount = maxOf(0, _uiState.value.totalCount - 1),
                        successMessage = "Note deleted successfully!",
                        errorMessage = null
                    )
                    onSuccess()
                }
                is ApiResult.Error -> {
                    val isAuthError = handleAuthenticationError(result.message)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = if (isAuthError) "Session expired. Please log in again." else result.message,
                        isNetworkError = false
                    )
                    if (isAuthError) {
                        // Handle logout or token refresh logic if needed
                    }
                }
                is ApiResult.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        isNetworkError = true
                    )
                }
            }
        }
    }
    

    
    fun loadNextPage() {
        if (_uiState.value.hasNextPage && !_uiState.value.isLoading) {
            if (currentSearchQuery != null) {
                searchNotes(currentSearchQuery!!, _uiState.value.currentPage + 1)
            } else {
                loadNotes(_uiState.value.currentPage + 1)
            }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            
            // Trigger background sync
            SyncWorker.scheduleImmediateSync(context)
            
            if (currentSearchQuery != null) {
                searchNotes(currentSearchQuery!!, 1)
            } else {
                loadNotes(1, refresh = true)
            }
            
            _uiState.value = _uiState.value.copy(isSyncing = false)
        }
    }
    
    // Add manual sync function
    fun syncNow() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            
            val result = notesRepository.syncWithServer()
            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        successMessage = "Sync completed successfully"
                    )
                    // Refresh current view
                    refresh()
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        errorMessage = result.message
                    )
                }
                is ApiResult.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        errorMessage = result.message,
                        isNetworkError = true
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            isNetworkError = false
        )
    }
    
    // Observe notes in real-time
    fun observeNotes() {
        viewModelScope.launch {
            notesRepository.getAllNotesFlow().collect { notes ->
                _uiState.value = _uiState.value.copy(
                    notes = notes,
                    totalCount = notes.size
                )
            }
        }
    }
    
    fun observeSearchResults(query: String) {
        viewModelScope.launch {
            notesRepository.searchNotesFlow(query).collect { notes ->
                _uiState.value = _uiState.value.copy(
                    notes = notes,
                    totalCount = notes.size
                )
            }
        }
    }
    
    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
    
    fun clearCurrentNote() {
        _uiState.value = _uiState.value.copy(currentNote = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        networkMonitor.unregister()
    }
}

// Extension functions to convert between API models and UI models
fun NoteResponse.toUiNote(): com.example.noteproject.ui.components.Note {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
    val fallbackFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    val lastEditedMillis = try {
        dateFormat.parse(this.updatedAt)?.time ?: fallbackFormat.parse(this.updatedAt)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
    
    return com.example.noteproject.ui.components.Note(
        id = this.id,
        header = this.title,
        body = this.description,
        lastEdited = lastEditedMillis
    )
}

fun com.example.noteproject.ui.components.Note.toApiNote(): CreateNoteRequest {
    return CreateNoteRequest(
        title = this.header,
        description = this.body
    )
}
