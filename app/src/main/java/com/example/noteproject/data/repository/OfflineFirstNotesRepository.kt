package com.example.noteproject.data.repository

import android.content.Context
import android.util.Log
import com.example.noteproject.data.api.ApiClient
import com.example.noteproject.data.local.NotesDatabase
import com.example.noteproject.data.local.NetworkMonitor
import com.example.noteproject.data.local.TokenManager
import com.example.noteproject.data.local.dao.NoteDao
import com.example.noteproject.data.local.dao.PendingOperationDao
import com.example.noteproject.data.local.entity.*
import com.example.noteproject.data.model.*
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.Response

class OfflineFirstNotesRepository(
    private val context: Context,
    private val apiClient: ApiClient,
    private val tokenManager: TokenManager,
    private val database: NotesDatabase,
    private val networkMonitor: NetworkMonitor,
    private val gson: Gson = Gson()
) {
    
    private val noteDao: NoteDao = database.noteDao()
    private val pendingOperationDao: PendingOperationDao = database.pendingOperationDao()
    
    companion object {
        private const val TAG = "OfflineFirstNotesRepo"
        private const val PAGE_SIZE = 20
        private const val MAX_RETRY_COUNT = 3
    }
    
    // Flow-based data access for real-time updates
    fun getAllNotesFlow(): Flow<List<NoteResponse>> {
        return noteDao.getAllNotes().map { entities ->
            entities.map { it.toNoteResponse() }
        }
    }
    
    fun searchNotesFlow(query: String): Flow<List<NoteResponse>> {
        return noteDao.searchNotesFlow(query).map { entities ->
            entities.map { it.toNoteResponse() }
        }
    }
    
    fun getNoteByIdFlow(id: Int): Flow<NoteResponse?> {
        return noteDao.getNoteByIdFlow(id).map { entity ->
            entity?.toNoteResponse()
        }
    }
    
    // Pagination support for offline-first approach
    suspend fun getNotes(page: Int? = null, pageSize: Int? = null, refresh: Boolean = false): ApiResult<PaginatedNotesResponse> {
        return try {
            val actualPageSize = pageSize ?: PAGE_SIZE
            val currentPage = page ?: 1
            val offset = (currentPage - 1) * actualPageSize
            
            // Always try to refresh from server if online and refresh is requested
            if (refresh && networkMonitor.isConnected.value && tokenManager.isLoggedIn()) {
                syncFromServer(currentPage, actualPageSize)
            }
            
            // Get data from local database
            val localNotes = noteDao.getNotesPage(actualPageSize, offset)
            val totalCount = noteDao.getTotalNotesCount()
            val hasNext = (offset + actualPageSize) < totalCount
            val hasPrevious = currentPage > 1
            
            val response = PaginatedNotesResponse(
                count = totalCount,
                next = if (hasNext) "page=${currentPage + 1}" else null,
                previous = if (hasPrevious) "page=${currentPage - 1}" else null,
                results = localNotes.map { it.toNoteResponse() }
            )
            
            ApiResult.Success(response)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting notes", e)
            ApiResult.Error("Failed to load notes: ${e.message}")
        }
    }
    
    suspend fun getNoteById(id: Int): ApiResult<NoteResponse> {
        return try {
            val localNote = noteDao.getNoteById(id)
            if (localNote != null) {
                ApiResult.Success(localNote.toNoteResponse())
            } else {
                // Try to fetch from server if online
                if (networkMonitor.isConnected.value && tokenManager.isLoggedIn()) {
                    val serverResult = fetchNoteFromServer(id)
                    if (serverResult is ApiResult.Success) {
                        val noteEntity = serverResult.data.toNoteEntity()
                        noteDao.insertNote(noteEntity)
                        ApiResult.Success(serverResult.data)
                    } else {
                        ApiResult.Error("Note not found")
                    }
                } else {
                    ApiResult.Error("Note not found")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting note by ID", e)
            ApiResult.Error("Failed to load note: ${e.message}")
        }
    }
    
    suspend fun searchNotes(
        title: String? = null,
        description: String? = null,
        updatedGte: String? = null,
        updatedLte: String? = null,
        page: Int? = null,
        pageSize: Int? = null
    ): ApiResult<PaginatedNotesResponse> {
        return try {
            val query = title ?: description ?: ""
            val actualPageSize = pageSize ?: PAGE_SIZE
            val currentPage = page ?: 1
            val offset = (currentPage - 1) * actualPageSize
            
            // Search in local database
            val localResults = if (query.isNotBlank()) {
                noteDao.searchNotes(query, actualPageSize, offset)
            } else {
                noteDao.getNotesPage(actualPageSize, offset)
            }
            
            val totalCount = if (query.isNotBlank()) {
                noteDao.getSearchResultsCount(query)
            } else {
                noteDao.getTotalNotesCount()
            }
            
            val hasNext = (offset + actualPageSize) < totalCount
            val hasPrevious = currentPage > 1
            
            val response = PaginatedNotesResponse(
                count = totalCount,
                next = if (hasNext) "page=${currentPage + 1}" else null,
                previous = if (hasPrevious) "page=${currentPage - 1}" else null,
                results = localResults.map { it.toNoteResponse() }
            )
            
            ApiResult.Success(response)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching notes", e)
            ApiResult.Error("Failed to search notes: ${e.message}")
        }
    }
    
    suspend fun createNote(title: String, description: String): ApiResult<NoteResponse> {
        return try {
            if (networkMonitor.isConnected.value && tokenManager.isLoggedIn()) {
                // Online: Create directly on server
                val result = createNoteOnServer(title, description)
                if (result is ApiResult.Success) {
                    // Save to local database
                    val noteEntity = result.data.toNoteEntity()
                    noteDao.insertNote(noteEntity)
                    result
                } else {
                    // If server fails, create locally
                    createNoteLocally(title, description)
                }
            } else {
                // Offline: Create locally
                createNoteLocally(title, description)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating note", e)
            ApiResult.Error("Failed to create note: ${e.message}")
        }
    }
    
    suspend fun updateNote(id: Int, title: String, description: String): ApiResult<NoteResponse> {
        return try {
            val existingNote = noteDao.getNoteById(id)
            if (existingNote == null) {
                return ApiResult.Error("Note not found")
            }
            
            val updatedNote = existingNote.copy(
                title = title,
                description = description,
                updatedAt = System.currentTimeMillis(),
                isDirty = true,
                syncStatus = SyncStatus.PENDING_UPLOAD
            )
            
            noteDao.updateNote(updatedNote)
            
            if (networkMonitor.isConnected.value && tokenManager.isLoggedIn()) {
                // Try to sync immediately if online
                val serverResult = updateNoteOnServer(id, title, description)
                if (serverResult is ApiResult.Success) {
                    // Mark as synced
                    noteDao.markNoteSynced(id)
                } else {
                    // Keep as pending upload for later sync
                    addPendingOperation(PendingOperationEntity(
                        noteId = id,
                        operationType = OperationType.UPDATE,
                        title = title,
                        description = description
                    ))
                }
            } else {
                // Queue for later sync
                addPendingOperation(PendingOperationEntity(
                    noteId = id,
                    operationType = OperationType.UPDATE,
                    title = title,
                    description = description
                ))
            }
            
            ApiResult.Success(updatedNote.toNoteResponse())
        } catch (e: Exception) {
            Log.e(TAG, "Error updating note", e)
            ApiResult.Error("Failed to update note: ${e.message}")
        }
    }
    
    suspend fun deleteNote(id: Int): ApiResult<Unit> {
        return try {
            // Mark as deleted locally
            noteDao.markNoteAsDeleted(id, SyncStatus.PENDING_DELETE)
            
            if (networkMonitor.isConnected.value && tokenManager.isLoggedIn()) {
                // Try to delete on server immediately
                val serverResult = deleteNoteOnServer(id)
                if (serverResult is ApiResult.Success) {
                    // Permanently delete from local database
                    noteDao.deleteNote(id)
                } else {
                    // Queue for later sync
                    addPendingOperation(PendingOperationEntity(
                        noteId = id,
                        operationType = OperationType.DELETE,
                        title = null,
                        description = null
                    ))
                }
            } else {
                // Queue for later sync
                addPendingOperation(PendingOperationEntity(
                    noteId = id,
                    operationType = OperationType.DELETE,
                    title = null,
                    description = null
                ))
            }
            
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting note", e)
            ApiResult.Error("Failed to delete note: ${e.message}")
        }
    }
    
    // Sync operations
    suspend fun syncWithServer(): ApiResult<String> {
        if (!networkMonitor.isConnected.value || !tokenManager.isLoggedIn()) {
            return ApiResult.NetworkError("No network connection or not authenticated")
        }
        
        return try {
            // Process pending operations
            processPendingOperations()
            
            // Sync notes from server
            syncFromServer()
            
            ApiResult.Success("Sync completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            ApiResult.Error("Sync failed: ${e.message}")
        }
    }
    
    private suspend fun createNoteLocally(title: String, description: String): ApiResult<NoteResponse> {
        val noteEntity = noteDao.insertNoteWithTempId(title, description)
        
        // Queue for server sync
        addPendingOperation(PendingOperationEntity(
            noteId = null, // null for new notes
            operationType = OperationType.CREATE,
            title = title,
            description = description
        ))
        
        return ApiResult.Success(noteEntity.toNoteResponse())
    }
    
    private suspend fun syncFromServer(page: Int = 1, pageSize: Int = PAGE_SIZE) {
        try {
            val response = apiClient.notesService.getNotes(page, pageSize)
            if (response.isSuccessful) {
                response.body()?.let { paginatedResponse ->
                    val noteEntities = paginatedResponse.results.map { it.toNoteEntity() }
                    noteDao.insertNotes(noteEntities)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync from server", e)
        }
    }
    
    private suspend fun processPendingOperations() {
        val pendingOps = pendingOperationDao.getAllPendingOperations()
        
        for (operation in pendingOps) {
            try {
                when (operation.operationType) {
                    OperationType.CREATE -> {
                        operation.title?.let { title ->
                            operation.description?.let { description ->
                                val result = createNoteOnServer(title, description)
                                if (result is ApiResult.Success) {
                                    // Update local note with server ID
                                    val localNote = noteDao.getUnsyncedNotes().find { 
                                        it.title == title && it.description == description && it.isLocalOnly 
                                    }
                                    localNote?.let { note ->
                                        noteDao.deleteNote(note.id) // Remove temp note
                                        noteDao.insertNote(result.data.toNoteEntity()) // Insert with real ID
                                    }
                                    pendingOperationDao.deletePendingOperation(operation)
                                }
                            }
                        }
                    }
                    OperationType.UPDATE -> {
                        operation.noteId?.let { noteId ->
                            operation.title?.let { title ->
                                operation.description?.let { description ->
                                    val result = updateNoteOnServer(noteId, title, description)
                                    if (result is ApiResult.Success) {
                                        noteDao.markNoteSynced(noteId)
                                        pendingOperationDao.deletePendingOperation(operation)
                                    }
                                }
                            }
                        }
                    }
                    OperationType.DELETE -> {
                        operation.noteId?.let { noteId ->
                            val result = deleteNoteOnServer(noteId)
                            if (result is ApiResult.Success) {
                                noteDao.deleteNote(noteId)
                                pendingOperationDao.deletePendingOperation(operation)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to process pending operation", e)
                // Increment retry count
                pendingOperationDao.incrementRetryCount(operation.id)
                
                // Remove operation if max retries exceeded
                if (operation.retryCount >= MAX_RETRY_COUNT) {
                    pendingOperationDao.deletePendingOperation(operation)
                }
            }
        }
    }
    
    private suspend fun addPendingOperation(operation: PendingOperationEntity) {
        pendingOperationDao.insertPendingOperation(operation)
    }
    
    // Server API calls
    private suspend fun createNoteOnServer(title: String, description: String): ApiResult<NoteResponse> {
        return try {
            val request = CreateNoteRequest(title, description)
            val response = apiClient.notesService.createNote(request)
            handleResponse(response) { it }
        } catch (e: Exception) {
            handleNetworkException(e)
        }
    }
    
    private suspend fun updateNoteOnServer(id: Int, title: String, description: String): ApiResult<NoteResponse> {
        return try {
            val request = UpdateNoteRequest(title, description)
            val response = apiClient.notesService.updateNote(id, request)
            handleResponse(response) { it }
        } catch (e: Exception) {
            handleNetworkException(e)
        }
    }
    
    private suspend fun deleteNoteOnServer(id: Int): ApiResult<Unit> {
        return try {
            val response = apiClient.notesService.deleteNote(id)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Failed to delete note on server")
            }
        } catch (e: Exception) {
            handleNetworkException(e)
        }
    }
    
    private suspend fun fetchNoteFromServer(id: Int): ApiResult<NoteResponse> {
        return try {
            val response = apiClient.notesService.getNoteById(id)
            handleResponse(response) { it }
        } catch (e: Exception) {
            handleNetworkException(e)
        }
    }
    
    private fun <T, R> handleResponse(
        response: Response<T>,
        onSuccess: (T) -> R
    ): ApiResult<R> {
        return if (response.isSuccessful) {
            response.body()?.let { body ->
                ApiResult.Success(onSuccess(body))
            } ?: ApiResult.Error("Empty response body")
        } else {
            val errorBody = response.errorBody()?.string()
            ApiResult.Error(errorBody ?: "Server error")
        }
    }
    
    private fun <T> handleNetworkException(e: Exception): ApiResult<T> {
        return when {
            e.message?.contains("UnknownHostException") == true -> {
                ApiResult.NetworkError("No internet connection")
            }
            e.message?.contains("ConnectException") == true -> {
                ApiResult.NetworkError("Cannot connect to server")
            }
            e.message?.contains("SocketTimeoutException") == true -> {
                ApiResult.NetworkError("Connection timeout")
            }
            else -> {
                ApiResult.NetworkError("Network error: ${e.message}")
            }
        }
    }
}
