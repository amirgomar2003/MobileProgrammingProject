package com.example.noteproject.data.repository

import com.example.noteproject.data.api.ApiClient
import com.example.noteproject.data.local.TokenManager
import com.example.noteproject.data.model.*
import com.google.gson.Gson
import retrofit2.Response

class NotesRepository(
    private val apiClient: ApiClient,
    private val tokenManager: TokenManager,
    private val gson: Gson = Gson()
) {
    
    suspend fun getNotes(page: Int? = null, pageSize: Int? = null): ApiResult<PaginatedNotesResponse> {
        return try {
            if (!tokenManager.isLoggedIn()) {
                return ApiResult.Error("Not authenticated")
            }
            
            val response = apiClient.notesService.getNotes(page, pageSize)
            handleResponse(response) { it }
        } catch (e: Exception) {
            handleNetworkException(e)
        }
    }
    
    suspend fun getNoteById(id: Int): ApiResult<NoteResponse> {
        return try {
            if (!tokenManager.isLoggedIn()) {
                return ApiResult.Error("Not authenticated")
            }
            
            val response = apiClient.notesService.getNoteById(id)
            handleResponse(response) { it }
        } catch (e: Exception) {
            handleNetworkException(e)
        }
    }
    
    suspend fun createNote(title: String, description: String): ApiResult<NoteResponse> {
        return try {
            if (!tokenManager.isLoggedIn()) {
                return ApiResult.Error("Not authenticated")
            }
            
            val request = CreateNoteRequest(title, description)
            val response = apiClient.notesService.createNote(request)
            handleResponse(response) { it }
        } catch (e: Exception) {
            handleNetworkException(e)
        }
    }
    
    suspend fun updateNote(id: Int, title: String, description: String): ApiResult<NoteResponse> {
        return try {
            if (!tokenManager.isLoggedIn()) {
                return ApiResult.Error("Not authenticated")
            }
            
            // Pass non-null values to ensure fields are updated even if empty
            val request = UpdateNoteRequest(title, description)
            val response = apiClient.notesService.partialUpdateNote(id, request)
            handleResponse(response) { it }
        } catch (e: Exception) {
            handleNetworkException(e)
        }
    }
    
    suspend fun deleteNote(id: Int): ApiResult<Unit> {
        return try {
            if (!tokenManager.isLoggedIn()) {
                return ApiResult.Error("Not authenticated")
            }
            
            val response = apiClient.notesService.deleteNote(id)
            
            // Handle DELETE response which may have no content (204 status)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = errorBody ?: "Failed to delete note"
                ApiResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            handleNetworkException(e)
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
            if (!tokenManager.isLoggedIn()) {
                return ApiResult.Error("Not authenticated")
            }
            
            val response = apiClient.notesService.searchNotes(
                title, description, updatedGte, updatedLte, page, pageSize
            )
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
            val errorResponse = try {
                errorBody?.let { gson.fromJson(it, ErrorResponse::class.java) }
            } catch (e: Exception) {
                null
            }
            
            val errorMessage = parseNotesError(errorResponse, response.code())
            
            ApiResult.Error(errorMessage, errorResponse)
        }
    }
    
    private fun parseNotesError(errorResponse: ErrorResponse?, httpCode: Int): String {
        errorResponse?.errors?.forEach { error ->
            when {
                error.attr == "title" -> {
                    return when (error.code) {
                        "required" -> "Note title is required."
                        "max_length" -> "Note title is too long."
                        "blank" -> "Note title cannot be empty."
                        else -> "Invalid title: ${error.detail}"
                    }
                }
                error.attr == "description" -> {
                    return when (error.code) {
                        "required" -> "Note description is required."
                        "max_length" -> "Note description is too long."
                        else -> "Invalid description: ${error.detail}"
                    }
                }
                error.code == "permission_denied" -> {
                    return "You don't have permission to access this note."
                }
                error.code == "not_found" -> {
                    return "Note not found."
                }
                error.detail.isNotEmpty() -> {
                    return error.detail
                }
            }
        }
        
        // HTTP status code fallbacks
        return when (httpCode) {
            400 -> "Invalid request. Please check your information."
            401 -> "Authentication failed. Please log in again."
            403 -> "You don't have permission to perform this action."
            404 -> "Note not found."
            422 -> "Please check your input and try again."
            500 -> "Server error. Please try again later."
            503 -> "Service temporarily unavailable. Please try again later."
            else -> "An error occurred. Please try again."
        }
    }
    
    private fun <T> handleNetworkException(e: Exception): ApiResult<T> {
        return when {
            e.message?.contains("UnknownHostException") == true -> {
                ApiResult.NetworkError("No internet connection. Please check your network.")
            }
            e.message?.contains("ConnectException") == true -> {
                ApiResult.NetworkError("Cannot connect to server. Please try again later.")
            }
            e.message?.contains("SocketTimeoutException") == true -> {
                ApiResult.NetworkError("Connection timeout. Please check your internet connection.")
            }
            e.message?.contains("HttpException") == true -> {
                ApiResult.NetworkError("Server error. Please try again later.")
            }
            else -> {
                ApiResult.NetworkError("Network error: ${e.message}")
            }
        }
    }
}
