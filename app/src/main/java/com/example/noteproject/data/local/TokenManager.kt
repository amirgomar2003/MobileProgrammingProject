package com.example.noteproject.data.local

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val USER_INFO_KEY = "user_info"
    }
    
    fun saveTokens(accessToken: String, refreshToken: String) {
        if (accessToken.isNotBlank() && refreshToken.isNotBlank()) {
            prefs.edit()
                .putString(ACCESS_TOKEN_KEY, accessToken)
                .putString(REFRESH_TOKEN_KEY, refreshToken)
                .apply()
        }
    }
    
    fun getAccessToken(): String? = prefs.getString(ACCESS_TOKEN_KEY, null)
    
    fun getRefreshToken(): String? = prefs.getString(REFRESH_TOKEN_KEY, null)
    
    fun clearTokens() {
        prefs.edit()
            .remove(ACCESS_TOKEN_KEY)
            .remove(REFRESH_TOKEN_KEY)
            .remove(USER_INFO_KEY)
            .apply()
    }
    
    fun saveUserInfo(userInfoJson: String) {
        prefs.edit()
            .putString(USER_INFO_KEY, userInfoJson)
            .apply()
    }
    
    fun getUserInfo(): String? = prefs.getString(USER_INFO_KEY, null)
    
    fun isLoggedIn(): Boolean {
        val accessToken = getAccessToken()
        return !accessToken.isNullOrBlank()
    }
    
    fun getAuthHeader(): String? {
        val token = getAccessToken()
        return if (token?.isNotBlank() == true) {
            "Bearer $token"
        } else {
            null
        }
    }
}
