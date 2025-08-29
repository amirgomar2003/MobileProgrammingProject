package com.example.noteproject.data.api

import com.example.noteproject.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient(private val tokenManager: TokenManager) {
    
    companion object {
        private const val BASE_URL = "http://10.0.2.2:8000/"  // Use 10.0.2.2 for Android emulator to access localhost
        private const val TIMEOUT_SECONDS = 30L
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    // Separate client for refresh calls (no auth interceptor to avoid circular dependency)
    private val refreshClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()
    
    private val refreshRetrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(refreshClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val refreshAuthService: AuthApiService = refreshRetrofit.create(AuthApiService::class.java)
    
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // Add authorization header if token exists and endpoint requires it
        val path = originalRequest.url.encodedPath
        if (requiresAuth(path)) {
            tokenManager.getAuthHeader()?.let { authHeader ->
                requestBuilder.addHeader("Authorization", authHeader)
            }
        }

        val response = chain.proceed(requestBuilder.build())
        
        // Handle 401 with automatic token refresh
        if (response.code == 401 && requiresAuth(path) && !path.contains("/api/auth/token/refresh/")) {
            // Try to refresh the token automatically
            val refreshToken = tokenManager.getRefreshToken()
            if (!refreshToken.isNullOrBlank()) {
                try {
                    // Make synchronous refresh call using separate client
                    val refreshResponse = runBlocking {
                        refreshAuthService.refreshToken(
                            com.example.noteproject.data.model.TokenRefreshRequest(refreshToken)
                        )
                    }
                    
                    if (refreshResponse.isSuccessful) {
                        refreshResponse.body()?.let { tokenRefreshResponse ->
                            // Save the new access token
                            tokenManager.saveTokens(tokenRefreshResponse.access, refreshToken)
                            
                            // Retry the original request with new token
                            val newRequest = originalRequest.newBuilder()
                                .header("Authorization", "Bearer ${tokenRefreshResponse.access}")
                                .build()
                            
                            // Close the original response and return the new one
                            response.close()
                            return@Interceptor chain.proceed(newRequest)
                        }
                    }
                } catch (e: Exception) {
                    // Refresh failed, continue with clearing tokens
                }
            }
            
            // If refresh failed or no refresh token, clear tokens
            tokenManager.clearTokens()
        }
        
        response
    }

    private fun requiresAuth(path: String): Boolean {
        return when {
            path.contains("/api/auth/userinfo/") -> true
            path.contains("/api/auth/change-password/") -> true
            path.contains("/api/notes/") -> true
            path.contains("/api/auth/token/refresh/") -> false
            path.contains("/api/auth/token/") -> false
            path.contains("/api/auth/register/") -> false
            else -> true // Default to requiring auth for other endpoints
        }
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val authService: AuthApiService = retrofit.create(AuthApiService::class.java)
    val notesService: NotesApiService = retrofit.create(NotesApiService::class.java)
}
