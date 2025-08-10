package com.example.noteproject.data.model

import com.google.gson.annotations.SerializedName

// Request models
data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String
)

data class ChangePasswordRequest(
    @SerializedName("old_password")
    val oldPassword: String,
    @SerializedName("new_password")
    val newPassword: String
)

data class TokenRefreshRequest(
    val refresh: String
)

// Response models
data class TokenResponse(
    val access: String,
    val refresh: String
)

data class TokenRefreshResponse(
    val access: String
)

data class RegisterResponse(
    val username: String,
    val email: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String
)

data class ChangePasswordResponse(
    val detail: String
)

data class UserInfo(
    val id: Int,
    val username: String,
    val email: String,
    @SerializedName("first_name")
    val firstName: String?,
    @SerializedName("last_name")
    val lastName: String?
)

// Error response models
data class ErrorDetail(
    val code: String,
    val detail: String,
    val attr: String?
)

data class ErrorResponse(
    val type: String,
    val errors: List<ErrorDetail>
)
