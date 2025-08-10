package com.example.noteproject.data.api

import com.example.noteproject.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {
    
    @POST("api/auth/token/")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>
    
    @POST("api/auth/register/")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
    
    @GET("api/auth/userinfo/")
    suspend fun getUserInfo(): Response<UserInfo>
    
    @POST("api/auth/change-password/")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ChangePasswordResponse>
    
    @POST("api/auth/token/refresh/")
    suspend fun refreshToken(@Body request: TokenRefreshRequest): Response<TokenRefreshResponse>
}
