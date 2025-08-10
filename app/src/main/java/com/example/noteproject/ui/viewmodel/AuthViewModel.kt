package com.example.noteproject.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteproject.data.api.ApiClient
import com.example.noteproject.data.local.TokenManager
import com.example.noteproject.data.model.UserInfo
import com.example.noteproject.data.repository.ApiResult
import com.example.noteproject.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userInfo: UserInfo? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isNetworkError: Boolean = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val tokenManager = TokenManager(application)
    private val apiClient = ApiClient(tokenManager)
    private val authRepository = AuthRepository(apiClient, tokenManager)
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    init {
        // Check if user is already logged in
        checkLoginStatus()
    }
    
    private fun checkLoginStatus() {
        val isLoggedIn = authRepository.isLoggedIn()
        val userInfo = authRepository.getCachedUserInfo()
        
        _uiState.value = _uiState.value.copy(
            isLoggedIn = isLoggedIn,
            userInfo = userInfo
        )
        
        // If logged in, try to refresh user info
        if (isLoggedIn) {
            refreshUserInfo()
        }
    }
    
    fun login(username: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                errorMessage = null, 
                isNetworkError = false
            )
            
            when (val result = authRepository.login(username, password)) {
                is ApiResult.Success -> {
                    // Fetch user info immediately after successful login
                    when (val userInfoResult = authRepository.getUserInfo()) {
                        is ApiResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                userInfo = userInfoResult.data
                            )
                        }
                        else -> {
                            // Even if user info fetch fails, we're still logged in
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                userInfo = null
                            )
                        }
                    }
                    onSuccess()
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
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
    
    fun register(
        username: String,
        password: String,
        email: String,
        firstName: String,
        lastName: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                errorMessage = null, 
                isNetworkError = false
            )
            
            when (val result = authRepository.register(username, password, email, firstName, lastName)) {
                is ApiResult.Success -> {
                    // Fetch user info immediately after successful registration
                    when (val userInfoResult = authRepository.getUserInfo()) {
                        is ApiResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                userInfo = userInfoResult.data
                            )
                        }
                        else -> {
                            // Even if user info fetch fails, we're still logged in
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                userInfo = null
                            )
                        }
                    }
                    onSuccess()
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
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
    
    fun changePassword(oldPassword: String, newPassword: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                errorMessage = null, 
                successMessage = null,
                isNetworkError = false
            )
            
            when (val result = authRepository.changePassword(oldPassword, newPassword)) {
                is ApiResult.Success -> {
                    // Password changed successfully - user stays logged in
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Password changed successfully!"
                    )
                    onSuccess()
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
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
    
    private fun refreshUserInfo() {
        viewModelScope.launch {
            // Only try to refresh user info if we have a valid token
            if (authRepository.isLoggedIn()) {
                when (val result = authRepository.getUserInfo()) {
                    is ApiResult.Success -> {
                        _uiState.value = _uiState.value.copy(userInfo = result.data)
                    }
                    is ApiResult.Error -> {
                        // If we get an authentication error, try to refresh the token
                        if (result.message.contains("authentication", ignoreCase = true) || 
                            result.message.contains("token", ignoreCase = true)) {
                            tryRefreshToken()
                        }
                    }
                    is ApiResult.NetworkError -> {
                        // Network error, don't try to refresh token
                    }
                }
            }
        }
    }
    
    private fun tryRefreshToken() {
        viewModelScope.launch {
            when (val result = authRepository.refreshAccessToken()) {
                is ApiResult.Success -> {
                    // Token refreshed successfully, try to get user info again
                    refreshUserInfo()
                }
                is ApiResult.Error, is ApiResult.NetworkError -> {
                    // Refresh failed, logout the user
                    logout()
                }
            }
        }
    }
    
    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState(isLoggedIn = false)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null, 
            isNetworkError = false
        )
    }
    
    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

}
