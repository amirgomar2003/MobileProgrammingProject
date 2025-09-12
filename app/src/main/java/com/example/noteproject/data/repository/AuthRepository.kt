package com.example.noteproject.data.repository

import com.example.noteproject.data.api.ApiClient
import com.example.noteproject.data.local.TokenManager
import com.example.noteproject.data.model.*
import com.google.gson.Gson
import retrofit2.Response

sealed class ApiResult<T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error<T>(val message: String, val errorResponse: ErrorResponse? = null) : ApiResult<T>()
    data class NetworkError<T>(val message: String) : ApiResult<T>()
}

class AuthRepository(
    private val apiClient: ApiClient,
    private val tokenManager: TokenManager,
    private val gson: Gson = Gson()
) {
    
    suspend fun login(username: String, password: String): ApiResult<TokenResponse> {
        return try {
            val response = apiClient.authService.login(LoginRequest(username, password))
            handleResponse(response) { tokenResponse ->
                tokenManager.saveTokens(tokenResponse.access, tokenResponse.refresh)
                tokenResponse
            }
        } catch (e: Exception) {
            when {
                e.message?.contains("UnknownHostException") == true -> {
                    ApiResult.NetworkError("No internet connection. Please check your network.")
                }
                e.message?.contains("ConnectException") == true -> {
                    ApiResult.NetworkError("Cannot connect to server. Please try again later.")
                }
                e.message?.contains("SocketTimeoutException") == true -> {
                    ApiResult.NetworkError("Connection timeout. Please check your internet connection.")
                }
                else -> {
                    ApiResult.NetworkError("Network error: ${e.message}")
                }
            }
        }
    }
    
    suspend fun register(
        username: String,
        password: String,
        email: String,
        firstName: String,
        lastName: String
    ): ApiResult<TokenResponse> {
        return try {
            // Step 1: Register the user (this only creates the account)
            val registerResponse = apiClient.authService.register(
                RegisterRequest(username, password, email, firstName, lastName)
            )
            
            if (registerResponse.isSuccessful) {
                // Step 2: Auto-login after successful registration
                val loginResponse = apiClient.authService.login(LoginRequest(username, password))
                
                handleResponse(loginResponse) { tokenResponse ->
                    tokenManager.saveTokens(tokenResponse.access, tokenResponse.refresh)
                    tokenResponse
                }
            } else {
                val errorBody = registerResponse.errorBody()?.string()
                val errorResponse = try {
                    errorBody?.let { gson.fromJson(it, ErrorResponse::class.java) }
                } catch (e: Exception) {
                    null
                }
                
                val errorMessage = parseRegistrationError(errorResponse, registerResponse.code())
                
                ApiResult.Error(errorMessage, errorResponse)
            }
        } catch (e: Exception) {
            handleNetworkException(e)
        }
    }
    
    suspend fun changePassword(oldPassword: String, newPassword: String): ApiResult<String> {
        return try {
            // Check if user is authenticated
            if (!tokenManager.isLoggedIn()) {
                return ApiResult.Error("Not authenticated")
            }
            
            val response = apiClient.authService.changePassword(
                ChangePasswordRequest(oldPassword, newPassword)
            )
            handleResponse(response) { changePasswordResponse ->
                changePasswordResponse.detail
            }
        } catch (e: Exception) {
            handleNetworkException(e)
        }
    }
    
    suspend fun getUserInfo(): ApiResult<UserInfo> {
        return try {
            // Check if user is authenticated
            if (!tokenManager.isLoggedIn()) {
                return ApiResult.Error("Not authenticated")
            }
            
            val response = apiClient.authService.getUserInfo()
            handleResponse(response) { userInfo ->
                tokenManager.saveUserInfo(gson.toJson(userInfo))
                userInfo
            }
        } catch (e: Exception) {
            handleNetworkException(e)
        }
    }
    
    fun logout() {
        tokenManager.clearTokens()
    }
    
    suspend fun refreshAccessToken(): ApiResult<String> {
        return try {
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken.isNullOrBlank()) {
                return ApiResult.Error("No refresh token available")
            }
            
            val response = apiClient.authService.refreshToken(
                TokenRefreshRequest(refreshToken)
            )
            
            handleResponse(response) { tokenRefreshResponse ->
                // Save the new access token (keep the same refresh token)
                val oldRefreshToken = tokenManager.getRefreshToken() ?: ""
                tokenManager.saveTokens(tokenRefreshResponse.access, oldRefreshToken)
                tokenRefreshResponse.access
            }
        } catch (e: Exception) {
            handleNetworkException(e)
        }
    }
    
    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()
    
    fun getCachedUserInfo(): UserInfo? {
        return try {
            tokenManager.getUserInfo()?.let { userInfoJson ->
                gson.fromJson(userInfoJson, UserInfo::class.java)
            }
        } catch (e: Exception) {
            null
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
            
            val errorMessage = parseGenericError(errorResponse, response.code())
            
            ApiResult.Error(errorMessage, errorResponse)
        }
    }
    
    private fun parseRegistrationError(errorResponse: ErrorResponse?, httpCode: Int): String {
        errorResponse?.errors?.forEach { error ->
            when {
                error.attr == "username" && error.code == "unique" -> {
                    return "This username is already taken. Please choose a different username."
                }
                error.attr == "email" && error.code == "unique" -> {
                    return "This email address is already registered. Please use a different email or try logging in."
                }
                error.attr == "username" -> {
                    return when (error.code) {
                        "required" -> "Username is required."
                        "invalid" -> "Please enter a valid username."
                        "max_length" -> "Username is too long."
                        "min_length" -> "Username is too short."
                        else -> "Invalid username: ${error.detail}"
                    }
                }
                error.attr == "email" -> {
                    return when (error.code) {
                        "required" -> "Email address is required."
                        "invalid" -> "Please enter a valid email address."
                        else -> "Invalid email: ${error.detail}"
                    }
                }
                error.attr == "password" -> {
                    return when (error.code) {
                        "required" -> "Password is required."
                        "too_weak" -> "Password is too weak. Please choose a stronger password."
                        "min_length" -> "Password is too short. Please use at least 8 characters."
                        else -> "Invalid password: ${error.detail}"
                    }
                }
                error.attr == "first_name" -> {
                    return "Invalid first name: ${error.detail}"
                }
                error.attr == "last_name" -> {
                    return "Invalid last name: ${error.detail}"
                }
            }
        }
        
        // Fallback if no specific error found
        return when (httpCode) {
            400 -> "Please check your information and try again."
            409 -> "Some of the information you provided is already in use."
            500 -> "Server error. Please try again later."
            else -> "Registration failed. Please try again."
        }
    }
    
    private fun parseGenericError(errorResponse: ErrorResponse?, httpCode: Int): String {
        errorResponse?.errors?.forEach { error ->
            when {
                error.code == "no_active_account" -> {
                    return "Invalid username or password. Please check your credentials and try again."
                }
                error.code == "authentication_failed" -> {
                    return "Invalid username or password."
                }
                error.code == "invalid_token" -> {
                    return "Session expired. Please log in again."
                }
                error.code == "permission_denied" -> {
                    return "You don't have permission to perform this action."
                }
                error.attr == "old_password" -> {
                    return "Current password is incorrect."
                }
                error.attr == "new_password" -> {
                    return when (error.code) {
                        "too_weak" -> "New password is too weak. Please choose a stronger password."
                        "min_length" -> "New password is too short. Please use at least 8 characters."
                        else -> "Invalid new password: ${error.detail}"
                    }
                }
                error.detail.isNotEmpty() -> {
                    return error.detail
                }
            }
        }
        
        // HTTP status code fallbacks
        return when (httpCode) {
            400 -> "Invalid request. Please check your information."
            401 -> "Authentication failed. Please check your credentials."
            403 -> "Access denied."
            404 -> "Resource not found."
            409 -> "Conflict: The information provided is already in use."
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
    
    // Helper method to check if we have valid refresh token for offline mode
    fun canRefreshToken(): Boolean {
        val refreshToken = tokenManager.getRefreshToken()
        return !refreshToken.isNullOrBlank()
    }
    
    // Offline-aware refresh token method
    suspend fun refreshAccessTokenIfPossible(): ApiResult<String> {
        return if (canRefreshToken()) {
            refreshAccessToken()
        } else {
            ApiResult.Error("No refresh token available")
        }
    }
}
